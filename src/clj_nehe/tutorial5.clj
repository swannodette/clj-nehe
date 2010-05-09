(ns clj-nehe.tutorial5
  (:use [penumbra opengl]
        [penumbra.opengl.core :only [gl-import]])
  (:require [penumbra.app :as app]))

;; -----------------------------------------------------------------------------
;; Vars

(def app-width 640)
(def app-height 480)

(def pyramid 
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

(def cube
     [
      ; top face
      [0 1 0]     ; green
      [1 1 -1]    ; top right
      [-1 1 -1]   ; top left
      [-1 1 1]    ; bottom left
      [1 1 1]     ; bottom right

      ; bottom
      [0 1 0]     ; green
      [1 -1 1]    ; top right
      [-1 -1 1]   ; top left
      [-1 -1 -1]  ; bottom left
      [1 -1 -1]   ; bottom right

      ; front
      [1 0 0]     ; red
      [1 1 1]     ; top right
      [-1 1 1]    ; top left
      [-1 -1 1]   ; bottom left
      [1 -1 1]    ; bottom right
      
      ; back
      [1 1 0]     ; yellow
      [1 -1 -1]   ; bottom left
      [-1 -1 -1]  ; bottom right
      [-1 1 -1]   ; top right
      [1 1 -1]    ; top left

      ; left
      [0 0 1]     ; blue
      [-1 1 1]    ; top right
      [-1 1 -1]   ; top left
      [-1 -1 -1]  ; bottom left
      [-1 -1 1]   ; bottom right

      ; right
      [1 0 1]     ; violet
      [1 1 -1]    ; top right
      [1 1 1]     ; top left
      [1 -1 1]    ; bottom left
      [1 -1 -1]   ; bottom right
      ])

;; -----------------------------------------------------------------------------
;; Import

(gl-import glClearDepth clear-depth)

;; -----------------------------------------------------------------------------
;; Fns

(defn init [state]
  (app/title! "Nehe Tutorial 5")
  (app/vsync! false)
  (app/display-mode! app-width app-height)
  (shade-model :smooth)
  (clear-color 0 0 0 0.5)
  (clear-depth 1)
  (enable :depth-test)
  (depth-test :lequal)
  (hint :perspective-correction-hint :nicest)
  (-> state
      (assoc :fullscreen false)
      (assoc :rpyramid 0)
      (assoc :rcube 0)))

(defn reshape [[x y width height] state]
  (viewport 0 0 app-width app-height)
  (frustum-view 45 (/ (double app-width) app-height) 0.1 100)
  (load-identity)
  state)

(defn update [[delta time] state]
   (-> state
       (update-in [:rpyramid] #(+ % 0.2))
       (update-in [:rcube] #(+ % 0.15))))

(defn key-press [key state]
  (condp = key
    :f1 (let [state (update-in state [:fullscreen] #(not %))]
          (app/fullscreen! (:fullscreen state))
          state)
    state))

(defn display [[delta time] state]
  (translate -1.5 0 -6)
  (rotate (:rpyramid state) 0 1 0)
  (draw-triangles
   (doseq [[[r g b] [x y z]] (partition 2 pyramid)]
     (color r g b)
     (vertex x y z)))
  (load-identity)
  (translate -1.5 0 -7)
  (translate 3 0 0)
  (rotate (:rcube state) 1 1 1)
  (color 0.5 0.5 1)
  (draw-quads
   (doseq [[[r g b]
            [v1x v1y v1z]
            [v2x v2y v2z]
            [v3x v3y v3z]
            [v4x v4y v4z]] (partition 5 cube)] 
     (color r g b)
     (vertex v1x v1y v1z)
     (vertex v2x v2y v2z)
     (vertex v3x v3y v3z)
     (vertex v4x v4y v4z)))
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