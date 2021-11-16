ALTER TABLE grouper_prov_zoom_user MODIFY COLUMN status VARCHAR(40) NULL;

ALTER TABLE grouper_members ADD COLUMN subject_resolution_eligible VARCHAR(1) DEFAULT 'T' not null;

CREATE INDEX member_eligible_idx ON grouper_members (subject_resolution_eligible);

CREATE TABLE grouper_failsafe
(
    id VARCHAR(40) NOT NULL,
    name VARCHAR(200) NOT NULL,
    last_run BIGINT,
    last_failsafe_issue_started BIGINT,
    last_failsafe_issue BIGINT,
    last_success BIGINT,
    last_approval BIGINT,
    approval_member_id VARCHAR(40) NULL,
    approved_once VARCHAR(1) NOT NULL,
    approved_until BIGINT,
    last_updated BIGINT,
    PRIMARY KEY (id)
);

CREATE UNIQUE INDEX grouper_failsafe_id_idx ON grouper_failsafe (id);

CREATE UNIQUE INDEX grouper_failsafe_name_idx ON grouper_failsafe (name);

CREATE TABLE grouper_last_login
(
    member_uuid VARCHAR(40) NOT NULL,
    last_login BIGINT,
    last_stem_view_need BIGINT,
    last_stem_view_compute BIGINT,
    PRIMARY KEY (member_uuid)
);

CREATE UNIQUE INDEX grouper_last_login_mem_idx ON grouper_last_login (member_uuid);

CREATE INDEX grouper_last_login_login_idx ON grouper_last_login (last_login);

CREATE INDEX grouper_last_login_st_view_idx ON grouper_last_login (last_stem_view_need);

CREATE INDEX grouper_last_login_st_comp_idx ON grouper_last_login (last_stem_view_compute);

CREATE TABLE grouper_stem_view_privilege
(
    member_uuid VARCHAR(40) NOT NULL,
    stem_uuid VARCHAR(40) NOT NULL,
    object_type CHAR(1) NOT NULL
);

CREATE INDEX grouper_stem_v_priv_mem_idx ON grouper_stem_view_privilege (member_uuid, object_type);

CREATE INDEX grouper_stem_v_priv_stem_idx ON grouper_stem_view_privilege (stem_uuid, object_type);

ALTER TABLE grouper_last_login
    ADD CONSTRAINT fk_grouper_last_login_mem FOREIGN KEY (member_uuid) REFERENCES grouper_members (id) on delete cascade;

ALTER TABLE grouper_stem_view_privilege
    ADD CONSTRAINT fk_grouper_st_v_pr_mem FOREIGN KEY (member_uuid) REFERENCES grouper_members (id) on delete cascade;

ALTER TABLE grouper_stem_view_privilege
    ADD CONSTRAINT fk_grouper_st_v_pr_st FOREIGN KEY (stem_uuid) REFERENCES grouper_stems (id) on delete cascade;

update grouper_ddl set last_updated = date_format(current_timestamp(), '%Y/%m/%d %H:%i:%s'), history = substring(concat(date_format(current_timestamp(), '%Y/%m/%d %H:%i:%s'), ': upgrade Grouper from V', db_version, ' to V39, ', history), 1, 3500), db_version = 39 where object_name = 'Grouper';
commit;

