(ns cljstest.client
  (:require [goog.events :as events]
            [goog.dom :as dom]
            [goog.graphics :as graphics]
            [ajax.core :refer [GET POST]]))

(declare draw-image)

(defn get-port [idx callback err-callback]
  (GET (str "/port/get/" idx) {:handler callback :error-handler err-callback}))

(defn set-port [idx value callback err-callback]
  (GET (str "/port/set/" idx) {:params {:val value} :handler callback :error-handler err-callback}))

(defn get-anchors [] (js->clj js/anchors))

(defn get-onoffs [idx] (aget js/onoffs idx))
(defn set-onoffs [idx state] (aset js/onoffs idx state))

(defn get-image [idx] (aget js/images idx))
(defn save-image [idx img] (aset js/images idx img))

(defn handle-click [graph x y idx anchor-group]
  (fn [evt] 
    (.log js/console (str "click swtich-" idx))
    (if (= 3 (get-onoffs idx))
      (js/alert "处理进行中...")
      (let [next-state  (if (= (aget js/onoffs idx) 2) 0 (+ 1 (aget js/onoffs idx)))
            old-state (get-onoffs idx)]
        (set-onoffs idx 3)
        (draw-image graph x y idx anchor-group)
        (set-port idx 
                  next-state 
                  (fn [resp]
                    (set-onoffs idx next-state)
                    (draw-image graph x y idx anchor-group))
                  (fn [{:keys [status]}] 
                    (js/alert "服务器出错啦")
                    (set-onoffs idx old-state)
                    (draw-image graph x y idx anchor-group)))))))

(defn draw-image [graph x y idx anchor-group]
 (let [state (get-onoffs idx)
       image (.drawImage graph x y 47 47 (str "sensor_state" state ".png"))]
  (when-let [old-img (get-image idx)] (.dispose old-img))
  (save-image idx image)
  (events/listen image (.-CLICK events/EventType) (handle-click graph x y idx anchor-group))))

(defn render-switchs [graph]
 (let [anchors (map conj (get-anchors) (iterate inc 0))]
  (doseq [[x y group idx] anchors]
   (draw-image graph x y idx (filter #(= group (nth % 2)) anchors)))))

(defn main []
  (let [graph (graphics/createGraphics 1282 802)
        div-canvas (dom/getElement "canvas")]
    (.render graph div-canvas)
    (.drawImage graph 0 0 1280 800 "sensor_page.png")
    (render-switchs graph)))

(set! (.-onload js/window) main)

;(defn handle-start [e]
; (.log js/console "start button")
; (set-port (alength js/anchors) 1))
;(defn handle-stop [e]
; (.log js/console "stop button")
; (set-port (alength js/anchors) 0))
;
;(defn render-buttons []
; (doto (CustomButton. "开始")
;  (.render (dom/getElement "start-stop"))
;  (events/listen #js [goog.ui.Component.EventType.ACTION] handle-start))
; (doto (CustomButton. "停止")
;  (.render (dom/getElement "start-stop"))
;  (events/listen #js [goog.ui.Component.EventType.ACTION] handle-stop)))
; (doto (CustomButton. "全部打开")
;  (.render (dom/getElement "start-reset"))
;  (events/listen #js [goog.ui.Component.EventType.ACTION] handle-openall)))

;(declare main)

;(defn click-enter [graph images]
; (fn [_] 
;  (dorun (map #(.dispose %) images))
;  (main graph)))
;
;(defn click-about [_]
; (js/alert "no about now"))
;
;(defn render-images [graph]
; [(.drawImage graph 0 0 1280 800 "entry_page.png")
;  (.drawImage graph 598 516 87 35 "entry_icon.png")
;  (.drawImage graph 1030 706 241 80 "about.png")])
;
;(defn render-front-buttons [graph images]
; (events/listen (images 1) (.-CLICK events/EventType) (click-enter graph images))
; (events/listen (images 2) (.-CLICK events/EventType) click-about))
;
;(defn front []
; (let [graph (graphics/createGraphics 1282 802)
;       div-canvas (dom/getElement "canvas")
;       images (render-images graph)]
;  (render-front-buttons graph images)
;  (.render graph div-canvas)))

