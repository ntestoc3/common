(ns common.http
  (:refer-clojure :exclude [update get])
  (:require [clj-http.cookies :as cookies]
            [clj-http.client]
            [java-time :as time]
            [clojure.java.io :as io]
            [common.core :refer [deep-merge]]
            [common.var :refer [pull]]
            [common.config :refer [get-config]]
            [taoensso.timbre :as log]
            [me.raynes.fs :as fs])
  (:import (org.apache.http.impl.cookie BasicClientCookie2)
           (java.net URL)
           (java.security.cert Certificate X509Certificate)
           ))

;; 导出clj-http的函数到当前命名空间
(pull clj-http.client (get post request))

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

(def default-ua "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.87 Safari/537.36")

(defn build-http-opt
  "构造http请求参数
  从配置中读取:default-http-option并加上指定的`custom-opt`构造http请求参数
  默认会加上配置指定的:user-agent头"
  ([] (build-http-opt nil))
  ([custom-opt]
   (deep-merge {:headers {"User-Agent" (or (get-config :user-agent)
                                           default-ua)
                          "Accept-Charset" "utf-8"}}
               (get-config :default-http-option)
               custom-opt)))

(defn get-cert-info
  [https-url]
  (-> (URL. https-url)
      (.openConnection)
      (doto (.connect))
      (.getServerCertificates)))

(defn random-ua
  "返回一个随机的user-agent"
  [uas-file-path]
  (when (fs/exists? uas-file-path)
    (with-open [rdr (io/reader uas-file-path)]
      (-> (line-seq rdr)
          rand-nth))))

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
