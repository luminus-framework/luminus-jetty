(ns luminus.http-server
  (:require [clojure.tools.logging :as log]
            [clojure.set :refer [rename-keys]]
            [qbits.jet.server :refer [run-jetty]]))

(defonce http-server (atom nil))

(defn start [{:keys [init port] :as opts}]
  (if @http-server
    (log/error "HTTP server is already running!")
    (try
      (init)
      (reset! http-server
              (run-jetty
               (merge
                {:join? false}
                (-> opts
                    (dissoc :init)
                    (rename-keys {:handler :ring-handler})))))
      (log/info "server started on port" port)
      (catch Throwable t
        (log/error t (str "server failed to start on port: " port))))))

(defn stop [destroy]
  (when @http-server
    (destroy)
    (.stop @http-server)
    (reset! http-server nil)
    (log/info "HTTP server stopped")))
