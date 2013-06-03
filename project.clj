(defproject jogltest "1.0.0-SNAPSHOT"
  :description "FIXME: write description"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojars.toxi/jogl "2.0.0-rc11"]
                 ;[org.clojars.toxi/gluegen-rt "2.0.0-rc11"]
                 ]
  :repositories {"local" ~(str (.toURI (java.io.File. "maven-repo")))}
  :native-path "lib/jogamp-all-platforms/lib/"
  :main jogltest.core
  :jvm-opts ["-Djogamp.gluegen.UseTempJarCache=false" "-Xmx 2048M"]
)
