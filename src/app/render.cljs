(ns app.render
  (:require [clojure.string :as str]))

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

(defn space ([w] (space w \space)) ([w filler] (str/join (repeat w filler))))

(defn clear
  ([grid x y w h] (clear grid x y w h \space))
  ([grid x y w h filler]
   (let [clear-line (space w filler)]
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

(def border-plus "-|++++")
(def border-simple "─│┌┐└┘")

(defn box
  ([grid x y w h] (box grid x y w h border-simple))
  ([grid x y w h border]
   (let [x2 (+ x (dec w))
         y2 (+ y (dec h))]
     (-> (reduce (fn [g i]
                   (-> g
                       (put (+ x i) y (nth border 0))
                       (put (+ x i) y2 (nth border 0))))
                 grid
                 (range 1 (dec w)))
         (as-> g
           (reduce (fn [g i]
                     (-> g
                         (put x (+ y i) (nth border 1))
                         (put x2 (+ y i) (nth border 1))))
                   g
                   (range 1 (dec h))))
         (put x y (nth border 2))
         (put x2 y (nth border 3))
         (put x y2 (nth border 4))
         (put x2 y2 (nth border 5))))))

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
  ([words line-len] (split-text words line-len #" "))
  ([words line-len splitter]
   (reduce (fn [acc word]
             (let [last-line (last acc)
                   current-line-len (count last-line)]
               (if (>= (+ current-line-len (count word)) line-len)
                 (conj acc word) ; New line
                 (assoc acc
                        (dec (count acc))
                        (str/trim (str/join " " [(last acc) word]))))))
           [""]
           (str/split words splitter))))

(defn format-money [v _] [(str "$" (.toString (.toFixed (js/parseFloat v) 2)))])

(defn pad-start [s pad] (.padStart (.toString s) pad " "))
(defn pad-end [s pad] (.padEnd (.toString s) pad " "))
(defn padder [align] (if (= align :right) pad-start pad-end))

(def sample
  [{:description "This was some serious stuff."
    :qty "3"
    :rate "350"}
   {:description "It took me ages"
    :qty "3"
    :rate "100"}
   {:description
    "It took me ages lorem ipsum dolor sit amet consectetur adipiscing elit."
    :qty "5"
    :rate "300"}
   {:description "I'm being cereal."
    :qty "4"
    :rate "19.50"}])

(def default-spec
  {:width 10
   :align :left
   :xform (fn [s _] [(str s)])})

(defn normalize-spec [spec] (mapv #(merge default-spec %) spec))

(def col-spec
  [{:header "Description"
    :item-key :description
    :width 40
    :xform split-text}
   {:header "Quantity"
    :item-key :qty}
   {:header "Rate"
    :item-key :rate
    :align "r"}
   {:header "Total"
    :item-key (fn [this] (* (:qty this) (:rate this)))
    :align "r"}])

(defn fill-in-gaps
  [row column-spec]
  (let [height (apply max (map count row))]
    (map (fn [cell {:keys [width]}]
           (take height (concat cell (repeat (space width)))))
         row
         column-spec)))

(defn cells
  [col-spec lines-fn]
  (mapv (fn [{:keys [width align]
              :as col}]
          (let [pad (padder align)] (mapv #(pad % width) (lines-fn col))))
        col-spec))

(defn row->lines
  [col-spec lines-fn]
  (apply map str (fill-in-gaps (cells col-spec lines-fn) col-spec)))

(defn item-lines
  [item]
  (fn [{:keys [item-key xform width]}] (xform (item-key item) width)))

(defn header-lines [{:keys [header]}] [header])

(defn footer-lines
  [items]
  (fn [{:keys [footer]}] [(if footer (str (footer items)) "")]))

(defn seperator-line [{:keys [width]}] [(str/join (take width (repeat "-")))])

(defn make-table
  [col-spec items]
  (let [spec (normalize-spec col-spec)]
    (concat (row->lines spec header-lines)
            (row->lines spec seperator-line)
            (mapcat #(row->lines spec (item-lines %)) items)
            (row->lines spec seperator-line)
            (row->lines spec (footer-lines items)))))

(comment
  (make-table sample col-spec)
  (render (text-box (blank-grid 100 10) 0 0 ["hello this is some text"])))

(defn render [grid] (str/join "\n" (map str/join grid)))
