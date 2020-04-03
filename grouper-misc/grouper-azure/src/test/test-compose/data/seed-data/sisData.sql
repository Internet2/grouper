CREATE TABLE SIS_COURSES (
  uid varchar(255) NOT NULL,
  surname varchar(255) default NULL,
  givenName varchar(255) default NULL,
  courseId varchar(255) default NULL,
  PRIMARY KEY (uid, courseId)
);