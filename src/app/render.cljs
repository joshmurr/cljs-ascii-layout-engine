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

(defn clear
  ([grid x y w h] (clear grid x y w h \space))
  ([grid x y w h filler]
   (let [clear-line (str/join (repeat w filler))]
     (reduce (fn [g row] (put-str g x row clear-line))
             grid
             (range y (+ y h))))))

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
      (as-> grid g
        (clear g (- x px mx border-width) (- y py my border-width) width height)
        (cond-> g
          border (box (- x px border-width)
                      (- y py border-width)
                      (- width (* 2 mx))
                      (- height (* 2 my)))))
      (map-indexed vector text)))))

(defn split-text
  "Splits up a long string into a vector of multiple strings
  no longer than line-len."
  [words line-len]
  (reduce (fn [acc word]
            (let [current-line-len (count (last acc))]
              (if (>= (+ current-line-len (count word)) line-len)
                (conj acc word)
                (assoc acc
                       (dec (count acc))
                       (str/trim (str/join " " [(last acc) word]))))))
          [""]
          (str/split words #" ")))

(defn format-qty
  ([v] (format-qty v 8))
  ([v pad] (.padStart (.toString v) pad " ")))

(defn format-money
  ([v] (format-money v 8))
  ([v pad] (.padStart (.toString (.toFixed v 2)) pad " ")))

(defn pre-process-table
  ([data] (pre-process-table data {}))
  ([data
    {:keys [desc-width cell-width]
     :or {desc-width 28
          cell-width 10}}]
   (reduce-kv
    (fn [acc i item]
      (conj acc
            (assoc item
                   :key [(format-qty (inc i) cell-width)]
                   :description (split-text (:description item) desc-width)
                   :qty [(format-qty (:qty item) cell-width)]
                   :rate [(format-money (:rate item) cell-width)]
                   :total [(format-money (* (:qty item) (:rate item))
                                         cell-width)]
                   :y-offset (+ (* i 3) (count (:description (last acc)))))))
    []
    data)))

(defn table
  ([grid x y data] (table grid x y data {}))
  ([grid
    x
    y
    data
    {:keys [desc-width cell-width gutter]
     :or {desc-width 28
          cell-width 10
          gutter 2}
     :as table-opts}]
   (reduce (fn [g v]
             (-> g
                 (clear (- x 3)
                        (+ (- y 2) (:y-offset v))
                        (+ 3 (* 4 cell-width) desc-width (* 4 gutter))
                        (+ (count (:description v)) (* 2 2)))
                 (text-box x (+ y (:y-offset v)) (:key v))
                 (text-box (+ x cell-width gutter)
                           (+ y (:y-offset v))
                           (:description v))
                 (text-box (+ x desc-width cell-width gutter)
                           (+ y (:y-offset v))
                           (:qty v))
                 (text-box (+ x desc-width (* 2 (+ cell-width gutter)))
                           (+ y (:y-offset v))
                           (:rate v))
                 (text-box (+ x desc-width (* 3 (+ cell-width gutter)))
                           (+ y (:y-offset v))
                           (:total v))))
           grid
           (pre-process-table data table-opts))))

(defn render [grid] (str/join "\n" (map str/join grid)))

(comment
  (name :thing)
  (keyword "thing")
  (str/split "Hello this is a sentence" #" ")
  (def table-data
    [{:description
      "22 days work but actually I did loads of stuff which I am going to mention that I did it but I don't have time to go into it now."
      :qty 22
      :rate 450}
     {:description "Some materials"
      :qty 1
      :rate 42.50}])
  (map? (first table-data)) ;true
  (assoc (first table-data)
         :description (split-text (:description (first table-data)) 20)
         :total (* (:qty (first table-data)) (:rate (first table-data))))
  (pre-process-table table-data)
  (reduce-kv (fn [g k v] (assoc g k v)) {} table-data))
