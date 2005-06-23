
-- 
-- Grouper Database Schema
-- 

DROP TABLE grouper_attribute IF EXISTS;
CREATE TABLE grouper_attribute (
  groupKey        VARCHAR(64) NOT NULL,
  groupField      VARCHAR(64) NOT NULL,
  groupFieldValue VARCHAR(64),
  CONSTRAINT      uniq_ga_gk_gf UNIQUE (groupKey, groupField)
);

DROP TABLE grouper_field IF EXISTS;
CREATE TABLE grouper_field (
  groupField  VARCHAR(64) NOT NULL PRIMARY KEY,
  readPriv    VARCHAR(64),
  writePriv   VARCHAR(64),
  isList      VARCHAR(8)
);

DROP TABLE grouper_group IF EXISTS;
CREATE TABLE grouper_group (
  groupKey      VARCHAR(64) NOT NULL PRIMARY KEY,
  groupID       VARCHAR(64) NOT NULL,
  classType     VARCHAR(64),
  createTime    VARCHAR(16),
  createSubject VARCHAR(64),
  createSource  VARCHAR(64),
  modifyTime    VARCHAR(16),
  modifySubject VARCHAR(64),
  modifySource  VARCHAR(64),
  groupComment  VARCHAR(256),
  CONSTRAINT    uniq_gg_gid  UNIQUE (groupID)
);

DROP TABLE grouper_list IF EXISTS;
CREATE TABLE grouper_list (
  listKey       VARCHAR(64) NOT NULL PRIMARY KEY,
  groupKey      VARCHAR(64) NOT NULL,
  groupField    VARCHAR(64) NOT NULL,
  memberKey     VARCHAR(64) NOT NULL,
  chainKey      VARCHAR(64),
  viaKey        VARCHAR(64)
);
CREATE UNIQUE INDEX idx_gl_gk_gf_mk_ck ON grouper_list 
  (groupKey, groupField, memberKey, chainKey);

DROP TABLE grouper_member IF EXISTS;
CREATE TABLE grouper_member (
  memberKey     VARCHAR(64) NOT NULL PRIMARY KEY,
  memberID      VARCHAR(64) NOT NULL,
  subjectID     VARCHAR(64) NOT NULL,
  subjectSource VARCHAR(64) NOT NULL,
  subjectTypeID VARCHAR(64) NOT NULL
);
CREATE UNIQUE INDEX idx_gm_sid_ss_stid ON grouper_member
  (subjectID, subjectSource, subjectTypeID);

DROP TABLE grouper_memberVia IF EXISTS;
CREATE TABLE grouper_memberVia (
  chainKey    VARCHAR(64) NOT NULL,
  chainIdx    INTEGER NOT NULL,
  listKey     VARCHAR(64) NOT NULL,
  CONSTRAINT  uniq_gmv_ck_ci_lk UNIQUE (chainKey, chainIdx, listKey)
);

-- TODO Are these the right indices for this table?
CREATE  INDEX idx_mv_ck ON grouper_memberVia (chainKey);
CREATE  INDEX idx_mv_lk ON grouper_memberVia (listKey);

DROP TABLE grouper_schema IF EXISTS;
CREATE TABLE grouper_schema (
  groupKey    VARCHAR(64) NOT NULL,
  groupType   VARCHAR(64) NOT NULL,
  CONSTRAINT  uniq_gsch_gk_gt  UNIQUE (groupKey, groupType)
);

DROP TABLE grouper_session IF EXISTS;
CREATE TABLE grouper_session (
  sessionID  VARCHAR(64) NOT NULL PRIMARY KEY,
  memberID   VARCHAR(64) NOT NULL,
  startTime  VARCHAR(16)
);

DROP TABLE grouper_typeDef IF EXISTS;
CREATE TABLE grouper_typeDef (
  groupType   VARCHAR(64) NOT NULL,
  groupField  VARCHAR(64) NOT NULL,
  CONSTRAINT  uniq_gtypdef_gt_gf  UNIQUE (groupType, groupField)
);

DROP TABLE grouper_type IF EXISTS;
CREATE TABLE grouper_type (
  groupType   VARCHAR(64) NOT NULL PRIMARY KEY
);

DROP TABLE Subject IF EXISTS;
CREATE TABLE Subject (
  subjectID     VARCHAR(64) NOT NULL PRIMARY KEY,
  subjectTypeID VARCHAR(32) NOT NULL,
  name          VARCHAR(128),
  description   VARCHAR(255),
  CONSTRAINT    uniq_sub_sid_stid UNIQUE (subjectID, subjectTypeID)
);

DROP TABLE SubjectAttribute IF EXISTS;
CREATE TABLE SubjectAttribute (
  subjectID   VARCHAR(64) NOT NULL,
  name        VARCHAR(32) NOT NULL,
  value       VARCHAR(255) NOT NULL,
  searchValue VARCHAR(255) NOT NULL,
  PRIMARY KEY (subjectID, name, value),
  FOREIGN KEY (subjectID) REFERENCES Subject (subjectID)
);

COMMIT;

