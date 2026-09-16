(ns app.core
  (:require [clojure.string :as str]
            [app.render
             :as
             render
             :refer
             [blank-grid text-box render horzontal-line table MAX_WIDTH]]
            [app.shader :refer [shader diagonal-gradient-frag paul-ramp]]
            [app.example.data :refer [client payment company]]))

(defn str-pair ([a b] (str-pair a b " ")) ([a b sep] (str/join sep [a b])))

(defn company-details
  [grid x y]
  (text-box grid
            x
            y
            (flatten [(:name company)
                      (vals (:address company))
                      (:email company)
                      (:phone company)
                      (str/join " " ["Company No:" (:number company)])])
            {:px 2
             :py 1
             :mx 2
             :my 1
             :border true}))

(defn invoice-details
  [grid x y invoice-number date due-date]
  (text-box grid
            x
            y
            [#_"Invoice"
             (str-pair "Invoice #" invoice-number)
             (str-pair "Date" date)
             (str-pair "Due Date" due-date)]
            {:px 2
             :py 1
             :mx 2
             :my 1
             :border true}))

(defn client-details
  [grid x y]
  (text-box grid
            x
            y
            (flatten ["Bill to:" (:name client) (vals (:address client))])
            {:px 2
             :py 1
             :mx 2
             :my 1
             :border true}))

(defn payment-details
  [grid x y]
  (text-box grid
            x
            y
            [(str-pair "Bank" (:bank payment) ": ")
             (str-pair "Sort Code" (:sort-code payment) ": ")
             (str-pair "Account" (:account payment) ": ")
             (str-pair "IBAN" (:iban payment) ": ")]
            {:px 2
             :py 1
             :mx 2
             :my 1
             :border true}))

(def table-data
  [{:description
    "22 days work but actually I did loads of stuff which I am going to mention that I did it but I don't have time to go into it now."
    :qty 22
    :rate 450}
   {:description "Some materials"
    :qty 1
    :rate 42.50}])

(defn draw!
  []
  (let [grid (-> (blank-grid)
                 (shader diagonal-gradient-frag paul-ramp)
                 (company-details 8 5)
                 (invoice-details 63
                                  5
                                  1
                                  (.toDateString (js/Date.))
                                  (.toDateString (js/Date.)))
                 (client-details 8 21)
                 (horzontal-line 2 33 (- MAX_WIDTH 3))
                 (table 8 40 table-data {:desc-width 35})
                 (payment-details 8 60))]
    (set! (.-textContent (.getElementById js/document "screen"))
          (render grid))))


(defn ^:export init [] (draw!))
