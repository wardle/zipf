(ns com.eldrix.zipf-test
  (:require [clojure.test :refer :all]
            [com.eldrix.zipf :as zipf]
            [clojure.string :as str]
            [clojure.java.io :as io])
  (:import (java.nio.file Path Paths LinkOption Files)
           (java.nio.file.attribute FileAttribute)))

(def test-query
  ["test/resources/test.zip"
   ["Z1.ZIP" "z1f1"]
   ["z3.zip"]
   ["z3.zip" "z3f3"]
   ["z3.zip" #"z3/z3f\d"]
   ["z3.zip" ["z3" #"z3f\d"]]])

(deftest test-unzip-query
  (let [paths (zipf/unzip-query test-query)]
    (is (= 15 (count (flatten paths))))
    (doseq [path (flatten paths)]
      (is (instance? Path path)))
    (let [z1f1 (get-in paths [1 1])]                        ;; use coordinates to get what we need
      (is (str/ends-with? (.toString z1f1) "z1f1")))
    (let [z3-files (get-in paths [4 1])]
      (is (= 3 (count z3-files))))
    (let [z3-files (get-in paths [5 1 1])]
      (is (= 3 (count z3-files))))
    (zipf/delete-paths paths)))

(deftest test-unzip-nested
  (let [unzipped (zipf/unzip-nested (Paths/get (.toURI (io/resource "test.zip"))))]
    (is (Files/exists unzipped (into-array LinkOption [])))
    (is (Files/exists (.resolve unzipped "f1") (into-array LinkOption [])))
    (is (Files/exists (.resolve unzipped "Z1-ZIP/z1f1") (into-array LinkOption [])))
    (is (Files/exists (.resolve unzipped "z2/z2-zip/z2f1") (into-array LinkOption [])))))

(deftest test-zip
  (let [dir (Files/createTempDirectory "zipf" (make-array FileAttribute 0))
        archive-name (str (.getFileName dir))
        f1 (.resolve dir "f1.txt")
        f2 (.resolve dir "f2.txt")]
    (spit (.toFile f1) "f1.txt")
    (spit (.toFile f2) "f2.txt")
    (let [zip-file (zipf/zip dir)
          unzipped (zipf/unzip zip-file)
          root (.resolve unzipped archive-name)
          f1' (.resolve root "f1.txt")
          f2' (.resolve root "f2.txt")]
      (is (= "f1.txt" (slurp (.toFile f1'))))
      (is (= "f2.txt" (slurp (.toFile f2')))))))

(comment
  (run-tests))

