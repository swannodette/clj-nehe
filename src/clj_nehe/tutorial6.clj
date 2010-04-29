(ns clj-nehe.tutorial6
  (:use [penumbra opengl]
        [penumbra.opengl.core :only [gl-import]])
  (:use [clojure.contrib.io :only [pwd]])
  (:require [penumbra.app :as app]))

;; -----------------------------------------------------------------------------
;; Vars

(def *image-path* (str (pwd) "/src/clj_nehe/NeHe.bmp"))
(def *width* 640)
(def *height* 480)

(def *cube*
     [
      ; front face
      [0 0] [-1 -1 1]   ; bottom left of the texture and quad
      [1 0] [1 -1 1]    ; bottom right of the texture and quad
      [1 1] [1 1 1]     ; top right of the texture and quad
      [0 1] [-1 1 1]    ; top left of the texture and quad

      ; back face
      [1 0] [-1 -1 -1]  ; bottom right of the texture and quad
      [1 1] [-1 1 -1]   ; top right of the texture and quad
      [0 1] [1 1 -1]    ; top left of the texture and quad
      [0 0] [1 -1 -1]   ; bottom left of the texture and quad

      ; top face
      [0 1] [-1 1 -1]   ; top left of the texture and quad
      [0 0] [-1 1 1]    ; bottom left of the texture and quad
      [1 0] [1 1 1]     ; bottom right of the texture and quad
      [1 1] [1 1 -1]    ; top right of the texture and quad

      ; bottom face
      [1 1] [-1 -1 -1]  ; top right of the texture and quad
      [0 1] [1 -1 -1]   ; top left of the texture and quad
      [0 0] [1 -1 1]    ; bottom left of the texture and quad
      [1 0] [-1 -1 1]   ; bottom right of the texture and quad

      ; right face
      [1 0] [1 -1 -1]   ; bottom right of the texture and quad
      [1 1] [1 1 -1]    ; top right of the texture and quad
      [0 1] [1 1 1]     ; top left of the texture and quad
      [0 0] [1 -1 1]    ; bottom left of the texture and quad

      ; left face
      [0 0] [-1 -1 -1]  ; bottom left of the texture and quad
      [1 0] [-1 -1 1]   ; bottom right of the texture and quad
      [1 1] [-1 1 1]    ; top right of the texture and quad
      [0 1] [-1 1 -1]   ; top left of the texture and quad
      ])

;; -----------------------------------------------------------------------------
;; Helpers
(defmacro series [& args]
  (let [syms (take (count args) (repeatedly gensym))
        forms (map #(cons 'apply %) (partition 2 (interleave args syms)))]
   `(fn [[~@syms]]
      ~@forms)))

(def texture-and-vertex (series texture vertex))

;; -----------------------------------------------------------------------------
;; Import

(gl-import glClearDepth clear-depth)

;; -----------------------------------------------------------------------------
;; Fns

(defn init [state]
  (app/title! "Nehe Tutorial 6")
  (app/vsync! false)
  (app/display-mode! *width* *height*)
  (enable :texture-2d)
  (shade-model :smooth)
  (clear-color 0 0 0 0.5)
  (clear-depth 1)
  (enable :depth-test)
  (depth-test :lequal)
  (hint :perspective-correction-hint :nicest)
  (merge state
         {:fullscreen false
          :xrot 0
          :yrot 0
          :zrot 0
          :texture (load-texture-from-file *image-path*)}))

(defn reshape [[x y width height] state]
  (println "reshape")
  (viewport 0 0 *width* *height*)
  (frustum-view 45 (/ (double *width*) *height*) 0.1 100)
  (load-identity)
  state)

(defn update [[delta time] state]
   (-> state
       (update-in [:xrot] #(+ % 0.3))
       (update-in [:yrot] #(+ % 0.2))
       (update-in [:zrot] #(+ % 0.4))))

(defn key-press [key state]
  (condp = key
    :f1 (let [state (update-in state [:fullscreen] #(not %))]
          (app/fullscreen! (:fullscreen state))
          state)
    state))

(defn display [[delta time] state]
  (translate 0 0 -5)
  (rotate (:xrot state) 1 0 0)
  (rotate (:yrot state) 0 1 0)
  (rotate (:zrot state) 0 0 1)
  (with-texture (:texture state)
    (draw-quads
     (doall
      (map texture-and-vertex (partition 2 *cube*)))))
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