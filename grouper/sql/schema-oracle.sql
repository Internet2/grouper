
-- 
-- Grouper Database Schema
-- 

DROP TABLE grouper_attribute;
CREATE TABLE grouper_attribute (
  groupKey        VARCHAR2(64) NOT NULL,
  groupField      VARCHAR2(64) NOT NULL,
  groupFieldValue VARCHAR2(64),
  CONSTRAINT      uniq_ga_gk_gf UNIQUE (groupKey, groupField)
);

DROP TABLE grouper_field;
CREATE TABLE grouper_field (
  groupField  VARCHAR2(64) NOT NULL PRIMARY KEY,
  readPriv    VARCHAR2(64),
  writePriv   VARCHAR2(64),
  isList      VARCHAR2(8)
);

DROP TABLE grouper_group;
CREATE TABLE grouper_group (
  groupKey      VARCHAR2(64) NOT NULL PRIMARY KEY,
  groupID       VARCHAR2(64) NOT NULL,
  createTime    VARCHAR2(16),
  createSubject VARCHAR2(64),
  createSource  VARCHAR2(64),
  modifyTime    VARCHAR2(16),
  modifySubject VARCHAR2(64),
  modifySource  VARCHAR2(64),
  groupComment  VARCHAR2(256),
  CONSTRAINT    uniq_gg_gid  UNIQUE (groupID)
);

DROP TABLE grouper_list;
CREATE  TABLE grouper_list (
  groupKey      VARCHAR2(64) NOT NULL,
  groupField    VARCHAR2(64) NOT NULL,
  memberKey     VARCHAR2(64) NOT NULL,
  pathKey       VARCHAR2(64),
  via           VARCHAR2(64),
  removeAfter   VARCHAR2(16)
);
CREATE UNIQUE INDEX idx_gl_gk_gf_mk_pk ON grouper_list 
  (groupKey, groupField, memberKey, pathKey);

DROP TABLE grouper_member;
CREATE TABLE grouper_member (
  memberKey     VARCHAR2(64) NOT NULL PRIMARY KEY,
  memberID      VARCHAR2(64) NOT NULL,
  subjectID     VARCHAR2(64) NOT NULL,
  subjectTypeID VARCHAR2(64) NOT NULL
);
CREATE UNIQUE INDEX idx_gm_sid_stid ON grouper_member
  (subjectID, subjectTypeID);

DROP TABLE grouper_schema;
CREATE TABLE grouper_schema (
  groupKey    VARCHAR2(64) NOT NULL,
  groupType   VARCHAR2(64) NOT NULL,
  CONSTRAINT  uniq_gsch_gk_gt  UNIQUE (groupKey, groupType)
);

DROP TABLE grouper_session;
CREATE TABLE grouper_session (
  sessionID  VARCHAR2(64) NOT NULL PRIMARY KEY,
  memberID   VARCHAR2(64) NOT NULL,
  startTime  VARCHAR2(16)
);

DROP TABLE grouper_subject;
CREATE TABLE grouper_subject (
  subjectID     VARCHAR2(64) NOT NULL PRIMARY KEY,
  subjectTypeID VARCHAR2(64) NOT NULL,
  CONSTRAINT    uniq_gsub_sid_stid UNIQUE (subjectID, subjectTypeID)
);

DROP TABLE grouper_subjectAttribute;
CREATE TABLE grouper_subjectAttribute (
  subjectID     VARCHAR2(64) NOT NULL, 
  subjectTypeID VARCHAR2(64) NOT NULL,
  name          VARCHAR2(64) NOT NULL,
  instance      INTEGER,
  value         VARCHAR2(64) NOT NULL,
  searchValue   VARCHAR2(64),
  CONSTRAINT    uniq_gsubattr_sid_stid UNIQUE (subjectID, subjectTypeID)
);

DROP TABLE grouper_subjectType;
CREATE TABLE grouper_subjectType (
  subjectTypeID VARCHAR2(64) NOT NULL PRIMARY KEY,
  name          VARCHAR2(64) NOT NULL,
  adapterClass  VARCHAR2(128) NOT NULL
);

DROP TABLE grouper_typeDef;
CREATE TABLE grouper_typeDef (
  groupType   VARCHAR2(64) NOT NULL,
  groupField  VARCHAR2(64) NOT NULL,
  CONSTRAINT  uniq_gtypdef_gt_gf  UNIQUE (groupType, groupField)
);

DROP TABLE grouper_type;
CREATE TABLE grouper_type (
  groupType   VARCHAR2(64) NOT NULL PRIMARY KEY
);

DROP TABLE grouper_viaElement;
CREATE  TABLE grouper_viaElement (
  pathKey       VARCHAR2(64) NOT NULL,
  pathIdx       INTEGER NOT NULL,
  groupKey      VARCHAR2(64) NOT NULL,
  CONSTRAINT    uniq_gve_pk_pi_gk UNIQUE (pathKey, pathIdx, groupKey)
);
-- TODO Are these the right indices for this table?
CREATE  INDEX gve_pk ON grouper_viaElement (pathKey);
CREATE  INDEX gve_gk ON grouper_viaElement (groupKey);

DROP TABLE grouper_viaPath;
CREATE  TABLE grouper_viaPath (
  pathKey       VARCHAR2(64) NOT NULL PRIMARY KEY
);

COMMIT;

