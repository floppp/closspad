(ns qoback.closspad.network.query
  (:require [cljs.core.async :as async]
            [cljs.core.async.interop :refer [<p!]]
            [qoback.closspad.network.domain :refer [base-url query->http-request]]
            [qoback.closspad.utils.datetime :as dt]))

(defn GET
  ([url] (GET url nil))
  ([url options]
   (let [ch (async/chan)]
     (async/go
       (try
         (let [response (<p! (js/fetch url (clj->js options)))
               json (-> response .json .then <p!)]
           (async/>! ch {:data (js->clj json {:keywordize-keys true})}))
         (catch js/Error err
           (js/console.warn (ex-cause err))
           (async/>! ch {:error (ex-cause err)}))))
     ch)))

(def http-verb-handlers
  {:get #'GET})

(defn query-async
  [params]
  (let [request-params (query->http-request (assoc params :query/date (dt/date->minus-one-year)))
        {:keys [method url options on-success on-failure]} request-params
        http-handler (method http-verb-handlers)
        chan (http-handler
              (str base-url "rest/" url)
              (assoc options :signal (js/AbortSignal.timeout 5000)))]
    (async/go
      (let [{:keys [error data]} (async/<! chan)]
        (if error
          (when on-failure (on-failure error))
          (when on-success (on-success data)))))))
