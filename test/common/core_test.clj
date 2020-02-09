(ns common.core-test
  (:require [midje.sweet :refer :all]
            [common.core :refer :all]))


(fact "deep merge"

      (deep-merge {:a 1} {:a 2}) => {:a 2}
      (deep-merge {:a 1 :b 2} {:a nil}) => {:a nil :b 2}
      (deep-merge {:a 1 :b 2} {:a nil} {:a false}) => {:a false :b 2}
      (deep-merge {:a {:b {:c 1 :d false}}} {:a {:b {:d nil}}}) => {:a {:b {:c 1 :d nil}}}
      (deep-merge {:a 1} nil) => {:a 1}

      )
