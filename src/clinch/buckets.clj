
(ns clinch.buckets
  (:use [clojure.contrib def]
	[clinch ri utils]
	[clinch.vectors :as vec]))

(defstruct
    #^{:doc "Bucket to classify text to. "}
  bucket :name :text :store-text :own-vector :parents :children)

(defnk make-bucket
  "Creates classification bucket. If :store-text is true, will also store
   indexed text. You can create bucket hierarchies by using (add-child)
   procedure. "
  [vbox name text :store-text true]
  (struct-map bucket
    :name name
    :text (if store-text text nil)
    :store-text store-text
    :own-vector (atom (context-vector vbox text))
    :parents (atom #{})
    :children (atom #{})))

;; wrappers to not mess with atoms by hand

(defn own-vector [buck]
  @(:own-vector buck))

(defn bucket-name [buck]
  (:name buck))

(defn bucket-text [buck]
  (:text buck))

(defn- add-vector! [buck v]
  (swap! (:own-vector buck) vec/plus v)
  (doseq [parent @(:parents buck)]
    (add-vector! parent v)))

(defn- sub-vector! [buck v]
  (swap! (:own-vector buck) vec/minus v)
  (doseq [parent @(:parents buck)]
    (sub-vector! parent v)))

(defn add-child! [parent child]
  (swap! (:parents child) conj parent)
  (swap! (:children parent) conj child)
  (add-vector! parent @(:own-vector child)))

(defn remove-child! [parent child]
  (swap! (:parents child) disj parent)
  (swap! (:children parent) disj child)
  (sub-vector! parent @(:own-vector child)))


;; util functions

(defn find-bucket [root name]
  (println (= name (:name root)))
  (cond true "Ok!"
	(= name (:name root)) "Here"
	(empty? @(:children root)) nil
	:else (first
	       (filter #(not= % nil)
		       (map #(find-bucket % name) @(:children root))))))