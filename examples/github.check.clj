(checking "Github API"
	(with :base "https://api.github.com"
		($ "GET /users/blandinw" :assert #(-> % :json :login (= "blandinw")))
		($ "GET /nukes" :status 404)))
