-- 
-- Create required Grouper tables
-- $Id: schema-hsqldb.sql,v 1.17 2004-11-20 02:19:27 blair Exp $
-- 

CREATE TABLE grouper_attributes (
  groupKey        VARCHAR(255) NOT NULL,
  groupField      VARCHAR(255) NOT NULL,
  groupFieldValue VARCHAR(255) NOT NULL,
  CONSTRAINT      uniq_gk_gf UNIQUE (groupKey, groupField)
);

-- XXX Right types?
CREATE TABLE grouper_fields (
  groupField  VARCHAR(255) NOT NULL PRIMARY KEY,
  readPriv    VARCHAR(255),
  writePriv   VARCHAR(255),
  isList      VARCHAR(255),
  CONSTRAINT  uniq_gf UNIQUE (groupField)
);

CREATE TABLE grouper_group (
  groupKey      VARCHAR(255) NOT NULL PRIMARY KEY,
  createTime    VARCHAR(255),
  createSubject VARCHAR(255),
  createSource  VARCHAR(255),
  modifyTime    VARCHAR(255),
  modifySubject VARCHAR(255),
  modifySource  VARCHAR(255),
  comment       VARCHAR(255),
  CONSTRAINT    uniq_gk UNIQUE (groupKey)
);

CREATE TABLE grouper_lists (
  groupKey        VARCHAR(255) NOT NULL,
  groupField      VARCHAR(255) NOT NULL,
  memberKey       VARCHAR(255) NOT NULL,
  via             VARCHAR(255),
  removeAfter     VARCHAR(255),
  CONSTRAINT      uniq_gk_gf_mk_via UNIQUE (groupKey, groupField, memberKey, via)
);

CREATE TABLE grouper_member (
  memberKey     VARCHAR(255) NOT NULL PRIMARY KEY,
  -- TODO Foreign Key
  subjectID     VARCHAR(255) NOT NULL,
  -- TODO Foreign Key
  subjectTypeID VARCHAR(255) NOT NULL,
  CONSTRAINT    uniq_mk_sid_stid UNIQUE (memberKey, subjectID, subjectTypeID)
);

CREATE TABLE grouper_schema (
  -- TODO Foreign Key
  groupKey    VARCHAR(255) NOT NULL,
  -- TOOD Foreign Key
  groupType   VARCHAR(255) NOT NULL,
  CONSTRAINT  uniq_gk_gt  UNIQUE (groupKey, groupType)
);

CREATE TABLE grouper_session (
  sessionID  VARCHAR(255) NOT NULL PRIMARY KEY,
  subjectID  VARCHAR(255) NOT NULL,
  startTime  BIGINT,
  CONSTRAINT uniq_si  UNIQUE (sessionID)
);

CREATE TABLE grouper_subject (
  subjectID     VARCHAR(255) NOT NULL PRIMARY KEY,
  -- TODO Foreign Key
  subjectTypeID VARCHAR(255) NOT NULL,
  CONSTRAINT    uniq_sid_stid UNIQUE (subjectID, subjectTypeID)
);

CREATE TABLE grouper_subjectAttribute (
  -- TODO Foreign Key
  subjectID     VARCHAR(255) NOT NULL, 
  -- TODO Foreign Key
  subjectTypeID VARCHAR(255) NOT NULL,
  name          VARCHAR(255) NOT NULL,
  instance      INTEGER,
  value         VARCHAR(255) NOT NULL,
  searchValue   VARCHAR(255),
  -- TODO Include `instance' (if made !null)?
  CONSTRAINT    uniq_sid_stid UNIQUE (subjectID, subjectTypeID)
);

CREATE TABLE grouper_subjectType (
  subjectTypeID VARCHAR(255) NOT NULL PRIMARY KEY,
  name          VARCHAR(255) NOT NULL,
  adapterClass  VARCHAR(255) NOT NULL,
  CONSTRAINT    uniq_stid UNIQUE (subjectTypeID)
);

CREATE TABLE grouper_typeDefs (
  -- TOOD Foreign Key
  groupType   VARCHAR(255) NOT NULL,
  -- TOOD Foreign Key
  groupField  VARCHAR(255) NOT NULL,
  CONSTRAINT  uniq_gt_gf  UNIQUE (groupType, groupField)
);

CREATE TABLE grouper_types (
  groupType   VARCHAR(255) NOT NULL PRIMARY KEY,
  CONSTRAINT  uniq_gt UNIQUE (groupType)
);

