(ns luminus.http-server
  (:require 
   [luminus.ws :as ws]
   [clojure.tools.logging :as log]
   [ring.adapter.jetty :refer [run-jetty]])
  (:import
   [org.eclipse.jetty.server Handler]
   [org.eclipse.jetty.server.handler ContextHandler HandlerList]))

(defn update-handlers [server handlers]
  (let [http-handler (.getHandler server)]
    (.setHandler 
     server 
     (doto (HandlerList.) 
       (.setHandlers (into-array Handler (conj (vec handlers) http-handler)))))))

(defn ws-configurator [configurator ws-handlers]
  (fn [server]
    (update-handlers server (map ws/ws-handler ws-handlers))
    (when configurator
      (configurator server))))

(defn handle-ws-opts [{:keys [ws-handler ws-handlers] :as opts}]
  (cond-> opts
    ws-handler (update :configurator ws-configurator [ws-handler])
    ws-handlers (update :configurator ws-configurator ws-handlers)))

(defn start [{:keys [handler port] :as opts}]
  (try
    (let [server (run-jetty
                   handler
                   (-> opts
                       (handle-ws-opts)
                       (dissoc :handler)
                       (assoc :join? false)))]
      (log/info "server started on port" port)
      server)
    (catch Throwable t
      (log/error t (str "server failed to start on port: " port)))))

(defn stop [server]
  (.stop server)
  (log/info "HTTP server stopped"))
