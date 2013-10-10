(ns httpcheck.utils)

(defn dirname [path]
  (let [[_ dir] (re-find #"^(.+/).*" path)]
    dir))
