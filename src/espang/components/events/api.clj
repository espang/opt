(ns espang.components.events.api
  (:require
   [clojure.core.async :as a]))

;; sign up
;; create-entry in database
;; send email
;;
;; receive email
;;
;; reset password
;; * don't invalidate current password
;; * create one time token with ttl
;; * send email with token
;; * send request with token and new password
;; * check



(defn publish [topic msg]
  true)

(defn subscribe [topic])

(defn enqueue [topic task])

(defn take-tasks [topic])


;; (pub ch topic-fn)
(a/pub )

(comment
  (def pub-channel (a/chan 1))
  (def publisher (a/pub pub-channel :tag))
  (def print-channel (a/chan 1))

  (defn run-print-channel
    []
    (a/go-loop []
      (when-let [value (a/<! print-channel)]
        (println value)
        (recur))))

  (defn subscribe [p s tags cb]
    (let [ch (a/chan 1)]
      (doseq [tag tags]
        (a/sub p tag ch))
      (a/go-loop []
        (when-let [v (a/<! ch)]
          (a/>! print-channel (cb s v))
          (recur)))))

  (defn callback [s v]
    (pr-str
     (format "%s got message: %s" s v)))

  (defn send [ch msg]
    (doseq [tag (:tags msg)]
      (println "sending..." tag)
      (a/>!! ch {:tag tag
                 :msg (:msg msg)})))

  (run-print-channel)

  (subscribe publisher "one" [:dogs] callback)
  (subscribe publisher "two" [:cats] callback)
  (subscribe publisher "three" [:dogs :cats] callback)

  (send pub-channel {:msg "New Pet Story" :tags [:cats :dogs]})


  )