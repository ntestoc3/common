(ns common.num)

(defn in-range-int
  "数字n在min-max范围内，如果超过则取相应的最大最小值"
  [n min max]
  (cond
    (<= n min) min
    (>= n max) max
    :else n))
