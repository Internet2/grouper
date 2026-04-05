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

/**
 * Upgrade task to drop and recreate grouper_duo_user_user_name_idx as non-unique,
 * and add group_set_member_member_field_idx on grouper_group_set.
 * @author mchyzer
 */
public class UpgradeTaskV40 implements UpgradeTasksInterface {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(UpgradeTaskV40.class);

  @Override
  public boolean doesUpgradeTaskHaveDdlWorkToDo() {
    return true;
  }

  @Override
  public boolean upgradeTaskIsDdl() {
    return true;
  }

  @Override
  public GrouperVersion versionIntroduced() {
    return GrouperVersion.valueOfIgnoreCase("4.23.0");
  }

  @Override
  public void updateVersionFromPrevious(OtherJobInput otherJobInput) {
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {

        // ==================== grouper_duo_user_user_name_idx ====================
        // drop and recreate as non-unique (was previously unique)
        if (GrouperDdlUtils.assertTableThere(true, "grouper_prov_duo_user")) {
          if (GrouperDdlUtils.assertIndexExists("grouper_prov_duo_user", "grouper_duo_user_user_name_idx")) {
            if (GrouperDdlUtils.isMysql()) {
              new GcDbAccess().sql("DROP INDEX grouper_duo_user_user_name_idx ON grouper_prov_duo_user").executeSql();
            } else {
              new GcDbAccess().sql("DROP INDEX grouper_duo_user_user_name_idx").executeSql();
            }
          }
          if (GrouperDdlUtils.isMysql()) {
            new GcDbAccess().sql("CREATE INDEX grouper_duo_user_user_name_idx ON grouper_prov_duo_user (user_name(100), config_id)").executeSql();
          } else {
            new GcDbAccess().sql("CREATE INDEX grouper_duo_user_user_name_idx ON grouper_prov_duo_user (user_name, config_id)").executeSql();
          }
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addUpdateCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", recreated index grouper_duo_user_user_name_idx as non-unique");
          }
        }

        // ==================== group_set_member_member_field_idx ====================
        if (!GrouperDdlUtils.assertIndexExists("grouper_group_set", "group_set_member_member_field_idx")) {
          new GcDbAccess().sql("CREATE INDEX group_set_member_member_field_idx ON grouper_group_set (member_id, member_field_id)").executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index group_set_member_member_field_idx");
          }
        }

        return null;
      }
    });
  }

}
