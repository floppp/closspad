(ns qoback.closspad.components.dialog)

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
  [opened? {:keys [title info]}]
  [:div {:style {:position "absolute"
                 :width "100vw"
                 :height "100vh"
                 :display "grid"
                 :place-items "center"
                 :background "rgba(0, 0, 0, 0.5)"
                 :backdrop-filter "blur(5px)"}
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
     (filter (fn [[k _]] (not= k :id)) info))]])
