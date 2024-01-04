;; # Forward declarations
(ns declare
  (:require [nextjournal.clerk :as clerk]))

(declare ask-later)

;; This blows up when evaluating normally top to bottom, but is perfectly fine
;; when rendering with Clerk:
(def answer (str "And the answer is: " (ask-later)))

(defn ask-later []
  42)
