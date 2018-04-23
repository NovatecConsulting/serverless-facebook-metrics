(ns marketing-facebook-metrics.core
  (:require [uswitch.lambada.core :refer [deflambdafn]]))

(deflambdafn de.novatec.MarketingFacebookMetrics
  [in out ctx]
  (println "Hello, World!"))
