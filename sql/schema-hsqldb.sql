-- 
-- Create required Grouper tables
-- $Id: schema-hsqldb.sql,v 1.2 2004-07-26 17:08:08 blair Exp $
-- 

-- XXX Right types?
-- XXX Do I need a unique constraint on groupField?
CREATE TABLE grouper_fields (
  groupField  VARCHAR(255) NOT NULL PRIMARY KEY,
  readPriv    VARCHAR(255),
  writePriv   VARCHAR(255),
  isList      VARCHAR(255)
);

CREATE TABLE grouper_groupTypeDefs (
  -- TOOD Foreign Key
  groupType   INTEGER NOT NULL,
  -- TOOD Foreign Key
  groupField  VARCHAR(255) NOT NULL
);

CREATE TABLE grouper_groupTypes (
  groupType   INTEGER NOT NULL PRIMARY KEY,
  CONSTRAINT  uniq_gt UNIQUE (groupType)
);

CREATE TABLE grouper_members (
  memberID        VARCHAR(255) NOT NULL PRIMARY KEY,
  presentationID  VARCHAR(255),
  CONSTRAINT      uniq_mi  UNIQUE (memberID),
  CONSTRAINT      uniq_pi  UNIQUE (presentationID)
);

CREATE TABLE grouper_session (
  sessionID  VARCHAR(255) NOT NULL PRIMARY KEY,
  cred       VARCHAR(255),
  startTime  BIGINT,
  CONSTRAINT uniq_si  UNIQUE (sessionID)
);

