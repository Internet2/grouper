CREATE TABLE tic_entry
(
    uuid VARCHAR(40) NOT NULL,
    report_format BIGINT NOT NULL,
    the_timestamp TIMESTAMP NOT NULL,
    component VARCHAR(100) NOT NULL,
    institution VARCHAR(100),
    environment VARCHAR(40),
    version VARCHAR(40),
    PRIMARY KEY (uuid)
);

CREATE INDEX tic_entry_version_idx ON tic_entry (report_format);
CREATE INDEX tic_entry_timestamp_idx ON tic_entry (the_timestamp);
CREATE INDEX tic_entry_component_name_idx ON tic_entry (component);
CREATE INDEX tic_entry_institution_idx ON tic_entry (institution);
CREATE INDEX tic_entry_environment_idx ON tic_entry (environment);
CREATE INDEX tic_entry_component_ver_idx ON tic_entry (version);

CREATE TABLE tic_entry_attr
(
    uuid VARCHAR(40) NOT NULL,
    entry_uuid VARCHAR(40) NOT NULL,
    attribute_name VARCHAR(60) NOT NULL,
    attribute_type VARCHAR(40) NOT NULL,
    attribute_value_floating FLOAT,
    attribute_value_integer BIGINT,
    attribute_value_timestamp TIMESTAMP,
    attribute_value_string VARCHAR(4000),
    PRIMARY KEY (uuid)
);

ALTER TABLE tic_entry_attr
    ADD CONSTRAINT tic_entry_attr_entry_uuid_fk FOREIGN KEY (entry_uuid) REFERENCES tic_entry (uuid);

CREATE INDEX tic_entry_attr_entry_uuid_idx ON tic_entry_attr (entry_uuid);
CREATE INDEX tic_entry_attr_name_idx ON tic_entry_attr (attribute_name);
CREATE INDEX tic_entry_attr_type_idx ON tic_entry_attr (attribute_type);
CREATE INDEX tic_entry_attr_float_idx ON tic_entry_attr (attribute_value_floating);
CREATE INDEX tic_entry_attr_int_idx ON tic_entry_attr (attribute_value_integer);
CREATE INDEX tic_entry_attr_tstamp_idx ON tic_entry_attr (attribute_value_timestamp);
CREATE INDEX tic_entry_attr_string_idx ON tic_entry_attr (attribute_value_string);


