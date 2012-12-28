(ns Test.core
  (:import [javax.swing JFrame JPanel SwingUtilities]
           [java.awt Dimension Color Graphics Graphics2D]
           [java.awt.event KeyAdapter KeyEvent])
  (:use [Test.render :only [render]]
        [Test.input :only [input]])
  (:gen-class :main true))

(set! *warn-on-reflection* true)

(def panel (atom nil))
(def ka (atom nil))
(def frame (atom nil))
(def animator (agent nil))

(def running (atom true))
(def time-delta 50)

(def player (ref nil))
(def enemy (ref nil))
(def state (ref :start))
(def tile-types (ref nil))
(def game-map (ref nil))
(def enemies (ref nil))
(def spawns (ref nil))
(def tile-renders (ref nil))
(def battle-state (ref nil))

(defn create-panel []
  (doto (proxy [JPanel] []
          (paint [^Graphics2D g] (render ^Graphics2D g @state @game-map @player @enemy @tile-renders @battle-state)))
    (.setPreferredSize (new Dimension 500 500))))

(defn create-key-adapter []
  (proxy [KeyAdapter] []
    (keyReleased [^KeyEvent e] 
                 (input {:event (.getKeyCode e) 
                         :state state
                         :game-map game-map 
                         :player player
                         :enemy enemy
                         :enemies enemies
                         :tile-types tile-types
                         :tile-renders tile-renders
                         :spawns spawns
                         :battle-state battle-state}))))

(defn create-frame []
  (doto (new JFrame) 
    (.add ^JPanel @panel) 
    (.addKeyListener @ka)
    (.setDefaultCloseOperation JFrame/EXIT_ON_CLOSE)
    .pack .show))

(defn animation [_]
  (when @running
    (send-off *agent* #'animation))
  (.repaint ^JPanel @panel)
  (Thread/sleep time-delta)
  nil)

(defn -main [& args]
  (reset! panel (create-panel))
  (reset! ka (create-key-adapter))
  (reset! frame (SwingUtilities/invokeLater create-frame))
  (send-off animator animation))

(comment
  (-main)
  
  (spit "resources/blubber.txt" "test")
  (let [directory (new java.io.File "resources")]
    (if (.exists directory)
      (spit "resources/blubber.txt" (str @game-map))
      (do (.mkdirs directory)
        (spit "resources/game_map1.txt" (str @game-map)))))
  
  (read-string (slurp "resources/blubber.txt"))
  (slurp "resources/game_map1.txt")
  (read-string (str '(fn a [] (println "a"))))
  (doc with-out-str)
  ((fn remove-directory [^java.io.File directory]
    (let [prefix (.getName directory)]
      (do (doseq [file-name (.list directory)]
        (let [file (new java.io.File (str prefix "/" file-name))]
          (if (.isDirectory file) 
            (remove-directory file)
            (.delete file))))
        (.delete directory))))
    (new java.io.File "resources"))
  
  (def content "(ns user) (defn foo [a b] (str a \" \" b))")
  (map eval (read-string (str \( content \))))
  (user/foo 2 3)
  (ns-unmap *ns* 'content)
  (remove-ns 'user)
  
  (.delete (new java.io.File "resources/blubber.txt"))
  
  (.isDirectory (new java.io.File "resources"))
  (str (.getName (new java.io.File "resources")) "/" "blubber.txt")
  
  (.delete (new java.io.File "resources"))

(meta (eval (read-string (let [a (with-meta { :e 2} {:type 3})]
  (str "(with-meta " (str a) (str (meta a)) ")")))))

  
  (reset! @running true)
  
  (reset! @running false)
  
)