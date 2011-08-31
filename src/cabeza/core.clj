
(ns cabeza.core
	(:import [ java.util UUID])
	(:require [clojure.contrib.sql :as sql :only ()]  )
	(:use 
		[ cabeza util ]
		[clojure.contrib.string :only ( as-str )]
		[clojureql.core :as ql :only ()]))


; http://download.oracle.com/docs/cd/E12840_01/wls/docs103/jms/interop.html

(def *id*)

(def states { 
	:edition #{ :verification  }
	:verification #{ :production :edition }
	:production #{ :handover }
	:handover #{ :withdrawn :stolen :broken } })

(defn now[]
	(java.sql.Date. (System/currentTimeMillis)))

(defn date-as-string[date]
	(-> (java.text.SimpleDateFormat. "yyyy-MM-dd'T'HH:mm:ss") (.format date))) 	; .SSSZ

(defn uuid[]
	(.. (UUID/randomUUID) (toString)))


(def actions {

	:edition { :assert (fn[] (println "assering edition") true)
				:on-enter (fn[] (println "entering to edition") ) 
				:on-exit  (fn[] (println "exiting from edition") ) }

	:verification { :assert (fn[] (println "assering verification") true)
				:on-enter (fn[] (println "entering to verification") ) 
				:on-exit  (fn[] (println "exiting from verification") ) }

	:production { :assert (fn[] (println "assering production") true)
				:on-enter (fn[] (println "entering to production") ) 
				:on-exit  (fn[] (println "exiting from production") ) }


	:broken { :on-enter (fn[] (println "entering to broken") true) }
	})


(defn model-process[ & args ]
	(apply sql/create-table
			 :process
			[:id "varchar(64)" "PRIMARY KEY"]
			[:started "datetime" ]
			[:state "varchar(64)"]
			 args))


(defn spawn-process[ state & args ]
	(let [ id (uuid) ]
		(ql/conj! (ql/table :process) (conj {:id id :started (date-as-string (now)) :state (as-str state) } args ))
		id))


(defn alter-process[ & args ]
	(ql/update-in! (ql/table :process) (ql/where (= :id *id*)) args ))


(defn read-process[ arg ]
	(-> (ql/table :process) (ql/select (ql/where (= :id *id*)))
		(ql/pick! arg)))


(defn contains-in?[ m keys ]
	(not (nil? (get-in m keys))))


(def commands {

	:add-comment (fn[ message status data ]   )

	:request-transition (fn[ requested ]
	(let [ current-state (keyword (read-process :state)) ]
		(if (contains-in? states [ current-state requested])
			(let[ assert-fn (get-in actions [ requested :assert ] ) ]
				(if (or (nil? assert-fn) (assert-fn))
					(do (when-let[ exit-fn (get-in actions [ current-state :on-exit ]) ]
							(exit-fn))
						(when-let[ on-enter (get-in actions [ requested :on-enter ]) ]
							(on-enter))
						(alter-process {:state (as-str requested)} ) true)
						 	false)) false)))

})


(defn exec-command[ cmd-label & args ]
	(when-let[ f (cmd-label commands) ]
		 (apply f args)))


(def db (load-prop "cfg.properties" "db." ) )


(sql/with-connection db
	(sql/transaction
		(comment model-process [ :cost :int ] )
		(binding[ *id* (spawn-process :edition)  ]
			(exec-command :request-transition :verification )
			(exec-command :request-transition :production )
			(exec-command :request-transition :handover )
			(exec-command :request-transition :broken ))))



;	 (comment sql/transaction
;			(sql/create-table
;			   :process
;			   [:name "varchar(32)" "PRIMARY KEY"]
;			   [:appearance "varchar(32)"]
;			   [:cost :int]
;			   [:grade :int])
;		)
;
;
;	(comment ql/conj! (ql/table :process) {:name "tom" :appearance "cool" :cost 312 :grade 5 })
;	(comment ql/update-in! (ql/table :process) (ql/where (= :id id)) {:started 999})






