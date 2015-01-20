;; gorilla-repl.fileformat = 1

;; **
;;; # Simple star maps
;;; 
;;; We have a bright star catalog in the file "stars.dat".
;; **

;; @@
(ns parsers.stars
  (:use parsers.utils
        parsers.starmap)
  (:require [instaparse.core :as insta]))
;; @@

;; @@
(use 'parsers.utils :reload)
;; @@

;; @@
(def stars-parser
  (insta/parser
   "stars = star*
    star = line1 <newline> label <newline> constellation <newline> spec-class <newline>
    <line1> = hd-number <space> bsc-number <space> 
              ra <space> dec <space> 
              visual-brightness <space> bv-brightness <space> 
              flam-number

    hd-number = int
    bsc-number = int
    ra = float
    dec = float
    visual-brightness = float
    bv-brightness = float
    flam-number = int

    label = line
    spec-class = line
    constellation = line

    newline = #'\\n'
    space = ' '
    <int> = #'-?[0-9]+'
    <float> = #'-?[0-9]+(\\.[0-9]+)?'
    <line> = #'[^\\n]*'"))
;; @@

;; @@
(def stars-tree (stars-parser (slurp "data/stars.dat")))
;; @@

;; @@
(def xfrm-map
  {:stars             vector
   :star              (fn [& l] (into {} l))
   :hd-number         (fn [x] [:hd-number (parse-int x)])
   :visual-brightness (fn [x] [:visual-brightness (parse-double x)])
   :bv-brightness     (fn [x] [:bv-brightness (parse-double x)])
   :flam-number       (fn [x] [:flam-number (parse-nonzero x)])
   :bsc-number        (fn [x] [:bsc-number (parse-int x)])
   :ra                (fn [x] [:ra (parse-double x)])
   :dec               (fn [x] [:dec (parse-double x)])})
;; @@

;; @@
(def stars (insta/transform xfrm-map stars-tree))
;; @@

;; @@
(star-map stars {:dec0 20 :ra0 2.5})
;; @@

;; @@
(def stars-by-const (group-by :constellation stars))
;; @@

;; @@
(def orion (stars-by-const "ORI"))
;; @@

;; @@
(star-map orion {:ra0 6})
;; @@

;; @@
(def lines-file (slurp "data/lines.dat"))
;; @@

;; @@
(def line-parser
  (insta/parser
    "F = L*
     L = #'[^\\n]*\\n'"))
;; @@

;; @@
(line-parser "aa a\nbbb\n")
;; @@

;; @@
(count (line-parser lines-file))
;; @@

;; @@
(def const-parser
  (insta/parser
    "consts = line* <comment?>
     line = (whitespace? (star | <comment>))* <#';\\s*'>
     star = #'\\w+  ?\\w+' <whitespace>
     <comment> = #'#.*\\n'
     <whitespace> = #'\\s*'"))
;; @@

;; @@
(def const-tree (const-parser lines-file))
;; @@

;; @@
(def consts (insta/transform {:star identity
                              :line vector
                              :consts vector}
                             const-tree))
;; @@

;; @@
(take 2 consts)
;; @@

;; @@
(def orion-lines (filter identity (map (partial const-stars orion) consts)))
;; @@

;; @@
(star-map orion {:ra0 6 :lines consts})
;; @@

;; @@
(def all-lines (filter identity (map (partial const-stars stars) consts)))
;; @@

;; @@
(star-map stars {:ra0 5 :dec 20 :lines consts})
;; @@

;; @@

;; @@
