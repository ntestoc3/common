(ns common.warp-test
  (:require  [midje.sweet :refer :all]
             [common.wrap :refer :all]))

(fact "with exception"
      (with-exception-default 1
        (/ 2 0)) => 1

      (with-exception-default 1
        (/ 4 2)) => 2

      )
