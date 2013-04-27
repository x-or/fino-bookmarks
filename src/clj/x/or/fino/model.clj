(ns x.or.fino.model
  (:use [korma db core]))



(def env (into {} (System/getenv)))

(def dbhost (or (get env "OPENSHIFT_MYSQL_DB_HOST") "localhost"))
(def dbport (or (get env "OPENSHIFT_MYSQL_DB_PORT") "3306"))

(def default-conn {:classname "com.mysql.jdbc.Driver"
                   :subprotocol "mysql"
                   :user "admin2yE2sae"
                   :password "l88YYSdqhYWb"
                   :subname (str "//" dbhost ":" dbport "/task04?useUnicode=true&characterEncoding=utf8")
                   :delimiters "`"})

(defdb korma-db default-conn)

;; Utility functions

(defn map-values [f m]
  ; from StackOverflow
  (into {} (for [[k v] m] [k (f v)])))

(defn sempty? [s]
  (cond 
   (string? s) (empty? s)
   (nil? s) true
   :else false))

(defn sempty->nil [s]
  (if (sempty? s) nil s))

(defn hash-map-sempty->nil [m]
  (map-values sempty->nil m))

;; Entities

(declare item)

(defentity category
  (pk :item_id)
  (many-to-many item :label)
  (prepare hash-map-sempty->nil))

(defentity domain
  (pk :item_id)
  (prepare hash-map-sempty->nil))

(defentity item
  (has-one domain)
  (has-one category)
  ;(many-to-many category :label) ; not compatible with item-category inheritance?
  (prepare hash-map-sempty->nil))

(defentity label
  (belongs-to item)
  (belongs-to category))

;; Model definitions

(def item-type-generic 0)
(def item-type-domain 1)
(def item-type-category 2)

(defn ensure-item-type [type]
  (cond
    (integer? type) type
    (empty? type) item-type-generic
    (string? type) (Integer/parseInt type)))

(defn create-item [v]
  (transaction
   (let [type (ensure-item-type (:type v))
         generic-vals (-> v (dissoc :domain-id) (assoc :type type))
         {new-item-id :GENERATED_KEY} (insert item (values generic-vals))]
     (do
       (condp = type
         item-type-generic nil
         item-type-domain (insert domain (values {:item_id new-item-id}))
         item-type-category (insert category (values {:item_id new-item-id :domain_id (:domain-id v)})))
       new-item-id))))

(defn create-domain [v]
  (insert domain (values (assoc v :type item-type-domain))))

(defn create-category [v]
  (insert category (values (assoc v :type item-type-category))))

(defn select-item []
  (select item))

(defn select-item-type [type]
  (select item
    (where {:type type})))

(defn select-generic-item []
  (select-item-type item-type-generic))

(defn select-domain []
  (select item
    (with domain)
    (where {:type item-type-domain})))

(defn load-domain [id]
  (first
    (select item
      (with domain)
      (where {:id id
              :type item-type-domain}))))

(defn load-category [domain-id]
  (select item
    (with category)
    (fields :* [(subselect label
                  (aggregate (count :*) :cnt)
                  (where {:category_id :item.id})) 
                :label-count])
    (where {:category.domain_id domain-id})
    (order :category.linear_order :asc)))

(defn load-label [item-id]
  (select item
    (with category)
    (where {:id [in (subselect label 
                      (fields :category_id)
                      (where {:item_id item-id}))]})))

(defn load-label-into [item]
  (assoc item :label (load-label (:id item))))

(defn select-labeled-items [category-id]
  (select item
    (where {:id [in (subselect label 
                      (fields :item_id)
                      (where {:category_id category-id}))]})))

(defn load-item [id]
  (transaction
    (let [item (first (select item (where {:id id})))
          type (:type item)
          child-table (condp = type
                     item-type-generic false
                     item-type-domain domain
                     item-type-category category)
          item (if child-table 
                 (merge item (first (select child-table (where {:item_id id}))))
                 item)
          item (load-label-into item)
          item (condp = type 
                 item-type-generic item
                 item-type-category (-> item 
                                        (assoc :domain (load-domain (:domain_id item)))
                                        (assoc :labeled-items (select-labeled-items id)))
                 item-type-domain (assoc item :categories (load-category id)))]
        item)))

(defn update-item [v]
  (update item
          (set-fields v)
          (where {:id (:id v)})))

(defn delete-item [id]
  (delete item
          (where {:id id})))

(defn add-item-label [id category-id]
  (insert label
    (values {:item_id id
             :category_id category-id})))

(defn delete-item-label [id category-id]
  (delete label
    (where {:item_id id
            :category_id category-id})))
