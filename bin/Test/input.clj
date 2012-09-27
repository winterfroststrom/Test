(ns Test.input
  (:import [java.awt.event KeyEvent])
  (:require Test.game))

(set! *warn-on-reflection* true)

(defn input-start [params]
  (condp = (params :event)
    KeyEvent/VK_SPACE (reset! (params :state) :world)
    nil))

(defn valid-position? [game-map x y]
  (not (= (get-in game-map [y x]) 1)))

(defn move [params dx dy]
  (update-in (update-in @(params :player) [:position :x] + dx) [:position :y] + dy))

(defn move-to [player x y]
  (assoc-in (assoc-in player [:position :x] x) [:position :y] y))

(defn try-battle! [params x y]
  (when (and (= (get-in @(params :game-map) [y x]) 0) (> (rand 20) 15))
    (reset! (params :state) :battle)))

(defn try-move! [params dx dy]
  (let [np (move params dx dy)
        x (get-in np [:position :x])
        y (get-in np [:position :y])]
    (when (valid-position? @(params :game-map) x y)
      (reset! (params :player) np)
      (condp =  (get-in @(params :game-map) [y x])
        3 (swap! (params :player) move-to 1 2)
        4 (swap! (params :player) move-to 13 7)
        5 (swap! (params :game-map) (fn [m] (assoc-in m [y x] 6)))
        6 (swap! (params :game-map) (fn [m] (assoc-in m [y x] 5)))
        (try-battle! params x y)))))

(defn input-world [params]
  (condp = (params :event) 
    KeyEvent/VK_SPACE (reset! (params :state) :battle)
    KeyEvent/VK_UP (try-move! params 0 -1)
    KeyEvent/VK_DOWN (try-move! params 0 1)
    KeyEvent/VK_LEFT (try-move! params -1 0)
    KeyEvent/VK_RIGHT (try-move! params 1 0)
    nil))

(defn input-battle [params]
  (condp = (params :event)
    KeyEvent/VK_SPACE 
    (do (reset! (params :enemy) (Test.game/make-enemy))
      (reset! (params :player) (Test.game/make-player))
      (reset! (params :state) :start))
    KeyEvent/VK_A (do (swap! (params :enemy) (fn [en] (update-in en [:hp] - (get-in @(params :player) [:stats :atk]))))
                    (if (> (@(params :enemy) :hp) 0) 
                      (swap! (params :player) (fn [pl] (update-in pl [:stats :hp]  - (@(params :enemy) :atk))))))
    KeyEvent/VK_S (swap! (params :player) (fn [pl] (update-in pl [:stats :hp] - (@(params :enemy) :atk))))
    nil)
  (cond 
    (< (get-in @(params :player) [:stats :hp]) 1) 
    (do (reset! (params :enemy) (Test.game/make-enemy))
      (reset! (params :player) (Test.game/make-player))
      (reset! (params :state) :start))
    (< (@(params :enemy) :hp) 1) 
    (do (reset! (params :enemy) (Test.game/make-enemy))
      (reset! (params :state) :world)
      (swap! (params :player) (fn [pl] (update-in pl [:kills] inc))))))

(defn input [params]
  (condp = @(params :state)
    :start (input-start params)
    :world (input-world params)
    :battle (input-battle params)))
