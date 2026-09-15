(ns app.core
  (:require [app.render
             :as
             render
             :refer
             [blank-grid clear box put-str text-box render]]
            [app.shader :refer [shader diagonal-gradient-frag paul-ramp]]))

(def MAX_WIDTH 95)
(def MAX_HEIGHT 73)

(defn draw!
  []
  (let [grid (-> (blank-grid MAX_WIDTH MAX_HEIGHT)
                 (shader diagonal-gradient-frag paul-ramp)
                 (clear 17 19 66 20)
                 (box 20 20 60 18)
                 (box 24 23 28 6)
                 (put-str 26 25 "hello, ascii")
                 (put-str 26 26 "edit src/app/core.cljs")
                 (text-box 20
                           45
                           ["This is my text box"
                            "another string"
                            "some other stuff"
                            "a new line"]))]
    (set! (.-textContent (.getElementById js/document "screen"))
          (render grid))))

(defn ^:export init [] (draw!))
