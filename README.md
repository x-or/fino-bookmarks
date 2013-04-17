# Fino is not ontology	

Эксперимент с фасетной классификацией закладок.

Проект задеплоен здесь: http://task04-facet.rhcloud.com/

## Структура 

В БД 3 сущности: item, domain и category.

Закладки описываются полями item: title, uri, description.

Domain и category являются наследниками item, и поэтому имеют все свойства item.

Каждый domain содержит список category.

Каждая сategory может быть только в одном domain.

Любой item может быть промаркирован любыми category без повторов. Такая маркировка называется label.