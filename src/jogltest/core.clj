(ns jogltest.core
    (:gen-class)
    (:import (java.nio IntBuffer FloatBuffer))
    (:import (java.awt Frame))
    (:import (java.awt.event WindowAdapter))
    (:import (java.awt.event KeyListener KeyEvent))
    (:import (javax.media.opengl.awt GLCanvas))
    (:import (javax.media.opengl.fixedfunc GLMatrixFunc))
    (:import (javax.media.opengl GL2 GL GLAutoDrawable GLEventListener))
    (:import (com.jogamp.opengl.util Animator ))
    (:import (com.jogamp.opengl.util.awt TextRenderer))
    (:import (java.awt Font))
    (:use [jogltest.vbo-test :as vbo-test])
  )

(defn throw-str [s]
  (throw (Exception. s)))


(def ^:dynamic canvas (GLCanvas.))
(def ^:dynamic frame (Frame.))
(def ^:dynamic animator (Animator. canvas))

(def frames-per-sec (atom -99))

;(defn jogl-drawble []
;  (reify GLAutoDrawable
;    (getWidth [this]
;      256)))

(def run-time (atom 0.0))

(defn exit []
  (println "bye")
  (.stop animator)
  (.dispose frame)
  (def animator nil)
  (def canvas nil)
  (def frame nil))


(defn do-fog [gl]
  (let [fog-col [0.0 0.0 0.0 0]
        fog-dist 200]
    (doto gl
      (.glEnable GL2/GL_FOG)
      (.glFogi GL2/GL_FOG_MODE GL/GL_LINEAR)
      (.glFogf GL2/GL_FOG_DENSITY (float 0.5))
      (.glFogfv GL2/GL_FOG_COLOR (float-array fog-col) (float 0.0))
      (.glFogf GL2/GL_FOG_START (- fog-dist 100))
      (.glFogf GL2/GL_FOG_END fog-dist)
      (.glHint GL2/GL_FOG_HINT GL/GL_NICEST))))


(defn draw-quad [gl]
  (doto gl
      (.glPushMatrix)
      ;(.glTranslatef 0.0 0.0 -100.1) 
      ;(.glScalef 0.5 0.5 0.5)
      ;(.glRotatef @run-time 0.0 1.0 1.0)
      (.glBegin GL2/GL_QUADS)
      (.glColor4f 0.0 0.0 0.0 0.25)
      (.glVertex3f -1.0  1.0 0.0)
      (.glVertex3f  1.0  1.0 0.0)
      (.glVertex3f  1.0 -1.0 0.0)
      (.glVertex3f -1.0 -1.0 0.0)
      (.glEnd)
      (.glPopMatrix)
  ))


(defn render [drawable]
  (let [gl (.. drawable getGL getGL2)]
    (do-fog gl)
    
    (doto gl
      (.glClearColor 0.0 0.0 0.0 1.0)
      (.glClearDepth 1.0)
      (.glEnable GL/GL_DEPTH_TEST)
      (.glClear GL/GL_DEPTH_BUFFER_BIT)
      (.glClear GL/GL_COLOR_BUFFER_BIT)
      (.glLoadIdentity)
      (.glTranslatef 0.0 0.0 -150.0)
      (.glRotatef @run-time 0.0 1.0 1.0)
    )
    
    (vbo-test/render-vbo gl)
    ;(draw-quad gl)

    (let [rnd (TextRenderer. (Font. "SansSerif" Font/PLAIN 14))]
      (.beginRendering rnd (.getWidth drawable) (.getHeight drawable))
      (.setColor rnd 1 1 1 1)
      (.draw rnd (str "fps:" @frames-per-sec) 10 60)
      (.draw rnd (str "tris:" vbo-test/num-tris) 10 40)
      (.draw rnd (str "tris/s:" (int (* vbo-test/num-tris @frames-per-sec))) 10 20)

      (.endRendering rnd)
      (.dispose rnd))

    ))


(defn key-pressed [e]
  (when (= (.getKeyCode e) (. KeyEvent VK_ESCAPE))
    (exit)))


(defn jogl-listener []
  (reify GLEventListener
    (display [this drawable]
      (when (nil? drawable)
        (throw-str "nil drawable"))
      (do
        (render drawable)
        (reset! frames-per-sec (.getLastFPS animator))
        (swap! run-time #(+ % 0.3))))

    (reshape [this drawable x y width height]
      (let [gl (.. drawable getGL getGL2)
            aspect (float (/ width height))
            fh 0.5
            fw (* fh aspect)]
        (doto gl
          (.glMatrixMode GLMatrixFunc/GL_PROJECTION)
          (.glLoadIdentity)
          (.glFrustum (- fw) fw (- fh) fh 1.0 1000.0)
          (.glMatrixMode GLMatrixFunc/GL_MODELVIEW)
          (.glLoadIdentity))))

    (dispose [this drawable]
      (vbo-test/dispose drawable))

    (init [this drawable]
      (.addKeyListener
        drawable
        (proxy [KeyListener] []
          (keyPressed [e]
            (key-pressed e))
          (keyTyped [e]
            (println (.getKeyChar e) ))
          (keyReleased [e])
        )
      )
      (println "VBO supported:" (vbo-test/vbo-supported? (.. drawable getGL getGL2)))
      
      (.setUpdateFPSFrames (.getAnimator drawable) 30 nil)
      ; drawable.getAnimator().setUpdateFPSFrames(3, null);

      (doto (.getGL2 (.getGL drawable))
        (.glEnable GL/GL_BLEND)
        (.glBlendFunc GL/GL_SRC_ALPHA  GL/GL_ONE_MINUS_SRC_ALPHA ))
      (vbo-test/init drawable)
    )))


(defn add-window-listener [component]
  (let [listener (proxy [WindowAdapter] []
                   (windowClosing [event]
                     (exit)
                     ))]
    (.addWindowListener component listener)
    listener))
    

(defn main []
  (do
    (def frame (Frame.))
    (def canvas (GLCanvas.))
    (def animator (Animator. canvas))
    (.addGLEventListener canvas (jogl-listener))
    (doto frame
        (.add canvas)
        (.setSize 640 480)
        (.setResizable true)
        (add-window-listener)
        (.setVisible true))
    ;(.setRunAsFastAsPossible animator true)
    (.start animator)
    (.requestFocus canvas)))

;(main)



(defn -main [& args]
  (println "hello jogl, goodbye processing")
  (main))

