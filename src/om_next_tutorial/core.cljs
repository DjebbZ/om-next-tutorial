(ns om-next-tutorial.core
  (:require [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]))



;;; CLJS utils

(enable-console-print!)



;;; Data management

(defonce app-state (atom {:count 0}))

(defn read-fn [{:keys [state]} key _]
  (let [st @state]
    (if-let [[_ value] (find st key)]
      {:value value}
      {:value :not-found})))

(defn mutate-fn [{:keys [state]} key _]
  (if (= 'increment key)
    {:value  {:keys [:count]}
     :action #(swap! state update-in [:count] inc)}))

(def reconciler
  (om/reconciler {:state  app-state
                  :parser (om/parser {:read read-fn :mutate mutate-fn})}))



;;; Actions

(defn increment [comp-instance]
  (om/transact! comp-instance '[(increment)]))




;;; UI

(defui Counter
  static om/IQuery
  (query [_]
    [:count])

  Object
  (render [this]
    (let [{:keys [count]} (om/props this)]
      (dom/div nil
               (dom/span nil (str "Count: " count))
               (dom/button
                 #js {:onClick #(increment this)}
                 "Click me")))))

(om/add-root! reconciler Counter (gdom/getElement "app"))

(defn on-js-reload [])