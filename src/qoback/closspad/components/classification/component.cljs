(ns qoback.closspad.components.classification.component
  (:require [qoback.closspad.helpers :as h]))

(defn player-color?
  [points]
  (cond
    (>= points 60) ["bg-green-100" "border-l-4" "border-green-500"]
    ;; (>= points 60) ["bg-blue-100" "border-l-4" "border-blue-500"]
    (< points 30) ["bg-red-100" "border-l-4" "border-red-500"]
    (< points 45) ["bg-orange-100" "border-l-4" "border-orange-500"]
    :else ["bg-gray-50" "border-l-4" "border-gray-300"]))

(defn- player
  [[name points prev-points]]
  (let [cl (player-color? points)
        diff (- points prev-points)
        preffix (if (> diff 0) "+" (if (< diff 0) "-" ""))]
    [:a.flex.justify-between.items-center.p-4.rounded-lg.shadow-sm
     {:class (concat cl ["transition-all" "duration-300" "ease-in-out"])
      :href (str "#/stats/" name)}
     [:span.font-medium.text-gray-800 name]
     [:div.text-right.w-24.flex.items-center
      [:span.text-gray-500.text-right
       {:class ["w-1/2"]}
       (str "("  preffix  (.abs js/Math (- prev-points points)) ") ")]
      [:span.font-bold.text-lg.text-right
       {:class ["w-1/2"]}
       (str points)]]]))

(defn filter-day-ratings
  [c day-str ratings]
  (filter
   (fn [[d _]]
     (c (js/Date. (h/format-iso-date (js/Date. d)))
         (js/Date. day-str)))
   ratings))

(defn players-list
  [prev-day-ratings player-ratings]
  (let [enrichted-ratings (map (fn [p]
                                 (let [prev-points
                                       (first
                                        (filter
                                         #(= (first p) (first %))
                                         prev-day-ratings))]
                                   (conj p (second prev-points))))
                               player-ratings)]
    [:div.space-y-3
     (map player enrichted-ratings)]))

(defn component
  [state]
  (let [day (:date (:page/navigated state))
        day-str (h/format-iso-date day)
        ratings (:ratings (:classification state))
        day-ratings (filter-day-ratings <= day-str ratings)
        sorted-ratings (sort-by first day-ratings)
        day-ratings (last sorted-ratings)
        prev-day-player-ratings (second (last (filter-day-ratings < day-str sorted-ratings)))
        players-ratings (->> (second day-ratings)
                             (sort-by second >))]
    [:div.bg-white.rounded-b-lg.shadow-md.p-8
     [:h2.text-3xl.font-bold.text-center.mb-6.text-gray-800 "Clasificación"]
     (players-list prev-day-player-ratings players-ratings)]))
