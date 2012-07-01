(ns neo4clj.core
  (:require [borneo.core :as neo]
            [graph-zip.core :as g])
  (:import [org.neo4j.graphdb Node]))

(defn- wrap-vals-in-vector [m]
  (into {} (for [[k v] m]
             [k [v]])))

(defn- all-node-props [node]
  (wrap-vals-in-vector (neo/props node)))

(defn- all-node-rels [node]
  (wrap-vals-in-vector (into {} (for [rel (neo/rels node nil :out)]
                                  [(neo/rel-type rel) (neo/other-node rel node)]))))

(defn- node-props-map [node]
  (merge-with concat (all-node-props node) (all-node-rels node)))


(defn- node-prop [node prop]
  (if (neo/prop? node prop)
    (vector (neo/prop node prop))))

(defn- node-rels [node type]
  (into [] (for [rel (neo/rels node type :out)]
             (neo/other-node rel node))))

(defn- node-prop-values [node prop]
  (concat (node-prop node prop) (node-rels node prop)))

(defrecord NeoGraph []
  g/Graph
  (props-map [this node]
    (if (instance? Node node)
      (node-props-map node)))
  (prop-values [this node prop]
    (if (instance? Node node)
      (node-prop-values node prop))))

(defn get-root-zipper
  ([] (get-root-zipper (NeoGraph.)))
  ([graph]
      (g/graph-zipper graph (neo/root))))

