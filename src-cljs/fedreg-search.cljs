(ns fedreg-search 
  (:import [goog.net Jsonp])
  (:require
      [google-vis :as gv]
      [fedreg-time :as ft]))

(def my-url "https://www.federalregister.gov/api/v1/articles.json?per_page=500&order=relevance&fields%5B%5D=action&fields%5B%5D=agency_names&fields%5B%5D=dates&fields%5B%5D=docket_id&fields%5B%5D=publication_date&fields%5B%5D=title&fields%5B%5D=topics&fields%5B%5D=type&fields%5B%5D=comments_close_on&fields%5B%5D=html_url&conditions%5Bagencies%5D%5B%5D=environmental-protection-agency&conditions%5Bagencies%5D%5B%5D=nuclear-regulatory-commission&conditions%5Bagencies%5D%5B%5D=mine-safety-and-health-administration&conditions%5Bagencies%5D%5B%5D=federal-energy-regulatory-commission&conditions%5Bagencies%5D%5B%5D=engineers-corps&conditions%5Bagencies%5D%5B%5D=surface-mining-reclamation-and-enforcement-office&conditions%5Bagencies%5D%5B%5D=energy-department")

; Agencies to search for
(def agencies { "EPA"   "environmental-protection-agency"
                "NRC"   "nuclear-regulatory-commission"
                "MSHA"  "mine-safety-and-health-administration"
                "FERC"  "federal-energy-regulatory-commission"
                "Army Corps" "engineers-corps"
                "OSM"   "surface-mining-reclamation-and-enforcement-office"
                "DOE"   "energy-department"})
 
(defn build-url
  "combine agencies into the url"
  [url my-keys]
  (if (= 0 (count my-keys))
    url
    (do
      (let [agency (first my-keys)]
      (recur (str url "&conditions%5Bagencies%5D%5B%5D=" (agencies agency)) (rest my-keys))))))

;; write the initial HTML

;; elements we will set at runtime
(def data-element (.getElementById js/document "datatable"))
(def dates-element (.getElementById js/document "dates"))


(set! data-element.innerHTML "Data loading...<br>  Please wait.")

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

(defn filter-results
  [in-vector out-vector]
  (if (= 0 (count in-vector))
    out-vector
    (do
      (let 
        [my-map (first in-vector)]
        (recur 
          (rest in-vector)
          (conj out-vector
            (vector 
              (get my-map "title")
              (get my-map "action")
              (first (get my-map "agency_names"))
              (get my-map "docket_id")
              (get my-map "comments_close_on")
              (get my-map "publication_date"))))))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ajax code 

(defn handler 
  "Handle the ajax response"
  [response]
  (let 
    [clj-resp (js->clj response {:kewordize-keys true})]
    (set! data-element.innerHTML (str 
        (keys clj-resp) "<p>"
        (filter-results (get clj-resp "results") [])
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

(let
  [url (str my-url (build-url "" (keys agencies))
              (ft/one-week-ago))]

;(set! data-element.innerHTML url)

(.send (goog.net.Jsonp. url nil)
  "" handler err-handler nil)

(comment "for testing, skip the json and table-build" 
(.load js/google "visualization" "1" (clj->js {:packages ["table"]})) ;macro or function
(.setOnLoadCallback js/google build-chart)
)
)

