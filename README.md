# luminus-jetty

[![Clojars Project](https://img.shields.io/clojars/v/luminus-jetty.svg)](https://clojars.org/luminus-jetty)

Jetty HTTP adapter for Luminus

### HTTP handler

```clojure
(ns myapp.core
  (:require
   [luminus.ws :as ws]
   [luminus.http-server :as http]))

(defn http-handler [request]
  {:status 200
   :headers {"Content-Type" "text/plain"}
   :body (:remote-addr request)})

(http/start
 {:handler http-handler
  :port 3000})
```

### WS handler

```clojure
(ns myapp.core
  (:require
   [luminus.ws :as ws]
   [luminus.http-server :as http]
   [clojure.tools.logging :as log]))

;; a handler can be specified using a map
(def ws-handler-a
  {:on-connect           (fn [ws]
                           (log/info "WS connect" ws))
   :on-error             (fn [ws e]
                           (log/info "WS error" e))
   :on-text              (fn [ws text]
                           (log/info "text:" text)
                           (ws/send! ws text))
   :on-close             (fn [ws status-code reason]
                           (log/info "WS close" reason))
   :on-bytes             (fn [ws bytes offset len]
                           (log/info "WS bytes" bytes))})

;; alternatively you can provide a function that accepts a
;; Ring request map to initialize the websocket connection
;;
;; websocket upgrade headers like subprotocol and extensions
;; are available from the `req` via `(:websocket-subprotocols req)`
;; and `(:websocket-extensions req)`.

(def ws-handler-b
  (fn [req]
    {:on-connect (fn [& args]
                   (log/info "WS connect" args))
     :on-error   (fn [& args]
                   (log/info "WS error" args))
     :on-text    (fn [ws text]
                   (log/info "text:" text)
                   (ws/send! ws text))
     :on-close   (fn [& args]
                   (log/info "WS close" args))
     :on-bytes   (fn [& args]
                   (log/info "WS bytes" args))})})

(defn web-handler [request]
  (if (ws/ws-upgrade-request? request)
    ;; websocket upgrade request
    (ws/ws-upgrade-response ws-handler-a)
    ;; normal http request
    {:status 200
     :headers {"Content-Type" "text/plain"}
     :body (:remote-addr request)}))

;;create a single WS handler
(http/start
 {:handler web-handler
  :port 3000})
```

WebSocketProtocol allows you to read and write data on the `ws` value:

* (send! ws msg)
* (send! ws msg callback)
* (close! ws)
* (remote-addr ws)
* (idle-timeout! ws timeout)

Notice that we support different type of msg:

* **byte[]** and **ByteBuffer**: send binary websocket message
* **String** and other Object: send text websocket message
* **(fn [ws])** (clojure function): Custom function you can operate on
  Jetty's [RemoteEndpoint](http://download.eclipse.org/jetty/stable-9/apidocs/org/eclipse/jetty/websocket/api/RemoteEndpoint.html)

A callback can also be specified for `send!`:

```clojure
(send! ws msg {:write-failed (fn [throwable]) :write-success (fn [])})
```

A callback is a map where keys `:write-failed` and `:write-success` are optional.

### Js client example

```javascript
var websocket = new WebSocket("ws://localhost:3000/ws/");

websocket.onopen = function (evt) { console.log("socket open"); };
websocket.onclose = function (evt) { console.log("socket close"); };
websocket.onmessage = function (evt) { console.log("socket message: " + evt.data); };
websocket.onerror = function (evt) { websocket.send("message"); };
```

### attribution

This library is based on
[ring-jetty9-adapter](https://github.com/sunng87/ring-jetty9-adapter)
which provides a Jetty ring adapter based on Jetty 10, with additional
websocket support.

## License

Copyright © 2016 Dmitri Sotnikov

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
