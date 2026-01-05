CREATE TABLE subject
(
    subjectId VARCHAR(255) NOT NULL,
    subjectTypeId VARCHAR(32) NOT NULL,
    name VARCHAR(255) NULL,
    PRIMARY KEY (subjectId)
);

CREATE TABLE subjectattribute
(
    subjectId VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    value VARCHAR(255) NOT NULL,
    searchValue VARCHAR(255) NULL,
    PRIMARY KEY (subjectId, name, value)
);

CREATE INDEX searchattribute_value_idx ON subjectattribute (value);

CREATE UNIQUE INDEX searchattribute_id_name_idx ON subjectattribute (subjectId, name);

CREATE INDEX searchattribute_name_idx ON subjectattribute (name);

ALTER TABLE subjectattribute
    ADD CONSTRAINT fk_subjectattr_subjectid FOREIGN KEY (subjectId) REFERENCES subject (subjectId);


CREATE VIEW subject_base_v
   AS SELECT subjectid AS id,
  name,
  ( SELECT sa2.value
         FROM subjectattribute sa2
        WHERE sa2.name = 'name' AND sa2.subjectid = s.subjectid) AS lfname,
  ( SELECT sa3.value
         FROM subjectattribute sa3
        WHERE sa3.name = 'loginid' AND sa3.subjectid = s.subjectid) AS loginid,
  ( SELECT sa4.value
         FROM subjectattribute sa4
        WHERE sa4.name = 'description' AND sa4.subjectid = s.subjectid) AS description,
  ( SELECT sa5.value
         FROM subjectattribute sa5
        WHERE sa5.name = 'email' AND sa5.subjectid = s.subjectid) AS email
FROM subject s;

create view subject_v as 
  select sbv.id as id, 
  case when id in ('test.subject.6', 'test.subject.7') then null else name end as name_public, 
  name as name_private,
  case when id in ('test.subject.6', 'test.subject.7') then null else lfname end as lfname_public, 
  lfname as lfname_private,
  case when id in ('test.subject.7', 'test.subject.8') then null else loginid end as loginid_public, 
  loginid as loginid_private,
  case when id in ('test.subject.8', 'test.subject.9') then null else description end as description_public, 
  description as description_private,
  case when id in ('test.subject.5', 'test.subject.6') then null else email end as email_public, 
  email as email_private
from subject_base_v sbv;

insert into grouper_ddl (id, object_name, db_version, last_updated, history) values 
('c08d3e076fdb4c41acdafe5992e5dc4e', 'Subject', 2, date_format(current_timestamp(), '%Y/%m/%d %H:%i:%s'), 
concat(date_format(current_timestamp(), '%Y/%m/%d %H:%i:%s'), ': upgrade Subject from V1 to V2, '));
commit;
