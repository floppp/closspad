(ns qoback.closspad.ui.card-elements
  (:require [replicant.alias :refer [defalias]]
            [qoback.closspad.state.db :refer [get-dispatcher]]))

(defalias card-title
  [attrs body]
  [:h2.text-base.font-bold.flex.gap-2.items-start attrs body])

(defalias card-details
  [attrs body]
  [:div.text-base attrs body])

(defalias card
  [attrs body]
  [:article.rounded-md.shadow-sm.bg-base-100.relative
   (assoc attrs
          :draggable true
          :on {:dragstart (fn [_]
                            ;; he de hacerlo así, lanzando el evento no funciona
                            (let [dispatcher (get-dispatcher)]
                              (dispatcher
                               nil
                               [[:event/prevent-default]
                                [:drag :start (:element attrs)]])))
               :dragend (fn [_]
                          [[:event/prevent-default]
                           [:drag :end]])})
   [:div.p-4.flex.flex-col.gap-4
    body]])
