(ns clj-nehe.utils
  (:use [clojure.walk :only [postwalk]]
        [clojure.contrib.macro-utils :only [mexpand-all]]))

(defn casted? [expr]
  (when-let [[_ x] expr]
    (when (and (coll? x) (#{'float 'int 'double 'long 'short 'byte} (first x)))
      true)))

(defn to-prim-op [type]
  (fn [expr]
    (if-let [op (and (coll? expr)
                     (not (casted? expr))
                     (#{'+ '- '* '/ '< '> '=} (first expr)))]
      (concat `(prim ~type ~op) (rest expr))
      expr)))

(defmacro prim
  ([type a]
     `(~type ~(postwalk (to-prim-op type) (mexpand-all a))))
  ([type fn a]
     `(~fn (~type ~a)))
  ([type fn a b]
     `(~fn (~type ~a) (~type ~b)))
  ([type fn a b & rest]
     `(~fn (~type ~a)
        (~type (prim ~type ~fn ~b ~@rest)))))

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
