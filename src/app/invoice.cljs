(ns app.invoice
  (:require [clojure.string :as str]
            [app.storage :refer [get-data]]
            [app.render
             :as
             render
             :refer
             [blank-grid text-box render make-table split-text format-money]]
            [app.shader :refer [shader amplitude paul-ramp]]
            [app.example.data :refer [client payment company]]))

(defn str-pair ([a b] (str-pair a b " ")) ([a b sep] (str/join sep [a b])))

(def default-opts
  {:px 2
   :py 1
   :mx 4
   :my 2
   :border true})

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
            default-opts))

(defn invoice-details
  [grid x y invoice-number date due-date]
  (text-box grid
            x
            y
            [(str-pair "Invoice #" invoice-number)
             (str-pair "Date" date)
             (str-pair "Due Date" due-date)]
            default-opts))

(defn client-details
  [grid x y]
  (text-box grid
            x
            y
            (flatten ["Bill to:" (:name client) (vals (:address client))])
            default-opts))

(defn payment-details
  [grid x y]
  (text-box grid
            x
            y
            [(str-pair "Bank" (:bank payment) ": ")
             (str-pair "Sort Code" (:sort-code payment) ": ")
             (str-pair "Account" (:account payment) ": ")
             (str-pair "IBAN" (:iban payment) ": ")]
            default-opts))

(defn line-total [{:keys [qty rate]}] (* qty rate))

(defn draw!
  []
  (let [data (get-data "invoice")
        grid (-> (blank-grid)
                 (shader amplitude paul-ramp)
                 (company-details 11 7)
                 (invoice-details 61
                                  7
                                  1
                                  (.toDateString (js/Date.))
                                  (.toDateString (js/Date.)))
                 (client-details 11 27)
                 (payment-details 56 27)
                 (text-box
                  10
                  43
                  (make-table
                   [#_{:item-key :key}
                    {:header "Description"
                     :footer (constantly "Grand Total")
                     :item-key :description
                     :width 43
                     :xform split-text}
                    {:header "Quantity"
                     :item-key :qty
                     :align :right}
                    {:header "Rate"
                     :item-key :rate
                     :xform format-money
                     :align :right}
                    {:header "Total"
                     :footer #(first (format-money (reduce + (map line-total %))
                                                   nil))
                     :align :right
                     :xform format-money
                     :item-key (fn [this] (* (:qty this) (:rate this)))}]
                   data)
                  {:my 3
                   :mx 5}))]
    (set! (.-textContent (.getElementById js/document "screen"))
          (render grid))))

(defn ^:export init [] (draw!))
