(ns common.validator
  (:require [clojure.spec.gen.alpha :as gen]
            [me.raynes.fs :as fs]
            [camel-snake-kebab.core :as csk]
            [clojure.string :as str]
            [malli.core :as m]
            [java-time :as jt])
  (:import [org.apache.commons.validator.routines
            DomainValidator
            EmailValidator
            InetAddressValidator]
           java.io.File))

(defmacro def-validator
  [validator-type doc]
  (let [fn-name (-> (str "valid-" (csk/->kebab-case-string validator-type) "?")
                    symbol)
        classname (-> (str validator-type "Validator")
                      symbol)]
    `(defn ~fn-name
       ~doc
       [s#]
       (-> (. ~classname getInstance)
           (.isValid s#)))))

(def-validator InetAddress "是否是一个有效的ip v4或ip v6字符串")

(defn valid-ipv4?
  [ip]
  (and (string? ip)
       (-> (InetAddressValidator.)
           (.isValidInet4Address ip))))

(defn valid-ipv6?
  [ip]
  (and (string? ip)
       (-> (InetAddressValidator.)
           (.isValidInet6Address ip))))

(def-validator Domain "是否为一个有效的域名")

(def-validator Email "是否为一个有效的email")

(defn valid-phone?
  [s]
  (boolean (re-matches #"^1\d{10}$" s)))

(defn canonical-path
  "获取文件的绝对路径，去掉./ ../"
  [f]
  (try
    (.getCanonicalPath f)
    (catch Exception _ nil)))

(defn valid-path?
  "检查文件s是否在root-path文件夹下"
  [s root-path]
  (let [base-path (-> root-path
                      fs/file
                      canonical-path
                      (str File/separator))
        real-path (-> (fs/file base-path s)
                      canonical-path)]
    (and base-path
         real-path
         (str/starts-with? real-path base-path))))

