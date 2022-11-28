;; # 2 ? 2 ? 2 ? 2 = 7
;;
;; Given the four $$2$$'s can you make a $$7$$?..
;;
^{:nextjournal.clerk/visibility {:code :hide}}
(ns four-twos
  (:refer-clojure :exclude [+ - * / abs partial ref zero? infinite?
                            numerator denominator compare = run!])
  (:require [nextjournal.clerk :as clerk]
            [clojure.java.io :as io]
            [clojure.math.combinatorics :as combo]
            [sicmutils.env :as e :refer :all]
            [sicmutils.expression :as xp]
            [sicmutils.expression.render :as xr])
  (:import [java.nio.file Paths Files]
           [javax.imageio ImageIO]))

;;
;; First, we will need a function for doing the concatenation of two (long)
;; numbers.  Let's call it `long-cat`:
;;
^{:nextjournal.clerk/visibility {:code :hide}}
(with-open [in (->> ["resources/long-cat.jpg"]
                    into-array
                    (Paths/get "")
                    Files/readAllBytes
                    io/input-stream)]
  (ImageIO/read in))

(defn long-cat [x y]
  (Long/valueOf (str (.toString x)
                     (.toString y))))

(long-cat 1 2)

;; Operator symbols that we are allowed to use:
(def ops '[+ - * / expt])

;;
;; The solution is to do the exhaustive search of all possible combinations
;; with the given operations on the given number of $$2$$'s.
;;
;; For that, define a recursive function that takes $$n$$—the number of
;; $$2$$'s, and produces all expressions that one can build from them.
;;
;; The function's result is a concatenation of all possible splits where $$s$$
;; $$2$$'s are on the left side, and $$\left( n-s \right)$$—on the right side,
;; with all possible operators in the middle:
;;
(defn exprs2 [n]
  (if (= 1 n)
    [2]
    (mapcat (fn [s]
              (let [ls (exprs2 s)
                    rs (exprs2 (- n s))]
                (concat #_(map #(apply long-cat %)
                             (combo/cartesian-product (filter integer? ls)
                                                      (filter integer? rs)))
                        (combo/cartesian-product ops ls rs))))
            (range 1 n))))

(exprs2 1)

(exprs2 2)
(clerk/table (exprs2 2))

;;
;; ## TeX Rendering
;;
;; Literal $$2 \times 2$$ looks just so much better with `\times`:
;;
(defn ->tex [& args]
  (clojure.string/replace (apply xr/->TeX args) #"\\," " \\\\times "))

(def render (comp clerk/tex ->tex))

(def two-by-two '(* 2 2))

;; Default rendering:
(clerk/tex (xr/->TeX two-by-two))

;; Improved:
(render two-by-two)

;;
;; ## Rational Evaluation
;;
;; "Rational", because we only eagerly evaluate where the result can be
;; represented as a rational number exactly.  In practice, this means that we
;; replace value raised to the power of $$\frac{1}{2}$$ with the square root.
;;
(defn reval [x]
  (if-not (seq? x)
    x
    (let [[op l r] x
          l (reval l)
          r (reval r)]
      (if (and (= 'expt op)
               (= (/ 1 2) r))
        (sqrt (literal-number l))
        ((resolve op) l r)))))

(reval '(+ 1 2))
(render (reval '(expt 2 (/ 2 (+ 2 2)))))

;;
;; ## Solutions
;;
;; Notice, how the nested tables are rendered nicely?..
;;
(->> (exprs2 4)
     distinct
     (map (fn [x]
            (let [val (try (reval x) (catch Throwable t))
                                        ; ignore division by zero and raising
                                        ; to a non-integer power
                  texval (->tex val)
                  tex    (format "%s = %s" texval (->tex x))]
              {:expression      x
               :value           val
               :texval          texval
               :tex             tex
               :equation        (clerk/tex tex)})))
     (remove (fn [{:keys [value]}]
               (or (nil? value)
                   #_(not (exact? value))
                                        ; disable the above to see the √2 in the table
                   (ratio? value)
                   (negative? value))))
     (group-by :texval)
     (map (fn [[texval xs]]
            {:value     (let [x (->> xs first :value)]
                          (if (= x '(sqrt 2))
                            (Math/sqrt 2)
                            x))
                                        ; this is a hack before we understand
                                        ; how to do it better, see:
                                        ; https://github.com/sicmutils/sicmutils/issues/527
             :result    (clerk/tex texval)
             :exprs     (->> xs (map #(dissoc % :value :texval)) clerk/table)}))
     (sort-by :value)
     (map #(dissoc % :value))
     clerk/table)
