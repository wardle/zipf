(ns com.eldrix.zipf-test
  (:require [clojure.test :refer :all]
            [com.eldrix.zipf :as zipf]
            [clojure.string :as str]
            [clojure.java.io :as io])
  (:import (java.nio.file Path Paths LinkOption Files)))

(def test-query
  ["test/resources/test.zip"
   ["Z1.ZIP" "z1f1"]
   ["z3.zip"]
   ["z3.zip" "z3f3"]
   ["z3.zip" #"z3/z3f\d"]
   ["z3.zip" ["z3" #"z3f\d"]]])

(deftest unzip-query
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

(deftest unzip-nested
  (let [unzipped (zipf/unzip-nested (Paths/get (.toURI (io/resource "test.zip"))))]
    (is (Files/exists unzipped (into-array LinkOption [])))
    (is (Files/exists (.resolve unzipped "f1") (into-array LinkOption [])))
    (is (Files/exists (.resolve unzipped "Z1-ZIP/z1f1") (into-array LinkOption [])))
    (is (Files/exists (.resolve unzipped "z2/z2-zip/z2f1") (into-array LinkOption [])))))

(deftest zip
  (let [dir (Files/createTempDirectory "zipf")])
  (let [unzipped (zipf/unzip (Paths/get (.toURI (io/resource "test.zip"))))
        zipped (zipf/zip (.toFile unzipped))
        unzipped' (zipf/unzip (.toPath zipped))]))

(comment
  (run-tests))

