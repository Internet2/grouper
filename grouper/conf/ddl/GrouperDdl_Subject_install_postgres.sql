

CREATE TABLE subject
(
    subjectId VARCHAR(255) NOT NULL,
    subjectTypeId VARCHAR(32) NOT NULL,
    name VARCHAR(255),
    PRIMARY KEY (subjectId)
);

CREATE TABLE subjectattribute
(
    subjectId VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    value VARCHAR(255) NOT NULL,
    searchValue VARCHAR(255),
    PRIMARY KEY (subjectId, name, value)
);

CREATE INDEX searchattribute_value_idx ON subjectattribute (value);

CREATE UNIQUE INDEX searchattribute_id_name_idx ON subjectattribute (subjectId, name);

CREATE INDEX searchattribute_name_idx ON subjectattribute (name);

ALTER TABLE subjectattribute
    ADD CONSTRAINT fk_subjectattr_subjectid FOREIGN KEY (subjectId) REFERENCES subject (subjectId);

COMMENT ON TABLE subject IS 'sample subject table for grouper unit tests';

COMMENT ON COLUMN subject.subjectId IS 'subject id of row';

COMMENT ON COLUMN subject.subjectTypeId IS 'subject type e.g. person';

COMMENT ON COLUMN subject.name IS 'name of this subject';

COMMENT ON TABLE subjectattribute IS 'attribute data for each subject';

COMMENT ON COLUMN subjectattribute.subjectId IS 'subject id of row';

COMMENT ON COLUMN subjectattribute.name IS 'name of attribute';

COMMENT ON COLUMN subjectattribute.value IS 'value of attribute';

COMMENT ON COLUMN subjectattribute.searchValue IS 'search value (e.g. all lower)';

insert into grouper_ddl (id, object_name, db_version, last_updated, history) values 
('c08d3e076fdb4c41acdafe5992e5dc4e', 'Subject', 1, to_char(current_timestamp, 'YYYY/MM/DD HH12:MI:SS'), 
to_char(current_timestamp, 'YYYY/MM/DD HH12:MI:SS') || ': upgrade Subject from V0 to V1, ');
commit;
