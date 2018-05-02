(ns marketing-facebook-metrics.core
  (:require #_[lambada.core :refer [deflambdafn]]
            [clj-http.client :as client]
            [environ.core :refer [env]]
            [cheshire.core :as json]
            [java-time :as time]
            [clojure.java.io :as io]
            [clojure.pprint :refer [pprint]]))

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

(defmacro deflambdafn
  "Create a named class that can be invoked as a AWS Lambda function.
  `class-name` must be a symbol defining a fully qualified class name.
  `fun-name` must be a symbol which will be the name of the defined function.
  `args` must be a vector of 2 arguments.
  `body` is the body that will be executed by your lambda function.

  Arguments:
  The 2 arguments are
  1. The input argument to your lambda function
  2. The context of your lambda function invocation, supplied by the AWS infrastructure. This is a `com.amazonaws.services.lambda.runtime.Context`.

  The return value of your handler function will be used as the response.

  Example:
  (deflambdafn com.mycompany.somedomain.MyFunctionHandler handleRequest
    [in ctx]
    (println \"Request:\" in)
    (handle-request in ctx))

  "
  [class-name fun-name args & body]
  (assert (symbol? class-name) "Lambda handler class name must be a symbol")
  (assert (symbol? fun-name) "Lambda handler function name must be a symbol")
  (assert (and (vector? args)
               (= 2 (count args)))
          "Lambda handler args must be a vector of 2 elements")
  (let [method-name (symbol (str "-" fun-name))]
    `(do
       (gen-class
        :name ~class-name
        :methods [[~fun-name [String Context] String]])
       (defn ~fun-name ~args ~@body)
       (defn ~method-name
         ~(into ['this] args)
         (~fun-name ~@args)))))

(deflambdafn de.novatec.MarketingFacebookMetrics handleRequest
   [in ctx]
   (handle-lambda in ctx))

