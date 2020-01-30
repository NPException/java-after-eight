(ns java-after-eight-clj.article
  (:require [java-after-eight-clj.util :refer [remove-outer-quotation-marks
                                               read-lines-from-file
                                               assert-not-empty]]
            [clojure.string :as string])
  (:import [java.util Objects]
           [java.time LocalDate]
           [java.io IOException UncheckedIOException]))

;; Note: I will try to stay away from introducing new types, as much as possible,
;;       but instead use basic Clojure data structures as long as they suffice.


;; below here -> .article.Title

(defn ^:private create-title
  [text]
  (-> (remove-outer-quotation-marks text)
      (assert-not-empty "Titles can't have an empty text.")))


;; below here -> .article.Description

(defn ^:private create-description
  [text]
  (-> (remove-outer-quotation-marks text)
      (assert-not-empty "Description can't have an empty text.")))

;; below here -> .article.Slug

(defn ^:private create-slug
  [value]
  (-> (Objects/requireNonNull value)
      (assert-not-empty "Slugs can't have an empty value.")))


;; below here -> .article.Tag

(defn ^:private create-tag
  [text]
  (-> (Objects/requireNonNull text)
      (assert-not-empty "Tags can't have en empty text.")))

(defn ^:private create-tags
  [tags-text]
  (let [raw-tags (-> tags-text
                     (string/replace #"^\[|\]$" "")
                     (string/split #","))]
    (->> raw-tags
         (map string/trim)
         (filter #(not (empty? %)))
         (mapv create-tag))))


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
  (let [[k v] (string/split line #":" 2)]
    (when (nil? v)
      (throw (IllegalArgumentException. (str "Line doesn't seem to be a key/value pair (no colon): " line))))
    (when (string/blank? k)
      (throw (IllegalArgumentException. (str "Line \"" line "\" has no key."))))
    [(-> k string/trim string/lower-case keyword)
     (string/trim v)]))


;; equivalents of ArticleFactory.createArticle(..)

(defn parse-article
  [front-matter content]
  (let [{:keys [title tags date description slug]}
        (->> front-matter
                     (map line->key-value-pair)
                     (into {}))]
    (article
      (create-title title)
      (create-tags tags)
      (LocalDate/parse date)
      (create-description description)
      (create-slug slug)
      content)))


(defn parse-article-from-lines
  [lines]
  (parse-article
    (extract-front-matter lines)
    (extract-content lines)))


(defn parse-article-from-file
  [file]
  (try
    (parse-article-from-lines (read-lines-from-file file))
    (catch IOException ex
      (throw (UncheckedIOException. (str "Creating article failed: " file, ex))))
    (catch RuntimeException ex
      (throw (RuntimeException. (str "Creating article failed: " file, ex))))))
