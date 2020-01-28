(ns java-after-eight-clj.util
  (:require [clojure.string :as string]))


(defn remove-outer-quotation-marks
  [s]
  (string/replace s #"^\"|\"$" ""))
