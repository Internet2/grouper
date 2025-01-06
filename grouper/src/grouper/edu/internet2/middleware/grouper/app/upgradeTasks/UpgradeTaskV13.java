package edu.internet2.middleware.grouper.app.upgradeTasks;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase.OtherJobInput;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

public class UpgradeTaskV13 implements UpgradeTasksInterface {
  
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(UpgradeTaskV13.class);
  
  @Override
  public boolean upgradeTaskIsDdl() {
    return true;
  }
  
  @Override
  public GrouperVersion versionIntroduced() {
    return GrouperVersion.valueOfIgnoreCase("4.15.0");
  }

  
  @Override
  public boolean doesUpgradeTaskHaveDdlWorkToDo() {
    return (boolean) GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          
        if (!GrouperDdlUtils.assertTableThere(true, "grouper_prov_azure_user")) {
          return true;
        }
                    
        if (!GrouperDdlUtils.assertIndexExists("grouper_prov_azure_user", "grouper_prov_azure_user_idx1")) {
          return true;
        }
        return false;
      }
    });
  }



  @Override
  public void updateVersionFromPrevious(OtherJobInput otherJobInput) {
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        
        try {
          
          if (!GrouperDdlUtils.assertTableThere(true, "grouper_prov_azure_user")) {
            
            if (GrouperDdlUtils.isPostgres()) {
              new GcDbAccess().sql("""
            CREATE TABLE grouper_prov_azure_user
            (
                config_id VARCHAR(50) NOT NULL,
                account_enabled VARCHAR(1) NULL,
                display_name VARCHAR(256) NULL,
                id VARCHAR(180) NOT NULL,
                mail_nickname VARCHAR(256) NULL,
                on_premises_immutable_id VARCHAR(256) NULL,
                user_principal_name VARCHAR(256) NULL,
                PRIMARY KEY (config_id, id)
            )                    
                  """).executeSql();
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
              }
            } else if (GrouperDdlUtils.isOracle()) {
              new GcDbAccess().sql("""
            CREATE TABLE grouper_prov_azure_user
            (
                config_id VARCHAR2(50) NOT NULL,
                account_enabled VARCHAR2(1) NULL,
                display_name VARCHAR2(256) NULL,
                id VARCHAR2(180) NOT NULL,
                mail_nickname VARCHAR2(256) NULL,
                on_premises_immutable_id VARCHAR2(256) NULL,
                user_principal_name VARCHAR2(256) NULL,
                PRIMARY KEY (config_id, id)
            )
                  """).executeSql();
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
              }

            } else if (GrouperDdlUtils.isMysql()) {
              new GcDbAccess().sql("""
                CREATE TABLE grouper_prov_azure_user
                (
                    config_id VARCHAR(50) NOT NULL,
                    account_enabled VARCHAR(1) NULL,
                    display_name VARCHAR(256) NULL,
                    id VARCHAR(180) NOT NULL,
                    mail_nickname VARCHAR(256) NULL,
                    on_premises_immutable_id VARCHAR(256) NULL,
                    user_principal_name VARCHAR(256) NULL,
                    PRIMARY KEY (config_id, id)
                )
                  """).executeSql();
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
              }
            }
            if (GrouperDdlUtils.isPostgres() || GrouperDdlUtils.isOracle()) {
              
              new GcDbAccess().sql("COMMENT ON TABLE grouper_prov_azure_user IS 'table to load azure users into a sql for reporting, provisioning, and deprovisioning'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_azure_user.config_id IS 'azure config id identifies which azure external system is being loaded'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_azure_user.id IS 'azure internal ID for this user (used in web services)'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_azure_user.account_enabled IS 'Is account enabled'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_azure_user.mail_nickname IS 'mail nickname for the user'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_azure_user.on_premises_immutable_id IS 'in premises immutable id for the user'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_azure_user.display_name IS 'display name for the user'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_azure_user.user_principal_name IS 'user principal name for the user'").executeSql();
        
            }
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            }
            
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added table grouper_prov_azure_user");
            }
          } else {
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", table grouper_prov_azure_user exists already");
            }
          }
                      
          if (!GrouperDdlUtils.assertIndexExists("grouper_prov_azure_user", "grouper_prov_azure_user_idx1")) {
            if (GrouperDdlUtils.isPostgres() || GrouperDdlUtils.isOracle()) {
              new GcDbAccess().sql("CREATE INDEX grouper_prov_azure_user_idx1 ON grouper_prov_azure_user (user_principal_name, config_id)").executeSql();
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
              }

            } else if (GrouperDdlUtils.isMysql() ) {
              new GcDbAccess().sql("CREATE INDEX grouper_prov_azure_user_idx1 ON grouper_prov_azure_user (user_principal_name(180), config_id)").executeSql();
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
              }
            }
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index grouper_prov_azure_user_idx1");
            }
          } else {
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", index grouper_prov_azure_user_idx1 exists already");
            }
          }
          
        } catch (Throwable t) {
          String message = "Could not perform upgrade task V20 adding tables/foreign keys/indexes for GRP-5625 load azure from provisioner to table!  "
              + "Skipping this upgrade task, install the tables/foreign keys/indexes manually";
          LOG.error(message, t);
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", " + message);
          }
          throw new RuntimeException(message, t);
        }
        return null;
      }
    });
  }

}
