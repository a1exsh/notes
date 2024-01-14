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

(defn make-image
  [width height pixels]
  (doto (BufferedImage. width height BufferedImage/TYPE_INT_ARGB)
    (.setRGB 0 0 width height pixels 0 width)))

(defn invert-image
  [^BufferedImage img]
  (let [width  (.getWidth  img)
        height (.getHeight img)]
    (make-image width height
                (amap (.getRGB img 0 0 width height nil 0 width)
                      pos inv
                      (bit-xor 0xFFFFFF (aget inv pos))))))

(defn analyze-image
  [^BufferedImage img sampling]
  (let [width  (.getWidth  img)
        height (.getHeight img)
        area   (* width height)

        xs (range 0 width  sampling)
        ys (range 0 height sampling)
        sw (count xs)
        sh (count ys)
        samples (vec (for [y ys x xs] (.getRGB img x y)))

        freqs          (frequencies samples)
        distinct-cols  (->> freqs keys count)
        distinct-ratio (float (/ distinct-cols (count samples)))

        [bg-col bg-freq] (->> freqs
                              (sort-by val >)
                              first)
        samp (->> samples
                  (mapcat int->rgb-vec)
                  (reduce +))
        white (* 3 255 (count samples))
        lum   (float (/ samp white))
        result {:image    img
                :sampled  (make-image sw sh (int-array samples))

                :width  width
                :height height
                ;;:area   area

                :samples (count samples)
                :samp    samp
                :lum     lum
                :white   white

                ;;:freqs           freqs
                :distinct-cols  distinct-cols
                :distinct-ratio distinct-ratio

                #_#_:background {:color     (int->rgb-vec bg-col)
                                 :frequency bg-freq
                                 :ratio     (float (/ bg-freq (/ area sampling sampling)))}}]

    (if (> distinct-ratio 0.1)
      (assoc result
             :screenshot? false)
      (assoc result
             :screenshot? true
             :inverted (invert-image img)))))

(def sampling 16)

;; ## Dark
;; `binding [*warn-on-reflection* true]`
(time (-> "resources/screenshots/dark/javadoc.png"
          load-image
          (analyze-image sampling)))

(time (-> "resources/screenshots/dark/javadoc2.png"
          load-image
          (analyze-image sampling)))

(time (-> "resources/screenshots/dark/emacs.png"
          load-image
          (analyze-image sampling)))

(time (-> "resources/screenshots/dark/emacs2.png"
          load-image
          (analyze-image sampling)))

;; ## Light
(time (-> "resources/screenshots/light/stackoverflow.png"
          load-image
          (analyze-image sampling)))

(time (-> "resources/screenshots/light/emacs.png"
          load-image
          (analyze-image sampling)))

;; ## Not a screenshot
(time (-> "resources/rick.jpg"
          load-image
          (analyze-image sampling)))
