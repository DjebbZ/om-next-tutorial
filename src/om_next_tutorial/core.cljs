(ns om-next-tutorial.core
  (:require [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]))



;;; CLJS utils

(enable-console-print!)



;;; Data management

(defonce app-state (atom {:app/title    "Animals"
                          :animals/list [[1 "Ant"] [2 "Antelope"] [3 "Bird"] [4 "Cat"] [5 "Dog"]
                                         [6 "Lion"] [7 "Mouse"] [8 "Monkey"] [9 "Snake"] [10 "Zebra"]]}))

(defmulti read-fn (fn [env key params] key))

(defmethod read-fn :default
  [{:keys [state]} key _]
  (let [st @state]
    (if-let [[_ value] (find st key)]
      {:value value}
      {:value :not-found})))

(defmethod read-fn :animals/list
  [{:keys [state] :as env} key {:keys [start end]}]
  {:value (subvec (:animals/list @state) start end)})

(defn mutate-fn [{:keys [state] :as env} key {:keys [animal]}]
  (println (str "mutate fn. key " key))
  (println animal)
  (if (= 'remove-animal key)
    (do
      (println "act-remove-animal")
      {:value  {:keys [:animals/list]}
       :action (fn []
                 (println "before action")
                 (println animal)
                 (println @state)
                 (swap! state update-in [:animals/list] (fn [st]
                                                          (remove #(= % animal) st)))
                 (println "after action")
                 (println @state))})
    {:value nil}))

(def reconciler
  (om/reconciler {:state  app-state
                  :parser (om/parser {:read read-fn :mutate mutate-fn})}))



;;; UI

(defui AnimalsList
  static om/IQueryParams
  (params [_]
    {:start 0 :end (count (get-in @app-state [:animals/list]))})

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
                                   (dom/a
                                     #js {:href    "#"
                                          :key     i
                                          :onClick (fn [e]
                                                     (.preventDefault e)
                                                     (println "onClick handler")
                                                     (om/transact! this `[(remove-animal ~{:animal [i name]})]))}
                                     name)]))
                        list))))))

(om/add-root! reconciler AnimalsList (gdom/getElement "app"))

