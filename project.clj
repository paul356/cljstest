(defproject cljstest "0.1.0-SNAPSHOT"
            :description "cljstest"
            :url "http://127.0.0.1/cljstest"
            :dependencies [[org.clojure/clojure "1.7.0"]
                           [org.clojure/clojurescript "1.7.170"]
                           [ring/ring-defaults "0.1.5" :exclusions [javax.servlet/servlet-api]]
                           [cc.qbits/jet "0.7.1"]
                           [compojure "1.4.0"]]
            :plugins [[lein-cljsbuild "1.1.2"]
                      [lein-ring "0.9.7"]
                      [lein-figwheel "0.5.0-2"]]
            :source-paths ["src/clj"]
            :cljsbuild {
                        :builds [{:id "main-build"
                                  :source-paths ["src/cljs"]
                                  :figwheel true
                                  :compiler {
                                             :main "cljstest.client"
                                             :asset-path "js/out"
                                             :output-to "resources/public/js/cljs.js"
                                             :output-dir "resources/public/js/out"
                                            }
                                 }]
                       }
            :main cljstest.server
            :ring {:handler cljstest.server/main})
