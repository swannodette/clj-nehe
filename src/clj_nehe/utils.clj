(ns clj-nehe.utils
  (:use [clojure.walk :only [postwalk postwalk-demo]]))

(defn to-prim-op [expr type]
  (if-let [op (and (coll? expr)
                   (#{'+ '- '* '/ '< '> '=} (first expr)))]
    (concat `(prim ~type ~op) (rest expr))
    expr))

(defmacro prim
  ([type a]
     `(~type ~(to-prim-op a type)))
  ([type fn a]
     `(~fn (~type ~(to-prim-op a type))))
  ([type fn a b]
     `(~fn (~type ~(to-prim-op a type)) (~type ~(to-prim-op b type))))
  ([type fn a b & rest]
     `(~fn (~type ~(to-prim-op a type))
        (prim ~type ~fn ~b ~@rest))))

(comment

  (prim float (+ 4 5 (* 6 7) 8))
  
  ; < ~20ms
  (dotimes [_ 10]
    (time (dotimes [_ 1000000]
            (prim float (+ 4 5 (* 6 7) 8)))))

  ; < ~20ms
  (dotimes [_ 10]
    (time (dotimes [_ 1000000]
            (prim float (+ 4 5 (prim int + 1 2))))))

  ; ~260ms
  (dotimes [_ 10]
    (time (dotimes [_ 1000000]
            (+ 4 5 (* 6 7) 8))))

  )
