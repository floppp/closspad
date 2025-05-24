(ns qoback.closspad.network.core-async
  (:require [cljs.core.async :as async]
            [cljs.core.async.interop :refer [<p!]]
            [qoback.closspad.state.db :refer [get-dispatcher]]
            [qoback.closspad.rating-system :refer [process-matches]]
            [qoback.closspad.network.domain :refer [base-url query->http-request]]))

(defn GET
  ([url] (GET url nil))
  ([url options]
   (let [ch (async/chan)]
     (async/go
       (try
         (let [response (<p! (js/fetch url (clj->js options)))
               json (-> response .json .then <p!)]
           (async/>! ch (js->clj json {:keywordize-keys true})))
         (catch js/Error err (js/console.warn (ex-cause err)))))
     ch)))

(def method-handler {:get #'GET})

(defn query-async
  [params]
  (let [{:keys [method url options callback]} (query->http-request params)
        chan ((method method-handler) (str base-url url) options)]
    (async/go
      (let [data (async/<! chan)]
        (when callback (callback data))))))

