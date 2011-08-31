
(ns cabeza.client
	(:use [cabeza ws]))




; generate client stub
(generate-ws-client-stub "http://localhost:8283/sales?wsdl")

; first compact way but assumes Service and Port
(let [ c (get-jax-ws-client "com.jaaka.michael" "Sales") ]
	(println "sum is" (.addNumbers c 2 31))
	(.doSomething c "this rox"))


; explicite call of generated stub
(let [ c (.getSalesPort (get-ws-client-stub "com.jaaka.michael" "SalesService")) ]
	(.addNumbers c 2 10))













