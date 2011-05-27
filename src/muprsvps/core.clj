(ns muprsvps.core
  (:use [clojure.contrib.json :only (read-json)]
        [clojure.contrib.duck-streams :only (reader read-lines)]
        [clojure.string :only (trim)])
  
  (:import (java.awt Color Graphics Frame RenderingHints)
           (java.net Socket SocketException)
           (java.io InputStream OutputStream)))

(defn- merge-topics
  [r]
  (assoc-in r [:group :group_topics]
            (apply hash-set (map :urlkey (:group_topics (:group r))))))

(defn rsvp-stream-from-file
  "Gets RSVPs from a file, and filters them"
  [file]
  (map (comp merge-topics read-json)
       (read-lines file)))

;;; W - E
;;; (-130 - -60)
;;; S - N
;;; (25 - 50)

(defn map-coords
  [lat long width height]
  (let [x (- 70 (- -60 long))
        y (- 50 lat)
        dx (/ width 70)
        dy (/ height 25)]
    [(* x dx) (* y dy)]))

(defn continental-usa?
  [lat long]
  (and (> long -130) (< long -60)
       (> lat 25) (< lat 50)))


(def colors {"yes" (Color. 0 0 255 100)
             "no" (Color. 255 0 0 100)})

(defn draw
  [graphics rsvps width height]
  (loop [rsvps (filter #(continental-usa?
                         (:group_lat (:group %))
                         (:group_lon (:group %)))
                       rsvps)]
    (when (seq rsvps)
      (let [rsvp (first rsvps)
            [x y] (map-coords
                   (:group_lat (:group rsvp))
                   (:group_lon (:group rsvp))
                   width
                   height)]
        (.setColor graphics (colors (:response rsvp)))
        (.fillOval graphics (- x 5) (- y 5) 10 10)
        (java.lang.Thread/sleep 20)
        (recur (rest rsvps))))))


;;; max_r - (long_m - long_a) => x
;;; 50 - 25

(comment
  (def frame (Frame.))
  (doto frame
    (.setSize (java.awt.Dimension. 800 600))
    (.show))
  (def graphics (.getGraphics frame))
  (draw graphics rsvps 800 600))