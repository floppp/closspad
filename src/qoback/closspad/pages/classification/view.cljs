(ns qoback.closspad.pages.classification.view
  (:require [qoback.closspad.helpers :as h]
            [qoback.closspad.pages.classification.order :refer [animate-reorder]]))

;; (def prev-order (atom nil))

#_(defn change-prev [x]
    (tap> @prev-order)
    (reset! prev-order x)
    x)

(defn player-color?
  [points]
  (cond
    (>= points 60) ["bg-green-100" "border-l-4" "border-green-500"]
    ;; (>= points 60) ["bg-blue-100" "border-l-4" "border-blue-500"]
    (< points 30) ["bg-red-100" "border-l-4" "border-red-500"]
    (< points 45) ["bg-orange-100" "border-l-4" "border-orange-500"]
    :else ["bg-gray-50" "border-l-4" "border-gray-300"]))

(defn- player
  [[name, points]]
  (let [cl (player-color? points)]
    [:a.flex.justify-between.items-center.p-4.rounded-lg.shadow-sm
     {:class (concat cl ["transition-all" "duration-300" "ease-in-out"])
      :href (str "#/stats/" name)}
     [:span.font-medium.text-gray-800 name]
     [:span.font-bold.text-lg (str points)]]))

(defn filter-day-ratings
  [c day-str ratings]
  (filter
   (fn [[d _]]
     (c (js/Date. (h/format-iso-date (js/Date. d)))
         (js/Date. day-str)))
   ratings))

(defn players-list
  [prev-day-ratings day-ratings]
  (let [diff (reduce
              (fn [acc [d [name points]]]
                ))])
  (map player players))

(defn view
  [state]
  (let [day (:date (:page/navigated state))
        day-str (h/format-iso-date day)
        ratings (:ratings (:classification state))
        day-ratings (filter-day-ratings <= day-str ratings)
        sorted-ratings (sort-by first day-ratings)
        day-ratings (last sorted-ratings)
        prev-day-ratings (filter-day-ratings < day-str sorted-ratings)
        prev-day-rating (last prev-day-ratings)
        players (->> (second day-ratings)
                     (sort-by second >))]
    (.log js/console prev-day-rating)
    [:div.bg-white.rounded-b-lg.shadow-md.p-8
     [:h2.text-3xl.font-bold.text-center.mb-6.text-gray-800 "Clasificación"]
     [:div.space-y-3
      (map player players)]]))
