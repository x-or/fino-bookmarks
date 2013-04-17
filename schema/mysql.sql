DROP TABLE IF EXISTS label;
DROP TABLE IF EXISTS category;
DROP TABLE IF EXISTS domain;
DROP TABLE IF EXISTS item;

CREATE TABLE item (
  id INT AUTO_INCREMENT PRIMARY KEY,
  type INT NOT NULL DEFAULT 0,
  title TEXT,
  description TEXT,
  uri TEXT, 
  INDEX (id))  
  ENGINE=InnoDB;

CREATE TABLE domain(
  item_id INT NOT NULL PRIMARY KEY,
  FOREIGN KEY (item_id) 
    REFERENCES item(id)
    ON DELETE CASCADE,
  INDEX (item_id))
  ENGINE=InnoDB;

CREATE TABLE category(
  item_id INT NOT NULL PRIMARY KEY,
  domain_id INT NOT NULL,
  linear_order INT,
  INDEX (item_id),
  INDEX (linear_order),
  FOREIGN KEY (item_id) 
    REFERENCES item(id)
    ON DELETE CASCADE,
  FOREIGN KEY (domain_id) 
    REFERENCES domain(item_id)
    ON DELETE CASCADE)  
  ENGINE=InnoDB;

CREATE TABLE label(
  item_id INT NOT NULL,
  category_id INT NOT NULL,
  INDEX(item_id),
  INDEX(category_id),
  UNIQUE(item_id, category_id),
  FOREIGN KEY (item_id) 
    REFERENCES item(id)
    ON DELETE CASCADE,
  FOREIGN KEY (category_id) 
    REFERENCES category(item_id)
    ON DELETE CASCADE) 
  ENGINE=InnoDB;

ALTER TABLE item AUTO_INCREMENT = 1;

INSERT INTO item(id, type, title, description) VALUES (1, 2, 'Singleton', '');
INSERT INTO item(id, type, title, description, uri) VALUES (2, 2, 'False', 'A truth value', 'http://en.wikipedia.org/wiki/False_%28logic%29');
INSERT INTO item(id, type, title, description) VALUES (3, 2, 'True', 'A truth value');

ALTER TABLE item AUTO_INCREMENT = 1000;

INSERT INTO item(id, type, title, description) VALUES (1000, 1, 'Void', 'Empty domain');
INSERT INTO item(id, type, title, description) VALUES (1001, 1, 'Singleton', 'Single category domain');
INSERT INTO item(id, type, title, description, uri) VALUES (1002, 1, 'Boolean', 'Truth value', 'http://en.wikipedia.org/wiki/Truth_value');


INSERT INTO domain(item_id) VALUES (1000);
INSERT INTO domain(item_id) VALUES (1001);
INSERT INTO domain(item_id) VALUES (1002);

INSERT INTO category(item_id, domain_id) VALUES (1, 1001);
INSERT INTO category(item_id, domain_id, linear_order) VALUES (2, 1002, 0);
INSERT INTO category(item_id, domain_id, linear_order) VALUES (3, 1002, 1);

ALTER TABLE item AUTO_INCREMENT = 100000
;
INSERT INTO item(title, description) VALUES ('Chair', 'A brown chair');
INSERT INTO item(title, description) VALUES ('Table', 'A wood table');

