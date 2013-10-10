(ns httpcheck.reader
  (:require [clojure.walk :as w]
            [cheshire.core :as j]
            [httpcheck.utils :as u]
            [httpcheck.types :refer [map->PathSpec]])
  (:import [httpcheck.types APISpec]))

(def ^:dynamic *config-dir*)
(def specs (atom #{}))

(defn $ [& args]
  (let [[url & {:as args}] (if (string? (first args)) args (cons nil args))
        {:keys [url] :as args} (if url (assoc args :url url) args)
        [_ meth path] (re-find #"(GET|POST|PUT|DELETE|OPTIONS|HEAD|PATCH)\s+(.*)"
                               url)]
    (when-not (and meth path)
      (throw (ex-info (format "could not find method and/or path url=%s"
                              url)
                      {:args args})))
    (map->PathSpec (-> args
                       (assoc :method meth :path path)
                       (update-in [:status] #(or % 200))
                       (update-in [:body] #(if (and % (not (string? %)))
                                             (j/encode %)
                                             %))))))

(defn $? [form]
  (boolean (when (seq? form)
             (= '$ (first form)))))

(defn find-$ [forms]
  (->> forms
       w/macroexpand-all
       (tree-seq coll? seq)
       (filter $?)))

(defn augment-with-bindings [[op & args] mappings]
  (let [[string & args] (if (string? (first args)) args (cons nil args))]
    (cons op (if string
               (cons string (concat (mapcat seq mappings) args))
               (concat (mapcat seq mappings) args)))))

(defmacro with [& args]
  (let [[{:as mappings} forms] (split-with (complement seq?) args)]
    (map #(augment-with-bindings % mappings) forms)))

(defmacro checking [name & forms]
  (let [$exprs (->> forms find-$ vec)]
    `(let [path-specs# ~$exprs]
       (swap! specs conj (APISpec. ~name path-specs#)))))

;; -----------------------------------------------------------------------------
;; Public API

(defn filepath->api-spec! [path]
  (try
    (binding [*ns* (find-ns 'httpcheck.reader)
              *config-dir* (u/dirname path)]
      (load-file path)
      @specs)
    (catch Exception e
      (println (format "Error while reading config file \"%s\": %s"
                       path (.getMessage e)))
      (.printStackTrace e)
      #_(System/exit 1))))