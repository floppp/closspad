(ns qoback.closspad.state.match-events
  (:require [qoback.closspad.rating.match :refer [full-matches-process]]))

(defn process
  [state args]
  (let [[action-name & args] args]
    (case action-name
      :new (let [current-ms (-> state :match :results)
                 all-ms (sort-by :played_at (concat current-ms args))
                 ;; Determine system type based on toggle state
                 ;; falsy (false/nil) -> "elo" (bounded)
                 ;; truthy (true) -> "elo-unbounded" (unbounded)
                  system-type (if (:ui/toggle-value state)
                                "atp"  ; Toggle true = ATP system
                                (if (:ui/elo-unbounded? state)
                                  "elo-unbounded"  ; Toggle false + checkbox checked = unbounded Elo
                                  "elo"))  ; Toggle false + checkbox unchecked = bounded Elo
                 {:keys [ratings history players stats-by-player oponent-stats matches]}
                 (full-matches-process all-ms :system-type system-type)]
             (-> state
                 (assoc :is-loading? false)
                 (assoc-in [:classification :ratings] ratings)
                 (assoc-in [:system :history] history)
                 (assoc-in [:stats :players] players)
                 (assoc-in [:stats :by-player] stats-by-player)
                 (assoc-in [:stats :oponents] oponent-stats)
                 (assoc-in [:match :results] matches)))
      
       :rating-system/recalculate-all (let [current-ms (-> state :match :results)
                                            ;; Recalculate all matches with current toggle state
                                            system-type (if (:ui/toggle-value state)
                                                          "atp"  ; Toggle true = ATP system
                                                          (if (:ui/elo-unbounded? state)
                                                            "elo-unbounded"  ; Toggle false + checkbox checked = unbounded Elo
                                                            "elo"))  ; Toggle false + checkbox unchecked = bounded Elo
                                            {:keys [ratings history players stats-by-player oponent-stats matches]}
                                            (full-matches-process current-ms :system-type system-type)]
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
