(ns common.config-test
  (:require [common.config :refer :all]
            [midje.sweet :refer :all]
            [clojure.java.io :as io]
            [cprop.source :as source]))

(def test-env {:test 1
               :conf1 true
               :conf2 nil
               :conf3 false
               :conf4 []})

(defmacro with-config
  [& forms]
  `(with-redefs [default-config "./test_config.edn"
                 env test-env]
     ~@forms))

(fact "read curr config."
 (with-config
   (read-curr-config) => {}
   (spit default-config (str {:conf3 true
                              :conf4 [1 2 3]}))
   (read-curr-config) => {:conf3 true
                          :conf4 [1 2 3]}
   (io/delete-file default-config)

   ))

(fact "get and set config."
      (with-config
        (read-curr-config) => {}
        (spit default-config (str {:conf3 true
                                   :conf4 [1 2 3]}))
        (with-redefs [env (merge env
                                 (read-curr-config))]
          (get-config :conf3) => true
          (get-config :conf4) => [1 2 3]
          (get-config :test) => 1
          (get-config :test5) => nil
          (get-config :conf2) => nil

          ;; 正确，因为nil无法判断
          (set-config! :conf3 nil)
          (get-config :conf3) => true

          (set-config! :conf3 false)
          (get-config :conf3) => false
          )
        (reset-config!)
        (io/delete-file default-config)
        ))

(fact "get-in set-in test"
      (with-config
        (reset-config!)
        (get-in-config []) => {}
        (get-in-config [:test]) => 1
        (get-in-config [:conf4]) => []

        (set-in-config! [:a :b] false)
        (get-in-config [:a :b]) => false

        (set-in-config! [:a :b] nil)
        (get-in-config [:a :b]) => nil

        (set-in-config! [:a :b] true)
        (get-in-config [:a :b]) => true

        (reset-config!)
        ))
