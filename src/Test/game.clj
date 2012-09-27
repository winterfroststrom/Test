(ns Test.game)

(defn make-player []
  {:position {:x 2 :y 2} 
   :kills 0
   :stats {:hp 10 :atk 2 :name :player}})


(defn make-enemy []
  {:hp 2 :atk 1 :name :enemy})
