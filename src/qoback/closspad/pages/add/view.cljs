(ns qoback.closspad.pages.add.view
  (:require [qoback.closspad.pages.add.elements :as ui]
            [qoback.closspad.components.match.domain :as m]))

(def common-style
  ["p-2"])

(def select-style
  (concat common-style
          ["text-gray-700"
           "bg-white"
           "rounded-md"
           "focus:outline-none"
           "focus:ring-2"
           "focus:border-gray-300"]))

(defn ui-set-points
  [s total-s]
  [:div.flex.flex-col.gap-4.border.padding
   [:div.flex.gap-4.justify-between.items-center.sm:justify-start
    [:span
     {:class ["pt-2" "pl-2"]}
     (str "Set " (inc s))]
    (when (and (> s 0) (= s (dec total-s)))
      [:button.pt-2.ml-2.text-red-500
       {:on {:click [[:remove/match-set s]]}}
       "Eliminar Set"])]
   [:div.flex.flex-col.gap-4.sm:flex-row
    [:input.flex-1
     {:class (concat common-style) :type "number" :min 0 :max 7
      :on {:change [[:add-match :event/target.value :result :a s]]}}
     "Equipo A"]
    [:input.flex-1
     {:class (concat common-style) :type "number" :min 0 :max 7
      :on {:change [[:add-match :event/target.value :result :b s]]}}
     "Equipo B"]]])

(defn ui-sets
  [n-sets]
  (for [s (range n-sets)]
    (ui-set-points s n-sets)))

(defn render-task-form
  [state]
  (let [new-match                          (-> state :add/match)
        player-opts                        (conj (-> state :stats :players) "")
        {:keys [couple_a couple_b n-sets]} new-match
        n-sets                             (min (or n-sets 1) 3)]
    [:form.mb-4.flex.gap-2.max-w-screen-sm.flex-col
     {:on {:submit [[:event/prevent-default]
                    [:post/network :match (m/new-match->match new-match)]]}}
     [:div.w-full
      [:div.w-full.flex.flex-col
       [:span {:class ["w-1/2" "p-2"]} "Equipo A"]
       [:div.w-full.flex.flex-col.sm:flex-row.gap-4
        [ui/player-options
         {:classes select-style
          :selection (first couple_a)
          :actions [[:event/prevent-default]
                    [:add-match :event/target.value :couple_a 0]]}
         player-opts]
        [ui/player-options
         {:classes select-style
          :selection (second couple_a)
          :actions [[:event/prevent-default]
                    [:add-match :event/target.value :couple_a 1]]}
         player-opts]]]]
     [:div.w-full
      [:div.w-full.flex.flex-col
       [:span {:class ["w-1/2" "p-2"]} "Equipo B"]
       [:div.w-full.flex.flex-col.sm:flex-row.gap-4
        [ui/player-options
         {:classes select-style
          :selection (first couple_b)
          :actions [[:event/prevent-default]
                    [:add-match :event/target.value :couple_b 0]]}
         player-opts]
        [ui/player-options
         {:classes select-style
          :selection (second couple_b)
          :actions [[:event/prevent-default]
                    [:add-match :event/target.value :couple_b 1]]}
         player-opts]]]]

     [:div.flex.flex-col.justify-between.gap-4.my-4.sm:flex-row
      [:input.flex-1.py-2
       {:type "datetime-local"
        :style {:background "white"
                :border-radius "4px"}
        :class ["sm:w-1/2"]
        :on {:change
             [[:add-match/played-at :event/target.value [:add/match :played_at]]]}}]
      [:button.text-blue-600.flex-1
       {:style {:border-radius "4px"}
        :on {:click [[:add/match-set]]}}
       [:strong "Añadir Set"]]]

     (ui-sets n-sets)

     (when (m/valid-match? (:add/match state))
       [:div.flex.justify-center
        [:button.btn.btn-soft.btn-info
         {:class ["w-full" "sm:w-1/2" "md:w-1/4"]
          :style {:border-radius "4px"}}
         "Crear"]])]))

(defn view
  [state]
  [:main.md:p-8.p-4.max-w-screen-m
   [:h1.text-2xl.mb-4 "Introducir Partido"]
   (render-task-form state)])
