(ns qoback.closspad.state.supabase
  (:require [cljs.core.async :as async]
            [cljs.core.async.interop :refer [<p!]]
            [qoback.closspad.state.db :refer [get-dispatcher]]
            [qoback.closspad.network.domain :refer [supabase]]))


;; (defn handle-supabase-auth
;;   [auth-fn {:keys [email password on-success on-failure]}]
;;   (async/go
;;     (try
;;       (let [response (<p! (auth-fn #js {:email email :password password}))
;;             error (.-error response)
;;             data (js->clj (.-data response) {:keywordize-keys true})]
;;         (if error
;;           (re-frame/dispatch (conj on-failure error))
;;           (re-frame/dispatch (conj on-success data))))
;;       (catch js/Error err
;;         (re-frame/dispatch (conj on-failure (ex-cause err)))
;; ))))

;; (re-frame/reg-fx
;;  :auth/supabase-login
;;  (fn [params]
;;    (handle-supabase-auth
;;     (fn [credentials]
;;       (-> supabase .-auth (.signInWithPassword credentials)))
;;     params)))

;; (re-frame/reg-fx
;;  :auth/supabase-signup
;;  (fn [params]
;;    (handle-supabase-auth
;;     (fn [credentials]
;;       (-> supabase .-auth (.signUp credentials)))
;;     params)))
