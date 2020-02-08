(ns common.range-test
  (:require [common.range :refer :all]
            [clojure.test :refer :all]))


(deftest test-range?
  (testing "range? "
    (is (= true (range? 5)))
    (is (= true (range? [10 20])))
    (is (= false (range? [5])))
    (is (= false (range? [1 2 3])))
    (is (= true (range? #{1 2})))
    (is (= true (range? #{})))
    (is (= false (range? nil)))))

(deftest test-in-range?
  (testing "in-range?"
    (is (= true (in-range? 5 [0 30])))
    (is (= false (in-range? 4 [5 10])))
    (is (= true (in-range? 4 [4 5])))
    (is (= true (in-range? 4 [0 4])))
    (is (= true (in-range? 5 5)))
    (is (= false (in-range? 5 6)))
    (is (in-range? 5 #{5 3 2}))
    (is (not (in-range? 8 #{5 3 2})))))

(deftest test-parse-range
  (is (= 5 (parse-range "5")))
  (is (= [5 10] (parse-range " 5-10")))
  (is (= #{3 2} (parse-range " 3, 2"))))

(deftest test-range->str
  (is (= "0" (range->str 0)))
  (is (= "5-10" (range->str [5 10])))
  (is (#{"2,3" "3,2"} (range->str #{2 3}))))
