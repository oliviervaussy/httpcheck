(ns httpcheck.reader-test
  (:use [clojure.test]
        [httpcheck.reader])
  (:require [clojure.walk :as walk]
            [httpcheck.reader :refer [with]]))

(deftest with-expansion-should-accept-function-calls
  (is (= '(($ :foo 1 :bar (inc 2) :quux 3))
         (macroexpand '(httpcheck.reader/with :foo 1 :bar (inc 2) ($ :quux 3))))))
