-- This is the Postgres DDL for the Signet database
-- Originally submitted by Simon McLeish, London School of Economics 
-- modified
--    6/20/2005 - tablename prefixes; assignment history tables; assignment expirationDate
--    1/10/2006 - renamed signet_privilegedSubject to signet_subject
--    3/09/2006 - add categoryKey, functionKey, subjectKey, choiceKey, choiceSetKey
--
-- $Header: /home/hagleyj/i2mi/signet/sql/postgres.sql,v 1.23 2006-04-11 23:20:32 ddonn Exp $
--

-- Tree tables
drop table signet_treeNodeRelationship;
drop table signet_treeNode;
drop table signet_tree;

-- ChoiceSet tables
drop table signet_choice;
drop sequence choiceSerial;
drop table signet_choiceSet;
drop sequence choiceSetSerial;

-- Assignment tables
drop table signet_assignmentLimit_history;
drop table signet_assignment_history cascade;
drop table signet_assignmentLimit;
drop table signet_assignment cascade;
drop sequence assignmentSerial;
drop table signet_proxy_history cascade;
drop table signet_proxy cascade;
drop sequence proxySerial;

-- Subsystem tables
drop table signet_permission_limit cascade;
drop table signet_function_permission cascade;
drop table signet_category cascade;
drop sequence categorySerial;
drop table signet_function cascade;
drop sequence functionSerial;
drop table signet_permission cascade;
drop sequence permissionSerial;
drop table signet_limit cascade;
drop table signet_subsystem cascade;

-- Signet Subject table
drop table signet_subject;
drop sequence subjectSerial;

-- Local Source Subject tables (optional)
drop table SubjectAttribute;
drop table Subject;
drop table SubjectType;

-- Miscellaneous
drop sequence limitSerial;
drop sequence assignmenthistoryserial;
drop sequence proxyhistoryserial;

-- Signet Implementation-specific
DROP SEQUENCE hibernate_sequence;


-- Subsystem tables

create table signet_subsystem
(
	subsystemID         varchar(64)         NOT NULL,
	status              varchar(16)         NOT NULL,
	name                varchar(120)        NOT NULL,
	helpText            text                NOT NULL,
	scopeTreeID         varchar(64)         NULL,
	modifyDatetime      timestamp           NOT NULL,
	primary key (subsystemID)
)
;

create sequence categorySerial START 1;

create table signet_category
(
	categoryKey         integer             DEFAULT nextval('categorySerial'),
	subsystemID         varchar(64)         NOT NULL,
	categoryID          varchar(64)         NOT NULL,
	status              varchar(16)         NOT NULL,
	name                varchar(120)        NOT NULL,
	modifyDatetime      timestamp           NOT NULL,
	primary key (categoryKey),
	unique (subsystemID, categoryID),
	foreign key (subsystemID) references signet_subsystem (subsystemID)
)
;

create sequence functionSerial START 1;

create table signet_function
(
	functionKey         integer             DEFAULT nextval('functionSerial'),
	subsystemID         varchar(64)         NOT NULL,
	functionID          varchar(64)         NOT NULL,
	categoryKey         integer             NULL,
	status              varchar(16)         NOT NULL,
	name                varchar(120)        NOT NULL,
	helpText            text                NOT NULL,
	modifyDatetime      timestamp           NOT NULL,
	primary key (functionKey),
	unique (subsystemID, functionID),
	foreign key (subsystemID) references signet_subsystem (subsystemID)
)
;

create sequence permissionSerial START 1;

create table signet_permission
(
	permissionKey       integer             DEFAULT nextval('permissionSerial'),
	subsystemID         varchar(64)         NOT NULL,
	permissionID        varchar(64)         NOT NULL,
	status              varchar(16)         NOT NULL,
	modifyDatetime      timestamp           NOT NULL,
	primary key (permissionKey),
	unique (subsystemID, permissionID),
	foreign key (subsystemID) references signet_subsystem (subsystemID)
)
;

create sequence limitSerial START 1;

create table signet_limit
(
	limitKey            integer             DEFAULT nextval('limitSerial'),
	subsystemID         varchar(64)         NOT NULL,
	limitID             varchar(64)         NOT NULL,
	status              varchar(16)         NOT NULL,
	limitType           varchar(16)         NOT NULL,
	limitTypeID         varchar(64)         NOT NULL,
	name                varchar(120)        NOT NULL,
	helpText            text                NULL,
	dataType            varchar(32)         NOT NULL,
	valueType           varchar(32)         NOT NULL,
	displayOrder        smallint            NOT NULL,
	renderer            varchar(255)        NOT NULL,
	modifyDatetime      timestamp           NOT NULL,
	primary key (limitKey),
	unique (subsystemID, limitID),
	foreign key (subsystemID) references signet_subsystem (subsystemID)
)
;

create table signet_function_permission
(
	functionKey         integer             NOT NULL,
	permissionKey       integer             NOT NULL,
	primary key (functionKey, permissionKey),
	foreign key (functionKey) references signet_function (functionKey),
	foreign key (permissionKey) references signet_permission (permissionKey)
)
;

create table signet_permission_limit
(
	permissionKey       integer             NOT NULL,
	limitKey            integer             NOT NULL,
	defaultLimitValueValue  varchar(64)     NULL,
	primary key (permissionKey, limitKey),
	foreign key (permissionKey) references signet_permission (permissionKey),
	foreign key (limitKey) references signet_limit (limitKey)
)
;

-- Signet Subject table

create sequence subjectSerial START 1;

create table signet_subject (
	subjectKey          integer             DEFAULT nextval('subjectSerial'),
	subjectTypeID       varchar(32)         NOT NULL,
	subjectID           varchar(64)         NOT NULL,
	description         varchar(255)        NOT NULL,
	name                varchar(120)        NOT NULL,
	modifyDatetime      timestamp           NOT NULL,
	primary key (subjectKey),
	unique (subjectTypeID, subjectID)
)
;

-- Tree tables

create table signet_tree
(
	treeID              varchar(64)         NOT NULL,
	name                varchar(120)        NOT NULL,
	adapterClass        varchar(255)        NOT NULL,
	modifyDatetime      timestamp           NOT NULL,
	primary key (treeID)
)
;

create table signet_treeNode
(
	treeID              varchar(64)         NOT NULL,
	nodeID              varchar(64)         NOT NULL,
	nodeType            varchar(32)         NOT NULL,
	status              varchar(16)         NOT NULL,
	name                varchar(120)        NOT NULL,
	modifyDatetime      timestamp           NOT NULL,
	primary key (treeID, nodeID),
	foreign key (treeID) references signet_tree (treeID)
)
;

create table signet_treeNodeRelationship
(
	treeID              varchar(64)         NOT NULL,
	nodeID              varchar(64)         NOT NULL,
	parentNodeID        varchar(64)         NOT NULL,
	primary key (treeID, nodeID, parentNodeID),
	foreign key (treeID) references signet_tree (treeID),
	foreign key (treeID, nodeID) references signet_treeNode (treeID, nodeID),
	foreign key (treeID, parentNodeID) references signet_treeNode(treeID, nodeID)
)
;

-- ChoiceSet tables

create sequence choiceSetSerial START 1;

create table signet_choiceSet
(
	choiceSetKey        integer             DEFAULT nextval('choiceSetSerial'),
	choiceSetID         varchar(64)         NOT NULL,
	adapterClass        varchar(255)        NOT NULL,
	subsystemID         varchar(64)         NULL,
	modifyDatetime      timestamp           NOT NULL,
	primary key (choiceSetKey),
	unique (choiceSetID, subsystemID)
)
;

create sequence choiceSerial START 1;

create table signet_choice
(
	choiceKey           integer             DEFAULT nextval('choiceSerial'),
	choiceSetKey        integer             NOT NULL,
	value               varchar(32)         NOT NULL,
	label               varchar(64)         NOT NULL,
	rank                smallint            NOT NULL,
	displayOrder        smallint            NOT NULL,
	modifyDatetime      timestamp           NOT NULL,
	primary key (choiceKey),
	unique (choiceSetKey, value),
	foreign key (choiceSetKey) references signet_choiceSet (choiceSetKey)
)
;

-- Assignment tables

create sequence assignmentSerial START 1;

create table signet_assignment
(
	assignmentID        integer             DEFAULT nextval('assignmentSerial'),
	instanceNumber      integer             NOT NULL,
	status              varchar(16)         NOT NULL,
	functionKey         integer             NOT NULL,
	grantorKey          integer             NOT NULL,
	granteeKey          integer             NOT NULL,
	proxyKey            integer             NULL,
	scopeID             varchar(64)         NULL,
	scopeNodeID         varchar(64)         NULL,
	canUse              boolean             NOT NULL,
	canGrant            boolean             NOT NULL,
	effectiveDate       timestamp           NOT NULL,
	expirationDate      timestamp           NULL,
	revokerKey          integer             NULL,
	modifyDatetime      timestamp           NOT NULL,
	primary key (assignmentID),
	foreign key (grantorKey) references signet_subject (subjectKey),
	foreign key (granteeKey) references signet_subject (subjectKey),
	foreign key (proxyKey) references signet_subject (subjectKey),
	foreign key (revokerKey) references signet_subject (subjectKey)
)
;

create index signet_assignment_1
	on signet_assignment (
	  grantorKey
)
;
create index signet_assignment_2
	on signet_assignment (
	  granteeKey
)
;
create index signet_assignment_3
	on signet_assignment (
	  effectiveDate
)
;
create index signet_assignment_4
	on signet_assignment (
	  expirationDate
)
;

create table signet_assignmentLimit
(
	assignmentID        integer             NOT NULL,
	limitKey            integer             NOT NULL,
	value               varchar(32)         NOT NULL,
	primary key (assignmentID, limitKey, value),
	foreign key (assignmentID) references signet_assignment (assignmentID),
	foreign key (limitKey) references signet_limit (limitKey)
)
;

create sequence assignmentHistorySerial START 1;

create table signet_assignment_history
(
	historyID           integer             DEFAULT nextval('assignmentHistorySerial'),
	assignmentID        integer             NOT NULL,
	instanceNumber      integer             NOT NULL,
	status              varchar(16)         NOT NULL,
	functionKey         integer             NOT NULL,
	grantorKey          integer             NOT NULL,
	granteeKey          integer             NOT NULL,
	proxyKey            integer             NULL,
	scopeID             varchar(64)         NULL,
	scopeNodeID         varchar(64)         NULL,
	canUse              boolean             NOT NULL,
	canGrant            boolean             NOT NULL,
	effectiveDate       timestamp           NOT NULL,
	expirationDate      timestamp           NULL,
	revokerKey          integer             NULL,
	historyDatetime     timestamp           NOT NULL,
	modifyDatetime      timestamp           NOT NULL,
	primary key (historyID),
	unique (assignmentID, instanceNumber),
	foreign key (grantorKey) references signet_subject (subjectKey),
	foreign key (granteeKey) references signet_subject (subjectKey),
	foreign key (proxyKey) references signet_subject (subjectKey),
	foreign key (revokerKey) references signet_subject (subjectKey)
)
;

create index signet_assignment_history_1
	on signet_assignment_history (
	  grantorKey
)
;
create index signet_assignment_history_2
	on signet_assignment_history (
	  granteeKey
)
;

create table signet_assignmentLimit_history
(
	assignment_historyID integer             NOT NULL,
	limitKey             integer             NOT NULL,
	value                varchar(32)         NOT NULL,
	primary key (assignment_historyID, limitKey, value),
	foreign key (assignment_historyID) references signet_assignment_history (historyID),
	foreign key (limitKey) references signet_limit (limitKey)
)
;

create sequence proxySerial START 1;

create table signet_proxy
(
	proxyID             integer             DEFAULT nextval('proxySerial'),
	instanceNumber      integer             NOT NULL,
	status              varchar(16)         NOT NULL,
	subsystemID         varchar(64)         NULL,
	grantorKey          integer             NOT NULL,
	granteeKey          integer             NOT NULL,
	proxySubjectKey     integer             NULL,
	canUse              boolean             NOT NULL,
	canExtend           boolean             NOT NULL,
	effectiveDate       timestamp           NOT NULL,
	expirationDate      timestamp           NULL,
	revokerKey          integer             NULL,
	modifyDatetime      timestamp           NOT NULL,
	primary key (proxyID),
	foreign key (grantorKey) references signet_subject (subjectKey),
	foreign key (granteeKey) references signet_subject (subjectKey),
	foreign key (proxySubjectKey) references signet_subject (subjectKey),
	foreign key (revokerKey) references signet_subject (subjectKey)
)
;

create sequence proxyHistorySerial START 1;

create table signet_proxy_history
(
	historyID           integer             DEFAULT nextval('proxyHistorySerial'),
	proxyID             integer             NOT NULL,
	instanceNumber      integer             NOT NULL,
	status              varchar(16)         NOT NULL,
	subsystemID         varchar(64)         NULL,
	grantorKey          integer             NOT NULL,
	granteeKey          integer             NOT NULL,
	proxySubjectKey     integer             NULL,
	canUse              boolean             NOT NULL,
	canExtend           boolean             NOT NULL,
	effectiveDate       timestamp           NOT NULL,
	expirationDate      timestamp           NULL,
	revokerKey          integer             NULL,
	historyDatetime     timestamp           NOT NULL,
	modifyDatetime      timestamp           NOT NULL,
	primary key (historyID),
	unique (proxyID, instanceNumber),
	foreign key (grantorKey) references signet_subject (subjectKey),
	foreign key (granteeKey) references signet_subject (subjectKey),
	foreign key (proxySubjectKey) references signet_subject (subjectKey),
	foreign key (revokerKey) references signet_subject (subjectKey)
)
;

-- Subject tables (optional, for local subject tables)

create table SubjectType
(
	subjectTypeID     varchar(32)           NOT NULL,
	name              varchar(120)          NOT NULL,
	adapterClass      varchar(255)          NOT NULL,
	modifyDatetime    timestamp             NOT NULL,
	primary key (subjectTypeID)
)
;

create table Subject
(
	subjectTypeID     varchar(32)           NOT NULL,
	subjectID         varchar(64)           NOT NULL,
	name              varchar(120)          NOT NULL,
	description       varchar(255)          NOT NULL,
	displayID         varchar(64)           NOT NULL,
	modifyDatetime    timestamp             NOT NULL,
	primary key (subjectTypeID, subjectID)
)
;

create table SubjectAttribute
(
	subjectTypeID     varchar(32)           NOT NULL,
	subjectID         varchar(64)           NOT NULL,
	name              varchar(32)           NOT NULL,
	instance          smallint              NOT NULL,
	value             varchar(255)          NOT NULL,
	searchValue       varchar(255)          NOT NULL,
	modifyDatetime    timestamp             NOT NULL,
	primary key (subjectTypeID, subjectID, name, instance),
	foreign key (subjectTypeID, subjectID) references Subject (subjectTypeID, subjectID)
)
;

create index SubjectAttribute_1
	on SubjectAttribute (
	  subjectID,
	  name,
	  value
)
;

CREATE SEQUENCE hibernate_sequence
	START WITH 1;
