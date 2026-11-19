(ns build.pages
  (:require [clojure.java.io :as io]
            [hiccup2.core :as h]
            [app.fields :refer [line-item-fields]]))

(def font-url
  (str "https://fonts.googleapis.com/css2?family=IBM+Plex+Mono:ital,wght@"
       "0,100;0,200;0,300;0,400;0,500;0,600;0,700;"
       "1,100;1,200;1,300;1,400;1,500;1,600;1,700&display=swap"))

(def nav-links [["/index.html" "Home"]])

(defn create-key [prefix idx] (str prefix "-" idx))

(defn invoice-nav
  []
  [:nav
   [:h3 "Invoices"]
   [:ol#invoice-nav]])

(defn dividends-nav
  []
  [:nav
   [:h3 "Dividends"]
   [:ol#dividend-nav]])

(defn invoice-form
  []
  [:form
   {:id     "invoice-form"
    :action ""
    :method "POST"}
   [:div
    [:label {:for "invoice-number"} "Invoice number:"]
    [:input
     {:type     "number"
      :name     "invoice-number"
      :id       "invoice-number"
      :step     "1"
      :required true}]]
   [:div
    [:label {:for "issue-date"} "Issue Date:"]
    [:input
     {:type     "date"
      :name     "issue-date"
      :id       "issue-date"
      :required true}]]
   [:div
    [:label {:for "due-date"} "Due Date:"]
    [:input
     {:type     "date"
      :name     "due-date"
      :id       "due-date"
      :required true}]]
   (map (fn [[idx item]]
          (let [k (create-key (:id item) idx)]
            [:div
             [:label {:for k} (:label item)]
             [(keyword (:tag item))
              (merge {:required true
                      :id       k
                      :name     (:id item)}
                     (:attrs item))]]))
        (map-indexed vector line-item-fields))
   [:div.button-container
    [:button {:id "add-line-item"} "Add line item"]
    [:input {:type  "submit" :value "Invoice!"}]]])

(defn dividend-form
  []
  [:form
   {:id     "dividend-form"
    :action ""
    :method "POST"}
   [:div
    [:label {:for "voucher-number"} "Voucher number:"]
    [:input
     {:type     "text"
      :name     "voucher-number"
      :id       "voucher-number"
      :required true}]]
   [:div
    [:label {:for "date"} "Date:"]
    [:input
     {:type     "date"
      :name     "date"
      :id       "date"
      :required true}]]
   [:div
    [:label {:for "payment-date"} "Payment date:"]
    [:input
     {:type     "date"
      :name     "payment-date"
      :id       "payment-date"
      :required true}]]
   [:div
    [:label {:for "amount-per-share"} "Dividend per share (£):"]
    [:input
     {:type     "number"
      :name     "amount-per-share"
      :id       "amount-per-share"
      :step     "0.01"
      :min      "0.01"
      :required true}]]
   [:div.button-container
    [:button {:type "submit" :value "dividend-voucher"} "Dividends!"]
    [:button {:type "submit" :value "board-minutes"} "Board minutes!"]]])

(def pages
  [{:file    "index.html"
    :title   "Home"
    :modules ["main"]
    :body
    [:main
     (invoice-nav)
     (dividends-nav)
     [:section (invoice-form) ]
     [:section
      (dividend-form)]]}
   {:file    "invoice.html"
    :title   "Invoice"
    :modules ["invoice"]
    :body    [:main [:pre#screen "invoice"]]}
   {:file    "dividend-voucher.html"
    :title   "Dividends voucher"
    :modules ["dividends"]
    :body    [:main [:pre#screen "dividends-voucher"]]}
   {:file    "board-minutes.html"
    :title   "Board minutes"
    :modules ["dividends"]
    :body    [:main [:pre#screen "board-minutes"]]}])

(defn nav
  []
  [:nav [:ul (for [[href label] nav-links] [:li [:a {:href href} label]])]])

(defn head
  [title]
  [:head
   [:meta {:charset "utf-8"}]
   [:title (if title (str "ascii-layout | " title) "ascii-layout")]
   [:link
    {:rel  "preconnect"
     :href "https://fonts.googleapis.com"}]
   [:link
    {:crossorigin true
     :href        "https://fonts.gstatic.com"
     :rel         "preconnect"}]
   [:link
    {:href font-url
     :rel  "stylesheet"}]
   [:link
    {:href "/css/main.css"
     :rel  "stylesheet"}]])

(defn layout
  [{:keys [title modules body]}]
  [:html
   {:lang "en"}
   (head title)
   [:body
    (nav)
    body
    (for [m (cons "shared" modules)] [:script {:src (str "/js/" m ".js")}])]])

(defn render
  [page]
  (str "<!doctype html>\n" (h/html {:mode :html} (layout page))))

(defn copy-static!
  "Copy everything under public/css into out-dir (release builds only)."
  [out-dir]
  (doseq [f     (file-seq (io/file "public/css"))
          :when (.isFile f)
          :let  [target (io/file out-dir (str (.relativize (.toPath (io/file "public"))
                                                           (.toPath f))))]]
    (io/make-parents target)
    (io/copy f target)))

(defn emit
  {:shadow.build/stage :flush}
  [build-state]
  (let [out-dir (if (= :release (:shadow.build/mode build-state)) "dist" "public")]
    (when (= "dist" out-dir) (copy-static! out-dir))
    (doseq [p pages]
      (let [f (io/file out-dir (:file p))]
        (io/make-parents f)
        (spit f (render p)))))
  build-state)
