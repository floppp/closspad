(ns qoback.closspad.network.supabase
  (:require [cljs.core.async :as async]
            [cljs.core.async.interop :refer [<p!]]
            [qoback.closspad.components.match.domain :as m]
            [qoback.closspad.state.db :refer [get-dispatcher]]
            [qoback.closspad.network.domain :refer [supabase]]
            [clojure.string :as str]))


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



(defn user-friendly-error [error]
  (let [error-str (if (string? error) error (str error))
        friendly (cond
                   ;; Timeout or abort
                   (or (str/includes? error-str "timeout")
                       (str/includes? error-str "abort"))
                   "Error de conexión: tiempo de espera agotado"

                   ;; Network error
                   (str/includes? error-str "failed to fetch")
                   "Error de conexión con Supabase"

                   ;; Duplicate constraint (code 23505)
                   (or (str/includes? error-str "23505")
                       (str/includes? error-str "duplicate")
                       (str/includes? error-str "already exists"))
                   "El partido ya existe"

                   ;; Default
                   :else "Error al guardar el partido")]
    {:friendly-message friendly
     :full-error error-str
     :show-details false}))


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
                               [:db/assoc-in [:db/login :error (->> error user-friendly-error :friendly-message)]]
                               [:db/dissoc :is-loading?]
                               on-error]))
        (dispatcher nil (conj
                         [[:db/dissoc :db/login]
                          [:db/assoc :auth success]
                          [:auth/check-login]]
                         on-success))))))

(defn- post
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
            (dispatcher nil (on-success
                             (-> response
                                 .-data
                                 (aget 0)
                                 (js->clj {:keywordize-keys true}))))))
        (catch js/Error err
          (dispatcher nil [[:data/error (ex-cause err)]]))))))

(defn post-match
  [table match]
  (post table
        (-> match
            (dissoc :n-sets)
            (assoc :importance (-> match :importance str/lower-case keyword m/importances)))
        {:on-failure (fn [err]
                       [[:data/error (user-friendly-error err)]])
         :on-success (fn [added-match]
                        [[:db/dissoc :add/match]
                         [:data/query]
                         [:route :match {:date (-> added-match :played_at)}]
                         [:match :new added-match]])}))
