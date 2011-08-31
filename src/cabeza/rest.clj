(ns cabeza.rest)

;(meta addNumbers)
;(ns cabeza.ws
;  (:use [noir.core]  [hiccup.core])
;  (:require  [clj-json.core :as json] 
;		[ring.util.response :as response] [noir.server :as server]))
;
;
;(defpartial todo-item [{:keys [id title due]}]
;    [:li {:id id} ;; maps define HTML attributes
;        [:h3 title]
;        [:span.due due]]) ;; add a class
;
;(defpartial todos-list [items]
;    [:ul#todoItems ;; set the id attribute
;        (map todo-item items)])
;
;(defn all-todos[]
;	[ {:id "todo1"
;              :title "Get Milk"
;              :due "today"} ])
;
;(defn add-todo[ title due ]
;	)
;
;;(defpage "/todos" {}
;;         (let [items (all-todos)]
;;           (html
;;             [:h1 "Todo list!"]
;;             (todos-list items))))
;
;;; Handle an HTTP POST to /todos, returning a 
;;; json object if successful
;(defpage [:get "/todos"] {:keys [title due]}
;         (if-let [todo-id true]
;           (json/generate-string  {:id todo-id
;                           :title title
;                           :due-date due})
;           (json/generate-string  {})))
;
;(defpage "/welcome" []
;    "Welcome to Noir!")
;
;
;(defn -main[]
;	(server/start 8282))






;(ns bpml.ws
;  (:use [compojure core] [ring.adapter jetty]))
;
;(defn app [req]
;  {:status  200
;   :headers {"Content-Type" "text/html"}
;   :body    "Hello World from Ring"})
;
;
;(run-jetty app {:port 8080})
;
;
;(defroutes  math-servlet 
;  (GET "/add" 
;    (let [x (Integer/parseInt (params :x)) 
;          y (Integer/parseInt (params :y))] 
;      (str (+ x y) "\n")))) 
;
;(defn -main[]
;	(println "Hello World")
;	(run-server {:port 4545} 
;	  "/math/*" (servlet math-servlet)))
;
;(ns cabeza.ws
;  (:use [compojure.core] [ring.adapter.jetty])
;  (:require [compojure.route :as route]))
;
;(defroutes main-routes
;  (GET "/" [] "<h1>Hello World</h1>")
;  (route/not-found "<h1>Page not found</h1>"))
;
;
;
;(defn -main[]
;	(println "Hello World")
;	(run-jetty main-routes {:port 4545}))
