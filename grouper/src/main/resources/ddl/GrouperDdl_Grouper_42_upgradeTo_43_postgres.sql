CREATE TABLE grouper_mship_req_change
(
    id BIGINT NOT NULL,
    member_id VARCHAR(40) NOT NULL,
    group_id VARCHAR(40) NOT NULL,
    the_timestamp TIMESTAMP NOT NULL,
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

COMMENT ON TABLE grouper_mship_req_change IS 'table to log membership requirements when memberships fall out';

COMMENT ON COLUMN grouper_mship_req_change.id IS 'integer id for this table';

COMMENT ON COLUMN grouper_mship_req_change.member_id IS 'grouper_members uuid reference';

COMMENT ON COLUMN grouper_mship_req_change.group_id IS 'grouper_groups id reference';

COMMENT ON COLUMN grouper_mship_req_change.the_timestamp IS 'when the event took place';

COMMENT ON COLUMN grouper_mship_req_change.engine IS 'H = hook, C = change log consumer, F = full sync';

COMMENT ON COLUMN grouper_mship_req_change.attribute_def_name_id IS 'grouper_attribute_def_name id reference';

COMMENT ON COLUMN grouper_mship_req_change.require_group_id IS 'grouper_groups id reference for the require group';

COMMENT ON COLUMN grouper_mship_req_change.config_id IS 'config id in the grouper.properties config file';

ALTER TABLE grouper_members ADD COLUMN id_index BIGINT;

CREATE UNIQUE INDEX member_id_index_idx ON grouper_members (id_index);

COMMENT ON COLUMN grouper_members.id_index IS 'Sequential id index integer that can we used outside of Grouper';

ALTER TABLE grouper_stem_view_privilege DROP CONSTRAINT fk_grouper_st_v_pr_mem;
ALTER TABLE grouper_stem_view_privilege DROP CONSTRAINT fk_grouper_st_v_pr_st;

update grouper_ddl set last_updated = to_char(current_timestamp, 'YYYY/MM/DD HH12:MI:SS'), history = substring((to_char(current_timestamp, 'YYYY/MM/DD HH12:MI:SS') || ': upgrade Grouper from V' || db_version || ' to V43, ' || history) from 1 for 3500), db_version = 43 where object_name = 'Grouper';
commit;