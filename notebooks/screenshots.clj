;; # Screenshots vs. Dark mode
(ns screenshots
  (:require [nextjournal.clerk :as clerk]
            [clojure.java.io :as io])
  (:import [java.awt.image BufferedImage]
           [javax.imageio ImageIO]))

(defn int->rgb-vec
  [s]
  [(bit-and 0xFF (bit-shift-right s 16))
   (bit-and 0xFF (bit-shift-right s  8))
   (bit-and 0xFF                  s   )])

(defn rgb-vec->int
  [[r g b]]
  (bit-or (bit-shift-left r 16)
          (bit-shift-left g  8)
                          b   ))

(defn load-image
  [path]
  (with-open [in (io/input-stream path)]
    (ImageIO/read in)))

(defn invert-image
  [^BufferedImage img]
  (let [width  (.getWidth  img)
        height (.getHeight img)
        res    (BufferedImage. width height BufferedImage/TYPE_INT_ARGB)]
    (.setRGB res
             0 0 width height
             (amap (.getRGB img 0 0 width height nil 0 width)
                   pos inv
                   (bit-xor 0xFFFFFF (aget inv pos)))
             0 width)
    res))

(defn analyze-image
  [^BufferedImage img sampling]
  (let [width  (.getWidth  img)
        height (.getHeight img)
        area   (* width height)
        samples (vec (for [y (range 0 height sampling)
                           x (range 0 width  sampling)]
                       (.getRGB img x y)))
        [bg-col bg-freq] (->> samples
                              frequencies
                              (sort-by val >)
                              first)
        samp (->> samples
                  (mapcat int->rgb-vec)
                  (reduce +))
        white (* 3 255 (count samples))
        lum   (float (/ samp white))]
    {:image  img
     :inverted (invert-image img)

     :width  width
     :height height
     :area   area

     :samples (count samples)
     :samp    samp
     :lum     lum
     :white   white

     :bg-col   (int->rgb-vec bg-col)
     :bg-freq  bg-freq
     :bg-ratio (float (/ bg-freq (/ area sampling sampling)))}))


;; ## Dark
;; `binding [*warn-on-reflection* true]`
(time (-> "resources/screenshots/dark/javadoc.png"
          load-image
          (analyze-image 10)))

(time (-> "resources/screenshots/dark/javadoc2.png"
          load-image
          (analyze-image 10)))

(time (-> "resources/screenshots/dark/emacs.png"
          load-image
          (analyze-image 10)))

(time (-> "resources/screenshots/dark/emacs2.png"
          load-image
          (analyze-image 10)))

;; ## Light
(time (-> "resources/screenshots/light/stackoverflow.png"
          load-image
          (analyze-image 10)))

(time (-> "resources/screenshots/light/emacs.png"
          load-image
          (analyze-image 10)))
