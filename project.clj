(defproject luminus-jetty "0.1.8"
  :description "Jetty adapter for Luminus"
  :url "https://github.com/luminus-framework/luminus-jetty"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/tools.logging "0.4.0"]
                 [org.clojure/core.async "0.3.465"]
                 [ring/ring-jetty-adapter "1.7.1"]
                 [org.eclipse.jetty.websocket/websocket-server "9.4.26.v20200117"]
                 [org.eclipse.jetty.websocket/websocket-servlet "9.4.26.v20200117"]])
