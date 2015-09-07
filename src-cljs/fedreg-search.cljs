(ns fedreg-search 
  (:import [goog.net Jsonp])
  (:require
      [cljs-time.core :as t]
      [cljs-time.local :as l]
      [cljs-time.format :as f]))

(def my-url "http://ajax.googleapis.com/ajax/services/feed/load?v=1.0&num=10&q=http://feeds.feedburner.com/bbc")

;; write the initial HTML
(.write js/document "<div id=\"data\">Data loading...<br>  Please wait.</div>")

;; elements we will set at runtime
(def data-element (.getElementById js/document "datatable"))
(def dates-element (.getElementById js/document "dates"))

;; code to format time strings
(def federal-formatter (f/formatter "yyyy-MM-dd"))
(def us-formatter (f/formatter "MM-dd-yyyy"))

(defn my-time-string
  "output formated 'now' or 'now minus x weeks'"
  ([my-format]
    (f/unparse my-format (l/local-now)))
  ([my-format minus-weeks]
    (f/unparse my-format (t/minus (l/local-now) (t/weeks minus-weeks)))))


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

  (set! dates-element.innerHTML (str (my-time-string us-formatter 1) " to " (my-time-string us-formatter)))

(comment "for testing, skip the json" 
(.send (goog.net.Jsonp. my-url nil)
  "" handler err-handler nil)
)

(defn draw-chart
" from:  https://github.com/fraburnham/google-charts"
  [columns vectors options chart
                  &{:keys [tooltip]
                    :or {tooltip false}}]
  (let [data (new js/google.visualization.DataTable)]
    (doall ;gotta keep the doall on maps. lazy sequence...
     (map (fn [[type name]]
            (.addColumn data type name)) columns))
    (if tooltip
      (.addColumn data (clj->js {:type "string" :role "tooltip"})))
    (.addRows data vectors)
    (.draw chart data options)))

(defn draw-demo-chart []
(draw-chart
  [["string" "Title"] ["string" "Action"]
   ["string" "Agency"] ["string" "Docket ID"]
   ["date" "Comments Close"] ["date" "Publication Date"]]
  (clj->js 
    [[ "my title" "action" "EPA" "XXX-YYY-111" (new js/Date "07/11/14") (new js/Date "07/12/14")]
    [ "my title" "action" "EPA" "XXX-YYY-111" (new js/Date "07/11/14") (new js/Date "07/12/14")]])
  (clj->js {:width "100%"})
  (new js/google.visualization.Table data-element))
)

(.load js/google "visualization" "1" (clj->js {:packages ["table"]})) ;macro or function
(.setOnLoadCallback js/google draw-demo-chart)
