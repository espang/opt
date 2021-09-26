(ns espang.components.identity.api-test
  (:require
   [clojure.test :refer :all]
   [com.brunobonacci.mulog :as u]
   [espang.components.identity.api :as sut]
   [espang.components.identity.store :as db]))

(u/start-publisher! {:type :console
                     :pretty? true})

(def ctx (atom {}))

(defn wrap-test-database
  [f]
  (let [_ (reset! ctx (db/setup "testing" "identities-test"))]
    (f)
    (db/tear-down (@ctx :client) "identities-test")))

(use-fixtures :once wrap-test-database)

(deftest register-a-user
  (testing "that a user an be registered"
    (is (true? (sut/register @ctx
                             {:email    "test@gmail.com"
                              :password "secret"}))))
  (testing "that a user can only be registered once"
    (is (true? (sut/register @ctx
                             {:email    "conflict@gmail.com"
                              :password "secret"})))
    (is (false? (sut/register @ctx
                              {:email    "conflict@gmail.com"
                              :password "secret"})))))

(deftest check-a-user
  (testing "returns user when a user is checked with the same password"
    (let [user {:email "check1@gmail.com" :password "secret"}
          _    (sut/register @ctx user)
          resp (sut/check @ctx user)]
      (is (some? (resp :id)))
      (is (false? (resp :verified)))))
  (testing "returns :invalid-password when the password is wrong"
    (let [user {:email "check2@gmail.com" :password "secret"}
          _    (sut/register @ctx user)]
      (is (= :invalid-password (sut/check @ctx (assoc user :password "classified"))))))
  (testing "returns :unknown-identity when the email is unknown"
    (let [user {:email "check3@gmail.com" :password "secret"}
          _    (sut/register @ctx user)]
      (is (= :unknown-identity (sut/check @ctx (assoc user :email "unknown@gmail.com")))))))

(deftest reset-password-flow
  (testing "a user should be able to reset their password"
    (let [user  {:email "x" :password "y"}
          _     (sut/register @ctx user)
          token ((sut/create-one-time-token @ctx user) :token)
          resp  (sut/new-password @ctx (assoc user :token token :password "z"))]
      (is (true? resp))
      (is (= :invalid-password (sut/check @ctx user)))
      (is (some? (sut/check @ctx (assoc user :password "z")))))))
