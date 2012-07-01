# Neo-Zip

``Neo-Zip`` provides an easier-to-use syntax for traversing Neo4J graphs using the syntax of ``Graph-Zip``.

It is built atop [Graph-Zip](https://github.com/james-henderson/graph-zip) and [Borneo](https://github.com/wagjo/borneo) (a Clojure Neo4J wrapper).

## Usage

There is only one function in ``Neo-Zip`` - ```get-zipper```, which is
used to get a zipper that is compatible with ``Graph-Zip``.

It must be used inside a ```with-db!``` or ```with-local-db!``` block:

    (use 'neo-zip.core :only [get-zipper])
    (require '[borneo.core :as neo]
             '[graph-zip.core :as g])
    

    (neo/with-local-db! "db/testNeo"
                        (g/zip-> (get-zipper)
                                 ... ))
                                 
### An example graph:

For these examples, we use the Matrix social graph as per the ``Neo4J``
documentation. To set this up, run the following Clojure expression
(changing the database path if required):

    (neo/with-local-db! "db/testNeo"
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
            (neo/create-rel! trinity :loves the-one)))

Notice that mutations to the graph are made using the ``Borneo``
library - Neo-Zip is a read-only syntax.

We can then query the graph using Neo-Zip's graph-zip syntax:

    (neo/with-local-db! "db/testNeo"
     (for [human (g/zip-> (get-zipper)
                          :humans
                          :human)]
       (g/prop1 human :name)))

    ;; -> ("Cypher" "Morpheus" "Trinity" "Thomas Anderson")
    
We can also use the 'incoming' function of graph-zip to find out the
name of everyone that knows 'trinity'

    (neo/with-local-db! "db/testNeo"
     (map #(g/prop1 % :name) (g/zip-> (get-zipper)
                                      :humans
                                      :human
                                      [(g/prop= :name "Trinity")]
                                      (g/incoming :knows))))
    ;; -> ("Morpheus" "Thomas Anderson")
    
For more examples of how to use Graph-Zip and Borneo, please refer to
their relevant projects:

* [Graph-Zip](https://github.com/james-henderson/graph-zip)
* [Borneo](https://github.com/wagjo/borneo)

## License

Copyright (C) 2012 James Henderson

Distributed under the Eclipse Public License, the same as Clojure.
