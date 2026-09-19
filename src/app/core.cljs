(ns app.core)

(enable-console-print!)

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


(defn create-field
  [index {:keys [id label tag attrs]}]
  (let [k (create-key id index)]
    (create-el "div"
               nil
               [(create-el "label" {:for k} [label])
                (create-el tag
                           (merge {:name k
                                   :id k}
                                  attrs))])))

(defn create-line-item [index] (map #(create-field index %) line-item-fields))

(defn go
  []
  (let [form (.querySelector js/document "form")
        submit-button-wrapper (.-parentNode (.querySelector js/document
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
      (.addEventListener
       "formdata"
       (fn [e]
         (let [data (.-formData e)
               json (.stringify js/JSON (.fromEntries js/Object data))]
           (.setItem js/sessionStorage "form-data" json))
         (set! (.-href js/location) "invoice.html"))))))

(defn ^:export init [] (go))
