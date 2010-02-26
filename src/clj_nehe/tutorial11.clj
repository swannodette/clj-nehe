(ns clj-nehe.tutorial11
  (:use [penumbra opengl geometry]
        [penumbra.opengl.texture :only [gl-tex-coord-2]]
        [penumbra.opengl.core :only [gl-import]])
  (:use [clojure.contrib.duck-streams :only [pwd]]
        [clojure.contrib.seq-utils :only [flatten]])
  (:require [penumbra.app :as app]))

;; -----------------------------------------------------------------------------
;; Vars

(def *image-path* (str (pwd) "/src/clj_nehe/Tim.bmp"))
(def *width* 640)
(def *height* 480)

(def *xys*
     (into []
           (for [x (range 45) y (range 45)]
             [(- (/ x 5.0) 4.5)
              (- (/ y 5.0) 4.5)])))

(def *zs*
     (into []
           (for [x (range 45)]
                (Math/sin (* (/ (* (/ x 5.0) 40.0) 360.0) (* Math/PI 2.0))))))

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

(defn rotatev [v]
     (into [] (concat (rest v) [(first v)])))

;; -----------------------------------------------------------------------------
;; Import

(gl-import glClearDepth clear-depth)

;; -----------------------------------------------------------------------------
;; Fns

(defn init [state]
  (app/title! "Nehe Tutorial 11")
  (app/vsync! true)
  (app/display-mode! *width* *height*)
  (enable :texture-2d)
  (shade-model :smooth)
  (clear-color 0 0 0 0.5)
  (clear-depth 1)
  (enable :depth-test)
  (depth-test :lequal)
  (gl-polygon-mode :back :fill)
  (gl-polygon-mode :front :line)
  (hint :perspective-correction-hint :nicest)
  (-> state
      (assoc :xrot 0)
      (assoc :yrot 0)
      (assoc :zrot 0)
      (assoc :xys *xys*)
      (assoc :zs *zs*)
      (assoc :texture (load-texture-from-file *image-path*))))

(defn reshape [[x y width height] state]
  (viewport 0 0 width height)
  (frustum-view 45 (/ (double width) height) 0.1 100)
  (load-identity)
  state)

(defn update [[delta time] state]
   (-> state
       (update-in [:xrot] #(+ % 0.3))
       (update-in [:yrot] #(+ % 0.2))
       (update-in [:zrot] #(+ % 0.4))
       (update-in [:zs] #(rotatev %))))

(defn display [[delta time] {xys :xys zs :zs :as state}]
  (translate 0 0 -12)
  (rotate (:xrot state) 1 0 0)
  (rotate (:yrot state) 0 1 0)
  (rotate (:zrot state) 0 0 1)
  (with-texture (:texture state)
    (draw-quads
     (let [points (into [] (map conj xys (cycle zs)))]
      (doseq [i (range 44) j (range 44)]
        (let [fx  (/ i 44.0)
              fy  (/ j 44.0)
              fxb (/ (inc i) 44.0)
              fyb (/ (inc j) 44.0)
              tex-coords [[fx fy] [fx fyb] [fxb fyb] [fxb fy]]
              vertices [(nth points (+ (* i 45) j))
                        (nth points (+ (* i 45) (inc j)))
                        (nth points (+ (* (inc i) 45) (inc j)))
                        (nth points (+ (* (inc i) 45) j))]]
          (doall (map tex-coord-and-vertex (partition 2 (interleave tex-coords vertices)))))))))
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