(ns espang.components.identity.schema)

(def schema
  [{:db/ident       :user/email
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one
    :db/unique      :db.unique/value
    :db/doc         "email used by the user to log in"}
   {:db/ident       :user/email-verified
    :db/valueType   :db.type/boolean
    :db/cardinality :db.cardinality/one
    :db/doc         "true when the user has verified their email address"}
   {:db/ident       :user/password
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc         "hashed password of the user"}
   {:db/ident       :user/token
    :db/isComponent true
    :db/valueType   :db.type/ref
    :db/cardinality :db.cardinality/one
    :db/doc         "token to reset the password"}
   {:db/ident       :token/secret
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc         "secret of a token with a limited lifetime"}
   {:db/ident       :token/valid-until
    :db/valueType   :db.type/instant
    :db/cardinality :db.cardinality/one
    :db/doc         "token should only be accepted before that instant"}])
