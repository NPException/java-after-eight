(ns java-after-eight-clj.relation-tests
  (:require [clojure.test :refer :all]
            [java-after-eight-clj.test-helper :refer :all]
            [java-after-eight-clj.genealogy :as g]))

(def TAG_WEIGHT 1.0)
(def LINK_WEIGHT 0.25)

(def articleA (create-article-with-slug "a"))
(def articleB (create-article-with-slug "b"))

(def tag-relation (g/validate-relation-type "tag"))
(def link-relation (g/validate-relation-type "link"))

(def weights (g/create-weights {tag-relation  TAG_WEIGHT
                                link-relation LINK_WEIGHT}
                               0.5))


(deftest singleTypedRelation_weightOne_sameArticlesAndScore
  (let [score 60
        relation (@#'g/aggregate-relation
                   [(g/create-typed-relation
                      articleA articleB tag-relation score)]
                   weights)]
    (is (= (-> relation :articles first)
           articleA))
    (is (= (-> relation :articles second)
           articleB))
    (is (= score (:score relation)))))


(deftest twoTypedRelation_weightOne_averagedScore
  (let [relation (@#'g/aggregate-relation
                   [(g/create-typed-relation
                      articleA articleB tag-relation 40)
                    (g/create-typed-relation
                      articleA articleB tag-relation 80)]
                   weights)]
    (is (= (/ (+ 40 80) 2)
           (:score relation)))))


(deftest twoTypedRelation_differingWeight_weightedScore
  (let [relation (@#'g/aggregate-relation
                   [(g/create-typed-relation
                      articleA articleB tag-relation 40)
                    (g/create-typed-relation
                      articleA articleB link-relation 80)]
                   weights)]
    (is (= (round (/ (+ (* 40 TAG_WEIGHT)
                        (* 80 LINK_WEIGHT))
                     2))
           (:score relation)))))
