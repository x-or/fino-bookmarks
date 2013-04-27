(ns x.or.fino.api
  (:require [x.or.fino.model :as model]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [noir.session :as session]))



(defn item-categories [id]
  (str (model/load-item id)))
  
(defn add-item-label [id category-id]
  (model/add-item-label id category-id)
  ":done")

(defn delete-item-label [id category-id]
  (model/delete-item-label id category-id)
  ":done")

(defroutes api-routes
  (GET "/item/:id/categories" [id] (item-categories id))
  (POST "/item/:id/label/:category-id" [id category-id] (add-item-label id category-id))
  (POST "/item/:id/label/:category-id/delete" [id category-id] (delete-item-label id category-id)))
