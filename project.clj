(defproject cabeza "1.0.0-SNAPSHOT"
  :main cabeza.server
  :description "Framework for enterprise class systems"
  :dependencies [[org.clojure/clojure "1.2.1"] 
	[com.sun.messaging.mq/jms "4.5.1-b03"] 
	[org.eclipse.jetty/jetty-server "7.5.0.RC1"]
	[org.eclipse.jetty/jetty-http "7.5.0.RC1"]
	[org.mortbay.jetty/jetty "6.1.26"]
	[org.mortbay.jetty/jetty-j2sehttpspi "7.5.0.RC1"]
	[compojure "0.6.5"] [clj-json "0.4.0"]
	[com.sun.xml.ws/jaxws-rt "2.2.5"]
	[ring "0.3.11"] [noir "1.1.0"] [hiccup "0.3.6"]])