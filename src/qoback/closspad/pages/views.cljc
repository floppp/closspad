(ns qoback.closspad.pages.views)

(defn view [state]
  ;(f-util/clog "view, state: " state)
  [:div.flex.h-screen
   [:div.flex-grow.p-4
    [:div.flex.flex-col.items-center.min-h-screen.mt-10

     (page-content state)]]])
