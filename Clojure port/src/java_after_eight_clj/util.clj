(ns java-after-eight-clj.util
  (:require [clojure.string :as string]
            [clojure.stacktrace :as st])
  (:import [java.lang.management ManagementFactory]
           [java.util.function Function]))


(defn remove-outer-quotation-marks
  [s]
  (string/replace s #"^\"|\"$" ""))


;; Note: The only reason that this is a macro, is so that
;;  I can see the expression that evaluated to an empty sequence.
;;  It's mostly just a toy example to show a macro.
(defmacro assert-not-empty
  "Throws an exception with the provided message
  if x is empty. Else returns x."
  [x & [msg]]
  `(if (empty? ~x)
     (throw (ex-info (or ~msg "Expression return an empty sequence") {:empty-expression '~x}))
     ~x))


(defmacro assert-not-nil
  "Throws a NullPointer exception with the provided message
  if x is nil. Else returns x."
  [x & [msg]]
  `(if (nil? ~x)
     (throw (NullPointerException. ~msg))
     ~x))


(def java-version
  "The major java version of the running JVM.
  Zero if it couldn't be determined."
  (try
    (let [version (System/getProperty "java.version")]
      (Integer/parseInt
        (cond
          (string/starts-with? version "1.")
          (-> version (subs 2 3))

          (.contains version ".")
          (-> version (string/split #"\.") first)

          :else
          version)))
    (catch Exception _
      0)))


;; this is merely a proof of concept for myself, that it's
;; possible to vary the implementation at runtime based on
;; the available Java version. In this case for slightly
;; better performance than clojure.string/trim.
(declare strip)
(if (>= java-version 11)
  ; Use Java 11 strip for better performance if available
  (eval '(defn strip
           [^String s]
           (.strip s)))
  ;; fallback to clojure's trim if we're not yet on Java 11
  (eval '(defn strip
           [^String s]
           (string/trim s))))


(def reverse-comparator
  #(compare %2 %1))


;; only use getPidFromMxBeanName
(defn process-id
  []
  (try
    (-> (ManagementFactory/getRuntimeMXBean)
        .getName
        (string/split #"@")
        first
        Long/parseLong)
    (catch Exception _)))


(defn process-details
  []
  (format "Process ID: %s | Major Java version: %s"
          (if-let [pid (process-id)]
            (str pid) "unknown")
          (if (> java-version 0)
            (str java-version) "unknown")))


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


(def ^:private op-prio
  '{+ 1, - 1, * 2, / 2})

(defn ^:private takes-precedence?
  "checks if the first operator takes precedence over the second"
  [op1 op2]
  (> (op-prio op1) (op-prio op2)))


(defmacro infix
  "Toy macro. Takes a mathmatical calculation in infix notation. Supports +,-,*,/
  Do not use in serious applications!"
  [[a op b & args :as element]]
  (cond
    ;; if op is not known, assume the entire element is a function call
    (not (op-prio op))
    element
    ;; only three elements
    (empty? args)
    (list op
          (if (list? a) `(infix ~a) a)
          (if (list? b) `(infix ~b) b))
    ;; first operator takes precedence
    (takes-precedence? op (first args))
    `(infix ~(concat [(list a op b)] args))
    ;; second operator takes precedence
    :else
    `(infix ~(concat [a op] [(conj args b)]))))

(defn jFunction
  ^Function [f]
  (reify Function
    (apply [_ x] (f x))))
