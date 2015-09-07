(ns fedreg-search 
  (:import [goog.net Jsonp])
  (:require
      [google-vis :as gv]
      [fedreg-time :as ft]))

(def my-url "http://ajax.googleapis.com/ajax/services/feed/load?v=1.0&num=10&q=http://feeds.feedburner.com/bbc")

;; write the initial HTML
(.write js/document "<div id=\"data\">Data loading...<br>  Please wait.</div>")

;; elements we will set at runtime
(def data-element (.getElementById js/document "datatable"))
(def dates-element (.getElementById js/document "dates"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; interface with google visualization 
(defn build-chart[]
(gv/draw-chart
  [["string" "Title"] ["string" "Action"]
   ["string" "Agency"] ["string" "Docket ID"]
   ["date" "Comments Close"] ["date" "Publication Date"]]
  (clj->js 
    [[ "my title" "action" "EPA" "XXX-YYY-111" (new js/Date "07/11/14") (new js/Date "07/12/14")]
    [ "my title" "action" "EPA" "XXX-YYY-111" (new js/Date "07/11/14") (new js/Date "07/12/14")]])
  (clj->js {:width "100%"})
  (new js/google.visualization.Table data-element))
)


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ajax code 

(defn handler 
  "Handle the ajax response"
  [response]
  (let 
    [clj-resp (js->clj response {:kewordize-keys true})]
    (set! data-element.innerHTML (str 
        clj-resp 
        ))
    ;(.log js/console (str "Success:" clj-resp ))
    ))

(defn err-handler 
  "Handle the ajax errors"
  [response]
    (.log js/console (str "ERROR: " response)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; main

  (set! dates-element.innerHTML (str (ft/my-time-string ft/us-formatter 1) " to " (ft/my-time-string ft/us-formatter)))

(comment "for testing, skip the json" 
(.send (goog.net.Jsonp. my-url nil)
  "" handler err-handler nil)
)

(.load js/google "visualization" "1" (clj->js {:packages ["table"]})) ;macro or function
(.setOnLoadCallback js/google build-chart)
