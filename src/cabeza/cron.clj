(ns cabeza.cron
	(:import [java.util Calendar GregorianCalendar Timer TimerTask Date])
	(:use 
		[clojure pprint]
		[cabeza util]
		[clojure.contrib.string :only ( as-str )]
		[clojureql.core :as ql :only ()]))

(def *cron-date*)
(def *cron-date-curval*)
(def *cron-tab*)
(def *cron-timer*)
(def *cron-date-found*)

(defn parse-cron[ input ]

	(def calendar (Calendar/getInstance))
	
	(defn split[ input pattern ]
		(into [] (.split input pattern)))
	
	(defn range-of-unit[ unit div ]
		(let [ r (range (.getMinimum calendar unit) (inc (.getMaximum calendar unit))) ]
			(condp = unit
				Calendar/MINUTE r
				Calendar/HOUR_OF_DAY r
				Calendar/MONTH (map inc r)
				(if (nil? div) nil r) )))
	
	(defn resolve-range[ vals div unit]
		(mapcat
			(fn[ val ] (let[ r (split val "-") ]
				(if (= (count r) 2)
					(range (Integer. (first r)) (inc (Integer. (second r))))
					(if (= val "*")  
						(range-of-unit unit div)
							(list (Integer. val)))))) vals))
	
	(defn shift-weekdays[ col ]
		(if (empty? col) col
			(let [ z (zipmap (range 1 8) (iterate #(inc (mod %1 7)) (.getFirstDayOfWeek calendar))) ]
				(map #(get z %1) col))))
	
	(defn shift-months[ col ]
		(if (empty? col) col
			(map dec col)))
		
	(let [ split-result (split input "\\s+") ]
		(let[ dividers (map (fn[ part ] (split part "/")  ) split-result) ]
					(let[ commas (map (fn[ [val div] ] [ (split val ",") div] ) dividers )]
								(let[ ranges (map (fn[ [vals div ] unit ] [ (resolve-range vals div unit) (if (nil? div) nil (Integer. div)) ] )  commas 
													[Calendar/MINUTE Calendar/HOUR_OF_DAY Calendar/DAY_OF_MONTH	Calendar/MONTH  Calendar/DAY_OF_WEEK]  ) ]
										(let[  [minutes hours days months weekday ]  ranges  ]
											(let [ weekday (if (and (empty? (first days)) (empty? (first weekday))) 
														[ (range-of-unit Calendar/DAY_OF_WEEK true) (second weekday)  ] weekday) ]
											(let [ divided (map (fn[ [vals div] ] (if (nil? div) vals (filter #(= (mod %1 div) 0) vals) )   )  [minutes hours days months weekday ] ) ]
												(let[ [minutes hours days months weekday ] divided ]
														(map #(into #{} %1)	[ minutes hours days (shift-months months) (shift-weekdays weekday) ])
													)
											)))
								)
						)
				)
	))

(defn date-set[ unit val ]
	(.set *cron-date* unit val))

(defn date-inc[ unit val ]
	(.add *cron-date* unit val))

(defn date-value[]
	(reset!  *cron-date-found* (.getTime *cron-date*)))

(defmacro with-date [ n & body ]
	`(binding [ *cron-date* ~n *cron-date-found*  (atom nil) ]
		~@body @*cron-date-found*))

(defmacro iterate-with-limit[ n & body ]
	`(loop[x# ~n]
		~@body
		(if (and (nil? @*cron-date-found*) (pos? x#)) (recur (dec x#)))))

(defmacro if-date-matches [ pattern body & else ]
	`(let[ new-pattern# (partition 2 ~pattern) ]
		(if (some (fn[ [ u# v# ] ] (contains? v# (.get *cron-date* u#))) new-pattern#)
				(~@body)
			(do ~@else ))))

(defmacro while-date-dont-change[ unit & body ]
	`(binding[ *cron-date-curval* (.get *cron-date* ~unit) ]
		(while (and (nil? @*cron-date-found*) (= (.get *cron-date* ~unit) *cron-date-curval*))
			~@body)))

(defn cron[ pattern ]
	(let[ [min-pat hour-pat day-pat month-pat week-pat] (parse-cron pattern) ]
			(with-date (Calendar/getInstance)
				(date-set Calendar/SECOND 0)
				(date-set Calendar/MILLISECOND 0)
				(date-inc Calendar/MINUTE 1)
				(iterate-with-limit 128 
					(if-date-matches [Calendar/MONTH month-pat]
						(while-date-dont-change Calendar/MONTH
			 				(if-date-matches [Calendar/DAY_OF_MONTH day-pat Calendar/DAY_OF_WEEK week-pat]
								(while-date-dont-change Calendar/DAY_OF_MONTH
									(if-date-matches [Calendar/HOUR_OF_DAY hour-pat]
										(while-date-dont-change Calendar/HOUR_OF_DAY
											(if-date-matches [Calendar/MINUTE min-pat]
												(date-value)
											)
											(date-inc Calendar/MINUTE 1)
										)
										(date-set Calendar/MINUTE 0)
										(date-inc Calendar/HOUR_OF_DAY 1)
									)	
								)
								(date-set Calendar/HOUR_OF_DAY 0)
								(date-set Calendar/MINUTE 0)
								(date-inc Calendar/DAY_OF_MONTH 1)
							)
						)
						(date-set Calendar/DAY_OF_MONTH 1)
						(date-set Calendar/HOUR_OF_DAY 0)
						(date-set Calendar/MINUTE 0)
						(date-inc Calendar/MONTH 1)
					)
				)
			)))

(defn calculate[]
	(reduce (fn[ a [ b f ] ] (update-in-using a
		[ (cron (first f)) ] #{} conj b  )) {} *cron-tab* ))

(declare schedule)

(defn execution[c]
	(bound-fn[] (let[ d (Date.) ]
		(doseq[ [k v] (filter (fn[ [k v]] (or (.before k d) (.equals k d))) c) ]
			(doseq[ id v]
				(if-let[ e (second (get *cron-tab* id))]
						(e)))) (schedule))))
(defn schedule[]
	(let[ c (calculate) ]
		(if-let[ f (or (ffirst (sort-by first (fn[a b] (.before a b)) c))
				 (Date. (+ 60000 (System/currentTimeMillis)) )) ]
			(let[ e (execution c) ]
				(println "Executing at" f)
				(.schedule *cron-timer* (proxy[TimerTask][]
					(run[] (e))) f)))))

(defmacro with-cron-tab [ path & body ]
	`(binding [*cron-timer* (Timer. true) *cron-tab* (load-data ~path)]
		(schedule)
		~@body			
		(.cancel *cron-timer* )))

(with-cron-tab "../cron.tab"
	(println "hello world")
	(Thread/sleep (* 60 1000 15)))










