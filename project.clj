(defproject clojure_course_task04 "0.1.0-SNAPSHOT"
  :description "User interface to facet DB"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [compojure "1.1.5"]
                 [me.raynes/laser "1.1.1"]
                 [mysql/mysql-connector-java "5.1.24"]
                 [korma "0.3.0-RC5"]
                 [lib-noir "0.4.9"]
                 [markdown-clj "0.9.19"]]
  :plugins [[lein-ring "0.8.3"]]
  :ring {:handler x.or.fino.handler/app}
  :profiles
  {:dev {:dependencies [[ring-mock "0.1.3"]]}})
