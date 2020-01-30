# luminus-jetty

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
   [luminus.http-server :as http]))

(defn http-handler [request]
  {:status 200
   :headers {"Content-Type" "text/plain"}
   :body (:remote-addr request)})

(def ws-handler-a
   {:context-path         "/ws-a" ;WS handler context
    :allow-null-path-info true ;default false
    :on-connect           (fn [& args]
                            (log/info "WS connect" args))
    :on-error             (fn [& args]
                            (log/info "WS error" args))
    :on-text              (fn [ws text]
                            (log/info "text:" text)
                            (ws/send! ws text))
    :on-close             (fn [ws]
                            (log/info "WS close" args))
    :on-bytes             (fn [& args]
                            (log/info "WS bytes" args))}))

(def ws-handler-b
   {:context-path         "/ws-b" ;WS handler context    
    :on-text              (fn [ws text]
                            (log/info "text:" text)
                            (ws/send! ws text))}))

;;create a single WS handler
(http/start
 {:handler http-handler
  :was-handler ws-handler-a
  :port 3000})

;;create multiple WS handlers
(http/start
 {:handler http-handler
  :was-handlers [ws-handler-a ws-handler-b]
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

There is a new option `:websockets` available. Accepting a map of context path and listener class:
```clojure
(use 'ring.adapter.jetty9)
(run-jetty app {:websockets {"/loc" ws-handler}})
```

### attribution

Websocket support added based on [ring-jetty9-adapter](https://github.com/sunng87/ring-jetty9-adapter) implementation.

## License

Copyright © 2016 Dmitri Sotnikov

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
