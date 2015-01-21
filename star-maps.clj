;; gorilla-repl.fileformat = 1

;; **
;;; # Simple star maps
;;; 
;;; We have a bright star catalog in the file "stars.dat".  This consists of a series of fairly complex four-line records. that look like
;;; 
;;;     39801 2061 5.91953 7.40694 0.5 1.85 58      
;;;     $\alpha$    # name
;;;     ORI         # constellation
;;;     M1-2Ia-Iab  # spectral class
;;;     
;;; Needless to say, we'll write a grammar!
;; **

;; @@
(ns parsers.stars
  (:use parsers.utils
        parsers.starmap)
  (:require [instaparse.core :as insta]))
;; @@

;; @@
(def stars-parser
  (insta/parser
   "stars = star*
    star = line1 <newline> label <newline> constellation <newline> spec-class <newline>
    <line1> = (<'0'> | hd-number) <space> (<'0'> |bsc-number) <space> 
              ra <space> dec <space> 
              visual-brightness <space> bv-brightness <space> 
              (<'0'> | flam-number)

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
    <int> = #'[1-9][0-9]*'
    <float> = #'-?[0-9]+(\\.[0-9]+)?'
    <line> = #'[^\\n]*'"))
;; @@

;; @@
(def stars-tree (stars-parser (slurp "data/stars.dat")))
;; @@

;; **
;;; Let's take a look...
;; **

;; @@
(take 2 stars-tree)
;; @@

;; **
;;; Again, we'll use `transform` to flatten some of the structure and parse the numbers.
;; **

;; @@
(def star-xfrm-map
  {:stars             vector
   :star              (fn [& l] (into {} l))
   :hd-number         (fn [x] [:hd-number (parse-int x)])
   :visual-brightness (fn [x] [:visual-brightness (parse-double x)])
   :bv-brightness     (fn [x] [:bv-brightness (parse-double x)])
   :flam-number       (fn [x] [:flam-number (parse-int x)])
   :bsc-number        (fn [x] [:bsc-number (parse-int x)])
   :ra                (fn [x] [:ra (parse-double x)])
   :dec               (fn [x] [:dec (parse-double x)])})
;; @@

;; @@
(def stars (insta/transform star-xfrm-map stars-tree))
;; @@

;; **
;;; ...which gives us some fairly tractable-looking Clojure maps.
;; **

;; @@
(first stars)
;; @@

;; **
;;; Use the `star-map` function to get a simple orphographic projection (adjust `:dec0` and `:ra0` to rotate if you like.)
;; **

;; @@
(star-map stars {:dec0 20 :ra0 2.5})
;; @@

;; **
;;; Could looking at constellations help me get my bearings?
;; **

;; @@
(def stars-by-const (group-by :constellation stars))
;; @@

;; @@
(def orion (stars-by-const "ORI"))
;; @@

;; @@
(star-map orion {:ra0 2 :dec0 20})
;; @@

;; **
;;; Fortunately, we've also got a set of "constellation lines", which probably come closer to what you normally think of when talking about constellations....
;;; 
;;;      ORI 19  ORI 34  ORI 24 ;
;;;      ORI 53  ORI 50  ORI 46  ORI 34 ;
;;;      ORI 50  ORI 58 ;
;;;      
;;; This format uses semicolons as record-terminators, and also allows '#" as a comment character.
;; **

;; @@
(def lines-file (slurp "data/lines.dat"))
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

;; **
;;; Use the `:lines` option to the `star-map` function to draw any constellation lines which can be found in the stars you provide.
;; **

;; @@
(star-map orion {:ra0 6 :lines consts})
;; @@

;; @@
(star-map stars {:ra0 6 :dec -20 :lines consts})
;; @@

;; @@

;; @@
