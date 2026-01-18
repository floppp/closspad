(ns qoback.closspad.components.classification.component
  (:require [qoback.closspad.helpers :as h]
            [qoback.closspad.components.icons.info :as info]
            [qoback.closspad.state.db :refer [get-dispatcher]]
            [qoback.closspad.ui.button-elements :as bui]
            [qoback.closspad.ui.text-elements :as tui]))

(defn determine-system-type [state]
  (cond
    (:ui/toggle-value state) :atp
    (:ui/elo-unbounded? state) :elo-unbounded
    :else :elo-bounded))

(defn calculate-percentiles [points]
  (let [sorted (sort points)
        n (count sorted)]
    {:p25 (when (pos? n) (nth sorted (max 0 (int (* n 0.25)))))
     :p50 (when (pos? n) (nth sorted (max 0 (int (* n 0.50)))))
     :p75 (when (pos? n) (nth sorted (max 0 (int (* n 0.75)))))}))

;; Fixed thresholds for bounded Elo
(defn player-color-elo-bounded [points _]
  (cond
    (< points 30) ["bg-red-100" "border-l-4" "border-red-500"]
    (< points 45) ["bg-orange-100" "border-l-4" "border-orange-500"]
    (>= points 60) ["bg-green-100" "border-l-4" "border-green-500"]
    :else ["bg-gray-50" "border-l-4" "border-gray-300"]))

;; Percentile-based for unbounded Elo and ATP
(defn player-color-percentile [points all-points]
  (let [{:keys [p25 p50 p75]} (calculate-percentiles all-points)]
    (cond
      (and p25 (< points p25)) ["bg-red-100" "border-l-4" "border-red-500"]
      (and p50 (< points p50)) ["bg-orange-100" "border-l-4" "border-orange-500"]
      (and p75 (>= points p75)) ["bg-green-100" "border-l-4" "border-green-500"]
      ;; Fallback for small lists
      (< (count all-points) 4) ["bg-gray-50" "border-l-4" "border-gray-300"]
      :else ["bg-gray-50" "border-l-4" "border-gray-300"])))

;; Color function registry
(def color-functions
  {:elo-bounded player-color-elo-bounded
   :elo-unbounded player-color-percentile
   :atp player-color-percentile})

(defn player-color? [points system-type all-points]
  ((get color-functions system-type) points all-points))

(defn- player
  [[name points prev-points] system-type all-points]
  (let [cl (player-color? points system-type all-points)
        diff (- points prev-points)
        preffix (if (> diff 0) "+" (if (< diff 0) "-" ""))]
    [:a.flex.justify-between.items-center.p-4.rounded-lg.shadow-sm
     {:class (concat cl ["transition-all" "duration-300" "ease-in-out"])
      :href (str "#/stats/" name)}
     [tui/text-dark-gray name]
     [:div.grid.grid-cols-3
      {:style {:place-items "center"}}
      [bui/icon-button
       {:actions [[:event/prevent-default]
                  [:ui/dialog :player-info name]]}
       info/icon]
      [tui/text-gray (str "("  preffix  (.toFixed (js/Math.abs (- prev-points points))
                                                  1) ") ")]
      [tui/text-lg-bold (.toFixed points 1)]]]))

(defn filter-day-ratings
  [c day-str ratings]
  (filter
   (fn [[d _]]
     (c (js/Date. (h/format-iso-date (js/Date. d)))
         (js/Date. day-str)))
   ratings))

(defn players-list
  [prev-day-ratings player-ratings system-type]
  (let [all-points (map second player-ratings)
        enrichted-ratings (map (fn [p]
                                 (let [prev-points
                                       (first
                                        (filter
                                         #(= (first p) (first %))
                                         prev-day-ratings))]
                                   (conj p (second prev-points))))
                               player-ratings)]
    [:div.space-y-3
     (map #(player % system-type all-points) enrichted-ratings)]))

(defn- header
  [state]
  (let [is-mobile? (-> state :app :screen/is-mobile?)
    classes (if is-mobile? ["flex" "flex-col" "gap-2"] ["flex"])]
    [:div.flex.justify-between.items-center.mb-4
      {:class classes}
    [:h2.text-3xl.font-bold.text-gray-800 "Clasificación"]
    [:div.flex.gap-4.items-center
      [bui/toggle-button
        {:active (:ui/toggle-value state)
        :on-text "Anual"
        :off-text "Race"}]
      (when (not (:ui/toggle-value state))
        [:label.flex.items-center.gap-2.cursor-pointer
        [:input {:type "checkbox"
                  :checked (:ui/elo-unbounded? state)
                  :on {:click [[:ui/elo-unbounded]]}}]
        [:span.text-sm.text-gray-600 "Llorón"]])
      [bui/refresh-button state]]]))

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
                             (sort-by second >))
        system-type (determine-system-type state)]
    [:div.bg-white.rounded-b-lg.shadow-md.px-8.pb-8
    (header state)
     (players-list prev-day-player-ratings players-ratings system-type)]))
