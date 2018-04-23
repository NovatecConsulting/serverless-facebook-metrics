(ns marketing-facebook-metrics.core
  (:require [uswitch.lambada.core :refer [deflambdafn]]
            [clj-http.client :as client]))

(deflambdafn de.novatec.MarketingFacebookMetrics
  [in out ctx]
  (println "Hello, World!"))
