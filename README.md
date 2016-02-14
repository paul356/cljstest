#cljstest

_Keywords: Clojure ClojureScript Arduino_

A simple ***Clojure*** and Arduino project which consists of a server and a web client and arduino. You can control the states of arduino ports or some in memory values from a web browser.

The backend is written in plain clojure which uses ring, compojure to service web requests and jssc to do the serial communication. The backend provides intefaces to switch some "PORT"s. The "PORT" represents an Arduino digital pin. You can set these Arduino pins to high and low voltage though /port/set/<index>?val=1/0 and get pin status by /port/get/<index>. These APIs are used by the entry page which has some buttons to turn on and off these "PORT"s.

