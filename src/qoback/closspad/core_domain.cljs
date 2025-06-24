(ns qoback.closspad.core-domain
  (:require [cljs.spec.alpha :as s]))

(s/def ::non-empty-string (s/and string? #(seq %)))
