(ns qoback.closspad.components.dialog
  (:require [qoback.closspad.state.db :refer [get-dispatcher]]))

(defn component-dialog
  []
  ;; <!-- You can open the modal using ID.showModal() method -->
  ;; <button class="btn" onclick="my_modal_3.showModal()">open modal</button>
  [:dialog.modal
   [:div.modal-box
    [:form {:method "dialog"}
     [:button.btn.btn-sm.btn-circle.btn-ghost.absolute.right-2.top-2
      "✕"]]
    [:div "holan"]]])

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
               :border-radius "8px"
               :padding "20px"
               :box-shadow "0 4px 12px rgba(0, 0, 0, 0.15)"
               :background "white"
               :z-index "100"}}
      [:h2.mb-4.pb-2 title]
      (map
       (fn [[k v]]
         [:p.flex.justify-between.gap-8.mb-2
          [:span (name k)]
          [:span v]])
       (filter (fn [[k _]] (not= k :id)) info))
      (when extra-node
        extra-node)]]))
