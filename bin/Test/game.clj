(ns Test.game
  (:require [Test.io :as io]))

(defn type-dispatch [x] (x :type))
(defn target-dispatch [x] (x :target))

(defmulti tile-action! (fn [params tile instance] (tile :type)))

(defn make-player []
  (io/load-resource :save1))

(defn save-player [params]
  (io/save-resource :save1 @(params :player)))

(defmulti make-enemy (fn [table spawns] (type-dispatch spawns)))

(defmethod make-enemy :probabilities [enemies spawns]
  (let [probabilities (spawns :probabilities)
        sum (reduce + probabilities)
        place (rand sum)]
    (loop [place place index 0]
      (let [probability (probabilities index)]
        (if (> probability place)
          (enemies index)
          (recur (- place probability) (inc index)))))))

(defn load-map! [params file]
  (dosync
    (let [file1 (io/load-resource file)]
      (commute (params :player) assoc :map file)
      (ref-set (params :tile-types) (file1 :tile-types))
      (ref-set (params :game-map) (file1 :tiles))
      (ref-set (params :enemies) (file1 :enemies))
      (ref-set (params :spawns) (file1 :spawns)))))

(defn init-world! [params]
  (let [player (make-player)]
    (dosync
      (ref-set (params :player) player)
      (load-map! params (player :map))
      (ref-set (params :state) :world))))

(defn clear! [params]
  (dosync
    (ref-set (params :player) nil)
    (ref-set (params :state) :start)
    (ref-set (params :enemies) nil)
    (ref-set (params :enemy) nil)
    (ref-set (params :spawns) nil)
    (ref-set (params :game-map) nil)
    (ref-set (params :tile-types) nil)))

(defn init-battle! [params]
  (dosync
    (ref-set (params :state) :battle)
    (ref-set (params :enemy) (make-enemy (params :enemies) (params :spawns)))))

(defn try-battle! [params]
  (when (> (rand 20) 15)
    (init-battle! params)))

(defn valid-position? [params x y]
  (let [index (or (get-in @(params :game-map) [y x]) 1)]
    (not (= ((get-in @(params :tile-types) [index]) :type) :invalid))))

(defn move [params dx dy]
  (update-in (update-in @(params :player) [:position :x] + dx) [:position :y] + dy))

(defn move-to [player x y]
  (assoc-in (assoc-in player [:position :x] x) [:position :y] y))

(defn do-tile! [params x y]
  (tile-action! params (@(params :tile-types) (or (get-in @(params :game-map) [y x]) 1)) {:x x :y y}))

(defmethod tile-action! :none [params tile instance]
  )

(defmethod tile-action! :invalid [params tile instance]
  )

(defmethod tile-action! :default [params tile instance]
  )

(defmethod tile-action! :battle [params tile instance]
  (try-battle! params))

(defmethod tile-action! :move-to [params tile instance]
  (dosync 
    (let [target (params :player)
          x (or (tile :x) (instance :x)) 
          y (or (tile :y) (instance :y))]
      (alter target move-to x y)
      (do-tile! params x y))))

(defmethod tile-action! :portal [params tile instance]
  (dosync 
    (let [target (params :player)
          x (or (tile :x) (instance :x)) 
          y (or (tile :y) (instance :y))]
      (commute target move-to x y)
      (load-map! params (tile :file)))))


(defn change-tile! [params replacement-tile x y]
  (dosync
    (alter (params :game-map) (fn [m] (assoc-in m [y x] replacement-tile)))))

(defmethod tile-action! :replace [params tile instance]
  (condp = (tile :target)
    :self (change-tile! params (tile :data) (instance :x) (instance :y))
    nil))

(defn try-move! [params dx dy]
  (dosync
    (let [np (move params dx dy)
          x (get-in np [:position :x])
          y (get-in np [:position :y])]
      (when (valid-position? params x y)
        (ref-set (params :player) np) (do-tile! params x y)))))

(defn player-attack! [params]
  (dosync
    (alter (params :enemy) (fn [en] (update-in en [:hp] - (get-in @(params :player) [:stats :atk]))))))

(defn enemy-attack! [params]
  (dosync
    (alter (params :player) (fn [pl] (update-in pl [:stats :hp] - (@(params :enemy) :atk))))))

(defn do-turn! [params]
  (dosync (player-attack! params)
    (if (> (@(params :enemy) :hp) 0) 
      (enemy-attack! params))))

(defn win-battle! [params]
  (dosync
    (ref-set (params :state) :world)
    (alter (params :player) (fn [pl] (update-in pl [:kills] inc)))))

(defn try-victory! [params] 
  (cond 
    (< (get-in @(params :player) [:stats :hp]) 1) 
    (clear! params)
    (< (@(params :enemy) :hp) 1) 
    (win-battle! params)))