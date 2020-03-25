(ns common.config
  (:require [omniconf.core :as cfg]
            [common.core :refer [deep-merge]]
            [common.time]
            [fipp.edn :refer [pprint]]
            [clojure.edn :as edn]
            [me.raynes.fs :as fs]
            [clojure.java.io :as io]
            )
  (:import (java.io File PushbackReader)))

(defn get-config-path
  []
  (or (cfg/get :conf)
      (when (fs/exists? "config.edn")
        "config.edn")))

(defn init-config
  "初始化配置，
  :cli-args 命令行参数
  :cfg-scheme 配置定义"
  [{:keys [cli-args cfg-scheme] :as params
    :or {cli-args []}}]
  ;; define the configuration
  (cfg/define
    (merge {:conf {:type :file
                   :verifier omniconf.core/verify-file-exists
                   :description "APP configuration file path"}}
           cfg-scheme))
  ;; like- :some-option => SOME_OPTION
  (cfg/populate-from-env)
  ;; load properties to pick -Dconf for the config file
  (cfg/populate-from-properties)
  ;; Configuration file specified as
  ;; Environment variable CONF or JVM Opt -Dconf
  ;; or from config.edn
  (when-let [conf (get-config-path)]
    (cfg/populate-from-file conf))
  ;; like- :some-option => (java -Dsome-option=...)
  ;; reload JVM args to overwrite configuration file params
  (cfg/populate-from-properties)
  ;; like- :some-option => -some-option
  (cfg/populate-from-cmd cli-args)
  ;; Verify the configuration(cfg/verify :quit-on-error quit-on-error))
  (cfg/verify))

(defn get-config
  "获取ks指定的配置， 可以指定多个key，获取嵌套的配置"
  [& ks]
  (apply cfg/get ks))

(def ^:private --new-configs-- (atom {}))

(defn set-config!
  "临时设置配置项，建议仅用于repl测试"
  {:forms '([& ks value] [ks-vec value])}
  [& args]
  (let [k (if (sequential? (first args))
            (ffirst args)
            (first args))
        new-v (apply cfg/set args)]
    (swap! --new-configs-- assoc k (get new-v k))))

(defn save-config!
  ([] (save-config! (cfg/get :conf)))
  ([save-path]
   (let [old-conf  (when-let [conf-path (get-config-path)]
                     (with-open [r (-> conf-path
                                       io/reader
                                       PushbackReader.)]
                       (edn/read {:eof nil} r)))]
     (with-open [w (io/writer save-path)]
       (-> (deep-merge old-conf @--new-configs--)
           (pprint {:writer w
                    :width 0}))))))
