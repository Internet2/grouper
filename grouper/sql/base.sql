-- 
-- $Id: base.sql,v 1.5 2004-09-19 01:34:02 blair Exp $
-- 

-- Base Grouper group type
INSERT INTO grouper_types (groupType) VALUES ('base');

INSERT INTO grouper_Fields (groupField, readPriv, writePriv, isList) 
  VALUES ('stem', 'READ', 'ADMIN', 'FALSE');
INSERT INTO grouper_Fields (groupField, readPriv, writePriv, isList) 
  VALUES ('descriptor', 'READ', 'ADMIN', 'FALSE');
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

INSERT INTO grouper_typeDefs (groupType, groupField) 
  VALUES ('base', 'stem');
INSERT INTO grouper_typeDefs (groupType, groupField) 
  VALUES ('base', 'descriptor');
INSERT INTO grouper_typeDefs (groupType, groupField) 
  VALUES ('base', 'description');
INSERT INTO grouper_typeDefs (groupType, groupField) 
  VALUES ('base', 'members');
INSERT INTO grouper_typeDefs (groupType, groupField) 
  VALUES ('base', 'viewers');
INSERT INTO grouper_typeDefs (groupType, groupField) 
  VALUES ('base', 'readers');
INSERT INTO grouper_typeDefs (groupType, groupField) 
  VALUES ('base', 'updaters');
INSERT INTO grouper_typeDefs (groupType, groupField) 
  VALUES ('base', 'admins');
INSERT INTO grouper_typeDefs (groupType, groupField) 
  VALUES ('base', 'optins');
INSERT INTO grouper_typeDefs (groupType, groupField) 
  VALUES ('base', 'optouts');

