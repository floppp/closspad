(ns qoback.closspad.state.drag-events)

;; selectign? -> si estamos seleccionando
(defn- process-drop
  [{:keys [forecast]} player selecting?]
  (let [{:players/keys [selected non-selected]} forecast
        selected (if (seq selected) selected [])]
    (.log js/console selected)
    (if selecting?
      {:selected     (if (contains? (set selected) player)
                       selected
                       (conj selected player))
       :non-selected (filter #(not= % player) non-selected)}
      {:selected     (filter #(not= % player) selected)
       :non-selected (if (contains? (set non-selected) player)
                       non-selected
                       (conj non-selected player))})))

(defn process
  [state args]
  (let [[action-name & args] args]
    (case action-name
      ;; tengo que guardar elemento y columna
      :start (let [[element-val] args]
               (-> state
                   #_(assoc-in [:forecast :tmp/column] column-val)
                   (assoc-in [:forecast :tmp/element] element-val)))
      :end state
      :drop (let [[dst-col] args
                  player (-> state :forecast :tmp/element)
                  {:keys [selected non-selected]}
                  (process-drop state player (= dst-col :selected))]
              (-> state
                  (assoc-in [:forecast :players/selected] selected)
                  (assoc-in [:forecast :players/non-selected] non-selected)))
      (when goog.DEBUG
        (.log js/console "Unknown UI event " action-name "with arguments" args)))))
