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
  [[email pass]]
  (async/go
    (let [dispatcher (get-dispatcher)
          auth-fn #(-> supabase .-auth (.signInWithPassword %))
          result (async/<! (handle-supabase-auth auth-fn {:email email :password pass}))
          {:keys [success error]} result]
      (if error
        ;; TODO: events to dispatch must be pass as params
        (dispatcher nil [[:db/dissoc :db/login]
                         [:db/assoc-in [:db/login :error error]]])
        (dispatcher nil [[:db/dissoc :db/login]
                         [:db/assoc :auth success]
                         [:auth/check-login]
                         #_[:route/match {:date (js/Date.)}]])))))

(defn extract-scores [match]
  (let [n-sets (:n-sets match)]
    (mapv (fn [set-n]
            [(get match (keyword (str "couple-a-score-set-" set-n)))
             (get match (keyword (str "couple-b-score-set-" set-n)))])
          (range n-sets))))

(defn post
  [table {:keys [args]}]
  (let [dispatcher (get-dispatcher)
        {:keys [couple-a-1
                couple-a-2
                couple-b-1
                couple-b-2
                played-at]} args]
    (async/go
      (try
        (let [response (<p! (-> supabase
                                (.from table)
                                (.insert (clj->js
                                          [{:played_at played-at
                                            :couple_a [couple-a-1 couple-a-2]
                                            :couple_b [couple-b-1 couple-b-2]
                                            :organization organization
                                            :result (extract-scores args)}]))
                                (.select)))
              error (.-error response)]
          (if error
            (dispatcher nil [[:data/query [1 2]]])
            (dispatcher nil [[:data/error error]])))
        (catch js/Error err
          (dispatcher nil [[:data/error (ex-cause err)]]))))))

