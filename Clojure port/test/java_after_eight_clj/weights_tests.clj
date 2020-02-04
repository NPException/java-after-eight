(ns java-after-eight-clj.weights-tests
  (:require [clojure.test :refer :all]
            [java-after-eight-clj.test-helper :refer :all]
            [java-after-eight-clj.genealogy :as g]))

(def tag-type (g/validate-relation-type "tag"))
(def list-type (g/validate-relation-type "list"))

(def weights (g/create-weights {tag-type 0.42} 0.5))

(deftest nullRelationType_throwsException
  (is (thrown?
        NullPointerException
        (g/create-weights {nil 1.0} 0.5))))


(deftest nullWeight_throwsException
  (is (thrown?
        NullPointerException
        (g/create-weights {tag-type nil} 0.5))))


(deftest knownRelationType_returnsWeight
  (is (= 0.42 (weights tag-type))))


(deftest unknownRelationType_returnsDefaultWeight
  (is (= 0.5 (weights list-type))))
