(ns common.var)

(defmacro pull
  "导出ns下的符号到当前命名空间"
  [ns vlist]
  `(do ~@(for [i vlist]
           `(def ~i ~(symbol (str ns "/" i))))))

(defmacro pull-all
  "导出ns下的所有符号到当前命名空间"
  [ns]
  `(do ~@(for [i (map first (ns-publics ns))]
           (let [sym (symbol (str ns "/" i))
                 macro (-> sym
                           resolve
                           meta
                           :macro)]
             (if macro
               (prn "pull-all skip macro:" sym)
               `(def ~i ~sym))))))


