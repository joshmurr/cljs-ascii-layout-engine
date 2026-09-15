(ns app.core
  (:require [app.render :as render :refer [blank-grid text-box render]]
            [app.shader :refer [shader diagonal-gradient-frag paul-ramp]]))


(defn draw!
  []
  (let [grid (-> (blank-grid)
                 (shader diagonal-gradient-frag paul-ramp)
                 (text-box 22
                           25
                           ["Hello, ASCII" "edit src/app/core.cljs"]
                           {:px 2
                            :py 1
                            :mx 2
                            :my 1
                            :border true})
                 (text-box 20
                           45
                           ["This is my text box"
                            "another string"
                            "some other stuff"
                            "a new line"]
                           {:px 2
                            :py 1
                            :mx 2
                            :my 1
                            :border true}))]
    (set! (.-textContent (.getElementById js/document "screen"))
          (render grid))))

(defn ^:export init [] (draw!))
