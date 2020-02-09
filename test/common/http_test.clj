(ns common.http-test
  (:require [common.http :refer :all]
            [common.config :as config]
            [midje.sweet :refer :all]))


(fact "build http options"
      (config/set-config! :default-http-option {:as :json})
      (:as (build-http-opt)) => :json

      (config/set-config! :user-agent "googlebot+")
      (get-in (build-http-opt) [:headers "User-Agent"]) => "googlebot+"

      (get-in (build-http-opt {:headers {"User-Agent" "none"}})
              [:headers "User-Agent"]) => "none"

      (config/reset-config!)
      )
