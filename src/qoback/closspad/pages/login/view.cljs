(ns qoback.closspad.pages.login.view)

(defn- render-login-form [state]
  (let [{:db/keys [login]} state
        {:keys [email password]} login]
    [:div.mt-10.flex.justify-center
     [:div.bg-white.rounded-lg.shadow-lg.p-8.w-full.max-w-md

      ;; FIX-ME:
      ;; Me está dando muchos problemas y no tengo ni idea de por qué.
      #_[:form.flex.flex-col.gap-4
         {:on {:submit [[:event/prevent-default]
                        [:fetch/login email password]]}}]
      [:div.flex.flex-col.gap-4
       [:div
        [:label.block.text-sm.font-medium.text-gray-700.mb-1 "Email"]
        [:input.w-full.px-4.py-2.rounded-md.border.border-gray-300.focus:outline-none.focus:ring-2.focus:ring-blue-500
         {:type "text"
          :name "email"
          :placeholder "user@example.com"
          :required true
          :on {:input [[:db/login :event/target.value :email]]}}]]

       [:div
        [:label.block.text-sm.font-medium.text-gray-700.mb-1 "Password"]
        [:input.w-full.px-4.py-2.rounded-md.border.border-gray-300.focus:outline-none.focus:ring-2.focus:ring-blue-500
         {:type "password"
          :name "password"
          :placeholder "••••••••"
          :required true
          :on {:input [[:db/login :event/target.value :password]]}}]]

       [:button.w-full.bg-blue-600.text-white.py-2.px-4.rounded-md.hover:bg-blue-700.transition-colors
        (cond-> {:on {:click [[:fetch/login email password]]}}
          (or (empty? email) (empty? password))
          (merge {:disabled true :class "cursor-not-allowed"}))
        "Entrar"]]]]))

(defn view
  [state]
  (render-login-form state))
