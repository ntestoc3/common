(ns common.config
  (:require [omniconf.core :as cfg]
            [me.raynes.fs :as fs]))

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
  (when-let [conf (cfg/get :conf)]
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

(defn set-config!
  "临时设置配置项，建议仅用于repl测试"
  [& args]
  (apply cfg/set args))
