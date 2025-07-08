(ns qoback.closspad.state.dom-effects
  (:require [qoback.closspad.state.db :refer [get-dispatcher]]))

(defn perform!
  [[effect-name & args]]
  (let [dispatcher (get-dispatcher)]
    (case effect-name
      :body/scroll-hidden (set! (.. js/document.body -style -overflow) "hidden")
      :body/scroll-show   (set! (.. js/document.body -style -overflow) "")
      :screen/update-mobile-state
      (let [is-mobile (.. js/window (matchMedia "(max-width: 768px)") -matches)]
        (dispatcher nil [[:db/assoc-in [:app :screen/is-mobile?] is-mobile]]))
      (when goog.DEBUG
        (.log js/console "Unknown DOM effect " effect-name " with arguments " args)))))
