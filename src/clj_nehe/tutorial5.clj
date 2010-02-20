(ns clj-nehe.tutorial5
  (:use [penumbra opengl geometry]
        [penumbra.opengl.core :only [gl-import]])
  (:require [penumbra.app :as app]))

;; -----------------------------------------------------------------------------
;; Vars

(def *width* 640)

(def *height* 480)

(def *pyramid* 
     [
      ; front face
      [1 0 0]     ; red
      [0 1 0]     ; top
      [0 1 0]     ; green
      [-1 -1 1]   ; left
      [0 0 1]     ; blue
      [1 -1 1]    ; right

      ; right face
      [1 0 0]     ; red
      [0 1 0]     ; top
      [0 0 1]     ; blue
      [1 -1 1]    ; left
      [0 1 0]     ; green
      [1 -1 -1]   ; right

      ; back face
      [1 0 0]     ; red
      [0 1 0]     ; top
      [0 1 0]     ; green
      [1 -1 -1]   ; left
      [0 0 1]     ; blue
      [-1 -1 -1]  ; right

      ; left face
      [1 0 0]     ; red
      [0 1 0]     ; top
      [0 0 1]     ; blue
      [-1 -1 -1]  ; left
      [0 1 0]     ; green
      [-1 -1 1]   ; right
      ]
     )

(def *cube*
     [
      ; front face
      [-1 1 0]
      [1 1 0]
      [1 -1 0]
      [-1 -1 0]

      ; 
      ])

;; -----------------------------------------------------------------------------
;; Helpers

(defmacro series [& args]
  (let [syms (take (count args) (repeatedly gensym))
        forms (map #(cons 'apply %) (partition 2 (interleave args syms)))]
   `(fn [[~@syms]]
      ~@forms)))

(def color-and-vertex (series color vertex))
(def color-and-4vertices (series color vertex vertex vertex vertex))

;; -----------------------------------------------------------------------------
;; Import

(gl-import glClearDepth clear-depth)

;; -----------------------------------------------------------------------------
;; Fns

(defn init [state]
  (app/title! "Nehe Tutorial 5")
  (app/vsync! false)
  (app/display-mode! *width* *height*)
  (shade-model :smooth)
  (clear-color 0 0 0 0.5)
  (clear-depth 1)
  (enable :depth-test)
  (depth-test :lequal)
  (hint :perspective-correction-hint :nicest)
  (-> state
      (assoc :rpyramid 0)
      (assoc :rcube 0)))

(defn reshape [[x y width height] state]
  (viewport 0 0 *width* *height*)
  (frustum-view 45 (/ (double *width*) *height*) 0.1 100)
  (load-identity)
  state)

(defn update [[delta time] state]
   (-> state
       (update-in [:rpyramid] #(+ % 0.2))
       (update-in [:rcube] #(+ % 0.15))))

(defn display [[delta time] state]
  (clear)
  (load-identity)
  (translate -1.5 0 -6)
  (rotate (:rpyramid state) 0 1 0)
  (draw-triangles
   (doall
    (map color-and-vertex (partition 2 *pyramid*))))
  (load-identity)
  (translate -1.5 0 -6)
  (translate 3 0 0)
  (rotate (:rcube state) 1 0 0)
  (color 0.5 0.5 1)
  (draw-quads
   (doall (map #(apply vertex %) *cube*)))
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