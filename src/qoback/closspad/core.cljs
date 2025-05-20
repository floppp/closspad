(ns qoback.closspad.core
  (:require    [replicant.dom :as r-dom]
   [reitit.frontend.easy :as rfe]
   [qoback.closspad.router :as router]))

(defonce ^:private !state (atom {}))
(defonce ^:private !dispatcher (atom {}))

(defn- get-dispatcher [] (:dispatcher @!dispatcher))

(defn match-view
  []
  [:div "Match"])

(defn not-found-view
  []
  [:div "Not Found"])


(defn home-view 
  []
  [:div [:div "Home"
         [:ul 
          [:li "foo"]
          [:li "bar"]]]])

(defn- header-view [state]
  [:div.flex ;.h-screen
   [:div.flex-grow.p-4
    [:h1.text-3xl.font-bold.text-center.mt-5 "WEB STORE with REPLICANT"]
    [:h2.text-xl.font-bold.text-center.mt-10 "Choose product group:"]]])

(defn view [state]
  ;(f-util/clog "view, state: " state)
  [:div.flex.h-screen
   [:div.flex-grow.p-4
    [:div.flex.flex-col.items-center.min-h-screen.mt-10
     (header-view state)
     (.log js/console "page: " (:page (:page/navigated state)))
     (case (:page (:page/navigated state))
       :not-found [:div (not-found-view)]
       :home [:div (home-view)]
       :match [:div (match-view)])]]])

(defn- render! [state]
  (r-dom/render
   (js/document.getElementById "app")
   (view state)))

(defn ^{:dev/after-load true :export true} start! []
  (render! @!state))


(defn navigated-not-found-page []
  (let [dispatcher (get-dispatcher)]
    (dispatcher nil [[:db/assoc :page/navigated {:page :not-found}]])))

(defn navigated-home-page []
  (let [dispatcher (get-dispatcher)]
    (dispatcher nil [[:db/assoc :page/navigated {:page :home}]])))

(defn navigated-match-page [_]
  (let [dispatcher (get-dispatcher)]
    (dispatcher nil [[:db/assoc :page/navigated {:page :match}]])))

(defn- handle-action [state replicant-data action]
  (let [[action-name & args] action]
    (.log js/console "handle-action" action-name)
    (case action-name
      :dom/prevent-default {:effects [[:dom/fx.prevent-default]]} ;;(.preventDefault js-event)
      :db/assoc {:new-state (apply swap! !state assoc args)}
      :db/assoc-in {:new-state (apply swap! !state assoc-in args)}
      :db/dissoc {:new-state (apply swap! !state dissoc args)}
      :route/not-found {:effects [[:route/not-found]]}
      :route/home {:effects [[:route/home]]}
      :route/match {:effects [[:route/match {:id 1}]]}
      ;; :route/classification (navigated-product-page (assoc (second enriched-action) :state @!state))
      ;; :route/login (navigated-new-product-page (assoc (second enriched-action) :state @!state))
      )))

(defn handle-actions [state replicant-data actions]
  (reduce (fn [{state :new-state :as acc} action]
            (let [{:keys [new-state effects]} (handle-action state replicant-data action)]
              (cond-> acc
                :new-state (assoc :new-state new-state)
                :effects (update :effects into effects))))
          {:new-state state
           :effects []}
          actions))

(defn perform-effect! 
  [{:keys [^js replicant/js-event]} [effect & args]]
  (case effect
    :route/not-found (navigated-not-found-page)
    :route/home (navigated-home-page)
    :route/match (navigated-match-page nil)
    :else (js/console.error "Unknown effect" effect)))

(defn- event-handler [replicant-data actions]
  (let [{:keys [new-state effects]} (handle-actions @!state replicant-data actions)]
    (when new-state
      (reset! !state new-state))
    (when effects
      (doseq [effect effects]
        ;;(when js/goog.DEBUG (js/console.debug "Triggered effect" effect))
        (perform-effect! replicant-data effect)
        
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
