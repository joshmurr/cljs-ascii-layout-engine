(ns app.core
  (:require [clojure.string :as str]
            [app.storage :refer [save-with-timestamp! get-keys-of]]
            [app.fields :refer [line-item-fields]]))

(enable-console-print!)

(defn form->map [form]
  (-> (js/Object.fromEntries (js/FormData. form))
      (js->clj :keywordize-keys true)))

(defn form-data->map [fd]
  (js->clj (js/Object.fromEntries fd) :keywordize-keys true))

(defn create-key [prefix idx] (str prefix "-" idx))

(defn ->node [n] (if (string? n) (.createTextNode js/document n) n))

(defn html
  "Hiccup parser"
  [x]
  (cond
    (nil? x)    (.createDocumentFragment js/document)
    (vector? x) (let [[tag & more]     x
                      [attrs children] (if (map? (first more))
                                         [(first more) (rest more)]
                                         [nil more])
                      el               (.createElement js/document (name tag))]

                  (doseq [[k v] attrs]
                    (if (fn? v)
                      (.addEventListener el (subs (name k) 3) v)
                      (.setAttribute el (name k) (str v))))
                  (doseq [c children] (.appendChild el (html c)))
                  el)
    (seq? x)              (let [frag (.createDocumentFragment js/document)]
                            (doseq [c x] (.appendChild frag (html c)))
               frag)
    (instance? js/Node x) x
    :else                 (.createTextNode js/document (str x))))

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

(defn change-location [path] (set! (.-href js/location) path))

(defn id->url [id] (let [[kind number] (str/split (name id) "-")]
                     (case kind
                       "invoice"  (str "/invoice.html?id=" number)
                       "dividend" (str "/dividend-voucher.html?id=" number)
                       "#")))

(defn populate-form-nav [type]
  (let [parent (.getElementById js/document (str type "-nav"))]
    (.replaceChildren parent
                      (html (for [k (get-keys-of type)] [:li [:a {:href (id->url k)} (name k)]])))))

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
    (.addEventListener form "submit"
                       (fn [e]
                         (.preventDefault e)
                         (let [fd   (js/FormData. form)
                               data (form-data->map fd)
                               id   (:invoice-number data)]
                           (save-with-timestamp! (str "invoice-" id)
                                                 (-> data
                                                     (select-keys [:issue-date :due-date])
                                                     (assoc :id id
                                                            :line-items (line-items fd line-item-fields))))
                           (change-location (str "invoice.html?id=" id)))))))


(defn init-dividends-form
  []
  (let [form                 (.getElementById js/document "dividend-form")]
    (.addEventListener form "submit"
                       (fn [e]
                         (.preventDefault e)
                         (let [data (form->map form)
                               k (:voucher-number data)
                               page (.. e -submitter -value)]
                           (save-with-timestamp! (str "dividend-" k) data)
                           (change-location (str page ".html?id=" k)))))))

(defn go []
  (init-invoice-form)
  (init-dividends-form)
  (populate-form-nav "invoice")
  (populate-form-nav "dividend"))

(defn ^:export init [] (go))
