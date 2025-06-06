(ns qoback.closspad.utils.datetime)

(defn date->minus-one-year
  ([] (date->minus-one-year (js/Date.)))
  ([^js date]
   (let [d (doto date
             (.setFullYear (-> date .getFullYear dec))
             (.setHours 0 0 0))]
     (-> d .toISOString (.split "T") first))))



(comment
  (date->minus-one-year)
  )
