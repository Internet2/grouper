--
-- Remove all records from Signet DB and reset sequences to 1
--
-- Note: may not work with anything other than PostgreSQL, but might serve as a template for others.
--
-- $Header: /home/hagleyj/i2mi/signet/sql/Psql_clean_db.sql,v 1.2 2006-04-14 23:06:18 ddonn Exp $
--

DELETE FROM signet_category;
DELETE FROM signet_function_permission;
DELETE FROM signet_function;
DELETE FROM signet_permission_limit;
DELETE FROM signet_permission;
DELETE FROM signet_treeNodeRelationship;
DELETE FROM signet_treeNode;
DELETE FROM signet_tree;
DELETE FROM signet_choice;
DELETE FROM signet_choiceSet;
DELETE FROM signet_assignmentLimit_history;
DELETE FROM signet_assignment_history;
DELETE FROM signet_assignmentLimit;
DELETE FROM signet_limit;
DELETE FROM signet_assignment;
DELETE FROM signet_proxy_history;
DELETE FROM signet_proxy;
DELETE FROM signet_subject;
DELETE FROM signet_subsystem;
DELETE FROM SubjectAttribute;
DELETE FROM Subject;
DELETE FROM SubjectType;

SELECT setval('choiceSerial', 1, false);
SELECT setval('choiceSetSerial', 1, false);
SELECT setval('assignmentSerial', 1, false);
SELECT setval('proxySerial', 1, false);
SELECT setval('categorySerial', 1, false);
SELECT setval('functionSerial', 1, false);
SELECT setval('permissionSerial', 1, false);
SELECT setval('subjectSerial', 1, false);
SELECT setval('limitSerial', 1, false);
SELECT setval('assignmenthistoryserial', 1, false);
SELECT setval('proxyhistoryserial', 1, false);
SELECT setval('hibernate_sequence', 1, false);
