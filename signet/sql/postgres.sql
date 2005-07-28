-- This is the Postgres DDL for the Signet database
-- Originally submitted by Simon McLeish, London School of Economics 
-- modified
--    6/20/2005 - tablename prefixes; assignment history tables; assignment expirationDate

-- Signet Subject tables
drop table signet_privilegedSubject;

-- Tree tables
drop table signet_treeNodeRelationship;
drop table signet_treeNode;
drop table signet_tree;

-- ChoiceSet tables
drop table signet_choice;
drop table signet_choiceSet;

-- Assignment tables
drop table signet_assignment cascade;
drop table signet_assignmentLimit;
drop table signet_assignment_history cascade;
drop table signet_assignmentLimit_history;
drop sequence assignmentSerial;

-- Subsystem tables
drop table signet_proxyType_function cascade;
drop table signet-permission_limit cascade; 
drop table signet_function_permission cascade;
drop table signet_category cascade;
drop table signet_function cascade;
drop table signet_permission cascade;
drop table signet_proxyType cascade;
drop table signet_limit cascade;
drop table signet_subsystem cascade;

-- Subject tables (optional, for local subject tables)
drop table SubjectAttribute;
drop table Subject;
drop table SubjectType;

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
);


create table signet_category
(
subsystemID         varchar(64)         NOT NULL,
categoryID          varchar(64)         NOT NULL,
status              varchar(16)         NOT NULL,
name                varchar(120)        NOT NULL,
modifyDatetime      timestamp           NOT NULL,

primary key (subsystemID, categoryID),
foreign key (subsystemID) references signet_subsystem (subsystemID)
);


create table signet_function
(
subsystemID         varchar(64)         NOT NULL,
functionID          varchar(64)         NOT NULL,
categoryID          varchar(64)         NULL,
status              varchar(16)         NOT NULL,
name                varchar(120)        NOT NULL,
helpText            text                NOT NULL,
modifyDatetime      timestamp           NOT NULL,

primary key (subsystemID, functionID),
foreign key (subsystemID) references signet_subsystem (subsystemID)
);


create table signet_permission
(
permissionKey		int                 NOT NULL IDENTITY,  ???
subsystemID         varchar(64)         NOT NULL,
permissionID        varchar(64)         NOT NULL,
status              varchar(16)         NOT NULL,
modifyDatetime      timestamp           NOT NULL,

primary key (permissionKey),
foreign key (subsystemID) references signet_subsystem (subsystemID)
);


create table signet_proxyType
(
subsystemID         varchar(64)         NOT NULL,
proxyTypeID         varchar(64)         NOT NULL,
status              varchar(16)         NOT NULL,
name                varchar(120)        NOT NULL,
helpText            text                NULL,
modifyDatetime      timestamp           NOT NULL,

primary key (subsystemID, proxyTypeID),
foreign key (subsystemID) references signet_subsystem (subsystemID)
);


create table signet_limit
(
limitKey			int                 NOT NULL IDENTITY, ???
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
foreign key (subsystemID) references signet_subsystem (subsystemID)
);


create table signet_function_permission
(
subsystemID         varchar(64)         NOT NULL,
functionID          varchar(64)         NOT NULL,
permissionKey       int                 NOT NULL,

primary key (subsystemID, functionID, permissionKey),
foreign key (subsystemID, functionID) references signet_function (subsystemID, functionID),
foreign key (subsystemID, permissionKey) references signet_permission (subsystemID, permissionKey)
);


create table signet_permission_limit
(
permissionKey       int                 NOT NULL,
limitKey            int                 NOT NULL,
defaultLimitValueValue  varchar(64)     NULL,

primary key (permissionKey, limitKey),
foreign key (permissionKey) references signet_permission (permissionKey),
foreign key (limitKey) references signet_limit (limitKey)
);


create table signet_proxyType_function
(
subsystemID         varchar(64)         NOT NULL,
proxyTypeID         varchar(64)         NOT NULL,
functionID          varchar(64)         NOT NULL,

primary key (subsystemID, proxyTypeID, functionID),
foreign key (subsystemID, proxyTypeID) references signet_proxyType (subsystemID, proxyTypeID),
foreign key (subsystemID, functionID) references signet_function (subsystemID, functionID)
);


-- Signet Subject tables

create table signet_privilegedSubject (
subjectTypeID     varchar(32)     NOT NULL,
subjectID         varchar(64)     NOT NULL,
name              varchar(120)    NOT NULL,

primary key (subjectTypeID, subjectID)
);


-- Tree tables

create table signet_tree
(
treeID              varchar(64)         NOT NULL,
name                varchar(120)        NOT NULL,
adapterClass        varchar(255)        NOT NULL,
modifyDatetime      timestamp           NOT NULL,

primary key (treeID)
);


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
);


create table signet_treeNodeRelationship
(
treeID              varchar(64)         NOT NULL,
nodeID              varchar(64)         NOT NULL,
parentNodeID        varchar(64)         NOT NULL,

primary key (treeID, nodeID, parentNodeID),
foreign key (treeID) references signet_tree (treeID)
);


-- ChoiceSet tables

create table signet_choiceSet
(
choiceSetID         varchar(64)         NOT NULL,
adapterClass        varchar(255)        NOT NULL,
subsystemID         varchar(64)         NULL,
modifyDatetime      timestamp           NOT NULL,

primary key (choiceSetID)
);


create table signet_choice
(
choiceSetID         varchar(64)         NOT NULL,
value               varchar(32)         NOT NULL,
label               varchar(64)         NOT NULL,
rank                smallint            NOT NULL,
displayOrder        smallint            NOT NULL,
modifyDatetime      timestamp           NOT NULL,

primary key (choiceSetID, value),
foreign key (choiceSetID) references signet_choiceSet (choiceSetID)
);


-- Assignment tables

create sequence assignmentSerial START 1;

create table signet_assignment
(
assignmentID        integer             DEFAULT nextval('assignmentSerial'),
instanceNumber      integer             NOT NULL,
status              varchar(16)         NOT NULL,
subsystemID         varchar(64)         NOT NULL,
functionID          varchar(64)         NOT NULL,
grantorTypeID       varchar(32)         NOT NULL,
grantorID           varchar(64)         NOT NULL,
granteeTypeID       varchar(32)         NOT NULL,
granteeID           varchar(64)         NOT NULL,
proxyTypeID         varchar(64)         NULL,
proxyID             varchar(64)         NULL,
scopeID             varchar(64)         NULL,
scopeNodeID         varchar(64)         NULL,
canGrant            bit                 NOT NULL,
grantOnly           bit                 NOT NULL,
effectiveDate       timestamp           NOT NULL,
expirationDate      timestamp           NULL,
revokerTypeID       varchar(32)         NULL,
revokerID           varchar(64)         NULL,
modifyDatetime      timestamp           NOT NULL,

primary key (assignmentID, instanceNumber)
);


create table signet_assignmentLimit
(
assignmentID        integer             NOT NULL,
limitKey    		int                 NOT NULL,
value               varchar(32)         NOT NULL,
primary key (assignmentID, limitKey, value),
foreign key (assignmentID) references signet_assignment (assignmentID)
foreign key (limitKey) references signet_limit (limitKey)
);



create table signet_assignment_history
(
historyID           integer             DEFAULT nextval('assignmentSerial'),
assignmentID        integer             NOT NULL,
instanceNumber      integer             NOT NULL,
status              varchar(16)         NOT NULL,
subsystemID         varchar(64)         NOT NULL,
functionID          varchar(64)         NOT NULL,
grantorTypeID       varchar(32)         NOT NULL,
grantorID           varchar(64)         NOT NULL,
granteeTypeID       varchar(32)         NOT NULL,
granteeID           varchar(64)         NOT NULL,
proxyTypeID         varchar(64)         NULL,
proxyID             varchar(64)         NULL,
scopeID             varchar(64)         NULL,
scopeNodeID         varchar(64)         NULL,
canGrant            bit                 NOT NULL,
grantOnly           bit                 NOT NULL,
effectiveDate       timestamp           NOT NULL,
expirationDate      timestamp           NULL,
revokerTypeID       varchar(32)         NULL,
revokerID           varchar(64)         NULL,
historyDatetime     timestamp           NOT NULL;
modifyDatetime      timestamp           NOT NULL,

primary key (assignmentID, instanceNumber)
);


create table signet_assignmentLimit_history
(
historyID           int                 NOT NULL,
assignmentID        int                 NOT NULL,
limitSubsystemID    varchar(64)         NOT NULL,
limitType           varchar(32)         NOT NULL,
limitTypeID         varchar(64)         NOT NULL,
value               varchar(32)         NOT NULL,
primary key (historyID),
foreign key (assignmentID, instanceNumber) references signet_assignment (assignmentID, instanceNumber)
);


-- Subject tables (optional, for local subject tables)

create table SubjectType (
  subjectTypeID     varchar(32)     NOT NULL,
  name              varchar(120)    NOT NULL,
  adapterClass      varchar(255)    NOT NULL,
  modifyDateTime    timestamp           NOT NULL,
  primary key (subjectTypeID)
  );


create table Subject (
  subjectTypeID     varchar(32)     NOT NULL,
  subjectID         varchar(64)     NOT NULL,
  name              varchar(120)    NOT NULL,
  description       varchar(255)    NOT NULL,
  displayId         varchar(64)     NOT NULL,
  modifyDateTime    timestamp           NOT NULL,
  primary key (subjectTypeID, subjectID),
  foreign key (subjectTypeID) references signet_subjectType (subjectTypeID)
  );


create table SubjectAttribute (
  subjectTypeID     varchar(32)     NOT NULL,
  subjectID         varchar(64)     NOT NULL,
  name              varchar(32)     NOT NULL,
  instance          smallint        NOT NULL,
  value             varchar(255)    NOT NULL,
  searchValue       varchar(255)    NOT NULL,
  modifyDateTime    timestamp        NOT NULL,
  primary key (subjectTypeID, subjectID, name, instance),
  foreign key (subjectTypeID,subjectID) references signet_subject (subjectTypeID,subjectID)
  );
