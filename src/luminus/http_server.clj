(ns luminus.http-server
  (:require [clojure.tools.logging :as log]
            [clojure.set :refer [rename-keys]]
            [qbits.jet.server :refer [run-jetty]]))

(defn start [{:keys [port] :as opts}]
  (try
    (let [server (run-jetty
                   (merge
                     {:join? false}
                     (rename-keys opts {:handler :ring-handler})))]
      (log/info "server started on port" port)
      server)
    (catch Throwable t
      (log/error t (str "server failed to start on port: " port)))))

(defn stop [server]
  (.stop server)
  (log/info "HTTP server stopped"))
