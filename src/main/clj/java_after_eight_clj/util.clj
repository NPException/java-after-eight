(ns java-after-eight-clj.util
  (:require [clojure.string :as string]
            [clojure.stacktrace :as st]))


(defn remove-outer-quotation-marks
  [s]
  (string/replace s #"^\"|\"$" ""))


(defmacro assert-not-empty
  "Throws an exception with the provided message
  if x is empty. Else returns x."
  [x & [msg]]
  `(if (empty? ~x)
    (throw (ex-info (or ~msg "Expression return an empty sequence") {:empty-expression '~x}))
    ~x))


(defn print-ex
  "Prints an exception to *err*, with all messages and ex-info data maps in the
  cause chain. Only prints the stack trace of the root cause,
  unless parameter `:full? true` is provided."
  [ex & {:keys [full?]}]
  (binding [*out* *err*]
    (if full?
      (st/print-cause-trace ex)
      (loop [ex ex]
        (if-let [cause (ex-cause ex)]
          (do (st/print-throwable ex)
              (newline)
              (print "Caused by: ")
              (recur cause))
          (st/print-stack-trace ex))))))
