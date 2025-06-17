(ns qoback.closspad.state.ui-events
  (:require [qoback.closspad.utils.datetime :as h]
            [qoback.closspad.components.match-analysis :as m]))

(defn process-dialogs
  [state args]
  (let [[action-name & args] args]
    (case action-name
      :match-info (let [match (first args)
                        hist (->> state :system :history)
                        date (js/Date. (:played_at match))
                        system (->> hist
                                    (filter #(= (:date %) date))
                                    first)]
                    (assoc state
                           :dialog {:extra-node
                                    (m/component (-> system :auditLog) match)}
                           :ui/dialog true))
      :player-info (when-let [date (-> state :page/navigated :date h/datetime->date->str)]
                     (let [selected-player (-> args first keyword)
                           hist (->> state :system :history)
                           players-info (->> hist
                                             (filter #(= (h/datetime->date->str (:date %)) date))
                                             first
                                             :players)]
                       (assoc state
                              :dialog {:title (name selected-player)
                                       :info (-> args first keyword players-info)}
                              :ui/dialog true)))
      (when goog.DEBUG
        (.log js/console "Unknown UI event " action-name "with arguments" args)))))
