(def scope-msg {"authorization" "Bearer BACONPASTA"})

(checking "Public Wit API"
  (with :base "https://api.wit.ai" :headers scope-msg
    (with :url "GET /message"
      ($ :query {:q "dismiss me"} :status 200 :assert #(= "dismiss me" (-> % :json :msg_body)))
      ($ :status 400)
      ($ :headers {"authorization" "foo"} :query {:q "dismiss me"} :status 401))

    ($ "GET /wisps" :assert (fn [{:keys [json]}] (>= (count json) 15)))

    (with :url "GET /wisps/wit$datetime"
      ($ :assert #(every? identity ((juxt :id (complement :lookups)) (:json %))))
      ($ :query {:sudo true} :assert #((every-pred :id :lookups) (:json %)))
      ($ :headers {"authorization" "foo"} :status 401))

    (with :url "POST /wisps"
      ($ :status 401 :body {:id "wit$datetime"}))

    (with :url "PUT /wisps/wit$datetime" :body {:doc "my new doc"}
      ($ :status 401)
      ($ :query {:sudo true} :assert #(-> % :json :doc (= "my new doc"))))

    ($ "GET /wisps/foo$bar" :status 401)))
