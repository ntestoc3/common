(defproject ntestoc3/common "2.1.10"
  :description "my common libs helper"
  :url "https://github.com/ntestoc3/common"
  :license {:name "MIT"
            :url "https://opensource.org/licenses/MIT"}
  :deploy-repositories [["clojars" {:url "https://clojars.org/repo"
                                    :username :env/clojars_user
                                    :password :env/clojars_pass
                                    :sign-releases false}]]
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [clj-http "3.12.3"]
                 [com.taoensso/timbre "6.5.0"]      ; logging
                 [me.raynes/fs "1.4.6"]             ;; file util
                 [net.lingala.zip4j/zip4j "2.11.5"] ;; zip with password
                 [clojure.java-time "1.4.2"]        ; datetime
                 [diehard "0.11.12"]                 ;; guard
                 [com.grammarly/omniconf "0.4.4-SNAPSHOT"]   ;; config
                 [fipp "0.6.26"]                    ;; pretty print
                 [clojurewerkz/quartzite "2.2.0"]   ;; cron

                 ;; validators
                 [commons-validator/commons-validator "1.8.0"]

                 [camel-snake-kebab/camel-snake-kebab "0.4.3"] ;; name convert

                 [metosin/malli "0.14.0"]

                 ;; repl test
                 [com.hyperfiddle/rcf "20220926-202227"]

                 [cheshire "5.12.0"]
                 [org.clojure/spec.alpha "0.4.233"]]
  :profiles {:dev {:dependencies
                   [[midje "2.0.0-SNAPSHOT" :exclusions [org.clojure/clojure]]]
                   :plugins [[lein-midje "3.2.1"]]}}
  :repl-options {:init-ns common.core})
