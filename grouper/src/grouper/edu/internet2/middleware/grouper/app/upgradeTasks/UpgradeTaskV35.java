package edu.internet2.middleware.grouper.app.upgradeTasks;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase.OtherJobInput;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

public class UpgradeTaskV35 implements UpgradeTasksInterface {
  
  @Override
  public boolean doesUpgradeTaskHaveDdlWorkToDo() {
    
    if (!GrouperDdlUtils.assertIndexExists("grouper_data_field_assign", "fld_assgn_mem_df_dict_idx")) {
      return true;
    }
    
    if (!GrouperDdlUtils.assertIndexExists("grouper_data_row_field_assign", "dtrwfldasg_df_dict_dra_idx")) {
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
    return GrouperVersion.valueOfIgnoreCase("5.20.7");
  }

  @Override
  public void updateVersionFromPrevious(OtherJobInput otherJobInput) {
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        
        if (!GrouperDdlUtils.assertIndexExists("grouper_data_field_assign", "fld_assgn_mem_df_dict_idx")) {
          new GcDbAccess().sql("CREATE INDEX fld_assgn_mem_df_dict_idx ON grouper_data_field_assign (member_internal_id, data_field_internal_id, value_dictionary_internal_id)").executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index fld_assgn_mem_df_dict_idx");
          }
        }
        
        if (!GrouperDdlUtils.assertIndexExists("grouper_data_row_field_assign", "dtrwfldasg_df_dict_dra_idx")) {
          new GcDbAccess().sql("CREATE INDEX dtrwfldasg_df_dict_dra_idx ON grouper_data_row_field_assign (data_field_internal_id, value_dictionary_internal_id, data_row_assign_internal_id)").executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index dtrwfldasg_df_dict_dra_idx");
          }
        }
        
        return null;
      }
    });
  }

}
