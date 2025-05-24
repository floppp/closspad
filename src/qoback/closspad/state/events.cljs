(ns qoback.closspad.state.events
  (:require [qoback.closspad.state.db :refer [!state]]
            [qoback.closspad.state.effects :refer [perform-effect!]]))

(defn- handle-event
  [state replicant-data event]
  (let [[action-name & args] event]
    (case action-name
      :dom/prevent-default {:effects [[:dom/fx.prevent-default]]} ;;(.preventDefault js-event)
      :db/assoc {:new-state (apply assoc state args)}
      :db/dissoc {:new-state (apply dissoc state args)}
      :route/not-found {:effects [[:route/not-found]]}
      :route/home {:effects [[:route/home]]}
      :route/match {:effects [[:route/match args]]}
      :data/query {:effects [[:data/query {:state state :args args}]]}
      (.log js/console "Unknown event " action-name "with arguments" args )
      )))

(defn- handle-events
  [state replicant-data events]
  (reduce
   (fn [{state :new-state :as acc} event]
     (let [{:keys [new-state effects]} (handle-event state replicant-data event)]
       (cond-> acc
         :new-state (assoc :new-state new-state)
         :effects (update :effects into effects))))
   {:new-state state :effects []}
   events))

(defn event-handler [replicant-data actions]
  (let [{:keys [new-state effects]} (handle-events @!state replicant-data actions)]
    (when new-state
      (reset! !state new-state)
      (.log js/console "New state" @!state))
    (when effects
      (doseq [effect effects]
        (perform-effect! replicant-data effect)))))
