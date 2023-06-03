(ns common.openai
  (:require [common.http :as http]
            [common.config :as config]
            [clojure.spec.alpha :as s]
            [cheshire.core]
            [clojure.string :as str]
            [clojure.java.io :as io])
  (:import (org.apache.http.entity.mime HttpMultipartMode))
  )

;; https://platform.openai.com/docs/api-reference

(def api-host "https://api.openai.com")

(defn api-req
  ([path] (api-req path nil))
  ([path {:keys [method
                 query-params
                 api-key]
          :or {method :get}
          :as opts}]
   (-> (http/build-http-opt
        (merge
         {:url (str api-host path)
          :method method
          :headers
          {:authorization (str "Bearer " (or api-key
                                             (System/getenv "OPENAI_API_KEY")
                                             (config/get-config :openai-api-key)))}
          :as :json}
         (dissoc opts :api-key)))
       http/request
       :body)))

(defn get-models
  []
  (api-req "/v1/models"))

(defn model-id?
  [id]
  (defonce model-ids (->> (get-models)
                          :data
                          (map :id)
                          set))
  (model-ids (name id)))

;; 参数定义
(s/def ::api-key string?)
(s/def ::model model-id?)
(s/def ::role (s/or :string-role (s/and string? #{"system" "user" "assistant"})
                    :keyword-role (s/and keyword? #{:system :user :assistant})))
(s/def ::content string?)
(s/def ::name (s/nilable (s/and string? #(<= (count %) 64))))
(s/def ::temperature (s/nilable number?))
(s/def ::top_p (s/nilable number?))
(s/def ::n (s/nilable integer?))
(s/def ::stream (s/nilable boolean?))
(s/def ::stop (s/nilable (s/or :string string? :array (s/coll-of string?))))
(s/def ::max_tokens (s/nilable integer?))
(s/def ::presence_penalty (s/nilable number?))
(s/def ::frequency_penalty (s/nilable number?))
(s/def ::logit_bias (s/nilable map?))
(s/def ::user (s/nilable string?))

;; messages 定义
(s/def ::message (s/keys :req-un [::role ::content] :opt-un [::name]))
(s/def ::messages (s/coll-of ::message))

;; 请求体定义
(s/def ::request-body (s/keys :req-un [::model
                                       ::messages]
                              :opt-un [::temperature
                                       ::top_p
                                       ::n
                                       ::stream
                                       ::stop
                                       ::max_tokens
                                       ::presence_penalty
                                       ::frequency_penalty
                                       ::logit_bias
                                       ::user
                                       ::api-key]))

(defn chat
  [req-body]
  {:pre [(s/assert ::request-body req-body)]}
  (api-req "/v1/chat/completions"
           {:method :post
            :content-type :json
            :form-params req-body}))


(s/def :edit/model (s/and (s/or :string string? :keyword keyword?)
                          #(contains? #{"text-davinci-edit-001" "code-davinci-edit-001"} (name %))))

(s/def :edit/input string?)
(s/def :edit/instruction string?)
(s/def :edit/n (s/nilable (s/and integer? #(>= % 1))))
(s/def :edit/temperature (s/nilable (s/and number? #(and (<= 0 %) (<= % 2)))))
(s/def :edit/top_p (s/nilable (s/and number? #(and (<= 0 %) (<= % 1)))))


(s/def :edit/api-request (s/keys :req-un [:edit/model
                                          :edit/instruction]
                                 :opt [:edit/input
                                       :edit/n
                                       :edit/temperature
                                       :edit/top_p
                                       ::api-key]))

(defn edit
  [req-body]
  {:pre [(s/assert :edit/api-request req-body)]}
  (api-req "/v1/edits"
           {:method :post
            :content-type :json
            :form-params req-body}))

(s/def :image/prompt string?)
(s/def :image/n (s/nilable (s/and integer? #(and (>= % 1) (<= % 10)))))
(s/def :image/size (s/and (s/or :string string? :keyword keyword?)
                          #(contains? #{"256x256" "512x512" "1024x1024"} (name %))))
(s/def :image/response_format (s/and (s/or :string string? :keyword keyword?)
                                     #(contains? #{"url" "b64_json"} (name %))))
(s/def :image/user (s/nilable string?))

(s/def :image/api-request (s/keys :req-un
                                  [:image/prompt]
                                  :opt [:image/n
                                        :image/size
                                        :image/response_format
                                        :image/user
                                        ::api-key]))

(defn generate-image
  [request-body]
  {:pre [(s/assert :image/api-request-valid? request-body)]}
  (api-req "/v1/images/generations"
           {:form-params request-body
            :method :post
            :content-type :json
            }))

(s/def :transcription/file (s/and string? #(re-matches #"\.(mp3|mp4|mpeg|mpga|m4a|wav|webm)$" %)))
(s/def :transcription/model (s/and (s/or :string string? :keyword keyword?)
                                    #(= "whisper-1" (name %))))
(s/def :transcription/prompt (s/nilable string?))
(s/def :transcription/response_format (s/and (s/or :string string? :keyword keyword?)
                                             #(contains? #{"json" "text" "srt" "verbose_json" "vtt"} (name %))))
(s/def :transcription/temperature (s/nilable (s/and number? #(and (>= % 0) (<= % 1)))))
(s/def :transcription/language (s/nilable string?))

(s/def :transcription/api-request (s/keys :req-un
                                          [:transcription/file
                                           :transcription/model]
                                          :opt [:transcription/prompt
                                                :transcription/response_format
                                                :transcription/temperature
                                                :transcription/language
                                                ::api-key]))

(defn ->val
  [v]
  (if (keyword v)
    (name v)
    v))

(defn transcribe-audio
  [{:keys [file model prompt response_format temperature language]
    :as request-body}]
  {:pre [(s/assert :transcription/api-request request-body)]}
  (api-req "/v1/audio/transcriptions"
           {:multipart (-> request-body
                           (dissoc :file)
                           (->> (map (fn [[k v]]
                                       {:name (->val k)
                                        :content (->val v)})))
                           (conj {:name "file" :content (io/file file)}))
            :multipart-mode HttpMultipartMode/BROWSER_COMPATIBLE
            ;; :multipart-charset "UTF-8"
            :method :post
            }))
(comment

  (def models (get-models))

  ;; 初始化配置,使用配置设置key
  (config/init-config {:cfg-scheme
                       {:openai-api-key {:type :string
                                         :description "openai api key"}
                        :openai-org {:type :string
                                     :description "openai organization"}}})

  (config/get-config :openai-api-key)

  ;; 打开参数验证
  (s/check-asserts true)

  (chat {:model :gpt-3.5-turbo
         :messages [{:role :user
                     :content "世上最美的人是谁?"}
                    {:role "assistant", :content "作为人工智能，我没有权力和能力给出主观判断和评价。每个人都有自己独特的美丽和价值。"}
                    {:role :user
                     :content "相对客观的说，你觉得什么是美"}
                    ]
         })

  (generate-image {:prompt "一个橘黄色的猫咪，穿着黑色的皮夹克,坐在一辆哈雷摩托车的座驾上，迎面驾驶过来"
                   :n 2
                   :size :512x512
                   :response_format :url})

  (transcribe-audio {:file "/tmp/musicclass.m4a"
                     :model :whisper-1
                     })



  (def prod-review "The total ecommerce turnover in Italy is estimated to have been worth 75.89 billion euros in 2022. This is a growth of 18.6 percent, when compared to a year earlier. However, most of that growth was caused by price increases.

These data come from the Ecommerce in Italy 2023 report from Casaleggio Associati, a digital strategy consulting business in Italy. In 2021, the ecommerce market in Italy reached a turnover of 64 billion euros, which was a growth of 33 percent compared to a year earlier. With a growth of 18.6 percent, it seems that the development of online sales is slowing down in the country.
")

  (def prod-review "7月14日下单，显示预计7月18日送达，货物物流位置看不懂！
7月18日上午950616来电话 自称客服工号814412 说我的收货地址送不到，建议我重新下单。随后骂完你们的在线客服，货物立即开始配送。据在线客服解释：大家电是沿途配送的，这可以理解。但是我的货物从上午开始派送，“沿途”了几乎全城到晚上一遍一遍的投诉你们的才勉强送到。其他已预约好安装师傅18号当天上门安装，实际货到后联系说需要给我排队到21号。又骂完在线客服后，第二天中午才上门安装 垃圾！")

  (chat {:model :gpt-3.5-turbo
         :temperature 0 ;; 总结类的temp使用0，结果更准确
         :messages [{:role :user
                     :content (format "Your task is to generate a short summary of a product review from an ecommerce site.

Summarize the review below, delimited by triple backtricks, in at most 30 words, and use chinese language.

Review: ```%s```
" prod-review)}
                    ]})

  (chat {:model :gpt-3.5-turbo
         :temperature 0 ;; 总结类的temp使用0，结果更准确
         :messages [{:role :user
                     :content (format "你的任务是对电商网站上的产品评论生成一个简短的描述，并给定价部门进行反馈，用来调整产品价格。

生成下面的使用```分割的评论的简短描述，回答尽可能的简短，关注价格和性价比，以及评论者的情绪，使用json格式，包含desc,feedback,user_felling

评论: ```%s```
" prod-review)}
                    ]})


  )
