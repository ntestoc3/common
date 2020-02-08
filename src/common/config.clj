(ns common.config
  (:require [cprop.core :refer [load-config]]
            [cprop.source :as source]
            [common.fs-ext :as fs]))

(def default-config "config.edn")
(fs/extract-resource! default-config)

(defn read-curr-config
  "读取当前配置文件内容"
  []
  (source/ignore-missing-default (fn []
                                   (source/from-file default-config))
                                 nil))
(def env
  (merge
   (source/from-env)
   (source/from-system-props)
   (read-curr-config)))

(def config (atom {}))

(defn- some-first
  [& exps]
  (if-some [r (first exps)]
    r
    (when (seq? exps)
      (recur (next exps)))))

;; 如果配置项值为nil,则会继续查找
(defn get-config
  "从全局配置和环境中 获取配置项k的值"
  ([k] (get-config k nil))
  ([k default] (some-first
                (get @config k)
                (get env k)
                default)))

(defn get-in-config
  "从全局配置和环境中 获取配置path的值"
  ([path] (get-in-config path nil))
  ([path default]
   (some-first
    (get-in @config path)
    (get-in env path)
    default)))

(defn set-config!
  "设置配置项"
  [& kvs]
  (apply swap! config assoc kvs))

(defn set-in-config!
  "设置配置项"
  [path v]
  (swap! config assoc-in path v))

(defn save-config!
  "保存配置"
  ([] (save-config! (or (:conf env) default-config)))
  ([file-name]
   (spit file-name (-> (merge (read-curr-config)
                              @config)
                       pr-str))))
