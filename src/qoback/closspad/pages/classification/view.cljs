(ns qoback.closspad.pages.classification.view
  (:require [qoback.closspad.helpers :as h]
            [qoback.closspad.pages.classification.order :refer [animate-reorder]]))

(def prev-order (atom nil))

(when goog.DEBUG
  (add-tap
   (fn [x]
     (doseq [idx (range (count x))]
       (let [[name points] (nth x idx)]
         (.log js/console (str idx " points: " points " " name)))))
   #_(partial println "tap>")))

(defn change-prev [x]
  (when goog.DEBUG
    (tap> @prev-order))
  (reset! prev-order x)
  x)

(defn player-color?
  [points]
  (cond
    (>= points 60) ["bg-green-100" "border-l-4" "border-green-500"]
    (< points 40) ["bg-red-100" "border-l-4" "border-red-500"]
    (< points 50) ["bg-orange-100" "border-l-4" "border-orange-500"]
    :else ["bg-gray-50" "border-l-4" "border-gray-300"]))

(defn- player
  [[name, points]]
  (let [cl (player-color? points)]
    [:div.flex.justify-between.items-center.p-4.rounded-lg.shadow-sm
     {:key name
      :class (concat cl ["transition-all" "duration-300" "ease-in-out"])}
     [:span.font-medium.text-gray-800 name]
     [:span.font-bold.text-lg (str points)]]))

(defn view
  [state]
  (let [day (:date (:page/navigated state))
        day-str (h/datetime->date->str day)
        ratings (:ratings (:classification state))
        day-ratings (first
                     (filter
                      (fn [[d _]]
                        (= day-str (h/datetime->date->str (js/Date. d))))
                      ratings))
        players (->> (second day-ratings)
                     (sort-by second >)
                     (change-prev))]
    [:div.bg-white.rounded-b-lg.shadow-md.p-8
     [:h2.text-3xl.font-bold.text-center.mb-6.text-gray-800 "Clasificación"]
     [:div.space-y-3
      (map player players)]]))
