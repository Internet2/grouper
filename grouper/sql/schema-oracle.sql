
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
  listKey       VARCHAR2(64) NOT NULL PRIMARY KEY,
  groupKey      VARCHAR2(64) NOT NULL,
  groupField    VARCHAR2(64) NOT NULL,
  memberKey     VARCHAR2(64) NOT NULL,
  chainKey      VARCHAR2(64),
  viaKey        VARCHAR2(64)
);
CREATE UNIQUE INDEX idx_gl_gk_gf_mk_ck ON grouper_list 
  (groupKey, groupField, memberKey, chainKey);

DROP TABLE grouper_member;
CREATE TABLE grouper_member (
  memberKey     VARCHAR2(64) NOT NULL PRIMARY KEY,
  memberID      VARCHAR2(64) NOT NULL,
  subjectID     VARCHAR2(64) NOT NULL,
  subjectTypeID VARCHAR2(64) NOT NULL
);
CREATE UNIQUE INDEX idx_gm_sid_stid ON grouper_member
  (subjectID, subjectTypeID);

DROP TABLE grouper_memberVia;
CREATE  TABLE grouper_memberVia (
  chainKey    VARCHAR2(64) NOT NULL,
  chainIdx    INTEGER NOT NULL,
  listKey     VARCHAR2(64) NOT NULL,
  CONSTRAINT  uniq_gmv_ck_ci_lk UNIQUE (chainKey, chainIdx, listKey)
);

-- TODO Are these the right indices for this table?
CREATE  INDEX idx_mv_ck ON grouper_memberVia (chainKey);
CREATE  INDEX idx_mv_lk ON grouper_memberVia (listKey);

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

COMMIT;

