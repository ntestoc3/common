(defproject ntestoc3/common "2.1.6"
  :description "my common libs helper"
  :url "https://github.com/ntestoc3/common"
  :license {:name "MIT"
            :url "https://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.10.2"]
                 [clj-http "3.12.1"]
                 [com.taoensso/timbre "5.1.2"]    ; logging
                 [me.raynes/fs "1.4.6"]            ;; file util
                 [net.lingala.zip4j/zip4j "2.7.0"] ;; zip with password
                 [clojure.java-time "0.3.2"]       ; datetime
                 [diehard "0.10.3"]                 ;; guard
                 [com.grammarly/omniconf "0.4.2"]  ;; config
                 [fipp "0.6.23"] ;; pretty print
                 ]
  :profiles {:dev {:dependencies
                   [[midje "1.9.9" :exclusions [org.clojure/clojure]]]
                   :plugins [[lein-midje "3.2.1"]]}}
  :repl-options {:init-ns common.core})
