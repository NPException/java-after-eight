(ns java-after-eight-clj.test-helper
  (:require [java-after-eight-clj.article :as a]
            [java-after-eight-clj.genealogy :as g]
            [java-time :as t]))

;; workaround for an annoying "thrown? cannot be resolved"
;; warning in the IntelliJ Cursive plugin
(declare thrown? thrown-with-msg?)


(defn create-article-with-slug
  [slug]
  {:title       (@#'a/create-title "Title"),
   :tags        (@#'a/create-tags "[Tag]"),
   :date        (t/local-date),
   :description (@#'a/create-description "description"),
   :slug        (@#'a/create-slug slug),
   :content-fn  (constantly [""])})


(defn create-relation
  [article-1 article-2 ^long score]
  (@#'g/create-relation [article-1 article-2] score))


(defn create-genealogist
  [relation-type score-fn]
  (reify g/Genealogist
    (infer-typed-relation [_ article-1 article-2]
      (g/create-typed-relation
        article-1 article-2
        relation-type
        (score-fn article-1 article-2)))))
