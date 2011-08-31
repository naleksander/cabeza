(defproject cabeza "1.0.0-SNAPSHOT"
  :main cabeza.ws
  :description "Framework for enterprise class systems"
  :dependencies [[org.clojure/clojure "1.2.1"] 
	[com.sun.messaging.mq/jms "4.5.1-b03"] 
	[compojure "0.6.5"] [clj-json "0.4.0"]
	[com.sun.xml.ws/jaxws-rt "2.2.5"]
	[ring "0.3.11"] [noir "1.1.0"] [hiccup "0.3.6"]])