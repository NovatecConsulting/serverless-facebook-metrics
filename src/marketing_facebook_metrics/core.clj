(ns marketing-facebook-metrics.core
  (:require [lambada.core :refer [deflambdafn]]
            [clj-http.client :as client]
            [environ.core :refer [env]]
            [cheshire.core :as json]
            [java-time :as time]
            [clojure.java.io :as io]
            [clojure.pprint :refer [pprint]]))

(defn month->quarter
  "Converts a month (represented as the month number as a string) to its quarter (also a string)
  Example:
  (month->quarter \"3\") -> \"1\"
  (month->quarter \"7\") -> \"3\"
  "
  [month]
  (case month
    ("01" "02" "03") "1"
    ("04" "05" "06") "2"
    ("07" "08" "09") "3"
    ("10" "11" "12") "4"
    (throw (ex-info
            (str "cannot get quarter for month: " month)
            {}))))

(defn timestamp->quarter
  [post]
  (update post :created_time
          (fn [ts]
            (let [year-month (->> ts
                                  (take 7)
                                  (apply str))
                  [year month] (.split year-month "-")
                  quarter (month->quarter month)]
              (str year "-" quarter)))))

(defn parse-posts
  [{:keys [posts]}]
  (->> posts
       :data
       (mapv timestamp->quarter)
       (group-by :created_time)
       (into [])
       (mapv (fn [[quarter posts]] {:quarter quarter
                                    :posts (count posts)}))
       (sort-by first)))

(defn get-fb-posts
  "Calls Facebook and returns information about the item"
  [id token]
  (let [resp (-> (str "https://graph.facebook.com/v2.11/" id)
                 (client/get
                  {:query-params {"access_token" token
                                  "fields" "posts{created_time}"}
                   :debug false})
                 :body
                 (json/parse-string true))
        new-posts (parse-posts resp)]
    new-posts))

(defn handle-lambda
  [in out ctx]
  (let [{:keys [id token] :as input} (if in
                                       (json/parse-stream (io/reader in) true)
                                       nil)]
    (println "Request: " input)
    (let [token (or
                 (if (empty? token) nil token)
                 ;; empty string token is converted to nil, else the token from the environment won't be used
                 (:app-token env)
                 (throw (ex-info "Must pass a token as query-param or as environment variable")))
          id (or id "me")
          response (get-fb-posts id token)]
      (println "Response: " response)
      (json/generate-stream response (io/writer out))
      (json/generate-string response))))

(deflambdafn de.novatec.MarketingFacebookMetrics
  [in out ctx]
  (handle-lambda in out ctx))

