(ns java-after-eight-clj.util
  (:require [clojure.string :as string]))


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
