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

public class UpgradeTaskV12 implements UpgradeTasksInterface {
  
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(UpgradeTaskV12.class);
  
  @Override
  public boolean upgradeTaskIsDdl() {
    return true;
  }

  @Override
  public GrouperVersion versionIntroduced() {
    return GrouperVersion.valueOfIgnoreCase("4.12.0");
  }

  @Override
  public boolean doesUpgradeTaskHaveDdlWorkToDo() {
    return (boolean) GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        
          
        if (!GrouperDdlUtils.assertTableThere(true, "grouper_prov_scim_user")) {
          return true;
        }
        
        if (!GrouperDdlUtils.assertIndexExists("grouper_prov_scim_user", "grouper_prov_scim_user_idx1")) {
          return true;
        }
        
        if (!GrouperDdlUtils.assertIndexExists("grouper_prov_scim_user", "grouper_prov_scim_user_idx2")) {
          return true;
        }
        
        if (!GrouperDdlUtils.assertTableThere(true, "grouper_prov_scim_user_attr")) {
          return true;
        }
        
        if (!GrouperDdlUtils.assertIndexExists("grouper_prov_scim_user_attr", "grouper_prov_scim_usat_idx1")) {
          return true;
        }
          
        if (!GrouperDdlUtils.assertIndexExists("grouper_prov_scim_user_attr", "grouper_prov_scim_usat_idx2")) {
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
          
          if (!GrouperDdlUtils.assertTableThere(true, "grouper_prov_scim_user")) {
            if (GrouperDdlUtils.isPostgres()) {
              new GcDbAccess().sql("CREATE TABLE grouper_prov_scim_user ( config_id VARCHAR(50) NOT NULL, active VARCHAR(1), cost_center VARCHAR(256), department VARCHAR(256), display_name VARCHAR(256), division VARCHAR(256), email_type VARCHAR(256), email_value VARCHAR(256), email_type2 VARCHAR(256), email_value2 VARCHAR(256), employee_number VARCHAR(256), external_id VARCHAR(256), family_name VARCHAR(256), formatted_name VARCHAR(256), given_name VARCHAR(256), id VARCHAR(180) NOT NULL, middle_name VARCHAR(256), phone_number VARCHAR(256), phone_number_type VARCHAR(256), phone_number2 VARCHAR(256), phone_number_type2 VARCHAR(256), the_schemas VARCHAR(256), title VARCHAR(256), user_name VARCHAR(256), user_type VARCHAR(256), PRIMARY KEY (config_id, id) )").executeSql();
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
              }
            } else if (GrouperDdlUtils.isOracle()) {
              new GcDbAccess().sql("CREATE TABLE grouper_prov_scim_user ( config_id VARCHAR2(50) NOT NULL, active VARCHAR2(1), cost_center VARCHAR2(256), department VARCHAR2(256), display_name VARCHAR2(256), division VARCHAR2(256), email_type VARCHAR2(256), email_value VARCHAR2(256), email_type2 VARCHAR2(256), email_value2 VARCHAR2(256), employee_number VARCHAR2(256), external_id VARCHAR2(256), family_name VARCHAR2(256), formatted_name VARCHAR2(256), given_name VARCHAR2(256), id VARCHAR2(180) NOT NULL, middle_name VARCHAR2(256), phone_number VARCHAR2(256), phone_number_type VARCHAR2(256), phone_number2 VARCHAR2(256), phone_number_type2 VARCHAR2(256), the_schemas VARCHAR2(256), title VARCHAR2(256), user_name VARCHAR2(256), user_type VARCHAR2(256), PRIMARY KEY (config_id, id) )").executeSql();
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
              }
            } else if (GrouperDdlUtils.isMysql()) {
              new GcDbAccess().sql("CREATE TABLE grouper_prov_scim_user ( config_id VARCHAR(50) NOT NULL, active VARCHAR(1) NULL, cost_center VARCHAR(256) NULL, department VARCHAR(256) NULL, display_name VARCHAR(256) NULL, division VARCHAR(256) NULL, email_type VARCHAR(256) NULL, email_value VARCHAR(256) NULL, email_type2 VARCHAR(256) NULL, email_value2 VARCHAR(256) NULL, employee_number VARCHAR(256) NULL, external_id VARCHAR(256) NULL, family_name VARCHAR(256) NULL, formatted_name VARCHAR(256) NULL, given_name VARCHAR(256) NULL, id VARCHAR(180) NOT NULL, middle_name VARCHAR(256) NULL, phone_number VARCHAR(256) NULL, phone_number_type VARCHAR(256) NULL, phone_number2 VARCHAR(256) NULL, phone_number_type2 VARCHAR(256) NULL, the_schemas VARCHAR(256) NULL, title VARCHAR(256) NULL, user_name VARCHAR(256) NULL, user_type VARCHAR(256) NULL, PRIMARY KEY (config_id, id) )").executeSql();
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
              }
            }
            if (GrouperDdlUtils.isPostgres() || GrouperDdlUtils.isOracle()) {
              
              new GcDbAccess().sql("COMMENT ON TABLE grouper_prov_scim_user IS 'table to load scim users into a sql for reporting, provisioning, and deprovisioning'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_scim_user.config_id IS 'scim config id identifies which scim external system is being loaded'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_scim_user.id IS 'scim internal ID for this user (used in web services)'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_scim_user.active IS 'Is user active'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_scim_user.cost_center IS 'cost center for the user'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_scim_user.department IS 'department for the user'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_scim_user.display_name IS 'display name for the user'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_scim_user.division IS 'divsion for the user'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_scim_user.email_type IS 'email type for the user'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_scim_user.email_value IS 'email value for the user'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_scim_user.email_type2 IS 'email type2 for the user'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_scim_user.email_value2 IS 'email value2 for the user'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_scim_user.employee_number IS 'employee number for the user'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_scim_user.external_id IS 'external id for the user'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_scim_user.family_name IS 'family name for the user'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_scim_user.formatted_name IS 'formatted name for the user'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_scim_user.given_name IS 'given name for the user'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_scim_user.middle_name IS 'middle name for the user'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_scim_user.phone_number IS 'phone number for the user'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_scim_user.phone_number_type IS 'phone number type for the user'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_scim_user.phone_number2 IS 'phone number2 for the user'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_scim_user.phone_number_type2 IS 'phone number type2 for the user'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_scim_user.the_schemas IS 'schemas for the user'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_scim_user.title IS 'title for the user'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_scim_user.user_name IS 'user name for the user'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_scim_user.user_type IS 'user type for the user'").executeSql();
        
            }
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            }
            
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added table grouper_prov_scim_user");
            }
          } else {
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", table grouper_prov_scim_user exists already");
            }
          }
          
          if (!GrouperDdlUtils.assertIndexExists("grouper_prov_scim_user", "grouper_prov_scim_user_idx1")) {
            new GcDbAccess().sql("CREATE INDEX grouper_prov_scim_user_idx1 ON grouper_prov_scim_user (email_value, config_id)").executeSql();
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            }
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index grouper_prov_scim_user_idx1");
            }
          } else {
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", index grouper_prov_scim_user_idx1 exists already");
            }
          }
          
          if (!GrouperDdlUtils.assertIndexExists("grouper_prov_scim_user", "grouper_prov_scim_user_idx2")) {
            new GcDbAccess().sql("CREATE INDEX grouper_prov_scim_user_idx2 ON grouper_prov_scim_user (user_name, config_id)").executeSql();
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            }
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index grouper_prov_scim_user_idx2");
            }
          } else {
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", index grouper_prov_scim_user_idx2 exists already");
            }
          }
          
          
          if (!GrouperDdlUtils.assertTableThere(true, "grouper_prov_scim_user_attr")) {
            if (GrouperDdlUtils.isPostgres()) {
              new GcDbAccess().sql("CREATE TABLE grouper_prov_scim_user_attr ( config_id VARCHAR(50) NOT NULL, id VARCHAR(256) NOT NULL, attribute_name VARCHAR(256) NULL, attribute_value VARCHAR(4000) NULL, PRIMARY KEY (config_id, id, attribute_name, attribute_value) )").executeSql();
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
              }
            } else if (GrouperDdlUtils.isOracle()) {
              new GcDbAccess().sql("CREATE TABLE grouper_prov_scim_user_attr ( config_id VARCHAR2(50) NOT NULL, id VARCHAR2(256) NOT NULL, attribute_name VARCHAR2(256) NULL, attribute_value VARCHAR2(4000) NULL, PRIMARY KEY (config_id, id, attribute_name, attribute_value) )").executeSql();
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
              }
            } else if (GrouperDdlUtils.isMysql()) {
              new GcDbAccess().sql("CREATE TABLE grouper_prov_scim_user_attr ( config_id VARCHAR(50) NOT NULL, id VARCHAR(256) NOT NULL, attribute_name VARCHAR(256) NULL, attribute_value VARCHAR(4000) NULL )").executeSql();
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
              }
            }
            
            new GcDbAccess().sql("ALTER TABLE grouper_prov_scim_user_attr ADD CONSTRAINT grouper_prov_scim_usat_fk FOREIGN KEY (config_id, id) REFERENCES grouper_prov_scim_user(config_id, id) on delete cascade").executeSql();
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            }
            
            if (GrouperDdlUtils.isPostgres() || GrouperDdlUtils.isOracle()) {
              
              new GcDbAccess().sql("COMMENT ON TABLE grouper_prov_scim_user_attr IS 'table to load scim user attributes into a sql for reporting, provisioning, and deprovisioning'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_scim_user_attr.config_id IS 'scim config id identifies which scim external system is being loaded'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_scim_user_attr.id IS 'scim internal ID for this user (used in web services)'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_scim_user_attr.attribute_name IS 'scim user attribute name'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_scim_user_attr.attribute_value IS 'scim user attribute value'").executeSql();
        
            }
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            }
            
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added table grouper_prov_scim_user_attr");
            }
          } else {
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", table grouper_prov_scim_user_attr exists already");
            }
          }
          
          if (!GrouperDdlUtils.assertIndexExists("grouper_prov_scim_user_attr", "grouper_prov_scim_usat_idx1")) {
            if (GrouperDdlUtils.isMysql()) {
              new GcDbAccess().sql("CREATE INDEX grouper_prov_scim_usat_idx1 ON grouper_prov_scim_user_attr (id(100), config_id, attribute_name(100))").executeSql();
            } else {
              new GcDbAccess().sql("CREATE INDEX grouper_prov_scim_usat_idx1 ON grouper_prov_scim_user_attr (id, config_id, attribute_name)").executeSql();
            }
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            }
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index grouper_prov_scim_user_idx1");
            }
          } else {
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", index grouper_prov_scim_user_idx1 exists already");
            }
          }
          
          if (!GrouperDdlUtils.assertIndexExists("grouper_prov_scim_user_attr", "grouper_prov_scim_usat_idx2")) {
            if (GrouperDdlUtils.isMysql()) {
              new GcDbAccess().sql("CREATE INDEX grouper_prov_scim_usat_idx2 ON grouper_prov_scim_user_attr (id(100), config_id, attribute_value(100))").executeSql();
            } else {
              new GcDbAccess().sql("CREATE INDEX grouper_prov_scim_usat_idx2 ON grouper_prov_scim_user_attr (id, config_id, attribute_value)").executeSql();
            }
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            }
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index grouper_prov_scim_usat_idx2");
            }
          } else {
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", index grouper_prov_scim_usat_idx2 exists already");
            }
          }              
          
        } catch (Throwable t) {
          String message = "Could not perform upgrade task V12 adding tables/foreign keys/indexes for GRP-5514 scim loading!  "
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
