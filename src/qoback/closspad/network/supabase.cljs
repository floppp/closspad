(ns qoback.closspad.network.supabase
  (:require [cljs.core.async :as async]
            [cljs.core.async.interop :refer [<p!]]
            [qoback.closspad.state.db :refer [get-dispatcher]]
            [qoback.closspad.network.domain :refer [supabase organization]]))


(defn handle-supabase-auth
  [auth-fn {:keys [email password]}]
  (async/go
    (try
      (let [response (<p! (auth-fn #js {:email email :password password}))
            error (.-error response)
            data (js->clj (.-data response) {:keywordize-keys true})]
        (if error
          {:error error}
          {:success data}))
      (catch js/Error err
        {:error (ex-cause err)}))))

(defn login
  [[email pass] {:keys [on-success on-error]}]
  (async/go
    (let [dispatcher (get-dispatcher)
          auth-fn #(-> supabase .-auth (.signInWithPassword %))
          result (async/<! (handle-supabase-auth auth-fn {:email email :password pass}))
          {:keys [success error]} result]
      (if error
        ;; TODO: events to dispatch must be pass as params
        (dispatcher nil (conj [[:db/dissoc :db/login]
                               [:db/assoc-in [:db/login :error error]]
                               on-error]))
        (dispatcher nil (conj
                         [[:db/dissoc :db/login]
                          [:db/assoc :auth success]
                          [:auth/check-login]]
                         on-success))))))

(defn post
  [table entity {:keys [on-success on-failure]}]
  (let [dispatcher (get-dispatcher)]
    (async/go
      (try
        (let [response (<p! (-> supabase
                                (.from table)
                                (.insert (clj->js [entity]))
                                (.select)))
              error (.-error response)]
          (if error
            (dispatcher nil (on-failure error))
            (dispatcher nil (on-success))))
        (catch js/Error err
          (dispatcher nil [[:data/error (ex-cause err)]]))))))

(defn post-match
  [table match]
  (post table match {:on-failure (fn [err]
                                   [[:data/error err]])
                     :on-success (fn [_]
                                   [[:db/dissoc :add/match]
                                    [:auth/check-login]
                                    [:data/query [1 2]]])}))
