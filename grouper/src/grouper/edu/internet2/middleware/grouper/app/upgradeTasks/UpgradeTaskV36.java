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

public class UpgradeTaskV36 implements UpgradeTasksInterface {
  
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(UpgradeTaskV36.class);
  
  @Override
  public boolean doesUpgradeTaskHaveDdlWorkToDo() {
    
    if (!GrouperDdlUtils.assertTableThere(true, "grouper_lifecycle_event_config")) {
      return true;
    }

    if (!GrouperDdlUtils.assertTableThere(true, "grouper_lifecycle_event")) {
      return true;
    }

    return false;
  }

  @Override
  public boolean upgradeTaskIsDdl() {
    return true;
  }

  @Override
  public GrouperVersion versionIntroduced() {
    return GrouperVersion.valueOfIgnoreCase("5.22.0");
  }
  
  @Override
  public void updateVersionFromPrevious(OtherJobInput otherJobInput) {
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        
        if (!GrouperDdlUtils.assertTableThere(true, "grouper_lifecycle_event_config")) {
          if (GrouperDdlUtils.isOracle()) {
            new GcDbAccess().sql("""
                CREATE TABLE grouper_lifecycle_event_config (
                  internal_id NUMBER(38) NOT NULL,
                  config_id varchar2(100) NOT NULL,
                  group_internal_id NUMBER(38),
                  stem_id_index NUMBER(38),
                  data_field_internal_id NUMBER(38),
                  data_row_internal_id NUMBER(38),
                  created_on_micros NUMBER(38) NOT NULL,
                  PRIMARY KEY (internal_id)
                )
              """).executeSql();
            
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", created table grouper_lifecycle_event_config");
            }
          } else {
            new GcDbAccess().sql("""
                CREATE TABLE grouper_lifecycle_event_config (
                  internal_id BIGINT NOT NULL,
                  config_id varchar(100) NOT NULL,
                  group_internal_id BIGINT,
                  stem_id_index BIGINT,
                  data_field_internal_id BIGINT,
                  data_row_internal_id BIGINT,
                  created_on_micros BIGINT NOT NULL,
                  PRIMARY KEY (internal_id)
                )
              """).executeSql();
            
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", created table grouper_lifecycle_event_config");
            }
          }
        }
        
        if (!GrouperDdlUtils.assertIndexExists("grouper_lifecycle_event_config", "grouper_lcycle_evnt_cnfg_idx")) {
          new GcDbAccess().sql("CREATE UNIQUE INDEX grouper_lcycle_evnt_cnfg_idx ON grouper_lifecycle_event_config (config_id)").executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index grouper_lcycle_evnt_cnfg_idx");
          }
        }
        
        if (!GrouperDdlUtils.assertForeignKeyExists("grouper_lifecycle_event_config", "group_internal_id_fk")) {
          new GcDbAccess().sql("ALTER TABLE  grouper_lifecycle_event_config ADD CONSTRAINT group_internal_id_fk FOREIGN KEY (group_internal_id) REFERENCES  grouper_groups(internal_id)").executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added foreign key group_internal_id_fk");
          }
        }
        
        if (!GrouperDdlUtils.assertForeignKeyExists("grouper_lifecycle_event_config", "stem_id_index_fk")) {
          new GcDbAccess().sql("ALTER TABLE  grouper_lifecycle_event_config ADD CONSTRAINT stem_id_index_fk FOREIGN KEY (stem_id_index) REFERENCES  grouper_stems(id_index)").executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added foreign key stem_id_index_fk");
          }
        }
        
        if (!GrouperDdlUtils.assertForeignKeyExists("grouper_lifecycle_event_config", "data_field_internal_id_fk")) {
          new GcDbAccess().sql("ALTER TABLE  grouper_lifecycle_event_config ADD CONSTRAINT data_field_internal_id_fk FOREIGN KEY (data_field_internal_id) REFERENCES  grouper_data_field(internal_id)").executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added foreign key data_field_internal_id_fk");
          }
        }
        
        if (!GrouperDdlUtils.assertForeignKeyExists("grouper_lifecycle_event_config", "data_row_internal_id_fk")) {
          new GcDbAccess().sql("ALTER TABLE  grouper_lifecycle_event_config ADD CONSTRAINT data_row_internal_id_fk FOREIGN KEY (data_row_internal_id) REFERENCES  grouper_data_row(internal_id)").executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added foreign key data_row_internal_id_fk");
          }
        }
        
        if (!GrouperDdlUtils.assertTableThere(true, "grouper_lifecycle_event")) {
          if (GrouperDdlUtils.isOracle()) {
            new GcDbAccess().sql("""
                CREATE TABLE grouper_lifecycle_event (
                  internal_id NUMBER(38) NOT NULL,
                  grpr_lcycl_evnt_cnfg_intrnl_id NUMBER(38) NOT NULL,
                  member_internal_id NUMBER(38) NOT NULL,
                  event_micros NUMBER(38) NOT NULL,
                  ntrl_lng_priv_dic_intrnl_id NUMBER(38),
                  ntrl_lng_unpriv_dic_intrnl_id NUMBER(38),
                  PRIMARY KEY (internal_id)
                )
              """).executeSql();
            
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", created table grouper_lifecycle_event");
            }
          } else {
            new GcDbAccess().sql("""
                CREATE TABLE grouper_lifecycle_event (
                  internal_id BIGINT NOT NULL,
                  grpr_lcycl_evnt_cnfg_intrnl_id BIGINT NOT NULL,
                  member_internal_id BIGINT NOT NULL,
                  event_micros BIGINT NOT NULL,
                  ntrl_lng_priv_dic_intrnl_id BIGINT,
                  ntrl_lng_unpriv_dic_intrnl_id BIGINT,
                  PRIMARY KEY (internal_id)
                )
              """).executeSql();
            
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", created table grouper_lifecycle_event");
            }
          }
        }
        
        if (!GrouperDdlUtils.assertForeignKeyExists("grouper_lifecycle_event", "lcycl_evnt_cnfg_intrnl_id_fk")) {
          new GcDbAccess().sql("ALTER TABLE  grouper_lifecycle_event ADD CONSTRAINT lcycl_evnt_cnfg_intrnl_id_fk FOREIGN KEY (grpr_lcycl_evnt_cnfg_intrnl_id) REFERENCES  grouper_lifecycle_event_config(internal_id)").executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added foreign key lcycl_evnt_cnfg_intrnl_id_fk");
          }
        }
        
        if (!GrouperDdlUtils.assertForeignKeyExists("grouper_lifecycle_event", "member_internal_id_fk")) {
          new GcDbAccess().sql("ALTER TABLE  grouper_lifecycle_event ADD CONSTRAINT member_internal_id_fk FOREIGN KEY (member_internal_id) REFERENCES  grouper_members(internal_id)").executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added foreign key member_internal_id_fk");
          }
        }
        
        if (!GrouperDdlUtils.assertForeignKeyExists("grouper_lifecycle_event", "lng_priv_dic_intrnl_id_fk")) {
          new GcDbAccess().sql("ALTER TABLE  grouper_lifecycle_event ADD CONSTRAINT lng_priv_dic_intrnl_id_fk FOREIGN KEY (ntrl_lng_priv_dic_intrnl_id) REFERENCES  grouper_dictionary(internal_id)").executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added foreign key lng_priv_dic_intrnl_id_fk");
          }
        }
        
        if (!GrouperDdlUtils.assertForeignKeyExists("grouper_lifecycle_event", "lng_unpriv_dic_intrnl_id_fk")) {
          new GcDbAccess().sql("ALTER TABLE  grouper_lifecycle_event ADD CONSTRAINT lng_unpriv_dic_intrnl_id_fk FOREIGN KEY (ntrl_lng_unpriv_dic_intrnl_id) REFERENCES  grouper_dictionary(internal_id)").executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added foreign key lng_unpriv_dic_intrnl_id_fk");
          }
        }
        
        return null;
      }
    });
  }

}
