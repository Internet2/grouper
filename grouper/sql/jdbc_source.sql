--
-- JDBC Source Adapter DDL
-- $Id: jdbc_source.sql,v 1.1 2005-11-28 21:02:55 blair Exp $
--

create table Subject (
  subjectID     varchar(64)   NOT NULL,
  subjectTypeID varchar(32)   NOT NULL,
  name          varchar(255)  NOT NULL,
  primary key   (subjectID)
);

create table SubjectAttribute (
  subjectID     varchar(64)   NOT NULL,
  name          varchar(255)  NOT NULL,
  value         varchar(255)  NOT NULL,
  searchValue   varchar(255)  NULL,
  primary key   (subjectID, name, value),
  foreign key   (subjectID) references Subject (subjectID)
);

