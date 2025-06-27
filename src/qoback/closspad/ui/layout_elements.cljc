(ns qoback.closspad.ui.layout-elements
  (:require [replicant.alias :refer [defalias]]))

(defalias column
  [attrs body]
  (into [:section.column.min-h-full.flex.flex-col.basis-full.gap-4 attrs] body))

(defalias column-body
  [attrs body]
  (into [:div.column-body.rounded-lg.p-6.flex.flex-col.gap-4
         (assoc-in attrs
                   [:on :dragover]
                   (fn [ev]
                     (.preventDefault ev)
                     (set! (.-dropEffect (.-dataTransfer ev)) "move")))]
        body))
