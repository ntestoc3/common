(ns common.http
  (:refer-clojure :exclude [update get])
  (:require [clj-http.cookies :as cookies]
            [clj-http.client]
            [java-time :as time]
            [clojure.java.io :as io]
            [common.var :refer [pull]]
            [common.config :refer [get-config]]
            [taoensso.timbre :as log]
            )
  (:import (org.apache.http.impl.cookie BasicClientCookie2)
           (java.net URL)
           (java.security.cert Certificate X509Certificate)
           ))

;; 导出clj-http的函数到当前命名空间
(pull clj-http.client (get post))

(defn new-cookie
  [name value domain]
  (let [c (cookies/to-basic-client-cookie
           [name
            {:discard false
             :domain domain
             :path "/",
             :secure false,
             :expires  (-> (time/zoned-date-time)
                           (time/plus (time/days 10))
                           time/java-date),
             :value value}])]
    (.setAttribute c BasicClientCookie2/DOMAIN_ATTR "true")
    c))

(defn cookie-value
  [domain v]
  {:discard false
   :domain domain
   :path "/",
   :value v})

(defn build-header
  "从默认配置的http header加上custom-header构造http头"
  ([] (build-header {}))
  ([custom-header]
    (merge (get-config :default-header)
           {:headers {"User-Agent" (get-config :user-agent)
                      "Accept-Charset" "utf-8"}}
           custom-header)))



(defn get-cert-info
  [https-url]
  (let [certs (-> (URL. https-url)
                  (.openConnection)
                  (doto (.connect))
                  (.getServerCertificates))]
    certs))

(defn random-ua
  "返回一个随机的user-agent"
  []
  (with-open [rdr (-> (get-config :user-agents-file)
                      io/reader)]
    (-> (line-seq rdr)
        rand-nth)))

(defn get-cert-domains
  "获取https url证书中的域名，SAN不一定就是自己拥有的域名"
  [https-url]
  (let [certs (get-cert-info https-url)]
    (log/debug :get-cert-domains "cert chain count:" (count certs))
    ;; 一般网站都是两层证书链,第2层为根证书，不需要
    (let [cert (first certs)
          main-domain (-> (.getSubjectDN cert)
                          (.getCommonName))
          sub-domains (some->> (.getSubjectAlternativeNames cert)
                               vec
                               (map second))]
      (-> (apply vector main-domain sub-domains)
          set))))


(comment
  (def c1 (get-cert-info "https://www.baidu.com"))

  (def c2 (get-cert-domains "https://www.baidu.com"))

  (def c3 (get-cert-domains "https://www.163.com"))

  )
