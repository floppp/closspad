(ns qoback.closspad.utils.coll-extras)

(defn- pad-vector
  "Ensures a vector is at least `min-length` long, padding with `nil`."
  [v min-length]
  (if (>= (count v) min-length)
    v
    (vec (concat v (repeat (- min-length (count v)) nil)))))

(defn update-with-vector
  "Updates a value in a nested structure, ensuring the final segment of the path
   (which is expected to be a numeric index) acts on a vector.
   If the vector at the target path doesn't exist, it will be initialized as an empty vector.
   Works correctly for out-of-bounds indices in ClojureScript by padding the vector."
  [state full-path value]
  (let [path (butlast full-path)
        index                 (last full-path)]
    (update-in
     state
     (vec path)
     (fn [cur-vec]
       (let [vec-to-update (if (vector? cur-vec) cur-vec [])
             padded-vec    (pad-vector vec-to-update (inc index))]
         (assoc padded-vec index value))))))


(comment
  (update-with-vector {} [:a :b 1] :foo)
  ;; => {:a {:b [nil :foo]}}

  ;; 2. Updating an existing vector
  (update-with-vector {:a {:b [:existing-0 :existing-1 :existing-2]}} [:a :b 0] :updated-first)
  ;; => {:a {:b [:updated-first :existing-1 :existing-2]}}

  ;; 3. Extending a vector (this is where the `pad-vector` is crucial in CLJS)
  (update-with-vector {:a {:b [:existing-0]}} [:a :b 5] :new-at-five)
  ;; => {:a {:b [:existing-0 nil nil nil nil :new-at-five]}}

  ;; 4. Path with numbers (interprets them as map keys if not at end)
  (update-with-vector {} [:a 0 :b 1] :foo)
  ;; => {:a {0 {:b [nil :foo]}}}

  ;; Example of just `pad-vector` working
  (pad-vector [] 5)
  ;; => [nil nil nil nil nil]

  (pad-vector [:a :b] 5)
  ;; => [:a :b nil nil nil]

  (pad-vector [1 2 3 4 5] 3)
  ;; => [1 2 3 4 5] ; No padding needed<
  )

