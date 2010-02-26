(ns clj-nehe.tutorial8
  (:use [penumbra opengl geometry]
        [penumbra.opengl.texture :only [gl-tex-coord-2]]
        [penumbra.opengl.core :only [gl-import]])
  (:use [clojure.contrib.duck-streams :only [pwd]]
        [clojure.contrib.seq-utils :only [indexed]])
  (:require [penumbra.app :as app])
  (:import [javax.imageio ImageIO]
           [java.io File]))

;; -----------------------------------------------------------------------------
;; Vars

(def *image-path* (str (pwd) "/src/clj_nehe/Star.bmp"))
(def *width* 640)
(def *height* 480)
(def *num* 50)
(def *stars*
     (for [x (range *num*)]
       {:angle 0
        :dist (* x 5)
        :r (rand-int 255)
        :g (rand-int 255)
        :b (rand-int 255)}))

(def *vertices*
     [
      [0 0] [-1 -1 0]
      [1 0] [1 -1 0]
      [1 1] [1 1 0]
      [0 1] [-1 1 0]
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

(def normal-and-4tex-coord-and-vertices
     (series tex-coord vertex))

(defn color-byte [r g b a]
  (color (/ r 255.0) (/ g 255.0) (/ b 255.0) (/ a 255.0)))

;; -----------------------------------------------------------------------------
;; Import

(gl-import glClearDepth clear-depth)

;; -----------------------------------------------------------------------------
;; Fns

(defn init [state]
  (app/title! "Nehe Tutorial 8")
  (app/vsync! false)
  (app/display-mode! *width* *height*)
  (enable :texture-2d)
  (shade-model :smooth)
  (clear-color 0 0 0 0.5)
  (clear-depth 1)
  (enable :blend)
  (hint :perspective-correction-hint :nicest)
  (blend-func :src-alpha :one)
  (merge state
         {:fullscreen false
          :twinkle false
          :zoom -15
          :tilt 90
          :spin 0
          :texture (load-texture-from-file *image-path*)
          :stars *stars*}))

(defn reshape [[x y width height] state]
  (viewport 0 0 width height)
  (frustum-view 45 (/ (double width) height) 0.1 100)
  (load-identity)
  state)

(defn reset-star [{dist :dist :as star}]
  (if (< dist 0.0)
    (-> star
     (update-in [:dist] #(+ % 5.0))
     (assoc :r (rand-int 255))
     (assoc :g (rand-int 255))
     (assoc :b (rand-int 255)))
    star))

(defn update-star [[i star] n]
  (-> star
   (update-in [:angle] #(+ % (/ (double i) n)))
   (update-in [:dist] #(- % 0.1))
   (reset-star)))

(defn update-stars [stars]
  (let [c (count stars)]
   (map #(update-star % c) (indexed stars))))

(defn update [[delta time] state]
  (-> state
    (update-in [:stars] update-stars)
    (update-in [:spin] #(+ % 0.1))))

(defn key-press [key state]
  (condp = key
    state))

(defn key-release [key state]
  (condp = key
    state))

(defn display [[delta time] state]
  (with-texture ((:texs state) (:filter state))
    (doseq [[i star] (indexed (:stars state))]
     (push-matrix
      (translate 0 0 (:zoom state))
      (rotate (:tilt state) 1 0 0)
      (rotate (:angle star) 0 1 0)
      (translate (:dist star) 0 0)
      (rotate (- (:angle star)) 0 1 0)
      (rotate (- (:tilt star)) 1 0 0)
      (if (:twinkle state)
        (let [n (mod (dec i) *num*)
              {r :r g :g b :b} (*stars* n)]
          (color-byte r g b 1)
          (draw-quads
           (doall (map tex-coord-and-vertex (partition 2 *vertices*))))))
      (rotate (:spin state) 0 0 1)
      (color-byte (:r star) (:g star) (:b star) 1)
      (draw-quads
       (doall (map tex-coord-and-vertex (parititon 2 *vertices*)))))))
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