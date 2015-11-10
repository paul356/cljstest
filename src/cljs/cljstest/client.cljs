(ns hello-clojurescript
  (:require [goog.events :as events]
            [goog.dom :as dom]))
           
(defn render-radios [] (.log js/console (str (js->clj js/anchors))))

(defn main []
 (render-radios))
(set! (.-onload js/window) main)
