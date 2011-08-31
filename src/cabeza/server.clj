(ns cabeza.server
	(:use [cabeza ws]))


(defn -main[]


	
	; publish web service with implementing methods
	(provide-ws "com.jaaka.michael" "Sales" "http://localhost:8283/"
	
		(String sayHello [ String name String b ]	
			(str "Hello " name))
	
		(Integer addNumbers [ Integer a Integer b ]
			(+ a b))		
	
		(void doSomething[ String dom ]
			(println dom)) 
	
	)
	(println "running")
	(Thread/sleep 30000))