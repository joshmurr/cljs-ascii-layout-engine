(ns app.core
  (:require [app.storage :refer [save!]]))

(enable-console-print!)

(def line-item-fields
  [{:id "description"
    :label "Description:"
    :tag "textarea"
    :attrs {:rows 8
            :cols 23}}
   {:id "qty"
    :label "Quantity:"
    :tag "input"
    :attrs {:type "number"}}
   {:id "rate"
    :label "Rate:"
    :tag "input"
    :attrs {:type "number"
            :step 0.1}}])

(defn create-key [prefix idx] (str prefix "-" idx))

(defn ->node [n] (if (string? n) (.createTextNode js/document n) n))

(defn create-el
  ([tag] (create-el tag nil nil))
  ([tag attrs] (create-el tag attrs nil))
  ([tag attrs children]
   (let [el (.createElement js/document tag)]
     (doseq [[k v] attrs] (.setAttribute el (name k) (str v)))
     (doseq [child children] (.appendChild el (->node child)))
     el)))


(defn create-field
  [index {:keys [id label tag attrs]}]
  (let [k (create-key id index)]
    (create-el "div"
               nil
               [(create-el "label" {:for k} [label])
                (create-el tag
                           (merge {:name id
                                   :id k}
                                  attrs))])))

(defn create-line-item [index] (map #(create-field index %) line-item-fields))

(defn line-items
  [form-data fields]
  (let [ks (map (comp keyword :id) fields)
        columns (map #(vec (.getAll form-data (:id %))) fields)]
    (apply mapv (fn [& vs] (zipmap ks vs)) columns)))

(defn init-invoice-form
  []
  (let [form (.getElementById js/document "invoice-form")
        submit-button-wrapper (.-parentNode (.querySelector form
                                                            "[type=submit]"))
        add-button (.querySelector js/document "button")
        line-item-count (atom 1)]
    (.addEventListener add-button
                       "click"
                       (fn []
                         (doseq [el (create-line-item @line-item-count)]
                           (.insertBefore form el submit-button-wrapper))
                         (swap! line-item-count inc)))
    (doto form
      (.addEventListener "submit"
                         (fn [e] (.preventDefault e) (js/FormData. form)))
      (.addEventListener "formdata"
                         (fn [e]
                           (let [form-data (.-formData e)
                                 data (line-items form-data line-item-fields)
                                 k (.get form-data "invoice-number")]
                             (save! (str "invoice-" k) data)
                             (set! (.-href js/location)
                                   (str "invoice.html?invoice=" k))))))))

(defn init-dividends-form
  []
  (let [form (.getElementById js/document "dividends-form")]
    (doto form
      (.addEventListener "submit"
                         (fn [e] (.preventDefault e) (js/FormData. form)))
      (.addEventListener
       "formdata"
       (fn [e]
         (let [form-data (.-formData e)
               data (line-items
                     form-data
                     [{:id "amount"} {:id "date-time"} {:id "voucher-number"}])
               k (.get form-data "voucher-number")]
           (save! (str "dividend-" k) data)
           (set! (.-href js/location)
                 (str "dividend-voucher.html?voucher-number=" k))))))))

(defn go [] (init-invoice-form) (init-dividends-form))

(defn ^:export init [] (go))
