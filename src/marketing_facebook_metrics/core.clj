(ns marketing-facebook-metrics.core
  (:require [uswitch.lambada.core :refer [deflambdafn]]
            [clj-http.client :as client]
            [environ.core :refer [env]]
            [cheshire.core :as json]
            [java-time :as time]
            [clojure.java.io :as io]
            [clojure.pprint :refer [pprint]])
  (:gen-class
   :name de.novatec.MarketingFacebookMetrics
   :methods [[handleRequest [String Object] String]]))

(defn parse-timestamp
  [post]
  (update post :created_time #(->> %
                                   (take 7)
                                   (apply str))))

(defn parse-posts
  [{:keys [posts]}]
  (->> posts
       :data
       (mapv parse-timestamp)
       (group-by :created_time)
       (into [])
       (mapv (fn [[ts posts]] [ts (count posts)]))
       (sort-by first)
       #_(mapv (fn [[ts posts]] [ts (mapv #(dissoc % :created_time) posts)]))))

(defn get-fb-posts
  "Calls Facebook and returns information about the item"
  []
  (let [resp (-> (str "https://graph.facebook.com/v2.11/" "me")
                 (client/get
                  {:query-params {"access_token" (:app-token env)
                                  "fields" "posts{created_time}"}
                   :debug false})
                 :body
                 (json/parse-string true))
        new-posts (parse-posts resp)]
    new-posts))

(defn handle-lambda
  [in #_out ctx]
  (let [input (if in
                (json/parse-stream (io/reader in))
                nil)]
    (println "Request:\n" (pprint input))
    (let [response (get-fb-posts)]
      (println "Response:\n" response)
      ;; (json/generate-stream response (io/writer out))
      (json/generate-string response))))

(defn -handleRequest
  [in ctx]
  (handle-lambda in ctx))

#_(macroexpand '(deflambdafn de.novatec.MarketingFacebookMetrics
   [in out ctx]
   (handle-lambda in out ctx)))
(do (clojure.core/gen-class :name de.novatec.MarketingFacebookMetrics :prefix G__20464 :implements [com.amazonaws.services.lambda.runtime.RequestStreamHandler]) (clojure.core/defn G__20464handleRequest [this in out ctx] (handle-lambda in out ctx)))

