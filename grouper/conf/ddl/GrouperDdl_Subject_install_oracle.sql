
CREATE TABLE subject
(
    subjectId VARCHAR2(255) NOT NULL,
    subjectTypeId VARCHAR2(32) NOT NULL,
    name VARCHAR2(255),
    PRIMARY KEY (subjectId)
);

CREATE TABLE subjectattribute
(
    subjectId VARCHAR2(255) NOT NULL,
    name VARCHAR2(255) NOT NULL,
    value VARCHAR2(255) NOT NULL,
    searchValue VARCHAR2(255),
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

COMMENT ON TABLE subject IS 'sample subject table for grouper unit tests';

COMMENT ON COLUMN subject.subjectId IS 'subject id of row';

COMMENT ON COLUMN subject.subjectTypeId IS 'subject type e.g. person';

COMMENT ON COLUMN subject.name IS 'name of this subject';

COMMENT ON TABLE subjectattribute IS 'attribute data for each subject';

COMMENT ON COLUMN subjectattribute.subjectId IS 'subject id of row';

COMMENT ON COLUMN subjectattribute.name IS 'name of attribute';

COMMENT ON COLUMN subjectattribute.value IS 'value of attribute';

COMMENT ON COLUMN subjectattribute.searchValue IS 'search value (e.g. all lower)';

COMMENT ON VIEW subject_base_v IS 'subject base view';

COMMENT ON COLUMN subject_base_v.id IS 'subject id of row';
COMMENT ON COLUMN subject_base_v.name IS 'name of this subject';
COMMENT ON COLUMN subject_base_v.lfname IS 'last first name of this subject';
COMMENT ON COLUMN subject_base_v.loginid IS 'login id of this subject';
COMMENT ON COLUMN subject_base_v.description IS 'description of this subject';
COMMENT ON COLUMN subject_base_v.description IS 'email of this subject';

COMMENT ON VIEW subject_v IS 'subject view';

COMMENT ON COLUMN subject_v.id IS 'subject id of row';
COMMENT ON COLUMN subject_v.name_public IS 'public name of this subject';
COMMENT ON COLUMN subject_v.name_private IS 'private name of this subject';
COMMENT ON COLUMN subject_v.lfname_public IS 'public last name first name of this subject';
COMMENT ON COLUMN subject_v.lfname_private IS 'private last name first name of this subject';
COMMENT ON COLUMN subject_v.loginid_public IS 'public login id of this subject';
COMMENT ON COLUMN subject_v.loginid_private IS 'private login id of this subject';
COMMENT ON COLUMN subject_v.description_public IS 'public description of this subject';
COMMENT ON COLUMN subject_v.description_private IS 'private description of this subject';
COMMENT ON COLUMN subject_v.email_public IS 'public email of this subject';
COMMENT ON COLUMN subject_v.email_private IS 'private email of this subject';

insert into grouper_ddl (id, object_name, db_version, last_updated, history) values 
('c08d3e076fdb4c41acdafe5992e5dc4e', 'Subject', 2, to_char(systimestamp, 'YYYY/MM/DD HH12:MI:SS'), 
to_char(systimestamp, 'YYYY/MM/DD HH12:MI:SS') || ': upgrade Subject from V1 to V2, ');
commit;
