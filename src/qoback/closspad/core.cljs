(ns qoback.closspad.core
  (:require [replicant.dom :as r-dom]
            [gadget.inspector :as inspector]
            [qoback.closspad.router :as router]
            [qoback.closspad.pages.views :as v]
            [qoback.closspad.state.db :refer [!state !dispatcher]]
            [qoback.closspad.state.events :refer [event-handler]]
            ["gsap" :as G]))

(def state (atom
            {:items (mapv #(hash-map :id % :text (str "Item " %))
                          (range 1 21))}))

#_(defn- render! [state]
  (r-dom/render
   (js/document.getElementById "app")
   (v/view state)))

;; (defn ^{:dev/after-load true :export true} start!
;;   []
;;   (render! @!state))

;; (defn watch! [render!]
;;   (add-watch !state
;;              ::render (fn [_ _ old-state new-state]
;;                         (when (not= old-state new-state)
;;                           (render! new-state)))))

;; (defn ^:export init! []
;;   (inspector/inspect "App state" !state)
;;   (r-dom/set-dispatch! event-handler)
;;   (swap! !dispatcher assoc :dispatcher event-handler)
;;   (router/start! router/routes event-handler)
;;   (watch! render!)
;;   (event-handler nil [[:data/query [1 2]]])
;;   (start!))


#_(ns your-app.core
  (:require [replicant.core :as replicant]
           ))



(defn shuffle-array [arr]
  (let [arr (into [] arr)]
    (loop [i (dec (count arr)) arr arr]
      (if (<= i 0)
        arr
        (let [j (rand-int (inc i))
              swapped (assoc arr i (arr j) j (arr i))]
          (recur (dec i) swapped))))))

(defn animate-reorder [items new-order]
  (let [container (js/document.querySelector ".container")
        items-array (into-array items)
        gsap (.-gsap G)
        first-positions (mapv #(let [rect (.getBoundingClientRect %)]
                                 {:left (.-left rect)
                                  :top (.-top rect)})
                              items)]
    (doseq [idx new-order]
      (.appendChild container (aget items-array idx)))

    (doseq [[i item] (map-indexed vector items)]
      (let [last-rect (.getBoundingClientRect item)
            first-pos (nth first-positions i)
            invert-x (- (:left first-pos) (.-left last-rect))
            invert-y (- (:top first-pos) (.-top last-rect))]

        (.set gsap item #js{:x invert-x :y invert-y})
        (.to gsap item #js{:x 0 :y 0 :duration 0.5 :ease "power2.out"})))))

(defn item-view [{:keys [id text]}]
  [:p {:key id
       :style {:padding "10px"
               :background "#eee"
               :margin "5px"
               :will-change "transform"}}
   text])

(defn app []
  (let [state @state]
    [:div
     [:div.container
      {:style {:display "flex"
               :flex-wrap "wrap"
               :gap "10px"}}
      (map item-view (:items state))]
     [:button
      {:on
       {:click
        #(let [new-order (shuffle-array (range (count (:items state))))]
           (animate-reorder
            (js/document.querySelectorAll ".container p")
            new-order))}}
      "Shuffle Items"]]))

;; Mount with Replicant
(defn mount []
  (r-dom/render
   (js/document.getElementById "app")
   (app))
  #_(replicant/render [app] (js/document.getElementById "app")))

(defn init! []
  (mount))
