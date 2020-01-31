(ns java-after-eight-clj.article
  (:require [java-after-eight-clj.util :as util]
            [clj-time.format :as tf]
            [clojure.string :as string]))

;; Note: I will try to stay away from introducing new types, as much as possible,
;;       but instead use basic Clojure data structures as long as they suffice.


;; below here -> .article.Title

(defn ^:private create-title
  [text]
  (-> (util/remove-outer-quotation-marks text)
      (util/assert-not-empty "Titles can't have an empty text.")))


;; below here -> .article.Description

(defn ^:private create-description
  [text]
  (-> (util/remove-outer-quotation-marks text)
      (util/assert-not-empty "Description can't have an empty text.")))

;; below here -> .article.Slug

(defn ^:private create-slug
  [value]
  (util/assert-not-empty value "Slugs can't have an empty value."))


;; below here -> .article.Tag

(defn ^:private create-tag
  [text]
  (util/assert-not-empty text "Tags can't have en empty text."))

(defn ^:private create-tags
  [tags-text]
  (let [raw-tags (-> tags-text
                     (string/replace #"^\[|\]$" "")
                     (string/split #","))]
    (->> raw-tags
         (map string/trim)
         (filter #(not (empty? %)))
         (map create-tag)
         set)))


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
      (throw (ex-info "Line doesn't seem to be a key/value pair (no colon)." {:line line})))
    (when (string/blank? k)
      (throw (ex-info "Line has no key." {:line line})))
    [(-> k string/trim string/lower-case keyword)
     (string/trim v)]))


;; equivalents of ArticleFactory.createArticle(..)

(defn ^:private parse-article
  [front-matter content-fn]
  (let [{:keys [title tags date description slug]}
        (->> front-matter
             (map line->key-value-pair)
             (into {}))]
    {:title       (create-title title)
     :tags        (create-tags tags)
     :date        (tf/parse date)
     :description (create-description description)
     :slug        (create-slug slug)
     :content-fn  content-fn}))


(defn parse-article-from-lines
  [lines]
  (parse-article
    (extract-front-matter lines)
    #(extract-content lines)))


(defn parse-article-from-file
  [file]
  (try
    (let [lines-fn #(-> file slurp string/split-lines)]
      (parse-article
        (extract-front-matter (lines-fn))
        (comp extract-content lines-fn)))
    (catch Exception ex
      (throw (ex-info "Creating article failed" {:file file} ex)))))
