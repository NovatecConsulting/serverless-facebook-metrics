(ns marketing-facebook-metrics.core
  (:require [uswitch.lambada.core :refer [deflambdafn]]
            [clj-http.client :as client]
            [environ.core :refer [env]]))

(def novatec-facebook-page-id
  "novatec.holding")

(defn get-fb-item
  "Calls Facebook and returns information about the item"
  [item-id]
  (client/get
   (str "https://graph.facebook.com/v2.11/" item-id)
   {:query-params {"access_token" (:user-token env)}
    :debug true}))

(defn handle-lambda
  [in out ctx]
  (println "Getting general data for the NovaTec Holding facebook page.")
  (let [page-data (try
                    (get-fb-item novatec-facebook-page-id)
                    (catch Exception e
                      (str "Error:\n"
                           (clojure.pprint/pprint
                            (ex-data e)))))]
    (spit out page-data)))

(deflambdafn de.novatec.MarketingFacebookMetrics
  [in out ctx]
  (handle-lambda in out ctx))
