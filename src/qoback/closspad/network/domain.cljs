(ns qoback.closspad.network.domain
  (:require [qoback.closspad.state.db :refer [get-dispatcher]]
            [qoback.closspad.rating.match :refer [full-matches-process]]
            ["@supabase/supabase-js" :refer [createClient]]))

(goog-define organization "")
(goog-define table "")
(goog-define base-url "")
(goog-define anon-key "")

(defonce supabase (createClient base-url anon-key))

(defn query->http-request
  [{:query/keys [kind data date]}]
  (case kind
    :query/matches
    {:method  :get
     :url     (str "v1/"
                   table
                   ;; "?select=*&order=played_at.asc&organization=eq."
                   "?select=*&played_at=gt.2025-05-20&order=played_at.asc&organization=eq."
                   organization
                   "&played_at=gte."
                   date)
     :options {:method (name :get)
               :headers
               {:apiKey anon-key
                :Authorization (str "Bearer " anon-key)}}
     :on-failure (fn [err]
                   (let [dispatcher (get-dispatcher)]
                     (dispatcher
                      nil
                      [[:db/assoc :error err]
                       [:db/dissoc :is-loading?]])))
       :on-success (fn [ms]
                      (let [;; Get toggle state from passed state data
                            toggle-value (get-in data [:state :ui/toggle-value])
                            ;; Determine system type based on toggle state
                            ;; falsy (false/nil) -> "elo" (bounded)
                            ;; truthy (true) -> "elo-unbounded" (unbounded)
                            system-type (if toggle-value "elo-unbounded" "elo")
                            {:keys [ratings history players stats-by-player oponent-stats matches]}
                            (full-matches-process ms :system-type system-type)
                            dispatch (get-dispatcher)]
                        (js/console.log "Network domain: toggle-value =" toggle-value "system-type =" system-type)
                     (dispatch
                      nil
                      [[:db/assoc-in [:classification :ratings] ratings]
                       [:db/assoc-in [:system :history] history]
                       [:db/assoc-in [:stats :players] players]
                       [:db/assoc-in [:stats :by-player] stats-by-player]
                       [:db/assoc-in [:stats :oponents] oponent-stats]
                       [:db/assoc-in [:match :results] matches]
                       [:db/assoc :is-loading? false]])))}
    :query/user
    [:get (str "/api/todo/users/" (:user-id data))]))

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
