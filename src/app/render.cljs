(ns app.render
  (:require [clojure.string :as str]))

(def two-pi (* js/Math.PI 2))

(defn blank-grid [cols rows] (vec (repeat rows (vec (repeat cols \space)))))

(defn put
  [grid x y ch]
  (if (and (<= 0 y (dec (count grid))) (<= 0 x (dec (count (first grid)))))
    (assoc-in grid [y x] ch)
    grid))

(defn put-str
  [grid x y s]
  (reduce (fn [g [i ch]] (put g (+ x i) y ch)) grid (map-indexed vector s)))

(defn box
  [grid x y w h]
  (let [x2 (+ x (dec w))
        y2 (+ y (dec h))]
    (-> (reduce (fn [g i]
                  (-> g
                      (put (+ x i) y \-)
                      (put (+ x i) y2 \-)))
                grid
                (range 1 (dec w)))
        (as-> g
          (reduce (fn [g i]
                    (-> g
                        (put x (+ y i) \|)
                        (put x2 (+ y i) \|)))
                  g
                  (range 1 (dec h))))
        (put x y \+)
        (put x2 y \+)
        (put x y2 \+)
        (put x2 y2 \+))))

(defn circle
  [grid x y r]
  (reduce (fn [g theta]
            (let [x1 (+ (* (js/Math.cos theta) r) x)
                  y1 (+ (* (js/Math.sin theta) r) y)]
              (-> g
                  (put x1 y1 \.))))
          grid
          (range 0 two-pi (/ two-pi 64))))
(defn clear
  ([grid x y w h] (clear grid x y w h \space))
  ([grid x y w h filler]
   (let [clear-line (clojure.string/join (repeat w filler))]
     (reduce (fn [g row] (put-str g x row clear-line))
             grid
             (range y (+ y h))))))

(defn text-box
  [grid x y text]
  (let [padding 1
        width (+ (reduce #(max (count %2) %1) 0 text) (* 2 padding))
        height (+ (count text) (* 2 padding))]
    (reduce (fn [g [idx line]] (put-str g x (+ y idx) line))
            (clear grid (- x padding) (- y padding) width height)
            (map-indexed vector text))))

(defn render [grid] (str/join "\n" (map str/join grid)))
