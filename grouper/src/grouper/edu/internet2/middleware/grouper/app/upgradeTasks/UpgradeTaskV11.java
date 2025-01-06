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

public class UpgradeTaskV11 implements UpgradeTasksInterface {
  
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(UpgradeTaskV11.class);
  
  @Override
  public boolean upgradeTaskIsDdl() {
    return true;
  }
  
  @Override
  public boolean doesUpgradeTaskHaveDdlWorkToDo() {
    
    return (boolean )GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        
        if (!GrouperDdlUtils.assertTableThere(true, "grouper_sync_dep_group_user")) {
          return true;
        }

        if (!GrouperDdlUtils.assertIndexExists("grouper_sync_dep_group_user", "grouper_sync_dep_grp_user_idx0")) {
          return true;
        }
        if (!GrouperDdlUtils.assertIndexExists("grouper_sync_dep_group_user", "grouper_sync_dep_grp_user_idx1")) {
          return true;
        }

        if (!GrouperDdlUtils.assertTableThere(true, "grouper_sync_dep_group_group")) {
          return true;
        }
          
        if (!GrouperDdlUtils.assertIndexExists("grouper_sync_dep_group_group", "grouper_sync_dep_grp_grp_idx0")) {
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
          
          if (!GrouperDdlUtils.assertTableThere(true, "grouper_sync_dep_group_user")) {
            if (GrouperDdlUtils.isPostgres()) {
              new GcDbAccess().sql("CREATE TABLE grouper_sync_dep_group_user (id_index BIGINT NOT NULL, grouper_sync_id varchar(40) NOT NULL, group_id varchar(40) NOT NULL, field_id varchar(40) NOT NULL, PRIMARY KEY (id_index) )").executeSql();
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
              }
            } else if (GrouperDdlUtils.isOracle()) {
              new GcDbAccess().sql("CREATE TABLE grouper_sync_dep_group_user ( id_index NUMBER(38) NOT NULL, grouper_sync_id VARCHAR2(40) NOT NULL, group_id VARCHAR2(40) NOT NULL, field_id VARCHAR2(40) NOT NULL, PRIMARY KEY (id_index) )").executeSql();
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
              }
            } else if (GrouperDdlUtils.isMysql()) {
              new GcDbAccess().sql("CREATE TABLE grouper_sync_dep_group_user ( id_index BIGINT NOT NULL, grouper_sync_id VARCHAR(40) NOT NULL, group_id VARCHAR(40) NOT NULL, field_id VARCHAR(40) NOT NULL, PRIMARY KEY (id_index))").executeSql();
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
              }
            }
            if (GrouperDdlUtils.isPostgres() || GrouperDdlUtils.isOracle()) {
              new GcDbAccess().sql("COMMENT ON TABLE grouper_sync_dep_group_user IS 'Groups are listed that are used in user translations.  Users will need to be recalced if there are changes (not membership recalc)'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_sync_dep_group_user.id_index IS 'primary key'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_sync_dep_group_user.grouper_sync_id IS 'provisioner'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_sync_dep_group_user.group_id IS 'group uuid'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_sync_dep_group_user.field_id IS 'field uuid'").executeSql();

            }
            new GcDbAccess().sql("alter table grouper_sync_dep_group_user add CONSTRAINT grouper_sync_dep_grp_user_fk_2 FOREIGN KEY (grouper_sync_id) REFERENCES grouper_sync(id)").executeSql();
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            }
            
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added table grouper_sync_dep_group_user");
            }
          } else {
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", table grouper_sync_dep_group_user exists already");
            }
          }

          if (!GrouperDdlUtils.assertIndexExists("grouper_sync_dep_group_user", "grouper_sync_dep_grp_user_idx0")) {
            new GcDbAccess().sql("CREATE INDEX grouper_sync_dep_grp_user_idx0 ON grouper_sync_dep_group_user (grouper_sync_id)").executeSql();
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            }
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index grouper_sync_dep_grp_user_idx0");
            }
          } else {
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", index grouper_sync_dep_grp_user_idx0 exists already");
            }
          }
          if (!GrouperDdlUtils.assertIndexExists("grouper_sync_dep_group_user", "grouper_sync_dep_grp_user_idx1")) {
            new GcDbAccess().sql("CREATE UNIQUE INDEX grouper_sync_dep_grp_user_idx1 ON grouper_sync_dep_group_user (grouper_sync_id,group_id,field_id)").executeSql();
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            }
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index grouper_sync_dep_grp_user_idx1");
            }
          } else {
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", index grouper_sync_dep_grp_user_idx1 exists already");
            }
          }

          if (!GrouperDdlUtils.assertTableThere(true, "grouper_sync_dep_group_group")) {
            if (GrouperDdlUtils.isPostgres()) {
              new GcDbAccess().sql("CREATE TABLE grouper_sync_dep_group_group (id_index BIGINT NOT NULL, grouper_sync_id varchar(40) NOT NULL, group_id varchar(40) NOT NULL, field_id varchar(40) NOT NULL, provisionable_group_id varchar(40) NOT NULL, PRIMARY KEY (id_index) )").executeSql();
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
              }
            } else if (GrouperDdlUtils.isOracle()) {
              new GcDbAccess().sql("CREATE TABLE grouper_sync_dep_group_group ( id_index NUMBER(38) NOT NULL,  grouper_sync_id VARCHAR2(40) NOT NULL, group_id VARCHAR2(40) NOT NULL, field_id VARCHAR2(40) NOT NULL, provisionable_group_id VARCHAR2(40) NOT NULL, PRIMARY KEY (id_index) )").executeSql();
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
              }
            } else if (GrouperDdlUtils.isMysql()) {
              new GcDbAccess().sql("CREATE TABLE grouper_sync_dep_group_group ( id_index BIGINT NOT NULL, grouper_sync_id VARCHAR(40) NOT NULL, group_id VARCHAR(40) NOT NULL, field_id VARCHAR(40) NOT NULL, provisionable_group_id VARCHAR(40) NOT NULL, PRIMARY KEY (id_index) )").executeSql();
              if (otherJobInput != null) {
                otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
              }
            }
            if (GrouperDdlUtils.isPostgres() || GrouperDdlUtils.isOracle()) {
              new GcDbAccess().sql("COMMENT ON TABLE grouper_sync_dep_group_group IS 'Groups are listed that are used in group translations.  Provisionable groups will need to be recalced if there are changes (not membership recalc)'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_sync_dep_group_group.id_index IS 'primary key'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_sync_dep_group_group.grouper_sync_id IS 'provisioner'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_sync_dep_group_group.group_id IS 'group uuid'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_sync_dep_group_group.field_id IS 'field uuid'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_sync_dep_group_group.provisionable_group_id IS 'group uuid of the provisionable group that uses this other group as a role'").executeSql();
            }
              
            new GcDbAccess().sql("alter table grouper_sync_dep_group_group add CONSTRAINT grouper_sync_dep_grp_grp_fk_1 FOREIGN KEY (provisionable_group_id) REFERENCES grouper_groups(id)").executeSql();
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            }
            
            new GcDbAccess().sql("alter table grouper_sync_dep_group_group add CONSTRAINT grouper_sync_dep_grp_grp_fk_3 FOREIGN KEY (grouper_sync_id) REFERENCES grouper_sync(id)").executeSql();
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            }
            
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added table grouper_sync_dep_group_group");
            }
          } else {
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", table grouper_sync_dep_group_group exists already");
            }
          }

          if (!GrouperDdlUtils.assertIndexExists("grouper_sync_dep_group_group", "grouper_sync_dep_grp_grp_idx0")) {
            new GcDbAccess().sql("CREATE INDEX grouper_sync_dep_grp_grp_idx0 ON grouper_sync_dep_group_group (grouper_sync_id)").executeSql();
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            }
          } else {
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", index grouper_sync_dep_grp_grp_idx0 exists already");
            }
          }
          if (!GrouperDdlUtils.assertIndexExists("grouper_sync_dep_group_group", "grouper_sync_dep_grp_grp_idx1")) {
            new GcDbAccess().sql("CREATE UNIQUE INDEX grouper_sync_dep_grp_grp_idx1 ON grouper_sync_dep_group_group (grouper_sync_id,group_id,field_id,provisionable_group_id)").executeSql();
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            }
          } else {
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", index grouper_sync_dep_grp_grp_idx1 exists already");
            }
          }
          if (!GrouperDdlUtils.assertIndexExists("grouper_sync_dep_group_group", "grouper_sync_dep_grp_grp_idx2")) {
            new GcDbAccess().sql("CREATE INDEX grouper_sync_dep_grp_grp_idx2 ON grouper_sync_dep_group_group (grouper_sync_id,provisionable_group_id)").executeSql();
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            }
          } else {
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", index grouper_sync_dep_grp_grp_idx2 exists already");
            }
          }
          if (!GrouperDdlUtils.assertIndexExists("grouper_sync_dep_group_group", "grouper_sync_dep_grp_grp_idx3")) {
            new GcDbAccess().sql("CREATE INDEX grouper_sync_dep_grp_grp_idx3 ON grouper_sync_dep_group_group (grouper_sync_id,group_id,field_id)").executeSql();
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            }
          } else {
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", index grouper_sync_dep_grp_grp_idx3 exists already");
            }
          }
            
          
        } catch (Throwable t) {
          String message = "Could not perform upgrade task V11 on grouper_loader_log adding tables/indexes for GRP-5302 for provisioning attributes!  "
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
  public GrouperVersion versionIntroduced() {
    return GrouperVersion.valueOfIgnoreCase("4.11.0");
  }

}
