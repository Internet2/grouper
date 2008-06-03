
Grouper loader is a generic loading utility for grouper that automatically manages lists
 
Documentation is available via the 'Grouper loader' on
the Grouper Project wiki
https://wiki.internet2.edu/confluence/display/GrouperWG/Grouper+-+Loader

## BUILD

cp build.example.properties to build.properties
edit the location of grouper if this is installing there

See "GROUPER_HOME/ext/README.txt" for information on building, testing and
packaging grouperLoader for use with the Grouper API.

Basically, unzip GROUPER_HOME/ext/grouper-loader.zip to that dir, then run:
GROUPER_HOME/ant build

## USE

Run grouper-loader from a Unix-like environment:

 $ GROUPER_HOME/ext/bin/grouperloader.sh

Run grouper-loader from a Windows environment:

 $ GROUPER_HOME/ext/bin/grouperloader.bat
 
## Notes on a quickstart

GROUPER-LOADER
[mchyzer@lukes grouper]$ cvs -d:pserver:anoncvs@anoncvs.internet2.edu:/home/cvs/i2mi login
[mchyzer@lukes grouper]$ cvs -d:pserver:anoncvs@anoncvs.internet2.edu:/home/cvs/i2mi co grouper
[mchyzer@lukes grouper]$ cvs -d:pserver:anoncvs@anoncvs.internet2.edu:/home/cvs/i2mi co grouper-ext/grouper-loader
[mchyzer@lukes grouper]$ cp grouper-ext/grouper-loader/build.example.properties grouper-ext/grouper-loader/build.properties
[mchyzer@lukes grouper]$ emacs grouper-ext/grouper-loader/build.properties 

## edit the build.properties file, set the grouper dir to ../../grouper

[mchyzer@lukes grouper]$ cd grouper-ext/grouper-loader/
[mchyzer@lukes grouper-loader]$ export ANT_HOME=/home/appadmin/software/apache-ant-1.7.0
[mchyzer@lukes grouper-loader]$ export JAVA_HOME=/opt/jdk1.5.0_13
[mchyzer@lukes grouper-loader]$ export PATH=$ANT_HOME/bin:$JAVA_HOME/bin:$PATH
[mchyzer@lukes grouper-loader]$ ant install.package.src

[mchyzer@lukes grouper-loader]$ cd ../../grouper/ext
[mchyzer@lukes ext]$ unzip gsh.zip
[mchyzer@lukes ext]$ cd ..
[mchyzer@lukes grouper]$ ant dist

[mchyzer@lukes grouper]$ ant db.init

[mchyzer@lukes grouper]$ ext/bin/gsh.sh

## add some test subjects

gsh-0.1.1 0% addSubject("10021368","person","Homer");
gsh-0.1.1 1% addSubject("10039438","person","Anna");
gsh-0.1.1 2% addSubject("10064187","person","George");  

## do the one-time grouper-loader setup

gsh-0.1.1 3% subj=SubjectFinder.findById("GrouperSystem")
gsh-0.1.1 4% sess=GrouperSession.start(subj)
gsh-0.1.1 5% type=GroupType.createType(sess, "grouperLoader")
gsh-0.1.1 6% read=Privilege.getInstance("read")
gsh-0.1.1 7% admin=Privilege.getInstance("admin")
gsh-0.1.1 8% type.addAttribute(sess, "grouperLoaderType", read, admin, true)
gsh-0.1.1 9% type.addAttribute(sess, "grouperLoaderDbName", read, admin, true)
gsh-0.1.1 10% type.addAttribute(sess, "grouperLoaderScheduleType", read, admin, true)
gsh-0.1.1 11% type.addAttribute(sess, "grouperLoaderQuery", read, admin, true)
gsh-0.1.1 12% type.addAttribute(sess, "grouperLoaderQuartzCron", read, admin, false)
gsh-0.1.1 13% type.addAttribute(sess, "grouperLoaderIntervalSeconds", read, admin, false)
gsh-0.1.1 14% type.addAttribute(sess, "grouperLoaderPriority", read, admin, false)
gsh-0.1.1 15% type.addAttribute(sess, "grouperLoaderAndGroups", read, admin, false)attribute: 'grouperLoaderType' 

## make a grouper-loadable group

gsh-0.1.1 16% addStem("","aStem","aStem")
gsh-0.1.1 17% addGroup("aStem","aGroup","aGroup");                                                                           
gsh-0.1.1 18% groupAddType("aStem:aGroup","grouperLoader")
gsh-0.1.1 19% setGroupAttr("aStem:aGroup", "grouperLoaderDbName", "testDb")
gsh-0.1.1 21% setGroupAttr("aStem:aGroup", "grouperLoaderIntervalSeconds", "30")
gsh-0.1.1 22% setGroupAttr("aStem:aGroup", "grouperLoaderType", "SQL_SIMPLE")
gsh-0.1.1 23% setGroupAttr("aStem:aGroup", "grouperLoaderQuery", ""select subject_id from agroup2_v")
gsh-0.1.1 24% setGroupAttr("aStem:aGroup", "grouperLoaderScheduleType", "START_TO_START_INTERVAL")
gsh-0.1.1 25% setGroupAttr("aStem:aGroup", "grouperLoaderQuery", "select subject_id from agroup2_v")

## edit the grouper-loader.properties, and add the database "testDb", set default subject source

[mchyzer@lukes grouper]$ cd ext/conf
[mchyzer@lukes conf]$ cp grouper-loader.example.properties  grouper-loader.properties 
[mchyzer@lukes conf]$ emacs grouper-loader.properties

default.subject.source.id = jdbc
db.testDb.user = authzadm
db.testDb.pass = whatever
db.testDb.url = jdbc:oracle:thin:@server.whatever.edu:1521:dcom
db.testDb.driver = oracle.jdbc.driver.OracleDriver

## create a table in that db with some data in it

CREATE TABLE AGROUP2_V (SUBJECT_ID  VARCHAR2(100 CHAR) NOT NULL);
Insert into AGROUP2_V (SUBJECT_ID) Values ('10021368');
Insert into AGROUP2_V (SUBJECT_ID) Values ('10039438');
Insert into AGROUP2_V (SUBJECT_ID) Values ('10064187');
COMMIT;

## start the loader, see the output

[mchyzer@lukes conf]$ cd ../..
[mchyzer@lukes grouper]$ ext/bin/grouperloader.sh

## look in logs, see that there are db updates:

2008-06-03 14:42:31,338: Database requires updates:

/* 2008/06/03 14:42:31: upgrade Grouper from V-1 to V0 */
CREATE TABLE grouper_ddl

...

## Run those against the DB

## Make a sqltool.rc file

[mchyzer@lukes grouper]$ emacs sqltool.rc

urlid grouper
url jdbc:hsqldb:/tmp/grouper/grouper/dist/run/grouper;create=true
username sa
password


[mchyzer@lukes grouper]$ java -jar lib/hsqldb-1.7.2.11.jar --rcfile /tmp/grouper/grouper/sqltool.rc grouper

## Fix the line breaks, and paste all this DDL / SQL in there (copied from logs, this is DB specific)

CREATE TABLE grouper_ddl (    id VARCHAR(128) NOT NULL,    object_name VARCHAR(128),    db_version INTEGER,    java_version INTEGER,    PRIMARY KEY (id));

/* 2008/06/03 14:42:31: upgrade Grouper from V0 to V1 */
ALTER TABLE grouper_ddl ADD COLUMN history VARCHAR(4000);

ALTER TABLE grouper_ddl ADD COLUMN last_updated VARCHAR(50) BEFORE history;

insert into grouper_ddl (id, object_name, db_version, java_version, last_updated, history) values ('ca80691f-7096-436e-a988-3d4fc6e33777', 'Grouper', 0, 1, '2008/06/03 14:42:31', '2008/06/03 14:42:31: upgrade Grouper from V-1 to V0, ');
commit;

CREATE UNIQUE INDEX grouper_ddl_object_name_idx ON grouper_ddl (object_name);

update grouper_ddl set db_version = 1, last_updated = '2008/06/03 14:42:31', history = '2008/06/03 14:42:31: upgrade Grouper from V0 to V1, 2008/06/03 14:42:31: upgrade Grouper from V-1 to V0, ' where object_name = 'Grouper';
commit;

/* 2008/06/03 14:42:31: upgrade GrouperLoader from V-1 to V0 */
CREATE TABLE grouploader_log
(
    id VARCHAR(128) NOT NULL,
    job_name VARCHAR(512),
    status VARCHAR(20),
    started_time TIMESTAMP,
    ended_time TIMESTAMP,
    millis INTEGER,
    millis_get_data INTEGER,
    millis_load_data INTEGER,
    job_type VARCHAR(128),
    job_schedule_type VARCHAR(128),
    job_description VARCHAR(4000),
    job_message VARCHAR(4000),
    host VARCHAR(128),
    group_uuid VARCHAR(128),
    job_schedule_quartz_cron VARCHAR(128),
    job_schedule_interval_seconds INTEGER,
    last_updated TIMESTAMP,
    unresolvable_subject_count INTEGER,
    insert_count INTEGER,
    update_count INTEGER,
    delete_count INTEGER,
    total_count INTEGER,
    parent_job_name VARCHAR(512),
    parent_job_id VARCHAR(128),
    and_group_names VARCHAR(512),
    PRIMARY KEY (id)
);

insert into grouper_ddl (id, object_name, db_version, java_version, last_updated, history) values ('f65d3089-9846-4da8-be3c-aa9b7855f64c', 'GrouperLoader', 0, 2, '2008/06/03 14:42:31', '2008/06/03 14:42:31: upgrade GrouperLoader from V-1 to V0, ');
commit;

/* 2008/06/03 14:42:31: upgrade GrouperLoader from V0 to V1 */
ALTER TABLE grouploader_log ADD COLUMN job_schedule_priority INTEGER;



update grouper_ddl set db_version = 1, last_updated = '2008/06/03 14:42:31', history = '2008/06/03 14:42:31: upgrade GrouperLoader from V0 to V1, 2008/06/03 14:42:31: upgrade GrouperLoader from V-1 to V0, ' where object_name = 'GrouperLoader';
commit;

/* 2008/06/03 14:42:31: upgrade GrouperLoader from V1 to V2 */
CREATE INDEX grouper_loader_job_name_idx ON grouploader_log (job_name);

update grouper_ddl set db_version = 2, last_updated = '2008/06/03 14:42:31', history = '2008/06/03 14:42:31: upgrade GrouperLoader from V1 to V2, 2008/06/03 14:42:31: upgrade GrouperLoader from V0 to V1, 2008/06/03 14:42:31: upgrade GrouperLoader from V-1 to V0, ' where object_name = 'GrouperLoader';
commit;

sql> \q

## Make sure driver is there

[mchyzer@lukes grouper]$ cp /tmp/ojdbc14.jar lib/

## Start up loader again

## Check logs:

[mchyzer@lukes grouper]$ tail -f grouper_error.log

2008-06-03 15:12:46,712: [5b5dfa14-8054-4b68-ae08-8edbcba0c351,'GrouperSystem','application'] add member: group='aStem:aGroup' list='members' subject='10064187'/'person'/'jdbc' (204ms)
2008-06-03 15:12:46,751: [5b5dfa14-8054-4b68-ae08-8edbcba0c351,'GrouperSystem','application'] add member: group='aStem:aGroup' list='members' subject='10039438'/'person'/'jdbc' (19ms)
2008-06-03 15:12:46,771: [5b5dfa14-8054-4b68-ae08-8edbcba0c351,'GrouperSystem','application'] add member: group='aStem:aGroup' list='members' subject='10021368'/'person'/'jdbc' (18ms)

## Go to DB, and remove a member, see the logs remove the member

delete from agroup2_v where subject_id = '10064187';
commit;

## See it update in the logs:

2008-06-03 15:14:15,168: [b4e8d407-94b2-4567-8037-104e82147aa7,'GrouperSystem','application'] delete member: group='aStem:aGroup' list='members' subject='10064187'/'person'/'jdbc' (78ms)

## Go to DB and add the member back in

insert into agroup2_v (subject_id) values ('10064187');
commit;

## See it update in logs

2008-06-03 15:14:45,168: [7aa2f2c3-6238-49d4-a646-b57d00ac2bd8,'GrouperSystem','application'] add member: group='aStem:aGroup' list='members' subject='10064187'/'person'/'jdbc' (54ms)

## note the daemon is running every 30 seconds per the configuration above in the group attribute

## Look in grouper db in the loader logs

[mchyzer@lukes grouper]$ java -jar lib/hsqldb-1.7.2.11.jar --rcfile /tmp/grouper/grouper/sqltool.rc grouper

sql> select * from grouploader_log order by started_time desc;

