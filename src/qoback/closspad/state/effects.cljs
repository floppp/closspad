(ns qoback.closspad.state.effects
  (:require [reitit.frontend.easy :as rfe]
            [clojure.set :refer [difference]]
            [qoback.closspad.network.query :as network]
            [qoback.closspad.network.supabase :as supabase]
            [qoback.closspad.state.dom-effects :as dom]
            [qoback.closspad.state.route-effects :as re]
            [qoback.closspad.state.post-effects :as pe]

            [qoback.closspad.state.db :refer [get-dispatcher]]))

(defn perform!
  [subeffect [winners all-players]]
  (let [dispatcher (get-dispatcher)]
    (case subeffect
      :classification
      (let [winners (into #{} winners)
            all-players (into #{} (flatten all-players))
            losers (difference all-players winners)]
        (js/console.log winners losers))
      (tap> (str "[FORECAST] Unknown effect " subeffect)))))


(defn perform-effect!
  [{:replicant/keys [^js js-event]} [effect & args]]
  #_(when goog.DEBUG
      (.log js/console effect args))
  (case effect
    :dom/fx.prevent-default   (.preventDefault js-event)
    :dom/fx.effect            (dom/perform! args)
    :post/fx.network          (pe/perform! (first args))
    :forecast/fx.effect       (perform! (first args) (first (rest args)))
    :route/fx.push
    (let [page (-> args first last)]
      (case page
        :route/stats (rfe/push-state
                      page
                      {:player (-> args first first)})
        :route/unkown (rfe/push-state page)))
    :route/fx                 (re/perform! (first args))
    :auth/fx.check-login      (let [session (-> args first :session :access_token)]
                                (when-not (nil? session)
                                  (rfe/push-state :route/home)))
    :auth/fx.check-not-logged (let [session (-> args first :session :access_token)]
                                (when (nil? session)
                                  (rfe/push-state :route/home)))
    ;; TODO: hacer comprobación para enviar o no
    :data/fx.query (network/query-async
                    {:query/kind :query/matches
                     :query/data args})
    ;; :post/fx.match (supabase/post table (first args))
    :fetch/fx.login (supabase/login
                     (first args)
                     {:on-failure [:db/dissoc :is-loading?]
                      :on-success [:db/dissoc :is-loading?]})
    (tap> (str "Unknown effect " effect))))
