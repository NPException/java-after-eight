(ns java-after-eight-clj.recommendation
  (:require [java-after-eight-clj.util :as util]))


(defn ^:private create-recommendation
  [article sorted-recommendations per-article]
  {:article         (util/assert-not-nil article)
   :recommendations (take per-article (util/assert-not-nil sorted-recommendations))})


(defn recommend
  [per-article relations]
  (when (< per-article 1)
    (throw
      (ex-info
        "Number of recommendations per article must be greater zero"
        {:relations relations
         :num       per-article})))

  (->> relations
       (sort-by (juxt #(-> % :articles first :slug)
                      :score)
                util/reverse-comparator)
       (group-by #(-> % :articles first))
       (mapv (fn [[article relations]]
               (create-recommendation
                 article
                 (map #(-> % :articles second) relations)
                 #_(map #(assoc (second (:articles %)) :score (:score %)) relations)
                 per-article)))))