(ns qoback.closspad.state.match-events
  (:require [qoback.closspad.rating.match :refer [full-matches-process]]))

(defn process
  [state args]
  (let [[action-name & args] args]
    (js/console.log action-name)
    (js/console.log args)
    (case action-name
      :new (let [current-ms (-> state :match :results)
                 all-ms (sort-by :played_at (concat current-ms args))
                 {:keys [ratings history players stats-by-player oponent-stats matches]}
                 (full-matches-process all-ms)]
             (-> state
                 (assoc :is-loading? false)
                 (assoc-in [:classification :ratings] ratings)
                 (assoc-in [:system :history] history)
                 (assoc-in [:stats :players] players)
                 (assoc-in [:stats :by-player] stats-by-player)
                 (assoc-in [:stats :oponents] oponent-stats)
                 (assoc-in [:match :results] matches)))

      (when goog.DEBUG
        (js/console.log "Unknown MATCH event " action-name "with arguments" args)))))
