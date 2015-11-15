(ns cljstest.client
  (:require [goog.events :as events]
            [goog.dom :as dom]
            [goog.graphics :as graphics])
  (:import  [goog.ui CustomButton]))

(defn get-anchors [] (js->clj js/anchors))
(defn get-onoffs [idx] (aget js/onoffs idx))

(defn flip-onoffs [idx]
 (aset js/onoffs idx (bit-xor 1 (aget js/onoffs idx))))

(defn handle-click [graph x y idx img]
 (fn [evt] 
  (flip-onoffs idx)
  (.dispose img)
  (draw-image graph x y idx)
  (.log js/console (str "-" idx))))

(defn draw-image [graph x y idx]
 (let [image (if (pos? (get-onoffs idx))
              (.drawImage graph x y 66 64 "static/on.jpg")
              (.drawImage graph x y 66 64 "static/off.jpg"))]
      (events/listen image (.-CLICK events/EventType) (handle-click graph x y idx image))))

(defn render-radios [graph]
 (doseq [[[x y] idx] (map list (get-anchors) (iterate inc 0))]
  (draw-image graph x y idx)))

(defn handle-open [e]
 (.log js/console "open button"))
(defn handle-close [e]
 (.log js/console "close button"))
(defn handle-openall [e]
 (.log js/console "openall button"))

(defn render-buttons []
 (doto (CustomButton. "开始")
  (.render (dom/getElement "start-reset"))
  (events/listen #js [goog.ui.Component.EventType.ACTION] handle-open))
 (doto (CustomButton. "关闭")
  (.render (dom/getElement "start-reset"))
  (events/listen #js [goog.ui.Component.EventType.ACTION] handle-close))
 (doto (CustomButton. "全部打开")
  (.render (dom/getElement "start-reset"))
  (events/listen #js [goog.ui.Component.EventType.ACTION] handle-openall)))

(defn main []
 (let [graph (graphics/createGraphics "100%" "100%" 600 400)
       div-canvas (dom/getElement "canvas")] 
  (render-radios graph)
  (.render graph div-canvas)
  (render-buttons)))

(set! (.-onload js/window) main)
