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

(defn handle-start [e]
 (.log js/console "start button")
 (set-port (alength js/anchors) 1))
(defn handle-stop [e]
 (.log js/console "stop button")
 (set-port (alength js/anchors) 0))

(defn render-buttons []
 (doto (CustomButton. "开始")
  (.render (dom/getElement "start-stop"))
  (events/listen #js [goog.ui.Component.EventType.ACTION] handle-start))
 (doto (CustomButton. "停止")
  (.render (dom/getElement "start-stop"))
  (events/listen #js [goog.ui.Component.EventType.ACTION] handle-stop)))
; (doto (CustomButton. "全部打开")
;  (.render (dom/getElement "start-reset"))
;  (events/listen #js [goog.ui.Component.EventType.ACTION] handle-openall)))

(declare main)

(defn click-enter [graph images]
 (fn [_] 
  (dorun (map #(.dispose %) images))
  (main graph)))

(defn click-about [_]
 (js/alert "no about now"))

(defn render-images [graph]
 [(.drawImage graph 0 0 1443 771 "static/entry_page.png")
  (.drawImage graph 630 548 184 109 "static/entry_icon.png")
  (.drawImage graph 1165 669 277 102 "static/about.png")])

(defn render-front-buttons [graph images]
 (events/listen (images 1) (.-CLICK events/EventType) (click-enter graph images))
 (events/listen (images 2) (.-CLICK events/EventType) click-about))

(defn front []
 (let [graph (graphics/createGraphics 1443 771)
       div-canvas (dom/getElement "canvas")
       images (render-images graph)]
  (render-front-buttons graph images)
  (.render graph div-canvas)))

(defn main [graph]
 (.drawImage graph 0 0 1440 770 "static/background.png")
 (render-switchs graph)
 (render-buttons))

(set! (.-onload js/window) front)
