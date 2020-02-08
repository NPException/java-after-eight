(ns java-after-eight-clj.recommendation
  (:require [java-after-eight-clj.util :as util])
  (:import [java.util Comparator]))


(defrecord Recommendation [article recommendations])

(defn ^:private create-recommendation
  [article sorted-recommendations per-article]
  (->Recommendation
    (util/assert-not-nil article)
    (take per-article (util/assert-not-nil sorted-recommendations))))


(def ^:private by-score-then-title
  (-> (Comparator/comparing (util/jFunction :score))
      .reversed
      (.thenComparing (util/jFunction #(-> % :article-2 :title)))))


(defn recommend
  [per-article relations]
  (when (< per-article 1)
    (throw
      (ex-info
        "Number of recommendations per article must be greater zero"
        {:relations relations
         :num       per-article})))
  (->> relations
       (group-by :article-1)
       (sort-by #(-> % key :title))
       (mapv (fn [[article relations]]
               (create-recommendation
                 article
                 (->> relations
                      (sort by-score-then-title)
                      (map :article-2)
                      #_(map #(assoc (:article-2 %) :score (:score %))))
                 per-article)))))