(ns java-after-eight-clj.test-helper
  (:require [java-after-eight-clj.article :as a]
            [java-after-eight-clj.genealogy :as g])
  (:import [java.time LocalDate]))

;; workaround for an annoying "thrown? cannot be resolved"
;; warning in the IntelliJ Cursive plugin
(declare thrown? thrown-with-msg?)


(defn round [n]
  (Math/round (double n)))


(defn create-article-with-slug
  [slug]
  {:title       (@#'a/create-title "Title"),
   :tags        (@#'a/create-tags "[Tag]"),
   :date        (LocalDate/now),
   :description (@#'a/create-description "description"),
   :slug        (@#'a/create-slug slug),
   :content-fn  (constantly [""])})


(defn create-relation
  [article-1 article-2 ^long score]
  (g/->Relation article-1 article-2 score))


(defn create-genealogist
  [relation-type score-fn]
  (reify g/Genealogist
    (infer-typed-relation [_ article-1 article-2]
      (g/create-typed-relation
        article-1 article-2
        relation-type
        (score-fn article-1 article-2)))))
