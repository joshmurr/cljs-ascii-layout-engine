(ns app.shader
  (:require [app.render :refer [put]]))

(def two-pi (* js/Math.PI 2))

; TODO: Write more vec2 based functions and then use them
(defn vec2
  [x y]
  {:x x
   :y y})
(defn dot [ax ay bx by] (+ (* ax bx) (* ay by)))
(defn re-scale [v] (update-vals v #(- (* %1 2) 1)))
(defn length [v] (js/Math.sqrt (+ (* (:x v) (:x v)) (* (:y v) (:y v)))))
(defn sub [v1 v2] (vec2 (- (:x v1) (:x v2)) (- (:y v1) (:y v2))))

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

(defn hex-grid
  [x y w _ ramp]
  (let [u (* (/ x (* 2 w)) 4)
        v (* (/ y w) 4)
        sx 1.732
        sy 1
        ax (- (* 2 (mod u sx)) sx)
        ay (- (* 2 (mod v sy)) sy)
        bx (- (* 2 (mod (* (+ u sx) 0.5) sx)) sx)
        by (- (* 2 (mod (* (+ v sy) 0.5) sy)) sy)
        gray (* 0.25 (min (dot ax ay ax ay) (dot bx by bx by)) 255)
        n (count ramp)
        c (min (dec n) (quot (* gray n) 256))]
    (nth ramp c)))

(defn amplitude
  "https://www.shadertoy.com/view/4llyR7"
  [x y w h ramp]
  (let [uv (re-scale (vec2 (/ x w) (/ y h)))
        uv (update uv :x #(* %1 (/ h w)))
        f1 32
        f2 16
        t 1
        d1 (length (sub uv (vec2 0.5 -0.1)))
        d2 (length (sub uv (vec2 -0.5 0.1)))
        s 2
        amp (+ (js/Math.sin (- (* f1 d1) (* t s)))
               (js/Math.cos (- (* f1 d1) (* t s)))
               (js/Math.sin (- (* f2 d2) (* t s)))
               (js/Math.cos (- (* f2 d2) (* t s))))
        gray (max 0 (min 255 (js/Math.floor (* (+ amp 1) 128))))
        n (count ramp)
        c (min (dec n) (quot (* gray n) 256))]
    (nth ramp c)))

(comment
  (amplitude 0 0 3 3 ramp))
