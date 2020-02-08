(ns common.wrap
  (:require [taoensso.timbre :as log]))

(defmacro with-exception-default
  [default & body]
  `(try ~@body
        (catch Exception e#
          (log/error e#)
          ~default)))
