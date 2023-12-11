(ns common.generators
  (:require [clojure.test.check.generators :as gen]
            [clojure.string :as str])
  (:import [org.joda.time.chrono ISOChronology]
           [org.joda.time.format ISODateTimeFormat]
           [org.joda.time LocalDate DateTime]))

(def mac-address
  "This generates MAC address strings, things like \"02:e4:f1:a6:54:82\"."
  (gen/fmap #(str/join ":" (map (partial format "%02x") %))
            (gen/vector (gen/choose 0x0 0xff) 6)))

(def ipv4-address
  "This generates IPv4 address strings, things like \"192.168.0.1\"."
  (gen/fmap #(str/join "." %)
            (gen/vector (gen/choose 0x0 0xff) 4)))

;;; TODO: add support for generating shortened IPv6 addresses.
(def ipv6-address
  "This generates IPv6 address strings,
  things like \"b6b5:4d8c:0d1f:4dfc:9b51:e8ba:3a39:4621\"."
  (gen/fmap (partial str/join ":")
            (gen/vector (gen/fmap (partial format "%04x")
                                  (gen/choose 0x0 0xffff))
                        8)))

(def port
  "Generates a valid port number."
  (gen/one-of [(gen/return nil)
               (gen/choose 1 65535)]))

(def valid-domain-name-characters
  "These are all of the characters that are valid for a domain name label."
  (seq "abcdefghijklmnopqrstuvwxyz0123456789-"))

(def valid-domain-name-endcap-characters
  "These are all of the characters that are valid for the first or last
  character of a domain name label."
  (seq "abcdefghijklmnopqrstuvwxyz0123456789"))

(def domain-name-label
  "This generates domain name labels, the components of a domain name. For
  example, in the domain name 'www.foo.com', there are three labels, 'www',
  'foo', and 'com'."
  (gen/such-that #(<= 1 (count %))
                 (gen/fmap str/join
                           (gen/tuple (gen/elements valid-domain-name-endcap-characters)
                                      (gen/fmap str/join (gen/such-that #(<= 1 (count %) 61)
                                                                        (gen/vector (gen/elements valid-domain-name-characters))))
                                      (gen/elements valid-domain-name-endcap-characters)))))

(def domain-name
  "This generates a complete domain name, such as 'www.foo.com'."
  (gen/such-that #(<= 1 (count %) 253)
                 (gen/fmap (partial str/join ".")
                           (gen/vector domain-name-label))))


(def default-chronology (ISOChronology/getInstanceUTC))

(def year-of-century
  (gen/choose 1 100))

(def year-of-era
  (gen/choose 1 10000))

(def month-of-year
  (gen/choose 1 12))

(def day-of-week
  (gen/choose 1 7))

(def day-of-month
  (gen/choose 1 28))

(def hour-of-day
  (gen/choose 0 23))

(def minute-of-hour
  (gen/choose 0 59))

(def second-of-minute
  (gen/choose 0 59))

(def millis-of-second
  (gen/choose 0 999))

;;;;;;;;;; Date-Times

(defn- date-time-tuple [& {:keys [chrono] :or {chrono default-chronology}}]
  (gen/tuple year-of-era month-of-year day-of-month
           hour-of-day minute-of-hour second-of-minute
           millis-of-second (gen/return chrono)))

(defn date-time [& {:keys [chrono] :or {chrono default-chronology}}]
  (gen/fmap (partial apply #(str (.toLocalDateTime (DateTime. %1 %2 %3 %4 %5 %6 %7 %8))))
          (date-time-tuple :chrono chrono)))

(defn- date-tuple [& _]
  (gen/tuple year-of-era month-of-year day-of-month))

(def local-date
  (gen/fmap (partial apply #(str (LocalDate. %1 %2 %3))) (date-tuple)))

(def email
  (gen/fmap (fn [[s1 s2]] (format "%s@%s.com" s1 s2))
            (gen/tuple (gen/such-that
                        (fn [s] (>= (count s) 2))
                        gen/string-alphanumeric)
                       (gen/such-that
                        (fn [s] (>= (count s) 2))
                        gen/string-alphanumeric))))
(comment

  (def dt (DateTime. 2023 1 1 8 8 9)) 
  (.toLocalDateTime dt)

  
  (gen/sample domain-name)

  (gen/sample (date-time))

  (gen/sample email)

  )

