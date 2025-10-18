(ns qoback.closspad.pages.add.view
  (:require [clojure.string :as str]
            [qoback.closspad.state.db :refer [get-dispatcher]]
            [qoback.closspad.widgets.select :as ui]
            [qoback.closspad.components.match.domain :as m]))

(def common-style
  ["p-2" "bg-slate-200"])

(def select-style
  (concat common-style
          ["text-gray-700"
           "rounded-md"
           "focus:outline-none"
           "focus:ring-2"
           "focus:border-gray-300"]))

(defn ui-set-points
  [s total-s]
  [:div.flex.flex-col.gap-4.padding
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
     {:class common-style
      :type "number" :min 0 :max 7
      :on {:change [[:add-match :event/target.value :result :a s]]}}
     "Equipo A"]
    [:input.flex-1
     {:class common-style
      :type "number" :min 0 :max 7
      :on {:change [[:add-match :event/target.value :result :b s]]}}
     "Equipo B"]]])

(defn ui-sets
  [n-sets]
  [:div.mt-8.flex.flex-col
   [:button.text-blue-600.flex-1.text-left.mb-4.ml-2
    {:style {:border-radius "4px"}
     :on {:click [[:event/prevent-default]
                  [:add/match-set]]}}
    [:strong "Añadir Set"]]
   (for [s (range n-sets)]
     (ui-set-points s n-sets))])

(defn render-task-form
  [state]
  (let [new-match                          (-> state :add/match)
        player-opts                        (conj (-> state :stats :players) "")
        {:keys [couple_a couple_b n-sets importance]} new-match
        n-sets                             (min (or n-sets 1) 3)]
    [:form.mb-4.flex.gap-2.flex-col
     {:on {:submit [[:event/prevent-default]
                    [:post/network :match (m/new-match->match new-match)]]}}

     [:div.w-full.flex.flex-col
      [:span {:class ["w-1/2" "p-2"]} "Equipo A"]
      [:div.w-full.flex.flex-col.sm:flex-row.gap-4
       [ui/select
        {:classes select-style
         :selection (first couple_a)
         :actions [[:event/prevent-default]
                   [:add-match :event/target.value :couple_a 0]]}
        player-opts]
       [ui/select
        {:classes select-style
         :selection (second couple_a)
         :actions [[:event/prevent-default]
                   [:add-match :event/target.value :couple_a 1]]}
        player-opts]]]

     [:div.w-full.flex.flex-col
      [:span {:class ["w-1/2" "p-2"]} "Equipo B"]
      [:div.w-full.flex.flex-col.sm:flex-row.gap-4
       [ui/select
        {:classes select-style
         :selection (first couple_b)
         :actions [[:event/prevent-default]
                   [:add-match :event/target.value :couple_b 0]]}
        player-opts]
       [ui/select
        {:classes select-style
         :selection (second couple_b)
         :actions [[:event/prevent-default]
                   [:add-match :event/target.value :couple_b 1]]}
        player-opts]]]

     [:div.w-full.flex.flex-col
      [:span {:class ["w-1/2" "p-2"]} "Tipo de Partido"]
      [:div.w-full.flex.flex-col.sm:flex-row.gap-4
       [ui/select
        {:classes (conj select-style "flex-1")
         :selection importance
         :actions [[:event/prevent-default]
                   [:add-match/importance :event/target.value [:add/match :importance]]]}
        (->> m/importances keys (map (comp str/capitalize name)))]

       [:input.flex-1
        {:type "datetime-local"
         :style {:border-radius "4px"}
         :class (concat common-style ["sm:w-1/2" "p0"])
         :on {:change
              [[:add-match/played-at :event/target.value [:add/match :played_at]]]}}]]]

     (ui-sets n-sets)

     (when (m/valid-match? (:add/match state))
       [:div.flex.justify-center
        [:button.btn.btn-soft.btn-info
         {:class ["w-full" "sm:w-1/2" "md:w-1/4"]
          :style {:border-radius "4px"}}
         "Crear"]])]))

(defn view
  [state]
  [:div.bg-white.rounded-lg.shadow-md.p-6.mx-auto.w-full
   {:replicant/key :add-match

    :replicant/on-mount
    (fn [_]
      (let [dispatcher (get-dispatcher)]
        (dispatcher nil [[:db/dissoc :add/match]
                         [:db/assoc-in [:add/match :n-sets] 1]])))}

   [:h1.text-2xl.mb-4 "Introducir Partido"]
   (render-task-form state)])
