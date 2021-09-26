(ns espang.components.email.api
  (:import
   [com.sendgrid Email Content Mail SendGrid Request Method]))

(def api-key (System/getenv "SENDGRID_API_KEY"))

(defn send [email]
  (let [from    (Email. "eike.spang@gmail.com")
        to      (Email. email)
        content (Content. "text/plain"
                          "This is the email body")
        mail    (Mail. from "subject" to content)
        sg      (SendGrid. api-key)
        request (new Request)]
    (try
      (.setMethod request Method/POST)
      (.setEndpoint request "mail/send")
      (.setBody request (.build mail))
      (let [response (.api sg request)]
        (println (.getStatusCode response))
        (println (.getBody response))
        (println (.getHeaders response)))
      (catch Exception e
        (println e)))))

(comment
  (send "receiver@email.com"))