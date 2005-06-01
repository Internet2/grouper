-- Subsystem tables
drop table ProxyType_Function;
drop table Permission_Limit;
drop table Function_Permission;
drop table Category;
drop table Function;
drop table Permission;
drop table ProxyType;
drop table Limit;
drop table Subsystem;
create table Subsystem
(
subsystemID         varchar(64)         NOT NULL,
status              varchar(16)         NOT NULL,
name                varchar(120)        NOT NULL,
helpText            text                NOT NULL,
scopeTreeID         varchar(64)         NULL,
modifyDatetime      smalldatetime       default getdate(),
primary key clustered (subsystemID)
)
;
create table Category
(
subsystemID         varchar(64)         NOT NULL,
categoryID          varchar(64)         NOT NULL,
status              varchar(16)         NOT NULL,
name                varchar(120)        NOT NULL,
modifyDatetime      smalldatetime       default getdate(),
primary key (subsystemID, categoryID),
foreign key (subsystemID) references Subsystem (subsystemID)
)
;
create table Function
(
subsystemID         varchar(64)         NOT NULL,
functionID          varchar(64)         NOT NULL,
categoryID          varchar(64)         NULL,
status              varchar(16)         NOT NULL,
name                varchar(120)        NOT NULL,
helpText            text                NOT NULL,
modifyDatetime      smalldatetime       default getdate(),
primary key (subsystemID, functionID),
foreign key (subsystemID) references Subsystem (subsystemID)
)
;
create table Permission
(
subsystemID         varchar(64)         NOT NULL,
permissionID        varchar(64)         NOT NULL,
status              varchar(16)         NOT NULL,
modifyDatetime      smalldatetime       default getdate(),
primary key (subsystemID, permissionID),
foreign key (subsystemID) references Subsystem (subsystemID)
)
;
create table ProxyType
(
subsystemID         varchar(64)         NOT NULL,
proxyTypeID         varchar(64)         NOT NULL,
status              varchar(16)         NOT NULL,
name                varchar(120)        NOT NULL,
helpText            text                NULL,
modifyDatetime      smalldatetime       default getdate(),
primary key (subsystemID, proxyTypeID),
foreign key (subsystemID) references Subsystem (subsystemID)
)
;
create table Limit
(
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
modifyDatetime      smalldatetime       default getdate(),
primary key (subsystemID, limitID),
foreign key (subsystemID) references Subsystem (subsystemID)
)
;
create table Function_Permission
(
subsystemID         varchar(64)         NOT NULL,
functionID          varchar(64)         NOT NULL,
permissionID        varchar(64)         NOT NULL,
primary key (subsystemID, functionID, permissionID),
foreign key (subsystemID, functionID) references Function (subsystemID, functionID),
foreign key (subsystemID, permissionID) references Permission (subsystemID, permissionID)
)
;
create table Permission_Limit
(
subsystemID         varchar(64)         NOT NULL,
permissionID        varchar(64)         NOT NULL,
limitID             varchar(64)         NOT NULL,
defaultLimitValueValue  varchar(64)     NULL,
primary key (subsystemID, permissionID, limitID),
foreign key (subsystemID, permissionID) references Permission (subsystemID, permissionID),
foreign key (subsystemID, limitID) references Limit (subsystemID, limitID)
)
;
create table ProxyType_Function
(
subsystemID         varchar(64)         NOT NULL,
proxyTypeID         varchar(64)         NOT NULL,
functionID          varchar(64)         NOT NULL,
primary key (subsystemID, proxyTypeID, functionID),
foreign key (subsystemID, proxyTypeID) references ProxyType (subsystemID, proxyTypeID),
foreign key (subsystemID, functionID) references Function (subsystemID, functionID)
)
;
-- Subject tables
drop table SubjectAttribute;
drop table Subject;
drop table SubjectType;
drop table PrivilegedSubject;
create table SubjectType
(
subjectTypeID     varchar(32)     NOT NULL,
name              varchar(120)    NOT NULL,
adapterClass      varchar(255)    NOT NULL,
modifyDatetime    smalldatetime   default getdate(),
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
modifyDatetime    smalldatetime   default getdate(),
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
modifyDatetime    smalldatetime   default getdate(),
primary key (subjectTypeID, subjectID, name, instance)
)
;
create table PrivilegedSubject (
subjectTypeID     varchar(32)     NOT NULL,
subjectID         varchar(64)     NOT NULL,
name              varchar(120)    NOT NULL,
primary key (subjectTypeID, subjectID)
)
;
-- Tree tables
drop table TreeNodeRelationship;
drop table TreeNode;
drop table Tree;
create table Tree
(
treeID              varchar(64)         NOT NULL,
name                varchar(120)        NOT NULL,
adapterClass        varchar(255)        NOT NULL,
modifyDatetime      smalldatetime       default getdate(),
primary key (treeID)
)
;
create table TreeNode
(
treeID              varchar(64)         NOT NULL,
nodeID              varchar(64)         NOT NULL,
nodeType            varchar(32)         NOT NULL,
status              varchar(16)         NOT NULL,
name                varchar(120)        NOT NULL,
modifyDatetime      smalldatetime       default getdate(),
primary key (treeID, nodeID),
foreign key (treeID, nodeID) references TreeNode (treeID, nodeID)
)
;
create table TreeNodeRelationship
(
treeID              varchar(64)         NOT NULL,
nodeID              varchar(64)         NOT NULL,
parentNodeID        varchar(64)         NOT NULL,
primary key (treeID, nodeID, parentNodeID),
foreign key (treeID) references Tree (treeID)
)
;
-- ChoiceSet tables
drop table Choice;
drop table ChoiceSet;
create table ChoiceSet
(
choiceSetID         varchar(64)         NOT NULL,
adapterClass        varchar(255)        NOT NULL,
subsystemID         varchar(64)         NULL,
modifyDatetime      smalldatetime       default getdate(),
primary key (choiceSetID)
)
;
create table Choice
(
choiceSetID         varchar(64)         NOT NULL,
value               varchar(32)         NOT NULL,
label               varchar(64)         NOT NULL,
rank                smallint            NOT NULL,
displayOrder        smallint            NOT NULL,
modifyDatetime      smalldatetime       default getdate(),
primary key (choiceSetID, value),
foreign key (choiceSetID) references ChoiceSet (choiceSetID)
)
;
-- Assignment tables
drop table Assignment;
drop table AssignmentLimitValue;
create table Assignment
(
assignmentID        numeric(12,0)       IDENTITY,
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
effectiveDate       smalldatetime       NOT NULL,
revokerTypeID       varchar(32)         NULL,
revokerID           varchar(64)         NULL,
modifyDatetime      smalldatetime       default getdate(),
unique clustered (assignmentID)
)
;
create table AssignmentLimitValue
(
assignmentID        numeric(12,0)       NOT NULL,
limitSubsystemID    varchar(64)         NOT NULL,
limitType           varchar(32)         NOT NULL,
limitTypeID         varchar(64)         NOT NULL,
value               varchar(32)         NOT NULL,
unique clustered (assignmentID, limitSubsystemID, limitType, limitTypeID, value)
)
;
