-- 
-- $Id: init.sql,v 1.11 2005-05-24 19:20:59 blair Exp $
-- 

-- Base Grouper group type
INSERT INTO grouper_type (groupType) VALUES ('base');
INSERT INTO grouper_type (groupType) VALUES ('naming');

INSERT INTO grouper_field (groupField, readPriv, writePriv, isList) 
  VALUES ('extension', 'VIEW', 'ADMIN', 'FALSE');
INSERT INTO grouper_field (groupField, readPriv, writePriv, isList) 
  VALUES ('stem', 'VIEW', 'ADMIN', 'FALSE');
INSERT INTO grouper_field (groupField, readPriv, writePriv, isList) 
  VALUES ('description', 'READ', 'ADMIN', 'FALSE');
INSERT INTO grouper_field (groupField, readPriv, writePriv, isList) 
  VALUES ('members', 'READ', 'UPDATE', 'TRUE');
INSERT INTO grouper_field (groupField, readPriv, writePriv, isList) 
  VALUES ('viewers', 'ADMIN', 'ADMIN', 'TRUE');
INSERT INTO grouper_field (groupField, readPriv, writePriv, isList) 
  VALUES ('readers', 'ADMIN', 'ADMIN', 'TRUE');
INSERT INTO grouper_field (groupField, readPriv, writePriv, isList) 
  VALUES ('updaters', 'ADMIN', 'ADMIN', 'TRUE');
INSERT INTO grouper_field (groupField, readPriv, writePriv, isList) 
  VALUES ('admins', 'ADMIN', 'ADMIN', 'TRUE');
INSERT INTO grouper_field (groupField, readPriv, writePriv, isList) 
  VALUES ('optins', 'UPDATE', 'UPDATE', 'TRUE');
INSERT INTO grouper_field (groupField, readPriv, writePriv, isList) 
  VALUES ('optouts', 'UPDATE', 'UPDATE', 'TRUE');
INSERT INTO grouper_field (groupField, readPriv, writePriv, isList) 
  VALUES ('name', 'VIEW', 'SYSTEM', 'FALSE');
INSERT INTO grouper_field (groupField, readPriv, writePriv, isList) 
  VALUES ('stemmers', 'STEM', 'STEM', 'TRUE');
INSERT INTO grouper_field (groupField, readPriv, writePriv, isList) 
  VALUES ('creators', 'STEM', 'STEM', 'TRUE');
INSERT INTO grouper_field (groupField, readPriv, writePriv, isList) 
  VALUES ('displayName', 'VIEW', 'SYSTEM', 'FALSE');
INSERT INTO grouper_field (groupField, readPriv, writePriv, isList) 
  VALUES ('displayExtension', 'VIEW', 'ADMIN', 'FALSE');

INSERT INTO grouper_typeDef (groupType, groupField) 
  VALUES ('base', 'extension');
INSERT INTO grouper_typeDef (groupType, groupField) 
  VALUES ('base', 'stem');
INSERT INTO grouper_typeDef (groupType, groupField) 
  VALUES ('base', 'name');
INSERT INTO grouper_typeDef (groupType, groupField) 
  VALUES ('base', 'description');
INSERT INTO grouper_typeDef (groupType, groupField) 
  VALUES ('base', 'displayExtension');
INSERT INTO grouper_typeDef (groupType, groupField) 
  VALUES ('base', 'displayName');
INSERT INTO grouper_typeDef (groupType, groupField) 
  VALUES ('base', 'members');
INSERT INTO grouper_typeDef (groupType, groupField) 
  VALUES ('base', 'viewers');
INSERT INTO grouper_typeDef (groupType, groupField) 
  VALUES ('base', 'readers');
INSERT INTO grouper_typeDef (groupType, groupField) 
  VALUES ('base', 'updaters');
INSERT INTO grouper_typeDef (groupType, groupField) 
  VALUES ('base', 'admins');
INSERT INTO grouper_typeDef (groupType, groupField) 
  VALUES ('base', 'optins');
INSERT INTO grouper_typeDef (groupType, groupField) 
  VALUES ('base', 'optouts');

INSERT INTO grouper_typeDef (groupType, groupField) 
  VALUES ('naming', 'displayExtension');
INSERT INTO grouper_typeDef (groupType, groupField) 
  VALUES ('naming', 'displayName');
INSERT INTO grouper_typeDef (groupType, groupField) 
  VALUES ('naming', 'extension');
INSERT INTO grouper_typeDef (groupType, groupField) 
  VALUES ('naming', 'stem');
INSERT INTO grouper_typeDef (groupType, groupField) 
  VALUES ('naming', 'name');
INSERT INTO grouper_typeDef (groupType, groupField) 
  VALUES ('naming', 'description');
INSERT INTO grouper_typeDef (groupType, groupField) 
  VALUES ('naming', 'creators');
INSERT INTO grouper_typeDef (groupType, groupField) 
  VALUES ('naming', 'stemmers');

INSERT INTO Subject (subjectID, subjectTypeID, name)
  VALUES ('GrouperSystem', 'person', 'Grouper Root');

COMMIT;

