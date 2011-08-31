package com.jaaka.michael;

import javax.jws.WebMethod;
import javax.jws.WebService;

import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;

@WebService
public class Sales { 
	

	

	
	    @WebMethod
	    public void doSomething(String dom) throws RuntimeException {

			try {

				/*System.out.println( "Invoking doSomething with " + dom  );*/

			Var service = RT.var("bpml.ws", "dispatch-ws-call" );

	        service.invoke(  "com.jaaka.michael", "Sales", "doSomething", dom );
		

			} catch(Exception e) {
				throw new RuntimeException( "Failed to call clojure method for web service call", e );
			}
	    }
	

	
	    @WebMethod
	    public Integer addNumbers(Integer a, Integer b) throws RuntimeException {

			try {

				/*System.out.println( "Invoking addNumbers with " + a + ", " + b  );*/

			Var service = RT.var("bpml.ws", "dispatch-ws-call" );

	        return (Integer)service.invoke(  "com.jaaka.michael", "Sales", "addNumbers", a, b );
		

			} catch(Exception e) {
				throw new RuntimeException( "Failed to call clojure method for web service call", e );
			}
	    }
	

	
	    @WebMethod
	    public String sayHello(String name, String b) throws RuntimeException {

			try {

				/*System.out.println( "Invoking sayHello with " + name + ", " + b  );*/

			Var service = RT.var("bpml.ws", "dispatch-ws-call" );

	        return (String)service.invoke(  "com.jaaka.michael", "Sales", "sayHello", name, b );
		

			} catch(Exception e) {
				throw new RuntimeException( "Failed to call clojure method for web service call", e );
			}
	    }
	

}

