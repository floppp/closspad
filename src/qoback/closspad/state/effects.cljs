(ns qoback.closspad.state.effects
  (:require [reitit.frontend.easy :as rfe]
            [qoback.closspad.state.db :refer [get-dispatcher]]
            [qoback.closspad.network.query :as network]
            [qoback.closspad.network.domain :refer [table]]
            [qoback.closspad.helpers :as h]
            [qoback.closspad.network.supabase :as supabase]))

(defn navigated-match-page
  [{:keys [date]}]
  (let [dispatcher (get-dispatcher)]
    (dispatcher
     nil
     [[:db/assoc :page/navigated {:page :match :date date}]])))

(defn goto->stats
  [player]
  (let [dispatcher (get-dispatcher)]
    (dispatcher
     nil
     [[:db/assoc :page/navigated {:page :stats :player player}]])))

(defn goto->page
  [page]
  (let [dispatcher (get-dispatcher)]
    (dispatcher
     nil
     [[:db/assoc :page/navigated {:page page}]])))

(defn perform-effect!
  [{:replicant/keys [^js js-event]} [effect & args]]
  (case effect
    :dom/fx.prevent-default (.preventDefault js-event)
    :route/fx.not-found
    (let [date (-> args
                   first
                   :match
                   :results
                   last
                   :played_at)]
      (rfe/push-state
       :route/match
       {:day (h/format-iso-date (if date date (js/Date.)))}))
    :route/fx.home (goto->page :home)
    :route/fx.login (let [session (-> args first :session :access_token)]
                      (if (nil? session)
                        (goto->page :login)
                        (rfe/push-state :route/home)))
    :route/fx.explanation (goto->page :explanation)
    :route/fx.add-match   (goto->page :add-match)
    :route/fx.push (rfe/push-state
                    (-> args first second)
                    {:player (-> args first first)})
    :route/fx.stats (goto->stats (-> args first first))
    :route/fx.match (navigated-match-page (-> args first first))
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
    (tap> (str "Unknown effect" effect))))
