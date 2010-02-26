(ns clj-nehe.tutorial11
  (:use [penumbra opengl geometry]
        [penumbra.opengl.texture :only [gl-tex-coord-2]]
        [penumbra.opengl.core :only [gl-import]])
  (:use [clojure.contrib.duck-streams :only [pwd]])
  (:require [penumbra.app :as app]))

;; -----------------------------------------------------------------------------
;; Vars

(def *image-path* (str (pwd) "/src/clj_nehe/Tim.bmp"))
(def *width* 640)
(def *height* 480)

(def *points*
     (for [x (range 45) y (range 45)]
       [(- (/ x 5.0) 4.5)
        (- (/ y 5.0) 4.5)
        (* (* (Math/sin (/ (* (/ x 5.0) 40.0) 360.0)) Math/PI) 2.0)]))

;; -----------------------------------------------------------------------------
;; Helpers

(defmacro series [& args]
  (let [syms (take (count args) (repeatedly gensym))
        forms (map #(cons 'apply %) (partition 2 (interleave args syms)))]
   `(fn [[~@syms]]
      ~@forms)))

(defn tex-coord [x y]
  (gl-tex-coord-2 x y))

(def tex-coord-and-vertex (series tex-coord vertex))

;; -----------------------------------------------------------------------------
;; Import

(gl-import glClearDepth clear-depth)

;; -----------------------------------------------------------------------------
;; Fns

(defn init [state]
  (app/title! "Nehe Tutorial 11")
  (app/vsync! false)
  (app/display-mode! *width* *height*)
  (enable :texture-2d)
  (shade-model :smooth)
  (clear-color 0 0 0 0.5)
  (clear-depth 1)
  ;(enable :depth-test)
  ;(depth-test :lequal)
  ;(gl-polygon-mode :back :fill)
  ;(gl-polygon-mode :front :line)
  (hint :perspective-correction-hint :nicest)
  (-> state
      (assoc :xrot 0)
      (assoc :yrot 0)
      (assoc :zrot 0)
      (assoc :points *points*)
      ;(assoc :texture (load-texture-from-file *image-path*))
      ))

(defn reshape [[x y width height] state]
  (viewport 0 0 width height)
  (frustum-view 45 (/ (double width) height) 0.1 100)
  (load-identity)
  state)

(defn update [[delta time] state]
   (-> state
       (update-in [:xrot] #(+ % 0.3))
       (update-in [:yrot] #(+ % 0.2))
       (update-in [:zrot] #(+ % 0.4))))

(defn display [[delta time] {points :points :as state}]
  (translate 0 0 -5)
;;   (rotate (:xrot state) 1 0 0)
;;   (rotate (:yrot state) 0 1 0)
;;   (rotate (:zrot state) 0 0 1)
;;   (with-texture (:texture state)
;;     (draw-quads
;;      (doseq [i (range 45) j (range 45)]
;;        (let [fx  (/ i 44.0)
;;              fy  (/ j 44.0)
;;              fxb (/ (inc i) 44.0)
;;              fyb (/ (inc j) 44.0)
;;              tex-coords [[fx fy] [fx fyb] [fxb fyb] fxb fy]
;;              x   (* i 45)
;;              y   j
;;              vertices [(nth points (+ x y))
;;                        (nth points (+ x (inc y)))
;;                        (nth points (+ (inc x) (inc y)))
;;                        (nth points (+ (inc x) y))]]
;;          (map tex-coord-and-vertex (interleave tex-coords vertices))))))
  (color 1 0 0 1)
  (draw-quads
   (vector -1 -1 0)
   (vector 1 -1 0)
   (vector 1 1 0)
   (vector -1 1 0))
  (app/repaint!))

(defn display-proxy [& args]
  (apply display args))

(def options {:reshape reshape
              :update update
              :display display-proxy
              :init init})

(defn start []
  (app/start options {}))

(start)