(ns java-after-eight-clj.genealogists.clojure.tag-genealogist
  (:require [java-after-eight-clj.genealogy :as g]
            [java-after-eight-clj.util :refer [infix]]))

(def ^:private tag-team
  (reify g/Genealogist
    (infer-typed-relation [_ article-1 article-2]
      (let [article-1-tags (:tags article-1)
            article-2-tags (:tags article-2)
            overlap-count (->> article-1-tags
                               (filter article-2-tags)
                               count)
            score (infix
                    (100.0 * 2 * overlap-count
                      / ((count article-1-tags) + (count article-2-tags))))]
        (g/create-typed-relation article-1 article-1 :tag score)))))


(def tag-service
  (reify g/GenealogistService
    (procure [_ articles] tag-team)))
