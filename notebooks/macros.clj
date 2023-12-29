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

;; Prints the `body` form, followed by `=>` and the result of `body`
;; evaluation.  Returns that result.
;;
;; Note the quoting of the unquote-spliced body...
(defmacro with-trace [& body]
  `(do
     (print '~@body "=> ")
     (let [res# ~@body]
       (println res#)
       res#)))

(with-trace (+ 1 2 3))

(with-out-str 
  (with-trace (+ 1 2 3)))
