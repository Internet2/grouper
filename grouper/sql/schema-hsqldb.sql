-- 
-- Create required Grouper tables
-- $Id: schema-hsqldb.sql,v 1.13 2004-09-19 05:03:27 blair Exp $
-- 

CREATE TABLE grouper_attributes (
  groupKey        VARCHAR(255) NOT NULL,
  groupField      VARCHAR(255) NOT NULL,
  groupFieldValue VARCHAR(255) NOT NULL,
  CONSTRAINT      uniq_gk_gf UNIQUE (groupKey, groupField)
);

-- XXX Right types?
-- XXX Do I need a unique constraint on groupField?
CREATE TABLE grouper_fields (
  groupField  VARCHAR(255) NOT NULL PRIMARY KEY,
  readPriv    VARCHAR(255),
  writePriv   VARCHAR(255),
  isList      VARCHAR(255)
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
  isImmediate     VARCHAR(255),
  via             VARCHAR(255),
  removeAfter     VARCHAR(255),
  CONSTRAINT      uniq_gk_gf_mk UNIQUE (groupKey, groupField, memberKey)
);

CREATE TABLE grouper_member (
  memberKey   VARCHAR(255) NOT NULL PRIMARY KEY,
  memberType  VARCHAR(255) NOT NULL,
  memberID    VARCHAR(255) NOT NULL,
  CONSTRAINT  uniq_mk UNIQUE (memberKey)
);

CREATE TABLE grouper_members (
  memberID        VARCHAR(255) NOT NULL PRIMARY KEY,
  presentationID  VARCHAR(255),
  CONSTRAINT      uniq_mi  UNIQUE (memberID),
  CONSTRAINT      uniq_pi  UNIQUE (presentationID)
);

CREATE TABLE grouper_memberTypes (
  memberType    VARCHAR(255) NOT NULL PRIMARY KEY,
  displayName   VARCHAR(255),
  CONSTRAINT    uniq_mt UNIQUE (memberType)
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

