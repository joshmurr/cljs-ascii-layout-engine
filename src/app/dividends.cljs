(ns app.dividends
  (:require [clojure.string :as str]
            [app.example.data :as data]
            [app.render :refer [blank-grid clear text-box split-text render]]))

(defn replace-several
  "https://andersmurphy.com/2019/01/15/clojure-string-interpolation.html"
  [s & {:as replacements}]
  (reduce (fn [s [match replacement]] (str/replace s match replacement))
          s
          replacements))

(defn long-str [& strings] (str/join "\n" strings))

(def board-minutes-str
  (long-str
   "$company-name LIMITED" ""
   "Company Number: $company-number" "Registered Office: $registered-address"
   "" "Minutes of a Meeting of the Board of Directors"
   "" "Date: $date-of-meeting"
   "Time: $time-of-meeting" "Location: $registered-address"
   ""
   "Present: $full-name, Director (sole director, constituting a quorum under the Company's Articles of Association)"))



(defn draw!
  []
  (let [text-content (replace-several
                      board-minutes-str
                      "$company-name" (:name data/company)
                      "$company-number" (:number data/company)
                      "$registered-address"
                      (str/join ", " (vals (:address data/company)))
                      "$full-name" (:director data/company)
                      "$date-of-meeting" (.toDateString (js/Date.))
                      "$time-of-meeting" (.toTimeString (js/Date.)))
        grid (-> (blank-grid)
                 (clear 0 0 80 50 ".")
                 (text-box 0 7 (split-text text-content 80)))]
    (set! (.-textContent (.getElementById js/document "screen"))
          (render grid))))

(defn ^:export init [] (draw!))
