(ns qoback.closspad.network.query
  (:require [cljs.reader :as reader]
            [qoback.closspad.state.db :refer [get-dispatcher]]))

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

(defn query->http-request [{:query/keys [kind data]}]
  (case kind
    :query/matches
    [:get "v1/CLOSSPAD_match?select=*"]
    :query/user
    [:get (str "/api/todo/users/" (:user-id data))]))

;; TODO: tengo que emitir evento que se encargue de actualizar, por lo que hay que obtener (o pasarle)
;; el `dispatcher`.
(defn query-backend
  [params]
  (let [[method url] (query->http-request params)
        dispatcher (get-dispatcher)]
    (-> (js/fetch (str base-url url)
                  (clj->js {:method (name method)
                            :headers {:apiKey anon-key
                                      :Authorization (str "Bearer " anon-key)}}))
        (.then #(.json %))
        (.then #(js->clj % {:keywordize-keys true}))
        (.then (fn [ms]
                 (dispatcher nil [[:db/assoc :match {:results ms}]])))
        (.catch #(.log js/console %)))))
