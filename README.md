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

;; a handler can be specified using a map
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
    :on-close             (fn [& args]
                            (log/info "WS close" args))
    :on-bytes             (fn [& args]
                            (log/info "WS bytes" args))}))

;; alternatively you can provide a :handler-fn key
;; the key should point to a function that accepts a
;; Ring request map to initialize the websocket connection
(def ws-handler-b
   {:context-path         "/ws-b" ;WS handler context    
    :handler-fn   (fn [req]                    
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

### attribution

Websocket support added based on [ring-jetty9-adapter](https://github.com/sunng87/ring-jetty9-adapter) implementation.

## License

Copyright © 2016 Dmitri Sotnikov

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
