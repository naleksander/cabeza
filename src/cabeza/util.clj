

(ns cabeza.util
	(:import [java.util Properties] 
		[java.io InputStreamReader FileInputStream]
		[java.util GregorianCalendar Calendar]))


(defn load-prop[ path & [ prefix ] ]
	(with-open[ fin (FileInputStream. path) rins (InputStreamReader. fin)  ]
		(let [ prop (Properties.) ]
			(.load prop rins)
			(let[ filter-fn 
				(or (and prefix (fn[ k v ]
						(when (.startsWith k prefix)
							[ (keyword (.substring k (.length prefix))) v ]))) 
					(fn[ k v ] [ (keyword k) v ]) )  ] 
			(reduce (fn[ p [ k v ] ] 
					(if-let[ [ nk nv] (filter-fn k v) ] 
						(assoc p nk nv) p)) {} prop)))))


(defn load-data[ path ]
	(read-string (slurp path)))

(defn funnel []
	 (let [q (java.util.concurrent.SynchronousQueue.) s (Object.)]
	   [ (take-while (partial not= q) 
			(repeatedly #(let[e (.take q)] (if (= e s) nil e))))
	    		(fn ([e] (.put q (if (nil? e) s e))) 
					([] (.put q q)))]))

(defmacro bound-future[ & body ]
	`(future-call (bound-fn [] ~@body)))

(def *yield-fn*)

(defn yield[ e ]
	(*yield-fn* e))

(defmacro yieldish[ & content ]
	`(let[ [s# f#] (funnel) ]
		(binding [ *yield-fn* f# ]
			(bound-future (do ~@content (*yield-fn*)))	s#)))

(defmacro update-in-using
	[ data keys ele op & args ]
		`(if-let [s# (get-in ~data ~keys)] 
			(update-in ~data ~keys ~op ~@args)
			(-> ~data (assoc-in ~keys ~ele)
				(update-in ~keys ~op ~@args))))


; (:jms.connection (load-prop "cfg.properties" ))

