package edu.internet2.middleware.grouper.app.upgradeTasks;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase.OtherJobInput;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.GrouperVersion;

public class UpgradeTaskV21 implements UpgradeTasksInterface {
  
  @Override
  public boolean upgradeTaskIsDdl() {
    return true;
  }
  
  @Override
  public GrouperVersion versionIntroduced() {
    return GrouperVersion.valueOfIgnoreCase("5.13.0");
  }

  @Override
  public boolean doesUpgradeTaskHaveDdlWorkToDo() {

    return (boolean) GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        
        if (GrouperDdlUtils.assertTableThere(true, "grouper_sql_cache_mship") && !GrouperDdlUtils.assertColumnThere(true, "grouper_sql_cache_group", "last_membership_sync")) {
          return true;
        }
        
        if (GrouperDdlUtils.assertIndexExists("grouper_sql_cache_mship", "grouper_sql_cache_mship2_idx")) {
          return true;
        }
        
        if (!GrouperDdlUtils.assertPrimaryKeyExists("grouper_sql_cache_mship")) {
          return true;
        }
        
        if (!GrouperDdlUtils.assertIndexExists("grouper_sql_cache_group", "grouper_sql_cache_group2_idx")) {
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
        
        if (!GrouperDdlUtils.assertTableThere(true, "grouper_sql_cache_mship")) {
          return null;
        }
        
        if (GrouperDdlUtils.assertTableThere(true, "grouper_sql_cache_mship_v")) {
          if (GrouperDdlUtils.assertColumnThere(true, "grouper_sql_cache_mship_v", "mship_hst_internal_id")) {
            HibernateSession.bySqlStatic().executeSql("DROP VIEW grouper_sql_cache_mship_v");
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", dropped view grouper_sql_cache_mship_v");
          }
        }
        
        if (GrouperDdlUtils.assertColumnThere(true, "grouper_sql_cache_mship", "internal_id")) {
          HibernateSession.bySqlStatic().executeSql("ALTER TABLE grouper_sql_cache_mship DROP COLUMN internal_id");
          otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", dropped column grouper_sql_cache_mship.internal_id");
        }
        
        if (GrouperDdlUtils.assertColumnThere(true, "grouper_sql_cache_mship", "created_on")) {
          HibernateSession.bySqlStatic().executeSql("ALTER TABLE grouper_sql_cache_mship DROP COLUMN created_on");
          otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", dropped column grouper_sql_cache_mship.created_on");
        }
        
        if (!GrouperDdlUtils.assertColumnThere(true, "grouper_sql_cache_group", "last_membership_sync")) {
          if (GrouperDdlUtils.isOracle()) {
            HibernateSession.bySqlStatic().executeSql("ALTER TABLE grouper_sql_cache_group ADD last_membership_sync DATE");
          } else if (GrouperDdlUtils.isMysql()) {
            HibernateSession.bySqlStatic().executeSql("ALTER TABLE grouper_sql_cache_group ADD COLUMN last_membership_sync DATETIME");
          } else {
            HibernateSession.bySqlStatic().executeSql("ALTER TABLE grouper_sql_cache_group ADD COLUMN last_membership_sync timestamp");
          }
          
          otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added column grouper_sql_cache_group.last_membership_sync");
        }
        
        if (GrouperDdlUtils.assertIndexExists("grouper_sql_cache_mship", "grouper_sql_cache_mship2_idx")) {
          if (GrouperDdlUtils.isMysql()) {
            HibernateSession.bySqlStatic().executeSql("DROP INDEX grouper_sql_cache_mship2_idx ON grouper_sql_cache_mship");
          } else {
            HibernateSession.bySqlStatic().executeSql("DROP INDEX grouper_sql_cache_mship2_idx");
          }
          otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", dropped index grouper_sql_cache_mship2_idx");
        }
        
        if (!GrouperDdlUtils.assertPrimaryKeyExists("grouper_sql_cache_mship")) {
          HibernateSession.bySqlStatic().executeSql("ALTER TABLE grouper_sql_cache_mship ADD PRIMARY KEY (member_internal_id, sql_cache_group_internal_id)");
          otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added primary key to grouper_sql_cache_mship");
        }
        
        if (!GrouperDdlUtils.assertIndexExists("grouper_sql_cache_group", "grouper_sql_cache_group2_idx")) {
          HibernateSession.bySqlStatic().executeSql("CREATE INDEX grouper_sql_cache_group2_idx ON grouper_sql_cache_group (last_membership_sync)");
          otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index grouper_sql_cache_group2_idx");
        }
        
        return null;
      }
    });
  }

}
