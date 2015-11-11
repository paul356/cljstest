(ns hello-clojurescript
  (:require [goog.events :as events]
            [goog.dom :as dom]
            [goog.graphics :as graphics]))
           
(defn render-radios [graph] 
 (let [fill (graphics/SolidFill. "red")
       stroke (graphics/Stroke. 2 "green")]
  (doseq [[x y] (js->clj js/anchors)] 
   (.drawCircle graph x y 5 fill stroke))))

(defn main []
 (let [graph (graphics/createGraphics "100%" "100%" 1000 600)
       div-canvas (dom/getElement "canvas")] 
  (render-radios graph)
  (.render graph div-canvas)))
(set! (.-onload js/window) main)
