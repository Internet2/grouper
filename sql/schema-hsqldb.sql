
-- 
-- Grouper Database Schema
-- $Id: schema-hsqldb.sql,v 1.30 2005-02-16 20:39:54 blair Exp $
-- 

DROP TABLE grouper_attribute IF EXISTS;
CREATE TABLE grouper_attribute (
  groupKey        VARCHAR(16) NOT NULL,
  groupField      VARCHAR(32) NOT NULL,
  groupFieldValue VARCHAR(64),
  CONSTRAINT      uniq_ga_gk_gf UNIQUE (groupKey, groupField)
);

DROP TABLE grouper_field IF EXISTS;
CREATE TABLE grouper_field (
  groupField  VARCHAR(32) NOT NULL PRIMARY KEY,
  readPriv    VARCHAR(32),
  writePriv   VARCHAR(32),
  isList      VARCHAR(8)
);

DROP TABLE grouper_group IF EXISTS;
CREATE TABLE grouper_group (
  groupKey      VARCHAR(16) NOT NULL PRIMARY KEY,
  groupID       VARCHAR(16) NOT NULL,
  createTime    VARCHAR(16),
  createSubject VARCHAR(16),
  createSource  VARCHAR(32),
  modifyTime    VARCHAR(16),
  modifySubject VARCHAR(16),
  modifySource  VARCHAR(32),
  groupComment  VARCHAR(256),
  CONSTRAINT    uniq_gg_gid  UNIQUE (groupID)
);

DROP TABLE grouper_list IF EXISTS;
CREATE  TABLE grouper_list (
  groupKey      VARCHAR(16) NOT NULL,
  groupField    VARCHAR(32) NOT NULL,
  memberKey     VARCHAR(16) NOT NULL,
  pathKey       VARCHAR(16),
  via           VARCHAR(16),
  removeAfter   VARCHAR(16)
);
CREATE UNIQUE INDEX idx_gl_gk_gf_mk_pk ON grouper_list 
  (groupKey, groupField, memberKey, pathKey);

DROP TABLE grouper_member IF EXISTS;
CREATE TABLE grouper_member (
  memberKey     VARCHAR(16) NOT NULL PRIMARY KEY,
  memberID      VARCHAR(16) NOT NULL,
  subjectID     VARCHAR(32) NOT NULL,
  subjectTypeID VARCHAR(32) NOT NULL
);
CREATE UNIQUE INDEX idx_gm_sid_stid ON grouper_member
  (subjectID, subjectTypeID);

DROP TABLE grouper_schema IF EXISTS;
CREATE TABLE grouper_schema (
  groupKey    VARCHAR(16) NOT NULL,
  groupType   VARCHAR(32) NOT NULL,
  CONSTRAINT  uniq_gsch_gk_gt  UNIQUE (groupKey, groupType)
);

DROP TABLE grouper_session IF EXISTS;
CREATE TABLE grouper_session (
  sessionID  VARCHAR(16) NOT NULL PRIMARY KEY,
  memberID   VARCHAR(16) NOT NULL,
  startTime  VARCHAR(16)
);

DROP TABLE grouper_subject IF EXISTS;
CREATE TABLE grouper_subject (
  subjectID     VARCHAR(64) NOT NULL PRIMARY KEY,
  subjectTypeID VARCHAR(32) NOT NULL,
  CONSTRAINT    uniq_gsub_sid_stid UNIQUE (subjectID, subjectTypeID)
);

DROP TABLE grouper_subjectAttribute IF EXISTS;
CREATE TABLE grouper_subjectAttribute (
  subjectID     VARCHAR(64) NOT NULL, 
  subjectTypeID VARCHAR(32) NOT NULL,
  name          VARCHAR(32) NOT NULL,
  instance      INTEGER,
  value         VARCHAR(32) NOT NULL,
  searchValue   VARCHAR(32),
  CONSTRAINT    uniq_gsubattr_sid_stid UNIQUE (subjectID, subjectTypeID)
);

DROP TABLE grouper_subjectType IF EXISTS;
CREATE TABLE grouper_subjectType (
  subjectTypeID VARCHAR(32) NOT NULL PRIMARY KEY,
  name          VARCHAR(32) NOT NULL,
  adapterClass  VARCHAR(128) NOT NULL
);

DROP TABLE grouper_typeDef IF EXISTS;
CREATE TABLE grouper_typeDef (
  groupType   VARCHAR(32) NOT NULL,
  groupField  VARCHAR(32) NOT NULL,
  CONSTRAINT  uniq_gtypdef_gt_gf  UNIQUE (groupType, groupField)
);

DROP TABLE grouper_type IF EXISTS;
CREATE TABLE grouper_type (
  groupType   VARCHAR(32) NOT NULL PRIMARY KEY
);

DROP TABLE grouper_viaElement IF EXISTS;
CREATE  TABLE grouper_viaElement (
  pathKey       VARCHAR(16) NOT NULL,
  pathIdx       INTEGER NOT NULL,
  groupKey      VARCHAR(16) NOT NULL,
  CONSTRAINT    uniq_gve_pk_pi_gk UNIQUE (pathKey, pathIdx, groupKey)
);
-- TODO Are these the right indices for this table?
CREATE  INDEX gve_pk ON grouper_viaElement (pathKey);
CREATE  INDEX gve_gk ON grouper_viaElement (groupKey);

DROP TABLE grouper_viaPath IF EXISTS;
CREATE  TABLE grouper_viaPath (
  pathKey       VARCHAR(16) NOT NULL PRIMARY KEY
);

COMMIT;

