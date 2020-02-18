(ns common.log-ext
  (:require [taoensso.timbre :as log]
            [again.core :as again]
            [taoensso.timbre.appenders.core :as appenders]))

(defmulti log-attempt ::again/status)
(defmethod log-attempt :retry [s]
  (swap! (::again/user-context s) assoc :retried? true)
  (log/warn "RETRY" s))
(defmethod log-attempt :success [s]
  (when (-> s ::again/user-context deref :retried?)
    (log/info "SUCCESS after" (::again/attempts s) "attempts" s)))
(defmethod log-attempt :failure [s]
  (log/error "FAILURE" s))

(def log-levels [:trace :debug :info :warn :error :fatal :report])

(defn log-time-format! []
  (log/merge-config!
   {:timestamp-opts
    {:pattern "yyyy/MM/dd HH:mm:ss"
     :locale (java.util.Locale/getDefault)
     :timezone (java.util.TimeZone/getDefault)}}))

(defonce __log-time (log-time-format!))

(defn make-log-appender
  "日志添加器
  `prn-fn` 接收日志字符串的函数"
  [prn-fn]
  {:enabled? true
   :async? true
   :min-level nil
   :rate-limit nil
   :output-fn :inherit
   :fn (fn [data]
         (let [{:keys [output_]} data
               formatted-output-str (-> (force output_)
                                        (str "\n"))]
           (prn-fn formatted-output-str)))})

(defn log-add-appender!
  "添加日志记录项"
  [appender]
  (log/merge-config!
   {:appenders appender}))

(defn log-to-fn!
  "配置log输出到函数回调
  `fn-key`为log appender的键"
  [fn-key prn-fn]
  (log-add-appender! {fn-key (make-log-appender prn-fn)}))

(defn log-to-file!
  "配置log输出文件"
  ([] (log-to-file! "logs.log"))
  ([file-name]
   (log-add-appender! {:spit (appenders/spit-appender {:fname file-name})})))

