(ns closspad.scenes
  (:require [portfolio.data :as data]
            [portfolio.ui :as portfolio]
            [replicant.dom :as r]
            [closspad.ui.card-scenes]))

:closspad.ui.card-scenes/keep

(data/register-collection!
 :closspad.ui
 {:title "UI elements"
  :idx 0})

(data/register-collection!
 :closspad.pages
 {:title "Page scenes"
  :idx 1})

(def light-theme
  {:background/background-color "#fff"
   :background/document-class "light"
   :background/document-data {:theme "light"}})

(def dark-theme
  {:background/background-color "#1e2329"
   :background/document-class "dark"
   :background/document-data {:theme "dark"}})

(defn main []
  (r/set-dispatch! #(prn %2))

  (portfolio/start!
   {:config {:css-paths ["/tailwind.css"]
             :background/options
             [{:id :light :title "Light" :value light-theme}
              {:id :dark :title "Dark" :value dark-theme}]
             :canvas/layout
             {:kind :rows :xs [light-theme dark-theme]}}}))
