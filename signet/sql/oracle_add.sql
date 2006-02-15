-- This is the Oracle DDL for the Signet database
-- Tom Poage, University of California at Davis, 10 Feb 2006

-- Subsystem tables
CREATE TABLE signet_subsystem
(
subsystemID         NVARCHAR2(64)       NOT NULL,
status              NVARCHAR2(16)       NOT NULL,
name                NVARCHAR2(120)      NOT NULL,
helpText            NCLOB               NOT NULL,
scopeTreeID         NVARCHAR2(64)       NULL,
modifyDatetime      TIMESTAMP           DEFAULT SYSDATE,
PRIMARY KEY (subsystemID)
)
;
CREATE TABLE signet_category
(
categoryKey         NUMERIC(12)         NOT NULL,
subsystemID         NVARCHAR2(64)       NOT NULL,
categoryID          NVARCHAR2(64)       NOT NULL,
status              NVARCHAR2(16)       NOT NULL,
name                NVARCHAR2(120)      NOT NULL,
modifyDatetime      TIMESTAMP           DEFAULT SYSDATE,
PRIMARY KEY (categoryKey),
UNIQUE (subsystemID, categoryID),
FOREIGN KEY (subsystemID) REFERENCES signet_subsystem (subsystemID)
)
;
CREATE TABLE signet_function
(
functionKey         NUMERIC(12)         NOT NULL,
subsystemID         NVARCHAR2(64)       NOT NULL,
functionID          NVARCHAR2(64)       NOT NULL,
categoryKey         NUMERIC(12)         NULL,
status              NVARCHAR2(16)       NOT NULL,
name                NVARCHAR2(120)      NOT NULL,
helpText            NCLOB               NOT NULL,
modifyDatetime      TIMESTAMP           DEFAULT SYSDATE,
PRIMARY KEY (functionKey),
UNIQUE (subsystemID, functionID),
FOREIGN KEY (subsystemID) REFERENCES signet_subsystem (subsystemID)
)
;
CREATE TABLE signet_permission
(
permissionKey       NUMERIC(12)         NOT NULL,
subsystemID         NVARCHAR2(64)       NOT NULL,
permissionID        NVARCHAR2(64)       NOT NULL,
status              NVARCHAR2(16)       NOT NULL,
modifyDatetime      TIMESTAMP           DEFAULT SYSDATE,
PRIMARY KEY (permissionKey),
UNIQUE (subsystemID, permissionID),
FOREIGN KEY (subsystemID) REFERENCES signet_subsystem (subsystemID)
)
;
CREATE TABLE signet_limit
(
limitKey            NUMERIC(12)         NOT NULL,
subsystemID         NVARCHAR2(64)       NOT NULL,
limitID             NVARCHAR2(64)       NOT NULL,
status              NVARCHAR2(16)       NOT NULL,
limitType           NVARCHAR2(16)       NOT NULL,
limitTypeID         NVARCHAR2(64)       NOT NULL,
name                NVARCHAR2(120)      NOT NULL,
helpText            NCLOB               NULL,
dataType            NVARCHAR2(32)       NOT NULL,
valueType           NVARCHAR2(32)       NOT NULL,
displayOrder        NUMERIC(12)         NOT NULL,
renderer            NVARCHAR2(255)      NOT NULL,
modifyDatetime      TIMESTAMP           DEFAULT SYSDATE,
PRIMARY KEY (limitKey),
UNIQUE (subsystemID, limitID),
FOREIGN KEY (subsystemID) REFERENCES signet_subsystem (subsystemID)
)
;
CREATE TABLE signet_function_permission
(
functionKey         NUMERIC(12)         NOT NULL,
permissionKey       NUMERIC(12)         NOT NULL,
PRIMARY KEY (functionKey, permissionKey),
FOREIGN KEY (functionKey) REFERENCES signet_function (functionKey),
FOREIGN KEY (permissionKey) REFERENCES signet_permission (permissionKey)
)
;
CREATE TABLE signet_permission_limit
(
permissionKey       NUMERIC(12)         NOT NULL,
limitKey            NUMERIC(12)         NOT NULL,
defaultLimitValueValue  NVARCHAR2(64)   NULL,
PRIMARY KEY (permissionKey, limitKey),
FOREIGN KEY (permissionKey) REFERENCES signet_permission (permissionKey),
FOREIGN KEY (limitKey) REFERENCES signet_limit (limitKey)
)
;
-- Signet Subject tables
CREATE TABLE signet_subject (
subjectKey          NUMERIC(12)         NOT NULL,
subjectTypeID       NVARCHAR2(32)       NOT NULL,
subjectID           NVARCHAR2(64)       NOT NULL,
name                NVARCHAR2(120)      NOT NULL,
description         NVARCHAR2(255)      NOT NULL,
modifyDatetime      TIMESTAMP           DEFAULT SYSDATE,
PRIMARY KEY (subjectKey),
UNIQUE (subjectTypeID, subjectID)
)
;
-- Tree tables
CREATE TABLE signet_tree
(
treeID              NVARCHAR2(64)       NOT NULL,
name                NVARCHAR2(120)      NOT NULL,
adapterClass        NVARCHAR2(255)      NOT NULL,
modifyDatetime      TIMESTAMP           DEFAULT SYSDATE,
PRIMARY KEY (treeID)
)
;
CREATE TABLE signet_treeNode
(
treeID              NVARCHAR2(64)       NOT NULL,
nodeID              NVARCHAR2(64)       NOT NULL,
nodeType            NVARCHAR2(32)       NOT NULL,
status              NVARCHAR2(16)       NOT NULL,
name                NVARCHAR2(120)      NOT NULL,
modifyDatetime      TIMESTAMP           DEFAULT SYSDATE,
PRIMARY KEY (treeID, nodeID),
FOREIGN KEY (treeID) REFERENCES signet_tree (treeID)
)
;
CREATE TABLE signet_treeNodeRelationship
(
treeID              NVARCHAR2(64)       NOT NULL,
nodeID              NVARCHAR2(64)       NOT NULL,
parentNodeID        NVARCHAR2(64)       NOT NULL,
PRIMARY KEY (treeID, nodeID, parentNodeID),
FOREIGN KEY (treeID) REFERENCES signet_tree (treeID),
FOREIGN KEY (treeID, nodeID) REFERENCES signet_treeNode (treeID, nodeID),
FOREIGN KEY (treeID, parentNodeID) REFERENCES signet_treeNode (treeID, nodeID)
)
;
-- ChoiceSet tables
CREATE TABLE signet_choiceSet
(
choiceSetKey        NUMERIC(12)         NOT NULL,
choiceSetID         NVARCHAR2(64)       NOT NULL,
adapterClass        NVARCHAR2(255)      NOT NULL,
subsystemID         NVARCHAR2(64)       NULL,
modifyDatetime      TIMESTAMP           DEFAULT SYSDATE,
PRIMARY KEY (choiceSetKey),
UNIQUE (choiceSetID, subsystemID)
)
;
CREATE TABLE signet_choice
(
choiceKey           NUMERIC(12)         NOT NULL,
choiceSetKey        NUMERIC(12)         NOT NULL,
value               NVARCHAR2(32)       NOT NULL,
label               NVARCHAR2(64)       NOT NULL,
rank                NUMERIC(12)         NOT NULL,
displayOrder        NUMERIC(12)         NOT NULL,
modifyDatetime      TIMESTAMP           DEFAULT SYSDATE,
PRIMARY KEY (choiceKey),
UNIQUE (choiceSetKey, value),
FOREIGN KEY (choiceSetKey) REFERENCES signet_choiceSet (choiceSetKey)
)
;
-- Assignment tables
CREATE TABLE signet_assignment
(
assignmentID        NUMERIC(12)         NOT NULL,
instanceNumber      NUMERIC(12)         NOT NULL,
status              NVARCHAR2(16)       NOT NULL,
functionKey         NUMERIC(12)         NOT NULL,
grantorKey          NUMERIC(12)         NOT NULL,
granteeKey          NUMERIC(12)         NOT NULL,
proxyKey            NUMERIC(12)         NULL,
scopeID             NVARCHAR2(64)       NULL,
scopeNodeID         NVARCHAR2(64)       NULL,
canUse              NUMERIC(1)          NOT NULL,
canGrant            NUMERIC(1)          NOT NULL,
effectiveDate       TIMESTAMP           NOT NULL,
expirationDate      TIMESTAMP           NULL,
revokerKey          NUMERIC(12)         NULL,
modifyDatetime      TIMESTAMP           DEFAULT SYSDATE,
PRIMARY KEY (assignmentID),
FOREIGN KEY (grantorKey) REFERENCES signet_subject (subjectKey),
FOREIGN KEY (granteeKey) REFERENCES signet_subject (subjectKey),
FOREIGN KEY (proxyKey) REFERENCES signet_subject (subjectKey),
FOREIGN KEY (revokerKey) REFERENCES signet_subject (subjectKey)
)
;
CREATE INDEX signet_assignment_1
ON signet_assignment (
  grantorKey
)
;
CREATE INDEX signet_assignment_2
ON signet_assignment (
  granteeKey
)
;
CREATE INDEX signet_assignment_3
ON signet_assignment (
  effectiveDate
)
;
CREATE INDEX signet_assignment_4
ON signet_assignment (
  expirationDate
)
;
CREATE TABLE signet_assignmentLimit
(
assignmentID        NUMERIC(12)         NOT NULL,
limitKey            NUMERIC(12)         NOT NULL,
value               NVARCHAR2(32)       NOT NULL,
UNIQUE (assignmentID, limitKey, value),
FOREIGN KEY (assignmentID) REFERENCES signet_assignment (assignmentID),
FOREIGN KEY (limitKey) REFERENCES signet_limit (limitKey)
)
;
CREATE TABLE signet_assignment_history
(
historyID           NUMERIC(12)         NOT NULL,
assignmentID        NUMERIC(12)         NOT NULL,
instanceNumber      NUMERIC(12)         NOT NULL,
status              NVARCHAR2(16)       NOT NULL,
functionKey         NUMERIC(12)         NOT NULL,
grantorKey          NUMERIC(12)         NOT NULL,
granteeKey          NUMERIC(12)         NOT NULL,
proxyKey            NUMERIC(12)         NULL,
scopeID             NVARCHAR2(64)       NULL,
scopeNodeID         NVARCHAR2(64)       NULL,
canUse              NUMERIC(1)          NOT NULL,
canGrant            NUMERIC(1)          NOT NULL,
effectiveDate       TIMESTAMP           NOT NULL,
expirationDate      TIMESTAMP           NULL,
revokerKey          NUMERIC(12)         NULL,
historyDatetime     TIMESTAMP           NOT NULL,
modifyDatetime      TIMESTAMP           DEFAULT SYSDATE,
PRIMARY KEY (historyID),
UNIQUE (assignmentID, instanceNumber),
FOREIGN KEY (grantorKey) REFERENCES signet_subject (subjectKey),
FOREIGN KEY (granteeKey) REFERENCES signet_subject (subjectKey),
FOREIGN KEY (proxyKey) REFERENCES signet_subject (subjectKey),
FOREIGN KEY (revokerKey) REFERENCES signet_subject (subjectKey)
)
;
CREATE INDEX signet_assignment_history_1
ON signet_assignment_history (
  grantorKey
)
;
CREATE INDEX signet_assignment_history_2
ON signet_assignment_history (
  granteeKey
)
;
CREATE TABLE signet_assignmentLimit_history
(
assignment_historyID NUMERIC(12)        NOT NULL,
limitKey            NUMERIC(12)         NOT NULL,
value               NVARCHAR2(32)       NOT NULL,
UNIQUE (assignment_historyID,limitKey,value),
FOREIGN KEY (assignment_historyID)
	REFERENCES signet_assignment_history (historyID),
FOREIGN KEY (limitKey) REFERENCES signet_limit (limitKey)
)
;
CREATE TABLE signet_proxy
(
proxyID             NUMERIC(12)         NOT NULL,
instanceNumber      NUMERIC(12)         NOT NULL,
status              NVARCHAR2(16)       NOT NULL,
subsystemID         NVARCHAR2(64)       NULL,
grantorKey          NUMERIC(12)         NOT NULL,
granteeKey          NUMERIC(12)         NOT NULL,
proxySubjectKey     NUMERIC(12)         NULL,
canUse              NUMERIC(1)          NOT NULL,
canExtend           NUMERIC(1)          NOT NULL,
effectiveDate       TIMESTAMP           NOT NULL,
expirationDate      TIMESTAMP           NULL,
revokerKey          NUMERIC(12)         NULL,
modifyDatetime      TIMESTAMP           DEFAULT SYSDATE,
PRIMARY KEY (proxyID),
FOREIGN KEY (grantorKey) REFERENCES signet_subject (subjectKey),
FOREIGN KEY (granteeKey) REFERENCES signet_subject (subjectKey),
FOREIGN KEY (proxySubjectKey) REFERENCES signet_subject (subjectKey),
FOREIGN KEY (revokerKey) REFERENCES signet_subject (subjectKey)
)
;
CREATE TABLE signet_proxy_history
(
historyID           NUMERIC(12)         NOT NULL,
proxyID             NUMERIC(12)         NOT NULL,
instanceNumber      NUMERIC(12)         NOT NULL,
status              NVARCHAR2(16)       NOT NULL,
subsystemID         NVARCHAR2(64)       NULL,
grantorKey          NUMERIC(12)         NOT NULL,
granteeKey          NUMERIC(12)         NOT NULL,
proxySubjectKey     NUMERIC(12)         NULL,
canUse              NUMERIC(1)          NOT NULL,
canExtend           NUMERIC(1)          NOT NULL,
effectiveDate       TIMESTAMP           NOT NULL,
expirationDate      TIMESTAMP           NULL,
revokerKey          NUMERIC(12)         NULL,
historyDatetime     TIMESTAMP           NOT NULL,
modifyDatetime      TIMESTAMP           DEFAULT SYSDATE,
UNIQUE (proxyID, instanceNumber),
FOREIGN KEY (grantorKey) REFERENCES signet_subject (subjectKey),
FOREIGN KEY (granteeKey) REFERENCES signet_subject (subjectKey),
FOREIGN KEY (proxySubjectKey) REFERENCES signet_subject (subjectKey),
FOREIGN KEY (revokerKey) REFERENCES signet_subject (subjectKey)
)
;
-- Local Source Subject tables (optional)
CREATE TABLE SubjectType (
subjectTypeID     NVARCHAR2(32)     NOT NULL,
name              NVARCHAR2(120)    NOT NULL,
adapterClass      NVARCHAR2(255)    NOT NULL,
modifyDatetime    TIMESTAMP         DEFAULT SYSDATE,
PRIMARY KEY (subjectTypeID)
)
;
CREATE TABLE Subject
(
subjectTypeID     NVARCHAR2(32)     NOT NULL,
subjectID         NVARCHAR2(64)     NOT NULL,
name              NVARCHAR2(120)    NOT NULL,
description       NVARCHAR2(255)    NOT NULL,
displayID         NVARCHAR2(64)     NOT NULL,
modifyDatetime    TIMESTAMP         DEFAULT SYSDATE,
PRIMARY KEY (subjectTypeID, subjectID)
)
;
CREATE TABLE SubjectAttribute
(
subjectTypeID     NVARCHAR2(32)     NOT NULL,
subjectID         NVARCHAR2(64)     NOT NULL,
name              NVARCHAR2(32)     NOT NULL,
instance          NUMERIC(12)       NOT NULL,
value             NVARCHAR2(255)    NOT NULL,
searchValue       NVARCHAR2(255)    NOT NULL,
modifyDatetime    TIMESTAMP         DEFAULT SYSDATE,
PRIMARY KEY (subjectTypeID, subjectID, name, instance),
FOREIGN KEY (subjectTypeID, subjectID)
	REFERENCES Subject (subjectTypeID, subjectID)
)
;
CREATE INDEX SubjectAttribute_1
ON SubjectAttribute (
  subjectID,
  name,
  value
)
;

CREATE SEQUENCE hibernate_sequence
	START WITH 1;
