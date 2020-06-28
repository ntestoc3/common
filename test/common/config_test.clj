(ns common.config-test
  (:require [common.config :refer :all]
            [midje.sweet :refer :all]))

(fact "set-config"
      (get-config :test) => nil
      (get-config [:a :b]) => nil
      (set-config! :test 123)

      (get-config :test) => 123

      (set-config! [:a :b] 456)
      (get-config :a :b) => 456

      (set-config! :ccf false)
      (get-config :ccf) => false

      (update-config! [:a :b] inc)
      (get-config :a :b) => 457

      (update-config! :test + 111)
      (get-config :test) => 234
      )


