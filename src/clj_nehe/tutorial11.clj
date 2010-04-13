(ns clj-nehe.tutorial11
  (:use [penumbra opengl geometry]
        [penumbra.opengl.texture :only [gl-tex-coord-2]]
        [penumbra.opengl.core :only [gl-import]]
        [clojure.contrib.duck-streams :only [pwd]]
        [clj-nehe.utils])
  (:require [penumbra.app :as app]))

;; -----------------------------------------------------------------------------
;; Vars

(def *image-path* (str (pwd) "/src/clj_nehe/Tim.bmp"))
(def *width* 640)
(def *height* 480)

(defn gen-points [n]
  (let [n (int n)]
   (into []
         (for [x (range 45) y (range 45)]
           (let [x (float x)
                 y (float y)]
             [(prim-float - (prim-float / x 5.0) 4.5)
              (prim-float - (prim-float / y 5.0) 4.5)
              (Math/sin
               (prim-float *
                           (prim-float /
                                       (prim-float *
                                                   (prim-float / (mod (int (+ x (float n))) (int 45)) 5.0)
                                                   40.0)
                                       360.0)
                           (prim-float * Math/PI 2.0)))])))))

(defn points []
  ((fn points* [n]
     (lazy-seq
      (cons (gen-points n) (points* (inc n))))) 0))

(defn tex-points [n]
  (into [] (for [x (range n) y (range n)]
             [(/ x (float n)) (/ y (float n))])))

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
  (merge state 
         {:fullscreen false
          :xrot 0
          :yrot 0
          :zrot 0
          :points (points)
          :tex-points (tex-points 45)
          :texture (load-texture-from-file *image-path*)}))

(defn reshape [[x y width height] state]
  (viewport 0 0 width height)
  (frustum-view 45 (prim-float / width height) 0.1 100)
  (load-identity)
  state)

(defn update [[delta time] state]
  (-> state
      (update-in [:xrot] (fn [xr] (prim-float + xr 0.3)))
      (update-in [:yrot] (fn [yr] (prim-float + yr 0.2)))
      (update-in [:zrot] (fn [zr] (prim-float + zr 0.4)))
      (update-in [:points] next)))

(defn key-press [key state]
  (condp = key
    :f1 (let [state (update-in state [:fullscreen] #(not %))]
          (app/fullscreen! (:fullscreen state))
          state)
    state))

(defn display [[delta time] state]
  (translate 0 0 -12)
  (rotate (:xrot state) 1 0 0)
  (rotate (:yrot state) 0 1 0)
  (rotate (:zrot state) 0 0 1)
  (with-texture (:texture state)
    (draw-quads
     (let [tex-points (:tex-points state)
           points (first (:points state))]
       (doseq [i (range 0 1980 45) j (range 44)]
         (let [i  (int i)
               j  (int j)
               tl (+ i j)
               bl (+ i (int (inc j)))
               br (prim-int + i 45 (inc j))
               tr (prim-int + i 45 j)
               vs [[(tex-points tl) (points tl)]
                   [(tex-points bl) (points bl)]
                   [(tex-points br) (points br)]
                   [(tex-points tr) (points tr)]]]
           (doall (map tex-coord-and-vertex vs)))))))
  (app/repaint!))

(defn display-proxy [& args]
  (apply display args))

(def options {:reshape reshape
              :update update
              :key-press key-press
              :display display-proxy
              :init init})

(defn start []
  (app/start options {}))

(start)