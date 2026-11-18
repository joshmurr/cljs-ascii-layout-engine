(ns app.fields)

(def line-item-fields
  [{:id    "description"
    :label "Description:"
    :tag   "textarea"
    :attrs {:rows 8
            :cols 23}}
   {:id    "qty"
    :label "Quantity:"
    :tag   "input"
    :attrs {:type "number"}}
   {:id    "rate"
    :label "Rate:"
    :tag   "input"
    :attrs {:type "number"
            :step 0.1}}])


