(ns cljstest.server
  (:require [qbits.jet.server :refer [run-jetty]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [compojure.core :refer :all]
            [compojure.route :as route]
            ;[compojure.response :refer [render]]
            [clojure.java.io :as io])
  (:import  [jssc SerialPort SerialPortList])
  (:gen-class))

(defn anchors-str [] (str 
                     "var anchors = [" 
                     (reduce
                      str
                      (map #(format "[%d,%d]," (:x %) (:y %)) [{:x 10 :y 10} {:x 250 :y 250} {:x 390 :y 10} {:x 530 :y 230}]))
                     "];"))

(defonce serial-conn 
 (let [ports (. SerialPortList getPortNames)
       myport "/dev/tty.usbmodem1411"]
  (if (and (pos? (alength ports)) (contains? (set ports) myport))
;   (doto (SerialPort. "/dev/tty.usbserial-A50285BI")
   (doto (SerialPort. myport)
    (.openPort)
    (.setParams 9600 8 1 0)
    ((fn [_] (Thread/sleep 1000))))
   nil)))

(defn set-port [idx val]
 (.writeBytes serial-conn (byte-array [(byte \s) (byte idx) (byte val)]))
 (.readBytes serial-conn 1))

(defn query-port [idx] 
 (.writeBytes serial-conn (byte-array [(byte \g) (byte idx)]))
 (aget (.readBytes serial-conn 1) 0))

(defn close-conn []
 (.closePort serial-conn))

(defn query-states []
 (if serial-conn
  (doall (map query-port [0 1 2 3])) 
  (do (println "serial-conn is nil") [0 0 0 0])))

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

