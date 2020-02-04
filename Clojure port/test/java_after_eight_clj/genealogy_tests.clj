(ns java-after-eight-clj.genealogy-tests
  (:require [clojure.test :refer :all]
            [java-after-eight-clj.test-helper :refer :all]
            [java-after-eight-clj.genealogy :as g]))

;; TEST CONSTANTS

(def TAG_SCORE_A_B 80)
(def TAG_SCORE_A_C 60)
(def TAG_SCORE_B_A 70)
(def TAG_SCORE_B_C 50)
(def TAG_SCORE_C_A 50)
(def TAG_SCORE_C_B 40)
(def LINK_SCORE_A_B 60)
(def LINK_SCORE_A_C 40)
(def LINK_SCORE_B_A 50)
(def LINK_SCORE_B_C 30)
(def LINK_SCORE_C_A 30)
(def LINK_SCORE_C_B 20)
(def TAG_WEIGHT 1.0)
(def LINK_WEIGHT 0.75)

(def articleA (create-article-with-slug "a"))
(def articleB (create-article-with-slug "b"))
(def articleC (create-article-with-slug "c"))

(def tag-relation (@#'g/validate-relation-type "tag"))
(def link-relation (@#'g/validate-relation-type "link"))


(defn build-score-function
  [score-AB score-AC score-BA score-BC score-CA score-CB]
  (let [scores {[articleA articleB] score-AB
                [articleA articleC] score-AC
                [articleB articleA] score-BA
                [articleB articleC] score-BC
                [articleC articleA] score-CA
                [articleC articleB] score-CB}]
    (fn [article1 article2]
      (if (= article1 article2)
        100
        (get scores [article1 article2] 0)))))


(def tag-score
  (build-score-function
    TAG_SCORE_A_B TAG_SCORE_A_C
    TAG_SCORE_B_A TAG_SCORE_B_C
    TAG_SCORE_C_A TAG_SCORE_C_B))

(def link-score
  (build-score-function
    LINK_SCORE_A_B LINK_SCORE_A_C
    LINK_SCORE_B_A LINK_SCORE_B_C
    LINK_SCORE_C_A LINK_SCORE_C_B))


(def tag-genealogist
  (create-genealogist tag-relation tag-score))

(def link-genealogist
  (create-genealogist link-relation link-score))

(def weights
  (g/create-weights
    {tag-relation  TAG_WEIGHT
     link-relation LINK_WEIGHT}
    0.5))


;; TESTS

(deftest oneGenealogist_twoArticles
  (is (every?
        (set (g/infer-relations
               [articleA articleB]
               [tag-genealogist]
               weights))
        [(create-relation
           articleA articleB
           (round (* TAG_SCORE_A_B TAG_WEIGHT)))
         (create-relation
           articleB articleA
           (round (* TAG_SCORE_B_A TAG_WEIGHT)))])))


(deftest otherGenealogist_twoArticles
  (is (every?
        (set (g/infer-relations
               [articleA articleB]
               [link-genealogist]
               weights))
        [(create-relation
           articleA articleB
           (round (* LINK_SCORE_A_B LINK_WEIGHT)))
         (create-relation
           articleB articleA
           (round (* LINK_SCORE_B_A LINK_WEIGHT)))])))


(deftest oneGenealogist_threeArticles
  (is (every?
        (set (g/infer-relations
               [articleA articleB articleC]
               [tag-genealogist]
               weights))
        [(create-relation
           articleA articleB
           (round (* TAG_SCORE_A_B TAG_WEIGHT)))
         (create-relation
           articleA articleC
           (round (* TAG_SCORE_A_C TAG_WEIGHT)))
         (create-relation
           articleB articleA
           (round (* TAG_SCORE_B_A TAG_WEIGHT)))
         (create-relation
           articleB articleC
           (round (* TAG_SCORE_B_C TAG_WEIGHT)))
         (create-relation
           articleC articleA
           (round (* TAG_SCORE_C_A TAG_WEIGHT)))
         (create-relation
           articleC articleB
           (round (* TAG_SCORE_C_B TAG_WEIGHT)))])))


(deftest twoGenealogists_threeArticles
  (is (every?
        (set (g/infer-relations
               [articleA articleB articleC]
               [tag-genealogist link-genealogist]
               weights))
        [(create-relation
           articleA articleB
           (round
             (/ (+ (* TAG_SCORE_A_B TAG_WEIGHT)
                   (* LINK_SCORE_A_B LINK_WEIGHT))
                2)))
         (create-relation
           articleA articleC
           (round
             (/ (+ (* TAG_SCORE_A_C TAG_WEIGHT)
                   (* LINK_SCORE_A_C LINK_WEIGHT))
                2)))
         (create-relation
           articleB articleA
           (round
             (/ (+ (* TAG_SCORE_B_A TAG_WEIGHT)
                   (* LINK_SCORE_B_A LINK_WEIGHT))
                2)))
         (create-relation
           articleB articleC
           (round
             (/ (+ (* TAG_SCORE_B_C TAG_WEIGHT)
                   (* LINK_SCORE_B_C LINK_WEIGHT))
                2)))
         (create-relation
           articleC articleA
           (round
             (/ (+ (* TAG_SCORE_C_A TAG_WEIGHT)
                   (* LINK_SCORE_C_A LINK_WEIGHT))
                2)))
         (create-relation
           articleC articleB
           (round
             (/ (+ (* TAG_SCORE_C_B TAG_WEIGHT)
                   (* LINK_SCORE_C_B LINK_WEIGHT))
                2)))])))
