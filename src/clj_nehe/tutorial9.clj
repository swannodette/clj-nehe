(ns clj-nehe.tutorial9
  (:use [penumbra opengl]
        [penumbra.opengl.core :only [gl-import]])
  (:use [clojure.contrib.io :only [pwd]]
        [clojure.contrib.seq :only [indexed]])
  (:require [penumbra.app :as app])
  (:import [javax.imageio ImageIO]
           [java.io File]))

;; -----------------------------------------------------------------------------
;; Vars

(def image-path (str (pwd) "/src/clj_nehe/Star.bmp"))
(def image (ImageIO/read (File. (pwd) "/src/clj_nehe/Star.bmp")))
(def app-width 640)
(def app-height 480)
(def num-stars 50)
(def the-stars
     (for [x (range num-stars)]
       {:angle 0
        :dist (* (/ x num-stars) 5)
        :r (rand-int 255)
        :g (rand-int 255)
        :b (rand-int 255)}))

(def vertices
     [[0 0] [-1 -1 0]
      [1 0] [1 -1 0]
      [1 1] [1 1 0]
      [0 1] [-1 1 0]])

(defn color-byte [r g b a]
  (let [r (/ r 255.0)
        g (/ g 255.0)
        b (/ b 255.0)]
    (color r g b (/ a 255.0))))

;; -----------------------------------------------------------------------------
;; Import

(gl-import glClearDepth clear-depth)

;; -----------------------------------------------------------------------------
;; Fns

(defn init [state]
  (app/title! "Nehe Tutorial 9")
  (app/vsync! false)
  (app/display-mode! app-width app-height)
  (enable :texture-2d)
  (shade-model :smooth)
  (clear-color 0 0 0 0.5)
  (clear-depth 1)
  (hint :perspective-correction-hint :nicest)
  (blend-func :src-alpha :one)
  (enable :blend)
  (merge state
         {:fullscreen false
          :twinkle false
          :zoom -15
          :tilt 90
          :spin 0
          :texture (load-texture-from-image image)
          :stars the-stars}))

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
   (update-in [:dist] #(- % 0.01))
   (reset-star)))

(defn update-stars [stars]
  (let [c (count stars)]
    (map #(update-star % c) (indexed stars))))

(defn update [[delta time] {stars :start :as state}]
  (let [state (if (state :zoom-in)
                (update-in state [:zoom] #(+ % 0.02))
                state)
        state (if (state :zoom-out)
                (update-in state [:zoom] #(- % 0.02))
                state)
        state (if (state :tilt-up)
                (update-in state [:tilt] #(+ % 0.05))
                state)
        state (if (state :tilt-down)
                (update-in state [:tilt] #(- % 0.05))
                state)]
   (-> state
       (update-in [:stars] update-stars)
       (update-in [:spin] #(+ % (* 0.01 (count stars)))))))

(defn key-press [key state]
  (condp = key
    :f1    (let [state (update-in state [:fullscreen] #(not %))]
             (app/fullscreen! (:fullscreen state))
             state)
    "t"    (update-in state [:twinkle] #(not %))
    "w"    (assoc state :zoom-in true)
    "s"    (assoc state :zoom-out true)
    :up    (assoc state :tilt-up true)
    :down  (assoc state :tilt-down true)
    state))

(defn key-release [key state]
  (condp = key
    "w"    (dissoc state :zoom-in)
    "s"    (dissoc state :zoom-out)
    :up    (dissoc state :tilt-up)
    :down  (dissoc state :tilt-down)
    state))

(defn display [[delta time] state]
  (with-texture (:texture state)
    (let [stars (:stars state)
          c     (count stars)]
      (doseq [[i star] (indexed stars)]
        (push-matrix
         (translate 0 0 (:zoom state))
         (rotate (:tilt state) 1 0 0)
         (rotate (:angle star) 0 1 0)
         (translate (:dist star) 0 0)
         (rotate (- (:angle star)) 0 1 0)
         (rotate (- (:tilt state)) 1 0 0)
         (if (:twinkle state)
           (let [n (mod (dec i) c)
                 {r :r g :g b :b} (stars n)]
             (color-byte r g b 255)
             (draw-quads
              (doseq [[[tx ty] [vx vy vz]] (partition 2 vertices)]
                (texture tx ty)
                (vertex vx vy vz)))))
         (rotate (+ (* i 0.01) (:spin state)) 0 0 1)
         (color-byte (:r star) (:g star) (:b star) 255)
         (draw-quads
          (doseq [[[tx ty] [vx vy vz]] (partition 2 vertices)]
            (texture tx ty)
            (vertex vx vy vz)))))))
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