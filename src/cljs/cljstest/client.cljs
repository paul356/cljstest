(ns cljstest.client
  (:require [goog.events :as events]
            [goog.dom :as dom]
            [goog.graphics :as graphics])
  (:import  [goog.ui CustomButton]
            [goog.net XmlHttp]))

(declare draw-image)

(defn get-port [idx]
 (let [req (XmlHttp.)]
  (.open req "GET" (str "/port/get/" idx))
  (.send req)
  (.-responseText req)))

(defn set-port [idx val]
 (let [req (XmlHttp.)]
  (.open req "GET" (str "/port/set/" idx "?val=" val))
  (.send req)
  (.-statusText req)))

(defn get-anchors [] (js->clj js/anchors))

(defn get-onoffs [idx] (aget js/onoffs idx))
(defn flip-onoffs [idx]
 (.log js/console (str "flip switch-" idx))
 (aset js/onoffs idx (bit-xor 1 (aget js/onoffs idx)))
 (set-port idx (aget js/onoffs idx)))

(defn get-image [idx] (aget js/images idx))
(defn save-image [idx img] (aset js/images idx img))

(defn handle-click [graph x y idx anchor-group]
 (fn [evt] 
  (.log js/console (str "click swtich-" idx))
  (flip-onoffs idx)
  (.dispose (get-image idx))
  (draw-image graph x y idx anchor-group)
  (if (pos? (get-onoffs idx))
   (doseq [[x y _ index] anchor-group 
           :when (and
                  (not= idx index) 
                  (pos? (get-onoffs index)))]
    (flip-onoffs index)
    (.dispose (get-image index))
    (draw-image graph x y index anchor-group)))))

(defn draw-image [graph x y idx anchor-group]
 (let [image (if (pos? (get-onoffs idx))
              (.drawImage graph x y 66 64 "static/on.jpg")
              (.drawImage graph x y 66 64 "static/off.jpg"))]
  (save-image idx image)
  (events/listen image (.-CLICK events/EventType) (handle-click graph x y idx anchor-group))))

(defn render-switchs [graph]
 (let [anchors (map conj (get-anchors) (iterate inc 0))]
  (doseq [[x y group idx] anchors]
   (draw-image graph x y idx (filter #(= group (nth % 2)) anchors)))))

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
  (render-switchs graph)
  (.render graph div-canvas)
  (render-buttons)))

(set! (.-onload js/window) main)
