(ns puzzle.cljs.core
  (:require [reagent.core :as r]
            [goog.dom :as dom]))

(enable-console-print!)

(def completed? (r/atom false))

(defn clamped-rand-int [min max]
  (+ min (rand-int (- max min))))

(defn xy [id]
  (let [elem (dom/getElement id)]
    {:x (.-offsetLeft elem)
     :y (.-offsetTop elem)}))

(defn remove-drag-shadow [e]
  (.setDragImage (.-dataTransfer e)
                 (.getElementById js/document "transparent-pixel")
                 0
                 0))

(defn id->coords [id e]
  (let [click-x (.-pageX e)
        click-y (.-pageY e)
        {:keys [x y]} (xy id)]
    {:x (- click-x x)
     :y (- click-y y)}))

(defn on-drag-start-fn [id angle valid-offset? z-index offset-x offset-y]
  (fn [e]
    (println "yo2")
    (remove-drag-shadow e)
    (reset! z-index 110)
    (let [{:keys [x y]} (id->coords id e)]
      (reset! offset-x x) 
      (reset! offset-y y)
      (when-not (valid-offset? x y @angle)
        (swap! z-index dec)
        (.preventDefault e)))))

(defn on-drag-fn [id x y offset-x offset-y]
  (fn [e]
    (println "yo3")
    (let [x1 (.-clientX e)
          y1 (.-clientY e)]
      (when-not (and (zero? x1) (zero? y1))
        (reset! x (- x1 @offset-x))
        (reset! y (- y1 @offset-y))))))

(defn shape [id src height width valid-offset?]
  (let [x (r/atom (clamped-rand-int 0 (- (.-innerWidth js/window) width)))
        y (r/atom (clamped-rand-int 0 (- (.-innerHeight js/window) height)))
        offset-x (r/atom 0)
        offset-y  (r/atom 0)
        z-index (r/atom 109)
        angle (r/atom 0)
        focus? (r/atom false)]
    (fn [id src height width valid-offset?]
      [:img.absolute
       {:id id
        :tabIndex 0
        :on-focus #(reset! focus? true)
        :on-blur #(reset! focus? false)
        :on-key-down (fn [e]
                       (println "yo!!")
                       (when (= (.-key e) "r")
                         (swap! angle (partial + 90))))
        :on-drag-start (on-drag-start-fn id angle valid-offset? z-index offset-x offset-y)
        :on-drag (on-drag-fn id x y offset-x offset-y)
        :style {:height (str height "px")
                :width (str width "px")
                :top (str @y "px")
                :left (str @x "px")
                :z-index @z-index
                :opacity (if @focus? 0.8 1)
                :transform (str "rotate(" @angle "deg)")
                :transition "transform 0.2s ease-in-out"
                :cursor :pointer
                :user-select "none"
                :outline "none"}
        :src (str "/assets/images/puzzle/" src)}])))

(def nop (constantly true))

(defn valid-gray? [x y]
  (and (<= y (+ 75 x))
       (>= y (+ 75 (- x)))))

(defn valid-navy? [x y]
  (and (>= y (+ 150 (- x)))
       (>= y (- x 150))))

(defn valid-green? [x y]
  (and (<= y (- 225 x))
       (>= y (- 75 x))
       (>= y (- x 75))
       (<= y (+ 75 x))))

(defn valid-red? [x y]
  (and (<= y x)
       (>= y (- x 150))))

(defn valid-teal? [x y]
  (<= y x))

(defn valid-yellow? [x y]
  (and
    (<= y (- 300 x))
    (>= y x)))

(defn valid-blue? [x y]
  (and
    (<= y x)
    (<= y (- 150 x))))


(defn contained? [id min-x min-y]
  (let [{:keys [x y]} (xy id)]
    (and (<= min-x x (+ min-x 175))
         (<= min-y y (+ min-y 175)))))

(defn quad2? [id ol ot]
  (contained? id ol ot)) 

(defn quad1? [id ol ot]
  (contained? id (+ ol 175) ot))

(defn quad3? [id ol ot]
  (contained? id ol (+ ot 175)))

(defn quad4? [id ol ot]
  (contained? id (+ ol 175) (+ ot 175)))

(defn success? []
  (let [box-elem (dom/getElement "box")
        ol (.-offsetLeft box-elem)
        ot (.-offsetTop box-elem)
        yellow? (quad2? "yellow" ol ot)
        blue? (quad2? "blue" ol ot)
        red? (quad2? "red" ol ot)
        green? (quad1? "green" ol ot)
        teal? (quad1? "teal" ol ot)
        navy? (quad3? "navy" ol ot)
        gray? (quad4? "gray" ol ot)]
    (and
      (every? #(quad2? % ol ot) ["yellow" "blue" "red"])
      (every? #(quad1? % ol ot) ["green" "teal"])
      (quad3? "navy" ol ot)
      (quad4? "gray" ol ot))))

(defn body []
  [:div.relative.flex.justify-center.items-center.flex-column {:on-click #(println "Yoooooo")
                                                               :style {:z-index 100}}
   [:div#box.border.flex.justify-center.items-center.flex-column
    {:style {:border-width "10px"
             :border-radius "10px"
             :height "350px"
             :width "350px"
             :color "black"}}]
   [:div.transition.pt3.center
    {:style {:opacity (if @completed? 1 0)
             :user-select "none"}}
    [:h1 "No manches, paisano!"]
    [:h2 "You did it."]]
   [shape "gray" "gray.svg" 150 75 valid-gray?]
   [shape "green" "green.svg" 150 150 valid-green?]
   [shape "navy" "navy.svg" 150 300 valid-navy?]
   [shape "red" "red.svg" 75 225 valid-red?]
   [shape "teal" "teal.svg" 150 150 valid-teal?]
   [shape "yellow" "yellow-rt.svg" 300 150 valid-yellow?]
   [shape "blue" "blue-rt.svg" 75 150 valid-blue?]
   [:img#transparent-pixel 
    {:src "data:image/gif;base64,R0lGODlhAQABAAAAACH5BAEKAAEALAAAAAABAAEAAAICTAEAOw=="}]])

(defn schedule-verify-success []
  (js/setInterval (fn []
                    (reset! completed? (success?)))
                  300))

(defn -main []
  (r/render-component [body]
                      (dom/getElement "app")
                      schedule-verify-success))

(-main)

