(ns app.storage
  (:require [cljs.reader :as reader]))


(defn save! [k v] (.setItem js/localStorage (name k) (pr-str v)))
(defn load
  [k]
  (some-> (.getItem js/localStorage (name k))
          reader/read-string))
(defn forget! [k] (.removeItem js/localStorage (name k)))
