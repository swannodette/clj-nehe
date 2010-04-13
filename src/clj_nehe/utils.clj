(ns clj-nehe.utils
  (:use [clojure.walk :only [postwalk]]
        [clojure.contrib.macro-utils :only [mexpand-all]]))

(defn to-prim-op [type]
  (fn [expr]
    (if-let [op (and (coll? expr)
                     (#{'+ '- '* '/ '< '> '=} (first expr)))]
      (concat `(prim ~type ~op) (rest expr))
      expr)))

(defmacro prim
  "If given an type and expression it will macroexpand the expression fully 
   and then convert all primitive operations into binary operations and 
   type-hint all parameters.
   If given a type, fn, and expression(s) it does not macroexpand all 
   subexpressions. Basically you have to be somewhat careful how you use 
   -> in conjuction with prim."
  ([type a]
     `(~type ~(postwalk (to-prim-op type) (mexpand-all a))))
  ([type fn a]
     `(~fn (~type ~((to-prim-op type) a))))
  ([type fn a b]
     `(~fn (~type ~((to-prim-op type) a)) (~type ~((to-prim-op type) b))))
  ([type fn a b & rest]
     `(~fn (~type ~((to-prim-op type) a))
        (prim ~type ~fn ~b ~@rest))))

(comment

  (prim float (+ 4 5 (* 6 7) 8))

  (prim float (-> 4 (+ 5) (+ (* 6 7)) (+ 8)))

  ; < ~20ms
  (dotimes [_ 10]
    (time (dotimes [_ 1000000]
            (prim float (+ 4 5 (* 6 7) 8)))))

  ; < ~20ms
  (dotimes [_ 10]
    (time (dotimes [_ 1000000]
            (prim float (-> 4 (+ 5) (+ (* 6 7)) (+ 8))))))

  ; < ~20ms
  (dotimes [_ 10]
    (time (dotimes [_ 1000000]
            (prim float (+ 4 5 (prim int + 1 2))))))

  ; ~260ms
  (dotimes [_ 10]
    (time (dotimes [_ 1000000]
            (+ 4 5 (* 6 7) 8))))

  )
