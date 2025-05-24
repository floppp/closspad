(ns qoback.closspad.pages.widgets-test
  (:require [cljs.test :refer [deftest is testing]]
            [qoback.closspad.pages.widgets :as widgets]
            [qoback.closspad.helpers :as h]))

(defn- make-date [date-str]
  (js/Date. date-str))

(deftest add-day-test
  (let [dates [(make-date "2025-05-20")
               (make-date "2025-05-21")
               (make-date "2025-05-22")]]

    (testing "Normal day increment"
      (is (= (.getTime (make-date "2025-05-21"))
             (.getTime (widgets/add-day (make-date "2025-05-20") dates)))))

    (testing "Last day returns nil"
      (is (nil? (widgets/add-day (make-date "2025-05-22") dates))))

    (testing "Date not in list finds next"
      (is (= (.getTime (make-date "2025-05-21"))
             (.getTime (widgets/add-day (make-date "2025-05-20T12:00:00") dates)))))

    (testing "Empty list returns nil"
      (is (nil? (widgets/add-day (make-date "2025-05-20") []))))))

(deftest arrow-right-behavior
  (let [dates [(make-date "2025-05-20")
               (make-date "2025-05-21")]]
    (testing "Last day should not advance"
      (is (nil? (-> (widgets/arrow-right (make-date "2025-05-21") dates)
                    :props :href))))))

;; Add more tests for other widget functions as needed
