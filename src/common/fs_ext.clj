(ns common.fs-ext
  (:require [clojure.java.io :as io]
            [me.raynes.fs :as fs]
            [taoensso.timbre :as log]
            [clojure.string :as str]
            [common.var :refer [pull-all]]))

(defn file-open
  "读取文件，如果找不到文件则读取资源文件"
  [file-path]
  (if (fs/file? file-path)
    (fs/file file-path)
    (some-> (io/resource file-path)
            .toURI
            io/file)))

(defn file-dir
  "获取文件的目录,如果是目录则返回目录本身"
  [file-path]
  (if (fs/directory? file-path)
    file-path
    (str (fs/parent file-path))))

(defn file-istream
  "获取文件的输入流"
  [file-path]
  (some-> (file-open file-path)
          io/input-stream))

(defn join-files!
  "连接多个文件到out-file"
  [out-file & files]
  (with-open [w (io/output-stream out-file)]
    (doseq [f files]
      (io/copy (io/input-stream f) w))))

(defn file-content-equal?
  "两个文件内容是否相同"
  [file1 file2]
  (= (slurp file1) (slurp file2)))

(defn extract-resource!
  "提取资源文件到当前目录"
  ([filename] (extract-resource! filename nil))
  ([filename overwrite]
   (let [o-file (io/file filename)]
     (when (or overwrite
               (not (.exists o-file)))
       (log/info :extract-refource filename)
       (when-let [res-file (io/resource filename)]
         (with-open [in (io/input-stream res-file)]
           (io/copy in o-file)))))))
