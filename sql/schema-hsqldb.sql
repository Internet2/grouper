-- 
-- Create required Grouper tables
-- $Id: schema-hsqldb.sql,v 1.1 2004-07-14 19:36:31 blair Exp $
-- 

CREATE TABLE grouper_members (
  memberID            VARCHAR(255) NOT NULL PRIMARY KEY,
  presentationID      VARCHAR(255),
  CONSTRAINT uniq_mi  UNIQUE (memberID),
  CONSTRAINT uniq_pi  UNIQUE (presentationID)
);

CREATE TABLE grouper_session (
  sessionID           VARCHAR(255) NOT NULL PRIMARY KEY,
  cred                VARCHAR(255),
  startTime           BIGINT,
  CONSTRAINT uniq_si  UNIQUE (sessionID)
);

