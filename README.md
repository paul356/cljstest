Summary

An experimental clojurescript and Arduino project which consists of a simple backend and frontend.

The backend is written in plain clojure which uses ring, compojure, and jssc to service web requests. The backend provides intefaces to switch some "PORT"s. The "PORT" is a delegate of an Arduino digital pin. You can set these Arduino pins to high and low voltage though /port/set/<index>?val=1/0 and get pin status by /port/get/<index>. Beside these interface the backend serve a simple html page which runs the javascript frontend.

The frontend is clojurescript generated javascript. It renders a background image and some buttons to control "PORT"s.
