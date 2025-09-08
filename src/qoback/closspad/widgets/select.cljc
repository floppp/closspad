(ns qoback.closspad.widgets.select
  (:require [replicant.alias :refer [defalias]]))

(def common-style
  ["p-2" "bg-slate-200"])

(def default-select-style
  (concat common-style
          ["text-gray-700"
           "rounded-md"
           "focus:outline-none"
           "focus:ring-2"
           "focus:border-gray-300"]))

(defalias option
  [{:keys [selection render-fn]} [o]]
  [:option {:value o
            :selected (= o selection)}
   (if render-fn (render-fn o) o)])

(defalias select
  [{:keys [classes selection actions render-fn] :as attrs} options]
  [:select.w-full (conj attrs {:class classes :on {:change actions}})
   (map
    (fn [o] [option {:selection selection :render-fn render-fn} o])
    options)])


