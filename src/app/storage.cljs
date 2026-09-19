(ns app.storage
  (:require [cljs.reader :as reader]
            [clojure.string :as str]))


(defn save! [k v] (.setItem js/localStorage (name k) (pr-str v)))
(defn load
  [k]
  (some-> (.getItem js/localStorage (name k))
          reader/read-string))
(defn forget! [k] (.removeItem js/localStorage (name k)))
(defn get-all
  []
  (reduce (fn [m [k v]] (assoc m (keyword k) (reader/read-string v)))
          {}
          (js->clj (.entries js/Object js/localStorage))))
(defn get-keys
  []
  (sort-by (fn [a] (js/parseInt (last (str/split (name a) #"-"))))
           (keys (get-all))))
(defn get-last [k] (last (filter #(str/includes? (name %) k) (get-keys))))
(defn get-data
  [t]
  (let [url (js/URL. (.-href (.-location js/window)))
        id-param (.get (.-searchParams url) "id")
        data (if id-param (load (str t "-" id-param)) (load (get-last t)))]
    data))

(comment
  (filter (fn [t] (str/includes? (name t) "invoice")) (get-keys))
  (name :invoice)
  (get-all)
  (get-keys)
  (get-last "invoice")
  (get-last "dividend"))
