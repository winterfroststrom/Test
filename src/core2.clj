(ns core2
  (:import [javax.swing JFrame JPanel]
           [java.awt Dimension Color Graphics Graphics2D]
           [java.awt.event KeyAdapter KeyEvent])
  (:gen-class :main true))

(set! *warn-on-reflection* true)

(def panel (atom nil))
(def ka (atom nil))
(def frame (atom nil))
(def animator (agent nil))

(def running true)
(def time-delta 50)

(def state (atom :start))

(defn make-player []
  {:position {:x 2 :y 2} 
   :kills 0
   :stats {:hp 10 :atk 2 :name :player}})

(def player (atom (make-player)))

(defn make-enemy []
  {:hp 2 :atk 1 :name :enemy})

(def enemy (atom (make-enemy)))

(def tile-dimensions {:x 20 :y 20})

(comment
(def tiles (letfn [t (fn [_] true)
                 f (fn [_] false)] 
             [[t t]
              [f f]
              [t t]
              [t ]
              [t ]
              [t ]
              [t ]]))

        3 (swap! p move-to 1 2)
        4 (swap! p move-to 13 7)
        5 (swap! game-map (fn [m] (assoc-in m [y x] 6)))
        6 (swap! game-map (fn [m] (assoc-in m [y x] 5)))
)

(def game-map (atom [[1 1 1 1 1 1 1 1 1 1 1 1 1 1 1]
               [1 0 0 0 1 1 0 0 0 1 1 0 0 0 1]
               [4 2 2 0 1 1 0 0 0 1 1 0 5 0 1]
               [1 0 2 2 1 1 0 0 0 1 1 0 0 0 1]
               [1 1 1 2 2 2 2 2 0 1 1 0 0 0 1]
               [1 1 1 1 1 1 0 2 0 1 1 0 0 0 1]
               [1 0 0 0 1 1 0 2 0 1 1 0 0 0 1]
               [1 0 0 0 1 1 0 2 2 2 2 2 2 2 3]
               [1 0 0 0 0 0 0 0 0 1 1 0 0 0 1]
               [1 1 1 1 1 1 1 1 1 1 1 1 1 1 1]]))

(defn render-start [^Graphics2D g]
  (doto g
    (.clearRect 0 0 500 500)
    (.setColor Color/BLACK)
    (.fillRect 0 0 500 500)
    (.setColor Color/RED)
    (.drawString "Alter" 230 100)))

(defn render-tile-rect [^Graphics2D g tile x y color]
  (doto g
    (.setColor color)
    (.fillRect x y (tile-dimensions :x) (tile-dimensions :y))))

(defn render-tile [^Graphics2D g tile x y]
  (condp = tile
    -1 (render-tile-rect g tile x y Color/BLUE)
    0 (render-tile-rect g tile x y Color/GREEN)
    1 (render-tile-rect g tile x y Color/GRAY)
    2 (render-tile-rect g tile x y Color/YELLOW)
    3 (render-tile-rect g tile x y Color/ORANGE)
    4 (render-tile-rect g tile x y Color/ORANGE)
    5 (render-tile-rect g tile x y Color/BLACK)
    6 (render-tile-rect g tile x y Color/WHITE)
    nil))

(defn tile-position [pos keyword offset]
  (+ (* pos (tile-dimensions keyword)) offset))

(defn render-game-map
  ([^Graphics2D g] (render-game-map g 0 0))
  ([^Graphics2D g offx offy]
    (.clearRect g 0 0 500 500)
    (.drawString g (str "Hp: " (get-in @player [:stats :hp])) 100 100)
    (.drawString g (str "Kills: " (@player :kills)) 100 120)
    (doseq [y (range (count @game-map))]
      (doseq [x (range (count (@game-map 0)))]
        (render-tile g (get-in @game-map [y x]) 
                     (tile-position x :x offx)
                     (tile-position y :y offy))))))

(defn render-world [^Graphics2D g]
  (let [offx 170 offy 170
        px (get-in @player [:position :x])
        py (get-in @player [:position :y])]
    (doto g
      (render-game-map offx offy)
      (render-tile -1 
                   (tile-position px :x offx)
                   (tile-position py :y offy)))))
  
(defn render-stats [^Graphics2D g stats x y]
  (doto ^Graphics2D g
    (.drawString (str (stats :name)) x y)
    (.drawString (str (stats :hp)) x (+ y 20))))

(defn render-battle [^Graphics2D g]
  (doto g
    (.clearRect 0 0 500 500)
    (render-stats (@player :stats) 100 100)
    (render-stats @enemy 300 100)))

(defn render [^Graphics2D g]
  (condp = @state
    :start (render-start g)
    :world (render-world g)
    :battle (render-battle g)))

(defn input-start [e]
  (condp = e
    KeyEvent/VK_SPACE (reset! state :world)
    nil))

(defn valid-position? [x y]
  (not (= (get-in @game-map [y x]) 1)))

(defn move [p dx dy]
  (update-in (update-in p [:position :x] + dx) [:position :y] + dy))

(defn move-to [p x y]
  (assoc-in (assoc-in p [:position :x] x) [:position :y] y))

(defn try-battle! [x y]
  (when (and (= (get-in @game-map [y x]) 0) (> (rand 20) 15))
    (reset! state :battle)))

(defn try-move! [p dx dy]
  (let [np (move @p dx dy)
        x (get-in np [:position :x])
        y (get-in np [:position :y])]
    (when (valid-position? x y)
      (reset! p np)
      (condp =  (get-in @game-map [y x])
        3 (swap! p move-to 1 2)
        4 (swap! p move-to 13 7)
        5 (swap! game-map (fn [m] (assoc-in m [y x] 6)))
        6 (swap! game-map (fn [m] (assoc-in m [y x] 5)))
        (try-battle! x y)))))

(defn input-world [e]
  (condp = e 
    KeyEvent/VK_SPACE (reset! state :battle)
    KeyEvent/VK_UP (try-move! player 0 -1)
    KeyEvent/VK_DOWN (try-move! player 0 1)
    KeyEvent/VK_LEFT (try-move! player -1 0)
    KeyEvent/VK_RIGHT (try-move! player 1 0)
    nil))

(defn input-battle [e]
  (condp = e
    KeyEvent/VK_SPACE 
    (do (reset! enemy (make-enemy))
      (reset! player (make-player))
      (reset! state :start))
    KeyEvent/VK_A (do (swap! enemy (fn [en] (update-in en [:hp] - (get-in @player [:stats :atk]))))
                    (if (> (get-in @enemy [:hp]) 0) 
                      (swap! player (fn [pl] (update-in pl [:stats :hp]  - (@enemy :atk))))))
    KeyEvent/VK_S (swap! player (fn [pl] (update-in pl [:stats :hp] - (get-in @enemy [:atk]))))
    nil)
  (cond 
    (< (get-in @player [:stats :hp]) 1) 
    (do (reset! enemy (make-enemy))
      (reset! player (make-player))
      (reset! state :start))
    (< (get-in @enemy [:hp]) 1) 
    (do (reset! enemy (make-enemy))
      (reset! state :world)
      (swap! player (fn [pl] (update-in pl [:kills] inc))))))

(defn input [^KeyEvent e]
  (let [e (.getKeyCode e)]
    (condp = @state
      :start (input-start e)
      :world (input-world e)
      :battle (input-battle e))))

(defn create-panel []
  (doto (proxy [JPanel] []
          (paint [^Graphics2D g] (render ^Graphics2D g)))
    (.setPreferredSize (new Dimension 500 500))))

(defn create-key-adapter []
  (proxy [KeyAdapter] []
    (keyReleased [^KeyEvent e] (input ^KeyEvent e))))

(defn create-frame []
  (doto (new JFrame) 
    (.add ^JPanel @panel) 
    (.addKeyListener @ka)
    (.setDefaultCloseOperation JFrame/EXIT_ON_CLOSE)
    .pack .show))

(defn animation [_]
  (when running
    (send-off *agent* #'animation))
  (.repaint ^JPanel @panel)
  (Thread/sleep time-delta)
  nil)

(defn -main [& args]
  (do
    (reset! panel (create-panel))
    (reset! ka (create-key-adapter))
    (reset! frame (create-frame))
    (send-off animator animation)))

(comment
  
  (-main)
  
  (def running true)
  
  (def running false)
  
)