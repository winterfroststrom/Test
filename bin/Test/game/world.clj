(ns Test.game.world
  (:require [Test.game.basic :as basic]))

(defn try-battle! [params]
  (when (> (rand 20) 15)
    (basic/init-state! params :battle)))

(defn valid-position? [params x y]
  (let [index (or (get-in @(params :game-map) [y x]) 1)]
    (not (= ((get-in @(params :tile-types) [index]) :type) :invalid))))

(defn move [params dx dy]
  (update-in (update-in @(params :player) [:position :x] + dx) [:position :y] + dy))

(defn move-to [player x y]
  (assoc-in (assoc-in player [:position :x] x) [:position :y] y))

(defn change-tile! [params replacement-tile x y]
  (dosync
    (alter (params :game-map) (fn [m] (assoc-in m [y x] replacement-tile)))))

(defn do-tile! [params x y]
  (basic/tile-action! params (@(params :tile-types) (or (get-in @(params :game-map) [y x]) 1)) {:x x :y y}))

(defmethod basic/tile-action! :none [params tile instance]
  )

(defmethod basic/tile-action! :invalid [params tile instance]
  )

(defmethod basic/tile-action! :default [params tile instance]
  )

(defmethod basic/tile-action! :battle [params tile instance]
  (try-battle! params))

(defmethod basic/tile-action! :move-to [params tile instance]
  (dosync 
    (let [target (params :player)
          x (or (tile :x) (instance :x)) 
          y (or (tile :y) (instance :y))]
      (alter target move-to x y)
      (do-tile! params x y))))

(defmethod basic/tile-action! :portal [params tile instance]
  (dosync 
    (let [target (params :player)
          x (or (tile :x) (instance :x)) 
          y (or (tile :y) (instance :y))]
      (commute target move-to x y)
      (basic/load-map! params (tile :file)))))

(defmethod basic/tile-action! :replace [params tile instance]
  (condp = (tile :target)
    :self (change-tile! params (tile :data) (instance :x) (instance :y))
    nil))
