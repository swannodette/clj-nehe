(defproject clj-nehe "0.1.0-SNAPSHOT"
  :description "The Nehe tutorials ported to Penumbra"
  :dependencies [[org.clojure/clojure "1.2.0-master-SNAPSHOT"]
                 [org.clojure/clojure-contrib "1.2.0-SNAPSHOT"]
                 [penumbra "0.6.0-SNAPSHOT"]
                 [slick-util "1.0.0"]
                 [cantor "0.1.0"]]
  :native-dependencies [[penumbra/lwjgl "2.4.2"]]
  :dev-dependencies [[native-deps "1.0.0"]
                     [leiningen/lein-swank "1.2.0-SNAPSHOT"]])