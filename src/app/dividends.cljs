(ns app.dividends
  (:require [clojure.string :as str]
            [app.example.data :as data]
            [app.render :refer [blank-grid text-box split-text render]]
            [app.shader :refer [shader diagonal-gradient-frag paul-ramp]]))

(defn replace-several
  "https://andersmurphy.com/2019/01/15/clojure-string-interpolation.html"
  [s & {:as replacements}]
  (reduce (fn [s [match replacement]] (str/replace s match replacement))
          s
          replacements))

(defn long-str [& strings] (str/join "\n" strings))

(def board-minutes-str
  (long-str
   "$company-name LIMITED"
   "" "Company Number: $company-number"
   "Registered Office: $registered-address" ""
   "Minutes of a Meeting of the Board of Directors" ""
   "Date: $date-of-meeting" "Time: $time-of-meeting"
   "Location: $registered-address" ""
   "Present: $full-name, Director (sole director, constituting a quorum under the Company's Articles of Association)"
   ""
   "1. Notice and Quorum"
   "The Chair noted that due notice of the meeting had been given and that a quorum was present."
   "" "2. Consideration of Interim Dividend"
   "The Director reviewed the Company's current financial position, including management accounts to $date-of-meeting, and confirmed that the Company has sufficient distributable reserves to support the payment of an interim dividend."
   ""
   "The Director considered the Company's cash flow requirements and ongoing liabilities, including a reserve for Corporation Tax on profits earned to date, and confirmed that payment of the proposed dividend would not prejudice the Company's ability to meet its liabilities as they fall due."
   "3. Resolution"
   "" "IT WAS RESOLVED THAT:"
   "An interim dividend of £$amount per Ordinary £1 share be declared and paid on $payment-date to shareholders on the register at the date of this meeting, being a total distribution of £$amount."
   ""
   "4. Close" "There being no further business, the meeting was closed."
   "" "Signed: $full-name"
   "" "Director and Chair of the Meeting"
   "" "Date: $date-of-meeting"))

(def dividend-voucher-str
  (long-str
   "Dividend Voucher"
   "$company-name LIMITED" "Company Number: $company-number"
   "Registered Office: $registered-address" ""
   "Voucher Number: $voucher-number" "Date of Payment: $payment-date"
   "Shareholder: $full-name" "Shareholder Address: $registered-address"
   "Shareholding: $number Ordinary £1 shares"
   "Dividend per share: £$amount-per-share"
   "Total dividend paid: £$total-amount" ""
   "This interim dividend was declared by resolution of the Board of Directors on $date-of-meeting."
   ""
   "Signed: $full-name" ""
   "Director" "Date: $date-of-meeting"))

(defn draw!
  [string-to-run]
  (let [text-content (replace-several
                      string-to-run
                      "$company-name" (:name data/company)
                      "$company-number" (:number data/company)
                      "$registered-address"
                      (str/join ", " (vals (:address data/company)))
                      "$full-name" (:director data/company)
                      "$date-of-meeting" (.toDateString (js/Date.))
                      "$time-of-meeting" (.toTimeString (js/Date.))
                      "$amount" "0.01"
                      "$payment-date" (.toDateString (js/Date.))
                      "$total-amount" "0.01"
                      "$amount-per-share" "0.01"
                      "$number" "1"
                      "$voucher-number" "80808080")
        grid (-> (blank-grid)
                 (shader diagonal-gradient-frag paul-ramp)
                 (text-box 6
                           4
                           (flatten (map #(split-text %1 74)
                                         (str/split text-content #"\n")))))]
    (set! (.-textContent (.getElementById js/document "screen"))
          (render grid))))

(defn ^:export init
  []
  (if (.includes (.-href (.-location js/window)) "board-minutes.html")
    (draw! board-minutes-str)
    (draw! dividend-voucher-str)))
