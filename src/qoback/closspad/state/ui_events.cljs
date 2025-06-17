(ns qoback.closspad.state.ui-events
  (:require [qoback.closspad.utils.datetime :as h]))

(defn process-dialogs
  [state args]
  (let [[action-name & args] args]
    (case action-name
      :player-info (when-let [date (-> state :page/navigated :date h/datetime->date->str)]
                     (let [hist (->> state :system :history)
                           players-info (->> hist
                                             (filter #(= (h/datetime->date->str (:date %)) date))
                                             first
                                             :players)
                           prev-system (->> hist
                                            (filter #(< (:date %) (js/Date. date)))
                                            first)]
                       (.log js/console prev-system)
                       {:new-state (assoc state
                                          :dialog {:title (name (-> args first keyword))
                                                   :info (-> args first keyword players-info)
                                                   :extra-node [:div [:p "foo"]]}
                                          :ui/dialog true)}))
      (when goog.DEBUG
        (.log js/console "Unknown UI event " action-name "with arguments" args)))))
