(defproject ntestoc3/common "2.1.8-SNAPSHOT"
  :description "my common libs helper"
  :url "https://github.com/ntestoc3/common"
  :license {:name "MIT"
            :url "https://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [clj-http "3.12.3"]
                 [com.taoensso/timbre "6.1.0"]    ; logging
                 [me.raynes/fs "1.4.6"]            ;; file util
                 [net.lingala.zip4j/zip4j "2.11.5"] ;; zip with password
                 [clojure.java-time "1.2.0"]       ; datetime
                 [diehard "0.11.6"]                 ;; guard
                 [com.grammarly/omniconf "0.4.3"]  ;; config
                 [fipp "0.6.26"] ;; pretty print
                 [clojurewerkz/quartzite "2.1.0"] ;; cron

                 [cheshire "5.11.0"]
                 [org.clojure/spec.alpha "0.3.218"]]
  :profiles {:dev {:dependencies
                   [[midje "1.10.9" :exclusions [org.clojure/clojure]]]
                   :plugins [[lein-midje "3.2.1"]]}}
  :repl-options {:init-ns common.core})
