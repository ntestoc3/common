(ns common.thread
  (:require [java-time :as time]))

(def default-wait-death (time/seconds 5))
(def default-wait-delay-ms 10)
(defn wait-until*
  "wait until a function has become true"
  ([name fn] (wait-until* name fn default-wait-death))
  ([name fn wait-death]
   (let [die (time/plus (time/local-time) wait-death)]
     (loop []
       (if-let [result (fn)]
         result
         (do
           (Thread/sleep default-wait-delay-ms)
           (if (time/after? (time/local-time) die)
             (throw (Exception. (str "timed out waiting for: " name)))
             (recur))))))))

(defmacro wait-until
  [expr]
  `(wait-until* ~(pr-str expr) (fn [] ~expr)))

