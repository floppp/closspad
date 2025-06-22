(ns qoback.closspad.components.dialog
  (:require [qoback.closspad.state.db :refer [get-dispatcher]]))

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
                   :display "grid"
                   :place-items "center"
                   :background "rgba(0, 0, 0, 0.5)"
                   :backdrop-filter "blur(5px)"
                   :overflow "hidden"
                   :top 0
                   :left 0}
           :on {:click  [[:db/dissoc :ui/dialog]]}}
     [:dialog
      {:open opened?
       :style {:border "none"
               :padding "20px"
               :box-shadow "0 4px 12px rgba(0, 0, 0, 0.15)"
               :background "white"
               :max-height "75vh"
               :overflow "auto"
               :z-index "100"}}
      (when title [:h2.mb-4.pb-2 title])
      (map
       (fn [[k v]]
         [:p.flex.justify-between.gap-8.mb-2
          [:span (name k)]
          [:span v]])
       (filter (fn [[k _]] (not= k :id)) info))
      (when extra-node
        extra-node)]]))
