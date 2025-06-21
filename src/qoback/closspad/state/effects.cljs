(ns qoback.closspad.state.effects
  (:require [reitit.frontend.easy :as rfe]
            [qoback.closspad.network.query :as network]
            [qoback.closspad.network.domain :refer [table]]
            [qoback.closspad.network.supabase :as supabase]
            [qoback.closspad.state.dom-effects :as dom]
            [qoback.closspad.state.route-effects :as re]))

(defn perform-effect!
  [{:replicant/keys [^js js-event]} [effect & args]]
  (case effect
    :dom/fx.effect (dom/process args)
    :dom/fx.prevent-default (.preventDefault js-event)
    :route/fx.push (rfe/push-state
                    (-> args first second)
                    {:player (-> args first first)})
    :route/fx (re/effects (first args))
    :auth/fx.check-login (let [session (-> args first :session :access_token)]
                           (when-not (nil? session)
                             (rfe/push-state :route/home)))
    :auth/fx.check-not-logged (let [session (-> args first :session :access_token)]
                                (when (nil? session)
                                  (rfe/push-state :route/home)))
    ;; TODO: hacer comprobación para enviar o no
    :data/fx.query (network/query-async {:query/kind :query/matches :query/data args})
    :post/fx.match (supabase/post table (first args))
    :fetch/fx.login (supabase/login (first args))
    (tap> (str "Unknown effect " effect))))
