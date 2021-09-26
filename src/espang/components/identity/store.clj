(ns espang.components.identity.store
  (:require
   [datomic.client.api :as d]
   [espang.components.identity.schema :refer [schema]]))

(defn setup
  ([]
   (setup "production" "identities"))
  ([system-name db-name]
   (let [cfg    {:server-type :dev-local
                 :system system-name}
         client (d/client cfg)
         _      (d/create-database client {:db-name db-name})
         conn   (d/connect client {:db-name db-name})]
     (d/transact conn {:tx-data schema})
     {:client client
      :conn   conn})))

(defn tear-down [client db-name]
  (d/delete-database client {:db-name db-name}))

(defn get-identity [conn email]
  (d/q '[:find ?e ?pw ?email-verified
         :in $ ?email
         :where
         [?e :user/email ?email]
         [?e :user/password ?pw]
         [?e :user/email-verified ?email-verified]]
       (d/db conn) email))

(defn create-identity
  "Creates the identity if the email doesn't exists."
  ([conn email hashed-password]
   (create-identity conn email hashed-password false))
  ([conn email hashed-password verified]
   (try
     (d/transact conn
                 {:tx-data [{:user/email          email
                             :user/email-verified verified
                             :user/password       hashed-password}]})
     true
     (catch clojure.lang.ExceptionInfo t
       (if (= (:cognitect.anomalies/category (ex-data t))
              :cognitect.anomalies/conflict)
         false
         (throw t))))))

(defn password-reset-token
  "create a password reset token"
  [conn email]
  (let [token (.toString (java.util.UUID/randomUUID))
        expiry  (.plus (java.time.Instant/now)
                       2
                       java.time.temporal.ChronoUnit/HOURS)]
    (println "expiry: " (type expiry))
    (d/transact conn
                {:tx-data [{:db/id [:user/email email]
                            :user/token {:token/secret token
                                         :token/valid-until (java.util.Date/from expiry)}}]})
    {:token  token
     :expiry expiry}))

(defn new-password
  [conn email new-hashed-password token]
  {:pre [(not-empty token)]}
  (let [db (d/db conn)
        user-lookup [:user/email email]
        {cur-token :user/token
         password  :user/password} (d/pull db '[:user/token :user/password] user-lookup)
        {exp-token :token/secret
         expiry    :token/valid-until} cur-token
        now (java.time.Instant/now)]
    (if (and (= exp-token token)
             (.isBefore now (.toInstant expiry)))
      (try
        (d/transact conn {:tx-data [[:db/cas (cur-token :db/id)
                                     :token/secret exp-token ""]
                                    [:db/cas user-lookup
                                     :user/password password new-hashed-password]]})
        (catch clojure.lang.ExceptionInfo t
          (if (= (:cognitect.anomalies/category (ex-data t)) :cognitect/conflict)
            :token-already-used
            (throw t))))
      :invalid-token-or-expired)))
