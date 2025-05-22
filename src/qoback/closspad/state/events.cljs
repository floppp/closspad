(ns qoback.closspad.state.events
  (:require [qoback.closspad.state.db :refer [!state]]
            [qoback.closspad.state.effects :refer [perform-effect!]]))

(defn- handle-event
  [state replicant-data event]
  (let [[action-name & args] event]
    (case action-name
      :dom/prevent-default {:effects [[:dom/fx.prevent-default]]} ;;(.preventDefault js-event)
      :db/assoc {:new-state (apply assoc state args)}
      ;; :db/assoc-in {:new-state (apply swap! !state assoc-in args)}
      :db/dissoc {:new-state (apply dissoc state args)}
      :route/not-found {:effects [[:route/not-found]]}
      :route/home {:effects [[:route/home]]}
      :route/match {:effects [[:route/match args]]}
      ;; :route/classification (navigated-product-page (assoc (second enriched-action) :state @!state))
      ;; :route/login (navigated-new-product-page (assoc (second enriched-action) :state @!state))
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
      (reset! !state new-state))
    (when effects
      (doseq [effect effects]
        (perform-effect! replicant-data effect)))))
