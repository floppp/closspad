(ns qoback.closspad.core
  (:require [clojure.walk :as walk]
            [replicant.dom :as r-dom]))

(defonce ^:private !state (atom {}))
(defonce ^:private !dispatcher (atom {}))

(defn- get-dispatcher [] (:dispatcher @!dispatcher))

(defn- render! [state]
  (r-dom/render
   (js/document.getElementById "app")
   [:div "foo"]))

(defn ^{:dev/after-load true :export true} start! []
  (render! !state))

(defn- enrich-action-from-event [{:replicant/keys [js-event node]} actions]
  (walk/postwalk
   (fn [x]
     (cond
       (keyword? x)
       (case x
         :event/target.value (-> js-event .-target .-value)
         :dom/node node
         x)
       :else x))
   actions))

(defn- enrich-action-from-state [state action]
  (walk/postwalk
   (fn [x]
     (cond
       (and (vector? x)
            (= :db/get (first x))) (get state (second x))
       :else x))
   action))

(defn navigated-home-page []
  (let [dispatcher (get-dispatcher)]
    (dispatcher nil [[:db/assoc :page/navigated {:page :home}]])))

(defn navigated-match-page [_]
  (let [dispatcher (get-dispatcher)]
    (dispatcher nil [[:db/assoc :page/navigated {:page :home}]])))

(defn- event-handler [{:replicant/keys [^js js-event] :as replicant-data} actions]
  (doseq [action actions]
    (let [enriched-action (->> action
                               (enrich-action-from-event replicant-data)
                               (enrich-action-from-state @!state))
          [action-name & args] enriched-action]
      (case action-name
        :dom/prevent-default (.preventDefault js-event)
        :db/assoc (apply swap! !state assoc args)
        :db/assoc-in (apply swap! !state assoc-in args)
        :db/dissoc (apply swap! !state dissoc args)
        :route/home (navigated-home-page)
        :route/match (navigated-match-page (assoc (second enriched-action) :state @!state))
        ;; :route/classification (navigated-product-page (assoc (second enriched-action) :state @!state))
        ;; :route/login (navigated-new-product-page (assoc (second enriched-action) :state @!state))
        )))
  (render! @!state))

#_(defn watch! [render!]
    (add-watch !state
               ::render (fn [_ _ old-state new-state]
                          (when (not= old-state new-state)
                            (render! new-state)))))

(defn ^:export init! []
  (r-dom/set-dispatch! event-handler)
  (swap! !dispatcher assoc :dispatcher event-handler)
  ;; (watch! render!)
  (start!))
