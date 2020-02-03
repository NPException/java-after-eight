(ns java-after-eight-clj.genealogists.java.silly-genealogist-service
  ;; this piece of the namespace definition will cause the namespace to be
  ;; compiled into a class of the given name, implementing the given
  ;; interfaces. A no-args constructor is provided by default, unless
  ;; specified otherwise.
  ;; All functions that start with the given prefix will be compiled to
  ;; proper java methods.
  (:gen-class
    :name java_after_eight_clj.genealogists.java.SillyGenealogistService
    :implements [org.codefx.java_after_eight.genealogist.GenealogistService]
    :prefix "java-"
    :main false)

  (:import [org.codefx.java_after_eight.article Article]
           [org.codefx.java_after_eight.genealogist Genealogist RelationType TypedRelation]))

(def ^RelationType TYPE (RelationType/from "silly"))

(defn ^:private title-letters
  [^Article article]
  (-> article
      .title
      .text
      .toLowerCase
      set))

(defn ^:private round
  ^long [^double x]
  (Math/round x))


(defn java-procure
  [articles]
  (reify Genealogist
    (infer [this article1 article2]
      (let [article1-letters (title-letters article1)
            article2-letters (title-letters article2)
            intersection (filter article1-letters article2-letters)
            score (round (/ (* 100.0 (count intersection))
                            (count article1-letters)))]
        (TypedRelation/from article1 article2 TYPE score)))))
