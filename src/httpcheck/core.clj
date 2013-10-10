(ns httpcheck.core
  (:use [plumbing.core])
  (:require [clojure.test :as test]
            [clojure.string :as string]
            [clojure.pprint :as pprint]
            [clojure.java.io :as io]
            [clojure.core.match :refer [match]]
            [clojure.core.async :refer [<! >! <!! >!! put! take! close! go chan] :as a]
            [cheshire.core :as j]
            [org.httpkit.client :as client]
            [httpcheck.reader :as reader])
  (:import [java.io StringWriter])
  (:gen-class :main true))


;; -----------------------------------------------------------------------------
;; Output

(def dumb? (= "dumb" (System/getenv "TERM")))
(defmacro defcolor [name c]
  `(defn ~name [s#]
     (if dumb?
       s#
       (format "%s%s%s%s[0m" \u001b ~c s# \u001b))))

(defcolor red   "[31m")
(defcolor green "[32m")

(defn output-ok
  [{:keys [path-spec ok]}]
  (let [{:keys [method path query status]} path-spec
        method (if (= "DELETE" method) "DEL" method)]
    (-> (format "%s %s %4s %s"
                (green "OK") status method path)
        println)))

(defn output-ko
  "Takes a diagnostic object and output it"
  [{:keys [path-spec status ok assert]}]
  (let [{:keys [method path query] req-body :body} path-spec
        method (if (= "DELETE" method) "DEL" method)
        query (if-not (empty? query)
                (format "\n   - query: %s" (pr-str query))
                "")
        assert (if assert
                 (format "\n   - assert: expected=%s actual=%s body=%s"
                         (:expected assert) (:actual assert) (:body assert))
                 "")
        status (if status
                 (format "\n   - status: expected=%s actual=%s"
                         (:expected status) (:actual status))
                 "")
        req-body (if req-body
                   (format "\n   - request body:\n<<EOF\n%s\nEOF" req-body)
                   "")]
    (-> (format "%s %s %s%s%s%s%s"
                (red "KO") method path query assert status req-body)
        println)))

(defn report-path! [{:keys [ok] :as diag}]
  ((if ok output-ok output-ko) diag))

;; -----------------------------------------------------------------------------
;; Checkers

(defn check-assertion [f headers body]
  (let [json-body (if (re-find #"application/json" (get headers :content-type ""))
                    (j/decode body true)
                    body)
        test-writer (StringWriter.)
        ok? (or (not f)
                (try
                  (binding [test/*test-out* test-writer]
                    (f {:json json-body :body body :headers headers}))
                  (catch Exception e
                    e)))
        test-out (str test-writer)
        [_ expected actual] (when-not ok?
                              (re-find #"expected:\s+(.*)\s+actual:\s+(.*)" test-out))
        body-writer (StringWriter.)
        _ (pprint/pprint json-body body-writer)
        body (str body-writer)]
    [ok? expected actual body]))

(defn diagnose
  "Given a path-spec and a response, returns a diagnostic object."
  [{:keys [status assert] :as path-spec}
   {resp-status :status resp-body :body :keys [headers] :as resp}]
  (let [status-ok? (= status resp-status)
        [assert-ok? assert-exp assert-act assert-body] (check-assertion assert headers resp-body)
        ok? (and status-ok? assert-ok?)]
    (-> {:path-spec path-spec :ok ok?}
        (?> (not status-ok?) assoc :status {:expected status :actual resp-status})
        (?> (not assert-ok?) assoc :assert {:expected assert-exp
                                            :actual assert-act
                                            :body assert-body}))))

(defn check-path! [{:keys [base method path headers query status body] :as path-spec}]
  (let [out (chan)
        opts {:headers headers
              :query-params query
              :body body
              :as :text}
        f (match method
            "GET"     #'client/get
            "POST"    #'client/post
            "PUT"     #'client/put
            "PATCH"   #'client/patch
            "DELETE"  #'client/delete
            "HEAD"    #'client/head
            "OPTIONS" #'client/options)
        path (format "%s%s" base path)]
    (f path opts (fn [resp]
                   (put! out (diagnose path-spec resp))
                   (close! out)))
    out))

(defn check-api! [{:keys [paths name] :as api-spec}]
  (println (format "Checking %s (%d paths)" name (count paths)))
  (let [path-results (mapv check-path! paths)]
    (<!! (go (doseq [r path-results]
               (report-path! (<! r)))))))

;; -----------------------------------------------------------------------------
;; Public API

(defn check! [path]
  (->> path
       reader/filepath->api-spec!
       (map check-api!)
       dorun))

(defn -main [& [path]]
  (if-not path
    (println "usage: http-check path/to/config")
    (check! path)))
