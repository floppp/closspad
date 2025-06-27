(ns qoback.closspad.state.events
  (:require [clojure.walk :as walk]
            [qoback.closspad.state.db :refer [!state]]
            [qoback.closspad.state.drag-events :as drag]
            [qoback.closspad.state.effects :refer [perform-effect!]]
            [qoback.closspad.state.ui-events :as ui-events]
            [qoback.closspad.utils.coll-extras :as ext]))

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
    ;; (when goog.DEBUG
        ;; (.log js/console action-name args)
        ;; (.log js/console enrichted-event)
        ;; )
    (case action-name
      :add-match/played-at (let [[value path] args]
                             {:new-state (assoc-in state path value)})
      :add-match (let [[value & path] args]
                   {:new-state (ext/update-with-vector state (concat  [:add/match] path) value)})
      :add/match-set          {:new-state (update-in state [:add/match :n-sets] (fnil inc 1))}
      :remove/match-set       {:new-state
                               ;; tremenda guarrada, problema de la estructra elegida, hay que mejorar aunque sí funciona
                               (assoc-in
                                (assoc-in
                                 (update-in state [:add/match :n-sets] (fnil dec 1))
                                 [:add/match :result :a]
                                 (pop (get-in state [:add/match :result :a])))
                                [:add/match :result :b]
                                (pop (get-in state [:add/match :result :b])))}
      :db/assoc               {:new-state (apply assoc state args)}
      :db/assoc-in            (let [[path args] args]
                                {:new-state (assoc-in state path args)})
      :db/dissoc              {:new-state (apply dissoc state args)}
      :db/login               (let [[_ value element] enrichted-event]
                                {:new-state (assoc-in state [:db/login element] value)})
      :ui/header              {:new-state (update state :ui/header not)}
      :auth/check-login       {:effects [[:auth/fx.check-login (:auth state)]]}
      :auth/check-not-logged  {:effects [[:auth/fx.check-not-logged (:auth state)]]}
      :data/query             {:new-state (assoc state :is-loading? true :error nil)
                               :effects [[:data/fx.query {:state state :args args}]]}
      :fetch/login            {:new-state (assoc state :is-loading? true :error nil)
                               :effects [[:fetch/fx.login args]]}
      :drag                   {:new-state (drag/process state args)}
      ;; No refactorizar porque es mucho lío para cambiar `enrich-action-from-event`.
      :event/prevent-default  {:effects [[:dom/fx.prevent-default]]}
      ;; >>>>> Refactorizados ya
      :ui/dialog              {:new-state (ui-events/process-dialogs state args)}
      :dom/effect             {:effects   [[:dom/fx.effect (first args)]]}
      ;; Routes
      :route/push             {:effects [[:route/fx.push args]]}
      :route                  {:effects [[:route/fx args state]]}
      :post/network           {:new-state (assoc state :is-loading? true :error nil)
                               :effects [[:post/fx.network args]]}
      (when goog.DEBUG
        (.log js/console "Unknown Event " action-name " with arguments" args)))))

(defn- handle-events
  [state replicant-data events]
  (reduce
   (fn [{state :new-state :as acc} event]
     (let [{:keys [new-state effects]} (handle-event state replicant-data event)]
       (-> acc
           (assoc :new-state (or new-state (:new-state acc)))
           (update :effects into (or effects [])))))
   {:new-state state :effects []}
   events))

(defn event-handler
  [replicant-data events]
  (let [{:keys [new-state effects]} (handle-events @!state replicant-data events)]
    (when new-state
      (reset! !state new-state)
      (when goog.DEBUG
        #_(prn  (->  @!state :classification :ratings))
        (.log js/console  @!state)))
    (when effects
      (doseq [effect effects]
        (perform-effect! replicant-data effect)))))
