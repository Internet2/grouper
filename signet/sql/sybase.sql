-- This is the Sybase DDL for the Signet database
-- Author Lynn McRae, Stanford University 
-- modified
--
-- Tree tables
drop table signet_treeNodeRelationship;
drop table signet_treeNode;
drop table signet_tree;
-- ChoiceSet tables
drop table signet_choice;
drop table signet_choiceSet;
-- Assignment tables
drop table signet_assignmentLimit_history;
drop table signet_assignment_history;
drop table signet_assignmentLimit;
drop table signet_assignment;
drop table signet_proxy_history;
drop table signet_proxy;
-- Subsystem tables
drop table signet_permission_limit;
drop table signet_function_permission;
drop table signet_category;
drop table signet_function;
drop table signet_permission;
drop table signet_limit;
drop table signet_subsystem;
-- Signet Subject tables
drop table signet_subject;
-- Local Source Subject tables (optional)
drop table SubjectAttribute;
drop table Subject;
drop table SubjectType;
--
-- Subsystem tables
create table signet_subsystem
(
subsystemID         varchar(64)         NOT NULL,
status              varchar(16)         NOT NULL,
name                varchar(120)        NOT NULL,
helpText            text                NOT NULL,
scopeTreeID         varchar(64)         NULL,
modifyDatetime      smalldatetime       NOT NULL,
primary key clustered (subsystemID)
)
;
create table signet_category
(
categoryKey         numeric(12,0)       IDENTITY,
subsystemID         varchar(64)         NOT NULL,
categoryID          varchar(64)         NOT NULL,
status              varchar(16)         NOT NULL,
name                varchar(120)        NOT NULL,
modifyDatetime      smalldatetime       NOT NULL,
primary key (categoryKey),
unique (subsystemID, categoryID),
foreign key (subsystemID) references signet_subsystem (subsystemID)
)
;
create table signet_function
(
functionKey         numeric(12,0)       IDENTITY,
subsystemID         varchar(64)         NOT NULL,
functionID          varchar(64)         NOT NULL,
categoryKey         numeric(12,0)       NULL,
status              varchar(16)         NOT NULL,
name                varchar(120)        NOT NULL,
helpText            text                NOT NULL,
modifyDatetime      smalldatetime       NOT NULL,
primary key (functionKey),
unique (subsystemID, functionID),
foreign key (subsystemID) references signet_subsystem (subsystemID)
)
;
create table signet_permission
(
permissionKey		numeric(12,0)       IDENTITY,
subsystemID         varchar(64)         NOT NULL,
permissionID        varchar(64)         NOT NULL,
status              varchar(16)         NOT NULL,
modifyDatetime      smalldatetime       NOT NULL,
primary key (permissionKey),
unique (subsystemID, permissionID),
foreign key (subsystemID) references signet_subsystem (subsystemID)
)
;
create table signet_limit
(
limitKey			numeric(12,0)       IDENTITY,
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
modifyDatetime      smalldatetime       NOT NULL,
primary key (limitKey),
unique (subsystemID, limitID),
foreign key (subsystemID) references signet_subsystem (subsystemID)
)
;
create table signet_function_permission
(
functionKey         numeric(12,0)       NOT NULL,
permissionKey       numeric(12,0)       NOT NULL,
primary key (functionKey, permissionKey),
foreign key (functionKey) references signet_function (functionKey),
foreign key (permissionKey) references signet_permission (permissionKey)
)
;
create table signet_permission_limit
(
permissionKey       numeric(12,0)       NOT NULL,
limitKey            numeric(12,0)       NOT NULL,
defaultLimitValueValue  varchar(64)     NULL,
primary key (permissionKey, limitKey),
foreign key (permissionKey) references signet_permission (permissionKey),
foreign key (limitKey) references signet_limit (limitKey)
)
;
-- Signet Subject tables
create table signet_subject (
subjectKey			numeric(12,0)       IDENTITY,
subjectTypeID       varchar(32)         NOT NULL,
subjectID           varchar(64)         NOT NULL,
description         varchar(255)        NOT NULL,
name                varchar(120)        NOT NULL,
modifyDatetime      smalldatetime       NOT NULL,
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
modifyDatetime      smalldatetime       NOT NULL,
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
modifyDatetime      smalldatetime       NOT NULL,
primary key (treeID, nodeID),
foreign key (treeID, nodeID) references signet_treeNode (treeID, nodeID)
)
;
create table signet_treeNodeRelationship
(
treeID              varchar(64)         NOT NULL,
nodeID              varchar(64)         NOT NULL,
parentNodeID        varchar(64)         NOT NULL,
primary key (treeID, nodeID, parentNodeID),
foreign key (treeID) references signet_tree (treeID)
)
;
-- ChoiceSet tables
create table signet_choiceSet
(
choiceSetKey		numeric(12,0)       IDENTITY,
choiceSetID         varchar(64)         NOT NULL,
adapterClass        varchar(255)        NOT NULL,
subsystemID         varchar(64)         NULL,
modifyDatetime      smalldatetime       NOT NULL,
primary key (choiceSetKey),
unique (choiceSetID, subsystemID)
)
;
create table signet_choice
(
choiceKey			numeric(12,0)       IDENTITY,
choiceSetKey        numeric(12,0)       NOT NULL,
value               varchar(32)         NOT NULL,
label               varchar(64)         NOT NULL,
rank                smallint            NOT NULL,
displayOrder        smallint            NOT NULL,
modifyDatetime      smalldatetime       NOT NULL,
primary key (choiceKey),
unique (choiceSetKey, value),
foreign key (choiceSetKey) references signet_choiceSet (choiceSetKey)
)
;
-- Assignment tables
create table signet_assignment
(
assignmentID        numeric(12,0)       IDENTITY,
instanceNumber      int                 NOT NULL,
status              varchar(16)         NOT NULL,
functionKey         numeric(12,0)       NOT NULL,
grantorKey          numeric(12,0)       NOT NULL,
granteeKey          numeric(12,0)       NOT NULL,
proxyKey            numeric(12,0)       NULL,
scopeID             varchar(64)         NULL,
scopeNodeID         varchar(64)         NULL,
canUse              bit                 NOT NULL,
canGrant            bit                 NOT NULL,
effectiveDate       smalldatetime       NOT NULL,
expirationDate      smalldatetime       NULL,
revokerKey          numeric(12,0)       NULL,
modifyDatetime      smalldatetime       NOT NULL,
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
create table signet_assignmentLimit
(
assignmentID        numeric(12,0)       NOT NULL,
limitKey    		numeric(12,0)       NOT NULL,
value               varchar(32)         NOT NULL,
unique (assignmentID, limitKey, value),
foreign key (assignmentID) references signet_assignment (assignmentID),
foreign key (limitKey) references signet_limit (limitKey)
)
;
create table signet_assignment_history
(
historyID           numeric(12,0)       IDENTITY,
assignmentID        numeric(12,0)       NOT NULL,
instanceNumber      int                 NOT NULL,
status              varchar(16)         NOT NULL,
functionKey         numeric(12,0)       NOT NULL,
grantorKey          numeric(12,0)       NOT NULL,
granteeKey          numeric(12,0)       NOT NULL,
proxyKey            numeric(12,0)       NULL,
scopeID             varchar(64)         NULL,
scopeNodeID         varchar(64)         NULL,
canUse              bit                 NOT NULL,
canGrant            bit                 NOT NULL,
effectiveDate       smalldatetime       NOT NULL,
expirationDate      smalldatetime       NULL,
revokerKey          numeric(12,0)       NULL,
historyDatetime     smalldatetime       NOT NULL,
modifyDatetime      smalldatetime       NOT NULL,
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
  assignment_historyID numeric(12,0) NOT NULL,
  limitKey             numeric(12,0) NOT NULL,
  value                varchar(32)   NOT NULL,
  unique      (assignment_historyID, limitKey, value),
  foreign key (assignment_historyID)
    references signet_assignment_history
      (historyID),
  foreign key (limitKey)
    references signet_limit
      (limitKey)
)
;
create table signet_proxy
(
proxyID             numeric(12,0)       IDENTITY,
instanceNumber      int                 NOT NULL,
status              varchar(16)         NOT NULL,
subsystemID         varchar(64)         NULL,
grantorKey          numeric(12,0)       NOT NULL,
granteeKey          numeric(12,0)       NOT NULL,
proxySubjectKey     numeric(12,0)       NULL,
canUse              bit                 NOT NULL,
canExtend           bit                 NOT NULL,
effectiveDate       smalldatetime       NOT NULL,
expirationDate      smalldatetime       NULL,
revokerKey          numeric(12,0)       NULL,
modifyDatetime      smalldatetime       NOT NULL,
primary key (proxyID),
foreign key (grantorKey) references signet_subject (subjectKey),
foreign key (granteeKey) references signet_subject (subjectKey),
foreign key (proxySubjectKey) references signet_subject (subjectKey),
foreign key (revokerKey) references signet_subject (subjectKey)
)
;
create table signet_proxy_history
(
historyID           numeric(12,0)       IDENTITY,
proxyID             numeric(12,0)       NOT NULL,
instanceNumber      int                 NOT NULL,
status              varchar(16)         NOT NULL,
subsystemID         varchar(64)         NULL,
grantorKey          numeric(12,0)       NOT NULL,
granteeKey          numeric(12,0)       NOT NULL,
proxySubjectKey		numeric(12,0)       NULL,
canUse              bit                 NOT NULL,
canExtend           bit                 NOT NULL,
effectiveDate       smalldatetime       NOT NULL,
expirationDate      smalldatetime       NULL,
revokerKey          numeric(12,0)       NULL,
historyDatetime     smalldatetime       NOT NULL,
modifyDatetime      smalldatetime       NOT NULL,
unique (proxyID, instanceNumber),
foreign key (grantorKey) references signet_subject (subjectKey),
foreign key (granteeKey) references signet_subject (subjectKey),
foreign key (proxySubjectKey) references signet_subject (subjectKey),
foreign key (revokerKey) references signet_subject (subjectKey)
)
;
-- Subject tables (optional, for local subject tables)
create table SubjectType (
subjectTypeID     varchar(32)     NOT NULL,
name              varchar(120)    NOT NULL,
adapterClass      varchar(255)    NOT NULL,
modifyDatetime    smalldatetime   NOT NULL,
primary key (subjectTypeID)
)
;
create table Subject
(
subjectTypeID     varchar(32)     NOT NULL,
subjectID         varchar(64)     NOT NULL,
name              varchar(120)    NOT NULL,
description       varchar(255)    NOT NULL,
displayID         varchar(64)     NOT NULL,
modifyDatetime    smalldatetime   NOT NULL,
primary key (subjectTypeID, subjectID)
)
;
create table SubjectAttribute
(
subjectTypeID     varchar(32)     NOT NULL,
subjectID         varchar(64)     NOT NULL,
name              varchar(32)     NOT NULL,
instance          smallint        NOT NULL,
value             varchar(255)    NOT NULL,
searchValue       varchar(255)    NOT NULL,
modifyDatetime    smalldatetime   NOT NULL,
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
