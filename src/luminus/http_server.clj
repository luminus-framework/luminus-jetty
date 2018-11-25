(ns luminus.http-server
  (:require [clojure.tools.logging :as log]
            [clojure.set :refer [rename-keys]]
            [ring.adapter.jetty :refer [run-jetty]]))

(defn start [{:keys [handler port] :as opts}]
  (try
    (let [server (run-jetty
                   handler
                   (-> opts
                       (dissoc :handler)
                       (assoc :join? false)))]
      (log/info "server started on port" port)
      server)
    (catch Throwable t
      (log/error t (str "server failed to start on port: " port)))))

(defn stop [server]
  (.stop server)
  (log/info "HTTP server stopped"))
