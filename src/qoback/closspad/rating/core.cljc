(ns qoback.closspad.rating.core)

(def DEFAULTS
  {:one-set-importance 0.8
   :regular-importance 1
   :time-distance-factor 365
   :max-decay 0.8})

(def default-options
  {:default-rating 50
   :max-rating 100
   :min-rating 0
   :base-k 20
   :scale-factor 100
   :players {}
   :date nil})
