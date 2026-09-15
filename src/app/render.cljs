(ns app.render
  (:require [clojure.string :as str]))

(def two-pi (* js/Math.PI 2))
(def MAX_WIDTH 95)
(def MAX_HEIGHT 73)

(defn blank-grid
  ([] (blank-grid MAX_WIDTH MAX_HEIGHT))
  ([cols rows] (vec (repeat rows (vec (repeat cols \space))))))

(defn put
  [grid x y ch]
  (if (and (<= 0 y (dec (count grid))) (<= 0 x (dec (count (first grid)))))
    (assoc-in grid [y x] ch)
    grid))

(defn put-str
  [grid x y s]
  (reduce (fn [g [i ch]] (put g (+ x i) y ch)) grid (map-indexed vector s)))

(defn horzontal-line
  [grid x y w]
  (reduce (fn [g i]
            (-> g
                (put (+ x i) y \-)))
          grid
          (range 1 (dec w))))

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
  ([grid x y text] (text-box grid x y text {}))
  ([grid
    x
    y
    text
    {:keys [px py mx my border]
     :or {px 1
          py 1
          mx 0
          my 0
          border false}}]
   (let [border-width (if border 1 0)
         width (+ (reduce #(max (count %2) %1) 0 text)
                  (* 2 px)
                  (* 2 mx)
                  (* 2 border-width))
         height (+ (count text) (* 2 py) (* 2 my) (* 2 border-width))]
     (reduce
      (fn [g [idx line]] (put-str g x (+ y idx) line))
      (-> grid
          (clear (- x px mx border-width) (- y py my border-width) width height)
          (box (- x px border-width)
               (- y py border-width)
               (- width (* 2 mx))
               (- height (* 2 my))))
      (map-indexed vector text)))))

(comment
  (name :thing)
  (keyword "thing")
  (str/split "Hello this is a sentence" #" ")
  (def table-data
    [{:description "22 days work"
      :qty 22
      :rate 450}
     {:description "Some materials"
      :qty 1
      :rate 42.50}]))

(defn make-lines
  [words line-len]
  (reduce (fn [acc word]
            (let [current-line-len (count (last acc))]
              (if (>= (+ current-line-len (count word)) line-len)
                (conj acc word)
                (assoc acc
                       (dec (count acc))
                       (str/trim (str/join " " [(last acc) word]))))))
          [""]
          words))

(comment
  (last ["a" "b"])
  (count (str/join " " ["abc ced" "word"]))
  (assoc [] 0 (str/join " " [(last ["this is the"]) "word"]))
  (count [1 2 3])
  (str "One" "two")
  (make-lines
   (str/split "Hello this is some words and some things word word word." #" ")
   20))

(defn fixed-width-text-box
  [grid x y width text]
  (let [words (str/split text #" ")
        lines (make-lines words width)]
    (text-box grid x y lines)))

#_(defn table [grid y data] (let [width (- 4 MAX_WIDTH) desc-width 20]))

(defn render [grid] (str/join "\n" (map str/join grid)))
