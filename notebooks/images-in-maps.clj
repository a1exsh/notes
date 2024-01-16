(ns images-in-maps
  (:require [nextjournal.clerk :as clerk]
            [clojure.java.io :as io])
  (:import [java.awt.image BufferedImage]
           [javax.imageio ImageIO]))

(def img
  (with-open [in (io/input-stream "resources/rick.jpg")]
    (ImageIO/read in)))

(->> (range 9)                          ; just enough to hide the 2nd image
     (reduce #(assoc %1 (keyword (str "f" %2)) %2)
             {:img1 img :img2 img}))

;; `^^^` Click on `more...` to expand the second image.
