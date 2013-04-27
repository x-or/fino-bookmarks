(ns x.or.fino.handler
  (:require [x.or.fino.model :as model]
            [x.or.fino.view :as view]
            [x.or.fino.api :as api]
            [compojure.core :refer :all]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.util.response :as resp]
            [noir.util.middleware :as noir]
            [noir.session :as session]))


(defn pint [s-int]
  (Integer/parseInt s-int))


(defn show-item-list []
  (->> (model/select-generic-item)
       (map model/load-label-into)
       (view/show-item-list)))

(defn show-domain-list []
  (->> (model/select-domain)
       (map model/load-label-into)
       (view/show-item-list)))

(defn show-item [id]
  (view/show-item (model/load-item (pint id))))

(defn edit-item [id]
  (view/edit-item (model/load-item (pint id))))

(defn label-item [id selected-domain]
  (let [selected-domain-item
        (try (model/load-item (pint selected-domain))
          (catch NumberFormatException e nil))]
    (view/label-item
      (model/load-item (pint id)) 
      (model/select-domain) 
      selected-domain-item)))

(defn delete-item [id]
  (model/delete-item (pint id))
  (resp/redirect "/items"))

(defn update-item [id title description uri]
  (let [item {:id (pint id), :title title, :description description, :uri uri}]
    (model/update-item item)
    (resp/redirect (str "/item/" id))))

(defn show-create-item [type]
  (view/show-create-item (model/ensure-item-type type)))

(defn create-item [item]
  (let [new-item-id (model/create-item item)]
    (condp = (model/ensure-item-type (:type item))
      model/item-type-generic (resp/redirect (str "/item/" new-item-id))
      model/item-type-domain (resp/redirect (str "/item/" new-item-id))
      model/item-type-category (resp/redirect (str "/domain/" (:domain-id item))))))

(defn show-create-category [domain-id]
  (view/show-create-category (model/load-item (pint domain-id))))

(defn add-item-label [id category-id]
  (model/add-item-label id category-id)
  (resp/redirect (str "/item/" id "/label")))

(defn delete-item-label [id category-id]
  (model/delete-item-label id category-id)
  (resp/redirect (str "/item/" id "/label")))

(defroutes app-routes

  (GET "/" [] (resp/redirect "/items"))
  (GET "/items" [] (show-item-list))
  (GET "/item/create" [type] (show-create-item type))
  (POST "/item/create" req (create-item (:params req)))
  (GET "/item/:id/edit" [id] (edit-item id))
  (GET "/item/:id/label" [id selected-domain] (label-item id selected-domain))
  (POST "/item/:id/label/:category-id" [id category-id] (add-item-label id category-id))
  (POST "/item/:id/label/:category-id/delete" [id category-id] (delete-item-label id category-id))
  (POST "/item/:id/edit" [id title description uri] (update-item id title description uri))
  (POST "/item/:id/delete" [id] (delete-item id))
  (GET "/item/:id" [id] (show-item id))

  
  (GET "/domains" [] (show-domain-list))
  (GET "/domain/create" [] (show-create-item model/item-type-domain))
  (GET "/domain/:domain-id" [domain-id] (resp/redirect (str "/item/" domain-id)))
  (GET "/domain/:domain-id/category/create" [domain-id] (show-create-category domain-id))
  
  (GET "/category/:category-id" [category-id] (resp/redirect (str "/item/" category-id)))
  
  (context "/api" [] api/api-routes)
  
  (route/resources "/") 
  (route/not-found "Not Found"))

(def app
  (->
    [(handler/site app-routes)]
    noir/app-handler
    noir/war-handler))
