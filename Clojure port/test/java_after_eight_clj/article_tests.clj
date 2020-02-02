(ns java-after-eight-clj.article-tests
  (:require [clojure.test :refer :all]
            [java-after-eight-clj.test-helper :refer :all]
            [java-time :as t]
            [java-after-eight-clj.util :as util]
            [java-after-eight-clj.article :as a]))


(defn find-buggy-quotation-removals
  [parse-fn]
  (for [[text expected] [["A cool blog post"
                          "A cool blog post"]
                         ["\"A cool blog post\""
                          "A cool blog post"]
                         ["\"\"A cool blog post\" he said\""
                          "\"A cool blog post\" he said"]]
        :let [result (parse-fn text)]
        :when (not= expected result)]
    [text expected result]))


(deftest util-remove-outer-quotation-marks
  (is (empty? (find-buggy-quotation-removals util/remove-outer-quotation-marks))))


(deftest title
  (is (empty? (find-buggy-quotation-removals @#'a/create-title)))
  (is (thrown-with-msg?
        Exception #"Titles can't have an empty text\."
        (@#'a/create-title ""))))


(deftest description
  (is (empty? (find-buggy-quotation-removals @#'a/create-description)))
  (is (thrown-with-msg?
        Exception #"Description can't have an empty text\."
        (@#'a/create-description ""))))


(deftest slug
  (is (thrown-with-msg?
        Exception #"Slugs can't have an empty value\."
        (@#'a/create-slug ""))))


(deftest tags
  (let [errors (for [[text expected]
                     [["[ ]" #{}]
                      ["[$TAG]" #{"$TAG"}]
                      ["[$TAG,$TOG,$TUG]" #{"$TAG", "$TOG", "$TUG"}]
                      ;; multipleElementsArrayWithSpaces_multipleTagsWithoutSpaces
                      ["[$TAG ,  $TOG , $TUG  ]" #{"$TAG", "$TOG", "$TUG"}]
                      ;; multipleElementsArrayWithJustSpacesTag_emptyTagIsIgnored
                      ["[$TAG ,  , $TUG  ]" #{"$TAG", "$TUG"}]
                      ;; multipleElementsArrayWithEmptyTag_emptyTagIsIgnored
                      ["[$TAG ,, $TUG  ]" #{"$TAG", "$TUG"}]
                      ;; multipleElementsArrayDuplicateTags_duplicateTagIsIgnored
                      ["[$TAG, $TAG]" #{"$TAG"}]]
                     :let [result (@#'a/create-tags text)]
                     :when (not (and (set? result)
                                     (= result expected)))]
                 [text expected result])]
    (is (empty? errors))))


(deftest article-from-front-matter
  (testing "createFromFrontMatter_multipleColons_getValidArticle"

    (let [front-matter ["title: Cool: A blog post",
                        "tags: [$TAG, $TOG]",
                        "date: 2020-01-23",
                        "description: \"Very blog, much post, so wow\"",
                        "slug: cool-blog-post"]
          article (@#'a/parse-article front-matter #(list))]
      (is (= (:title article) "Cool: A blog post"))
      (is (and (set? (:tags article))
               (= (:tags article) #{"$TAG", "$TOG"})))
      (is (= (:date article) (t/local-date 2020 1 23)))
      (is (= (:description article) "Very blog, much post, so wow"))
      (is (= (:slug article) "cool-blog-post"))))

  (testing "createFromFrontMatter_allTagsCorrect_getValidArticle"

    (let [front-matter ["title: A cool blog post",
                        "tags: [$TAG, $TOG]",
                        "date: 2020-01-23",
                        "description: \"Very blog, much post, so wow\"",
                        "slug: cool-blog-post"]
          article (@#'a/parse-article front-matter #(list))]
      (is (= (:title article) "A cool blog post"))
      (is (and (set? (:tags article))
               (= (:tags article) #{"$TAG", "$TOG"})))
      (is (= (:date article) (t/local-date 2020 1 23)))
      (is (= (:description article) "Very blog, much post, so wow"))
      (is (= (:slug article) "cool-blog-post")))))


(deftest article-from-file
  (testing "createFromFile_allTagsCorrect_getValidArticle"

    (let [file-lines ["---",
                      "title: A cool blog post",
                      "tags: [$TAG, $TOG]",
                      "date: 2020-01-23",
                      "description: \"Very blog, much post, so wow\"",
                      "slug: cool-blog-post",
                      "---",
                      "",
                      "Lorem ipsum dolor sit amet.",
                      "Ut enim ad minim veniam.",
                      "Duis aute irure dolor in reprehenderit.",
                      "Excepteur sint occaecat cupidatat non proident."]
          article (@#'a/parse-article-from-lines file-lines)]
      (is (= (:title article) "A cool blog post"))
      (is (and (set? (:tags article))
               (= (:tags article) #{"$TAG", "$TOG"})))
      (is (= (:date article) (t/local-date 2020 1 23)))
      (is (= (:description article) "Very blog, much post, so wow"))
      (is (= (:slug article) "cool-blog-post"))
      (is (= ((:content-fn article))
             ["",
              "Lorem ipsum dolor sit amet.",
              "Ut enim ad minim veniam.",
              "Duis aute irure dolor in reprehenderit.",
              "Excepteur sint occaecat cupidatat non proident."])))))
