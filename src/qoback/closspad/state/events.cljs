(ns qoback.closspad.state.events
  (:require [clojure.walk :as walk]
            [qoback.closspad.state.db :refer [!state]]
            [qoback.closspad.state.effects :refer [perform-effect!]]))

(defn- enrich-action-from-event [{:replicant/keys [js-event node]} actions]
  (walk/postwalk
   (fn [x]
     (cond (keyword? x)
           (case x
             :event/target.value (-> js-event .-target .-value)
             :dom/node node
             x)
           :else x))
   actions))

(defn- enrich-action-from-state [state action]
  (walk/postwalk
   (fn [x]
     (cond (and (vector? x) (= :db/get (first x)))
           (get state (second x))
           :else x))
   action))

(defn- handle-event
  [state replicant-data event]
  (let [enrichted-event (->> event
                             (enrich-action-from-event replicant-data)
                             (enrich-action-from-state state))
        [action-name & args] enrichted-event]
    ;; (.log js/console action-name args)
    (case action-name
      :event/prevent-default {:effects [[:dom/fx.prevent-default]]}
      :db/assoc (do
                  (.log js/console (apply assoc state args))
                  {:new-state (apply assoc state args)})
      :db/assoc-in (let [[path args] args]
                     {:new-state (assoc-in state path args)})
      :db/dissoc {:new-state (apply dissoc state args)}
      :db/login (let [[_ value element] enrichted-event]
                  {:new-state (assoc-in state [:db/login element] value)})
      :ui/header {:new-state (update state :ui/header not)}
      :route/not-found   {:effects [[:route/not-found state]]}
      :route/home        {:effects [[:route/fx.home]]}
      :route/explanation {:effects [[:route/fx.explanation]]}
      :route/login       {:effects [[:route/fx.login (:auth state)]]}
      :route/match       {:effects [[:route/fx.match args]]}
      :route/stats       {:effects [[:route/fx.stats args]]}
      :route/push        {:effects [[:route/fx.push args]]}
      :auth/check-login  {:effects [[:auth/fx.check-login (:auth state)]]}
      :data/query        {:effects [[:data/fx.query {:state state :args args}]]}
      :fetch/login       {:effects [[:fetch/fx.login args]]}
      (when goog.DEBUG
        (.log js/console "Unknown event " action-name "with arguments" args)))))

(defn- handle-events
  [state replicant-data events]
  (reduce
   (fn [{state :new-state :as acc} event]
     (let [{:keys [new-state effects]} (handle-event state replicant-data event)]
       (-> acc
           (assoc :new-state (or new-state (:new-state acc)))
           (update :effects into (or effects [])))
       #_(cond-> acc
           :new-state (assoc :new-state new-state)
           :effects (update :effects into effects))))
   {:new-state state :effects []}
   events))

(defn event-handler
  [replicant-data events]
  (let [{:keys [new-state effects]} (handle-events @!state replicant-data events)]
    (when new-state
      (reset! !state new-state)
      (when goog.DEBUG
        #_(.log js/console " >>>>>>>>>>>>")
        (.log js/console  @!state)
        #_(.log js/console " <<<<<<<<<<<<")
        #_(.log js/console "")))
    (when effects
      (doseq [effect effects]
        (perform-effect! replicant-data effect)))))
