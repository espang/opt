(ns espang.components.context.context)

(defprotocol Context
  (done [] "returns a channel ...")
  (err [] "returns the error, can be called multiple times")
  (value [key] "")
  (deadline [] "returns instant when the context should be done, nil when no deadline is set"))

(defn with-value [ctx k v] ctx)