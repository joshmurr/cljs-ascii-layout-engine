(ns app.example.data)

(def company
  {:director "The King"
   :name "Acme, Inc."
   :address {:street "123 Main St."
             :city "Anytown"
             :state "CA"
             :zip "12345"}
   :number "17123027"
   :phone "123-456-7890"
   :email "johndoe@me.com"})

(def client
  {:name "John Doe"
   :address {:street "123 Main St."
             :city "Anytown"
             :state "CA"
             :zip "12345"}})

(def payment
  {:bank "Monzo"
   :sort-code "123456"
   :account "12345678"
   :iban "GB29XXXXXXXX0123456789"})

(defn get-vals
  [m]
  (letfn
   [(paths* [vs m]
      (reduce-kv (fn [ps _ v] (if (map? v) (paths* ps v) (conj ps v))) vs m))]
    (paths* () m)))

(comment
  (vals (:address company))
  (get-vals company))
