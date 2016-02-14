(ns cljstest.server
  (:require [qbits.jet.server :refer [run-jetty]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.util.response :refer [redirect content-type response]]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [clojure.java.io :as io])
  (:import  [jssc SerialPort SerialPortList])
  (:gen-class))

(defonce serial-conn 
 (let [ports (. SerialPortList getPortNames)
       myport "/dev/tty.usbmodem1421"]
  (if (and (pos? (alength ports)) (contains? (set ports) myport))
;   (doto (SerialPort. "/dev/tty.usbserial-A50285BI")
   (doto (SerialPort. myport)
    (.openPort)
    (.setParams 9600 8 1 0)
    ((fn [_] (Thread/sleep 1000))))
   nil)))

(defn set-port [idx val]
 (.writeBytes serial-conn (byte-array [(byte \s) (byte idx) (byte val)]))
 (aget (.readBytes serial-conn 1) 0))

(defn query-port [idx] 
 (.writeBytes serial-conn (byte-array [(byte \g) (byte idx)]))
 (aget (.readBytes serial-conn 1) 0))

(defn close-conn []
 (.closePort serial-conn))

(def anchors-str (str 
                  "var anchors = [" 
                  (reduce
                   str
                   (map #(format "[%d,%d,%d]," (:x %) (:y %) (:group %)) 
                    [{:x 10  :y 10  :group 1} 
                     {:x 250 :y 250 :group 1} 
                     {:x 390 :y 10  :group 2} 
                     {:x 530 :y 230 :group 2}]))
                  "];"))

(def images-str "var images = [null, null, null, null];")

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

(defn get-data []
 (str anchors-str (states-str) images-str))

;(defn get-front-page []
; (reduce str (map replace-tag front-page-templ)))

(defroutes app
 (GET "/" [] (redirect "/index.html"))
 (GET "/js/data.js" [] (content-type (response (get-data)) "application/javascript"))
 (GET "/port/set/:index" [index :as req] 
  (let [params (:query-params req)]
   (if (contains? params "val")
    (str (set-port 
      (Integer. index)
      (Integer. (get params "val"))))
    "need ?val=1/0")))
 (GET "/port/get/:index" [index] 
  (str (query-port (Integer. index))))
 (route/resources "/")
 (route/not-found "<h1>Page Not Found</h1>"))

(defn -main [& args]
 (let [app (wrap-params (wrap-defaults app site-defaults))]
  (run-jetty {:ring-handler app :port 3000})))
