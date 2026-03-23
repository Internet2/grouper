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

public class UpgradeTaskV39 implements UpgradeTasksInterface {
  
  
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(UpgradeTaskV39.class);
  
  @Override
  public GrouperVersion versionIntroduced() {
    return GrouperVersion.valueOfIgnoreCase("7.1.0");
  }
  
  @Override
  public void updateVersionFromPrevious(OtherJobInput otherJobInput) {
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        
        if (GrouperDdlUtils.isOracle() || GrouperDdlUtils.isPostgres()) {

          // grouper_lifecycle_event_config table and column comments
          new GcDbAccess().sql("COMMENT ON TABLE grouper_lifecycle_event_config IS 'table to store user lifecycle event configs'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_lifecycle_event_config.internal_id IS 'integer id for this table'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_lifecycle_event_config.config_id IS 'unique user friendly id for the config'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_lifecycle_event_config.group_internal_id IS 'group internal id'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_lifecycle_event_config.stem_id_index IS 'folder id index'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_lifecycle_event_config.data_field_internal_id IS 'data field internal id'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_lifecycle_event_config.data_row_internal_id IS 'data row internal id'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_lifecycle_event_config.created_on_micros IS 'when this event config was created'").executeSql();

          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added comments for grouper_lifecycle_event_config");
          }

          // grouper_lifecycle_event table and column comments
          new GcDbAccess().sql("COMMENT ON TABLE grouper_lifecycle_event IS 'table to store user lifecycle events'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_lifecycle_event.internal_id IS 'integer id for this table'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_lifecycle_event.grpr_lcycl_evnt_cnfg_intrnl_id IS 'internal id of the grouper lifecycle config table'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_lifecycle_event.member_internal_id IS 'member internal id'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_lifecycle_event.event_micros IS 'when the event occurred'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_lifecycle_event.ntrl_lng_priv_dic_intrnl_id IS 'dictionary table internal id'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_lifecycle_event.ntrl_lng_unpriv_dic_intrnl_id IS 'dictionary table internal id'").executeSql();

          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added comments for grouper_lifecycle_event");
          }

        }
        
        return null;
      }
    });
  }

}
