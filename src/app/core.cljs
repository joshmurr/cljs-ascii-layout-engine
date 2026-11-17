(ns app.core
  (:require [clojure.string :as str]
             [app.storage :refer [save! get-all get-keys]]))

(enable-console-print!)

(def line-item-fields
  [{:id    "description"
    :label "Description:"
    :tag   "textarea"
    :attrs {:rows 8
            :cols 23}}
   {:id    "qty"
    :label "Quantity:"
    :tag   "input"
    :attrs {:type "number"}}
   {:id    "rate"
    :label "Rate:"
    :tag   "input"
    :attrs {:type "number"
            :step 0.1}}])

(defn form->map [form]
  (-> (js/Object.fromEntries (js/FormData. form))
      (js->clj :keywordize-keys true)))

(defn create-key [prefix idx] (str prefix "-" idx))

(defn ->node [n] (if (string? n) (.createTextNode js/document n) n))

(defn html
  "Hiccup parser"
  [x]
  (cond
    (nil? x) (.createDocumentFragment js/document)
    (vector? x) (let [[tag & more] x
                      [attrs children] (if (map? (first more))
                                         [(first more) (rest more)]
                                         [nil more])
                      el (.createElement js/document (name tag))]

                  (doseq [[k v] attrs]
                    (if (fn? v)
                      (.addEventListener el (subs (name k) 3) v)
                      (.setAttribute el (name k) (str v))))
                  (doseq [c children] (.appendChild el (html c)))
                  el)
    (seq? x) (let [frag (.createDocumentFragment js/document)]
               (doseq [c x] (.appendChild frag (html c)))
               frag)
    (instance? js/Node x) x
    :else (.createTextNode js/document (str x))))

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
    (html [:div [:label {:for k} label]
           [(keyword tag) (merge {:name id :id k} attrs)]])))

(defn create-line-item [index] (map #(create-field index %) line-item-fields))

(defn line-items
  [form-data fields]
  (let [ks      (map (comp keyword :id) fields)
        columns (map #(vec (.getAll form-data (:id %))) fields)]
    (apply mapv (fn [& vs] (zipmap ks vs)) columns)))

(defn init-invoice-form
  []
  (let [form                  (.getElementById js/document "invoice-form")
        submit-button-wrapper (.-parentNode (.querySelector form
                                                            "[type=submit]"))
        add-button            (.querySelector js/document "button")
        line-item-count       (atom 1)]
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
                                 data      (line-items form-data line-item-fields)
                                 k         (.get form-data "invoice-number")]
                             (save! (str "invoice-" k) data)
                             (set! (.-href js/location)
                                   (str "invoice.html?id=" k))))))))


(defn id->url [id] (let [[kind number] (str/split (name id) "-")]
                     (case kind
                       "invoice" (str "/invoice.html?id=" number)
                       "dividend" (str "/dividend-voucher.html?id=" number)
                       "#")))

(defn populate-all-forms []
  (let [parent (.getElementById js/document "all-forms")]
    (.replaceChildren parent (html (for [k (get-keys)] [:li [:a {:href (id->url k)} k]])))))

(defn init-dividends-form
  []
  (let [form                 (.getElementById js/document "dividend-form")]
    (.addEventListener form "submit"
                       (fn [e]
                         (.preventDefault e)
                         (let [data (form->map form)
                               k (:voucher-number data)
                               page (.. e -submitter -value)]
                           (save! (str "dividend-" k) data)
                           (set! (.-href js/location) (str page ".html?id=" k)))))))

(defn go [] (init-invoice-form) (init-dividends-form) (populate-all-forms))

(defn ^:export init [] (go))
