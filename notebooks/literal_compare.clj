(ns literal-compare
  (:refer-clojure :exclude [+ - * / abs partial ref zero? infinite?
                            numerator denominator compare run!])
  (:require [nextjournal.clerk :as clerk]
            [sicmutils.env :refer [literal-number sqrt]]
            [sicmutils.expression :as xp :refer [compare]]
            [sicmutils.expression.render :as xr]
            [sicmutils.value :as v]))

(def corecmp clojure.core/compare)

(def a 2)
(def b 3)

(def comparisons
  [[a b]
   [b a]
   [a (literal-number b)]
   [(literal-number a) b]
   [b (literal-number a)]
   [(literal-number b) a]
   [(literal-number a) (literal-number b)]
   [(literal-number b) (literal-number a)]
   [(literal-number a) (sqrt b)]
   [(literal-number a) (sqrt (literal-number b))]
   [(sqrt (literal-number b)) (literal-number a)]
   [(sqrt (literal-number b)) (sqrt (literal-number 3))]])

(->> comparisons
     (map (fn [[l r :as c]]
            (let [cc (try
                       (corecmp (xp/expression-of l)
                                (xp/expression-of r))
                       (catch Exception ex nil))

                  vc (try
                       (v/compare l r)
                       (catch Exception ex nil))

                  xc (xp/compare l r)

                  eq-str (fn [c1 c2]
                           (if (and c1 c2)
                             (if (= c1 c2) "=" "≠")
                             ""))]
              {:comparison c
               :corecmp    cc
               :eq1        (eq-str cc vc)
               :v/compare  vc
               :eq2        (eq-str vc xc)
               :xp/compare xc})))
     clerk/table)

;; Once again, the last row:
(xp/compare (sqrt (literal-number 3)) (sqrt (literal-number 3)))

;; 👆🏻 🧐
