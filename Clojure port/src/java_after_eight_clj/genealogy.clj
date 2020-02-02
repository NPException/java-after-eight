(ns java-after-eight-clj.genealogy
  (:require [java-after-eight-clj.util :as util]))


(defprotocol Genealogist
  (infer-typed-relation [this article-1 article-2]))

(defprotocol GenealogistService
  (procure ^Genealogist [this articles]))



(defn create-relation-type
  [value]
  (util/assert-not-empty value "Relation types can't have an empty value."))


;; org.codefx.java_after_eight.genealogy.Weights
(defn create-weights
  "Returns a function which will return a weight value for
  a provided genealogist type, or return the default weight
  if no weight is specified for the genealogist type."
  ;; equivalent of Weights.allEqual()
  ([] (constantly 1.0))
  ;; equivalent of Weights.from(..)
  ([weights-map ^double default-weight]
   (when-not (every? some? (flatten (seq weights-map)))
     (throw
       (NullPointerException.
         "Neither relation type nor weight can be null.")))
   (fn [genealogist-type]
     (get weights-map genealogist-type default-weight))))


(defn create-typed-relation
  [article-1 article-2 relation-type score]
  (when-not (<= 0 score 100)
    (throw (ex-info "Score should be in interval [0; 100]" {:score score})))
  {:articles [(util/assert-not-nil article-1)
              (util/assert-not-nil article-2)]
   :type     (util/assert-not-nil relation-type)
   :score    score})


(defn ^:private create-relation
  [articles score]
  (when-not (<= 0 score 100)
    (throw (ex-info "Score should be in interval [0; 100]" {:score score})))
  {:articles articles
   :score    score})


(defn ^:private create-unfinished-relation
  [{:keys [articles score] :as typed-relation} ^double weight]
  {:articles    articles
   :score-total (* score weight)
   :score-count 1})


(defn ^:private fold-unfinished-relations
  [ur-1 ur-2]
  (when (not= (:articles ur-1) (:articles ur-2))
    (ex-info "All typed relations must belong to the same article."
             {:unfinished-relation-1 ur-1
              :unfinished-relation-2 ur-2}))
  (-> ur-1
      (update :score-total + (:score-total ur-2))
      (update :score-count + (:score-count ur-2))))


;; equivalent of Relation.aggregate
(defn ^:private aggregate-relation
  [typed-relations weights]
  (util/assert-not-empty
    typed-relations "Can't create relation from zero typed relations.")
  (let [{:keys [articles score-total score-count]}
        (->> typed-relations
             (map #(create-unfinished-relation % (weights (:type %))))
             (reduce fold-unfinished-relations))]
    (create-relation
      articles
      (Math/round ^double (/ score-total score-count)))))


(defn ^:private infer-typed-relations
  [articles genealogists]
  (for [a1 articles
        a2 articles
        g genealogists]
    (infer-typed-relation g a1 a2)))


(defn ^:private aggregate-typed-relations
  [typed-relations weights]
  (->> typed-relations
       (reduce
         (fn [result {:keys [articles] :as relation}]
           (update result articles conj relation))
         {})
       (mapv #(aggregate-relation (val %) weights))))


(defn infer-relations
  [articles genealogists weights]
  (util/assert-not-empty articles)
  (util/assert-not-empty genealogists)
  (util/assert-not-nil weights)
  (-> (infer-typed-relations articles genealogists)
      (aggregate-typed-relations weights)))
