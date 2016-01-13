(defproject cljstest "0.1.0-SNAPSHOT"
            :description "cljstest"
            :url "http://127.0.0.1/cljstest"
            :dependencies [[org.clojure/clojure "1.5.1"]
                           [org.clojure/clojurescript "0.0-2156"]
                           [ring/ring-defaults "0.1.2" :exclusions [javax.servlet/servlet-api]]
                           [cc.qbits/jet "0.7.0-beta1"]
                           [compojure "1.3.1"]
                           [org.scream3r/jssc "2.8.0"]]
            :plugins [[lein-cljsbuild "1.0.2"]
                      [lein-ring "0.8.10"]
                      [lein-figwheel "0.5.0-2"]]
            :source-paths ["src/clj"]
            :cljsbuild { 
                        :builds [
                                 {:id "dev"
                                  :source-paths ["src/cljs"]
                                  :figwheel true
                                  :compiler {:main cljstest.client
                                             :asset-path "js/out"
                                             :output-to "resources/public/js/cljs.js"
                                             :output-dir "resources/public/js/out"
                                             :optimizations :none
                                             :source-map "resources/public/js/cljs.js.map"}}
                                 {:id "min"
                                  :source-paths ["src/cljs"]
                                  :compiler {:main cljstest.client
                                             :output-to "resources/public/js/cljs.js"
                                             :optimizations :advanced
                                             :print-pretty false}}
                                 ]}
            :main cljstest.server
            :ring {:handler cljstest.server/main})

