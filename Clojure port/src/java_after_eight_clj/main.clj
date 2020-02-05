(ns java-after-eight-clj.main
  (:require [clojure.string :as string]
            [clojure.java.io :as io]
            [clojure.pprint :as pp]
            [clojure.edn :as edn]
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
  (let [[line1 line2 line3] (or (not-empty args)
                                (read-project-config)
                                (read-user-config))]
    (when (nil? line1)
      (throw (ex-info "No article path defined." {})))
    (when (nil? line2)
      (throw (ex-info "No genealogists services file defined." {})))
    (let [article-folder (io/file line1)
          genealogists-file (io/file line2)
          output-file (some->> line3
                               (io/file (System/getProperty "user.dir")))]
      (when-not (.isDirectory article-folder)
        (throw (ex-info "Article path is no directory."
                        {:path line1})))
      (when-not (.canRead genealogists-file)
        (throw (ex-info "Genealogists services file not readable."
                        {:path line2})))
      (when (and output-file
                 (.exists output-file)
                 (not (.canWrite output-file)))
        (throw (ex-info "Output path is not writable."
                        {:path line3})))
      [article-folder genealogists-file output-file])))


(defn ^:private get-genealogists
  [genealogists-file articles]
  ;; I don't know what the best equivalent to using ServiceLoader
  ;; would be in Clojure. That's beyond my experience level...
  ;; So I'll do this instead.
  (->> (slurp genealogists-file)
       edn/read-string
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


(defn -main [& args]
  (println (util/process-details))
  (let [[^File article-folder
         ^File genealogists-file
         ^File output-file] (create-config args)
        articles (->> (.listFiles article-folder)
                      seq
                      (filter #(.isFile ^File %))
                      (filter #(-> ^File % .getName (.endsWith ".md")))
                      (mapv parse-article-from-file))
        genealogists (get-genealogists genealogists-file articles)
        weights (create-weights)]
    (->> (infer-relations articles genealogists weights)
         (recommend 3)
         (print-to-out (or output-file *out*)))))


(comment
  (-main "articles" "Clojure port/genealogists.edn" "recommendations.edn")
  (-main "articles" "Clojure port/genealogists.edn" "recommendations.json"))
