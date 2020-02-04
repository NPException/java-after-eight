(ns java-after-eight-clj.genealogists.clojure.silly-genealogist
  (:require [java-after-eight-clj.genealogy :as g]
            [java-after-eight-clj.util :refer [infix]]
            [clojure.string :as string]))


(defn ^:private letters
  [article]
  (-> article :title string/lower-case set))


(def ^:private silly-boy
  (reify g/Genealogist
    (infer-typed-relation [_ article-1 article-2]
      (let [letters-1 (letters article-1)
            letters-2 (letters article-2)
            overlap (->> letters-1
                         (filter letters-2)
                         count)
            score (infix
                    (100.0 * overlap / (count letters-1)))]
        (g/create-typed-relation
          article-1 article-2 :silly score)))))


(def silly-service
  (reify g/GenealogistService
    (procure [_ articles] silly-boy)))