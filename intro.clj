;; gorilla-repl.fileformat = 1

;; **
;;; # Instaparse intro
;;; 
;;; Welcome to gorilla :-)
;;; 
;;; Shift + enter evaluates code. Hit ctrl+g twice in quick succession or click the menu icon (upper-right corner) for more commands ...
;; **

;; @@
(ns parsers.intro
  (:require [instaparse.core :as insta]
            [parsers.utils :refer :all]))
;; @@

;; @@
(def xyz
  (insta/parser
    "S=(X|Y|Z)*
     X='x'+
     Y='y'+
     Z='z'+"))
;; @@

;; @@
(xyz "xyzzy")
;; @@

;; @@
(insta/parses xyz "xyzzy")
;; @@

;; @@
(def minilisp
  (insta/parser
    "s-expr = '(' expr (whitespace expr)* ')'
     expr = s-expr | symbol | number
     symbol = letter (letter | digit | '-')*
     number = digit+
     letter = #'[a-zA-Z]'
     digit = #'[0-9]'
     whitespace = #'\\s+'"))
;; @@

;; @@
(minilisp "(foo 42)")
;; @@

;; @@
(def minilisp2
  (insta/parser
    "s-expr = <'('> expr (<whitespace> expr)* <')'>
     <expr> = s-expr | symbol | number
     symbol = letter (letter | digit | '-')*
     number = digit+
     <letter> = #'[a-zA-Z]'
     <digit> = #'[0-9]'
     whitespace = #'\\s+'"))
;; @@

;; @@
(minilisp2 "(foo 42)")
;; @@

;; @@
(minilisp2 "(foo (times 7 6))")
;; @@

;; @@
(insta/transform 
  {:symbol (comp symbol str)
   :number (comp parse-int str)
   :s-expr list}
  (minilisp2 "(foo (times 7 6))"))
;; @@

;; @@

;; @@
