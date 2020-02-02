(ns java-after-eight-clj.recommendation-tests
  (:require [clojure.test :refer :all]
            [java-after-eight-clj.test-helper :refer :all]
            [java-after-eight-clj.recommendation :as r]))

(defn ^:private create-recommendation
  [article recommended-articles]
  (@#'r/create-recommendation
    article
    recommended-articles
    (count recommended-articles)))


(def ^:private articleA (create-article-with-slug "a"))
(def ^:private articleB (create-article-with-slug "b"))
(def ^:private articleC (create-article-with-slug "c"))

(def ^:private relation-AB (create-relation articleA, articleB, 60))
(def ^:private relation-AC (create-relation articleA, articleC, 40))
(def ^:private relation-BA (create-relation articleB, articleA, 50))
(def ^:private relation-BC (create-relation articleB, articleC, 70))
(def ^:private relation-CA (create-relation articleC, articleA, 80))
(def ^:private relation-CB (create-relation articleC, articleB, 60))


(deftest forOneArticle_oneRelation
  (is (= (set (r/recommend [relation-AC] 1))
         #{(create-recommendation articleA [articleC])})))


(deftest forOneArticle_twoRelations
  (is (= (set (r/recommend [relation-AB relation-AC] 1))
         #{(create-recommendation articleA [articleB])})))


(deftest forManyArticles_oneRelationEach
  (is (= (set (r/recommend [relation-AC relation-BC relation-CB] 1))
         #{(create-recommendation articleA [articleC])
           (create-recommendation articleB [articleC])
           (create-recommendation articleC [articleB])})))


(deftest forManyArticles_twoRelationsEach
  (is (= (set (r/recommend [relation-AB relation-AC
                            relation-BA relation-BC
                            relation-CA relation-CB]
                           1))
         #{(create-recommendation articleA [articleB])
           (create-recommendation articleB [articleC])
           (create-recommendation articleC [articleA])})))
