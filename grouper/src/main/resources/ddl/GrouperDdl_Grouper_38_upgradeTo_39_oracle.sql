ALTER TABLE GROUPER_PROV_ZOOM_USER MODIFY STATUS VARCHAR2(40);

alter table grouper_members add ( subject_resolution_eligible  VARCHAR2(1) default 'T' not null);

CREATE INDEX member_eligible_idx ON grouper_members (subject_resolution_eligible);

CREATE TABLE grouper_failsafe
(
    id VARCHAR2(40) NOT NULL,
    name VARCHAR2(200) NOT NULL,
    last_run NUMBER(38),
    last_failsafe_issue_started NUMBER(38),
    last_failsafe_issue NUMBER(38),
    last_success NUMBER(38),
    last_approval NUMBER(38),
    approval_member_id VARCHAR2(40),
    approved_once VARCHAR2(1) NOT NULL,
    approved_until NUMBER(38),
    last_updated NUMBER(38),
    PRIMARY KEY (id)
);

CREATE UNIQUE INDEX grouper_failsafe_name_idx ON grouper_failsafe (name);

CREATE TABLE grouper_last_login
(
    member_uuid VARCHAR2(40) NOT NULL,
    last_login NUMBER(38),
    last_stem_view_need NUMBER(38),
    last_stem_view_compute NUMBER(38),
    PRIMARY KEY (member_uuid)
);

CREATE INDEX grouper_last_login_login_idx ON grouper_last_login (last_login);

CREATE INDEX grouper_last_login_st_view_idx ON grouper_last_login (last_stem_view_need);

CREATE INDEX grouper_last_login_st_comp_idx ON grouper_last_login (last_stem_view_compute);

CREATE TABLE grouper_stem_view_privilege
(
    member_uuid VARCHAR2(40) NOT NULL,
    stem_uuid VARCHAR2(40) NOT NULL,
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

    COMMENT ON TABLE grouper_failsafe IS 'holds failsafe state and approvals';

COMMENT ON COLUMN grouper_failsafe.id IS 'uuid of this row';

COMMENT ON COLUMN grouper_failsafe.name IS 'name of this failsafe job, e.g. the job name in loader log';

COMMENT ON COLUMN grouper_failsafe.last_run IS 'millis since 1970 of last run of this job (fail or not)';

COMMENT ON COLUMN grouper_failsafe.last_failsafe_issue_started IS 'millis since 1970 of when the last failsafe issue started';

COMMENT ON COLUMN grouper_failsafe.last_failsafe_issue IS 'millis since 1970 of when the last failsafe issue occurred';

COMMENT ON COLUMN grouper_failsafe.last_success IS 'millis since 1970 of when last success of job occurred';

COMMENT ON COLUMN grouper_failsafe.last_approval IS 'millis since 1970 of last approval of failsafe';

COMMENT ON COLUMN grouper_failsafe.approval_member_id IS 'member uuid of user who last approved the failsafe';

COMMENT ON COLUMN grouper_failsafe.approved_once IS 'T if next run is approved (e.g. click button) and F if next run is not approved (steady state)';

COMMENT ON COLUMN grouper_failsafe.approved_until IS 'millis since 1970 that failsafes are approved for the job';

COMMENT ON COLUMN grouper_failsafe.last_updated IS 'millis since 1970 that this row was last updated';

COMMENT ON TABLE grouper_last_login IS 'caches when someone has logged in to grouper in some regard last';

COMMENT ON COLUMN grouper_last_login.member_uuid IS 'member uuid of the subject, foreign key cascade delete';

COMMENT ON COLUMN grouper_last_login.last_login IS 'when last logged in millis since 1970';

COMMENT ON COLUMN grouper_last_login.last_stem_view_need IS 'when last needed stem view';

COMMENT ON COLUMN grouper_last_login.last_stem_view_compute IS 'when stem view privs last computed';

COMMENT ON COLUMN grouper_members.subject_resolution_eligible IS 'T is this subject is resolvable and has privileges and memberships and should be checked periodically by USDU';

COMMENT ON TABLE grouper_stem_view_privilege IS 'caches which stems (not inherited) that a user can view since they have a privilege on an object in the folder';

COMMENT ON COLUMN grouper_stem_view_privilege.member_uuid IS 'member uuid of the subject, foreign key cascade delete';

COMMENT ON COLUMN grouper_stem_view_privilege.stem_uuid IS 'stem uuid of the stem with a view privilege, foreign key cascade delete';

COMMENT ON COLUMN grouper_stem_view_privilege.object_type IS 'G (has group privilege directly in folder), S (has folder privilege on this folder), A (has attribute privilege on an attribute directly in this folder)';


update grouper_ddl set last_updated = to_char(systimestamp, 'YYYY/MM/DD HH12:MI:SS'), history = substr((to_char(systimestamp, 'YYYY/MM/DD HH12:MI:SS') || ': upgrade Grouper from V' || db_version || ' to V39, ' || history), 1, 3500), db_version = 39 where object_name = 'Grouper';
commit;
