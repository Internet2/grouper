-- 
-- $Id: base.sql,v 1.2 2004-08-03 00:09:46 blair Exp $
-- 

-- Base Grouper group type
INSERT INTO grouper_Types (groupType) VALUES (1);

INSERT INTO grouper_Fields (groupField, readPriv, writePriv, isList) 
  VALUES ('name', 'READ', 'ADMIN', 'FALSE');
INSERT INTO grouper_Fields (groupField, readPriv, writePriv, isList) 
  VALUES ('description', 'READ', 'ADMIN', 'FALSE');
INSERT INTO grouper_Fields (groupField, readPriv, writePriv, isList) 
  VALUES ('members', 'READ', 'UPDATE', 'TRUE');
INSERT INTO grouper_Fields (groupField, readPriv, writePriv, isList) 
  VALUES ('viewers', 'UPDATE', 'UPDATE', 'TRUE');
INSERT INTO grouper_Fields (groupField, readPriv, writePriv, isList) 
  VALUES ('readers', 'UPDATE', 'UPDATE', 'TRUE');
INSERT INTO grouper_Fields (groupField, readPriv, writePriv, isList) 
  VALUES ('updaters', 'UPDATE', 'UPDATE', 'TRUE');
INSERT INTO grouper_Fields (groupField, readPriv, writePriv, isList) 
  VALUES ('admins', 'ADMIN', 'ADMIN', 'TRUE');
INSERT INTO grouper_Fields (groupField, readPriv, writePriv, isList) 
  VALUES ('optins', 'READ', 'UPDATE', 'TRUE');
INSERT INTO grouper_Fields (groupField, readPriv, writePriv, isList) 
  VALUES ('optouts', 'READ', 'UPDATE', 'TRUE');

INSERT INTO grouper_TypeDefs (groupType, groupField) 
  VALUES (1, 'name');
INSERT INTO grouper_TypeDefs (groupType, groupField) 
  VALUES (1, 'description');
INSERT INTO grouper_TypeDefs (groupType, groupField) 
  VALUES (1, 'members');
INSERT INTO grouper_TypeDefs (groupType, groupField) 
  VALUES (1, 'viewers');
INSERT INTO grouper_TypeDefs (groupType, groupField) 
  VALUES (1, 'readers');
INSERT INTO grouper_TypeDefs (groupType, groupField) 
  VALUES (1, 'updaters');
INSERT INTO grouper_TypeDefs (groupType, groupField) 
  VALUES (1, 'admins');
INSERT INTO grouper_TypeDefs (groupType, groupField) 
  VALUES (1, 'optins');
INSERT INTO grouper_TypeDefs (groupType, groupField) 
  VALUES (1, 'optouts');

