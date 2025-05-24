(ns qoback.closspad.state.effects
  (:require [qoback.closspad.state.db :refer [get-dispatcher]]
            [qoback.closspad.network.query :as network]))

(defn navigated-not-found-page []
  (let [dispatcher (get-dispatcher)]
    (dispatcher nil [[:db/assoc :page/navigated {:page :not-found}]])))

(defn navigated-home-page []
  (let [dispatcher (get-dispatcher)]
    (dispatcher nil [[:db/assoc :page/navigated {:page :home}]])))

(defn navigated-match-page
  [{:keys [date]}]
  (let [dispatcher (get-dispatcher)]
    (dispatcher
     nil
     [[:db/assoc :page/navigated {:page :match :date date}]])))


(defn perform-effect!
  [{:keys [^js replicant/js-event]} [effect & args]]
  (case effect
    :route/not-found (navigated-not-found-page)
    :route/home (navigated-home-page)
    :route/match (navigated-match-page (-> args first first))
    :data/query (network/query-async {:query/kind :query/matches :query/data args })
    ;; :data/query2 (network/query-async {:query/kind :query/matches :query/data args })
    (js/console.log "Unknown effect" effect)))
