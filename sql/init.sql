-- 
-- $Id: init.sql,v 1.6 2004-12-05 01:27:31 blair Exp $
-- 

-- Base Grouper group type
INSERT INTO grouper_type (groupType) VALUES ('base');
INSERT INTO grouper_type (groupType) VALUES ('naming');

INSERT INTO grouper_field (groupField, readPriv, writePriv, isList) 
  VALUES ('extension', 'READ', 'ADMIN', 'FALSE');
INSERT INTO grouper_field (groupField, readPriv, writePriv, isList) 
  VALUES ('stem', 'READ', 'ADMIN', 'FALSE');
INSERT INTO grouper_field (groupField, readPriv, writePriv, isList) 
  VALUES ('description', 'READ', 'ADMIN', 'FALSE');
INSERT INTO grouper_field (groupField, readPriv, writePriv, isList) 
  VALUES ('members', 'READ', 'UPDATE', 'TRUE');
INSERT INTO grouper_field (groupField, readPriv, writePriv, isList) 
  VALUES ('viewers', 'UPDATE', 'UPDATE', 'TRUE');
INSERT INTO grouper_field (groupField, readPriv, writePriv, isList) 
  VALUES ('readers', 'UPDATE', 'UPDATE', 'TRUE');
INSERT INTO grouper_field (groupField, readPriv, writePriv, isList) 
  VALUES ('updaters', 'UPDATE', 'UPDATE', 'TRUE');
INSERT INTO grouper_field (groupField, readPriv, writePriv, isList) 
  VALUES ('admins', 'ADMIN', 'ADMIN', 'TRUE');
INSERT INTO grouper_field (groupField, readPriv, writePriv, isList) 
  VALUES ('optins', 'READ', 'UPDATE', 'TRUE');
INSERT INTO grouper_field (groupField, readPriv, writePriv, isList) 
  VALUES ('optouts', 'READ', 'UPDATE', 'TRUE');
INSERT INTO grouper_field (groupField, readPriv, writePriv, isList) 
  VALUES ('name', 'READ', '', 'FALSE');
INSERT INTO grouper_field (groupField, readPriv, writePriv, isList) 
  VALUES ('stemmers', 'STEM', 'STEM', 'TRUE');
INSERT INTO grouper_field (groupField, readPriv, writePriv, isList) 
  VALUES ('creators', 'STEM', 'STEM', 'TRUE');

INSERT INTO grouper_typeDef (groupType, groupField) 
  VALUES ('base', 'extension');
INSERT INTO grouper_typeDef (groupType, groupField) 
  VALUES ('base', 'stem');
INSERT INTO grouper_typeDef (groupType, groupField) 
  VALUES ('base', 'name');
INSERT INTO grouper_typeDef (groupType, groupField) 
  VALUES ('base', 'description');
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

INSERT INTO grouper_subjectType (subjectTypeID, name, adapterClass)
  VALUES ('person', 'Person', 
          'edu.internet2.middleware.grouper.SubjectTypeAdapterPersonImpl');
INSERT INTO grouper_subjectType (subjectTypeID, name, adapterClass)
  VALUES ('group', 'Group', 
          'edu.internet2.middleware.grouper.SubjectTypeAdapterGroupImpl');

INSERT INTO grouper_subject (subjectID, subjectTypeID)
  VALUES ('GrouperSystem', 'person');

