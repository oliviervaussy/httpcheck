(defproject http-check "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :plugins []
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/core.async "0.1.242.0-44b1e3-alpha"]
                 [org.clojure/core.match "0.2.0"]
                 [cheshire "5.2.0"]
                 [http-kit "2.1.10"]
                 [prismatic/plumbing "0.1.0"]]
  :profiles {:dev {:dependencies [[org.clojure/tools.trace "0.7.6"]]}}
  :omit-source true
  :javac-options ["-target" "1.6"]
  :main httpcheck.core
  :core.typed {:check [http-check.core]})
