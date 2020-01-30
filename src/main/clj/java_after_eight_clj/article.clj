(ns java-after-eight-clj.article
  (:require [java-after-eight-clj.util :refer [remove-outer-quotation-marks
                                               read-lines-from-file
                                               assert-not-empty]]
            [clojure.string :as string])
  (:import [java.util Objects]
           [java.time LocalDate]))

;; Note: I will try to stay away from introducing new types, as much as possible,
;;       but instead use basic Clojure data structures as long as they suffice.


;; below here -> .article.Title

(defn title
  [text]
  (-> (remove-outer-quotation-marks text)
      (assert-not-empty "Titles can't have an empty text.")))


;; below here -> .article.Description

(defn description
  [text]
  (-> (remove-outer-quotation-marks text)
      (assert-not-empty "Description can't have an empty text.")))

;; below here -> .article.Slug

(defn slug
  [value]
  (-> (Objects/requireNonNull value)
      (assert-not-empty "Slugs can't have an empty value.")))


;; below here -> .article.Tag

(defn ^:private tag
  [text]
  (-> (string/trim text)
      (assert-not-empty "Tags can't have en empty text.")))

(defn tags
  [tags-text]
  (let [raw-tags (-> tags-text
                     (string/replace #"^\[|\]$" "")
                     ;; TODO should trailing commas result in an IAE? ("[T1,T2,,]")
                     (string/split #","))]
    (mapv tag raw-tags)))


;; below here -> .article.Article

(defn article
  [title tags date description slug content]
  ;; NOTE: none of the arguments should ever be nil, so the checks may be unnecessary
  {:title       (Objects/requireNonNull title)
   :tags        (Objects/requireNonNull tags)
   :date        (Objects/requireNonNull date)
   :description (Objects/requireNonNull description)
   :slug        (Objects/requireNonNull slug)
   :content     (Objects/requireNonNull content)})


;; below here -> .article.ArticleFactory

(defn ^:private not-front-matter-separator?
  [line]
  (not= "---" (string/trim line)))

(defn ^:private skip-next-front-matter-separator
  [lines]
  (->> lines
       (drop-while not-front-matter-separator?)
       (drop 1)))

(defn ^:private extract-front-matter
  [lines]
  (->> lines
       skip-next-front-matter-separator
       (take-while not-front-matter-separator?)))

(defn ^:private extract-content
  [lines]
  (->> lines
       skip-next-front-matter-separator
       skip-next-front-matter-separator))

(defn ^:private line->key-value-pair
  [line]
  (let [[k v] (string/split line #":")]
    (when (nil? v)
      (throw (IllegalArgumentException. (str "Line \"" line "\" doesn't seem to be a key/value pair."))))
    (when (string/blank? k)
      (throw (IllegalArgumentException. (str "Line \"" + line + "\" has no key."))))
    [(-> k string/trim string/lower-case keyword)
     (string/trim v)]))


(defn create-article
  ([input]
   (if (or (string? input)
           (not (seqable? input)))
     (recur (read-lines-from-file input))
     (create-article
       (extract-front-matter input)
       (extract-content input))))
  ([front-matter content]
   (let [entries (->> front-matter
                      (map line->key-value-pair)
                      (into {}))]
     (article
       (title (:title entries))
       (tags (:tags entries))
       (LocalDate/parse (:date entries))
       (description (:description entries))
       (slug (:slug entries))
       content))))
