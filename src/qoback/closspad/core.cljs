(ns qoback.closspad.core
  (:require [clojure.walk :as walk]
            [replicant.dom :as r-dom]
            [qoback.closspad.router :as router]))

(defonce ^:private !state (atom {}))
(defonce ^:private !dispatcher (atom {}))

(defn- get-dispatcher [] (:dispatcher @!dispatcher))

(defn- render! [state]
  (r-dom/render
   (js/document.getElementById "app")
   [:div "foo"]))

(defn ^{:dev/after-load true :export true} start! []
  (render! !state))





(defn navigated-home-page []
  (let [dispatcher (get-dispatcher)]
    (dispatcher nil [[:db/assoc :page/navigated {:page :home}]])))

(defn navigated-match-page [_]
  (let [dispatcher (get-dispatcher)]
    (dispatcher nil [[:db/assoc :page/navigated {:page :home}]])))

(defn- handle-action [state replicant-data action]
  (let [[action-name & args] action]
    (case action-name
      :dom/prevent-default {:effects [[:dom/fx.prevent-default]]} ;;(.preventDefault js-event)
      :db/assoc {:new-state (apply swap! !state assoc args)}
      :db/assoc-in {:new-state (apply swap! !state assoc-in args)}
      :db/dissoc {:new-state (apply swap! !state dissoc args)}
      :route/home {:effects [[:route/home]]}
      :route/match {:effects [[:route/match {:id 1}]]}
      ;; :route/classification (navigated-product-page (assoc (second enriched-action) :state @!state))
      ;; :route/login (navigated-new-product-page (assoc (second enriched-action) :state @!state))
      )))

(defn handle-actions [state replicant-data actions]
  (reduce (fn [{state :new-state :as acc} action]
            (let [{:keys [new-state effects]} (handle-action state replicant-data action)]
              (cond-> acc
                new-state (assoc :new-state new-state)
                effects (update :effects into effects))))
          {:new-state state
           :effects []}
          actions))

(defn- event-handler [replicant-data actions]
  (let [{:keys [new-state effects]} (handle-actions @!state replicant-data actions)]
    (when new-state
      (reset! !state new-state))
    (when effects
      (doseq [effect effects]
        (when js/goog.DEBUG (js/console.debug "Triggered effect" effect))
        ;;(effects/perform-effect! replicant-data effect)
        
        ))))

(defn watch! [render!]
    (add-watch !state
               ::render (fn [_ _ old-state new-state]
                          (when (not= old-state new-state)
                            (render! new-state)))))

(defn ^:export init! []
  (r-dom/set-dispatch! event-handler)
  (swap! !dispatcher assoc :dispatcher event-handler)
  (router/start! router/routes event-handler)
  (watch! render!)
  (start!))
