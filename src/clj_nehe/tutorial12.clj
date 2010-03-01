(ns clj-nehe.tutorial12
  (:use [penumbra opengl geometry]
        [penumbra.opengl.texture :only [gl-tex-coord-2]]
        [penumbra.opengl.core :only [gl-import]])
  (:use [clojure.contrib.duck-streams :only [pwd]])
  (:require [penumbra.app :as app]))

;; -----------------------------------------------------------------------------
;; Vars

(def *image-path* (str (pwd) "/src/clj_nehe/Cube.bmp"))
(def *width* 640)
(def *height* 480)

(def *boxcol* [[1 0 0]
               [1 0.5 0]
               [1 1 0]
               [0 1 0]
               [0 1 1]])

(def *topcol* [[0.5 0 0]
               [0.5 0.25 0]
               [0.5 0.5 0]
               [0 0.5 0]
               [0 0.5 0.5]])

(def *list1*
     [
      ; Bottom Face
      [1 1] [-1 -1 -1]  ; Top Right Of The Texture and Quad
      [0 1] [1 -1 -1]   ; Left Of The Texture and Quad
      [0 0] [1 -1 1]    ; Bottom Left Of The Texture and Quad
      [1 0] [-1 -1 1]	; Bottom Right Of The Texture and Quad

      ; Front Face
      [0 0] [-1 -1 1]	; Bottom Left Of The Texture and Quad
      [1 0] [1 -1 1]	; Bottom Right Of The Texture and Quad
      [1 1] [1 1 1]	; Top Right Of The Texture and Quad
      [0 1] [-1 1 1]	; Top Left Of The Texture and Quad

      ; Back Face
      [1 0] [-1 -1 -1]  ; Bottom Right Of The Texture and Quad
      [1 1] [-1 1 -1]	; Top Right Of The Texture and Quad
      [0 1] [1 1 -1]	; Top Left Of The Texture and Quad
      [0 0] [1 -1 -1]	; Bottom Left Of The Texture and Quad

      ; Right Face
      [1 0] [1 -1 -1]   ; Bottom Right Of The Texture and Quad
      [1 1] [1 1 -1]    ; Top Right Of The Texture and Quad
      [0 1] [1 1 1]     ; Top Left Of The Texture and Quad
      [0 0] [1 -1 1]    ; Bottom Left Of The Texture and Quad

      ; Left Face
      [0 0] [-1 -1 -1]  ; Left Of The Texture and Quad
      [1 0] [-1 -1 1]   ; Right Of The Texture and Quad
      [1 1] [-1 1 1]    ; Right Of The Texture and Quad
      [0 1] [-1 1 -1]   ; Left Of The Texture and Quad
      ])

(def *list2*
     [
      ; Top Face
      [0 1] [-1 1 -1] ; Top Left Of The Texture and Quad
      [0 0] [-1 1 1]  ; Bottom Left Of The Texture and Quad
      [1 0] [1 1 1]   ; Bottom Right Of The Texture and Quad
      [1 1] [1 1 -1]  ; Top Right Of The Texture and Quad
      ])

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
  (app/title! "Nehe Tutorial 12")
  (app/vsync! false)
  (app/display-mode! *width* *height*)
  (enable :texture-2d)
  (shade-model :smooth)
  (clear-color 0 0 0 0.5)
  (clear-depth 1)
  (enable :depth-test)
  (depth-test :lequal)
  (hint :perspective-correction-hint :nicest)
  (enable :light0)
  (enable :lighting)
  (enable :color-material)
  (merge state
      {:xrot 0.0
       :yrot 0.0
       :list1 (create-display-list (draw-quads (doall (map tex-coord-and-vertex (partition 2 *list1*)))))
       :list2 (create-display-list (draw-quads (doall (map tex-coord-and-vertex (partition 2 *list2*)))))
       :texture (load-texture-from-file *image-path*)}))

(defn reshape [[x y width height] state]
  (viewport 0 0 *width* *height*)
  (frustum-view 45 (/ (double *width*) *height*) 0.1 100)
  (load-identity)
  state)

(defn key-press [key state]
  (condp = key
    :f1    (let [state (update-in state [:fullscreen] #(not %))]
             (app/fullscreen! (:fullscreen state))
             state)
    :up    (assoc state :rotate-up true)
    :down  (assoc state :rotate-down true)
    :left  (assoc state :rotate-left true)
    :right (assoc state :rotate-right true)
    state))

(defn key-release [key state]
  (condp = key
    :up    (dissoc state :rotate-up)
    :down  (dissoc state :rotate-down)
    :left  (dissoc state :rotate-left)
    :right (dissoc state :rotate-right)
    state))

(defn update [[delta time] state]
  (cond (state :rotate-up) (update-in state [:xrot] #(+ % 0.02))
        (state :rotate-down) (update-in state [:xrot] #(- % 0.02))
        (state :rotate-right) (update-in state [:yrot] #(+ % 0.05))
        (state :rotate-left) (update-in state [:yrot] #(- % 0.05))
        :else state))

(defn display [[delta time] state]
  (with-texture (:texture state)
    (doseq [yloop (range 1 6)]
      (doseq [xloop (range 0 yloop)]
        (load-identity)
        (translate (- (+ 1.4 (* xloop 2.8)) (* yloop 1.4))
                   (- (* (- 6.0 yloop) 2.4) 7.0)
                   -20)
        (rotate (+ (- 45.0 (* 2.0 yloop)) (:xrot state)) 1 0 0)
        (rotate (+ 45.0 (:yrot state)) 0 1 0)
        (apply color (nth *boxcol* (dec yloop)))
        (call-display-list (:list1 state))
        (apply color (nth *topcol* (dec yloop)))
        (call-display-list (:list2 state)))))
  (app/repaint!))

(defn display-proxy [& args]
  (apply display args))

(def options {:reshape reshape
              :update update
              :key-press key-press
              :key-release key-release
              :display display-proxy
              :init init})

(defn start []
  (app/start options {}))

(start)