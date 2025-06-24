(ns qoback.closspad.state.post-effects
  (:require [qoback.closspad.network.domain :refer [table]]
            [qoback.closspad.network.supabase :as supabase]))

(defn perform!
  [[action & args]]
  (case action
    :match (supabase/post-match table (first args))
    (when goog.DEBUG
      (.log js/console "Unknown Post Effect " action " with arguments" args))))
