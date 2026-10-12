(ns app.invoice
  (:require
    [clojure.string :as str]
    [app.storage :refer [get-data]]
    [app.render :as render :refer [blank-grid text-box render table-2]]
    [app.shader
     :refer
     [shader amplitude ramp arrow-ramp block-ramp paul-ramp braile-ramp]]
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

(defn draw!
  []
  (let [data (get-data "invoice")
        grid (-> (blank-grid)
                 (shader amplitude block-ramp)
                 (company-details 11 7)
                 (invoice-details 61
                                  7
                                  1
                                  (.toDateString (js/Date.))
                                  (.toDateString (js/Date.)))
                 (client-details 11 27)
                 (payment-details 56 27)
                 #_(horzontal-line 2 33 (- MAX_WIDTH 3))
                 #_(clear 4 40 (- MAX_WIDTH 8) 15)
                 #_(table 8
                          43
                          data
                          {:desc-width 32
                           :gutter 1})
                 (text-box 8
                           43
                           (table-2 data
                                    [{:item-key :key}
                                     {:item-key :description
                                      :width 20}
                                     {:item-key :qty}
                                     {:item-key :rate}
                                     {:item-key :total}])
                           {:my 3
                            :mx 2}))]
    (print data)
    (set! (.-textContent (.getElementById js/document "screen"))
          (render grid))))


(defn ^:export init [] (draw!))
