(ns om-next-tutorial.core
  (:require [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]))



;;; CLJS utils

(enable-console-print!)



;;; Blabla

(declare reconciler AnimalsList)

;;; Data management

; State

(def initial-state {:app/title    "Animals"
                    :animals/list [[1 "Ant"] [2 "Antelope"] [3 "Bird"] [4 "Cat"] [5 "Dog"]
                                   [6 "Lion"] [7 "Mouse"] [8 "Monkey"] [9 "Snake"] [10 "Zebra"]]})

(defonce app-state (atom initial-state))

(defn reset-state! []
  (reset! app-state initial-state))

; Read

(defmulti read-fn om/dispatch)

(defmethod read-fn :default
  [{:keys [state]} key _]
  (let [st @state]
    (if-let [[_ value] (find st key)]
      {:value value}
      {:value :not-found})))

(defmethod read-fn :animals/list
  [{:keys [state]} _ {:keys [start end]}]
  {:value (subvec (:animals/list @state)
                  start
                  (min end (count (:animals/list @state))))})

; Mutate

(defmulti mutate-fn om/dispatch)

(defmethod mutate-fn 'animal/remove
  [{:keys [state]} _ {:keys [animal]}]
  {:action (fn []
             (swap! state update-in [:animals/list] (fn [st]
                                                      (into [] (remove #(= % animal) st)))))})

(defmethod mutate-fn 'global/reset
  [_ _ _]
  {:action reset-state!})

(def reconciler
  (om/reconciler {:state  app-state
                  :parser (om/parser {:read read-fn :mutate mutate-fn})}))



;;; UI

(defui AnimalsList
  static om/IQueryParams
  (params [_]
    {:start 0 :end 10})

  static om/IQuery
  (query [_]
    '[:app/title (:animals/list {:start ?start :end ?end})])

  Object
  (render [this]
    (let [{:keys [app/title animals/list]} (om/props this)]
      (dom/div nil
               (dom/h2 nil title)
               (apply dom/ul nil
                      (map
                        (fn [[i name]]
                          (dom/li nil
                                  [i
                                   " "
                                   name
                                   " "
                                   (dom/a
                                     #js {:href    "#"
                                                   :key     i
                                                   :onClick (fn [e]
                                                     (.preventDefault e)
                                                     (om/transact! this `[(animal/remove ~{:animal [i name]})]))}
                                     "x")]))
                        list))
               (dom/button
                 #js {:onClick (fn [_]
                                 (om/transact! this `[(global/reset)]))}
                 "RESET !")))))

(om/add-root! reconciler AnimalsList (gdom/getElement "app"))

