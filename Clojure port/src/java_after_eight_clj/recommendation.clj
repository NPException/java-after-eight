(ns java-after-eight-clj.recommendation
  (:require [java-after-eight-clj.util :as util]))


(defrecord Recommendation [article recommendations])

(defn ^:private create-recommendation
  [article sorted-recommendations per-article]
  (->Recommendation
    (util/assert-not-nil article)
    (take per-article (util/assert-not-nil sorted-recommendations))))


(defn recommend
  [per-article relations]
  (when (< per-article 1)
    (throw
      (ex-info
        "Number of recommendations per article must be greater zero"
        {:relations relations
         :num       per-article})))

  (->> relations
       (sort-by (juxt #(-> % :article-1 :slug)
                      :score)
                util/reverse-comparator)
       (group-by :article-1)
       (mapv (fn [[article relations]]
               (create-recommendation
                 article
                 (map :article-2 relations)
                 #_(map #(assoc (:article-2 %) :score (:score %)) relations)
                 per-article)))))