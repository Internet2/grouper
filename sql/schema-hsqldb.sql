-- 
-- Create required Grouper tables
-- $Id: schema-hsqldb.sql,v 1.28 2005-02-14 01:07:01 blair Exp $
-- 

DROP   TABLE grouper_attribute IF EXISTS;
CREATE TABLE grouper_attribute (
  groupKey        VARCHAR NOT NULL,
  groupField      VARCHAR NOT NULL,
  groupFieldValue VARCHAR,
  CONSTRAINT      uniq_ga_gk_gf UNIQUE (groupKey, groupField)
);

-- XXX Right types?
DROP   TABLE grouper_field IF EXISTS;
CREATE TABLE grouper_field (
  groupField  VARCHAR NOT NULL PRIMARY KEY,
  readPriv    VARCHAR,
  writePriv   VARCHAR,
  isList      VARCHAR,
  CONSTRAINT  uniq_gf_gf UNIQUE (groupField)
);

DROP   TABLE grouper_group IF EXISTS;
CREATE TABLE grouper_group (
  groupKey      VARCHAR NOT NULL PRIMARY KEY,
  groupID       VARCHAR NOT NULL,
  createTime    VARCHAR,
  createSubject VARCHAR,
  createSource  VARCHAR,
  modifyTime    VARCHAR,
  modifySubject VARCHAR,
  modifySource  VARCHAR,
  comment       VARCHAR,
  CONSTRAINT    uniq_gg_gk   UNIQUE (groupKey),
  CONSTRAINT    uniq_gg_gid  UNIQUE (groupID)
);

DROP    TABLE grouper_list IF EXISTS;
CREATE  TABLE grouper_list (
  groupKey      VARCHAR NOT NULL,
  groupField    VARCHAR NOT NULL,
  memberKey     VARCHAR NOT NULL,
  pathKey       VARCHAR,
  via           VARCHAR,         
  removeAfter   VARCHAR,
  CONSTRAINT    uniq_gl_gk_gf_mk_pk UNIQUE (
                  groupKey, groupField, memberKey, pathKey
                )
);
CREATE  INDEX idx_gl_gk_gf_mk_pk ON grouper_list 
  (groupKey, groupField, memberKey, pathKey);

DROP   TABLE grouper_member IF EXISTS;
CREATE TABLE grouper_member (
  memberKey     VARCHAR NOT NULL PRIMARY KEY,
  memberID      VARCHAR NOT NULL,
  -- TODO Foreign Key
  subjectID     VARCHAR NOT NULL,
  -- TODO Foreign Key
  subjectTypeID VARCHAR NOT NULL,
  CONSTRAINT    uniq_gm_mk_mid_sid_stid UNIQUE (memberKey, memberID, subjectID, subjectTypeID)
);
CREATE INDEX idx_gm_sid_stid ON grouper_member
  (subjectID, subjectTypeID);

DROP   TABLE grouper_schema IF EXISTS;
CREATE TABLE grouper_schema (
  -- TODO Foreign Key
  groupKey    VARCHAR NOT NULL,
  -- TOOD Foreign Key
  groupType   VARCHAR NOT NULL,
  CONSTRAINT  uniq_gsch_gk_gt  UNIQUE (groupKey, groupType)
);

DROP   TABLE grouper_session IF EXISTS;
CREATE TABLE grouper_session (
  sessionID  VARCHAR NOT NULL PRIMARY KEY,
  memberID   VARCHAR NOT NULL,
  startTime  VARCHAR,
  CONSTRAINT uniq_gsess_si  UNIQUE (sessionID)
);

DROP   TABLE grouper_subject IF EXISTS;
CREATE TABLE grouper_subject (
  subjectID     VARCHAR NOT NULL PRIMARY KEY,
  -- TODO Foreign Key
  subjectTypeID VARCHAR NOT NULL,
  CONSTRAINT    uniq_gsub_sid_stid UNIQUE (subjectID, subjectTypeID)
);

DROP   TABLE grouper_subjectAttribute IF EXISTS;
CREATE TABLE grouper_subjectAttribute (
  -- TODO Foreign Key
  subjectID     VARCHAR NOT NULL, 
  -- TODO Foreign Key
  subjectTypeID VARCHAR NOT NULL,
  name          VARCHAR NOT NULL,
  instance      INTEGER,
  value         VARCHAR NOT NULL,
  searchValue   VARCHAR,
  -- TODO Include `instance' (if made !null)?
  CONSTRAINT    uniq_gsubattr_sid_stid UNIQUE (subjectID, subjectTypeID)
);

DROP   TABLE grouper_subjectType IF EXISTS;
CREATE TABLE grouper_subjectType (
  subjectTypeID VARCHAR NOT NULL PRIMARY KEY,
  name          VARCHAR NOT NULL,
  adapterClass  VARCHAR NOT NULL,
  CONSTRAINT    uniq_gsubjtyp_stid UNIQUE (subjectTypeID)
);

DROP   TABLE grouper_typeDef IF EXISTS;
CREATE TABLE grouper_typeDef (
  -- TOOD Foreign Key
  groupType   VARCHAR NOT NULL,
  -- TOOD Foreign Key
  groupField  VARCHAR NOT NULL,
  CONSTRAINT  uniq_gtypdef_gt_gf  UNIQUE (groupType, groupField)
);

DROP   TABLE grouper_type IF EXISTS;
CREATE TABLE grouper_type (
  groupType   VARCHAR NOT NULL PRIMARY KEY,
  CONSTRAINT  uniq_gtype_gt UNIQUE (groupType)
);

DROP    TABLE grouper_viaElement IF EXISTS;
CREATE  TABLE grouper_viaElement (
  pathKey       VARCHAR NOT NULL,
  pathIdx       INTEGER NOT NULL,
  groupKey      VARCHAR NOT NULL,
  CONSTRAINT    uniq_gve_pk_pi_gk UNIQUE (pathKey, pathIdx, groupKey)
);
-- TODO Are these the right indices for this table?
CREATE  INDEX gve_pk ON grouper_viaElement (pathKey);
CREATE  INDEX gve_gk ON grouper_viaElement (groupKey);

DROP    TABLE grouper_viaPath IF EXISTS;
CREATE  TABLE grouper_viaPath (
  pathKey       VARCHAR NOT NULL PRIMARY KEY,
  CONSTRAINT    uniq_gvp_pk UNIQUE (pathKey)
);
CREATE  INDEX gvp_pk ON grouper_viaPath (pathKey);

COMMIT;

