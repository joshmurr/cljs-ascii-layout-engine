(ns app.shader
  (:require [app.render :refer [put]]))

(def two-pi (* js/Math.PI 2))

(def ramp " .:-=+*#%@")

(def paul-ramp
  "https://paulbourke.net/dataformats/asciiart/"
  (reverse
   "$@B%8&WM#*oahkbdpqwmZO0QLCJUYXzcvunxrjft/\\|()1{}[]?-_+~<>i!lI;:,\"^`'. "))

(defn gray-luma
  "Returns a value [0, 255)"
  [r g b]
  (long (+ (* 0.299 r) (* 0.587 g) (* 0.114 b))))


(defn circle
  [grid x y r]
  (reduce (fn [g theta]
            (let [x1 (+ (* (js/Math.cos theta) r) x)
                  y1 (+ (* (js/Math.sin theta) r) y)]
              (-> g
                  (put x1 y1 \.))))
          grid
          (range 0 two-pi (/ two-pi 64))))


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

(defn diagonal-gradient-frag
  [x y w h ramp]
  (let [r (* (/ x w) 256)
        g (* (/ y h) 256)
        b 256
        gray (gray-luma r g b)
        n (count ramp)
        c (min (dec n) (quot (* gray n) 256))]
    (nth ramp c)))
