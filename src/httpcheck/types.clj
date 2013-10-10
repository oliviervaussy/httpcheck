(ns httpcheck.types)

;; -----------------------------------------------------------------------------
;; Records

(defrecord APISpec  [name paths])
(defrecord PathSpec [base method path status assert headers query body])
