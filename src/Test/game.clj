(ns Test.game
  (:require [Test.game.basic :as basic] 
            [Test.game.battle :as battle]
            [Test.game.world :as world]))

(defn init-game! [params]
  (basic/init-state! params :world))

(defn save-player [params]
  (basic/save-player params))

(defn try-move! [params dx dy]
  (dosync
    (let [np (world/move params dx dy)
          x (get-in np [:position :x])
          y (get-in np [:position :y])]
      (when (world/valid-position? params x y)
        (ref-set (params :player) np) (world/do-tile! params x y)))))

(defn do-turn! [params]
  (dosync
    (condp = (mod (@(params :battle-state) :turn) 2)
      0 (battle/enemy-attack! params)
      1 (battle/player-attack! params)
      nil)
    (alter (params :battle-state) update-in [:turn] inc)
    (cond 
      (battle/death? params) (basic/clear! params)
      (battle/victory? params) (battle/win-battle! params)
      :else nil)))
