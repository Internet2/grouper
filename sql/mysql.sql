-- 
-- Create the appropriate Grouper tables
-- $Id: mysql.sql,v 1.2 2004-03-24 21:22:52 blair Exp $
-- 

DROP   DATABASE grouper;
CREATE DATABASE grouper;
USE    grouper;

--

-- TODO CREATE TABLE grouper_aging (
-- TODO  groupID           INTEGER UNSIGNED NOT NULL,
-- TODO  -- XXX The usual date and time issues
-- TODO  changeTime        VARCHAR(255),
-- TODO  -- XXX Hrm...
-- TODO  changeSource      VARCHAR(255),
-- TODO  -- XXX Or an ENUM?  Or something else altogether?
-- TODO  agingState        VARCHAR(255)
-- TODO  FOREIGN KEY  groupID REFERENCES grouper_group (groupID),
-- TODO ) TYPE=InnoDB;

-- TODO CREATE TABLE grouper_factor (
-- TODO  parentID          INTEGER UNSIGNED NOT NULL, -- TODO FOREIGN KEY,
-- TODO  factorID          INTEGER UNSIGNED NOT NULL
-- TODO ) TYPE=InnoDB;

CREATE TABLE grouper_fields (
  groupFieldID      INTEGER UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY UNIQUE,
  readGroupFieldID  INTEGER UNSIGNED NOT NULL,
  writeGroupFieldID INTEGER UNSIGNED NOT NULL,
  -- XXX Boolean types are not supported everywhere.  Alas.  Right now
  -- XXX I am thinking that an ENUM is the most portable option.
  isList            ENUM('TRUE', 'FALSE')
) TYPE=InnoDB;
CREATE INDEX groupfieldid_idx ON grouper_fields (groupFieldID);

CREATE TABLE grouper_group (
  groupID           INTEGER UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY UNIQUE,
  -- XXX What sort of limit do we want to impose upon group names?
  groupName         VARCHAR(255) NOT NULL UNIQUE,
  compoundExpr      TEXT,
  flattenedExpr     TEXT
) TYPE=InnoDB;
CREATE INDEX groupname_idx ON grouper_group (groupName);

CREATE TABLE grouper_members (
  -- XXX Can we rely upon this being numeric?  
  memberID          INTEGER UNSIGNED NOT NULL,
  presentationID    VARCHAR(255)
) TYPE=InnoDB;

CREATE TABLE grouper_membership (
  groupID           INTEGER UNSIGNED NOT NULL,
  groupFieldID      INTEGER UNSIGNED NOT NULL,
  memberID          INTEGER UNSIGNED,
  groupMemberID     INTEGER UNSIGNED,
  -- XXX I don't even want to think about portable date/time formats.
  -- XXX Maybe just store this as a string and rely on Java to Do The
  -- XXX Right Thing?  Good thing we don't care about aging -- yet.
  removeAfter       VARCHAR(255),
  PRIMARY KEY(groupID, groupFieldID),
  CONSTRAINT FOREIGN KEY (groupID)      REFERENCES grouper_group(groupID),
  -- TODO CONSTRAINT FOREIGN KEY (groupFieldID) REFERENCES grouper_fields (groupFieldID)
) TYPE=InnoDB;
CREATE INDEX member_groupmember_idx 
  ON grouper_membership (memberID, groupMemberID);

CREATE TABLE grouper_metadata (
  groupID           INTEGER UNSIGNED NOT NULL,
  groupFieldID      INTEGER UNSIGNED NOT NULL,
  -- XXX No idea
  groupFieldValue   VARCHAR(255),
  PRIMARY KEY (groupID, groupFieldID),
  FOREIGN KEY (groupID)       REFERENCES grouper_group(groupID),
  -- TODO FOREIGN KEY (groupFieldID)  REFERENCES grouper_fields(groupFieldiD),
) TYPE=InnoDB;

CREATE TABLE grouper_schema (
  groupID           INTEGER UNSIGNED NOT NULL,
  groupTypeID       INTEGER UNSIGNED NOT NULL,
  PRIMARY KEY (groupID, groupTypeID),
  FOREIGN KEY (groupID)       REFERENCES grouper_group(groupID),
  -- TODO FOREIGN KEY (groupTypeID)   REFERENCES grouper_types(groupTypeID),
) TYPE=InnoDB;

CREATE TABLE grouper_session (
  -- XXX Or do we let Grouper hand out (pseudo)random session ids?
  -- XXX The paranoia says yes.  
  sessionID         INTEGER UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY UNIQUE,
  cred              VARCHAR(255) NOT NULL, 
  -- XXX This has the same issues as 'membership:removeAfter'
  startTime         VARCHAR(255) NOT NULL
) TYPE=InnoDB;

CREATE TABLE grouper_types (
  groupTypeID       INTEGER UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY UNIQUE,
  groupTypename     VARCHAR(255) NOT NULL
) TYPE=InnoDB;

CREATE TABLE grouper_typedefs (
  groupTypeID       INTEGER UNSIGNED NOT NULL,
  groupFieldID      INTEGER UNSIGNED NOT NULL,
  -- TODO FOREIGN KEY (groupTypeID)   REFERENCES grouper_types (groupTypeID),
  -- TODO FOREIGN KEY (groupFieldID)  REFERENCES grouper_fields (groupFieldiD),
) TYPE=InnoDB;

CREATE TABLE grouper_via (
  groupID           INTEGER UNSIGNED NOT NULL,
  memberID          INTEGER UNSIGNED NOT NULL,
  groupMemberID     INTEGER,
  groupFieldID      INTEGER,
  viaGroupID        INTEGER UNSIGNED NOT NULL
) TYPE=InnoDB;
CREATE INDEX via_idx 
  ON grouper_via (groupID, memberID, groupMemberID, groupFieldID, viaGroupID);

-- 

GRANT ALL ON grouper.* 
  TO 'grouper'@'localhost'
  IDENTIFIED BY 'gr0up3r';

