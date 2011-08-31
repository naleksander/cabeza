
(ns cabeza.sh)

(defn- shell-out[ fnc & args ]
	(letfn[ (readchar [ s ] (if (> (.available s) 0) (let[ cc (char (.read s)) ] (fnc cc) true))) ]
				(let [ proc (.exec (Runtime/getRuntime) 
					(into-array String args))]
						(loop[ in (.getInputStream proc) er  (.getErrorStream proc) ]
							(let [ got (or (readchar in) (readchar er)) ]
							(if got (recur in er)
							(if-let [result (try
								(.exitValue proc)
									(catch Exception e (Thread/sleep 100) nil))]
											result (recur in er))))))))

(defn sh[ & args ]
	(apply shell-out (fn[ cc ] (print cc) (flush)) args))

(defn sh-out-str[ & args ]
	(let [ s (StringBuilder.) ]
		(apply shell-out (fn[ cc ] (.append s cc)) args)
			(.toString s)))


