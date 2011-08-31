

(ns cabeza.parser)

(defn split[ pat in ]
	(if (not (nil? in))
		(into [] (.split in pat)) []))

(def *ctx)
(defmacro with-c[ & body ]
	`(binding [ *ctx (atom []) ]
		~@body))

(defn m+[ z ]
	(swap! *ctx conj z))

(defn mr[]
	(let[ z @*ctx] (reset! *ctx []) z))

(defn to-int[ a ]
	(when a (Integer. a)))

(def *g)(def *g1)(def *g2)(def *g3)(def *g4)(def *g5)(def *g6)
(defmacro grep[ [ pat in ]  & body ]
	`(do (if-let[ r# (re-matches (re-pattern ~pat) ~in) ]
			(binding [ *g (first r#) *g1 (get r# 1)  *g2 (get r# 2)  *g3 (get r# 3)
					*g4 (get r# 4)  *g5 (get r# 5)  *g6 (get r# 6)  ]
			~@body))))

(defn mapcatlist [ fn in ]
	(for[ s in]
		(mapcat fn s)))

(defn onthat[ idx f in ]
	(map-indexed (fn[ i e ]
		(if (= i idx) (f e) e)) in))

(defn parse-cron[ pattern ]
	(with-c
		(map #(vector (into #{} %1) %2)  (doall (->> pattern
			(split "\\s+")
			(map #(split "/" %1) )
			(map (fn[ [f s] ] (m+ (to-int s)) (when-not (= f "*") f)) )
			(map #(split "," %1) )		
			(mapcatlist #(or (grep ["(.+)-(.+)" %1] (range (to-int *g1) (inc (to-int *g2)) ) ) [ (to-int %1) ] ))

			(onthat 3 (fn[ a ] (map dec a)))

			(onthat 4 (fn[a ] (let [ z (zipmap (range 1 8) (iterate #(inc (mod %1 7)) 
				(.getFirstDayOfWeek (java.util.Calendar/getInstance)))) ]
					(into #{} (map #(get z %1) a)))))

				)) (mr))))


(parse-cron "* 2 3,4/2 1-5,6 1,2,4")

(parse-cron "*/2 * */3 1-10 4")

(parse-cron "* 2 3,4/2 1-5,6 1,2,4")










