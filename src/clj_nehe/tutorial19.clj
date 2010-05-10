(ns clj-nehe.tutorial19
  (:use [penumbra opengl]
        [penumbra.opengl.core :only [gl-import]]
        [clj-nehe.utils :only [prim]]
        [clojure.contrib.io :only [pwd]]
        [clojure.contrib.seq :only [indexed]])
  (:require [penumbra.app :as app])
  (:import [javax.imageio ImageIO]
           [java.io File]))

;; -----------------------------------------------------------------------------
;; Vars

(def image-path (ImageIO/read (File. (pwd) "/src/clj_nehe/Particle.bmp")))
(def max-particles 5)
(def app-width 640)
(def app-height 480)

(def colors 
	[[1 0.5 0.5] [1 0.75 0.5] [1 1 0.5] [0.75 1 0.5] 
         [0.5 1 0.5] [0.5 1 0.75] [0.5 1 1] [0.5 0.75 1] 
         [0.5 0.5 1] [0.75 0.5 1] [1 0.5 1] [1 0.5 0.75]])

(def particles
     (into []
           (for [x (range max-particles)]
             (let [[r g b] (colors (int (* x (/ 12 max-particles))))]
               {:active true
                :life   1.0
                :fade   (+ (/ (rand-int 100) 1000.0) 0.003)
                :r      r
                :g      g
                :b      b
                :x      0.0
                :y      0.0
                :z      0.0
                :xi     (* (- (rand-int 50) 25.0) 10.0)
                :yi     (* (- (rand-int 50) 25.0) 10.0)
                :zi     (* (- (rand-int 50) 25.0) 10.0)
                :xg     0.0
                :yg     -0.8
                :zg     0.0}))))

(def tri [[0 1 0]
            [-1 -1 0]
            [1 -1 0]])

(def quad [[-1 1 0]
             [1 1 0]
             [1 -1 0]
             [-1 -1 0]])

(def initial-state
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
      :particles  particles})

;; -----------------------------------------------------------------------------
;; Import

(gl-import glClearDepth clear-depth)

(defmacro += [m ks form]
  `(update-in ~m [~ks] (fn [n#] (+ n# ~form))))

(defmacro -= [m ks form]
  `(update-in ~m [~ks] (fn [n#] (- n# ~form))))

;; -----------------------------------------------------------------------------
;; Fns

(defn init [state]
  (app/title! "Nehe Tutorial 19")
  (app/vsync! false)
  (app/display-mode! app-width app-height)
  (shade-model :smooth)
  (clear-color 0 0 0 0.5)
  (clear-depth 1)
  (disable :depth-test)
  (enable :blend)
  (blend-func :src-alpha :one)
  (hint :perspective-correction-hint :nicest)
  (hint :point-smooth-hint :nicest)
  (enable :texture-2d)
  (merge state initial-state {:texture (load-texture-from-image image-path)}))

(defn reshape [[x y width height] state]
  (viewport 0 0 app-width app-height)
  (frustum-view 45 (/ (double app-width) app-height) 0.1 200)
  (load-identity)
  state)

(defn key-press [key state]
  (condp = key
    :f1 (let [state (update-in state [:fullscreen] #(not %))]
          (app/fullscreen! (:fullscreen state))
          state)
    state))

(defn update-particle [particle {:keys [slowdown xspeed yspeed col] :as state}]
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
         (-= :life fade))
     (merge particle
            {:life  0.0
             :fade  (prim double (+ (/ (rand-int 100) 1000.0) 0.003))
             :r     ((colors col) 0)
             :g     ((colors col) 1)
             :b     ((colors col) 2)
             :x     0.0
             :y     0.0
             :z     0.0
             :xi    (prim double (- (+ xspeed (rand-int 60)) 32.0))
             :yi    (prim double (- (+ yspeed (rand-int 60)) 30.0))
             :zi    (prim double (- (rand-int 60) 30.0))}))))

(defn update [[delta time] state]
  (-> state
      (update-in [:particles] #(map (fn [p] (update-particle p state)) %))
      (update-in [:col] #(mod (inc %) 12))))

(defn display [[delta time] {:keys [particles zoom] :as state}]
  (doseq [{:keys [x y z r g b life active]} particles]
    (if active
      (do
        (color r g b life)
        (let [x    (double x)
              y    (double y)
              z    (+ (double z) (double zoom))
              half (double 0.5)]
          (draw-triangle-strip
           (texture 1 1) (vertex (+ x half) (+ y half) z)
           (texture 0 1) (vertex (- x half) (+ y half) z)
           (texture 1 0) (vertex (+ x half) (- y half) z)
           (texture 0 0) (vertex (- x half) (- y half) z)))))))

(defn display-proxy [& args]
  (apply display args))

(def options {:reshape reshape
              :key-press key-press
              :display display-proxy
              :init init})

(defn start []
  (app/start options {}))