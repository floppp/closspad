(ns qoback.closspad.network.query
  (:require [cljs.core.async :as async]
            [cljs.core.async.interop :refer [<p!]]
            [qoback.closspad.state.db :refer [get-dispatcher]]
            [qoback.closspad.rating-system :refer [process-matches]]))

(defn take-until [f xs]
  (loop [res []
         xs (seq xs)]
    (cond
      (nil? xs) res
      (f (first xs)) (conj res (first xs))
      :else (recur (conj res (first xs)) (next xs)))))

(defn add-log-entry [log entry]
  (cond->> (cons entry log)
    (= :query.status/success (:query/status entry))
    (take-until #(= (:query/status %) :query.status/loading))))

(defn send-request [state now query]
  (update-in state [::log query] add-log-entry
             {:query/status :query.status/loading
              :query/user-time now}))

(defn ^{:indent 2} receive-response [state now query response]
  (update-in state [::log query] add-log-entry
             (cond-> {:query/status (if (:success? response)
                                      :query.status/success
                                      :query.status/error)
                      :query/user-time now}
               (:success? response)
               (assoc :query/result (:result response)))))

(defn get-log [state query]
  (get-in state [::log query]))

(defn get-latest-status [state query]
  (:query/status (first (get-log state query))))

(defn loading? [state query]
  (= :query.status/loading
     (get-latest-status state query)))

(defn available? [state query]
  (->> (get-log state query)
       (some (comp #{:query.status/success} :query/status))
       boolean))

(defn error? [state query]
  (= :query.status/error
     (get-latest-status state query)))

(defn get-result [state query]
  (->> (get-log state query)
       (keep :query/result)
       first))

(defn requested-at [state query]
  (->> (get-log state query)
       (drop-while #(not= :query.status/loading (:query/status %)))
       first
       :query/user-time))

(def anon-key "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im9ycXdpdWJub3pmb3VhcXl5d2FoIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MjEzOTkzNzgsImV4cCI6MjAzNjk3NTM3OH0.6vRnb5W7DHSxpt-gQmbL9q_wRJzNW_tgks9Tu16vq1E")

(def base-url "https://orqwiubnozfouaqyywah.supabase.co/rest/")

(defn query->http-request
  [{:query/keys [kind data]}]
  (case kind
    :query/matches
    {:method  :get
     :url     "v1/CLOSSPAD_match?select=*&order=played_at.asc"
     :options {:method (name :get)
               :headers
               {:apiKey anon-key
                :Authorization (str "Bearer " anon-key)}}
     :callback (fn [ms]
                 (let [ratings (process-matches ms)
                       dispatcher (get-dispatcher)]
                   (dispatcher nil [[:db/assoc :classification {:ratings ratings}]
                                    [:db/assoc :match {:results ms}]])))}
    :query/user
    [:get (str "/api/todo/users/" (:user-id data))]))

(defn query-fetch
  [params]
  (let [{:keys [url options callback]} (query->http-request params)]
    (-> (js/fetch (str base-url url) (clj->js options))
        (.then #(.json %))
        (.then #(js->clj % {:keywordize-keys true}))
        (.then callback)
        (.catch #(js/console.warn (ex-cause %))))))


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

