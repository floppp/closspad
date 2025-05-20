(ns qoback.closspad.state.db)

(defonce !state (atom {}))

(defonce !dispatcher (atom {}))

(defn get-dispatcher
  []
  (:dispatcher @!dispatcher))

