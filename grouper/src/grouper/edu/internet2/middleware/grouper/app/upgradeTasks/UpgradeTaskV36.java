package edu.internet2.middleware.grouper.app.upgradeTasks;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase.OtherJobInput;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

/**
 * grouper_prov_duo_user primary key should be (user_id, config_id), not just user_name
 */
public class UpgradeTaskV36 implements UpgradeTasksInterface {
  
  @Override
  public boolean doesUpgradeTaskHaveDdlWorkToDo() {
    
    return (boolean) GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        
        if (!GrouperDdlUtils.assertPrimaryKeyExists("grouper_prov_duo_user", GrouperUtil.toSet("user_id", "config_id"))) {
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
    return GrouperVersion.valueOfIgnoreCase("4.20.4");
  }

  @Override
  public void updateVersionFromPrevious(OtherJobInput otherJobInput) {
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        
        // check if postgres
        if (GrouperDdlUtils.isPostgres()) {
          
          // drop index grouper_duo_user_id_idx
          new GcDbAccess().sql("DROP INDEX IF EXISTS grouper_duo_user_id_idx").executeSql();
          
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", dropped index: grouper_duo_user_id_idx");
          }

          // drop the primary key
          new GcDbAccess().sql("ALTER TABLE grouper_prov_duo_user DROP CONSTRAINT IF EXISTS grouper_prov_duo_user_pkey").executeSql();

          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", dropped primary key on table: grouper_prov_duo_user");
          }

          // add the new primary key
          new GcDbAccess().sql("ALTER TABLE grouper_prov_duo_user ADD CONSTRAINT grouper_prov_duo_user_pkey PRIMARY KEY (user_id, config_id)").executeSql();
          
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added primary key on table: grouper_prov_duo_user (user_id, config_id)");
          }

          // check if oracle
        } else if (GrouperDdlUtils.isOracle()) {
          
          // drop index grouper_duo_user_id_idx
          new GcDbAccess().sql("DROP INDEX grouper_duo_user_id_idx").executeSql();

          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", dropped index: grouper_duo_user_id_idx");
          }

          // drop the primary key
          new GcDbAccess().sql("ALTER TABLE grouper_prov_duo_user DROP PRIMARY KEY").executeSql();

          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", dropped primary key on table: grouper_prov_duo_user");
          }

          // add the new primary key
          new GcDbAccess().sql("ALTER TABLE grouper_prov_duo_user ADD CONSTRAINT grouper_prov_duo_user_pkey PRIMARY KEY (user_id, config_id)").executeSql();

          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added primary key on table: grouper_prov_duo_user (user_id, config_id)");
          }

          // check if mysql
        } else if (GrouperDdlUtils.isMysql()) {
          
          // drop index grouper_duo_user_id_idx
          new GcDbAccess().sql("DROP INDEX grouper_duo_user_id_idx ON grouper_prov_duo_user").executeSql();

          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", dropped index: grouper_duo_user_id_idx");
          }

          // drop the primary key
          new GcDbAccess().sql("ALTER TABLE grouper_prov_duo_user DROP PRIMARY KEY").executeSql();

          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", dropped primary key on table: grouper_prov_duo_user");
          }

          // add the new primary key
          new GcDbAccess().sql("ALTER TABLE grouper_prov_duo_user ADD PRIMARY KEY (user_id, config_id)").executeSql();
          
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added primary key on table: grouper_prov_duo_user (user_id, config_id)");
          }

        } else {
          throw new RuntimeException("Not expecting database type: " + GrouperDdlUtils.databaseType());
        }
        
        return null;
      }
    });
  }

}
