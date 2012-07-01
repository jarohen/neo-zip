(ns neo-zip.core
  (:require [borneo.core :as neo]
            [graph-zip.core :as g])
  (:import [org.neo4j.graphdb Node]))

(defn- wrap-vals-in-vector [m]
  (into {} (for [[k v] m]
             [k [v]])))

(defn- all-node-props [node]
  (wrap-vals-in-vector (neo/props node)))

(defn- all-node-rels [node direction]
  (apply merge-with concat (into [] (for [rel (neo/rels node nil direction)]
                                      {(neo/rel-type rel) [(neo/other-node rel node)]}))))

(defn- node-props-map [node]
  (merge-with concat (all-node-props node) (all-node-rels node)))


(defn- node-prop [node prop]
  (if (neo/prop? node prop)
    (vector (neo/prop node prop))))

(defn- node-rels [node type direction]
  (into [] (for [rel (neo/rels node type direction)]
             (neo/other-node rel node))))

(defn- node-prop-values [node prop direction]
  (concat (node-prop node prop) (node-rels node prop direction)))

(defrecord NeoGraph []
  g/Graph
  (props-map [this node direction]
    (if (instance? Node node)
      (node-props-map node direction)))
  (prop-values [this node prop direction]
    (if (instance? Node node)
      (node-prop-values node prop direction))))

(defn get-zipper
  ([] (get-zipper (neo/root)))
  ([^Node node]
     (g/graph-zipper (NeoGraph.) node)))

(neo/with-local-db! "db/testNeo"
  (map #(g/prop1 % :name) (g/zip-> (get-zipper)
                                   :humans
                                   :human
                                   (g/prop= :name "Trinity")
                                   (g/incoming :knows))))

(neo/with-local-db! "db/testNeo"
  (for [human (g/zip-> (get-zipper)
                       :humans
                       :human)]
    (g/prop1 human :name)))

(comment (neo/with-local-db! "db/testNeo"
           (neo/purge!)
           (let [humans (neo/create-child! :humans nil)
                 programs (neo/create-child! :programs nil)

                 ;; add programs
                 smith (neo/create-child! programs :program
                                          {:name "Agent Smith"
                                           :language "C++"
                                           :age 40})
                 architect (neo/create-child! programs :program
                                              {:name "Architect"
                                               :language "Clojure"
                                               :age 600})

                 ;; add humans
                 the-one (neo/create-child! humans :human
                                            {:name "Thomas Anderson"
                                             :age 29})
                 trinity (neo/create-child! humans :human
                                            {:name "Trinity"
                                             :age 27})
                 morpheus (neo/create-child! humans :human
                                             {:name "Morpheus"
                                              :rank "Captain"
                                              :age 35})
                 cypher (neo/create-child! humans :human
                                           {:name "Cypher"})]

             ;; add relationships

             (neo/create-rel! the-one :knows trinity)
             (neo/create-rel! the-one :knows morpheus)
             (neo/create-rel! morpheus :knows trinity)
             (neo/create-rel! morpheus :knows cypher)
             (neo/set-props! (neo/create-rel! cypher :knows smith)
                             {:disclosure "secret"
                              :age 6})
             (neo/create-rel! smith :knows architect)
             (neo/create-rel! trinity :loves the-one))))

