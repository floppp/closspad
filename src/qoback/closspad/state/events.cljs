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
    (case action-name
      :event/prevent-default {:effects [[:dom/fx.prevent-default]]} ;;(.preventDefault js-event)
      :db/assoc {:new-state (apply assoc state args)}
      :db/assoc-in (let [[path args] args]
                     {:new-state (assoc-in state path args)})
      :db/dissoc {:new-state (apply dissoc state args)}
      :db/login (let [[_ value element] enrichted-event]
                  {:new-state (assoc-in state [:db/login element] value)})
      :route/not-found {:effects [[:route/not-found state]]}
      :route/home {:effects [[:route/fx.home]]}
      :route/login {:effects [[:route/fx.login]]}
      :route/match {:effects [[:route/fx.match args]]}
      :data/query {:effects [[:data/fx.query {:state state :args args}]]}
      :fetch/login {:effects [[:fetch/fx.login args]]}
      (when goog.DEBUG
        (.log js/console "Unknown event " action-name "with arguments" args)))))

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

(defn event-handler
  [replicant-data events]
  (let [{:keys [new-state effects]} (handle-events @!state replicant-data events)]
    (when new-state
      (reset! !state new-state)
      (when goog.DEBUG
        (.log js/console " >>>>>>>>>>>>")
        (.log js/console  @!state)
        (.log js/console " <<<<<<<<<<<<")
        (.log js/console "")))
    (when effects
      (doseq [effect effects]
        (perform-effect! replicant-data effect)))))
