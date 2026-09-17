(ns app.core)

(enable-console-print!)

(defn create-key [prefix idx] (str prefix "-" idx))

(comment
  (create-key "hi" 1))

(defn create-el
  ([tag] (create-el tag nil))
  ([tag children]
   (let [el (.createElement js/document tag)]
     (if (nil? children) el (doto el (.appendChild children))))))

(defn create-description-el
  [index]
  (let [k (create-key "description" index)
        label (doto (create-el "label")
                (.setAttribute "for" k)
                (#(set! (.-innerText %1) "Description:")))
        input (doto (create-el "textarea")
                (.setAttribute "name" k)
                (.setAttribute "id" k)
                (.setAttribute "rows" "8")
                (.setAttribute "cols" "23"))
        div (doto (create-el "div") (.appendChild label) (.appendChild input))]
    div))

(defn create-quantity-el
  [index]
  (let [k (create-key "qty" index)
        label (doto (create-el "label")
                (.setAttribute "for" k)
                (#(set! (.-innerText %1) "Quantity:")))
        input (doto (create-el "input")
                (.setAttribute "type" "number")
                (.setAttribute "name" k)
                (.setAttribute "id" k))
        div (doto (create-el "div") (.appendChild label) (.appendChild input))]
    div))

(defn create-rate-el
  [index]
  (let [k (create-key "rate" index)
        label (doto (create-el "label")
                (.setAttribute "for" k)
                (#(set! (.-innerText %1) "Rate:")))
        input (doto (create-el "input")
                (.setAttribute "type" "number")
                (.setAttribute "name" k)
                (.setAttribute "id" k)
                (.setAttribute "step" "0.1"))
        div (doto (create-el "div") (.appendChild label) (.appendChild input))]
    div))

(defn go
  []
  (let [form (.querySelector js/document "form")
        submit-button-wrapper (.-parentNode (.querySelector js/document
                                                            "[type=submit]"))
        add-button (.querySelector js/document "button")]
    (.addEventListener
     add-button
     "click"
     (fn []
       (doto form
         (.insertBefore (create-description-el 1) submit-button-wrapper)
         (.insertBefore (create-quantity-el 1) submit-button-wrapper)
         (.insertBefore (create-rate-el 1) submit-button-wrapper))))
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
