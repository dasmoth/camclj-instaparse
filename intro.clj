;; gorilla-repl.fileformat = 1

;; **
;;; # Instaparse intro
;;; 
;;; This is a [Gorilla REPL](http://gorilla-repl.org/) worksheet.
;;; 
;;; Shift + enter evaluates code. Hit ctrl+g twice in quick succession or click the menu icon (upper-right corner) for more commands ...
;;; 
;;; Evaluate the namespace declaration before trying anything else!
;; **

;; @@
(ns parsers.intro
  (:require [instaparse.core :as insta]
            [parsers.utils :refer :all]))
;; @@

;; **
;;; Building parsers
;;; =========
;;; 
;;; Instaparse allows parses to be build from a set of "production rules" in EBNF format
;; **

;; @@
(def ab
  (insta/parser
    "S = A* B*
     A = 'a'
     B = 'b'"))
;; @@

;; **
;;; To parse a string, just use your parser as a function, e.g.:
;; **

;; @@
(ab "aaabb")
;; @@

;; **
;;; If the input doesn't match your grammar, you'll get back a map containing some reasonably intelligable error data, e.g.:
;; **

;; @@
(ab "aaabba")
;; @@

;; @@
(ab "aaaAbb")
;; @@

;; **
;;; Grammars can be ambiguous 
;; **

;; @@
(def xyz
  (insta/parser
    "S = (X|Y|Z)*
     X = 'x'+
     Y = 'y'+
     Z = 'z'+"))
;; @@

;; @@
(xyz "xyzzy")
;; @@

;; **
;;; The `parses` function allows you to see all possible parses.
;; **

;; @@
(insta/parses xyz "xyzzy")
;; @@

;; **
;;; Something a little more sophisticated
;;; ===========================
;;; 
;;; Note that we're using regexps for some production rules.  This is an easy way to recognize character classes, and also convenient if you want "greedy" terminal productions to make your grammar unambiguous.
;; **

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

;; **
;;; Angle brackets "hide" either the output of a production, or the production's label, in the parser output.
;; **

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

;; **
;;; The default output is reminiscent of Hiccup format (there is an enlive-like format as well, if you prefer that).  There are quite a few Clojure tools that are helpful for working with this (clojure.walk, core.match, etc).
;;; 
;;; Instaparse provides a `transform` function  which makes writing simple transformers quite easy.
;; **

;; @@
(def parsed-lisp (minilisp2 "(foo (times 7 6))"))

(insta/transform 
  {:symbol str}
  parsed-lisp)
;; @@

;; @@
(insta/transform 
  {:symbol (comp symbol str)
   :number (comp parse-int str)
   :s-expr list}
  parsed-lisp)
;; @@

;; @@

;; @@
