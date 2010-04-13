(ns clj-nehe.utils)

(defmacro prim
  ([fn type a]
     `(~fn (~type ~a)))
  ([fn type a b]
     `(~fn (~type ~a) (~type ~b)))
  ([fn type a b & rest]
     `(~fn (~type ~a)
        (prim ~fn ~type ~b ~@rest))))

(defmacro prim-float [fn & rest]
  `(prim ~fn float ~@rest))

(comment
  
  ; < ~20ms
  (dotimes [_ 10]
    (time (dotimes [_ 1000000]
            (prim + float 4 5 (prim * float 6 7) 8))))

  ; ~260ms
  (dotimes [_ 10]
    (time (dotimes [_ 1000000]
            (+ 4 5 (* 6 7) 8))))
  
  )
