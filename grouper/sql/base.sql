-- 
-- $Id: base.sql,v 1.10 2004-11-05 18:46:27 blair Exp $
-- 

-- Base Grouper group type
INSERT INTO grouper_types (groupType) VALUES ('base');

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

INSERT INTO grouper_memberType (memberTypeID, name, adapterClass)
  VALUES ('person', 'Person', 
          'edu.internet2.middleware.grouper.GrouperSubjectImpl');

INSERT INTO grouper_member (memberKey, memberID, memberTypeID)
  VALUES ('dc56fb33-4d04-4ab9-b0fd-117c8f6e47a6',
          'GrouperSystem', 'person');
INSERT INTO grouper_member (memberKey, memberID, memberTypeID)
  VALUES ('6377aa8c-a4cc-46cb-91e6-57d38ac1dbb8',
          'blair', 'person');
INSERT INTO grouper_member (memberKey, memberID, memberTypeID)
  VALUES ('ad8167a2-3897-4e52-8820-8fd70c990f81',
          'notblair', 'person');

