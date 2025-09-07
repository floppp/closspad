(ns qoback.closspad.components.dialog
  (:require [qoback.closspad.state.db :refer [get-dispatcher]]
            [qoback.closspad.utils.datetime :as dt]
            [qoback.closspad.utils.strings :as s]))

(defn- v->str
  [v]
  (cond
    (number? v) (.toFixed v 2)
    (instance? js/Date v) (dt/datetime->date->str v)
    (dt/invalid-date? v) v
    (instance? js/Date (js/Date. v)) (dt/datetime->date->str (js/Date. v))
    :else v))

(defn component
  [opened? {:keys [title info extra-node]}]
  (let [dispatcher (get-dispatcher)]
    (dispatcher nil [(if opened?
                       [:dom/effect :body/scroll-hidden]
                       [:dom/effect :body/scroll-show])]))
  (when opened?
    [:div {:style {:position "fixed"
                   :width "100vw"
                   :height "100vh"
                   :display "flex"
                   :background "rgba(0, 0, 0, 0.5)"
                   :backdrop-filter "blur(5px)"
                   :overflow "hidden"
                   :top 0
                   :left 0
                   :z-index "10"
                   :align-items "center"
                   }
           :on {:click  [[:db/dissoc :ui/dialog]]}}

     [:div.fixed.inset-0.flex.items-center.justify-center.backdrop-blur-sm.z-10
      [:div {:class ["bg-white" "rounded-lg" "p-6" "m-6"
                     "max-w-lg" "w-full" "max-h-[75vh]" "overflow-auto"]}
       (when title [:h2.mb-4.pb-2 title])
       [:div
        (map
         (fn [[k v]]
           [:p.flex.justify-between.gap-8.mb-2
            [:span (s/camel->capitalize (name k))]
            [:span (v->str v)]])
         (filter (fn [[k _]] (not= k :id)) info))]
       (when extra-node extra-node)
       ]]


     #_[:dialog.rounded-lg
        {:open opened?
         :style {:padding "20px"
                 :max-height "75vh"
                 :overflow "auto"
                 :margin "auto"}}

        (when title [:h2.mb-4.pb-2 title])
        [:div
         (map
          (fn [[k v]]
            [:p.flex.justify-between.gap-8.mb-2
             [:span (name k)]
             [:span v]])
          (filter (fn [[k _]] (not= k :id)) info))]
        (when extra-node
          extra-node)]]))
