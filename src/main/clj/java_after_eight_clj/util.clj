(ns java-after-eight-clj.util
  (:require [clojure.string :as string]
            [clojure.java.io :as io])
  (:import [java.nio.file Files Path]))


(defn remove-outer-quotation-marks
  [s]
  (string/replace s #"^\"|\"$" ""))


(defn assert-not-empty
  "Throws an IllegalArgumentException with the provided message
  if x is empty. Else returns x."
  [x ^String message]
  (if (empty? x)
    (throw (IllegalArgumentException. message))
    x))

(defn read-lines-from-file
  "Converts the argument to a java.nio.file.Path, and
  returns a lazy sequence with the lines contained in the file."
  [x]
  (let [^Path path (if-not (instance? Path x)
                     (-> x io/as-file .toURI Path/of)
                     x)]
    (-> path
        Files/lines
        .iterator
        iterator-seq)))
