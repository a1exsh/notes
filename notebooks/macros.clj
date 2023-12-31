;; # Macros
(ns macros
  (:require [nextjournal.clerk :as clerk]))

;; ## Basics

;; Prints a line `before`, evaluates `body`, then prints a line `after` and
;; returns the result of `body` evaluation.
(defmacro with-debug [& body]
  `(do
     (println "before")
     (let [res# ~@body]
       (println "after")
       res#)))

(with-debug 1)

(with-out-str
  (with-debug 1))

;; Prints the `form`, followed by `=>` and the result of its evaluation.
;; Returns that result.
;;
;; Note the quoting of the unquoted form...
(defmacro with-trace [form]
  `(do
     (print '~form "=> ")
     (let [res# ~form]
       (println res#)
       res#)))

(with-trace (+ 1 2 3))

(with-out-str 
  (with-trace (+ 1 2 3)))

;; ## Doing the work in fns
(defn str-form [form]
  `(str ~form))

(str-form 1)

(defn str-forms [& forms]
  (map str-form forms))

(str-forms 'a 'b 'c)

(defmacro stringify [& forms]
  ;; without the outer `(str)` it will look like a function call on a
  ;; `String`, which doesn't work for obvious reason
  `(str ~@(apply str-forms forms)))

(macroexpand-1
 '(stringify a b c))

(stringify 'a 'b 'c)

;; It also works directly, so no great benefit of having `str-forms` fn in
;; this simple case:
(defmacro stringify-direct [& forms]
  `(str ~@(map str-form forms)))

(stringify-direct 'a 'b 'c)

;; Or completely directly:
(defmacro stringify-most-direct [& forms]
  `(str ~@(->> forms
               (map (fn [form]
                      `(str ~form))))))

(stringify-most-direct 'a 'b 'c)
