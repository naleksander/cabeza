

(ns cabeza.jms
	(:import [java.util UUID] [javax.jms MessageListener 
		ConnectionFactory Connection Session MessageProducer
		MessageConsumer Queue Session Message ObjectMessage])
	(:require [clojure.contrib.sql :as sql :only ()])
	(:use 
		[clojure.contrib.string :only ( as-str )]
		[clojureql.core :as ql :only ()]))

(def *jms-conn*)

(defmacro using-jms-connection[ & body ]
	`(let[ factory# (com.sun.messaging.ConnectionFactory.) ]
		(with-open[ conn# (.createConnection factory#) ]
			(binding[ *jms-conn* conn# ]
				~@body))))

(defn send-message[ queue-name & datas ]
	(with-open [ session (.createSession *jms-conn* false Session/AUTO_ACKNOWLEDGE) ]
		(let[queue (com.sun.messaging.Queue. queue-name) 
			producer (.createProducer session queue) ]
	
			(doseq[ data datas ]
				(let[ message (.createObjectMessage session)]
					(.setObject message data)
					(.send producer message))))))

(defn listen-message[ queue-name listener-fn ] 
	(let [	session (.createSession *jms-conn* false Session/AUTO_ACKNOWLEDGE) ]
		(let[ queue (com.sun.messaging.Queue. queue-name) 
			consumer (.createConsumer session queue)]
					 
				(.setMessageListener consumer (proxy[MessageListener][] 
					(onMessage[ msg ]
						(listener-fn session msg)
					))) session)))

(defn start-consumption[]
	(.start *jms-conn*))



(using-jms-connection

(def a (listen-message "test" (fn[ session msg ]
	(println "receiving message by #1" (.getObject msg)))))

;(def b (listen-message "test" (fn[ session msg ]
;	(println "receiving message by #2" (.getObject msg)))))


(start-consumption)

(println "Started to listen")



(println "Now sending message")

(send-message "test" "Hello Super World") )




;	(comment let[ consumer (.createConsumer session queue)]
;
;			(println "Starting to listen synch")
;
;			(.start conn)
;
;			(let [ msg (.receive consumer) ]
;
;				
;
;				(println "got message synch" (.getObject msg))
;
;				))






