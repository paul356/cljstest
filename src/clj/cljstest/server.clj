(ns cljstest.server
  (:require [qbits.jet.server :refer [run-jetty]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.util.response :refer [redirect content-type response]]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [clojure.java.io :as io]
            [clojure.string :as string])
  (:import  [jssc SerialPort SerialPortList])
  (:gen-class))

(def ^:dynamic *serial-conn*)

(defn set-port [idx val]
 (.writeBytes *serial-conn* (byte-array [(byte \s) (byte idx) (byte val)]))
 (aget (.readBytes *serial-conn* 1) 0))

(defn query-port [idx] 
 (.writeBytes *serial-conn* (byte-array [(byte \g) (byte idx)]))
 (aget (.readBytes *serial-conn* 1) 0))

(defn close-conn []
 (.closePort *serial-conn*))

(defonce num-sensor 6)

(def anchors-str (str 
                  "var anchors = [" 
                  (reduce
                   str
                   (map #(format "[%d,%d,%d]," (:x %) (:y %) (:group %)) 
                    [{:x 617 :y 120 :group 1} 
                     {:x 617 :y 180 :group 1} 
                     {:x 499 :y 340 :group 2} 
                     {:x 416 :y 400 :group 2} 
                     {:x 734 :y 340 :group 3} 
                     {:x 817 :y 400 :group 3}]))
                  "];"))

(def images-str (str "var images = [" (string/join "," (repeat num-sensor "null")) "];"))

(defn query-states []
 (if *serial-conn*
  (doall (map query-port (take num-sensor (iterate inc 0)))) 
  (do (println "serial-conn is nil") (repeat num-sensor 0))))

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

(defn open-port [myport]
  (alter-var-root #'*serial-conn*
                  (let [ports (. SerialPortList getPortNames)]
                    (if (and (pos? (alength ports)) (contains? (set ports) myport))
                      (doto (SerialPort. myport)
                        (.openPort)
                        (.setParams 9600 8 1 0)
                        ((fn [_] (Thread/sleep 1000))))
                      nil))))

(defn -main [& args]
  (if (> (count args) 0)
    (do
      (open-port (first args))
      (let [app (wrap-params (wrap-defaults app site-defaults))]
        (run-jetty {:ring-handler app :port 3000})))
    (println "please provide device name")))
