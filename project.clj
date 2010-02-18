(defproject clj-nehe "0.1.0"
  :description "The Nehe tutorials ported to Penumbra"
  :dependencies [[org.clojure/clojure "1.1.0"]
                 [org.clojure/clojure-contrib "1.1.0"]
                 [penumbra "0.5.0-SNAPSHOT"]]
  :native-dependencies [[lwjgl "2.2.2"]]
  :dev-dependencies [[native-deps "1.0.0-SNAPSHOT"]
                     [leiningen/lein-swank "1.1.0"]])