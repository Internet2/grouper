-- 
-- $Id: base.sql,v 1.15 2004-11-22 18:23:30 blair Exp $
-- 

-- Base Grouper group type
INSERT INTO grouper_types (groupType) VALUES ('base');
INSERT INTO grouper_types (groupType) VALUES ('naming');

INSERT INTO grouper_Fields (groupField, readPriv, writePriv, isList) 
  VALUES ('descriptor', 'READ', 'ADMIN', 'FALSE');
INSERT INTO grouper_Fields (groupField, readPriv, writePriv, isList) 
  VALUES ('stem', 'READ', 'ADMIN', 'FALSE');
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
  VALUES ('base', 'descriptor');
INSERT INTO grouper_typeDefs (groupType, groupField) 
  VALUES ('base', 'stem');
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
INSERT INTO grouper_typeDefs (groupType, groupField) 
  VALUES ('naming', 'descriptor');
INSERT INTO grouper_typeDefs (groupType, groupField) 
  VALUES ('naming', 'stem');
INSERT INTO grouper_typeDefs (groupType, groupField) 
  VALUES ('naming', 'description');
INSERT INTO grouper_typeDefs (groupType, groupField) 
  VALUES ('naming', 'members');
INSERT INTO grouper_typeDefs (groupType, groupField) 
  VALUES ('naming', 'viewers');
INSERT INTO grouper_typeDefs (groupType, groupField) 
  VALUES ('naming', 'readers');
INSERT INTO grouper_typeDefs (groupType, groupField) 
  VALUES ('naming', 'updaters');
INSERT INTO grouper_typeDefs (groupType, groupField) 
  VALUES ('naming', 'admins');
INSERT INTO grouper_typeDefs (groupType, groupField) 
  VALUES ('naming', 'optins');
INSERT INTO grouper_typeDefs (groupType, groupField) 
  VALUES ('naming', 'optouts');
INSERT INTO grouper_typeDefs (groupType, groupField) 
  VALUES ('naming', 'creators');
INSERT INTO grouper_typeDefs (groupType, groupField) 
  VALUES ('naming', 'stemmers');

INSERT INTO grouper_subjectType (subjectTypeID, name, adapterClass)
  VALUES ('person', 'Person', 
          'edu.internet2.middleware.grouper.SubjectTypeAdapterPersonImpl');
INSERT INTO grouper_subjectType (subjectTypeID, name, adapterClass)
  VALUES ('group', 'Group', 
          'edu.internet2.middleware.grouper.SubjectTypeAdapterGroupImpl');

INSERT INTO grouper_subject (subjectID, subjectTypeID)
  VALUES ('GrouperSystem', 'person');
INSERT INTO grouper_subject (subjectID, subjectTypeID)
  VALUES ('blair', 'person');
INSERT INTO grouper_subject (subjectID, subjectTypeID)
  VALUES ('notblair', 'person');

