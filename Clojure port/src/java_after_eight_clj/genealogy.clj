(ns java-after-eight-clj.genealogy
  (:require [java-after-eight-clj.util :as util]))


(defprotocol Genealogist
  (infer-typed-relation [this article-1 article-2]))

(defprotocol GenealogistService
  (procure ^Genealogist [this articles]))



(defn validate-relation-type
  [value]
  (util/assert-not-nil value "Relation types can't be nil")
  (when (seqable? value)
    (util/assert-not-empty value "Relation types can't have an empty value."))
  value)


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


(defrecord TypedRelation [article-1, article-2, type, ^long score])

(defn create-typed-relation
  [article-1 article-2 relation-type score]
  (when-not (<= 0 score 100)
    (throw (ex-info "Score should be in interval [0; 100]" {:score score})))
  (->TypedRelation
    (util/assert-not-nil article-1)
    (util/assert-not-nil article-2)
    (validate-relation-type relation-type)
    (Math/round (double score))))


(defrecord Relation [article-1, article-2, ^long score])

;; equivalent of Relation.aggregate
(defn ^:private aggregate-relation
  [typed-relations weights]
  (util/assert-not-empty
    typed-relations "Can't create relation from zero typed relations.")
  (let [{:keys [article-1 article-2]} (first typed-relations)
        score (loop [score-total 0.0
                     score-count 0
                     remaining typed-relations]
                (if remaining
                  (let [relation (first remaining)]
                    (when-not (and (= article-1 (:article-1 relation))
                                   (= article-2 (:article-2 relation)))
                      (ex-info "All typed relations must belong to the same article." {}))
                    (recur (+ score-total (* (:score relation) (weights (:type relation))))
                           (inc score-count)
                           (next remaining)))
                  (Math/round ^double (/ score-total score-count))))]
    (when-not (<= 0 score 100)
      (throw (ex-info "Score should be in interval [0; 100]" {:score score})))
    (->Relation article-1 article-2 score)))


(defn ^:private infer-typed-relations
  [articles genealogists]
  (->> (for [a1 articles
             a2 articles
             g genealogists
             :when (not= a1 a2)]
         [g a1 a2])
       (pmap #(apply infer-typed-relation %))))


(defn infer-relations
  [articles genealogists weights]
  (util/assert-not-empty articles)
  (util/assert-not-empty genealogists)
  (util/assert-not-nil weights)
  (->> (infer-typed-relations articles genealogists)
       (group-by (juxt :article-1 :article-2))
       (mapv #(aggregate-relation (val %) weights))))
