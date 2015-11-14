(ns hello-clojurescript
  (:require [goog.events :as events]
            [goog.dom :as dom]
            [goog.graphics :as graphics])
  (:import  [goog.ui CustomButton]))

(defn handle-click [idx img]
 (fn [evt] (.log js/console (str "-" idx))))

(defn render-radios [graph] 
 (let [fill (graphics/SolidFill. "red")
       stroke (graphics/Stroke. 5 "green")]
      (doseq [[[x y] idx] (map (js->clj js/anchors) (iterate inc 1))]
       (doto (.drawImage graph x y 30 stroke fill)
        #(events/listen % (.-CLICK events/EventType) (handle-click idx %))))))

(defn handle-start [e]
 (.log js/console "start button"))
(defn handle-reset [e]
 (.log js/console "reset button"))

(defn render-buttons []
 (doto (CustomButton. "开始")
  (.render (dom/getElement "start-reset"))
  (events/listen #js [goog.ui.Component.EventType.ACTION] handle-start))
 (doto (CustomButton. "重置")
  (.render (dom/getElement "start-reset"))
  (events/listen #js [goog.ui.Component.EventType.ACTION] handle-reset)))

(defn main []
 (let [graph (graphics/createGraphics "100%" "100%" 1000 600)
       div-canvas (dom/getElement "canvas")] 
  (render-radios graph)
  (.render graph div-canvas)
  (render-buttons)))

(set! (.-onload js/window) main)
