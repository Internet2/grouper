-- 
-- Create the appropriate Grouper tables
-- $Id: mysql.sql,v 1.14 2004-07-14 02:52:17 blair Exp $
-- 

DROP   DATABASE grouper;
CREATE DATABASE grouper;
USE    grouper;

--

CREATE TABLE grouper_fields (
  groupField        VARCHAR(255) NOT NULL PRIMARY KEY UNIQUE,
  readPriv          VARCHAR(255),
  writePriv         VARCHAR(255),
  -- XXX Boolean types are not supported everywhere.  Alas.  Right now
  -- XXX I am thinking that an ENUM is the most portable option.
  isList            ENUM('TRUE', 'FALSE')
) TYPE=InnoDB;
CREATE INDEX grouper_fields_gf_idx 
  ON grouper_fields (groupField);

CREATE TABLE grouper_group (
  groupID           INTEGER UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY UNIQUE,
  -- XXX What sort of limit do we want to impose upon group names?
  compoundExpr      TEXT,
  flattenedExpr     TEXT
) TYPE=InnoDB;

CREATE TABLE grouper_members (
  memberID          VARCHAR(255) UNIQUE NOT NULL,
  presentationID    VARCHAR(255)
) TYPE=InnoDB;
CREATE UNIQUE INDEX grouper_members_mid_pid_idx
  ON grouper_members (memberID, presentationID);

CREATE TABLE grouper_membership (
  groupID           INTEGER UNSIGNED NOT NULL,
  groupField        VARCHAR(255) NOT NULL,
  memberID          VARCHAR(255) NOT NULL,
  groupMemberID     VARCHAR(255) NOT NULL,
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
CREATE INDEX grouper_membership_gid_gf_idx
  ON grouper_membership (groupID, groupField);
CREATE INDEX grouper_membership_mid_gmid_idx
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
CREATE UNIQUE INDEX grouper_metadata_gid_gf_idx
  ON grouper_metadata (groupID, groupField);

CREATE TABLE grouper_types (
  groupType         VARCHAR(255) NOT NULL PRIMARY KEY UNIQUE
) TYPE=InnoDB;
CREATE INDEX grouper_types_gt_idx
  ON grouper_types (groupType);

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
  -- XXX sessionID         INTEGER UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY UNIQUE,
  sessionID         BIGINT UNSIGNED NOT NULL PRIMARY KEY UNIQUE,
  -- XXX NOT NULL
  cred              VARCHAR(255),
  -- XXX This has the same issues as 'membership:removeAfter'
  -- XXX startTime         VARCHAR(255) NOT NULL
  -- XXX NOT NULL
  startTime         BIGINT UNSIGNED
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
CREATE INDEX grouper_via_gid_mid_gmid_gf_vgid_idx 
  ON grouper_via (groupID, memberID, groupMemberID, groupField, viaGroupID);

-- 

GRANT ALL ON grouper.* 
  TO 'grouper'@'localhost'
  IDENTIFIED BY 'gr0up3r';

