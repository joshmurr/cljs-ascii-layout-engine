(ns app.config
  (:require [cljs.reader :as reader]
            [shadow.resource :as rc]))

;; Inlined at compile time; shadow-cljs recompiles when config.edn changes.
(def config (reader/read-string (rc/inline "app/config.edn")))

(def company (:company config))
(def client (:client config))
(def payment (:payment config))
