(ns clj-nehe.tutorial19
  (:use [penumbra opengl geometry]
        [penumbra.opengl.core :only [gl-import]])
  (:require [penumbra.app :as app]))

;; -----------------------------------------------------------------------------
;; Vars

(def *max-particles* 1000)
(def *width* 640)
(def *height* 480)

(def *colors* 
	[[1 0.5 0.5] [1 0.75 0.5] [1 1 0.5] [0.75 1 0.5] 
         [0.5 1 0.5] [0.5 1 0.75] [0.5 1 1] [0.5 0.75 1] 
         [0.5 0.5 1] [0.75 0.5 1] [1 0.5 1] [1 0.5 0.75]])

(def *particles*
     (for [x (range *max-particles*)]
       (let [[r g b] (*colors* (int (* x (/ 12 *max-particles*))))]
        {:active true
         :life 1.0
         :fade (+ (/ (rand-int 100) 1000.0) 0.003)
         :r r
         :g g
         :b b
         :x 0.0
         :y 0.0
         :z 0.0
         :xi (* (- (rand-int 50) 25.0) 10.0)
         :yi (* (- (rand-int 50) 25.0) 10.0)
         :zi (* (- (rand-int 50) 25.0) 10.0)
         :xg 0.0
         :yg -0.8
         :zg 0.0})))

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
  (app/title! "Nehe Tutorial 19")
  (app/vsync! false)
  (app/display-mode! *width* *height*)
  (shade-model :smooth)
  (clear-color 0 0 0 0.5)
  (clear-depth 1)
  (enable :depth-test)
  (depth-test :lequal)
  (hint :perspective-correction-hint :nicest)
  (merge state
         {:fullscreen false
          :rainbow true
          :sp false
          :rp false
          :slowdown 2.0
          :xspeed 0
          :yspeed 0
          :zoom -40.0}))

(defn reshape [[x y width height] state]
  (viewport 0 0 *width* *height*)
  (frustum-view 45 (/ (double *width*) *height*) 0.1 100)
  (load-identity)
  state)

(defn key-press [key state]
  (condp = key
    :f1 (let [state (update-in state [:fullscreen] #(not %))]
          (app/fullscreen! (:fullscreen state))
          state)
    state))

(defn display [[delta time] state]
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
              :key-press key-press
              :display display-proxy
              :init init})

(defn start []
  (app/start options {}))

(start)