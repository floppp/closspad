(ns qoback.closspad.state.dom-effects)

(defn perform!
  [[effect-name & args]]
  (case effect-name
    :body/scroll-hidden (set! (.. js/document.body -style -overflow) "hidden")
    :body/scroll-show   (set! (.. js/document.body -style -overflow) "")
    (when goog.DEBUG
      (.log js/console "Unknown DOM effect " effect-name " with arguments " args))))
