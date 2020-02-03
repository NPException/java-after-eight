(ns java-after-eight-clj.genealogists.java.tag-genealogist-service
  (:gen-class
    :name java_after_eight_clj.genealogists.java.TagGenealogistService
    :implements [org.codefx.java_after_eight.genealogist.GenealogistService]
    :prefix "java-"
    :main false)

  (:import [org.codefx.java_after_eight.article Article]
           [org.codefx.java_after_eight.genealogist Genealogist RelationType TypedRelation]
           [java.util.stream Stream]))

(def ^RelationType TYPE (RelationType/from "tag"))

(defn ^:private stream-seq
  "Converts Java Stream to a Clojure sequence."
  [^Stream x]
  (-> x .iterator iterator-seq))

(defn ^:private get-tags
  [^Article article]
  (-> article .tags stream-seq set))

(defn ^:private round
  ^long [^double x]
  (Math/round x))


(defn java-procure
  [articles]
  (reify Genealogist
    (infer [this article1 article2]
      (let [article1-tags (get-tags article1)
            article2-tags (get-tags article2)
            number-of-shared-tags (count (filter article2-tags article1-tags))
            score (round (/ (* 100.0 2 number-of-shared-tags)
                            (+ (count article1-tags)
                               (count article2-tags))))]
        (TypedRelation/from article1 article2 TYPE score)))))
