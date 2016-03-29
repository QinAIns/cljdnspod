(ns cljdnspod.config)
(def config
  {:id          12345
   :token       "0123456789abcdef0123456789abcdef"
   :sub-domains [{:domain     "example.com"
                  :sub-domain "www"
                  :ttl        600}
                 {:domain     "example.com"
                  :sub-domain "m"
                  :ttl        600}]})
