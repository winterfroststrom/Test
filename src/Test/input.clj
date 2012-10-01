(ns Test.input
  (:import [java.awt.event KeyEvent])
  (:require [Test.game :as game]))

(set! *warn-on-reflection* true)

(defn input-start [params]
  (condp = (params :event)
    KeyEvent/VK_SPACE (game/init-world! params)
    nil))

(defn input-world [params]
  (condp = (params :event) 
    KeyEvent/VK_SPACE (game/init-battle! params) 
    KeyEvent/VK_UP (game/try-move! params 0 -1)
    KeyEvent/VK_DOWN (game/try-move! params 0 1)
    KeyEvent/VK_LEFT (game/try-move! params -1 0)
    KeyEvent/VK_RIGHT (game/try-move! params 1 0)
    nil))

(defn input-battle [params]
  (condp = (params :event)
    KeyEvent/VK_SPACE (game/clear! params)
    KeyEvent/VK_A (dosync (game/do-turn! params) (game/try-victory! params))
    KeyEvent/VK_S (dosync (game/enemy-attack! params) (game/try-victory! params))
    nil))

(defn input [params]
  (dosync
    (condp = @(params :state)
      :start (input-start params)
      :world (input-world params)
      :battle (input-battle params))))
