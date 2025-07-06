(ns qoback.closspad.core
  (:require [replicant.dom :as r-dom]
            [dataspex.core :as dataspex]
            [qoback.closspad.router :as router]
            [qoback.closspad.pages.views :as v]
            [qoback.closspad.state.db :refer [!state !dispatcher]]
            [qoback.closspad.state.events :refer [event-handler]]))

#_(when goog.DEBUG
  (add-tap #(.log js/console %)))

(defn setup-device-listener
  [dispatcher]
  (let [query (js/window.matchMedia "(max-width: 768px)")
        listener (fn [_]
                   (dispatcher nil [[:dom/effect :screen/update-mobile-state]]))]
    (.addListener query listener)
    (dispatcher nil [[:dom/effect :screen/update-mobile-state]])))

(defn- render! [state]
  (r-dom/render
   (js/document.getElementById "app")
   (v/view state)))

(defn ^{:dev/after-load true :export true} start!
  []
  (render! @!state))

(defn watch! [render!]
  (add-watch !state
             ::render (fn [_ _ old-state new-state]
                        (when (not= old-state new-state)
                          (render! new-state)))))

(defn ^:export init! []
  ;; (inspector/inspect "App state" !state)
  (dataspex/inspect "App state" !state)
  (r-dom/set-dispatch! event-handler)
  (swap! !dispatcher assoc :dispatcher event-handler)
  (router/start! router/routes event-handler)
  (watch! render!)
  (event-handler nil [[:data/query]])
  (setup-device-listener event-handler)
  (start!))
