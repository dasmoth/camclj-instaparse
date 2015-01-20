(ns parsers.starmap
  (:use hiccup.core)
  (:require [instaparse.core :as ip]
            [gorilla-repl.html :refer (html-view)]
            [clojure.string :as str]))

(defn const-stars [stars const]
  (let [stars-by-fn (->> (for [{:keys [flam-number constellation] :as star} stars]
                           [(str constellation " " flam-number) star])
                         (into {}))
        const-stars (map stars-by-fn const)]
    (if (every? identity const-stars)
      const-stars)))

(defn star-map 
  ([stars] (star-map stars {}))
  ([stars {:keys [ra0 dec0 scale size lines] 
           :or {ra0 0.0 dec0 0.0 scale 0.95 size 500}}]
     (letfn 
       [(proj [ra dec]
          (let [ra    (* ra Math/PI 1/12)
                ra0   (* ra0 Math/PI 1/12)
                dec   (* dec Math/PI 1/180)
                dec0  (* dec0 Math/PI 1/180)
                delta-ra (- ra ra0)
                x1    (* (Math/cos dec)
                         (Math/sin delta-ra))
                y1    (- (* (Math/sin dec)
                            (Math/cos dec0))
                         (* (Math/cos dec)
                            (Math/cos delta-ra)
                            (Math/sin dec0)))
                z1    (+ (* (Math/sin dec)
                            (Math/sin dec0)
                            (Math/cos dec)
                            (Math/cos dec0)
                            (Math/cos delta-ra)))]

            [(* (+ 1 (* scale x1)) size 1/2)
             (* (+ 1 (* scale y1)) size 1/2)]))]
       
       (html-view
        (html
         [:svg {:width size :height size}
          [:rect {:x 0 :y 0 :width size :height size :fill "black"}]
          (for [{:keys [ra dec]} stars
                :let [[x y] (proj ra dec)]]
            (if-let [[x y] (proj ra dec)]
              [:circle {:cx x :cy y :r 1.0 :fill "white"}]))

          (let [lines (->> (map (partial const-stars stars) lines)
                           (filter identity))]
            (for [line lines
                  :let [[fp & rp] (map (fn [{:keys [ra dec]}]
                                         (proj ra dec))
                                       line)]]
              [:path 
               {:stroke "yellow"
                :fill "none"
                :d (str/join 
                    (concat 
                     ["M " (first fp) " " (second fp) " "]
                     (mapcat (fn [[x y]]
                               [" L " x " " y])
                             rp)))}]))])))))
