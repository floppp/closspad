  (ns qoback.closspad.dev
    (:require [qoback.closspad.core :as app]))

  (defonce store (atom {}))
  (defonce el (js/document.getElementById "app"))

  (defn ^:dev/after-load main []
    (app/main store el))
