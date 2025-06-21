(ns qoback.closspad.rating.rating-system)

(defn determine-winner [result]
  (let [sets-won-by-a (count (filter (fn [[a b]] (> a b)) result))
        sets-won-by-b (count (filter (fn [[a b]] (< a b)) result))]
    (if (> sets-won-by-a sets-won-by-b)
      "A"
      "B")))
