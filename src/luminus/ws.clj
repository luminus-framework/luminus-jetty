(ns luminus.ws
  (:import [org.eclipse.jetty.server Request]
           [org.eclipse.jetty.server.handler
            ContextHandler]
           [org.eclipse.jetty.websocket.api
            WebSocketAdapter Session
            UpgradeRequest RemoteEndpoint WriteCallback]
           [org.eclipse.jetty.websocket.server WebSocketHandler]
           [org.eclipse.jetty.websocket.servlet
            WebSocketServletFactory WebSocketCreator]
           [clojure.lang IFn]
           [java.nio ByteBuffer])
  (:require [clojure.string :as string]))

(defprotocol RequestMapDecoder
  (build-request-map [r]))

(defprotocol WebSocketProtocol
  (send! [this msg] [this msg callback])
  (close! [this])
  (remote-addr [this])
  (idle-timeout! [this ms])
  (connected? [this])
  (req-of [this]))

(defprotocol WebSocketSend
  (-send! [x ws] [x ws callback] "How to encode content sent to the WebSocket clients"))

(def ^:private no-op (constantly nil))

(defn- write-callback
  [{:keys [write-failed write-success]
    :or {write-failed  no-op
         write-success no-op}}]
  (reify WriteCallback
    (writeFailed [_ throwable]
      (write-failed throwable))
    (writeSuccess [_]
      (write-success))))

(extend-protocol WebSocketSend
  (Class/forName "[B")
  (-send!
    ([ba ws]
     (-send! (ByteBuffer/wrap ba) ws))
    ([ba ws callback]
     (-send! (ByteBuffer/wrap ba) ws callback)))

  ByteBuffer
  (-send!
    ([bb ws]
     (-> ^WebSocketAdapter ws .getRemote (.sendBytes ^ByteBuffer bb)))
    ([bb ws callback]
     (-> ^WebSocketAdapter ws .getRemote (.sendBytes ^ByteBuffer bb ^WriteCallback (write-callback callback)))))

  String
  (-send!
    ([s ws]
     (-> ^WebSocketAdapter ws .getRemote (.sendString ^String s)))
    ([s ws callback]
     (-> ^WebSocketAdapter ws .getRemote (.sendString ^String s ^WriteCallback (write-callback callback)))))

  IFn
  (-send! [f ws]
    (-> ^WebSocketAdapter ws .getRemote f))

  Object
  (send!
    ([this ws]
     (-> ^WebSocketAdapter ws .getRemote
         (.sendString ^RemoteEndpoint (str this))))
    ([this ws callback]
     (-> ^WebSocketAdapter ws .getRemote
         (.sendString ^RemoteEndpoint (str this) ^WriteCallback (write-callback callback))))))

(extend-protocol RequestMapDecoder
  UpgradeRequest
  (build-request-map [request]
    {:uri (.getPath (.getRequestURI request))
     :query-string (.getQueryString request)
     :origin (.getOrigin request)
     :host (.getHost request)
     :request-method (keyword (.toLowerCase (.getMethod request)))
     :headers (reduce (fn [m [k v]]
                        (assoc m (keyword
                                  (string/lower-case k)) (string/join "," v)))
                      {}
                      (.getHeaders request))}))

(extend-protocol WebSocketProtocol
  WebSocketAdapter
  (send!
    ([this msg]
     (-send! msg this))
    ([this msg callback]
     (-send! msg this callback)))
  (close! [this]
    (.. this (getSession) (close)))
  (remote-addr [this]
    (.. this (getSession) (getRemoteAddress)))
  (idle-timeout! [this ms]
    (.. this (getSession) (setIdleTimeout ^long ms)))
  (connected? [this]
    (. this (isConnected)))
  (req-of [this]
    (build-request-map (.. this (getSession) (getUpgradeRequest)))))

(defn- proxy-ws-adapter
  [{:keys [on-connect on-error on-text on-close on-bytes]
    :or {on-connect no-op
         on-error   no-op
         on-text    no-op
         on-close   no-op
         on-bytes   no-op}}]
  (proxy [WebSocketAdapter] []
    (onWebSocketConnect [^Session session]
      (let [^WebSocketAdapter this this]
        (proxy-super onWebSocketConnect session))
      (on-connect this))
    (onWebSocketError [^Throwable e]
      (on-error this e))
    (onWebSocketText [^String message]
      (on-text this message))
    (onWebSocketClose [statusCode ^String reason]
      (let [^WebSocketAdapter this this]
        (proxy-super onWebSocketClose statusCode reason))
      (on-close this statusCode reason))
    (onWebSocketBinary [^bytes payload offset len]
      (on-bytes this payload offset len))))

(defn- reify-default-ws-creator
  [options]
  (reify WebSocketCreator
    (createWebSocket [this _ _]
      (proxy-ws-adapter options))))

(defn ^:internal proxy-ws-handler
  "Returns a Jetty websocket handler"
  [{:keys [ws-max-idle-time
           ws-max-text-message-size]
    :or   {ws-max-idle-time         500000
           ws-max-text-message-size 65536}
    :as   options}]
  (proxy [WebSocketHandler] []
    (configure [^WebSocketServletFactory factory]
      (doto (.getPolicy factory)
        (.setIdleTimeout ws-max-idle-time)
        (.setMaxTextMessageSize ws-max-text-message-size))
      (.setCreator factory (reify-default-ws-creator options)))
    (handle [^String target, ^Request request req res]
      (let [wsf (proxy-super getWebSocketFactory)]
        (if (.isUpgradeRequest wsf req res)
          (if (.acceptWebSocket wsf req res)
            (.setHandled request true)
            (when (.isCommitted res)
              (.setHandled request true)))
          (proxy-super handle target request req res))))))

(defn ws-handler [{:keys [context-path
                          allow-null-path-info?]
                   :or   {allow-null-path-info? false}
                   :as   options}]
  (doto (ContextHandler.)
    (.setContextPath context-path)
    (.setAllowNullPathInfo allow-null-path-info?)
    (.setHandler (proxy-ws-handler options))))