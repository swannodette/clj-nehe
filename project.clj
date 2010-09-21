(defproject clj-nehe "0.1.0-SNAPSHOT"
  :description "The Nehe tutorials ported to Penumbra"
  :dependencies [[org.clojure/clojure "1.2.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [penumbra "0.6.0-SNAPSHOT"]]
  :native-dependencies [[penumbra/lwjgl "2.4.2"]]
  :dev-dependencies [[native-deps "1.0.4"]
                     [swank-clojure "1.3.0-SNAPSHOT"]])