CREATE TABLE grouper_mship_req_change
(
    id BIGINT NOT NULL,
    member_id VARCHAR(40) NOT NULL,
    group_id VARCHAR(40) NOT NULL,
    the_timestamp DATETIME NOT NULL,
    engine VARCHAR(1) NOT NULL,
    attribute_def_name_id VARCHAR(40),
    require_group_id VARCHAR(40),
    config_id VARCHAR(80),
    PRIMARY KEY (id)
);

CREATE INDEX grouper_mship_req_mem_gr_idx ON grouper_mship_req_change (group_id, member_id);

CREATE INDEX grouper_mship_req_mem_idx ON grouper_mship_req_change (member_id);

CREATE INDEX grouper_mship_req_time_idx ON grouper_mship_req_change (the_timestamp);

CREATE INDEX grouper_mship_req_conf_id_idx ON grouper_mship_req_change (config_id);

ALTER TABLE grouper_members ADD COLUMN id_index BIGINT;

CREATE UNIQUE INDEX member_id_index_idx ON grouper_members (id_index);

update grouper_ddl set last_updated = date_format(current_timestamp(), '%Y/%m/%d %H:%i:%s'), history = substring(concat(date_format(current_timestamp(), '%Y/%m/%d %H:%i:%s'), ': upgrade Grouper from V', db_version, ' to V43, ', history), 1, 3500), db_version = 43 where object_name = 'Grouper';
commit;