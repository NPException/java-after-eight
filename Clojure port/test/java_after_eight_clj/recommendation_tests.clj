(ns java-after-eight-clj.recommendation-tests
  (:require [clojure.test :refer :all]
            [java-after-eight-clj.test-helper :refer :all]
            [java-after-eight-clj.recommendation :as r]))

;; convenience function
(defn create-recommendation
  [article recommended-articles]
  (@#'r/create-recommendation
    article
    recommended-articles
    (count recommended-articles)))


(def articleA (create-article-with-slug "a"))
(def articleB (create-article-with-slug "b"))
(def articleC (create-article-with-slug "c"))

(def relation-AB (create-relation articleA, articleB, 60))
(def relation-AC (create-relation articleA, articleC, 40))
(def relation-BA (create-relation articleB, articleA, 50))
(def relation-BC (create-relation articleB, articleC, 70))
(def relation-CA (create-relation articleC, articleA, 80))
(def relation-CB (create-relation articleC, articleB, 60))


(deftest forOneArticle_oneRelation
  (is (= (set (r/recommend 1 [relation-AC]))
         #{(create-recommendation articleA [articleC])})))


(deftest forOneArticle_twoRelations
  (is (= (set (r/recommend 1 [relation-AB relation-AC]))
         #{(create-recommendation articleA [articleB])})))


(deftest forManyArticles_oneRelationEach
  (is (= (set (r/recommend 1 [relation-AC relation-BC relation-CB]))
         #{(create-recommendation articleA [articleC])
           (create-recommendation articleB [articleC])
           (create-recommendation articleC [articleB])})))


(deftest forManyArticles_twoRelationsEach
  (is (= (set (r/recommend 1 [relation-AB relation-AC
                              relation-BA relation-BC
                              relation-CA relation-CB]))
         #{(create-recommendation articleA [articleB])
           (create-recommendation articleB [articleC])
           (create-recommendation articleC [articleA])})))
