CREATE TABLE grouper_ddl_worker
(
    id VARCHAR(40) NOT NULL,
    grouper VARCHAR(40) NOT NULL,
    worker_uuid VARCHAR(40) NOT NULL,
    heartbeat DATETIME,
    last_updated DATETIME NOT NULL,
    PRIMARY KEY (id)
);

CREATE UNIQUE INDEX grouper_ddl_worker_grp_idx ON grouper_ddl_worker (grouper);

