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

public class UpgradeTaskV14 implements UpgradeTasksInterface {
  
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(UpgradeTaskV14.class);
  
  @Override
  public GrouperVersion versionIntroduced() {
    return GrouperVersion.valueOfIgnoreCase("4.15.7");
  }

  
  @Override
  public boolean doesUpgradeTaskHaveDdlWorkToDo() {
    return (boolean) GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          
        if (!GrouperDdlUtils.assertTableThere(true, "grouper_prov_adobe_user")) {
          return true;
        }
        
        if (!GrouperDdlUtils.assertTableThere(true, "grouper_prov_adobe_group")) {
          return true;
        }
                    
        if (!GrouperDdlUtils.assertIndexExists("grouper_prov_adobe_user", "grouper_prov_adobe_user_idx1")) {
          return true;
        }

        if (!GrouperDdlUtils.assertIndexExists("grouper_prov_adobe_user", "grouper_prov_adobe_user_idx2")) {
          return true;
        }
          
        if (!GrouperDdlUtils.assertIndexExists("grouper_prov_adobe_group", "grouper_prov_adobe_group_idx1")) {
          return true;
        }
        
        if (!GrouperDdlUtils.assertTableThere(true, "grouper_prov_adobe_membership")) {
          return true;
        }
          
        if (!GrouperDdlUtils.assertForeignKeyExists("grouper_prov_adobe_membership", "grouper_prov_adobe_mship_fk1")) { 
          return true;
        }
          
        if (!GrouperDdlUtils.assertForeignKeyExists("grouper_prov_adobe_membership", "grouper_prov_adobe_mship_fk2")) { 
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
          
          if (!GrouperDdlUtils.assertTableThere(true, "grouper_prov_adobe_user")) {
            
            if (GrouperDdlUtils.isPostgres()) {
              new GcDbAccess().sql("""
            CREATE TABLE grouper_prov_adobe_user
            (    
                config_id VARCHAR(50) NOT NULL,
                user_id VARCHAR(100) NOT NULL,
                email VARCHAR(256) NOT NULL,
                username VARCHAR(100),
                status VARCHAR(30) NULL,
                adobe_type VARCHAR(30) NULL,
                firstname VARCHAR(100) NULL,
                lastname VARCHAR(100) NULL,
                domain VARCHAR(100) NULL,
                country VARCHAR(2) NULL,
                PRIMARY KEY (config_id, user_id)
            )                    
                  """).executeSql();
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
              }
            } else if (GrouperDdlUtils.isOracle()) {
              new GcDbAccess().sql("""
            CREATE TABLE grouper_prov_adobe_user
            (
                config_id VARCHAR2(50) NOT NULL,
                user_id VARCHAR2(100) NOT NULL,
                email VARCHAR2(256) NOT NULL,
                username VARCHAR2(100),
                status VARCHAR2(30) NULL,
                adobe_type VARCHAR2(30) NULL,
                firstname VARCHAR2(100) NULL,
                lastname VARCHAR2(100) NULL,
                domain VARCHAR2(100) NULL,
                country VARCHAR2(2) NULL,
                PRIMARY KEY (config_id, user_id)
            )
                  """).executeSql();
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
              }

            } else if (GrouperDdlUtils.isMysql()) {
              new GcDbAccess().sql("""
                CREATE TABLE grouper_prov_adobe_user
              (
                  config_id VARCHAR(50) NOT NULL,
                  user_id VARCHAR(100) NOT NULL,
                  email VARCHAR(256) NOT NULL,
                  username VARCHAR(100),
                  status VARCHAR(30) NULL,
                  adobe_type VARCHAR(30) NULL,
                  firstname VARCHAR(100) NULL,
                  lastname VARCHAR(100) NULL,
                  domain VARCHAR(100) NULL,
                  country VARCHAR(2) NULL,
                  PRIMARY KEY (config_id, user_id)
              )
                  """).executeSql();
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
              }
            }
            if (GrouperDdlUtils.isPostgres() || GrouperDdlUtils.isOracle()) {
              
              new GcDbAccess().sql("COMMENT ON TABLE grouper_prov_adobe_user IS 'table to load adobe users into a sql for reporting, provisioning, and deprovisioning'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_adobe_user.config_id IS 'adobe config id identifies which adobe external system is being loaded'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_adobe_user.user_id IS 'adobe user id for this user (used in web services)'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_adobe_user.email IS 'email address'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_adobe_user.username IS 'username the user'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_adobe_user.status IS 'adobe status for the user'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_adobe_user.adobe_type IS 'type for the user'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_adobe_user.firstname IS 'first name for the user'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_adobe_user.lastname IS 'last name for the user'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_adobe_user.domain IS 'domain for the user'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_adobe_user.country IS 'country for the user'").executeSql();
        
            }
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            }
            
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added table grouper_prov_adobe_user");
            }
          } else {
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", table grouper_prov_adobe_user exists already");
            }
          }
          
          if (!GrouperDdlUtils.assertTableThere(true, "grouper_prov_adobe_group")) {
            
            if (GrouperDdlUtils.isPostgres()) {
              new GcDbAccess().sql("""
            CREATE TABLE grouper_prov_adobe_group
            (
                config_id VARCHAR(50) NOT NULL,
                group_id BIGINT NOT NULL,
                name VARCHAR(2000) NOT NULL,
                adobe_type VARCHAR(100) NULL,
                product_name VARCHAR(2000) NULL,
                member_count BIGINT NULL,
                license_quota BIGINT NULL,
                PRIMARY KEY (config_id, group_id)
            )
                  """).executeSql();
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
              }
            } else if (GrouperDdlUtils.isOracle()) {
              new GcDbAccess().sql("""
            CREATE TABLE grouper_prov_adobe_group
          (
              config_id VARCHAR2(50) NOT NULL,
              group_id NUMBER(38) NOT NULL,
              name VARCHAR2(2000) NOT NULL,
              adobe_type VARCHAR2(100) NULL,
              product_name VARCHAR2(2000) NULL,
              member_count NUMBER(38) NULL,
              license_quota NUMBER(38) NULL,
              PRIMARY KEY (config_id, group_id)
          )
                  """).executeSql();
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
              }

            } else if (GrouperDdlUtils.isMysql()) {
              new GcDbAccess().sql("""
                CREATE TABLE grouper_prov_adobe_group
              (
                  config_id VARCHAR(50) NOT NULL,
                  group_id BIGINT NOT NULL,
                  name VARCHAR(2000) NOT NULL,
                  adobe_type VARCHAR(100) NULL,
                  product_name VARCHAR(2000) NULL,
                  member_count BIGINT NULL,
                  license_quota BIGINT NULL,
                  PRIMARY KEY (config_id, group_id)
              )
                  """).executeSql();
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
              }
            }
            if (GrouperDdlUtils.isPostgres() || GrouperDdlUtils.isOracle()) {
              new GcDbAccess().sql("COMMENT ON TABLE grouper_prov_adobe_group IS 'table to load adobe groups into a sql for reporting, provisioning, and deprovisioning'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_adobe_group.config_id IS 'adobe config id identifies which adobe external system is being loaded'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_adobe_group.group_id IS 'adobe group id for this group (used in web services)'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_adobe_group.name IS 'group name'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_adobe_group.adobe_type IS 'type for the group'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_adobe_group.product_name IS 'product name for the group'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_adobe_group.member_count IS 'member count for the group'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_adobe_group.license_quota IS 'license quota for the group'").executeSql();
            }
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            }
            
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added table grouper_prov_adobe_group");
            }
          } else {
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", table grouper_prov_adobe_group exists already");
            }
          }
                      
          if (!GrouperDdlUtils.assertIndexExists("grouper_prov_adobe_user", "grouper_prov_adobe_user_idx1")) {
            if (GrouperDdlUtils.isPostgres() || GrouperDdlUtils.isOracle()) {
              new GcDbAccess().sql("CREATE INDEX grouper_prov_adobe_user_idx1 ON grouper_prov_adobe_user (email, config_id)").executeSql();
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
              }

            } else if (GrouperDdlUtils.isMysql() ) {
              new GcDbAccess().sql("CREATE INDEX grouper_prov_adobe_user_idx1 ON grouper_prov_adobe_user (email(100), config_id)").executeSql();
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
              }
            }
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index grouper_prov_adobe_user_idx1");
            }
          } else {
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", index grouper_prov_adobe_user_idx1 exists already");
            }
          }
          
          if (!GrouperDdlUtils.assertIndexExists("grouper_prov_adobe_user", "grouper_prov_adobe_user_idx2")) {
            if (GrouperDdlUtils.isPostgres() || GrouperDdlUtils.isOracle()) {
              new GcDbAccess().sql("CREATE INDEX grouper_prov_adobe_user_idx2 ON grouper_prov_adobe_user (username, config_id)").executeSql();
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
              }

            } else if (GrouperDdlUtils.isMysql() ) {
              new GcDbAccess().sql("CREATE INDEX grouper_prov_adobe_user_idx2 ON grouper_prov_adobe_user (username, config_id)").executeSql();
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
              }
            }
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index grouper_prov_adobe_user_idx2");
            }
          } else {
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", index grouper_prov_adobe_user_idx2 exists already");
            }
          }
          
          if (!GrouperDdlUtils.assertIndexExists("grouper_prov_adobe_group", "grouper_prov_adobe_group_idx1")) {
            if (GrouperDdlUtils.isPostgres() || GrouperDdlUtils.isOracle()) {
              new GcDbAccess().sql("CREATE INDEX grouper_prov_adobe_group_idx1 ON grouper_prov_adobe_group (name, config_id)").executeSql();
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
              }

            } else if (GrouperDdlUtils.isMysql() ) {
              new GcDbAccess().sql("CREATE INDEX grouper_prov_adobe_group_idx1 ON grouper_prov_adobe_group (name(100), config_id)").executeSql();
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
              }
            }
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index grouper_prov_adobe_group_idx1");
            }
          } else {
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", index grouper_prov_adobe_group_idx1 exists already");
            }
          }
          
          if (!GrouperDdlUtils.assertTableThere(true, "grouper_prov_adobe_membership")) {
            
            if (GrouperDdlUtils.isPostgres()) {
              new GcDbAccess().sql("""
            CREATE TABLE grouper_prov_adobe_membership
            (
                config_id VARCHAR(50) NOT NULL,
                group_id BIGINT NOT NULL,
                user_id VARCHAR(100) NOT NULL,
                PRIMARY KEY (config_id, group_id, user_id)
            )

                  """).executeSql();
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
              }
            } else if (GrouperDdlUtils.isOracle()) {
              new GcDbAccess().sql("""
            CREATE TABLE grouper_prov_adobe_membership
            (
                config_id VARCHAR2(50) NOT NULL,
                group_id NUMBER(38) NOT NULL,
                user_id VARCHAR2(100) NOT NULL,
                PRIMARY KEY (config_id, group_id, user_id)
            )
                  """).executeSql();
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
              }

            } else if (GrouperDdlUtils.isMysql()) {
              new GcDbAccess().sql("""
                CREATE TABLE grouper_prov_adobe_membership
                (
                    config_id VARCHAR(50) NOT NULL,
                    group_id BIGINT NOT NULL,
                    user_id VARCHAR(100) NOT NULL,
                    PRIMARY KEY (config_id, group_id, user_id)
                )
                  """).executeSql();
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
              }
            }
            if (GrouperDdlUtils.isPostgres() || GrouperDdlUtils.isOracle()) {
              new GcDbAccess().sql("COMMENT ON TABLE grouper_prov_adobe_membership IS 'table to load adobe memberships into a sql for reporting, provisioning, and deprovisioning'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_adobe_membership.config_id IS 'adobe config id identifies which adobe external system is being loaded'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_adobe_membership.group_id IS 'adobe group id for this membership'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_prov_adobe_membership.user_id IS 'adobe user id for this membership'").executeSql();
            }
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            }
            
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added table grouper_prov_adobe_membership");
            }
          } else {
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", table grouper_prov_adobe_membership exists already");
            }
          }
          
          if (!GrouperDdlUtils.assertForeignKeyExists("grouper_prov_adobe_membership", "grouper_prov_adobe_mship_fk1")) { 
            
            if (GrouperDdlUtils.isPostgres() || GrouperDdlUtils.isOracle()) {
              new GcDbAccess().sql("ALTER TABLE  grouper_prov_adobe_membership ADD CONSTRAINT grouper_prov_adobe_mship_fk1 FOREIGN KEY (config_id, group_id) REFERENCES grouper_prov_adobe_group(config_id, group_id) on delete cascade").executeSql();
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
              }

            } else if (GrouperDdlUtils.isMysql() ) {
              new GcDbAccess().sql("ALTER TABLE  grouper_prov_adobe_membership ADD CONSTRAINT grouper_prov_adobe_mship_fk1 FOREIGN KEY (config_id, group_id) REFERENCES grouper_prov_adobe_group(config_id, group_id) on delete cascade").executeSql();
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
              }
            }
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added foreign key grouper_prov_adobe_mship_fk1");
            }
            
          } else {
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", foreign key grouper_prov_adobe_mship_fk1 exists already");
            }
          }
          
          if (!GrouperDdlUtils.assertForeignKeyExists("grouper_prov_adobe_membership", "grouper_prov_adobe_mship_fk2")) { 
            
            if (GrouperDdlUtils.isPostgres() || GrouperDdlUtils.isOracle()) {
              new GcDbAccess().sql("ALTER TABLE  grouper_prov_adobe_membership ADD CONSTRAINT grouper_prov_adobe_mship_fk2 FOREIGN KEY (config_id, user_id) REFERENCES grouper_prov_adobe_user(config_id, user_id) on delete cascade").executeSql();
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
              }

            } else if (GrouperDdlUtils.isMysql() ) {
              new GcDbAccess().sql("ALTER TABLE  grouper_prov_adobe_membership ADD CONSTRAINT grouper_prov_adobe_mship_fk2 FOREIGN KEY (config_id, user_id) REFERENCES grouper_prov_adobe_user(config_id, user_id) on delete cascade").executeSql();
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
              }
            }
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added foreign grouper_prov_adobe_mship_fk2");
            }
            
          } else {
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", foreign key grouper_prov_adobe_mship_fk2 exists already");
            }
          }

        } catch (Throwable t) {
          String message = "Could not perform upgrade task V14 adding tables/foreign keys/indexes for GRP-5794 adobe provisioner!  "
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
  
  @Override
  public boolean upgradeTaskIsDdl() {
    return true;
  }
  


}
