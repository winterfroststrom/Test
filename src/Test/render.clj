(ns Test.render
  (:import [java.awt Graphics2D Color]))

(set! *warn-on-reflection* true)

(def tile-dimensions {:x 20 :y 20})

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
  ([^Graphics2D g game-map player] (render-game-map g game-map player 0 0))
  ([^Graphics2D g game-map player offx offy]
    (.clearRect g 0 0 500 500)
    (.drawString g (str "Hp: " (get-in player [:stats :hp])) 100 100)
    (.drawString g (str "Kills: " (player :kills)) 100 120)
    (doseq [y (range (count game-map))]
      (doseq [x (range (count (game-map 0)))]
        (render-tile g (get-in game-map [y x]) 
                     (tile-position x :x offx)
                     (tile-position y :y offy))))))

(defn render-world [^Graphics2D g game-map player]
  (let [offx 170 offy 170
        px (get-in player [:position :x])
        py (get-in player [:position :y])]
    (doto g
      (render-game-map game-map player offx offy)
      (render-tile -1 
                   (tile-position px :x offx)
                   (tile-position py :y offy)))))
  
(defn render-stats [^Graphics2D g stats x y]
  (doto ^Graphics2D g
    (.drawString (str (stats :name)) (int x) (int y))
    (.drawString (str (stats :hp)) (int x) (int (+ y 20)))))

(defn render-battle [^Graphics2D g player enemy]
  (doto g
    (.clearRect 0 0 500 500)
    (render-stats (player :stats) 100 100)
    (render-stats enemy 300 100)))

(defn render [^Graphics2D g state game-map player enemy]
  (condp = state
    :start (render-start g)
    :world (render-world g game-map player)
    :battle (render-battle g player enemy)))
