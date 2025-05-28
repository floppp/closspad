(ns qoback.closspad.state.supabase
  (:require [cljs.core.async :as async]
            [cljs.core.async.interop :refer [<p!]]
            [qoback.closspad.state.db :refer [get-dispatcher]]
            [qoback.closspad.network.domain :refer [supabase]]))


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
  [[email pass]]
  (async/go
    (let [dispatcher (get-dispatcher)
          auth-fn #(-> supabase .-auth (.signInWithPassword %))
          result (async/<! (handle-supabase-auth auth-fn {:email email :password pass}))
          {:keys [success error]} result]
      (if error
        (dispatcher nil [[:db/dissoc :db/login]
                         [:db/assoc-in [:db/login :error error]]])
        (dispatcher nil [[:db/dissoc :db/login]
                         [:db/assoc-in [:db/login :user (get-in success [:user :email])]]
                         [:route/match {:date (js/Date.)}]])))))
