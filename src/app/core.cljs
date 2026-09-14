(ns app.core
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

#_(defn circle
    [grid x y r]
    (reduce (fn [g theta]
              (let [x1 (+ (* (js/Math.cos theta) r) x)
                    y1 (+ (* (js/Math.sin theta) r) y)]
                (-> g
                    (put x1 y1 \.))))
            grid
            (range 0 two-pi (/ two-pi 64))))

(clojure.string/join (repeat 10 \space))

(defn clear
  ([grid x y w h] (clear grid x y w h \space))
  ([grid x y w h filler]
   (let [clear-line (clojure.string/join (repeat w filler))]
     (reduce (fn [g row] (put-str g x row clear-line))
             grid
             (range y (+ y h))))))

(def ramp " .:-=+*#%@")
(def paul-ramp
  "https://paulbourke.net/dataformats/asciiart/"
  (reverse
   "$@B%8&WM#*oahkbdpqwmZO0QLCJUYXzcvunxrjft/\\|()1{}[]?-_+~<>i!lI;:,\"^`'. "))
(defn gray-luma [r g b] (long (+ (* 0.299 r) (* 0.587 g) (* 0.114 b))))

(defn shader
  ([grid frag] (shader grid frag paul-ramp))
  ([grid frag ramp]
   (let [height (count grid)
         width (count (first grid))]
     (reduce (fn [g i]
               (let [x (mod i width)
                     y (quot i width)]
                 (put g x y (frag x y width height ramp))))
             grid
             (range 0 (* width height))))))


(defn frag
  [x y w h ramp]
  (let [r (* (/ x w) 256)
        g (* (/ y h) 256)
        b 256
        gray (gray-luma r g b)
        n (count ramp)
        c (min (dec n) (quot (* gray n) 256))]
    (nth ramp c)))

(defn render [grid] (str/join "\n" (map str/join grid)))

(defn draw!
  []
  (let [grid (-> (blank-grid 120 50)
                 (shader frag ramp)
                 (clear 17 19 66 20)
                 (box 20 20 60 18)
                 (box 24 23 28 6)
                 (put-str 26 25 "hello, ascii")
                 (put-str 26 26 "edit src/app/core.cljs"))]
    (set! (.-textContent (.getElementById js/document "screen"))
          (render grid))))

(defn ^:export init [] (draw!))

#_(defn reload [] (draw!))
