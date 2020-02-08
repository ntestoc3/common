(ns common.gui
  (:require [clojure.java.io :as io])
  (:import java.awt.Desktop))

(defn desktop-open
  "打开文件浏览器"
  [path]
  (let [f (io/file path)
        path (if (.isFile f)
               (.getParent f)
               f)]
    (when (Desktop/isDesktopSupported)
      (-> (Desktop/getDesktop)
          (.open (io/file path))))))
