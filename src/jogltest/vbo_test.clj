(ns jogltest.vbo-test
  (:import (java.nio IntBuffer FloatBuffer))
  (:import (javax.media.opengl GL2 GL GLAutoDrawable GLEventListener))
  (:import (com.sun.opengl.util.BufferUtil)))


(def buffer-ids (atom {}))
(def vertex-buffer (atom nil))
(def color-buffer (atom nil))


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
  (let [gl (.getGL2 (.getGL drawable))]
    (.glEnableClientState gl GL2/GL_VERTEX_ARRAY)
    ;; Create the vertex bufer
    (generate-vbo gl :vertex-buffer)

    (reset! vertex-buffer (float-array [ 0 -1 0
                                         1  1 0
                                        -1  1 0]))
     
    (.glBindBuffer gl GL2/GL_ARRAY_BUFFER (get-vbo :vertex-buffer))
    (.glBufferData gl
                   GL2/GL_ARRAY_BUFFER
                   (* (alength @vertex-buffer) 4)
                   (FloatBuffer/wrap @vertex-buffer)
                   GL2/GL_STATIC_DRAW)

    ;; Create index buffer
    (generate-vbo gl :index-buffer)
    (.glBindBuffer gl GL2/GL_ELEMENT_ARRAY_BUFFER (get-vbo :index-buffer))
    (let [index-buffer (int-array [0 1 2])]
      (.glBufferData gl
                     GL2/GL_ELEMENT_ARRAY_BUFFER
                     (* (alength index-buffer) 4)
                     (IntBuffer/wrap index-buffer)
                     GL2/GL_STATIC_DRAW))
    
    ;; create color buffer
    (generate-vbo gl :color-buffer)
    (reset! color-buffer (float-array [ 1 0 0
                                        0 1 0
                                        0 0 1]))
    (.glBindBuffer gl GL2/GL_ARRAY_BUFFER (get-vbo :color-buffer))
    (.glBufferData gl
                   GL2/GL_ARRAY_BUFFER
                   (* (alength @color-buffer) 4)
                   (FloatBuffer/wrap @color-buffer)
                   GL2/GL_STATIC_DRAW)
    )
  (println "vertex-buffer:" (java.util.Arrays/toString @vertex-buffer))
  (println "color-buffer:" (java.util.Arrays/toString @color-buffer))
  )
    

(defn dispose [drawable]
  (println "dispose")
  (.glDeleteBuffers (.getGL2 (.getGL drawable))
                    1 (int-array (get-vbo :vertex-buffer)) 0))

(def rand-scale 10)
(def num-tris 100)
(def rand-positions (vec (map (fn [_] (vec [(* (- (rand) 0.5) rand-scale 2.0)
                                            (* (- (rand) 0.5) rand-scale 2.0)
                                            (* (- (rand) 0.5) rand-scale 2.0)]))
                              (range num-tris))))

(defn random-tris [gl] 
  (doseq [i (range (count rand-positions))]
    (let [scale 10
          xpos ((rand-positions i) 0)
          ypos ((rand-positions i) 1)
          zpos ((rand-positions i) 2)]
      (doto gl
        (.glPushMatrix)
        (.glTranslatef xpos ypos zpos)
        (.glDrawArrays GL2/GL_TRIANGLES 0 3)
        (.glPopMatrix)
    ))))

(defn render-vbo [gl]
  (doto gl
    (.glColor4f 0.0 0.0 1.0 0.5)

(comment
    (.glBegin GL2/GL_TRIANGLES)
    (.glVertex3f (aget @vertex-buffer 0)
                 (aget @vertex-buffer 1)
                 (aget @vertex-buffer 2))
    (.glVertex3f (aget @vertex-buffer 3)
                 (aget @vertex-buffer 4)
                 (aget @vertex-buffer 5))
    (.glVertex3f (aget @vertex-buffer 6)
                 (aget @vertex-buffer 7)
                 (aget @vertex-buffer 8))
    (.glEnd)
)

;(comment
    (.glEnableClientState GL2/GL_INDEX_ARRAY)
    (.glBindBuffer GL2/GL_ARRAY_BUFFER (get-vbo :index-buffer))
    ; (.glDrawElements GL2/GL_TRIANGLES 3 GL2/GL_UNSIGNED_INT (long 0))
    
    (.glEnableClientState GL2/GL_COLOR_ARRAY)
    (.glEnable GL2/GL_COLOR_MATERIAL)
    (.glBindBuffer GL2/GL_ARRAY_BUFFER (get-vbo :color-buffer)) 
    (.glColorPointer 3 GL2/GL_FLOAT 0 (long 0))
    ;
    (.glBindBuffer GL2/GL_ARRAY_BUFFER (get-vbo :vertex-buffer))
    (.glEnableClientState GL2/GL_VERTEX_ARRAY)
    (.glVertexPointer 3 GL2/GL_FLOAT 0 (long 0))
  )
    (random-tris gl)
    ;(.glDrawArrays GL2/GL_TRIANGLES 0 3)
  (doto gl
    (.glDisableClientState GL2/GL_VERTEX_ARRAY)
    (.glDisableClientState GL2/GL_INDEX_ARRAY)
    (.glDisableClientState GL2/GL_COLOR_ARRAY)
    
    (.glBindBuffer GL2/GL_ARRAY_BUFFER 0)
    (.glFlush)
;)
     ))
  
