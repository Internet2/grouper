define(`_DROP_TABLE',dnl
ifdef(`hsqldb', `DROP TABLE $1 IF EXISTS;', `')dnl
)dnl
define(`_TYPE_INT',         `INTEGER')dnl
define(`_TYPE_STRING',      `_TYPE_STRING_VAR(64)')dnl
define(`_TYPE_STRING_VAR',dnl
ifdef(`oracle', `VARCHAR2($1)', `VARCHAR($1)')dnl
)dnl
define(`_TYPE_TIME',        `_TYPE_STRING_VAR(16)')dnl
define(`_TYPE_UUID',        `_TYPE_STRING_VAR(64)')dnl

-- 
-- Grouper Database Schema
-- 

_DROP_TABLE(`grouper_attribute')
CREATE TABLE grouper_attribute (
  groupKey        _TYPE_UUID() NOT NULL,
  groupField      _TYPE_STRING() NOT NULL,
  groupFieldValue _TYPE_STRING(),
  CONSTRAINT      uniq_ga_gk_gf UNIQUE (groupKey, groupField)
);

_DROP_TABLE(`grouper_field')
CREATE TABLE grouper_field (
  groupField  _TYPE_STRING() NOT NULL PRIMARY KEY,
  readPriv    _TYPE_STRING(),
  writePriv   _TYPE_STRING(),
  isList      _TYPE_STRING_VAR(8)
);

_DROP_TABLE(`grouper_group')
CREATE TABLE grouper_group (
  groupKey      _TYPE_UUID() NOT NULL PRIMARY KEY,
  groupID       _TYPE_UUID() NOT NULL,
  classType     _TYPE_STRING(),
  createTime    _TYPE_TIME(),
  createSubject _TYPE_UUID(),
  createSource  _TYPE_STRING(),
  modifyTime    _TYPE_TIME(),
  modifySubject _TYPE_UUID(),
  modifySource  _TYPE_STRING(),
  groupComment  _TYPE_STRING_VAR(`256'),
  CONSTRAINT    uniq_gg_gid  UNIQUE (groupID)
);

_DROP_TABLE(`grouper_list')
CREATE TABLE grouper_list (
  listKey       _TYPE_UUID() NOT NULL PRIMARY KEY,
  groupKey      _TYPE_UUID() NOT NULL,
  groupField    _TYPE_STRING() NOT NULL,
  memberKey     _TYPE_UUID() NOT NULL,
  chainKey      _TYPE_UUID(),
  viaKey        _TYPE_UUID()
);
CREATE UNIQUE INDEX idx_gl_gk_gf_mk_ck ON grouper_list 
  (groupKey, groupField, memberKey, chainKey);

_DROP_TABLE(`grouper_member')
CREATE TABLE grouper_member (
  memberKey     _TYPE_UUID() NOT NULL PRIMARY KEY,
  memberID      _TYPE_UUID() NOT NULL,
  subjectID     _TYPE_STRING() NOT NULL,
  subjectTypeID _TYPE_STRING() NOT NULL
);
CREATE UNIQUE INDEX idx_gm_sid_stid ON grouper_member
  (subjectID, subjectTypeID);

_DROP_TABLE(`grouper_memberVia')
CREATE TABLE grouper_memberVia (
  chainKey    _TYPE_UUID() NOT NULL,
  chainIdx    _TYPE_INT() NOT NULL,
  listKey     _TYPE_UUID() NOT NULL,
  CONSTRAINT  uniq_gmv_ck_ci_lk UNIQUE (chainKey, chainIdx, listKey)
);

-- TODO Are these the right indices for this table?
CREATE  INDEX idx_mv_ck ON grouper_memberVia (chainKey);
CREATE  INDEX idx_mv_lk ON grouper_memberVia (listKey);

_DROP_TABLE(`grouper_schema')
CREATE TABLE grouper_schema (
  groupKey    _TYPE_UUID() NOT NULL,
  groupType   _TYPE_STRING() NOT NULL,
  CONSTRAINT  uniq_gsch_gk_gt  UNIQUE (groupKey, groupType)
);

_DROP_TABLE(`grouper_session')
CREATE TABLE grouper_session (
  sessionID  _TYPE_UUID() NOT NULL PRIMARY KEY,
  memberID   _TYPE_UUID() NOT NULL,
  startTime  _TYPE_TIME()
);

_DROP_TABLE(`grouper_subject')
CREATE TABLE grouper_subject (
  subjectID     _TYPE_STRING() NOT NULL PRIMARY KEY,
  subjectTypeID _TYPE_STRING() NOT NULL,
  CONSTRAINT    uniq_gsub_sid_stid UNIQUE (subjectID, subjectTypeID)
);

_DROP_TABLE(`grouper_subjectAttribute')
CREATE TABLE grouper_subjectAttribute (
  subjectID     _TYPE_STRING() NOT NULL, 
  subjectTypeID _TYPE_STRING() NOT NULL,
  name          _TYPE_STRING() NOT NULL,
  instance      _TYPE_INT(),
  value         _TYPE_STRING() NOT NULL,
  searchValue   _TYPE_STRING(),
  CONSTRAINT    uniq_gsubattr_sid_stid UNIQUE (subjectID, subjectTypeID)
);

_DROP_TABLE(`grouper_subjectType')
CREATE TABLE grouper_subjectType (
  subjectTypeID _TYPE_STRING() NOT NULL PRIMARY KEY,
  name          _TYPE_STRING() NOT NULL,
  adapterClass  _TYPE_STRING_VAR(`128') NOT NULL
);

_DROP_TABLE(`grouper_typeDef')
CREATE TABLE grouper_typeDef (
  groupType   _TYPE_STRING() NOT NULL,
  groupField  _TYPE_STRING() NOT NULL,
  CONSTRAINT  uniq_gtypdef_gt_gf  UNIQUE (groupType, groupField)
);

_DROP_TABLE(`grouper_type')
CREATE TABLE grouper_type (
  groupType   _TYPE_STRING() NOT NULL PRIMARY KEY
);

COMMIT;

