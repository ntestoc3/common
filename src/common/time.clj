(ns common.time
  (:require [java-time :as time]
            [fipp.ednize :as ednize]))

(extend-protocol ednize/IEdn
  java.time.LocalDate
  (-edn [x]
    (let [s (-> (time/format :iso-local-date x)
                (str "T00:00:00.000-00:00"))]
      (tagged-literal 'inst s)))
  java.time.LocalDateTime
  (-edn [x]
    (let [s  (time/format :iso-offset-date-time x)]
      (tagged-literal 'inst s))))

(defn date->local-date
  [date]
  (-> (time/instant date)
      (time/local-date (time/zone-id))))

(defn date->local-datetime
  [date]
  (-> (time/instant date)
      (time/local-date-time (time/zone-id))))
