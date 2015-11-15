(ns cljstest.server
  (:require [qbits.jet.server :refer [run-jetty]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [compojure.core :refer :all]
            [compojure.route :as route]
            ;[compojure.response :refer [render]]
            [clojure.java.io :as io])
  (:gen-class))

(defn anchors-str [] (str 
                     "var anchors = [" 
                     (reduce
                      str
                      (map #(format "[%d,%d]," (:x %) (:y %)) [{:x 10 :y 10} {:x 250 :y 250} {:x 390 :y 10} {:x 530 :y 230}]))
                     "];"))

(defn query-states [] [0 0 0 1])

(defn states-str [] (str
                     "var onoffs = ["
                     (reduce
                      str
                      (map #(format "%d," %) (query-states)))
                     "];"))

(defn replace-tag [tag]
 (if (= tag "<replace/>")
  (str "<script>" (anchors-str) (states-str) "</script>")
  tag))

(def front-page
 (let [templ (io/reader "resources/index.html")]
  (reduce str (map replace-tag (line-seq templ)))))

(defroutes app
 (GET "/" [] front-page)
 (route/resources "/static")
 (route/not-found "<h1>Page Not Found</h1>"))

(defn -main [& args]
 (let [app (wrap-defaults app site-defaults)]
  (run-jetty {:ring-handler app :port 3000})))

