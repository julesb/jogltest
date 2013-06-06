(ns jogltest.vbo-test
  (:import (java.nio IntBuffer FloatBuffer))
  (:import (javax.media.opengl GL2 GL GLAutoDrawable GLEventListener))
  (:import (com.sun.opengl.util.BufferUtil))
  (:use [jogltest.vector]))


(def buffer-ids (atom {}))

(def tri [[0 -1 0] [1 1 0] [-1 1 0]])
(def tri-color [[1 0 0 0.5] [0 1 0 0.5] [0 0 1 0.5]])
(def rand-scale 100)
(def num-tris 1000000)

(defn make-random-positions []
  (vec (map (fn [_] (vec [(* (- (rand) 0.5) rand-scale 2.0)
                          (* (- (rand) 0.5) rand-scale 2.0)
                          (* (- (rand) 0.5) rand-scale 2.0)]))
            (range num-tris))))

(defn make-rand-triangles []
  (->> (make-random-positions)
       (map #(vec [(vec3-add % (tri 0))
                   (vec3-add % (tri 1))
                   (vec3-add % (tri 2))]))
    flatten
    vec))

(defn make-tri-colors []
  (vec (flatten (repeat num-tris tri-color))))

(defn make-tri-colors2 []
  (vec (flatten (map (fn [_] (vec [[(rand) (rand) (rand) 0.2]
                                   [(rand) (rand) (rand) 0.2]
                                   [(rand) (rand) (rand) 0.2]]))
                     (range num-tris)))))

(defn make-tri-colors3 []
  (vec (flatten (map (fn [_] (let [[r g b a] [(rand) (rand) (rand) 0.5] ]
                                (vec [[r g b a] [r g b a] [r g b a]])))
                     (range num-tris)))))


(defn vbo-supported? [gl]
  (and 
    (.isFunctionAvailable gl "glGenBuffers")
    (.isFunctionAvailable gl "glBindBuffer")
    (.isFunctionAvailable gl "glBufferData")
    (.isFunctionAvailable gl "glDeleteBuffers")))


(defn generate-vbo [gl key]
  (let [id-array (int-array 1)]
    (.glGenBuffers gl 1 id-array 0)
    (reset! buffer-ids (assoc @buffer-ids key (first id-array)))
    (println (str "generated buffer " (first id-array)))
    (first id-array)))



(defn get-vbo [key]
  (key @buffer-ids))


(defn init [drawable]
  (let [gl (.getGL2 (.getGL drawable))
        float-size (/ Float/SIZE Byte/SIZE)
        int-size (/ Integer/SIZE Byte/SIZE)
        ]

    (.glEnableClientState gl GL2/GL_VERTEX_ARRAY)
    
    ;; Create the vertex bufer
    (let [vertex-buffer (float-array (make-rand-triangles))]
      (generate-vbo gl :vertex-buffer)
      (.glBindBuffer gl GL2/GL_ARRAY_BUFFER (get-vbo :vertex-buffer))
      (.glBufferData gl
                     GL2/GL_ARRAY_BUFFER
                     (* (alength vertex-buffer) float-size)
                     (FloatBuffer/wrap vertex-buffer)
                     GL2/GL_STATIC_DRAW))

    ;; Create index buffer
    (let [index-buffer (int-array (vec (range (* num-tris 3))))]
      (generate-vbo gl :index-buffer)
      (.glBindBuffer gl GL2/GL_ELEMENT_ARRAY_BUFFER (get-vbo :index-buffer))
      (.glBufferData gl
                     GL2/GL_ELEMENT_ARRAY_BUFFER
                     (* (alength index-buffer) int-size)
                     (IntBuffer/wrap index-buffer)
                     GL2/GL_STATIC_DRAW))
    
    ;; create color buffer
    (let [color-buffer (float-array (make-tri-colors2))]
      (generate-vbo gl :color-buffer)
      (.glBindBuffer gl GL2/GL_ARRAY_BUFFER (get-vbo :color-buffer))
      (.glBufferData gl
                     GL2/GL_ARRAY_BUFFER
                     (* (alength color-buffer) float-size)
                     (FloatBuffer/wrap color-buffer)
                     GL2/GL_STATIC_DRAW))

    ;(println "vertex-buffer:" (java.util.Arrays/toString @vertex-buffer))
    ;(println "color-buffer:" (java.util.Arrays/toString @color-buffer))
  ))
    

(defn dispose [drawable]
  (println "dispose")
  (.glDeleteBuffers (.getGL2 (.getGL drawable))
                    1 (int-array (get-vbo :vertex-buffer)) 0))


;(defn render-im [gl]
;  (doto gl
;    (.glColor4f 0.0 0.0 1.0 0.5)
;    (.glBegin GL2/GL_TRIANGLES)
;    (.glVertex3f (aget @vertex-buffer 0)
;                 (aget @vertex-buffer 1)
;                 (aget @vertex-buffer 2))
;    (.glVertex3f (aget @vertex-buffer 3)
;                 (aget @vertex-buffer 4)
;                 (aget @vertex-buffer 5))
;    (.glVertex3f (aget @vertex-buffer 6)
;                 (aget @vertex-buffer 7)
;                 (aget @vertex-buffer 8))
;    (.glEnd)))


(defn render-vbo [gl]
  (doto gl
    (.glEnableClientState GL2/GL_INDEX_ARRAY)
    (.glBindBuffer GL2/GL_ARRAY_BUFFER (get-vbo :index-buffer))
    
    (.glEnableClientState GL2/GL_COLOR_ARRAY)
    ;(.glEnable GL2/GL_COLOR_MATERIAL)
    (.glBindBuffer GL2/GL_ARRAY_BUFFER (get-vbo :color-buffer)) 
    (.glColorPointer 4 GL2/GL_FLOAT 0 (long 0))
    
    (.glEnableClientState GL2/GL_VERTEX_ARRAY)
    (.glBindBuffer GL2/GL_ARRAY_BUFFER (get-vbo :vertex-buffer))
    (.glVertexPointer 3 GL2/GL_FLOAT 0 (long 0))
  
    (.glDrawArrays GL2/GL_TRIANGLES 0 (long (* num-tris 3)))

    (.glDisableClientState GL2/GL_VERTEX_ARRAY)
    (.glDisableClientState GL2/GL_INDEX_ARRAY)
    (.glDisableClientState GL2/GL_COLOR_ARRAY)
    
    (.glBindBuffer GL2/GL_ARRAY_BUFFER 0)
    (.glFlush)

     ))
  
