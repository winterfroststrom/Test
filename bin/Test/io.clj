(ns Test.io)

(def slash (if (re-matches #".*window.*" (clojure.string/lower-case (System/getProperty "os.name"))) "\\" "/"))

(defn load-resource [resource]
  (read-string (slurp (str "resources" slash (name resource) ".txt"))))

(defn save-resource [resource data]
  (spit (str "resources" slash (name resource) ".txt") (str data)))

(defn init-vec [size value]
  (vec (take size (repeat value))))

(defn flat-world-map [number width height]
  (init-vec height (init-vec width number)))

(defn replace-row [world replacement index]
  (assoc world index (mapv (fn [_] replacement) (nth world index))))

(defn replace-column 
  ([world replacement index]
    (replace-column world replacement index 0))
  ([world replacement index row-index]
    (if (>= row-index (count world))
      world
      (recur (assoc world row-index (assoc (world row-index) index replacement)) replacement index (inc row-index)))))

(defn add-border [world border]
  (let [start 0
        end (dec (count world))]
    (-> world
      (replace-row border start)
      (replace-row border end)
      (replace-column border start)
      (replace-column border end))))


