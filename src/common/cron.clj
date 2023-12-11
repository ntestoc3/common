(ns common.cron
  (:require [clojurewerkz.quartzite.scheduler :as qs]
            [clojurewerkz.quartzite.triggers :as t]
            [clojurewerkz.quartzite.jobs :refer [defjob] :as j]
            [clojurewerkz.quartzite.schedule.cron :as cron]
            [taoensso.timbre :as log]
            ))


(defn make-cron
  "构造定时任务
  schedule为crontab格式 \"0 0/5 * * *?\"表示每5分钟运行一次
                       \"0 5 * * *?\" 表示每个整点的05分运行
  参考: http://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/tutorial-lesson-06.html"
  ([job-class] (make-cron job-class nil))
  ([job-class {:keys [job-key trigger-key schedule]
               :or {job-key "cron.job.1"
                    trigger-key "cron.trigger.1"
                    schedule "0 0 0 0 0 ?"}}]
   (let [s (-> (qs/initialize) qs/start)
         job (j/build
              (j/of-type job-class)
              (j/with-identity (j/key job-key)))
         trigger (t/build
                  (t/with-identity (t/key trigger-key))
                  (t/start-now)
                  (t/with-schedule (cron/schedule (cron/cron-schedule schedule))))]
     (log/info :make-cron job-class :job-key job-key :trigger-key trigger-key :schedule schedule)
     (qs/schedule s job trigger)
     s)))

(defn shutdown
  "停止所有定时器"
  []
  (-> (qs/initialize)
      (qs/shutdown)))

(comment
  (defjob TmpJob
    [ctx]
    (prn "job run:" ctx)
    )

  )
