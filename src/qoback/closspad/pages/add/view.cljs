(ns qoback.closspad.pages.add.view)

(def common-style
  ["p-2"
   "mb-4"
   "w-1/2"])

(def select-style
  (concat common-style
          ["text-gray-700"
           "bg-white"
           "rounded-md"
           "focus:outline-none"
           "focus:ring-2"
           "focus:border-gray-300"]))

(defn render-task-form
  [state]
  (let [new-match (-> state :add/match)
        players (-> state :stats :players)
        {:keys
         [couple-a-1
          couple-a-2
          couple-b-1
          couple-b-2
          played-at
          couple-a-score
          couple-b-score
          n-sets]} new-match
        n-sets (min (or n-sets 1) 3)]
    (.log js/console new-match)
    [:form.mb-4.flex.gap-2.max-w-screen-sm
     {:on {:submit [[:event/prevent-default]
                    (when-not (empty? couple-a-1)
                      [:post/match new-match])]}}

     [:div.w-full
      [:div.w-full.flex
       [:span {:class ["w-1/2" "p-2"]} "Equipo A"]
       [:span {:class ["w-1/2" "p-2"]} "Equipo B"]]
      [:div.couple-a.w-full.flex.gap-4
       [:select {:class select-style
                 :on {:change [[:add-match :event/target.value :couple-a-1]]}}
        (map (fn [p]
               [:option {:value p
                         :selected (= (keyword p) couple-a-1)} p]) players)]
       [:select {:class select-style
                 :on {:change [[:add-match :event/target.value :couple-b-1]]}}
        (map (fn [p]
               [:option {:value p
                         :selected (= (keyword p) couple-b-1)} p]) players)]]

      [:div.couple-a.w-full.flex.gap-4
       [:select {:class select-style
                 :on {:change [[:add-match :event/target.value :couple-a-2]]}}
        (map (fn [p]
               [:option {:value p
                         :selected (= (keyword p) couple-a-2)} p]) players)]
       [:select {:class select-style
                 :on {:change [[:add-match :event/target.value :couple-b-2]]}}
        (map (fn [p]
               [:option {:value p
                         :selected (= (keyword p) couple-b-2)} p]) players)]]

      (for [s (range n-sets)]
        [:div.flex.gap-4
         [:input
          {:class (concat common-style)
           :type "number"
           :min 0
           :max 7
           :on {:change
                [[:add-match :event/target.value (keyword (str "couple-a-score-set-" s))]]}}
          "Equipo A"]

         [:input
          {:class (concat common-style)
           :type "number"
           :min 0
           :max 7
           :on {:change
                [[:add-match :event/target.value (keyword (str "couple-b-score-set-" s))]]}}
          "Equipo B"]])

      [:div.flex.justify-between.gap-4.mb-8
       [:input.input
        {:type "datetime-local"
         :style {:background "white"
                 :border-radius "4px"}
         :class ["w-1/2"]
         :on {:change
                [[:add-match :event/target.value :played-at]]}}]
       [:div.flex.justify-end
        {:class ["w-1/2"]}
        [:button.btn.btn-outline.btn-info
         {:style {:border-radius "4px"}
          :on {:click [[:add/match-set]]}}
         "Añadir Set"]]]

      (when (and couple-a-1
                 couple-a-2
                 couple-b-1
                 couple-b-2
                 played-at
                 (> n-sets 0))
        [:div.flex.justify-center
         [:button.btn.btn-soft.btn-info
          {:class ["w-full" "sm:w-1/2" "md:w-1/4"]
           :style {:border-radius "4px"}
           :on {:click [[:post/match new-match]]}}
          "Crear"]])]]))

(defn view
  [state]
  [:main.md:p-8.p-4.max-w-screen-m
   [:h1.text-2xl.mb-4 "Introducir Partido"]
   (render-task-form state)])
