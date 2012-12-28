(ns Test.game.battle
  (:require [Test.game.basic :as basic]))

(defn player-attack! [params]
  (dosync
    (alter (params :enemy) (fn [en] (update-in en [:hp] - (get-in @(params :player) [:stats :atk]))))))

(defn enemy-attack! [params]
  (dosync
    (alter (params :player) (fn [pl] (update-in pl [:stats :hp] - (@(params :enemy) :atk))))))

(defn win-battle! [params]
  (dosync
    (ref-set (params :state) :world)
    (alter (params :player) (fn [pl] (update-in pl [:kills] inc)))))

(defn death? [params]
  (< (get-in @(params :player) [:stats :hp]) 1))

(defn victory? [params]
  (< (@(params :enemy) :hp) 1) )


