(defproject marketing-facebook-metrics "0.1.0-SNAPSHOT"
  :description "AWS Lambda function that gets statistics about Facebook activity."
  :url "https://github.com/nt-ca-aqe/marketing-facebook-metrics"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [lambada "1.0.3"]
                 [clj-http "3.8.0"]
                 [environ "1.1.0"]
                 [cheshire "5.8.0"]
                 [clojure.java-time "0.3.2"]]
  :profiles {:uberjar {:aot :all}
             :dev [:dev-env]}
  :plugins [[lein-environ "1.1.0"]]
  :uberjar-name "marketing-facebook-metrics.jar")
