(ns luminus.http-server
  (:require [clojure.tools.logging :as log]
            [clojure.set :refer [rename-keys]]
            [qbits.jet.server :refer [run-jetty]]))

(defn start [{:keys [init port] :as opts}]
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
      (log/error t (str "server failed to start on port: " port)))))

(defn stop [http-server destroy]
  (destroy)
  (.stop http-server)
  (log/info "HTTP server stopped"))
