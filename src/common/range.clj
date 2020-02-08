(ns common.range
  (:require [clojure.string :as str]))

(defn range?
  "range-v是否为一个范围值"
  [range-v]
  (or (number? range-v)
      (set? range-v)
      (and (seqable? range-v)
           (= 2 (count range-v))
           (>= (second range-v)
               (first range-v)))))

(defn range-value
  "获取一个范围数字
  如果是数字n,则为固定的n
  如果是[x y] 则为x-y之间的随机数,包含x和y
  如果是#{a b ...} 集合，则为任意一个集合中的数字"
  [range-v]
  {:pre [(range? range-v)]}
  (cond
    (set? range-v)
    (rand-nth (vec range-v))

    (seqable? range-v)
    (let [[x y] range-v]
      (+ x
         (rand-int (inc (- y x)))))

    :else range-v))

(defn in-range?
  "数字i是否在范围值range-v中 "
  [i range-v]
  {:pre [(int? i)
         (range? range-v)]}
  (cond
    (set? range-v)
    (range-v i)

    (seqable? range-v)
    (let [[x y] range-v]
      (<= x i y))

    :else (= i range-v)))

(defn parse-range
  "解析范围数字,可以是单个数字，或者是x-y之间的范围,或者是a,b,c数字集合"
  [s]
  (let [s (str/trim s)]
    (if-let [grps (re-matches #"\s*(\d+)\s*-\s*(\d+)\s*" s)]
      [(Integer/parseInt (second grps))
       (Integer/parseInt (last grps))]
      (let [grps (str/split s #"\s*,\s*")]
        (if (> (count grps) 1)
          (-> (map #(Integer/parseInt %) grps)
              set)
          (-> (first grps)
              (Integer/parseInt)))))))

(defn range->str
  "范围值转换为字符串表示"
  [range-v]
  {:pre [(range? range-v)]}
  (cond
    (set? range-v)
    (str/join "," range-v)

    (seqable? range-v)
    (str/join "-" range-v)

    :else (str range-v)))

