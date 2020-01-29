(ns java-after-eight-clj.article
  (:require [java-after-eight-clj.util :refer [remove-outer-quotation-marks
                                               assert-not-empty]]
            [clojure.string :as string])
  (:import [java.util Objects]))

;; Note: I will try to stay away from introducing new types, as much as possible,
;;       but instead use basic Clojure data structures as long as they suffice.


;; mimics ..article.Title.from
(defn title
  [text]
  (-> (remove-outer-quotation-marks text)
      (assert-not-empty "Titles can't have an empty text.")))


;; mimics ..article.Description.from
(defn description
  [text]
  (-> (remove-outer-quotation-marks text)
      (assert-not-empty "Description can't have an empty text.")))

;; mimics ..article.Slug.from
(defn slug
  [value]
  (-> (Objects/requireNonNull value)
      (assert-not-empty "Slugs can't have an empty value.")))


;; mimics ..article.Tag.from
(defn tag
  [text]
  (-> (string/trim text)
      (assert-not-empty "Tags can't have en empty text.")))

;; mimics ..article.Tag.from
(defn tags
  [tags-text]
  (let [raw-tags (-> tags-text
                     (string/replace #"^\[|\]$" "")
                     ;; TODO should trailing commas result in an IAE? ("[T1,T2,,]")
                     (string/split #","))]
    (mapv tag raw-tags)))


;; mimics ..article.Article.Article
(defn article
  [title tags date description slug content]
  ;; NOTE: none of the arguments should ever be nil, so the checks may be unnecessary
  {:title       (Objects/requireNonNull title)
   :tags        (Objects/requireNonNull tags)
   :date        (Objects/requireNonNull date)
   :description (Objects/requireNonNull description)
   :slug        (Objects/requireNonNull slug)
   :content     (Objects/requireNonNull content)})
