(ns Test.game)

(defn type-dispatch [x] (x :type))

(defn make-player []
  {:position {:x 2 :y 2} 
   :kills 0
   :stats {:hp 10 :atk 2 :name :player}})

(defmulti make-enemy (fn [table spawns] (type-dispatch spawns)))

(defmethod make-enemy :probabilities [table spawns]
  (let [probabilities (spawns :probabilities)
        sum (reduce + probabilities)
        place (rand sum)]
    (loop [place place index 0]
      (let [probability (probabilities index)]
        (if (> probability place)
          (table index)
          (recur (- place probability) (inc index)))))))