(ns Test.game)

(defn type-dispatch [x] (x :type))

(defn make-player []
  {:position {:x 2 :y 2} 
   :kills 0
   :stats {:hp 10 :atk 2 :name :player}})

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

(defn init-world! [params]
  (dosync
    (let [file1 (read-string (slurp "resources/game_map1.txt"))]
      (ref-set (params :game-map) (file1 :tiles))
      (ref-set (params :enemies) (file1 :enemies))
      (ref-set (params :spawns) (file1 :spawns))
      (ref-set (params :player) (make-player))
      (ref-set (params :state) :world))))

(defn clear! [params]
  (dosync
    (ref-set (params :player) nil)
    (ref-set (params :state) :start)
    (ref-set (params :enemies) nil)
    (ref-set (params :enemy) nil)
    (ref-set (params :spawns) nil)
    (ref-set (params :game-map) nil)))

(defn init-battle! [params]
  (dosync
    (ref-set (params :state) :battle)
    (ref-set (params :enemy) (make-enemy (params :enemies) (params :spawns)))))

(defn try-battle! [params x y]
  (dosync
    (when (and (= (get-in @(params :game-map) [y x]) 0) (> (rand 20) 15))
      (init-battle! params))))

(defn valid-position? [game-map x y]
  (not (= (get-in game-map [y x]) 1)))

(defn move [params dx dy]
  (update-in (update-in @(params :player) [:position :x] + dx) [:position :y] + dy))

(defn move-to [player x y]
  (assoc-in (assoc-in player [:position :x] x) [:position :y] y))

(defn do-tile! [params x y]
  (dosync
    (condp = (get-in @(params :game-map) [y x])
      0 (try-battle! params x y)
      3 (alter (params :player) move-to 1 2)
      4 (alter (params :player) move-to 13 7)
      5 (alter (params :game-map) (fn [m] (assoc-in m [y x] 6)))
      6 (alter (params :game-map) (fn [m] (assoc-in m [y x] 5)))
      nil)))

(defn try-move! [params dx dy]
  (dosync
    (let [np (move params dx dy)
          x (get-in np [:position :x])
          y (get-in np [:position :y])]
      (when (valid-position? @(params :game-map) x y)
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