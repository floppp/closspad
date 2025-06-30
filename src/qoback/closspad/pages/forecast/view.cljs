(ns qoback.closspad.pages.forecast.view
  (:require [clojure.string]
            [qoback.closspad.pages.forecast.elements :as e]
            [qoback.closspad.pages.forecast.desktop-view :as desktop]
            [qoback.closspad.pages.forecast.mobile-view :as mobile]))

(defn view
  [{:keys [forecast] :as state}]
  (let [ps (-> state :stats :players)
        is-mobile? (-> state :app :screen/is-mobile?)
        show-analysis? (= 4 (-> state :forecast :players/selected count))]
    [:div.bg-white.rounded-lg.shadow-md.p-6.mx-auto.w-full.pb-10
     [:h1.text-3xl.font-bold.text-gray-800.mb-6 "Simulador Partida"]
     [:button.btn.w-full.mb-4.rounded-lg.text-white.font-bold.shadow-md.bg-blue-400
      {:on
       {:click
        [[:event/prevent-default]
         [:db/dissoc :forecast]
         [:db/assoc-in [:forecast :players/non-selected] ps]]}}
      "Limpiar"]
     [:div.flex.flex-col.gap-4
      (if is-mobile?
        (mobile/component ps forecast)
        (desktop/component ps forecast))
      (when show-analysis?
        (e/analysis state is-mobile?))]]))
