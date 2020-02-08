(ns java-after-eight-clj.main
  (:gen-class)
  (:require [clojure.string :as string]
            [clojure.java.io :as io]
            [clojure.pprint :as pp]
            [java-after-eight-clj.article
             :refer [parse-article-from-file]]
            [java-after-eight-clj.genealogy
             :refer [create-weights infer-relations procure]]
            [java-after-eight-clj.recommendation
             :refer [recommend]]
            [java-after-eight-clj.util :as util])
  (:import [java.io File]))


(def ^:private config-file-name ".recs.config")


(defn ^:private read-config
  [file]
  (try
    (-> (slurp file)
        string/split-lines)
    (catch Exception _)))


(defn ^:private read-project-config []
  (read-config (io/file (System/getProperty "user.dir") config-file-name)))


(defn ^:private read-user-config []
  (read-config (io/file (System/getProperty "user.home") config-file-name)))


(defn ^:private create-config
  [args]
  (let [[line1 line2] (or (not-empty args)
                          (read-project-config)
                          (read-user-config))]
    (when (nil? line1)
      (throw (ex-info "No article path defined." {})))
    (let [article-folder (io/file line1)
          output-file (some->> line2
                               (io/file (System/getProperty "user.dir")))]
      (when-not (.isDirectory article-folder)
        (throw (ex-info "Article path is no directory."
                        {:path line1})))
      (when (and output-file
                 (.exists output-file)
                 (not (.canWrite output-file)))
        (throw (ex-info "Output path is not writable."
                        {:path line2})))
      [article-folder output-file])))


(defn ^:private magic-genealogist-service-discovery
  [articles]
  ;; I don't know what the best equivalent to using ServiceLoader
  ;; would be in Clojure. That's beyond my experience level...
  ;; So I'll do this instead.
  (->> '[java-after-eight-clj.genealogists.clojure.silly-genealogist/silly-service
         java-after-eight-clj.genealogists.clojure.tag-genealogist/tag-service]
       (mapv requiring-resolve)
       (map deref)
       (mapv #(procure % articles))
       (filter some?)))


(defn ^:private recommendations->edn
  "Pretty prints the given value as edn to a string"
  [recommendations]
  (let [pretty-print-to-string #(with-out-str (pp/pprint %))]
    (->> recommendations
         ;; only keep relevant data for printing
         (mapv (fn [recommandation]
                 (-> recommandation
                     (update :article
                             select-keys [:title])
                     (update :recommendations
                             (partial mapv #(select-keys % [:title]))))))
         pretty-print-to-string)))


(defn ^:private recommendations->json
  [recommendations]
  (let [frame "[\n$RECOMMENDATIONS\n]"
        recommendation (str "\t{"
                            "\n\t\t\"title\": \"$TITLE\",\n"
                            "\t\t\"recommendations\": [\n"
                            "$RECOMMENDED_ARTICLES\n"
                            "\t\t]\n"
                            "\t}")
        recommended-article "\t\t\t{ \"title\": \"$TITLE\" }"]
    (->> recommendations
         (map
           (fn [rec]
             (let [articles
                   (->> rec
                        :recommendations
                        (map #(->>
                                (:title %)
                                (string/replace recommended-article "$TITLE")))
                        (string/join ",\n"))]
               (-> recommendation
                   (string/replace "$TITLE" (-> rec :article :title))
                   (string/replace "$RECOMMENDED_ARTICLES" articles)))))
         (string/join ",\n")
         (string/replace frame "$RECOMMENDATIONS"))))


(defn ^:private print-to-out
  [out recommendations]
  (let [serialize (if (and (instance? File out)
                           (-> ^File out .getName .toLowerCase (.endsWith ".edn")))
                    recommendations->edn
                    recommendations->json)]
    (spit out (serialize recommendations))))


(defn clj-main
  [args]
  (println (util/process-details))
  (let [[^File article-folder
         ^File output-file] (create-config args)
        articles (->> (.listFiles article-folder)
                      seq
                      (filter #(.isFile ^File %))
                      (filter #(-> ^File % .getName (.endsWith ".md")))
                      (pmap parse-article-from-file)
                      vec)
        genealogists (magic-genealogist-service-discovery articles)
        weights (create-weights)]
    (->> (infer-relations articles genealogists weights)
         (recommend 3)
         (print-to-out (or output-file *out*)))))



(defmacro bench [name repetitions & body]
  `(let [time# (volatile! 0)]
     (dotimes [n# ~repetitions]
       (let [start# (System/currentTimeMillis)]
         ~@body
         (vswap! time# + (- (System/currentTimeMillis) start#))))
     (println ~name "- avg:" (quot @time# ~repetitions) "ms")))


(defn -main
  [& [articles-file
      java-output-file
      clj-output-file]]
  (let [java-args (into-array [articles-file java-output-file])
        clj-args [articles-file (or clj-output-file java-output-file)]]

    (bench "   Warmup Java" 50
           (org.codefx.java_after_eight.Main/main java-args))
    (bench "Warmup Clojure" 50
           (clj-main clj-args))

    (bench "   Java" 50
           (org.codefx.java_after_eight.Main/main java-args))
    (bench "Clojure" 50
           (clj-main clj-args)))
  (shutdown-agents))


(comment
  (-main "articles" "recommendations.json" "clj-recommendations.edn")
  (-main "articles" "recommendations.json" "clj-recommendations.json")

  (time (clj-main ["articles" "recommendations.edn"]))
  (time (clj-main ["articles" "recommendations.json"]))

  )

