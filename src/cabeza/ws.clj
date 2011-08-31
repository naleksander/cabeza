(ns cabeza.ws 
	(:use [cabeza sh util])
	(:import [javax.xml.ws Endpoint com.sun.net.httpserver HttpServer] 
			[java.net URLClassLoader URL InetSocketAddress]))

(def *ws-server-folder* "generated/server")
(def *ws-client-folder* "generated/client")
(def *ws-server-classloader*)
(def *ws-client-classloader*)
(def *ws-package*)
(def *ws-name*)
(def *ws-methods*)
(def *ws-services* (ref {}))

(defn ensure-folder[ folder ]
	(when-not (-> (java.io.File. folder) (#(or (.exists %1) (.mkdirs %1))))
		(throw (Exception. (str "Failed to create " folder " folder for web services runtime")))))

(ensure-folder *ws-server-folder*)
(ensure-folder *ws-client-folder*)

(defn local-to-rul[ path ]
	(-> (java.io.File. path) 
	(.toURI) (.toURL)))



(def *ws-server-classloader* (java.net.URLClassLoader. (into-array URL 
	[ (local-to-rul *ws-server-folder*) ]) (.getContextClassLoader (Thread/currentThread))))

(def *ws-client-classloader* (java.net.URLClassLoader. (into-array URL 
	[ (local-to-rul *ws-client-folder*) ]) (.getContextClassLoader (Thread/currentThread))))

(defn get-raw-ws-path[ package name ]
	(str *ws-server-folder* "/" (.replaceAll package "\\." "/") "/" name ".java"))

(defn spawn-ws-server[ package name endpoint ]
	(let[ java-packge (.replaceAll package "\\." "/") ]
		(when (not= (sh "javac" "-cp" (System/getProperty "java.class.path") "-d" *ws-server-folder* (get-raw-ws-path package name)) 0)
			(throw (Exception. (str "Failed to compile " name " web service"))))

		(let[ srv (HttpServer/create (InetSocketAddress. 8183) 10) ]
				(.start srv)
					
			)

		(Endpoint/publish (str endpoint (.toLowerCase name) )
			(.newInstance (.loadClass *ws-server-classloader* (str package "." name))))))

(defn generate-ws-client-stub[ endpoint ]
	(when (not= (sh "wsimport" "-quiet"  "-d" *ws-client-folder* "-s" *ws-client-folder* endpoint) 0)
		(throw (Exception. (str "Failed to generate client " name " web service")))))

(defn generate-ws-server-stub[ package name ]
	(ensure-folder (str *ws-server-folder* "/"	(.replaceAll package "\\." "/")))
	(binding [ *ws-package* package *ws-name* name *ws-methods* (get-in @*ws-services* [ package name ] ) ]
		(spit (get-raw-ws-path package name) (with-out-str
		(load-data "ws.template")))))

(defn get-ws-client-stub[ package service-stub-name ]
	(.newInstance (.loadClass *ws-client-classloader* (str package "." service-stub-name))) )

(defmacro get-jax-ws-client[ package name ]
	(let[ port (symbol (str "get" name "Port")) ]
		`(let[ stub# (get-ws-client-stub ~package (str ~name "Service"))]
			(. stub# ~port))))

(defmacro provide-ws [ package name endpoint & body ]
	(let [ s (reduce (fn[ result decl ]
				(assoc result (str (second decl)) { 
						:function (list 'bound-fn (vec (map second (partition 2 (nth decl 2)))) (when (< 3(count decl)) (nth decl 3)))
						:return `(first '~decl)
						:args `(partition 2 (nth '~decl 2)) }
					)) {} body ) ]
		`(do (dosync 
			(alter *ws-services* assoc-in [ ~package ~name ] ~s))
				(generate-ws-server-stub ~package ~name)
				(spawn-ws-server ~package ~name ~endpoint))))

(defn dispatch-ws-call [ package name method & args ]
	(let [ func  (get-in @*ws-services* [ package name method :function ]) ]
		(comment println @*ws-services*)
		(comment println "going to invoke" package name method " args " (apply str (interpose " " args)) )
		(apply func args)))





