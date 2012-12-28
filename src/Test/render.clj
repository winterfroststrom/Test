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

(defn render-tile-rect [^Graphics2D g x y color]
  (doto g
    (.setColor color)
    (.fillRect x y (tile-dimensions :x) (tile-dimensions :y))))

(defmulti do-render-tile (fn [^Graphics2D g x y tile-render] (get-in tile-render [:type])))

(defmethod do-render-tile :color [^Graphics2D g x y tile-render]
  (condp = (get-in tile-render [:color])
    :green (render-tile-rect g x y Color/GREEN)
    :blue (render-tile-rect g x y Color/BLUE)
    :grey (render-tile-rect g x y Color/GRAY)
    :yellow (render-tile-rect g x y Color/YELLOW)
    :orange (render-tile-rect g x y Color/ORANGE)
    :black (render-tile-rect g x y Color/BLACK)
    :white (render-tile-rect g x y Color/WHITE)
    (render-tile-rect g x y Color/BLUE)))

(defmethod do-render-tile :default [^Graphics2D g x y tile-render]
  (render-tile-rect g x y Color/BLUE))

(defn tile-position [pos keyword offset]
  (+ (* pos (tile-dimensions keyword)) offset))

(defn render-game-map
  ([^Graphics2D g game-map player tile-renders] 
    (render-game-map g game-map player 0 0 tile-renders))
  ([^Graphics2D g game-map player offx offy tile-renders]
    (.clearRect g 0 0 500 500)
    (.drawString g (str player) 100 100)
    (doseq [y (range (count game-map))]
      (doseq [x (range (count (game-map 0)))]
        (let [tile (get-in game-map [y x])]
          (do-render-tile g
                       (tile-position x :x offx)
                       (tile-position y :y offy)
                       (tile-renders tile)))))))

(defn render-world [^Graphics2D g game-map player tile-renders]
  (let [offx 170 offy 170
        px (get-in player [:position :x])
        py (get-in player [:position :y])]
    (doto g
      (render-game-map game-map player offx offy tile-renders)
      (do-render-tile (tile-position px :x offx) (tile-position py :y offy) nil))))
  
(defn render-stats [^Graphics2D g stats x y]
  (doto ^Graphics2D g
    (.drawString (str stats) (int x) (int y))))

(defn render-battle [^Graphics2D g player enemy]
  (doto g
    (.clearRect 0 0 500 500)
    (render-stats (player :stats) 100 100)
    (render-stats enemy 100 150)))

(defn render [^Graphics2D g state game-map player enemy tile-renders battle-state]
  (condp = state
    :start (render-start g)
    :world (render-world g game-map player tile-renders)
    :battle (render-battle g player enemy)))
