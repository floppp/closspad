(ns qoback.closspad.network.fetch
  (:require [qoback.closspad.network.domain :refer [base-url query->http-request]]))

(defn query-fetch
  [params]
  (let [{:keys [url options callback]} (query->http-request params)]
    (-> (js/fetch (str base-url url) (clj->js options))
        (.then #(.json %))
        (.then #(js->clj % {:keywordize-keys true}))
        (.then callback)
        (.catch #(js/console.warn (ex-cause %))))))
