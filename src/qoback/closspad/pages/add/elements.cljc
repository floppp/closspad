(ns qoback.closspad.pages.add.elements
  (:require [replicant.alias :refer [defalias]]))

(defalias player-option
  [{:keys [selection]} [p]]
  [:option {:value p :selected (= p selection)} p])

(defalias player-options
  [{:keys [classes selection actions]} players]
  [:select.w-full {:class classes :on {:change actions}}
   (map (fn [p] [player-option {:selection selection} p]) players)])

(defalias card-title
  [attrs body]
  [:h2.text-base.font-bold.flex.gap-2.items-start attrs body])

(defalias card-details
  [attrs body]
  [:div.text-base attrs body])

(defalias card-action
  [attrs body]
  (into [:div.absolute.top-0.right-0.m-4 attrs] body))

#_(defalias card
  [attrs body]
  [:article.card.shadow-sm.bg-base-100.relative
   (cond-> (assoc attrs :draggable true)
     (::expanded? attrs) (assoc-in [:style :transform] "scale(1.2")
     (::expanded? attrs) (update :class concat [:border :z-5])
     :then (update-in [:on :dragstart] (augment-event [:actions/start-drag-move]))
     :then (update-in [:on :dragend] (augment-event [:actions/end-drag-move])))
   (into [:div.card-body.flex.flex-col.gap-4] body)])

(defalias column
  [attrs body]
  (into [:section.column.min-h-full.flex.flex-col.basis-full.gap-4 attrs] body))

(defalias column-body
  [attrs body]
  (into [:div.column-body.rounded-lg.p-6.flex.flex-col.gap-4
         (assoc-in attrs [:on :dragover]
                   (fn [#?(:cljs ^js e :clj e)]
                     (.log js/console e "dragoverx")
                     (.preventDefault e)
                     (set! (.-dropEffect (.-dataTransfer e)) "move")))]
        body))
