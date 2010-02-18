(ns clj-nehe.tutorial1
  (:use [penumbra opengl geometry]
        [penumbra.opengl.core :only [gl-import]])
  (:require [penumbra.app :as app]
            [penumbra.text :as text]))

;; -----------------------------------------------------------------------------
;; Vars

(def *width* 640.0)

(def *height* 480.0)

(def *tri* [[0.0 1.0 0.0]
            [-1.0 -1.0 0.0]
            [1.0 -1.0 0.0]])

(def *sqr* [[-1.0 1.0 0.0]
            [1.0 1.0 0.0]
            [1.0 -1.0 0.0]
            [-1.0 -1.0 0.0]])

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
  (clear-color 0.0 0.0 0.0 0.5)
  (clear-depth 1.0)
  (enable :depth-test)
  (depth-test :lequal)
  (hint :perspective-correction-hint :nicest)
  state)

(defn reshape [[x y width height] state]
  (viewport 0 0 *width* *height*)
  (frustum-view 45.0 (/ *width* *height*) 0.1 100.0)
  (gl-load-identity-matrix))

(defn display [[delta time] state]
  (clear)
  (gl-load-identity-matrix)
  (translate -1.5 0.0 -6.0)
  (draw-triangles
   (doall (map #(apply vertex %) *tri*)))
  (translate 3.0 0.0 0.0)
  (draw-quads
   (doall (map #(apply vertex %) *sqr*)))
  (app/repaint!))

(defn display-proxy [& args]
  (apply display args))

(def options {:reshape reshape
              :display display-proxy
              :init init})

(defn start []
  (app/start options {}))

(start)