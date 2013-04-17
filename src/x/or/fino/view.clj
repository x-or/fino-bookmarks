(ns x.or.fino.view
  (:require [x.or.fino.model :as m]
            [me.raynes.laser :as l]
            [markdown.core :refer [md-to-html-string]]))


(defn format-uri [uri]
  (str "URI: " (or uri "None")))

(defn md->frag [s] 
  (l/unescaped (md-to-html-string s)))


(def main-html
  (l/parse
   (slurp (clojure.java.io/resource "public/html/main.html"))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Fragments

(def item-list-html (l/select main-html (l/id= "item-list")))
(def item-row-html (l/select main-html (l/id= "item-row")))
(def label-in-list-html (l/select main-html (l/descendant-of (l/id= "item-row") (l/id= "label") (l/element= :a))))
(def item-show-html (l/select main-html (l/id= "item-show")))
(def item-edit-html (l/select main-html (l/id= "item-edit")))
(def label-in-show-html (l/select main-html (l/descendant-of (l/id= "item-show") (l/id= "label") (l/element= :a))))
(def domain-show-extra-html (l/select main-html (l/descendant-of (l/id= "item-show") (l/id= "domain-extra"))))
(def category-show-extra-html (l/select main-html (l/descendant-of (l/id= "item-show") (l/id= "category-extra"))))
(def category-in-domain-html (l/select main-html (l/descendant-of  (l/id= "item-show") (l/id= "domain-extra") (l/id= "category"))))
(def labeled-item-html (l/select main-html (l/descendant-of  (l/id= "item-show") (l/id= "category-extra") (l/element= :li))))
(def label-html (l/select main-html (l/id= "item-label")))
(def domain-list-item-html (l/select main-html (l/id= "domain-list-item")))
(def label-in-item-label-html (l/select main-html (l/descendant-of (l/id= "item-label") (l/id= "label") (l/element= :span))))
(def category-in-label-html (l/select main-html (l/descendant-of (l/id= "item-label") (l/id= "category"))))


;; Used to show an item in the resources list
;; Path: /items/
(l/defragment label-in-list-frag label-in-list-html [{:keys [id title]}]
  (l/element= :a) (comp (l/attr :href (str "/category/" id)) (l/content title)))

(l/defragment item-frag item-row-html [{:keys [id title description uri label]}]
  (l/id= "title") (if (empty? uri) 
                    (l/replace title) 
                    (comp (l/content title) (l/attr :href uri)))
  (l/id= "description") (l/content (md->frag description))
  (l/id= "label") (l/content (when (seq label) [" [ " (for [l label] [(label-in-list-frag l) " "]) " ] "]))
  (l/id= "open") (l/attr :href (str "/item/" id)))

(l/defragment item-list-frag item-list-html [item-list]
  (l/element= :ul) (l/content (for [item item-list] (item-frag item))))

;; Shows item details
;; Path: /item/:id

(l/defragment label-in-show-frag label-in-show-html [{:keys [id title]}]
  (l/element= :a) (comp (l/attr :href (str "/category/" id)) (l/content title)))

(l/defragment category-in-domain-frag category-in-domain-html [domain-id {:keys [id title label-count]}]
  (l/element= :a) (comp 
                   (l/attr :href (str "/category/" id)) 
                   (l/content (if (= 0 label-count) title (str title  " (" label-count ")")))))

(l/defragment item-domain-show-frag domain-show-extra-html [id categories]
  (l/id= "categories") (l/content (for [category categories] [(category-in-domain-frag id category) " "]))
  (l/id= "add-category") (l/attr :href (str "/domain/" id "/category/create")))

(l/defragment labeled-item-frag labeled-item-html [{:keys [id title]}]
  (l/element= :a) (comp (l/attr :href (str "/item/" id)) (l/content title)))

(l/defragment item-category-show-frag category-show-extra-html [id title labeled-items]
  (l/element= :ul) (l/content (for [l labeled-items] (labeled-item-frag l))))

(l/defragment item-show-frag item-show-html [{:keys [id title description uri type domain categories label labeled-items]}]
  (l/descendant-of (l/element= :h2) (l/element= :a)) (if (not= type m/item-type-category)
                                                       (l/remove)
                                                       (comp (l/content (:title domain))
                                                             (l/attr :href (str "/domain/" (:id domain)))))
  (l/descendant-of (l/element= :h2) (l/element= :span)) (l/content [(if (= type m/item-type-category) " / " nil) title])
  (l/id= "description") (l/content (md->frag description))
  (l/id= "uri") (l/content (format-uri uri))
  (l/id= "label") (l/content (for [l label] [(label-in-show-frag l) " "]))
  (l/id= "edit") (l/attr :href (str "/item/" id "/edit"))
  (l/id= "label-btn") (l/attr :href (str "/item/" id "/label"))
  (l/id= "delete") (l/attr :onclick (str "deleteItem(" id ")"))
  (l/id= "domain-extra") (l/content (if (not= type m/item-type-domain) nil (item-domain-show-frag id categories)))
  (l/id= "category-extra") (l/content (if (not= type m/item-type-category) nil (item-category-show-frag id title labeled-items))))

;; Shows a form for item editting
;; Path: /item/:id/edit
(defn domain-link [type domain]
  (if (not= type m/item-type-category)
    (l/remove) 
    (comp (l/insert :left "In domain ")
          (l/content (:title domain))
          (l/attr :href (str "/domain/" (:id domain))))))

(l/defragment item-edit-frag item-edit-html [{:keys [id title description uri type domain]}]
  (l/id= "title") (l/attr :value title)
  (l/descendant-of (l/element= :h3) (l/element= :a)) (domain-link type domain)
  (l/element= :textarea) (l/content description)
  (l/id= "uri") (l/attr :value (or uri ""))
  (l/id= "close") (l/attr :href (str "/item/" id))
  (l/element= :form) (l/attr :action (str "/item/" id "/edit")))

;; Shows a from for item creating
;; Path: /item/create
(l/defragment item-create-frag item-edit-html [type domain]
  (l/descendant-of (l/element= :h2) (l/element= :em)) (l/content "Create item")
  (l/descendant-of (l/element= :h3) (l/element= :a)) (domain-link type domain)
  (l/id= "type") (l/attr :value (str type))
  (l/id= "domain-id") (l/attr :value (str (:item_id domain)))
  (l/id= "close") (l/attr :href "/items")
  (l/element= :form) (l/attr :action (str "/item/create")))

;; Shows a form for item labeling
;; Path: /item/:id/label

(l/defragment domain-list-item-frag domain-list-item-html [{:keys [id title]}]
  (l/element= :option) (comp (l/content title) (l/attr :value (str id))))

(l/defragment label-in-item-label-frag label-in-item-label-html [item-id {:keys [id title]}]
  (l/id= "title") (comp (l/attr :href (str "/item/" id)) (l/content title))
  (l/id= "remove") (l/attr :onclick (str "removeLabel(" item-id ", " id ")")))

(l/defragment category-in-label-frag category-in-label-html [item-id {:keys [id title]}]
  (l/element= :a) (comp (l/attr :onclick (str "labelItem(" item-id "," id ")")) (l/content title)))

(l/defragment label-frag label-html [{:keys [id title label]} domain-list {domain-title :title domain-categories :categories}]
  (l/element= :h2) (l/content title)
  (l/element= :select) (l/content (for [domain domain-list] (domain-list-item-frag domain)))
  (l/element= :form) (l/attr :action (str "/item/" id "/label"))
  (l/element= :h4) (l/content (str domain-title " categories"))
  (l/id= "label") (l/content (for [l label] (label-in-item-label-frag id l)))
  (l/id= "categories") (l/content (for [category domain-categories] (category-in-label-frag id category)))
  (l/id= "close") (l/attr :href (str "/item/" id)))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Pages



(defn show-item-list [item-list]
  (l/document main-html
              (l/id= "item-grid")
              (l/content
                 (item-list-frag item-list))))

(defn show-item [item]
  (l/document main-html
              (l/id= "item-grid") (l/content (item-show-frag item))))

(defn edit-item [item]
  (l/document main-html
              (l/id= "item-grid")
              (l/content
                (item-edit-frag item))))

(defn show-create-item [type]
  (l/document main-html
              (l/id= "item-grid")
              (l/content
                (item-create-frag type nil))))

(defn show-create-category [domain]
  (l/document main-html
              (l/id= "item-grid")
              (l/content
                (item-create-frag m/item-type-category domain))))

(defn label-item [item domain-list selected-domain]
  (l/document main-html
              (l/id= "item-grid")
              (l/content
                (label-frag item domain-list selected-domain))))
