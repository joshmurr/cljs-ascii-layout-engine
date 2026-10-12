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

(defn format-qty
  ([v] (format-qty v 8))
  ([v pad] (.padStart (.toString v) pad " ")))

(defn format-money
  ([v] (format-money v 8))
  ([v pad]
   (.padStart (str "$" (.toString (.toFixed (js/parseFloat v) 2))) pad " ")))

(defn pad-start [s pad] (.padEnd (.toString s) pad " "))
(defn pad-end [s pad] (.padStart (.toString s) pad " "))
(defn padder [align] (if (= align "r") pad-start pad-end))

(defn pre-process-table
  ([data] (pre-process-table data []))
  ([data
    {:keys [desc-width]
     :or {desc-width 28}}]
   (reduce-kv
    (fn [acc i item]
      (conj acc
            (assoc item
                   :key [i]
                   :description (split-text (:description item) desc-width)
                   :qty [(:qty item)]
                   :rate [(:rate item)]
                   :total [(* (:qty item) (:rate item))]
                   :y-offset (+ (* i 3) (count (:description (last acc)))))))
    []
    data)))

(defn make-table-headers
  ([grid x y] (make-table-headers grid x y {}))
  ([grid
    x
    y
    {:keys [desc-width cell-width gutter]
     :or {desc-width 28
          cell-width 10
          gutter 2}}]
   (-> grid
       (clear (- x gutter)
              (+ (- y gutter) 0)
              (+ gutter (* 4 cell-width) desc-width (* 4 gutter))
              (+ 3 (* 2 gutter)))
       (text-box x y ["Item No."])
       (text-box (+ x cell-width gutter) y ["Description"])
       (text-box (+ x desc-width cell-width gutter) y ["Qty"])
       (text-box (+ x desc-width (* 2 (+ cell-width gutter))) y ["Rate"])
       (text-box (+ x desc-width (* 3 (+ cell-width gutter))) y ["Total"]))))

#_(defn make-table-footer
    ([grid x y] (make-table-footer grid x y {}))
    ([grid
      x
      y
      {:keys [desc-width cell-width gutter]
       :or {desc-width 28
            cell-width 10
            gutter 2}}]
     (-> grid
         (clear (- x gutter)
                (+ (- y gutter) 0)
                (+ gutter (* 4 cell-width) desc-width (* 4 gutter))
                (+ 3 (* 2 gutter)))
         (text-box x y ["Item No."])
         (text-box (+ x cell-width gutter) y ["Description"])
         (text-box (+ x desc-width cell-width gutter) y ["Qty"])
         (text-box (+ x desc-width (* 2 (+ cell-width gutter))) y ["Rate"])
         (text-box (+ x desc-width (* 3 (+ cell-width gutter))) y ["Total"]))))

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
   (reduce
    (fn [g v]
      (let [y-off (+ (:y-offset v) 3)
            desc (:description v)]
        (-> g
            (clear (- x gutter)
                   (+ (- y gutter) y-off)
                   (+ gutter (* 4 cell-width) desc-width (* 4 gutter))
                   (+ (count desc) (* 2 gutter)))
            #_(box (- x gutter)
                   (- y gutter)
                   (+ gutter (* 4 cell-width) desc-width (* 4 gutter))
                   (+ (count desc) (* 2 gutter)))
            (text-box x (+ y y-off) (:key v))
            (text-box (+ x cell-width gutter) (+ y y-off) desc)
            (text-box (+ x desc-width cell-width gutter) (+ y y-off) (:qty v))
            (text-box (+ x desc-width (* 2 (+ cell-width gutter)))
                      (+ y y-off)
                      (:rate v))
            (text-box (+ x desc-width (* 3 (+ cell-width gutter)))
                      (+ y y-off)
                      (:total v)))))
    (make-table-headers grid x y table-opts)
    (pre-process-table data table-opts))))

#_(def config
    {:description 28
     :cell-width 10})

(defn create-line
  [item idx config]
  (str/join
   " "
   (map (fn [{:keys [item-key width]}]
          (let [_v (item-key item)
                v (get-in item [item-key idx])
                w (or width 10)]
            (if (nil? v) (str/join "" (repeat w " ")) (.padEnd (str v) w " "))))
        config)))

(defn table-2
  [unprocessed config]
  (mapcat (fn [line-item]
            (reduce (fn [acc [idx]]
                      (let [line (create-line line-item idx config)]
                        (conj acc line)))
                    []
                    (map-indexed vector (get line-item :description))))
   (pre-process-table unprocessed config)))

(comment
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
  (pre-process-table sample)
  (defn first-vector "Ignores all args other than first" [t & _] (vector t))
  (defn cells
    [row column-spec]
    (mapv (fn [{:keys [item-key xform width align]}]
            (let [item (item-key row)
                  pad (padder align)
                  xform (or xform first-vector)
                  f (xform item width)]
              (mapv #(pad %1 (or width 10)) f)))
          column-spec))
  (def col-spec
    [{:header "Description"
      :item-key :description
      :width 40
      :xform (fn [this width] (split-text this width))
      :align "l"}
     {:header "Quantity"
      :item-key :qty
      :align "r"}
     {:header "Rate"
      :item-key :rate
      :align "r"}
     {:header "Total"
      :item-key (fn [this] (* (:qty this) (:rate this)))
      :align "r"}])
  (map (fn [row]
         (-> row
             (cells col-spec)
             #_(format col-spec)))
       sample)
  (table-2 sample
           [{:item-key :description
             :width 40
             :xform (fn [this] (split-text this 40))}
            {:item-key :qty}
            {:item-key :rate}
            ; total
           ])
  (render (text-box (blank-grid 100 10) 0 0 ["hello this is some text"])))

(defn render [grid] (str/join "\n" (map str/join grid)))
