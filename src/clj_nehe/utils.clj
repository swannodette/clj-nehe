(ns clj-nehe.utils
  (:use [clojure.walk :only [postwalk postwalk-demo]]))

(defn to-prim-op [expr type]
  (if-let [op (and (coll? expr)
                   (#{'+ '- '* '/ '< '> '=} (first expr)))]
    (concat `(prim ~op ~type) (rest expr))
    expr))

(defmacro prim
  ([fn type a]
     `(~fn (~type ~(to-prim-op a type))))
  ([fn type a b]
     `(~fn (~type ~(to-prim-op a type)) (~type ~(to-prim-op b type))))
  ([fn type a b & rest]
     `(~fn (~type ~(to-prim-op a type))
        (prim ~fn ~type ~b ~@rest))))

(defmacro prim-int [fn & rest]
  `(prim ~fn int ~@rest))

(defmacro prim-float [fn & rest]
  `(prim ~fn float ~@rest))

(comment
  
  ; < ~20ms
  (dotimes [_ 10]
    (time (dotimes [_ 1000000]
            (prim + float 4 5 (* 6 7) 8))))

  ; ~260ms
  (dotimes [_ 10]
    (time (dotimes [_ 1000000]
            (+ 4 5 (* 6 7) 8))))
  
  )
