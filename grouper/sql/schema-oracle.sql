
-- 
-- Grouper Database Schema
-- 


CREATE TABLE grouper_attribute (
  groupKey        VARCHAR2(64) NOT NULL,
  groupField      VARCHAR2(64) NOT NULL,
  groupFieldValue VARCHAR2(1024),
  CONSTRAINT      uniq_ga_gk_gf UNIQUE (groupKey, groupField)
);


CREATE TABLE grouper_field (
  groupField  VARCHAR2(64) NOT NULL PRIMARY KEY,
  readPriv    VARCHAR2(64),
  writePriv   VARCHAR2(64),
  isList      VARCHAR2(8)
);


CREATE TABLE grouper_group (
  groupKey      VARCHAR2(64) NOT NULL PRIMARY KEY,
  groupID       VARCHAR2(64) NOT NULL,
  classType     VARCHAR2(64),
  createTime    VARCHAR2(16),
  createSubject VARCHAR2(64),
  createSource  VARCHAR2(64),
  modifyTime    VARCHAR2(16),
  modifySubject VARCHAR2(64),
  modifySource  VARCHAR2(64),
  groupComment  VARCHAR2(256),
  CONSTRAINT    uniq_gg_gid  UNIQUE (groupID)
);


CREATE TABLE grouper_list (
  listKey       VARCHAR2(64) NOT NULL PRIMARY KEY,
  groupKey      VARCHAR2(64) NOT NULL,
  groupField    VARCHAR2(64) NOT NULL,
  memberKey     VARCHAR2(64) NOT NULL,
  chainKey      VARCHAR2(64),
  viaKey        VARCHAR2(64)
);
CREATE UNIQUE INDEX idx_gl_gk_gf_mk_ck ON grouper_list 
  (groupKey, groupField, memberKey, chainKey);


CREATE TABLE grouper_member (
  memberKey     VARCHAR2(64) NOT NULL PRIMARY KEY,
  memberID      VARCHAR2(64) NOT NULL,
  subjectID     VARCHAR2(64) NOT NULL,
  subjectSource VARCHAR2(64) NOT NULL,
  subjectTypeID VARCHAR2(64) NOT NULL
);
CREATE UNIQUE INDEX idx_gm_sid_ss_stid ON grouper_member
  (subjectID, subjectSource, subjectTypeID);


CREATE TABLE grouper_memberVia (
  chainKey    VARCHAR2(64) NOT NULL,
  chainIdx    INTEGER NOT NULL,
  listKey     VARCHAR2(64) NOT NULL,
  CONSTRAINT  uniq_gmv_ck_ci_lk UNIQUE (chainKey, chainIdx, listKey)
);

-- TODO Are these the right indices for this table?
CREATE  INDEX idx_mv_ck ON grouper_memberVia (chainKey);
CREATE  INDEX idx_mv_lk ON grouper_memberVia (listKey);


CREATE TABLE grouper_schema (
  groupKey    VARCHAR2(64) NOT NULL,
  groupType   VARCHAR2(64) NOT NULL,
  CONSTRAINT  uniq_gsch_gk_gt  UNIQUE (groupKey, groupType)
);


CREATE TABLE grouper_session (
  sessionID  VARCHAR2(64) NOT NULL PRIMARY KEY,
  memberID   VARCHAR2(64) NOT NULL,
  startTime  VARCHAR2(16)
);


CREATE TABLE grouper_typeDef (
  groupType   VARCHAR2(64) NOT NULL,
  groupField  VARCHAR2(64) NOT NULL,
  CONSTRAINT  uniq_gtypdef_gt_gf  UNIQUE (groupType, groupField)
);


CREATE TABLE grouper_type (
  groupType   VARCHAR2(64) NOT NULL PRIMARY KEY
);


CREATE TABLE Subject (
  subjectID     VARCHAR2(64) NOT NULL PRIMARY KEY,
  subjectTypeID VARCHAR2(32) NOT NULL,
  name          VARCHAR2(128),
  description   VARCHAR2(255),
  CONSTRAINT    uniq_sub_sid_stid UNIQUE (subjectID, subjectTypeID)
);


CREATE TABLE SubjectAttribute (
  subjectID   VARCHAR2(64) NOT NULL,
  name        VARCHAR2(32) NOT NULL,
  value       VARCHAR2(255) NOT NULL,
  searchValue VARCHAR2(255) NOT NULL,
  PRIMARY KEY (subjectID, name, value),
  FOREIGN KEY (subjectID) REFERENCES Subject (subjectID)
);

COMMIT;

