(ns clj-nehe.tutorial2
  (:use [penumbra opengl geometry]
        [penumbra.opengl.core :only [gl-import]])
  (:require [penumbra.app :as app]))

;; -----------------------------------------------------------------------------
;; Vars

(def *width* 640)

(def *height* 480)

(def *tri* [[0 1 0]
            [-1 -1 0]
            [1 -1 0]])

(def *quad* [[-1 1 0]
             [1 1 0]
             [1 -1 0]
             [-1 -1 0]])

;; -----------------------------------------------------------------------------
;; Import

(gl-import glClearDepth clear-depth)

;; -----------------------------------------------------------------------------
;; Fns

(defn init [state]
  (app/title! "Nehe Tutorial 2")
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
   (doall (map #(apply vertex %) *tri*)))
  (translate 3 0 0)
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