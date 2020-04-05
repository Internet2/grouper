CREATE TABLE grouper_ddl_worker
(
    id VARCHAR2(40) NOT NULL,
    grouper VARCHAR2(40) NOT NULL,
    worker_uuid VARCHAR2(40) NOT NULL,
    heartbeat DATE,
    last_updated DATE NOT NULL,
    PRIMARY KEY (id)
);

CREATE UNIQUE INDEX grouper_ddl_worker_grp_idx ON grouper_ddl_worker (grouper);

COMMENT ON TABLE grouper_ddl_worker IS 'JVMs register a uuid so only one JVM does the DDL upgrades at a time';

COMMENT ON COLUMN grouper_ddl_worker.grouper IS 'this just holds the word grouper, so there is only one row here';

COMMENT ON COLUMN grouper_ddl_worker.worker_uuid IS 'random uuid from a jvm to do work on the database';

COMMENT ON COLUMN grouper_ddl_worker.heartbeat IS 'while the ddl is running, keep a heartbeat updated';

COMMENT ON COLUMN grouper_ddl_worker.last_updated IS 'when this record was last updated';

