(ns clj-nehe.tutorial19
  (:use [penumbra opengl geometry]
        [penumbra.opengl.texture :only [gl-tex-coord-2]]
        [penumbra.opengl.core :only [gl-import]]
        [clojure.contrib.duck-streams :only [pwd]]
        [clojure.contrib.seq-utils :only [indexed]])
  (:require [penumbra.app :as app])
  (:import [javax.imageio ImageIO]
           [java.io File]))

;; -----------------------------------------------------------------------------
;; Vars

(def *image* (ImageIO/read (File. (pwd) "/src/clj_nehe/Crate.bmp")))
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

(defmacro += [m ks form]
  `(update-in ~m [~ks] (fn [n#] (+ n# ~form))))

;; -----------------------------------------------------------------------------
;; Fns

(defn init [state]
  (app/title! "Nehe Tutorial 19")
  (app/vsync! false)
  (app/display-mode! *width* *height*)
  (shade-model :smooth)
  (clear-color 0 0 0 0.5)
  (clear-depth 1)
  (disable :depth-test)
  (enable :blend)
  (blend-func :src-alpha :one)
  (hint :perspective-correction-hint :nicest)
  (enable :texture-2d)
  (merge state
         {:fullscreen false
          :rainbow    true
          :col        0
          :sp         false
          :rp         false
          :slowdown   2.0
          :xspeed     0
          :yspeed     0
          :zoom       -40.0
          :delay      0
          :particles  *particles*
          :texture    (load-texture-from-image *image*)}))

(defn reshape [[x y width height] state]
  (viewport 0 0 *width* *height*)
  (frustum-view 45 (/ (double *width*) *height*) 0.1 200)
  (load-identity)
  state)

(defn key-press [key state]
  (condp = key
    :f1 (let [state (update-in state [:fullscreen] #(not %))]
          (app/fullscreen! (:fullscreen state))
          state)
    state))

(defn update-particle [particle {:keys [slowdown xspeed yspeed] :as state}]
  (let [{:keys [xi yi zi xg yg zg life fade]} particle
        active (< (- life fade) 0.0)]
    (if active
     (-> particle
         (+= :x (/ xi slowdown))
         (+= :y (/ yi slowdown))
         (+= :z (/ zi slowdown))
         (+= :xi xg)
         (+= :yi yg)
         (+= :zi zg)
         (+= :life fade))
     (merge particle
            {:life  0.0
             :fade  (+ (/ (rand-int 100) 1000.0) 0.003)
             :x     0.0
             :y     0.0
             :z     0.0
             :xi    (- (+ xspeed (rand-int 60)) 32.0)
             :yi    (- (+ yspeed (rand-int 60)) 30.0)
             :zi    (- (rand-int 60) 30.0)}))))

(defn update [[delta time] state]
  (-> state
      (update-in [:particles] #(map (fn [p] (update-particle p state)) %))
      (update-in [:col] #(mod (inc %) 11))))

(defn display [[delta time] {zoom :zoom particles :partcles :as state}]
  (doseq [{x :x y :y z :z
           r :r g :g b :b
           life :life
           :as particle} particles]
    (if (:active particle)
      (color r g b life)
      (let [z (+ z zoom)]
       (draw-triangle-strip
        (gl-tex-coord-2 1 1) (vertex (+ x 0.5) (+ y 0.5) z)
        (gl-tex-coord-2 0 1) (vertex (- x 0.5) (+ y 0.5) z)
        (gl-tex-coord-2 1 0) (vertex (+ x 0.5) (- y 0.5) z)
        (gl-tex-coord-2 0 0) (vertex (- x 0.5) (- y 0.5) z))))))

(defn display-proxy [& args]
  (apply display args))

(def options {:reshape reshape
              :key-press key-press
              :display display-proxy
              :init init})

(defn start []
  (app/start options {}))

(start)