(ns cljs.x.or.fino.main
  (:require [cljs.x.or.fino.util :as util]
            [enfocus.core :as ef]
            [clojure.browser.repl :as repl])
  (:require-macros [enfocus.macros :as em]))

;; (repl/connect "http://localhost:9000/repl")

(def item-id
  (->> js/document .-location (re-find #"/item/(\d+)") second)

(defn show-error [self]
  (js/alert (.getLastError self)))

(em/defsnippet ajax-loader "/html/fragments.html" [:#ajax-loader] [])

(defn delete-event->category-id [evt]
  (loop [node (.-target evt)]
    (cond 
     (nil? node) nil
     (= (.-nodeName node) "SPAN") (clojure.string/replace (.-id node) "label-" "")
     :else (recur (.-parentNode node)))))

(em/defaction delete-label [category-id]
  [(str "#label-" category-id)] (util/forget-element))

(defn ^:export  try-delete-item-label [category-id]
  (util/post-data-fail-safe (str "/api/item/" item-id "/label/" category-id "/delete")
                  #(delete-label category-id)
                  show-error))

(em/defsnippet label-item-label "/html/fragments.html" [:#label-item-label]
  [category-id category-title]
  [:span] (em/set-attr :id (str "label-" category-id))
  [:#title] (em/content category-title)
  [:#remove] (em/do-> (em/set-attr :href "#")
                      (em/listen :click #(try-delete-item-label category-id))))

(em/defaction item-labeled [category-id category-title]
  [:#label] (em/append (label-item-label category-id category-title)))

(defn ^:export try-label-item [category-id category-title]
  (util/post-data-fail-safe (str "/api/item/" item-id "/label/" category-id)
                  #(item-labeled category-id category-title)
                  show-error))

(em/defsnippet label-item-category "/html/fragments.html" [:#category]
  [{:keys [id title]}]
  [:#category] (em/do-> (em/set-attr :href "#")
                        (em/listen :click #(try-label-item id title))
                        (em/content title)
                        (em/after " ")))
               
(em/defsnippet show-categories "/html/fragments.html" [:#label-item-categories]
  [{:keys [id title categories]}]
  [:#header] (em/content title " categories")
  [:#categories] (em/content (doall (map label-item-category categories))))

(em/defaction domain-selected [item]
  [:#item-label-categories-place] (em/content (show-categories item))
  [:#ajax-loader] (util/forget-element))

(defn get-selected-domain [] 
  (:option
    (em/from js/document :option 
      ["select[name=selected-domain] option"] 
      (em/filter :selected (em/get-prop :value)))))

(defn ^:export try-select-domain []
  (let [domain-id (get-selected-domain)]
    (em/at js/document [:#item-label-categories-place] (em/content (ajax-loader)))
    (util/get-data (str "/api/item/" domain-id "/categories") domain-selected)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Entry point
;;

(em/defaction setup []
  ["select[name=selected-domain]"] (em/listen :change try-select-domain)
  ["* #remove"] (em/listen :click #(try-delete-item-label (delete-event->category-id %))))

(set! (.-onload js/window) setup) 
