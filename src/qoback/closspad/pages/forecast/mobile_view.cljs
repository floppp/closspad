(ns qoback.closspad.pages.forecast.mobile-view
  (:require [clojure.string]
            [qoback.closspad.ui.layout-elements :as lui]
            [qoback.closspad.ui.elements :as ui]
            [qoback.closspad.ui.button-elements :as bui]
            [qoback.closspad.ui.card-elements :as cui]
            [qoback.closspad.pages.forecast.services :as fs]
            [qoback.closspad.pages.forecast.elements :as e]))

(defn- player-card
  [selectable? p is-selected?]
  [cui/card
   {:element p
    :class (if is-selected? "bg-green-200" "bg-blue-200")}
   [cui/card-details
    [:p.flex.justify-between
     [:span p]
     [bui/icon-button
      {:actions (if is-selected?
                  [[:drag :start p]
                   [:drag :drop :not-selected]]
                  (when selectable?
                    [[:drag :start p]
                     [:drag :drop :selected]]))}
      (if is-selected?
        [ui/minus-icon]
        (when selectable?
          [ui/plus-icon]))]]]])

(defn ui-players
  [all-players {:players/keys [selected]}]
  (let [ss (set selected)
        selectable? (< (count selected) 4)]
    [lui/column
     {:class ["flex" "flex-col" "gap-2" "rounded-lg" "w-full"]}
     [lui/column-body
      {:on {:drop (when (> 4 (count selected)) [[:drag :drop :selected]])}
       :class ["bg-blue-400"  "shadow-md"]
       :style {:min-height "80px"}}
      (map
       #(player-card selectable? % (contains? ss %))
       all-players)]]))

(defn couple->str
  [couple]
  (str "r: " (first couple) ", " "d: " (second couple)))


(defn ui-matches
  [matches ca cb]
  (let [prev-matches (fs/same-match? [ca cb] matches)
        prev-matches-id (set (map :id prev-matches))
        rest-matches (filter #(not (contains? prev-matches-id (:id %)))
                             matches)
        ca-matches (fs/couple-matches? ca rest-matches)
        cb-matches (fs/couple-matches? cb rest-matches)
        date-fn (fn [d] (.toLocaleString (js/Date. d)))]
    [:div
     (when (seq prev-matches)
       [:div
        [:h2.p-4.text-bold.text-lg.mt-4 "Histórico Enfrentamiento Parejas"]
        [:hr.mx-4.mb-4
         {:style {:border "1px solid gray"}}]
        [:div.px-4.flex.flex-col.gap-4
         (map #(e/ui-match % date-fn) prev-matches)]])

     (when (or (seq ca-matches) (seq cb-matches))
       [:div
        [:h2.p-4.text-bold.text-lg.mt-4 "Histórico por Pareja"]
        [:hr.mx-4.mb-4
         {:style {:border "1px solid gray"}}]
        [:div.flex.flex-col.gap-8
         [:div.px-4.flex.flex-col.gap-4
          [:h2.text-right.text-bold (str (first ca) " &  " (second ca))]
          (map #(e/ui-match % date-fn ca) ca-matches)]
         [:div.px-4.flex.flex-col.gap-4
          [:h2.text-right.text-bold (str (first cb) " &  " (second cb))]
          (map #(e/ui-match % date-fn cb) cb-matches)]]])]))

(defn- analysis
  [{:keys [forecast] :as state}]
  (let [ps (:players/selected forecast)
        cs (for [i (range (count ps))
                 j (range (inc i) (count ps))]
             [i j])
        matches [[(nth cs 0) (nth cs 5)]
                 [(nth cs 1) (nth cs 4)]
                 [(nth cs 2) (nth cs 3)]]]
    [lui/column
     (for [[[a b] [c d]] matches]
       (let [a (nth ps a)
             b (nth ps b)
             c (nth ps c)
             d (nth ps d)]
         [lui/accordion-item
          [lui/accordion-item-title
           {:class ["grid" "grid-cols-3" "items-center"]}
           [:span (str a " &  " b)]
           [:span.text-center "vs"]
           [:span.text-right (str c " &  " d)]]
          [lui/accordion-item-body
           [:div
            (e/ui-probability state (fn [n] (.toFixed n 2)) [[a b] [c d]])
            (ui-matches (-> state :match :results)
                        [a b]
                        [c d])]]]))]))

(defn component
  [{:keys [forecast] :as state}]
  [:div.flex.flex-col.gap-4
   [:div.flex.flex-col.gap-4
    (ui-players (-> state :stats :players) forecast)]
   (when (= 4 (-> state :forecast :players/selected count))
     (analysis state))])
