(ns luminus.http-server
  (:require [clojure.tools.logging :as log]
            [qbits.jet.server :refer [run-jetty]]))

(defonce http-server (atom nil))

(defn start [handler init port]
  (if @http-server
    (log/error "HTTP server is already running!")
    (do
      (init)
      (reset! http-server
          (run-jetty
            {:ring-handler app
             :port port
             :join? false}))
      (log/info "server started on port:" (:port @http-server)))))

(defn stop [destroy]
  (when @http-server
    (destroy)
    (.stop @http-server)
    (reset! http-server nil)
    (log/info "HTTP server stopped")))
