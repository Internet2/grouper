package edu.internet2.middleware.grouper.app.upgradeTasks;

import edu.internet2.middleware.grouper.app.loader.GrouperDaemonDeleteOldRecords;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase.OtherJobInput;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

public class UpgradeTaskV15 implements UpgradeTasksInterface {
  
  
  @Override
  public boolean upgradeTaskIsDdl() {
    return true;
  }
  
  @Override
  public GrouperVersion versionIntroduced() {
    return GrouperVersion.valueOfIgnoreCase("5.8.1");
  }

  @Override
  public boolean doesUpgradeTaskHaveDdlWorkToDo() {
    
    boolean groupsNullable = GrouperDdlUtils.isColumnNullable("grouper_groups", "internal_id", "name", GrouperCheckConfig.attributeRootStemName() + ":upgradeTasks:upgradeTasksMetadataGroup");
    boolean fieldsNullable = GrouperDdlUtils.isColumnNullable("grouper_fields", "internal_id", "name", "admins");
    
    if (groupsNullable) {
      return true;
    }
    
    if (fieldsNullable) {
      return true;
    }
   
    if (GrouperDdlUtils.isOracle()) {
      if (!GrouperDdlUtils.doesConstraintExistOracle("grouper_fie_internal_id_unq")) {
        return true;
      }
      if (!GrouperDdlUtils.doesConstraintExistOracle("grouper_grp_internal_id_unq")) {
        return true;
      }
      if (!GrouperDdlUtils.doesConstraintExistOracle("grouper_sql_cache_group1_fk")) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void updateVersionFromPrevious(OtherJobInput otherJobInput) {
    boolean groupsNullable = GrouperDdlUtils.isColumnNullable("grouper_groups", "internal_id", "name", GrouperCheckConfig.attributeRootStemName() + ":upgradeTasks:upgradeTasksMetadataGroup");
    boolean fieldsNullable = GrouperDdlUtils.isColumnNullable("grouper_fields", "internal_id", "name", "admins");
    
    if (groupsNullable || fieldsNullable) {
      // ok nulls are allowed so make the change
      GrouperDaemonDeleteOldRecords.verifyTableIdIndexes(null);
    }
    
    if (groupsNullable) {
      String sql = null;
      
      if (GrouperDdlUtils.isOracle()) {
        sql = "ALTER TABLE grouper_groups MODIFY (internal_id NOT NULL)";
      } else if (GrouperDdlUtils.isMysql()) {
        sql = "ALTER TABLE grouper_groups MODIFY internal_id BIGINT NOT NULL";
      } else if (GrouperDdlUtils.isPostgres()) {
        sql = "ALTER TABLE grouper_groups ALTER COLUMN internal_id SET NOT NULL";
      } else {
        throw new RuntimeException("Which database are we????");
      }
      
      new GcDbAccess().sql(sql).executeSql();
    }
    
    if (fieldsNullable) {
      String sql = null;

      if (GrouperDdlUtils.isOracle()) {
        sql = "ALTER TABLE grouper_fields MODIFY (internal_id NOT NULL)";
      } else if (GrouperDdlUtils.isMysql()) {
        sql = "ALTER TABLE grouper_fields MODIFY internal_id BIGINT NOT NULL";
      } else if (GrouperDdlUtils.isPostgres()) {
        sql = "ALTER TABLE grouper_fields ALTER COLUMN internal_id SET NOT NULL";
      } else {
        throw new RuntimeException("Which database are we????");
      }
      
      new GcDbAccess().sql(sql).executeSql();
    }

    // cant add foreign key until this is there
    if (GrouperDdlUtils.isOracle()) {
      
      String sql = "ALTER TABLE grouper_fields ADD CONSTRAINT grouper_fie_internal_id_unq unique (internal_id)";
      
      if (!GrouperDdlUtils.doesConstraintExistOracle("grouper_fie_internal_id_unq")) {
        try {
          new GcDbAccess().sql(sql).executeSql();
        } catch (Exception e) {
          if (!GrouperUtil.getFullStackTrace(e).contains("ORA-02261")) {
            // throw if the exception is anything other than the constraint already exists
            throw e;
          }
        }
      }
      
      sql = "ALTER TABLE grouper_groups ADD CONSTRAINT grouper_grp_internal_id_unq unique (internal_id)";
      
      if (!GrouperDdlUtils.doesConstraintExistOracle("grouper_grp_internal_id_unq")) {
        try {
          new GcDbAccess().sql(sql).executeSql();
        } catch (Exception e) {
          if (!GrouperUtil.getFullStackTrace(e).contains("ORA-02261")) {
            // throw if the exception is anything other than the constraint already exists
            throw e;
          }
        }
      }

      sql = "ALTER TABLE grouper_sql_cache_group ADD CONSTRAINT grouper_sql_cache_group1_fk FOREIGN KEY (field_internal_id) REFERENCES grouper_fields(internal_id)";
      
      if (!GrouperDdlUtils.doesConstraintExistOracle("grouper_sql_cache_group1_fk")) {
        try {
          new GcDbAccess().sql(sql).executeSql();
        } catch (Exception e) {
          if (!GrouperUtil.getFullStackTrace(e).contains("ORA-02275")) {
            // throw if the exception is anything other than the constraint already exists
            throw e;
          }
        }
      }
    }
  }

}
