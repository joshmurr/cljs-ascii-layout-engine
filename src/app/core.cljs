(ns app.core)

(enable-console-print!)

(defn go
  []
  (let [form (.querySelector js/document "form")]
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
