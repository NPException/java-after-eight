(def junit-jupiter-version "5.6.0")
(def mockito-version "3.2.4")

(defproject java-after-eight-clj "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [clj-time "0.15.2"]]
  :source-paths ["Clojure port/src"]
  :java-source-paths ["genealogy/src/main/java" "genealogists/src/main/java"]
  :resource-paths ["genealogists/src/main/resources"]
  :test-paths ["genealogy/src/test/java" "Clojure port/test"]
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             :dev     {:dependencies [[org.junit.jupiter/junit-jupiter-api ~junit-jupiter-version]
                                      [org.junit.jupiter/junit-jupiter-params ~junit-jupiter-version]
                                      [org.mockito/mockito-core ~mockito-version]
                                      [org.mockito/mockito-junit-jupiter ~mockito-version]
                                      [org.assertj/assertj-core "3.14.0"]]}})
