(ns parsers.utils)

(defn parse-int [s]
  (Integer/parseInt s))

(defn parse-nonzero [s]
  (let [n (Integer/parseInt s)]
    (if-not (zero? n) n)))

(defn parse-double [s]
  (Double/parseDouble s))
