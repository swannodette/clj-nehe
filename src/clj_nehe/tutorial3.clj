(ns clj-nehe.tutorial3
  (:use [penumbra opengl geometry]
        [penumbra.opengl.core :only [gl-import]])
  (:require [penumbra.app :as app]))

;; -----------------------------------------------------------------------------
;; Vars

(def *width* 640)

(def *height* 480)

(def *tri* [[1 0 0]     ; color, red
            [0 1 0]     ; vertex
            [0 1 0]     ; color, green
            [-1 -1 0]   ; vertex
            [0 0 1]     ; color, blue
            [1 -1 0]]   ; vertex
     )

(def *quad* [[-1 1 0]
            [1 1 0]
            [1 -1 0]
            [-1 -1 0]])

;; -----------------------------------------------------------------------------
;; Helprs

(defn color-and-vertex [[a b]]
  (apply color a)
  (apply vertex b))

;; -----------------------------------------------------------------------------
;; Import

(gl-import glClearDepth clear-depth)

;; -----------------------------------------------------------------------------
;; Fns

(defn init [state]
  (app/title! "Nehe Tutorial 3")
  (app/vsync! false)
  (app/display-mode! *width* *height*)
  (shade-model :smooth)
  (clear-color 0 0 0 0.5)
  (clear-depth 1)
  (enable :depth-test)
  (depth-test :lequal)
  (hint :perspective-correction-hint :nicest)
  state)

(defn reshape [[x y width height] state]
  (viewport 0 0 *width* *height*)
  (frustum-view 45 (/ (double *width*) *height*) 0.1 100)
  (load-identity)
  state)

(defn display [[delta time] state]
  (clear)
  (load-identity)
  (translate -1.5 0 -6)
  (draw-triangles
   (doall
    (map color-and-vertex (partition 2 *tri*))))
  (translate 3 0 0)
  (color 0.5 0.5 1)
  (draw-quads
   (doall (map #(apply vertex %) *quad*)))
  (app/repaint!))

(defn display-proxy [& args]
  (apply display args))

(def options {:reshape reshape
              :display display-proxy
              :init init})

(defn start []
  (app/start options {}))

(start)