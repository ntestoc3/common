(ns common.zip
  (:require [clojure.java.io :as io]
            [me.raynes.fs :as fs]
            [clojure.java.shell :refer [sh]])
  (:import net.lingala.zip4j.ZipFile
           net.lingala.zip4j.model.ZipParameters
           net.lingala.zip4j.model.enums.EncryptionMethod
           [net.lingala.zip4j.progress ProgressMonitor$State ProgressMonitor$Result]
           ))


(defn zip-file!
  "创建压缩文件
  `out-fname` output zip file name.
  `files` files add to zip file.
  `password` optional, zip password."
  ([out-fname files] (zip-file! out-fname files nil))
  ([out-fname files password]
   (let [params (doto (ZipParameters.)
                  (.setEncryptFiles (if password
                                      true
                                      false))
                  ;; 使用AES加密的话，长密码会出错误?
                  (.setEncryptionMethod EncryptionMethod/ZIP_STANDARD))
         files (map io/file files)]
     (-> (ZipFile. out-fname (char-array password))
         (.addFiles files params)))))

(defn unzip-file!
  "解压缩文件
  `zip-file` 要解压的zip文件
  `out-dir` 解压缩的目标文件夹
  "
  ([zip-file out-dir] (unzip-file! zip-file out-dir nil))
  ([zip-file out-dir password]
   (let [zip (if password
               (ZipFile. zip-file (char-array password))
               (ZipFile. zip-file))]
     (.extractAll zip out-dir))))

(defn unzip-cmd!
  "使用unzip命令解压文件"
  ([zip-file out-dir] (unzip-cmd! zip-file out-dir nil))
  ([zip-file out-dir password]
   (let [cmd (concat ["unzip" "-o" ]
                     (when password
                       ["-P" password])
                     [zip-file "-d" out-dir])]
     (fs/mkdirs out-dir)
     (-> (apply sh cmd)
         :out
         print))))


(comment
  (zip-file! "test.zip" ["maze.txt"] "123456")

  (zip-file! "test.zip" ["maze.txt"])

  (unzip-file! "test.zip"  "./maze_unzip")

  (unzip-cmd! "test.zip"  "./maze_unzip")
  )
