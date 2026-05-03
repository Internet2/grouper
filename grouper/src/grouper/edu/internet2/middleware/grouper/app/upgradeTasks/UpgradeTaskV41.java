package edu.internet2.middleware.grouper.app.upgradeTasks;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase.OtherJobInput;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.tableIndex.TableIndex;
import edu.internet2.middleware.grouper.tableIndex.TableIndexType;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

public class UpgradeTaskV41 implements UpgradeTasksInterface {
  
  @Override
  public boolean doesUpgradeTaskHaveDdlWorkToDo() {
    return (boolean) GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        
        if (!GrouperDdlUtils.assertColumnThere(true, "grouper_sync", "internal_id")) {
          return true;
        }

        if (!GrouperDdlUtils.assertIndexExists("grouper_sync", "grouper_sync_internal_id_idx")) {
          return true;
        }

        if (!GrouperDdlUtils.assertTableThere(true, "grouper_prov_group")) {
          return true;
        }

        if (!GrouperDdlUtils.assertColumnThere(true, "grouper_prov_group", "internal_id")) {
          return true;
        }

        if (!GrouperDdlUtils.assertColumnThere(true, "grouper_prov_group", "grouper_sync_internal_id")) {
          return true;
        }

        if (!GrouperDdlUtils.assertColumnThere(true, "grouper_prov_group", "group_internal_id")) {
          return true;
        }

        if (!GrouperDdlUtils.assertColumnThere(true, "grouper_prov_group", "target_group_id")) {
          return true;
        }

        if (!GrouperDdlUtils.assertColumnThere(true, "grouper_prov_group", "last_updated")) {
          return true;
        }

        if (!GrouperDdlUtils.assertIndexExists("grouper_prov_group", "grouper_prov_grp_idx0")) {
          return true;
        }

        if (!GrouperDdlUtils.assertIndexExists("grouper_prov_group", "grouper_prov_grp_idx1")) {
          return true;
        }

        if (!GrouperDdlUtils.assertForeignKeyExists("grouper_prov_group", "grouper_prov_grp_fk1")) {
          return true;
        }

        if (!GrouperDdlUtils.assertForeignKeyExists("grouper_prov_group", "grouper_prov_grp_fk2")) {
          return true;
        }

        if (!GrouperDdlUtils.assertTableThere(true, "grouper_prov_group_attr")) {
          return true;
        }

        if (!GrouperDdlUtils.assertColumnThere(true, "grouper_prov_group_attr", "internal_id")) {
          return true;
        }

        if (!GrouperDdlUtils.assertColumnThere(true, "grouper_prov_group_attr", "attribute_name")) {
          return true;
        }

        if (!GrouperDdlUtils.assertColumnThere(true, "grouper_prov_group_attr", "grouper_prov_group_internal_id")) {
          return true;
        }

        if (!GrouperDdlUtils.assertColumnThere(true, "grouper_prov_group_attr", "attribute_type")) {
          return true;
        }

        if (!GrouperDdlUtils.assertColumnThere(true, "grouper_prov_group_attr", "last_updated")) {
          return true;
        }

        if (!GrouperDdlUtils.assertIndexExists("grouper_prov_group_attr", "grouper_prov_grpat_idx0")) {
          return true;
        }

        if (!GrouperDdlUtils.assertIndexExists("grouper_prov_group_attr", "grouper_prov_grpat_idx1")) {
          return true;
        }

        if (!GrouperDdlUtils.assertForeignKeyExists("grouper_prov_group_attr", "grouper_prov_grpat_fk1")) {
          return true;
        }

        if (!GrouperDdlUtils.assertTableThere(true, "grouper_prov_group_attr_value")) {
          return true;
        }

        if (!GrouperDdlUtils.assertColumnThere(true, "grouper_prov_group_attr_value", "internal_id")) {
          return true;
        }

        if (!GrouperDdlUtils.assertColumnThere(true, "grouper_prov_group_attr_value", "prov_group_attr_internal_id")) {
          return true;
        }

        if (!GrouperDdlUtils.assertColumnThere(true, "grouper_prov_group_attr_value", "prov_group_internal_id")) {
          return true;
        }

        if (!GrouperDdlUtils.assertColumnThere(true, "grouper_prov_group_attr_value", "value_integer")) {
          return true;
        }

        if (!GrouperDdlUtils.assertColumnThere(true, "grouper_prov_group_attr_value", "value_dictionary_internal_id")) {
          return true;
        }

        if (!GrouperDdlUtils.assertColumnThere(true, "grouper_prov_group_attr_value", "last_updated")) {
          return true;
        }

        if (!GrouperDdlUtils.assertIndexExists("grouper_prov_group_attr_value", "grouper_prov_grpatv_idx0")) {
          return true;
        }

        if (!GrouperDdlUtils.assertIndexExists("grouper_prov_group_attr_value", "grouper_prov_grpatv_idx1")) {
          return true;
        }

        if (!GrouperDdlUtils.assertIndexExists("grouper_prov_group_attr_value", "grouper_prov_grpatv_idx2")) {
          return true;
        }

        if (!GrouperDdlUtils.assertForeignKeyExists("grouper_prov_group_attr_value", "grouper_prov_grpatv_fk1")) {
          return true;
        }

        if (!GrouperDdlUtils.assertForeignKeyExists("grouper_prov_group_attr_value", "grouper_prov_grpatv_fk2")) {
          return true;
        }

        if (!GrouperDdlUtils.assertForeignKeyExists("grouper_prov_group_attr_value", "grouper_prov_grpatv_fk3")) {
          return true;
        }

        if (!GrouperDdlUtils.assertTableThere(true, "grouper_prov_user")) {
          return true;
        }

        if (!GrouperDdlUtils.assertColumnThere(true, "grouper_prov_user", "internal_id")) {
          return true;
        }

        if (!GrouperDdlUtils.assertColumnThere(true, "grouper_prov_user", "grouper_sync_internal_id")) {
          return true;
        }

        if (!GrouperDdlUtils.assertColumnThere(true, "grouper_prov_user", "member_internal_id")) {
          return true;
        }

        if (!GrouperDdlUtils.assertColumnThere(true, "grouper_prov_user", "target_user_id")) {
          return true;
        }

        if (!GrouperDdlUtils.assertColumnThere(true, "grouper_prov_user", "last_updated")) {
          return true;
        }

        if (!GrouperDdlUtils.assertIndexExists("grouper_prov_user", "grouper_prov_user_idx0")) {
          return true;
        }

        if (!GrouperDdlUtils.assertIndexExists("grouper_prov_user", "grouper_prov_user_idx1")) {
          return true;
        }

        if (!GrouperDdlUtils.assertForeignKeyExists("grouper_prov_user", "grouper_prov_user_fk1")) {
          return true;
        }

        if (!GrouperDdlUtils.assertForeignKeyExists("grouper_prov_user", "grouper_prov_user_fk2")) {
          return true;
        }

        if (!GrouperDdlUtils.assertTableThere(true, "grouper_prov_user_attr")) {
          return true;
        }

        if (!GrouperDdlUtils.assertColumnThere(true, "grouper_prov_user_attr", "internal_id")) {
          return true;
        }

        if (!GrouperDdlUtils.assertColumnThere(true, "grouper_prov_user_attr", "attribute_name")) {
          return true;
        }

        if (!GrouperDdlUtils.assertColumnThere(true, "grouper_prov_user_attr", "grouper_prov_user_internal_id")) {
          return true;
        }

        if (!GrouperDdlUtils.assertColumnThere(true, "grouper_prov_user_attr", "attribute_type")) {
          return true;
        }

        if (!GrouperDdlUtils.assertColumnThere(true, "grouper_prov_user_attr", "last_updated")) {
          return true;
        }

        if (!GrouperDdlUtils.assertIndexExists("grouper_prov_user_attr", "grouper_prov_userat_idx0")) {
          return true;
        }

        if (!GrouperDdlUtils.assertIndexExists("grouper_prov_user_attr", "grouper_prov_userat_idx1")) {
          return true;
        }

        if (!GrouperDdlUtils.assertForeignKeyExists("grouper_prov_user_attr", "grouper_prov_userat_fk1")) {
          return true;
        }

        if (!GrouperDdlUtils.assertTableThere(true, "grouper_prov_user_attr_value")) {
          return true;
        }

        if (!GrouperDdlUtils.assertColumnThere(true, "grouper_prov_user_attr_value", "internal_id")) {
          return true;
        }

        if (!GrouperDdlUtils.assertColumnThere(true, "grouper_prov_user_attr_value", "prov_user_attr_internal_id")) {
          return true;
        }

        if (!GrouperDdlUtils.assertColumnThere(true, "grouper_prov_user_attr_value", "prov_user_internal_id")) {
          return true;
        }

        if (!GrouperDdlUtils.assertColumnThere(true, "grouper_prov_user_attr_value", "value_integer")) {
          return true;
        }

        if (!GrouperDdlUtils.assertColumnThere(true, "grouper_prov_user_attr_value", "value_dictionary_internal_id")) {
          return true;
        }

        if (!GrouperDdlUtils.assertColumnThere(true, "grouper_prov_user_attr_value", "last_updated")) {
          return true;
        }

        if (!GrouperDdlUtils.assertIndexExists("grouper_prov_user_attr_value", "grouper_prov_useratv_idx0")) {
          return true;
        }

        if (!GrouperDdlUtils.assertIndexExists("grouper_prov_user_attr_value", "grouper_prov_useratv_idx1")) {
          return true;
        }

        if (!GrouperDdlUtils.assertIndexExists("grouper_prov_user_attr_value", "grouper_prov_useratv_idx2")) {
          return true;
        }

        if (!GrouperDdlUtils.assertForeignKeyExists("grouper_prov_user_attr_value", "grouper_prov_useratv_fk1")) {
          return true;
        }

        if (!GrouperDdlUtils.assertForeignKeyExists("grouper_prov_user_attr_value", "grouper_prov_useratv_fk2")) {
          return true;
        }

        if (!GrouperDdlUtils.assertForeignKeyExists("grouper_prov_user_attr_value", "grouper_prov_useratv_fk3")) {
          return true;
        }

        if (!GrouperDdlUtils.assertTableThere(true, "grouper_prov_mship_role")) {
          return true;
        }

        if (!GrouperDdlUtils.assertColumnThere(true, "grouper_prov_mship_role", "internal_id")) {
          return true;
        }

        if (!GrouperDdlUtils.assertColumnThere(true, "grouper_prov_mship_role", "role_name")) {
          return true;
        }

        if (!GrouperDdlUtils.assertColumnThere(true, "grouper_prov_mship_role", "grouper_sync_internal_id")) {
          return true;
        }

        if (!GrouperDdlUtils.assertColumnThere(true, "grouper_prov_mship_role", "last_updated")) {
          return true;
        }

        if (!GrouperDdlUtils.assertIndexExists("grouper_prov_mship_role", "grouper_prov_mshipr_idx0")) {
          return true;
        }

        if (!GrouperDdlUtils.assertIndexExists("grouper_prov_mship_role", "grouper_prov_mshipr_idx1")) {
          return true;
        }

        if (!GrouperDdlUtils.assertForeignKeyExists("grouper_prov_mship_role", "grouper_prov_mshipr_fk1")) {
          return true;
        }

        if (!GrouperDdlUtils.assertTableThere(true, "grouper_prov_mship")) {
          return true;
        }

        if (!GrouperDdlUtils.assertColumnThere(true, "grouper_prov_mship", "internal_id")) {
          return true;
        }

        if (!GrouperDdlUtils.assertColumnThere(true, "grouper_prov_mship", "grouper_sync_internal_id")) {
          return true;
        }

        if (!GrouperDdlUtils.assertColumnThere(true, "grouper_prov_mship", "prov_user_internal_id")) {
          return true;
        }

        if (!GrouperDdlUtils.assertColumnThere(true, "grouper_prov_mship", "prov_group_internal_id")) {
          return true;
        }

        if (!GrouperDdlUtils.assertColumnThere(true, "grouper_prov_mship", "prov_mship_role_internal_id")) {
          return true;
        }

        if (!GrouperDdlUtils.assertColumnThere(true, "grouper_prov_mship", "last_updated")) {
          return true;
        }

        if (!GrouperDdlUtils.assertIndexExists("grouper_prov_mship", "grouper_prov_mship_idx0")) {
          return true;
        }

        if (!GrouperDdlUtils.assertIndexExists("grouper_prov_mship", "grouper_prov_mship_idx1")) {
          return true;
        }

        if (!GrouperDdlUtils.assertIndexExists("grouper_prov_mship", "grouper_prov_mship_idx2")) {
          return true;
        }

        if (!GrouperDdlUtils.assertForeignKeyExists("grouper_prov_mship", "grouper_prov_mship_fk1")) {
          return true;
        }

        if (!GrouperDdlUtils.assertForeignKeyExists("grouper_prov_mship", "grouper_prov_mship_fk2")) {
          return true;
        }

        if (!GrouperDdlUtils.assertForeignKeyExists("grouper_prov_mship", "grouper_prov_mship_fk3")) {
          return true;
        }

        return false;
      }
    });
  }

  @Override
  public boolean upgradeTaskIsDdl() {
    return true;
  }

  @Override
  public GrouperVersion versionIntroduced() {
    return GrouperVersion.valueOfIgnoreCase("7.0.0");
  }

  @Override
  public void updateVersionFromPrevious(OtherJobInput otherJobInput) {
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        
        if (!GrouperDdlUtils.assertColumnThere(true, "grouper_sync", "internal_id")) {
          // add as nullable first so existing rows don't fail
          if (GrouperDdlUtils.isPostgres()) {
            new GcDbAccess().sql("ALTER TABLE grouper_sync ADD COLUMN internal_id BIGINT").executeSql();
          } else if (GrouperDdlUtils.isMysql()) {
            new GcDbAccess().sql("ALTER TABLE grouper_sync ADD COLUMN internal_id BIGINT AFTER last_full_metadata_sync_run").executeSql();
          } else {
            new GcDbAccess().sql("ALTER TABLE grouper_sync ADD internal_id NUMBER(38)").executeSql();
          }

          if (GrouperDdlUtils.isPostgres() || GrouperDdlUtils.isOracle()) {
            new GcDbAccess().sql("COMMENT ON COLUMN grouper_sync.internal_id IS 'internal integer id for this table.  Do not refer to this outside of Grouper.  This will differ per env (dev/test/prod)'").executeSql();
          }

          // backfill pre-existing rows so the column can be NOT NULL
          List<String> syncIdsMissingInternalId = new GcDbAccess()
              .sql("select id from grouper_sync where internal_id is null")
              .selectList(String.class);
          if (GrouperUtil.length(syncIdsMissingInternalId) > 0) {
            List<Long> reservedInternalIds = TableIndex.reserveIds(TableIndexType.syncInternalId, syncIdsMissingInternalId.size());
            List<List<Object>> batchBindVars = new ArrayList<List<Object>>();
            for (int i = 0; i < syncIdsMissingInternalId.size(); i++) {
              batchBindVars.add(GrouperUtil.toListObject(reservedInternalIds.get(i), syncIdsMissingInternalId.get(i)));
            }
            new GcDbAccess()
                .sql("update grouper_sync set internal_id = ? where id = ?")
                .batchBindVars(batchBindVars)
                .executeBatchSql();
          }

          // now that all rows have a value, alter to NOT NULL to match install/DDL definition
          if (GrouperDdlUtils.isPostgres()) {
            new GcDbAccess().sql("ALTER TABLE grouper_sync ALTER COLUMN internal_id SET NOT NULL").executeSql();
          } else if (GrouperDdlUtils.isMysql()) {
            new GcDbAccess().sql("ALTER TABLE grouper_sync MODIFY internal_id BIGINT NOT NULL").executeSql();
          } else {
            new GcDbAccess().sql("ALTER TABLE grouper_sync MODIFY (internal_id NOT NULL)").executeSql();
          }

          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added column grouper_sync.internal_id (backfilled "
                + GrouperUtil.length(syncIdsMissingInternalId) + " rows, NOT NULL)");
          }
        }

        if (!GrouperDdlUtils.assertIndexExists("grouper_sync", "grouper_sync_internal_id_idx")) {
          new GcDbAccess().sql("CREATE UNIQUE INDEX grouper_sync_internal_id_idx ON grouper_sync (internal_id)").executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index grouper_sync_internal_id_idx");
          }
        }

        // Oracle requires a unique CONSTRAINT (not just a unique index) to satisfy FK references
        if (GrouperDdlUtils.isOracle() && !GrouperDdlUtils.doesConstraintExistOracle("grouper_sync_internal_id_unq")) {
          try {
            new GcDbAccess().sql("ALTER TABLE grouper_sync ADD CONSTRAINT grouper_sync_internal_id_unq UNIQUE (internal_id)").executeSql();
          } catch (Exception e) {
            if (!GrouperUtil.getFullStackTrace(e).contains("ORA-02261")) {
              throw e;
            }
          }
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added constraint grouper_sync_internal_id_unq");
          }
        }

        if (!GrouperDdlUtils.assertTableThere(true, "grouper_prov_group")) {
          if (GrouperDdlUtils.isOracle()) {
            new GcDbAccess().sql("""
                CREATE TABLE grouper_prov_group (
                  internal_id NUMBER(38) NOT NULL,
                  grouper_sync_internal_id NUMBER(38) NOT NULL,
                  group_internal_id NUMBER(38),
                  target_group_id VARCHAR2(256) NOT NULL,
                  last_updated NUMBER(38) NOT NULL,
                  PRIMARY KEY (internal_id)
                )
              """).executeSql();
          } else {
            new GcDbAccess().sql("""
                CREATE TABLE grouper_prov_group (
                  internal_id BIGINT NOT NULL,
                  grouper_sync_internal_id BIGINT NOT NULL,
                  group_internal_id BIGINT,
                  target_group_id VARCHAR(256) NOT NULL,
                  last_updated BIGINT NOT NULL,
                  PRIMARY KEY (internal_id)
                )
              """).executeSql();
          }

          if (GrouperDdlUtils.isPostgres() || GrouperDdlUtils.isOracle()) {
            new GcDbAccess().sql("COMMENT ON TABLE grouper_prov_group IS 'Group mappings in a target system for a provisioner'").executeSql();
            new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_group.internal_id IS 'internal integer id for this table.  Do not refer to this outside of Grouper.  This will differ per env (dev/test/prod)'").executeSql();
            new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_group.grouper_sync_internal_id IS 'foreign key to grouper_sync.internal_id'").executeSql();
            new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_group.group_internal_id IS 'optional foreign key to grouper_groups.internal_id'").executeSql();
            new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_group.target_group_id IS 'target system group id'").executeSql();
            new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_group.last_updated IS 'timestamp in micros since 1970 when this record was last updated'").executeSql();
          }

          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", created table grouper_prov_group");
          }
        }

        if (!GrouperDdlUtils.assertIndexExists("grouper_prov_group", "grouper_prov_grp_idx0")) {
          new GcDbAccess().sql("CREATE INDEX grouper_prov_grp_idx0 ON grouper_prov_group (grouper_sync_internal_id)").executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index grouper_prov_grp_idx0");
          }
        }

        if (!GrouperDdlUtils.assertIndexExists("grouper_prov_group", "grouper_prov_grp_idx1")) {
          new GcDbAccess().sql("CREATE INDEX grouper_prov_grp_idx1 ON grouper_prov_group (group_internal_id)").executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index grouper_prov_grp_idx1");
          }
        }

        if (!GrouperDdlUtils.assertForeignKeyExists("grouper_prov_group", "grouper_prov_grp_fk1")) {
          new GcDbAccess().sql("ALTER TABLE grouper_prov_group ADD CONSTRAINT grouper_prov_grp_fk1 FOREIGN KEY (grouper_sync_internal_id) REFERENCES grouper_sync(internal_id)").executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added foreign key grouper_prov_grp_fk1");
          }
        }

        if (!GrouperDdlUtils.assertForeignKeyExists("grouper_prov_group", "grouper_prov_grp_fk2")) {
          new GcDbAccess().sql("ALTER TABLE grouper_prov_group ADD CONSTRAINT grouper_prov_grp_fk2 FOREIGN KEY (group_internal_id) REFERENCES grouper_groups(internal_id)").executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added foreign key grouper_prov_grp_fk2");
          }
        }

        if (!GrouperDdlUtils.assertTableThere(true, "grouper_prov_group_attr")) {
          if (GrouperDdlUtils.isOracle()) {
            new GcDbAccess().sql("""
                CREATE TABLE grouper_prov_group_attr (
                  internal_id NUMBER(38) NOT NULL,
                  attribute_name VARCHAR2(500) NOT NULL,
                  grouper_prov_group_internal_id NUMBER(38) NOT NULL,
                  attribute_type VARCHAR2(20) NOT NULL,
                  last_updated NUMBER(38) NOT NULL,
                  PRIMARY KEY (internal_id)
                )
              """).executeSql();
          } else {
            new GcDbAccess().sql("""
                CREATE TABLE grouper_prov_group_attr (
                  internal_id BIGINT NOT NULL,
                  attribute_name VARCHAR(500) NOT NULL,
                  grouper_prov_group_internal_id BIGINT NOT NULL,
                  attribute_type VARCHAR(20) NOT NULL,
                  last_updated BIGINT NOT NULL,
                  PRIMARY KEY (internal_id)
                )
              """).executeSql();
          }

          if (GrouperDdlUtils.isPostgres() || GrouperDdlUtils.isOracle()) {
            new GcDbAccess().sql("COMMENT ON TABLE grouper_prov_group_attr IS 'Provisioner group attributes metadata'").executeSql();
            new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_group_attr.internal_id IS 'internal integer id for this table.  Do not refer to this outside of Grouper.  This will differ per env (dev/test/prod)'").executeSql();
            new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_group_attr.attribute_name IS 'group attribute name e.g. group_name or group_description'").executeSql();
            new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_group_attr.grouper_prov_group_internal_id IS 'foreign key to grouper_prov_group.internal_id'").executeSql();
            new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_group_attr.attribute_type IS 'attribute type e.g. string, int, boolean, timestamp'").executeSql();
            new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_group_attr.last_updated IS 'timestamp in micros since 1970 when this record was last updated'").executeSql();
          }

          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", created table grouper_prov_group_attr");
          }
        }

        if (!GrouperDdlUtils.assertIndexExists("grouper_prov_group_attr", "grouper_prov_grpat_idx0")) {
          new GcDbAccess().sql("CREATE INDEX grouper_prov_grpat_idx0 ON grouper_prov_group_attr (grouper_prov_group_internal_id)").executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index grouper_prov_grpat_idx0");
          }
        }

        if (!GrouperDdlUtils.assertIndexExists("grouper_prov_group_attr", "grouper_prov_grpat_idx1")) {
          new GcDbAccess().sql("CREATE INDEX grouper_prov_grpat_idx1 ON grouper_prov_group_attr (grouper_prov_group_internal_id, attribute_name)").executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index grouper_prov_grpat_idx1");
          }
        }

        if (!GrouperDdlUtils.assertForeignKeyExists("grouper_prov_group_attr", "grouper_prov_grpat_fk1")) {
          new GcDbAccess().sql("ALTER TABLE grouper_prov_group_attr ADD CONSTRAINT grouper_prov_grpat_fk1 FOREIGN KEY (grouper_prov_group_internal_id) REFERENCES grouper_prov_group(internal_id) on delete cascade").executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added foreign key grouper_prov_grpat_fk1");
          }
        }

        if (!GrouperDdlUtils.assertTableThere(true, "grouper_prov_group_attr_value")) {
          if (GrouperDdlUtils.isOracle()) {
            new GcDbAccess().sql("""
                CREATE TABLE grouper_prov_group_attr_value (
                  internal_id NUMBER(38) NOT NULL,
                  prov_group_attr_internal_id NUMBER(38) NOT NULL,
                  prov_group_internal_id NUMBER(38) NOT NULL,
                  value_integer NUMBER(38),
                  value_dictionary_internal_id NUMBER(38),
                  last_updated NUMBER(38) NOT NULL,
                  PRIMARY KEY (internal_id)
                )
              """).executeSql();
          } else {
            new GcDbAccess().sql("""
                CREATE TABLE grouper_prov_group_attr_value (
                  internal_id BIGINT NOT NULL,
                  prov_group_attr_internal_id BIGINT NOT NULL,
                  prov_group_internal_id BIGINT NOT NULL,
                  value_integer BIGINT,
                  value_dictionary_internal_id BIGINT,
                  last_updated BIGINT NOT NULL,
                  PRIMARY KEY (internal_id)
                )
              """).executeSql();
          }

          if (GrouperDdlUtils.isPostgres() || GrouperDdlUtils.isOracle()) {
            new GcDbAccess().sql("COMMENT ON TABLE grouper_prov_group_attr_value IS 'Provisioner group attribute values'").executeSql();
            new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_group_attr_value.internal_id IS 'internal integer id for this table.  Do not refer to this outside of Grouper.  This will differ per env (dev/test/prod)'").executeSql();
            new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_group_attr_value.prov_group_attr_internal_id IS 'foreign key to grouper_prov_group_attr.internal_id'").executeSql();
            new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_group_attr_value.prov_group_internal_id IS 'foreign key to grouper_prov_group.internal_id'").executeSql();
            new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_group_attr_value.value_integer IS 'integer value used for int, boolean, or timestamp'").executeSql();
            new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_group_attr_value.value_dictionary_internal_id IS 'foreign key to grouper_dictionary.internal_id for string values'").executeSql();
            new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_group_attr_value.last_updated IS 'timestamp in micros since 1970 when this record was last updated'").executeSql();
          }

          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", created table grouper_prov_group_attr_value");
          }
        }

        if (!GrouperDdlUtils.assertIndexExists("grouper_prov_group_attr_value", "grouper_prov_grpatv_idx0")) {
          new GcDbAccess().sql("CREATE INDEX grouper_prov_grpatv_idx0 ON grouper_prov_group_attr_value (prov_group_attr_internal_id)").executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index grouper_prov_grpatv_idx0");
          }
        }

        if (!GrouperDdlUtils.assertIndexExists("grouper_prov_group_attr_value", "grouper_prov_grpatv_idx1")) {
          new GcDbAccess().sql("CREATE INDEX grouper_prov_grpatv_idx1 ON grouper_prov_group_attr_value (prov_group_internal_id)").executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index grouper_prov_grpatv_idx1");
          }
        }

        if (!GrouperDdlUtils.assertIndexExists("grouper_prov_group_attr_value", "grouper_prov_grpatv_idx2")) {
          new GcDbAccess().sql("CREATE INDEX grouper_prov_grpatv_idx2 ON grouper_prov_group_attr_value (value_dictionary_internal_id)").executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index grouper_prov_grpatv_idx2");
          }
        }

        if (!GrouperDdlUtils.assertForeignKeyExists("grouper_prov_group_attr_value", "grouper_prov_grpatv_fk1")) {
          new GcDbAccess().sql("ALTER TABLE grouper_prov_group_attr_value ADD CONSTRAINT grouper_prov_grpatv_fk1 FOREIGN KEY (prov_group_attr_internal_id) REFERENCES grouper_prov_group_attr(internal_id) on delete cascade").executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added foreign key grouper_prov_grpatv_fk1");
          }
        }

        if (!GrouperDdlUtils.assertForeignKeyExists("grouper_prov_group_attr_value", "grouper_prov_grpatv_fk2")) {
          new GcDbAccess().sql("ALTER TABLE grouper_prov_group_attr_value ADD CONSTRAINT grouper_prov_grpatv_fk2 FOREIGN KEY (prov_group_internal_id) REFERENCES grouper_prov_group(internal_id) on delete cascade").executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added foreign key grouper_prov_grpatv_fk2");
          }
        }

        if (!GrouperDdlUtils.assertForeignKeyExists("grouper_prov_group_attr_value", "grouper_prov_grpatv_fk3")) {
          new GcDbAccess().sql("ALTER TABLE grouper_prov_group_attr_value ADD CONSTRAINT grouper_prov_grpatv_fk3 FOREIGN KEY (value_dictionary_internal_id) REFERENCES grouper_dictionary(internal_id)").executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added foreign key grouper_prov_grpatv_fk3");
          }
        }

        if (!GrouperDdlUtils.assertTableThere(true, "grouper_prov_user")) {
          if (GrouperDdlUtils.isOracle()) {
            new GcDbAccess().sql("""
                CREATE TABLE grouper_prov_user (
                  internal_id NUMBER(38) NOT NULL,
                  grouper_sync_internal_id NUMBER(38) NOT NULL,
                  member_internal_id NUMBER(38),
                  target_user_id VARCHAR2(256) NOT NULL,
                  last_updated NUMBER(38) NOT NULL,
                  PRIMARY KEY (internal_id)
                )
              """).executeSql();
          } else {
            new GcDbAccess().sql("""
                CREATE TABLE grouper_prov_user (
                  internal_id BIGINT NOT NULL,
                  grouper_sync_internal_id BIGINT NOT NULL,
                  member_internal_id BIGINT,
                  target_user_id VARCHAR(256) NOT NULL,
                  last_updated BIGINT NOT NULL,
                  PRIMARY KEY (internal_id)
                )
              """).executeSql();
          }

          if (GrouperDdlUtils.isPostgres() || GrouperDdlUtils.isOracle()) {
            new GcDbAccess().sql("COMMENT ON TABLE grouper_prov_user IS 'Provisioner users'").executeSql();
            new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_user.internal_id IS 'internal integer id for this table.  Do not refer to this outside of Grouper.  This will differ per env (dev/test/prod)'").executeSql();
            new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_user.grouper_sync_internal_id IS 'foreign key to grouper_sync.internal_id'").executeSql();
            new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_user.member_internal_id IS 'optional foreign key to grouper_members.internal_id'").executeSql();
            new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_user.target_user_id IS 'target system user id'").executeSql();
            new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_user.last_updated IS 'timestamp in micros since 1970 when this record was last updated'").executeSql();
          }

          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", created table grouper_prov_user");
          }
        }

        if (!GrouperDdlUtils.assertIndexExists("grouper_prov_user", "grouper_prov_user_idx0")) {
          new GcDbAccess().sql("CREATE INDEX grouper_prov_user_idx0 ON grouper_prov_user (grouper_sync_internal_id)").executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index grouper_prov_user_idx0");
          }
        }

        if (!GrouperDdlUtils.assertIndexExists("grouper_prov_user", "grouper_prov_user_idx1")) {
          new GcDbAccess().sql("CREATE INDEX grouper_prov_user_idx1 ON grouper_prov_user (member_internal_id)").executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index grouper_prov_user_idx1");
          }
        }

        if (!GrouperDdlUtils.assertForeignKeyExists("grouper_prov_user", "grouper_prov_user_fk1")) {
          new GcDbAccess().sql("ALTER TABLE grouper_prov_user ADD CONSTRAINT grouper_prov_user_fk1 FOREIGN KEY (grouper_sync_internal_id) REFERENCES grouper_sync(internal_id)").executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added foreign key grouper_prov_user_fk1");
          }
        }

        if (!GrouperDdlUtils.assertForeignKeyExists("grouper_prov_user", "grouper_prov_user_fk2")) {
          new GcDbAccess().sql("ALTER TABLE grouper_prov_user ADD CONSTRAINT grouper_prov_user_fk2 FOREIGN KEY (member_internal_id) REFERENCES grouper_members(internal_id)").executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added foreign key grouper_prov_user_fk2");
          }
        }

        if (!GrouperDdlUtils.assertTableThere(true, "grouper_prov_user_attr")) {
          if (GrouperDdlUtils.isOracle()) {
            new GcDbAccess().sql("""
                CREATE TABLE grouper_prov_user_attr (
                  internal_id NUMBER(38) NOT NULL,
                  attribute_name VARCHAR2(500) NOT NULL,
                  grouper_prov_user_internal_id NUMBER(38) NOT NULL,
                  attribute_type VARCHAR2(20) NOT NULL,
                  last_updated NUMBER(38) NOT NULL,
                  PRIMARY KEY (internal_id)
                )
              """).executeSql();
          } else {
            new GcDbAccess().sql("""
                CREATE TABLE grouper_prov_user_attr (
                  internal_id BIGINT NOT NULL,
                  attribute_name VARCHAR(500) NOT NULL,
                  grouper_prov_user_internal_id BIGINT NOT NULL,
                  attribute_type VARCHAR(20) NOT NULL,
                  last_updated BIGINT NOT NULL,
                  PRIMARY KEY (internal_id)
                )
              """).executeSql();
          }

          if (GrouperDdlUtils.isPostgres() || GrouperDdlUtils.isOracle()) {
            new GcDbAccess().sql("COMMENT ON TABLE grouper_prov_user_attr IS 'Provisioner user attributes metadata'").executeSql();
            new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_user_attr.internal_id IS 'internal integer id for this table.  Do not refer to this outside of Grouper.  This will differ per env (dev/test/prod)'").executeSql();
            new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_user_attr.attribute_name IS 'user attribute name e.g. user_name or user_email'").executeSql();
            new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_user_attr.grouper_prov_user_internal_id IS 'foreign key to grouper_prov_user.internal_id'").executeSql();
            new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_user_attr.attribute_type IS 'attribute type e.g. string, int, boolean, timestamp'").executeSql();
            new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_user_attr.last_updated IS 'timestamp in micros since 1970 when this record was last updated'").executeSql();
          }

          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", created table grouper_prov_user_attr");
          }
        }

        if (!GrouperDdlUtils.assertIndexExists("grouper_prov_user_attr", "grouper_prov_userat_idx0")) {
          new GcDbAccess().sql("CREATE INDEX grouper_prov_userat_idx0 ON grouper_prov_user_attr (grouper_prov_user_internal_id)").executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index grouper_prov_userat_idx0");
          }
        }

        if (!GrouperDdlUtils.assertIndexExists("grouper_prov_user_attr", "grouper_prov_userat_idx1")) {
          new GcDbAccess().sql("CREATE INDEX grouper_prov_userat_idx1 ON grouper_prov_user_attr (grouper_prov_user_internal_id, attribute_name)").executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index grouper_prov_userat_idx1");
          }
        }

        if (!GrouperDdlUtils.assertForeignKeyExists("grouper_prov_user_attr", "grouper_prov_userat_fk1")) {
          new GcDbAccess().sql("ALTER TABLE grouper_prov_user_attr ADD CONSTRAINT grouper_prov_userat_fk1 FOREIGN KEY (grouper_prov_user_internal_id) REFERENCES grouper_prov_user(internal_id) on delete cascade").executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added foreign key grouper_prov_userat_fk1");
          }
        }

        if (!GrouperDdlUtils.assertTableThere(true, "grouper_prov_user_attr_value")) {
          if (GrouperDdlUtils.isOracle()) {
            new GcDbAccess().sql("""
                CREATE TABLE grouper_prov_user_attr_value (
                  internal_id NUMBER(38) NOT NULL,
                  prov_user_attr_internal_id NUMBER(38) NOT NULL,
                  prov_user_internal_id NUMBER(38) NOT NULL,
                  value_integer NUMBER(38),
                  value_dictionary_internal_id NUMBER(38),
                  last_updated NUMBER(38) NOT NULL,
                  PRIMARY KEY (internal_id)
                )
              """).executeSql();
          } else {
            new GcDbAccess().sql("""
                CREATE TABLE grouper_prov_user_attr_value (
                  internal_id BIGINT NOT NULL,
                  prov_user_attr_internal_id BIGINT NOT NULL,
                  prov_user_internal_id BIGINT NOT NULL,
                  value_integer BIGINT,
                  value_dictionary_internal_id BIGINT,
                  last_updated BIGINT NOT NULL,
                  PRIMARY KEY (internal_id)
                )
              """).executeSql();
          }

          if (GrouperDdlUtils.isPostgres() || GrouperDdlUtils.isOracle()) {
            new GcDbAccess().sql("COMMENT ON TABLE grouper_prov_user_attr_value IS 'Provisioner user attribute values'").executeSql();
            new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_user_attr_value.internal_id IS 'internal integer id for this table.  Do not refer to this outside of Grouper.  This will differ per env (dev/test/prod)'").executeSql();
            new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_user_attr_value.prov_user_attr_internal_id IS 'foreign key to grouper_prov_user_attr.internal_id'").executeSql();
            new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_user_attr_value.prov_user_internal_id IS 'foreign key to grouper_prov_user.internal_id'").executeSql();
            new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_user_attr_value.value_integer IS 'integer value used for int, boolean, or timestamp'").executeSql();
            new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_user_attr_value.value_dictionary_internal_id IS 'foreign key to grouper_dictionary.internal_id for string values'").executeSql();
            new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_user_attr_value.last_updated IS 'timestamp in micros since 1970 when this record was last updated'").executeSql();
          }

          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", created table grouper_prov_user_attr_value");
          }
        }

        if (!GrouperDdlUtils.assertIndexExists("grouper_prov_user_attr_value", "grouper_prov_useratv_idx0")) {
          new GcDbAccess().sql("CREATE INDEX grouper_prov_useratv_idx0 ON grouper_prov_user_attr_value (prov_user_attr_internal_id)").executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index grouper_prov_useratv_idx0");
          }
        }

        if (!GrouperDdlUtils.assertIndexExists("grouper_prov_user_attr_value", "grouper_prov_useratv_idx1")) {
          new GcDbAccess().sql("CREATE INDEX grouper_prov_useratv_idx1 ON grouper_prov_user_attr_value (prov_user_internal_id)").executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index grouper_prov_useratv_idx1");
          }
        }

        if (!GrouperDdlUtils.assertIndexExists("grouper_prov_user_attr_value", "grouper_prov_useratv_idx2")) {
          new GcDbAccess().sql("CREATE INDEX grouper_prov_useratv_idx2 ON grouper_prov_user_attr_value (value_dictionary_internal_id)").executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index grouper_prov_useratv_idx2");
          }
        }

        if (!GrouperDdlUtils.assertForeignKeyExists("grouper_prov_user_attr_value", "grouper_prov_useratv_fk1")) {
          new GcDbAccess().sql("ALTER TABLE grouper_prov_user_attr_value ADD CONSTRAINT grouper_prov_useratv_fk1 FOREIGN KEY (prov_user_attr_internal_id) REFERENCES grouper_prov_user_attr(internal_id) on delete cascade").executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added foreign key grouper_prov_useratv_fk1");
          }
        }

        if (!GrouperDdlUtils.assertForeignKeyExists("grouper_prov_user_attr_value", "grouper_prov_useratv_fk2")) {
          new GcDbAccess().sql("ALTER TABLE grouper_prov_user_attr_value ADD CONSTRAINT grouper_prov_useratv_fk2 FOREIGN KEY (prov_user_internal_id) REFERENCES grouper_prov_user(internal_id) on delete cascade").executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added foreign key grouper_prov_useratv_fk2");
          }
        }

        if (!GrouperDdlUtils.assertForeignKeyExists("grouper_prov_user_attr_value", "grouper_prov_useratv_fk3")) {
          new GcDbAccess().sql("ALTER TABLE grouper_prov_user_attr_value ADD CONSTRAINT grouper_prov_useratv_fk3 FOREIGN KEY (value_dictionary_internal_id) REFERENCES grouper_dictionary(internal_id)").executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added foreign key grouper_prov_useratv_fk3");
          }
        }

        if (!GrouperDdlUtils.assertTableThere(true, "grouper_prov_mship_role")) {
          if (GrouperDdlUtils.isOracle()) {
            new GcDbAccess().sql("""
                CREATE TABLE grouper_prov_mship_role (
                  internal_id NUMBER(38) NOT NULL,
                  role_name VARCHAR2(30) NOT NULL,
                  grouper_sync_internal_id NUMBER(38) NOT NULL,
                  last_updated NUMBER(38) NOT NULL,
                  PRIMARY KEY (internal_id)
                )
              """).executeSql();
          } else {
            new GcDbAccess().sql("""
                CREATE TABLE grouper_prov_mship_role (
                  internal_id BIGINT NOT NULL,
                  role_name VARCHAR(30) NOT NULL,
                  grouper_sync_internal_id BIGINT NOT NULL,
                  last_updated BIGINT NOT NULL,
                  PRIMARY KEY (internal_id)
                )
              """).executeSql();
          }

          if (GrouperDdlUtils.isPostgres() || GrouperDdlUtils.isOracle()) {
            new GcDbAccess().sql("COMMENT ON TABLE grouper_prov_mship_role IS 'Provisioner membership roles'").executeSql();
            new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_mship_role.internal_id IS 'internal integer id for this table.  Do not refer to this outside of Grouper.  This will differ per env (dev/test/prod)'").executeSql();
            new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_mship_role.role_name IS 'membership role name'").executeSql();
            new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_mship_role.grouper_sync_internal_id IS 'foreign key to grouper_sync.internal_id'").executeSql();
            new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_mship_role.last_updated IS 'timestamp in micros since 1970 when this record was last updated'").executeSql();
          }

          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", created table grouper_prov_mship_role");
          }
        }

        if (!GrouperDdlUtils.assertIndexExists("grouper_prov_mship_role", "grouper_prov_mshipr_idx0")) {
          new GcDbAccess().sql("CREATE INDEX grouper_prov_mshipr_idx0 ON grouper_prov_mship_role (grouper_sync_internal_id)").executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index grouper_prov_mshipr_idx0");
          }
        }

        if (!GrouperDdlUtils.assertIndexExists("grouper_prov_mship_role", "grouper_prov_mshipr_idx1")) {
          new GcDbAccess().sql("CREATE INDEX grouper_prov_mshipr_idx1 ON grouper_prov_mship_role (grouper_sync_internal_id, role_name)").executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index grouper_prov_mshipr_idx1");
          }
        }

        if (!GrouperDdlUtils.assertForeignKeyExists("grouper_prov_mship_role", "grouper_prov_mshipr_fk1")) {
          new GcDbAccess().sql("ALTER TABLE grouper_prov_mship_role ADD CONSTRAINT grouper_prov_mshipr_fk1 FOREIGN KEY (grouper_sync_internal_id) REFERENCES grouper_sync(internal_id)").executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added foreign key grouper_prov_mshipr_fk1");
          }
        }

        if (!GrouperDdlUtils.assertTableThere(true, "grouper_prov_mship")) {
          if (GrouperDdlUtils.isOracle()) {
            new GcDbAccess().sql("""
                CREATE TABLE grouper_prov_mship (
                  internal_id NUMBER(38) NOT NULL,
                  grouper_sync_internal_id NUMBER(38) NOT NULL,
                  prov_user_internal_id NUMBER(38) NOT NULL,
                  prov_group_internal_id NUMBER(38) NOT NULL,
                  prov_mship_role_internal_id NUMBER(38) NOT NULL,
                  last_updated NUMBER(38) NOT NULL,
                  PRIMARY KEY (internal_id)
                )
              """).executeSql();
          } else {
            new GcDbAccess().sql("""
                CREATE TABLE grouper_prov_mship (
                  internal_id BIGINT NOT NULL,
                  grouper_sync_internal_id BIGINT NOT NULL,
                  prov_user_internal_id BIGINT NOT NULL,
                  prov_group_internal_id BIGINT NOT NULL,
                  prov_mship_role_internal_id BIGINT NOT NULL,
                  last_updated BIGINT NOT NULL,
                  PRIMARY KEY (internal_id)
                )
              """).executeSql();
          }

          if (GrouperDdlUtils.isPostgres() || GrouperDdlUtils.isOracle()) {
            new GcDbAccess().sql("COMMENT ON TABLE grouper_prov_mship IS 'Provisioner memberships'").executeSql();
            new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_mship.internal_id IS 'internal integer id for this table.  Do not refer to this outside of Grouper.  This will differ per env (dev/test/prod)'").executeSql();
            new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_mship.grouper_sync_internal_id IS 'foreign key to grouper_sync.internal_id'").executeSql();
            new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_mship.prov_user_internal_id IS 'foreign key to grouper_prov_user.internal_id'").executeSql();
            new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_mship.prov_group_internal_id IS 'foreign key to grouper_prov_group.internal_id'").executeSql();
            new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_mship.prov_mship_role_internal_id IS 'foreign key to grouper_prov_mship_role.internal_id'").executeSql();
            new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_mship.last_updated IS 'timestamp in micros since 1970 when this record was last updated'").executeSql();
          }

          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", created table grouper_prov_mship");
          }
        }

        if (!GrouperDdlUtils.assertIndexExists("grouper_prov_mship", "grouper_prov_mship_idx0")) {
          new GcDbAccess().sql("CREATE INDEX grouper_prov_mship_idx0 ON grouper_prov_mship (grouper_sync_internal_id)").executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index grouper_prov_mship_idx0");
          }
        }

        if (!GrouperDdlUtils.assertIndexExists("grouper_prov_mship", "grouper_prov_mship_idx1")) {
          new GcDbAccess().sql("CREATE INDEX grouper_prov_mship_idx1 ON grouper_prov_mship (prov_user_internal_id)").executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index grouper_prov_mship_idx1");
          }
        }

        if (!GrouperDdlUtils.assertIndexExists("grouper_prov_mship", "grouper_prov_mship_idx2")) {
          new GcDbAccess().sql("CREATE INDEX grouper_prov_mship_idx2 ON grouper_prov_mship (prov_group_internal_id)").executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index grouper_prov_mship_idx2");
          }
        }

        if (!GrouperDdlUtils.assertForeignKeyExists("grouper_prov_mship", "grouper_prov_mship_fk1")) {
          new GcDbAccess().sql("ALTER TABLE grouper_prov_mship ADD CONSTRAINT grouper_prov_mship_fk1 FOREIGN KEY (grouper_sync_internal_id) REFERENCES grouper_sync(internal_id)").executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added foreign key grouper_prov_mship_fk1");
          }
        }

        if (!GrouperDdlUtils.assertForeignKeyExists("grouper_prov_mship", "grouper_prov_mship_fk2")) {
          new GcDbAccess().sql("ALTER TABLE grouper_prov_mship ADD CONSTRAINT grouper_prov_mship_fk2 FOREIGN KEY (prov_user_internal_id) REFERENCES grouper_prov_user(internal_id) on delete cascade").executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added foreign key grouper_prov_mship_fk2");
          }
        }

        if (!GrouperDdlUtils.assertForeignKeyExists("grouper_prov_mship", "grouper_prov_mship_fk3")) {
          new GcDbAccess().sql("ALTER TABLE grouper_prov_mship ADD CONSTRAINT grouper_prov_mship_fk3 FOREIGN KEY (prov_group_internal_id) REFERENCES grouper_prov_group(internal_id) on delete cascade").executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added foreign key grouper_prov_mship_fk3");
          }
        }

        new GcDbAccess().sql("""
            CREATE OR REPLACE VIEW grouper_prov_user_attr_v AS
            SELECT
                gs.provisioner_name,
                gm.subject_source             AS subject_source_id,
                gm.subject_id,
                gm.subject_identifier0,
                gm.subject_identifier1,
                pua.attribute_name,
                gd.the_text                   AS value_string,
                puav.value_integer,
                pua.attribute_type,
                gm.name                       AS member_name,
                gm.description                AS member_description,
                gsm.provisionable,
                gsm.in_target,
                gsm.in_target_insert_or_exists,
                gsm.provisionable_start,
                gsm.provisionable_end,
                gsm.in_target_start,
                gsm.in_target_end,
                gs.sync_engine,
                pu.target_user_id,
                gs.id                         AS grouper_sync_id,
                pu.last_updated               AS user_last_updated,
                puav.last_updated             AS value_last_updated,
                pu.internal_id                AS prov_user_internal_id,
                pu.grouper_sync_internal_id,
                gm.id                         AS member_id,
                gm.id_index                   AS member_id_index,
                pu.member_internal_id
            FROM            grouper_prov_user            pu
              LEFT JOIN     grouper_sync                 gs   ON gs.internal_id = pu.grouper_sync_internal_id
              LEFT JOIN     grouper_members              gm   ON gm.internal_id = pu.member_internal_id
              LEFT JOIN     grouper_sync_member          gsm  ON gsm.grouper_sync_id = gs.id AND gsm.member_id = gm.id
              LEFT JOIN     grouper_prov_user_attr       pua  ON pua.grouper_prov_user_internal_id = pu.internal_id
              LEFT JOIN     grouper_prov_user_attr_value puav ON puav.prov_user_attr_internal_id = pua.internal_id
              LEFT JOIN     grouper_dictionary           gd   ON gd.internal_id = puav.value_dictionary_internal_id
          """).executeSql();
        if (otherJobInput != null) {
          otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
          otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", created/replaced view grouper_prov_user_attr_v");
        }

        if (GrouperDdlUtils.isPostgres() || GrouperDdlUtils.isOracle()) {
          String viewObjectKeyword = GrouperDdlUtils.isPostgres() ? "VIEW" : "TABLE";
          new GcDbAccess().sql("COMMENT ON " + viewObjectKeyword + " grouper_prov_user_attr_v IS 'View of provisioner users joined with their attributes, grouper members, and grouper_sync_member provisioning state. Fans out to one row per user per attribute-value; users with zero attributes appear as one row with null attribute columns'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_user_attr_v.provisioner_name IS 'provisioner name from grouper_sync'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_user_attr_v.subject_source_id IS 'subject source id of the grouper member'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_user_attr_v.subject_id IS 'subject id of the grouper member'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_user_attr_v.subject_identifier0 IS 'first subject identifier of the grouper member'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_user_attr_v.subject_identifier1 IS 'second subject identifier of the grouper member'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_user_attr_v.attribute_name IS 'user attribute name e.g. user_name or user_email'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_user_attr_v.value_string IS 'string attribute value from the grouper dictionary'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_user_attr_v.value_integer IS 'integer attribute value used for int, boolean, or timestamp types'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_user_attr_v.attribute_type IS 'attribute type e.g. string, int, boolean, timestamp'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_user_attr_v.member_name IS 'subject name from grouper_members'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_user_attr_v.member_description IS 'subject description from grouper_members'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_user_attr_v.provisionable IS 'T/F if the grouper member is currently provisionable to this target'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_user_attr_v.in_target IS 'T/F if the grouper member currently exists in the target'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_user_attr_v.in_target_insert_or_exists IS 'T/F: T if grouper inserted the user into the target, F if it already existed'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_user_attr_v.provisionable_start IS 'timestamp when the grouper member became provisionable to this target'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_user_attr_v.provisionable_end IS 'timestamp when the grouper member stopped being provisionable to this target'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_user_attr_v.in_target_start IS 'timestamp when the grouper member first appeared in the target'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_user_attr_v.in_target_end IS 'timestamp when the grouper member was last removed from the target'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_user_attr_v.sync_engine IS 'provisioner engine from grouper_sync'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_user_attr_v.target_user_id IS 'target system user id'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_user_attr_v.grouper_sync_id IS 'grouper_sync uuid'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_user_attr_v.user_last_updated IS 'timestamp in micros since 1970 when the grouper_prov_user row was last updated'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_user_attr_v.value_last_updated IS 'timestamp in micros since 1970 when the grouper_prov_user_attr_value row was last updated'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_user_attr_v.prov_user_internal_id IS 'foreign key to grouper_prov_user.internal_id'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_user_attr_v.grouper_sync_internal_id IS 'foreign key to grouper_sync.internal_id'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_user_attr_v.member_id IS 'grouper_members.id uuid'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_user_attr_v.member_id_index IS 'grouper_members.id_index integer id'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_user_attr_v.member_internal_id IS 'foreign key to grouper_members.internal_id'").executeSql();
        }

        new GcDbAccess().sql("""
            CREATE OR REPLACE VIEW grouper_prov_group_attr_v AS
            SELECT
                gs.provisioner_name,
                gg.name                       AS group_name,
                pga.attribute_name,
                gd.the_text                   AS value_string,
                pgav.value_integer,
                pga.attribute_type,
                gsg.provisionable,
                gsg.in_target,
                gsg.in_target_insert_or_exists,
                gsg.provisionable_start,
                gsg.provisionable_end,
                gsg.in_target_start,
                gsg.in_target_end,
                gg.description                AS group_description,
                gg.extension                  AS group_extension,
                gg.display_extension          AS group_display_extension,
                gs.sync_engine,
                pg.target_group_id,
                gs.id                         AS grouper_sync_id,
                pg.last_updated               AS group_last_updated,
                pgav.last_updated             AS value_last_updated,
                pg.internal_id                AS prov_group_internal_id,
                pg.grouper_sync_internal_id,
                gg.id                         AS group_id,
                gg.id_index                   AS group_id_index,
                pg.group_internal_id
            FROM            grouper_prov_group            pg
              LEFT JOIN     grouper_sync                  gs   ON gs.internal_id = pg.grouper_sync_internal_id
              LEFT JOIN     grouper_groups                gg   ON gg.internal_id = pg.group_internal_id
              LEFT JOIN     grouper_sync_group            gsg  ON gsg.grouper_sync_id = gs.id AND gsg.group_id = gg.id
              LEFT JOIN     grouper_prov_group_attr       pga  ON pga.grouper_prov_group_internal_id = pg.internal_id
              LEFT JOIN     grouper_prov_group_attr_value pgav ON pgav.prov_group_attr_internal_id = pga.internal_id
              LEFT JOIN     grouper_dictionary            gd   ON gd.internal_id = pgav.value_dictionary_internal_id
          """).executeSql();
        if (otherJobInput != null) {
          otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
          otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", created/replaced view grouper_prov_group_attr_v");
        }

        if (GrouperDdlUtils.isPostgres() || GrouperDdlUtils.isOracle()) {
          String viewObjectKeyword = GrouperDdlUtils.isPostgres() ? "VIEW" : "TABLE";
          new GcDbAccess().sql("COMMENT ON " + viewObjectKeyword + " grouper_prov_group_attr_v IS 'View of provisioner groups joined with their attributes, grouper_groups, and grouper_sync_group provisioning state. Fans out to one row per group per attribute-value; groups with zero attributes appear as one row with null attribute columns'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_group_attr_v.provisioner_name IS 'provisioner name from grouper_sync'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_group_attr_v.group_name IS 'group system name from grouper_groups'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_group_attr_v.attribute_name IS 'group attribute name'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_group_attr_v.value_string IS 'string attribute value from the grouper dictionary'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_group_attr_v.value_integer IS 'integer attribute value used for int, boolean, or timestamp types'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_group_attr_v.attribute_type IS 'attribute type e.g. string, int, boolean, timestamp'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_group_attr_v.provisionable IS 'T/F if the group is currently provisionable to this target'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_group_attr_v.in_target IS 'T/F if the group currently exists in the target'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_group_attr_v.in_target_insert_or_exists IS 'T/F: T if grouper inserted the group into the target, F if it already existed'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_group_attr_v.provisionable_start IS 'timestamp when the group became provisionable to this target'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_group_attr_v.provisionable_end IS 'timestamp when the group stopped being provisionable to this target'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_group_attr_v.in_target_start IS 'timestamp when the group first appeared in the target'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_group_attr_v.in_target_end IS 'timestamp when the group was last removed from the target'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_group_attr_v.group_description IS 'group description from grouper_groups'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_group_attr_v.group_extension IS 'group extension (last segment of the group name) from grouper_groups'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_group_attr_v.group_display_extension IS 'group display extension (last segment of the display name) from grouper_groups'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_group_attr_v.sync_engine IS 'provisioner engine from grouper_sync'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_group_attr_v.target_group_id IS 'target system group id'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_group_attr_v.grouper_sync_id IS 'grouper_sync uuid'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_group_attr_v.group_last_updated IS 'timestamp in micros since 1970 when the grouper_prov_group row was last updated'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_group_attr_v.value_last_updated IS 'timestamp in micros since 1970 when the grouper_prov_group_attr_value row was last updated'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_group_attr_v.prov_group_internal_id IS 'foreign key to grouper_prov_group.internal_id'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_group_attr_v.grouper_sync_internal_id IS 'foreign key to grouper_sync.internal_id'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_group_attr_v.group_id IS 'grouper_groups.id uuid'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_group_attr_v.group_id_index IS 'grouper_groups.id_index integer id'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_group_attr_v.group_internal_id IS 'foreign key to grouper_groups.internal_id'").executeSql();
        }

        new GcDbAccess().sql("""
            CREATE OR REPLACE VIEW grouper_prov_mship_v AS
            SELECT
                gs.provisioner_name,
                gm.subject_source             AS subject_source_id,
                gm.subject_id,
                gm.subject_identifier0,
                gm.subject_identifier1,
                gm.name                       AS member_name,
                gg.name                       AS group_name,
                pmr.role_name,
                gsms.in_target,
                gsms.in_target_insert_or_exists,
                gsms.in_target_start,
                gsms.in_target_end,
                pm.last_updated               AS mship_last_updated,
                gs.sync_engine,
                pu.target_user_id,
                pg.target_group_id,
                gs.id                         AS grouper_sync_id,
                gsms.membership_id            AS grouper_sync_mship_id,
                pm.internal_id                AS prov_mship_internal_id,
                pm.prov_user_internal_id,
                pm.prov_group_internal_id,
                pm.grouper_sync_internal_id,
                gm.id                         AS member_id,
                gm.id_index                   AS member_id_index,
                pu.member_internal_id,
                gg.id                         AS group_id,
                gg.id_index                   AS group_id_index,
                pg.group_internal_id
            FROM            grouper_prov_mship            pm
              LEFT JOIN     grouper_prov_user             pu    ON pu.internal_id = pm.prov_user_internal_id
              LEFT JOIN     grouper_prov_group            pg    ON pg.internal_id = pm.prov_group_internal_id
              LEFT JOIN     grouper_prov_mship_role       pmr   ON pmr.internal_id = pm.prov_mship_role_internal_id
              LEFT JOIN     grouper_sync                  gs    ON gs.internal_id = pm.grouper_sync_internal_id
              LEFT JOIN     grouper_members               gm    ON gm.internal_id = pu.member_internal_id
              LEFT JOIN     grouper_groups                gg    ON gg.internal_id = pg.group_internal_id
              LEFT JOIN     grouper_sync_member           gsm_u ON gsm_u.grouper_sync_id = gs.id AND gsm_u.member_id = gm.id
              LEFT JOIN     grouper_sync_group            gsg   ON gsg.grouper_sync_id = gs.id AND gsg.group_id = gg.id
              LEFT JOIN     grouper_sync_membership       gsms  ON gsms.grouper_sync_id = gs.id AND gsms.grouper_sync_group_id = gsg.id AND gsms.grouper_sync_member_id = gsm_u.id
          """).executeSql();
        if (otherJobInput != null) {
          otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
          otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", created/replaced view grouper_prov_mship_v");
        }

        if (GrouperDdlUtils.isPostgres() || GrouperDdlUtils.isOracle()) {
          String viewObjectKeyword = GrouperDdlUtils.isPostgres() ? "VIEW" : "TABLE";
          new GcDbAccess().sql("COMMENT ON " + viewObjectKeyword + " grouper_prov_mship_v IS 'View of provisioner memberships joined with provisioner users/groups, grouper_members, grouper_groups, and grouper_sync_membership provisioning state'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_mship_v.provisioner_name IS 'provisioner name from grouper_sync'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_mship_v.subject_source_id IS 'subject source id of the grouper member'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_mship_v.subject_id IS 'subject id of the grouper member'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_mship_v.subject_identifier0 IS 'first subject identifier of the grouper member'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_mship_v.subject_identifier1 IS 'second subject identifier of the grouper member'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_mship_v.member_name IS 'subject name from grouper_members'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_mship_v.group_name IS 'group system name from grouper_groups'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_mship_v.role_name IS 'membership role name from grouper_prov_mship_role'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_mship_v.in_target IS 'T/F if the membership currently exists in the target'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_mship_v.in_target_insert_or_exists IS 'T/F: T if grouper inserted the membership into the target, F if it already existed'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_mship_v.in_target_start IS 'timestamp when the membership first appeared in the target'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_mship_v.in_target_end IS 'timestamp when the membership was last removed from the target'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_mship_v.mship_last_updated IS 'timestamp in micros since 1970 when the grouper_prov_mship row was last updated'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_mship_v.sync_engine IS 'provisioner engine from grouper_sync'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_mship_v.target_user_id IS 'target system user id'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_mship_v.target_group_id IS 'target system group id'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_mship_v.grouper_sync_id IS 'grouper_sync uuid'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_mship_v.grouper_sync_mship_id IS 'target system membership id from grouper_sync_membership.membership_id'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_mship_v.prov_mship_internal_id IS 'foreign key to grouper_prov_mship.internal_id'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_mship_v.prov_user_internal_id IS 'foreign key to grouper_prov_user.internal_id'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_mship_v.prov_group_internal_id IS 'foreign key to grouper_prov_group.internal_id'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_mship_v.grouper_sync_internal_id IS 'foreign key to grouper_sync.internal_id'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_mship_v.member_id IS 'grouper_members.id uuid'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_mship_v.member_id_index IS 'grouper_members.id_index integer id'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_mship_v.member_internal_id IS 'foreign key to grouper_members.internal_id'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_mship_v.group_id IS 'grouper_groups.id uuid'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_mship_v.group_id_index IS 'grouper_groups.id_index integer id'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_mship_v.group_internal_id IS 'foreign key to grouper_groups.internal_id'").executeSql();
        }

        return null;
      }
    });
  }

}
