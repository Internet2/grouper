-- 
-- $Id: base.sql,v 1.9 2004-10-29 13:25:31 blair Exp $
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

INSERT INTO grouper_member (memberKey, memberType, memberID)
  VALUES ('dc56fb33-4d04-4ab9-b0fd-117c8f6e47a6',
          'person', 'GrouperSystem');
INSERT INTO grouper_member (memberKey, memberType, memberID)
  VALUES ('6377aa8c-a4cc-46cb-91e6-57d38ac1dbb8',
          'person', 'blair');
INSERT INTO grouper_member (memberKey, memberType, memberID)
  VALUES ('ad8167a2-3897-4e52-8820-8fd70c990f81',
          'person', 'notblair');
