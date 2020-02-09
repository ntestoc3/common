(defproject ntestoc3/common "1.2.1-SNAPSHOT"
  :description "my common libs helper"
  :url "https://github.com/ntestoc3/common"
  :license {:name "MIT"
            :url "https://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [clj-http "3.10.0"]
                 [com.taoensso/timbre "4.10.0"]    ; logging
                 [me.raynes/fs "1.4.6"] ;; file util
                 [net.lingala.zip4j/zip4j "2.3.1"] ;; zip with password
                 [clojure.java-time "0.3.2"]       ; datetime
                 [cprop/cprop "0.1.15"]            ;; env manage
                 ]
  :profiles {:dev {:dependencies [[midje "1.9.9" :exclusions [org.clojure/clojure]]
                                  ]
                   :plugins [[lein-midje "3.2.1"]]}}
  :repl-options {:init-ns common.core})
