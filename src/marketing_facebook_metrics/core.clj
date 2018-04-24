(ns marketing-facebook-metrics.core
  (:require #_[uswitch.lambada.core :refer [deflambdafn]]
            [clj-http.client :as client]
            [environ.core :refer [env]]))

(def novatec-facebook-page-id
  "novatec.holding")

(defn get-fb-item
  "Calls Facebook and returns information about the item"
  [item-id]
  (client/get
   (str "https://graph.facebook.com/v2.11/" item-id)
   {:query-params {"access_token" (:app-token env)}}))

(defmacro deflambdafn
  "Create a named class that can be invoked as a AWS Lambda function."
  [name args & body]
  (assert (= (count args) 3) "lambda function must have exactly three args")
  (let [prefix (gensym)
        handleRequestMethod (symbol (str prefix "handleRequest"))]
    `(do
       (gen-class
        :name ~name
        :prefix ~prefix
        :implements [com.amazonaws.services.lambda.runtime.RequestStreamHandler])
       (defn ~handleRequestMethod
         ~(into ['this] args)
         ~@body))))

(deflambdafn de.novatec.MarketingFacebookMetrics
  [in out ctx]
  (println "Getting general data for the NovaTec Holding facebook page.")
  (let [page-data (try
                    (get-fb-item novatec-facebook-page-id)
                    (catch Exception e
                      (str "Error:\n"
                           (clojure.pprint/pprint
                            (ex-data e)))))]
    (spit out page-data)))
