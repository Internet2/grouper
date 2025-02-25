package edu.internet2.middleware.grouper.app.upgradeTasks;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase.OtherJobInput;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

public class UpgradeTaskV31 implements UpgradeTasksInterface {
  
  @Override
  public boolean doesUpgradeTaskHaveDdlWorkToDo() {
    
    if (!GrouperDdlUtils.assertColumnThere(true, "grouper_data_row_assign", "last_updated")) {
      return true;
    }
    
    if (GrouperDdlUtils.assertColumnThere(true, "grouper_data_row_field_asn_hst", "start_time")) {
      return true;
    }
    
    if (GrouperDdlUtils.assertColumnThere(true, "grouper_data_row_field_asn_hst", "end_time")) {
      return true;
    }
    
    if (!GrouperDdlUtils.assertForeignKeyExists("grouper_data_row_field_asn_hst", "data_row_field_asn_hst_fk_3")) {
      return true;
    }
    
    if (!GrouperDdlUtils.assertIndexExists("grouper_data_row_field_asn_hst", "data_row_field_asn_hst1_idx")) {
      return true;
    }
    
    if (!GrouperDdlUtils.assertIndexExists("grouper_data_row_field_asn_hst", "data_row_field_asn_hst2_idx")) {
      return true;
    }
    
    if (!GrouperDdlUtils.assertIndexExists("grouper_data_row_field_asn_hst", "data_row_field_asn_hst3_idx")) {
      return true;
    }
    
    if (!GrouperDdlUtils.assertIndexExists("grouper_data_row_field_asn_hst", "data_row_field_asn_hst4_idx")) {
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
    return GrouperVersion.valueOfIgnoreCase("5.17.0");
  }

  @Override
  public void updateVersionFromPrevious(OtherJobInput otherJobInput) {
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        
        boolean lastUpdatedExists = GrouperDdlUtils.assertColumnThere(true, "grouper_data_row_assign", "last_updated");
        boolean startTimeExists = GrouperDdlUtils.assertColumnThere(true, "grouper_data_row_field_asn_hst", "start_time");
        boolean endTimeExists = GrouperDdlUtils.assertColumnThere(true, "grouper_data_row_field_asn_hst", "end_time");
        boolean fkExists = GrouperDdlUtils.assertForeignKeyExists("grouper_data_row_field_asn_hst", "data_row_field_asn_hst_fk_3");
        boolean index1Exists = GrouperDdlUtils.assertIndexExists("grouper_data_row_field_asn_hst", "data_row_field_asn_hst1_idx");
        boolean index2Exists = GrouperDdlUtils.assertIndexExists("grouper_data_row_field_asn_hst", "data_row_field_asn_hst2_idx");
        boolean index3Exists = GrouperDdlUtils.assertIndexExists("grouper_data_row_field_asn_hst", "data_row_field_asn_hst3_idx");
        boolean index4Exists = GrouperDdlUtils.assertIndexExists("grouper_data_row_field_asn_hst", "data_row_field_asn_hst4_idx");
        
        if (!lastUpdatedExists && startTimeExists && endTimeExists && !fkExists) {
          // we'll delete the data only if there's high confidence that an upgrade is being done (vs the foreign key just being removed sometime in the future)
          int count1 = new GcDbAccess().sql("delete from grouper_data_row_field_asn_hst").executeSql();
          int count2 = new GcDbAccess().sql("delete from grouper_data_row_assign_hst").executeSql();
          
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addDeleteCount(count1);
            otherJobInput.getHib3GrouperLoaderLog().addDeleteCount(count2);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", cleared tables grouper_data_row_field_asn_hst and grouper_data_row_assign_hst");
          }
        }
        
        if (!lastUpdatedExists) {
          if (GrouperDdlUtils.isOracle()) {
            new GcDbAccess().sql("ALTER TABLE grouper_data_row_assign ADD last_updated NUMBER(38)").executeSql();
          } else {
            new GcDbAccess().sql("ALTER TABLE grouper_data_row_assign ADD COLUMN last_updated BIGINT").executeSql();
          }
          
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added column grouper_data_row_assign.last_updated");
          }
        }
        
        if (startTimeExists) {
          new GcDbAccess().sql("ALTER TABLE grouper_data_row_field_asn_hst DROP COLUMN start_time").executeSql();
          
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addDeleteCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", dropped column grouper_data_row_field_asn_hst.start_time");
          }
        }
        
        if (endTimeExists) {
          if (index1Exists) {
            if (GrouperDdlUtils.isMysql()) {
              new GcDbAccess().sql("DROP INDEX data_row_field_asn_hst1_idx ON grouper_data_row_field_asn_hst").executeSql();
            } else {
              new GcDbAccess().sql("DROP INDEX data_row_field_asn_hst1_idx").executeSql();
            }
            
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().addDeleteCount(1);
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", dropped index data_row_field_asn_hst1_idx");
            }
            
            index1Exists = false;
          }
          
          if (index2Exists) {
            if (GrouperDdlUtils.isMysql()) {
              new GcDbAccess().sql("DROP INDEX data_row_field_asn_hst2_idx ON grouper_data_row_field_asn_hst").executeSql();
            } else {
              new GcDbAccess().sql("DROP INDEX data_row_field_asn_hst2_idx").executeSql();
            }
            
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().addDeleteCount(1);
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", dropped index data_row_field_asn_hst2_idx");
            }
            
            index2Exists = false;
          }
          
          if (index3Exists) {
            if (GrouperDdlUtils.isMysql()) {
              new GcDbAccess().sql("DROP INDEX data_row_field_asn_hst3_idx ON grouper_data_row_field_asn_hst").executeSql();
            } else {
              new GcDbAccess().sql("DROP INDEX data_row_field_asn_hst3_idx").executeSql();
            }
            
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().addDeleteCount(1);
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", dropped index data_row_field_asn_hst3_idx");
            }
            
            index3Exists = false;
          }
          
          // mysql will give an error dropping the last index if there isn't another index for data_field_internal_id due to a foreign key
          {
            new GcDbAccess().sql("CREATE INDEX data_row_field_asn_hst3_idx ON grouper_data_row_field_asn_hst (data_field_internal_id, value_dictionary_internal_id)").executeSql();
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index data_row_field_asn_hst3_idx");
            }
            
            index3Exists = true;
          }
          
          if (index4Exists) {
            if (GrouperDdlUtils.isMysql()) {
              new GcDbAccess().sql("DROP INDEX data_row_field_asn_hst4_idx ON grouper_data_row_field_asn_hst").executeSql();
            } else {
              new GcDbAccess().sql("DROP INDEX data_row_field_asn_hst4_idx").executeSql();
            }
            
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().addDeleteCount(1);
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", dropped index data_row_field_asn_hst4_idx");
            }
            
            index4Exists = false;
          }
          
          new GcDbAccess().sql("ALTER TABLE grouper_data_row_field_asn_hst DROP COLUMN end_time").executeSql();
          
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addDeleteCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", dropped column grouper_data_row_field_asn_hst.end_time");
          }
        }
        
        if (!fkExists) {
          new GcDbAccess().sql("ALTER TABLE grouper_data_row_field_asn_hst ADD CONSTRAINT data_row_field_asn_hst_fk_3 FOREIGN KEY (data_row_assign_internal_id) REFERENCES grouper_data_row_assign_hst(internal_id)").executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added foreign key data_row_field_asn_hst_fk_3");
          }
        }
        
        if (!index1Exists) {
          new GcDbAccess().sql("CREATE INDEX data_row_field_asn_hst1_idx ON grouper_data_row_field_asn_hst (data_row_assign_internal_id, data_field_internal_id, value_dictionary_internal_id)").executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index data_row_field_asn_hst1_idx");
          }
        }
        
        if (!index2Exists) {
          new GcDbAccess().sql("CREATE INDEX data_row_field_asn_hst2_idx ON grouper_data_row_field_asn_hst (data_row_assign_internal_id, data_field_internal_id, value_integer)").executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index data_row_field_asn_hst2_idx");
          }
        }
        
        if (!index3Exists) {
          new GcDbAccess().sql("CREATE INDEX data_row_field_asn_hst3_idx ON grouper_data_row_field_asn_hst (data_field_internal_id, value_dictionary_internal_id)").executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index data_row_field_asn_hst3_idx");
          }
        }
        
        if (!index4Exists) {
          new GcDbAccess().sql("CREATE INDEX data_row_field_asn_hst4_idx ON grouper_data_row_field_asn_hst (data_field_internal_id, value_integer)").executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index data_row_field_asn_hst4_idx");
          }
        }
        
        return null;
      }
    });
  }

}
