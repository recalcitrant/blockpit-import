(ns blockpit.import_test
  (:require [clojure.test :refer :all]
            [blockpit.bybit-pnl :refer :all]))

(deftest a-test
  (testing "I don't fail."
    (is (= 1 1))))

(deftest b-test
  (testing "I won't fail."
    (is (= 42 42))))
