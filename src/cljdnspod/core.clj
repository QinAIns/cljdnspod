(ns cljdnspod.core
  (:require [clj-http.lite.client :as client]
            [clojure.string :as str]
            [cljdnspod.config :refer :all]))

(defn ip
  "Get the machine IP"
  []
  (str/replace (:body (client/get "http://members.3322.org/dyndns/getip")) "\n" ""))

(defn update-dnspod-ip
  "Update the dnspod IP"
  ([login-token domain sub-domain value]
   (update-dnspod-ip login-token domain sub-domain value 600))
  ([login-token domain sub-domain value ttl]
   (let [domains (:body (client/post "https://dnsapi.cn/Domain.Info"
                                     {:headers     {"User-Agent" "cljdnspod/0.1.0(qinains@gmail.com)"}
                                      :form-params {:login_token login-token
                                                    :domain      domain
                                                    :format      "json"}}))
         domain-id (get (re-find #"\"id\":\"([0-9]+)\"" domains) 1)
         records (:body (client/post "https://dnsapi.cn/Record.List"
                                     {:headers     {"User-Agent" "cljdnspod/0.1.0(qinains@gmail.com)"}
                                      :form-params {:login_token login-token
                                                    :format      "json"
                                                    :domain_id   domain-id
                                                    :sub_domain  sub-domain}}))
         record-id (get (re-find #"\"id\":\"([0-9]+)\"" records) 1)
         record-type (get (re-find #"\"type\":\"([^\"]+)" records) 1)
         record-value (get (re-find #"\"value\":\"([0-9\.]+)" records) 1)]
     (if (not= value record-value)
       (client/post "https://dnsapi.cn/Record.Modify"
                    {:headers     {"User-Agent" "cljdnspod/0.1.0(qinains@gmail.com)"}
                     :form-params {:login_token login-token
                                   :format      "json"
                                   :domain_id   domain-id
                                   :record_id   record-id
                                   :sub_domain  sub-domain
                                   :record_type record-type
                                   :record_line "默认"
                                   :ttl         ttl
                                   :value       value}})))))

(defn -main [& args]
  (while 1
    (let [ip (ip)
          login-token (str (:id config) "," (:token config))
          sub-domains (:sub-domains config)]
      (doseq [sub-domain sub-domains]
        (update-dnspod-ip login-token (:domain sub-domain) (:sub-domain sub-domain) ip (:ttl sub-domain)))

      ;;Check once every 10 minutes
      (Thread/sleep (* 10 60 1000)))))