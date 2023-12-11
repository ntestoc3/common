(ns common.common-schemas
  (:require [malli.core :as m]
            [common.validator :as validator]
            [common.generators :as g]
            [java-time :as jt]
            [malli.registry :as mr]
            [hyperfiddle.rcf :refer [tests tap]]
            [clojure.string :as str]
            [malli.generator :as mg]))

(def registry*
  (atom (m/default-schemas)))

(defn register! [type ?schema]
  (swap! registry* assoc type ?schema))

(mr/set-default-registry!
 (mr/mutable-registry registry*))

(register! :date
           (m/-simple-schema
            {:type :date
             :pred #(try (do (jt/local-date %) true)
                         (catch Exception e false))
             :type-properties {:error/message "should be valid date"
                               :json-schema/type "string"
                               :json-schema/example "2023-01-01"
                               :json-schema/format "date"
                               :gen/gen g/local-date}}))



(register! :datetime
           (m/-simple-schema
            {:type :datetime
             :pred #(try (do (jt/local-date-time %) true)
                         (catch Exception e false))
             :type-properties {:error/message "should be valid datetime"
                               :json-schema/type "string"
                               :json-schema/example "2023-01-01T08:08:09"
                               :json-schema/format "date"
                               :gen/gen (g/date-time)}}))



(tests
  "check date"

  (m/validate :date "2023-01-02") :=  true

  (m/validate :date "2023-01-32") := false
  (m/validate :date "") := false

  (m/validate :date "2999-05-05") := true

  (mg/generate :date)


  (m/validate :datetime "1778-07-26T05:59:28.450") := true
  (mg/generate :datetime)

 )

(register! :ipv4
           (m/-simple-schema
            {:type :ipv4
             :pred validator/valid-ipv4?
             :type-properties {:error/message  "should be valid ip v4 address"
                               :json-schema/type "string"
                               :json-schema/example "8.8.8.8"
                               :json-schema/format "ipv4"
                               :gen/gen g/ipv4-address}}))

(register! :ipv6
           (m/-simple-schema
            {:type :ipv6
             :pred validator/valid-ipv6?
             :type-properties {:error/message  "should be valid ip v6 address"
                               :json-schema/type "string"
                               :json-schema/example "b6b5:4d8c:0d1f:4dfc:9b51:e8ba:3a39:4621"
                               :json-schema/format "ipv6"
                               :gen/gen g/ipv6-address}}))


(tests
  (m/validate :ipv4 "192.168.0.1") := true

  (m/validate :ipv4 "192.168.0.256") := false

  (mg/generate :ipv4)

  (m/validate  :ipv6 "2001:0db8:85a3:0000:0000:8a2e:0370:7334") := true

  (mg/generate :ipv6)

  )


(register! :email
           (m/-simple-schema
            {:type :email
             :pred validator/valid-email?
             :type-properties {:error/message  "should be valid email address"
                               :json-schema/type "string"
                               :json-schema/example "test@test.com"
                               :json-schema/format "email"
                               :gen/gen g/email}}))

(tests
  (m/validate :email "mail@163.com") := true

  (m/validate :email "mail.163.com") := false

  (m/validate :email "a@.com") := false

  (mg/generate :email)
  )
