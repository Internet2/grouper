package edu.internet2.middleware.grouper.app.upgradeTasks;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase.OtherJobInput;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

public class UpgradeTaskV39 implements UpgradeTasksInterface {

  @Override
  public boolean doesUpgradeTaskHaveDdlWorkToDo() {
    return (boolean) GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {

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
          new GcDbAccess().sql("ALTER TABLE grouper_prov_group_attr ADD CONSTRAINT grouper_prov_grpat_fk1 FOREIGN KEY (grouper_prov_group_internal_id) REFERENCES grouper_prov_group(internal_id)").executeSql();
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
          new GcDbAccess().sql("ALTER TABLE grouper_prov_group_attr_value ADD CONSTRAINT grouper_prov_grpatv_fk1 FOREIGN KEY (prov_group_attr_internal_id) REFERENCES grouper_prov_group_attr(internal_id)").executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added foreign key grouper_prov_grpatv_fk1");
          }
        }

        if (!GrouperDdlUtils.assertForeignKeyExists("grouper_prov_group_attr_value", "grouper_prov_grpatv_fk2")) {
          new GcDbAccess().sql("ALTER TABLE grouper_prov_group_attr_value ADD CONSTRAINT grouper_prov_grpatv_fk2 FOREIGN KEY (prov_group_internal_id) REFERENCES grouper_prov_group(internal_id)").executeSql();
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
          new GcDbAccess().sql("ALTER TABLE grouper_prov_user_attr ADD CONSTRAINT grouper_prov_userat_fk1 FOREIGN KEY (grouper_prov_user_internal_id) REFERENCES grouper_prov_user(internal_id)").executeSql();
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
          new GcDbAccess().sql("ALTER TABLE grouper_prov_user_attr_value ADD CONSTRAINT grouper_prov_useratv_fk1 FOREIGN KEY (prov_user_attr_internal_id) REFERENCES grouper_prov_user_attr(internal_id)").executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added foreign key grouper_prov_useratv_fk1");
          }
        }

        if (!GrouperDdlUtils.assertForeignKeyExists("grouper_prov_user_attr_value", "grouper_prov_useratv_fk2")) {
          new GcDbAccess().sql("ALTER TABLE grouper_prov_user_attr_value ADD CONSTRAINT grouper_prov_useratv_fk2 FOREIGN KEY (prov_user_internal_id) REFERENCES grouper_prov_user(internal_id)").executeSql();
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
                  prov_mship_role_internal_id NUMBER(38),
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
                  prov_mship_role_internal_id BIGINT,
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
          new GcDbAccess().sql("ALTER TABLE grouper_prov_mship ADD CONSTRAINT grouper_prov_mship_fk2 FOREIGN KEY (prov_user_internal_id) REFERENCES grouper_prov_user(internal_id)").executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added foreign key grouper_prov_mship_fk2");
          }
        }

        if (!GrouperDdlUtils.assertForeignKeyExists("grouper_prov_mship", "grouper_prov_mship_fk3")) {
          new GcDbAccess().sql("ALTER TABLE grouper_prov_mship ADD CONSTRAINT grouper_prov_mship_fk3 FOREIGN KEY (prov_group_internal_id) REFERENCES grouper_prov_group(internal_id)").executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added foreign key grouper_prov_mship_fk3");
          }
        }

        return null;
      }
    });
  }

}
