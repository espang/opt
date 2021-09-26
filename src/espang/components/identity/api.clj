(ns espang.components.identity.api
  (:require
   [com.brunobonacci.mulog :as u]
   [crypto.password.bcrypt :as password]
   [espang.components.identity.store :as db]))

(defn register [{:keys [conn] :as _ctx} {:keys [email password]}]
  (u/log ::register)
  (let [h (password/encrypt password)
        r (db/create-identity conn email h)]
    r))

(defn check [{:keys [conn] :as _ctx} {:keys [email password]}]
  (u/log ::check)
  (if-let [[id hpw verified] (first (db/get-identity conn email))]
    (if (password/check password hpw)
      {:id id :verified verified}
      :invalid-password)
    :unknown-identity))

(defn create-one-time-token [{:keys [conn] :as _ctx} {:keys [email]}]
  (u/log ::create-one-time-token)
  (db/password-reset-token conn email))

(defn new-password [{:keys [conn] :as _ctx} {:keys [email password token]}]
  (u/log ::new-password)
  (let [h (password/encrypt password)]
    (when (db/new-password conn email h token)
      true)))