(ns om-next-tutorial.core
  (:require [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]))



;;; CLJS utils

(enable-console-print!)



;;; Data management

(def initial-state
  {:list/one [{:name "John" :points 0}
              {:name "Mary" :points 0}
              {:name "Bob" :points 0}]
   :list/two [{:name "Mary" :points 0 :age 27}
              {:name "Gwen" :points 0}
              {:name "Jeff" :points 0}]})

(defmulti read om/dispatch)

(defn get-people [state key]
  (let [st @state]
    (into [] (map #(get-in st %)) (get st key))))

(defmethod read :list/one
  [{:keys [state]} key params]
  {:value (get-people state key)})

(defmethod read :list/two
  [{:keys [state]} key params]
  {:value (get-people state key)})


(defmulti mutate om/dispatch)

(defmethod mutate 'points/increment
  [{:keys [state]} _ {:keys [name]}]
  {:action (fn []
             (swap! state update-in
                    [:person/by-name name :points]
                    inc))})

(defmethod mutate 'points/decrement
  [{:keys [state]} _ {:keys [name]}]
  {:action (fn []
             (swap! state update-in
                    [:person/by-name name :points]
                    #(let [n (dec %)] (max n 0))))})

;; UI

(defui Person
  static om/Ident
  (ident [this {:keys [name]}]
    [:person/by-name name])

  static om/IQuery
  (query [this]
    '[:name :points])

  Object
  (render [this]
    (println "Render Person" (-> this om/props :name))
    (let [{:keys [points name] :as props} (om/props this)]
      (dom/li nil
              (dom/label nil (str name ", points: " points))
              (dom/button
                #js {:onClick (fn [_]
                                (om/transact! this `[(points/increment ~props)]))}
                "+")
              (dom/button
                #js {:onClick (fn [_]
                                (om/transact! this `[(points/decrement ~props)]))}
                "-")))))

(def person (om/factory Person {:keyfn :name}))


(defui ListView
  Object
  (render [this]
    (println "Render ListView" (-> this om/path first))
    (let [list (om/props this)]
      (apply dom/ul nil
             (map person list)))))

(def list-view (om/factory ListView))


(defui RootView
  static om/IQuery
  (query [this]
    (let [subquery (om/get-query Person)]
      `[{:list/one ~subquery} {:list/two ~subquery}]))

  Object
  (render [this]
    (println "Render RootView")
    (let [{:keys [list/one list/two]} (om/props this)]
      (apply dom/div nil
             [(dom/h2 nil "List One")
              (list-view one)
              (dom/h2 nil "List Two")
              (list-view two)]))))


(def reconciler (om/reconciler {:state initial-state
                                :parser (om/parser {:read read :mutate mutate})}))

(om/add-root! reconciler
              RootView (gdom/getElement "app"))