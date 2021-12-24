(ns luminus.ws
  (:require [ring.adapter.jetty9.websocket :as jetty-ws]))

(def send! jetty-ws/send!)
(def ping! jetty-ws/ping!)
(def close! jetty-ws/close!)
(def remote-addr jetty-ws/remote-addr)
(def idle-timeout! jetty-ws/idle-timeout!)
(def connected? jetty-ws/connected?)
(def req-of jetty-ws/req-of)
(def ws-upgrade-request? jetty-ws/ws-upgrade-request?)
(def ws-upgrade-response jetty-ws/ws-upgrade-response)
