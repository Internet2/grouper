-- 
-- Create the appropriate Grouper tables
-- $Id: mysql.sql,v 1.7 2004-04-28 15:59:22 blair Exp $
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
  groupField        VARCHAR(255) NOT NULL PRIMARY KEY UNIQUE,
  readPriv          VARCHAR(255),
  writePriv         VARCHAR(255),
  -- XXX Boolean types are not supported everywhere.  Alas.  Right now
  -- XXX I am thinking that an ENUM is the most portable option.
  isList            ENUM('TRUE', 'FALSE')
) TYPE=InnoDB;
CREATE INDEX groupfield_idx ON grouper_fields (groupField);

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
  groupField        VARCHAR(255) NOT NULL,
  memberID          INTEGER UNSIGNED,
  groupMemberID     INTEGER UNSIGNED,
  -- XXX Boolean types are not supported everywhere.  Alas.  Right now
  -- XXX I am thinking that an ENUM is the most portable option.
  isImmediate       ENUM('TRUE', 'FALSE'),
  -- XXX I don't even want to think about portable date/time formats.
  -- XXX Maybe just store this as a string and rely on Java to Do The
  -- XXX Right Thing?  Good thing we don't care about aging -- yet.
  removeAfter       VARCHAR(255),
  INDEX(groupID),
  INDEX(groupField),
  CONSTRAINT FOREIGN KEY (groupID)    REFERENCES grouper_group (groupID),
  CONSTRAINT FOREIGN KEY (groupField) REFERENCES grouper_fields (groupField)
) TYPE=InnoDB;
CREATE INDEX member_groupmember_idx 
  ON grouper_membership (memberID, groupMemberID);

CREATE TABLE grouper_metadata (
  groupID           INTEGER UNSIGNED NOT NULL,
  groupField        VARCHAR(255) NOT NULL,
  -- XXX No idea
  groupFieldValue   VARCHAR(255),
  INDEX(groupID),
  INDEX(groupField),
  FOREIGN KEY (groupID)     REFERENCES grouper_group(groupID),
  FOREIGN KEY (groupField)  REFERENCES grouper_fields(groupField)
) TYPE=InnoDB;

CREATE TABLE grouper_types (
  groupType         VARCHAR(255) NOT NULL PRIMARY KEY UNIQUE
) TYPE=InnoDB;
CREATE INDEX grouptype_idx ON grouper_types (groupType);

CREATE TABLE grouper_schema (
  groupID           INTEGER UNSIGNED NOT NULL,
  groupType         VARCHAR(255) NOT NULL,
  INDEX(groupID),
  INDEX(groupType),
  FOREIGN KEY (groupID)   REFERENCES grouper_group(groupID),
  FOREIGN KEY (groupType) REFERENCES grouper_types(groupType)
) TYPE=InnoDB;

CREATE TABLE grouper_session (
  -- XXX Or do we let Grouper hand out (pseudo)random session ids?
  -- XXX The paranoia says yes.  
  sessionID         INTEGER UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY UNIQUE,
  cred              VARCHAR(255) NOT NULL, 
  -- XXX This has the same issues as 'membership:removeAfter'
  startTime         VARCHAR(255) NOT NULL
) TYPE=InnoDB;

CREATE TABLE grouper_typedefs (
  groupType         VARCHAR(255) NOT NULL,
  groupField        VARCHAR(255) NOT NULL,
  INDEX(groupType),
  INDEX(groupField),
  FOREIGN KEY (groupType)   REFERENCES grouper_types(groupType),
  FOREIGN KEY (groupField)  REFERENCES grouper_fields(groupField)
) TYPE=InnoDB;

CREATE TABLE grouper_via (
  groupID           INTEGER UNSIGNED NOT NULL,
  memberID          INTEGER UNSIGNED NOT NULL,
  groupMemberID     INTEGER,
  groupField        INTEGER,
  viaGroupID        INTEGER UNSIGNED NOT NULL
) TYPE=InnoDB;
CREATE INDEX via_idx 
  ON grouper_via (groupID, memberID, groupMemberID, groupField, viaGroupID);

-- 

GRANT ALL ON grouper.* 
  TO 'grouper'@'localhost'
  IDENTIFIED BY 'gr0up3r';

-- Insert the base group type
INSERT INTO grouper_types (groupType)
  VALUES ("base");

-- Insert the fields of the base group
INSERT INTO grouper_fields (groupField, readPriv, writePriv, isList)
  VALUES ("description", "READ",   "ADMIN",  'FALSE');
INSERT INTO grouper_fields (groupField, readPriv, writePriv, isList)
  VALUES ("members",     "READ",   "UPDATE", 'TRUE');
INSERT INTO grouper_fields (groupField, readPriv, writePriv, isList)
  VALUES ("viewers",     "UPDATE", "UPDATE", 'TRUE');
INSERT INTO grouper_fields (groupField, readPriv, writePriv, isList)
  VALUES ("readers",     "UPDATE", "UPDATE", 'TRUE');
INSERT INTO grouper_fields (groupField, readPriv, writePriv, isList)
  VALUES ("updaters",    "UPDATE", "UPDATE", 'TRUE');
INSERT INTO grouper_fields (groupField, readPriv, writePriv, isList)
  VALUES ("admins",      "ADMIN",  "ADMIN",  'TRUE');
INSERT INTO grouper_fields (groupField, readPriv, writePriv, isList)
  VALUES ("optins",      "READ",   "UPDATE", 'TRUE');
INSERT INTO grouper_fields (groupField, readPriv, writePriv, isList)
  VALUES ("optouts",     "READ",   "UPDATE", 'TRUE');

-- Assign the fields to the base group
INSERT INTO grouper_typedefs (groupType, groupField)
  VALUES ("base", "description");
INSERT INTO grouper_typedefs (groupType, groupField)
  VALUES ("base", "members");
INSERT INTO grouper_typedefs (groupType, groupField)
  VALUES ("base", "viewers");
INSERT INTO grouper_typedefs (groupType, groupField)
  VALUES ("base", "readers");
INSERT INTO grouper_typedefs (groupType, groupField)
  VALUES ("base", "updaters");
INSERT INTO grouper_typedefs (groupType, groupField)
  VALUES ("base", "admins");
INSERT INTO grouper_typedefs (groupType, groupField)
  VALUES ("base", "optins");
INSERT INTO grouper_typedefs (groupType, groupField)
  VALUES ("base", "optouts");

