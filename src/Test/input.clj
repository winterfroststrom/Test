(ns Test.input
  (:import [java.awt.event KeyEvent])
  (:require [Test.game :as game]))

(set! *warn-on-reflection* true)

(defn input-start [params]
  (condp = (params :event)
    KeyEvent/VK_SPACE (game/init-game! params)
    nil))

(defn input-world [params]
  (condp = (params :event) 
    KeyEvent/VK_SPACE nil;(game/init-battle! params)
    KeyEvent/VK_S (game/save-player params)
    KeyEvent/VK_UP (game/try-move! params 0 -1)
    KeyEvent/VK_DOWN (game/try-move! params 0 1)
    KeyEvent/VK_LEFT (game/try-move! params -1 0)
    KeyEvent/VK_RIGHT (game/try-move! params 1 0)
    nil))

(defn input-battle [params]
  (condp = (params :event)
    KeyEvent/VK_SPACE nil;(game/clear! params)
    KeyEvent/VK_A (game/do-turn! params)
    KeyEvent/VK_S nil;(game/do-turn! params)
    nil))

(defn input [params]
  (dosync
    (condp = @(params :state)
      :start (input-start params)
      :world (input-world params)
      :battle (input-battle params))))
