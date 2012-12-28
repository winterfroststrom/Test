(ns Test.game.basic
  (:require [Test.io :as io]))

(defn type-dispatch [x] (get-in x [:type]))

(defmulti init-state! (fn [_ state] state))

(defmulti tile-action! (fn [params tile instance] (type-dispatch tile)))

(defmulti make-enemy (fn [enemies spawns] (type-dispatch spawns)))

(defmethod make-enemy :probabilities [enemies spawns]
  (let [probabilities (spawns :probabilities)
        sum (reduce + probabilities)
        place (rand sum)]
    (loop [place place index 0]
      (let [probability (probabilities index)]
        (if (> probability place)
          (enemies index)
          (recur (- place probability) (inc index)))))))

(defn make-player []
  (io/load-resource :save1))

(defn save-player [params]
  (io/save-resource :save1 @(params :player)))

(defn load-tiles! [params file]
  (dosync
    (ref-set (params :tile-types) (file :tile-types))
    (ref-set (params :tile-renders) (file :tile-renders))))

(defn load-enemies! [params file]
  (dosync
    (ref-set (params :enemies) (file :enemies))
    (ref-set (params :spawns) (file :spawns))))

(defn load-map! [params file-name]
  (dosync
    (let [file1 (io/load-resource file-name)]
      (commute (params :player) assoc :map file-name)
      (load-tiles! params file1)
      (ref-set (params :game-map) (file1 :tiles))
      (load-enemies! params file1))))

(defn clear! [params]
  (dosync
    (ref-set (params :player) nil)
    (ref-set (params :state) :start)
    (ref-set (params :enemies) nil)
    (ref-set (params :enemy) nil)
    (ref-set (params :spawns) nil)
    (ref-set (params :game-map) nil)
    (ref-set (params :battle-state) nil)
    (ref-set (params :tile-types) nil)))

(defn init-battle! [params]
  (dosync
    (ref-set (params :state) :battle)
    (ref-set (params :enemy) (make-enemy @(params :enemies) @(params :spawns)))
    (ref-set (params :battle-state) {:turn 0})))

(defn init-world! [params]
  (let [player (make-player)]
    (dosync
      (ref-set (params :player) player)
      (load-map! params (player :map))
      (ref-set (params :state) :world)
      (ref-set (params :battle-state) nil))))

(defmethod init-state! :battle [params state]
  (init-battle! params))

(defmethod init-state! :world [params state]
  (init-world! params))

