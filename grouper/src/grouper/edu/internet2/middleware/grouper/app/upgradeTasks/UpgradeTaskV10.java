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

public class UpgradeTaskV10 implements UpgradeTasksInterface {
  
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(UpgradeTaskV10.class);
  
  @Override
  public boolean upgradeTaskIsDdl() {
    return true;
  }
  
  @Override
  public boolean doesUpgradeTaskHaveDdlWorkToDo() {
    
    return (boolean) GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
    
          if (!GrouperDdlUtils.assertIndexExists("grouper_loader_log", "grouper_loader_log_temp_st_idx")) {
            return true;
          }

          if (!GrouperDdlUtils.assertIndexExists("grouper_loader_log", "grouper_loader_log_temp_s2_idx")) {
            return true;
          }
          
          if (!GrouperDdlUtils.assertIndexExists("grouper_loader_log", "grouper_loader_log_temp_s3_idx")) {
            return true;
          }
           
          if (!GrouperDdlUtils.assertIndexExists("grouper_loader_log", "grouper_loader_log_temp_s4_idx")) {
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
          if (!GrouperDdlUtils.assertIndexExists("grouper_loader_log", "grouper_loader_log_temp_st_idx")) {
            new GcDbAccess().sql("CREATE INDEX grouper_loader_log_temp_st_idx ON grouper_loader_log (job_name,started_time)").executeSql();
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index grouper_loader_log_temp_st_idx");
            }
          } else {
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", index grouper_loader_log_temp_st_idx exists already");
            }
          }

          if (!GrouperDdlUtils.assertIndexExists("grouper_loader_log", "grouper_loader_log_temp_s2_idx")) {
            new GcDbAccess().sql("CREATE INDEX grouper_loader_log_temp_s2_idx ON grouper_loader_log (job_name,status,last_updated)").executeSql();
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index grouper_loader_log_temp_s2");
            }
          } else {
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", index grouper_loader_log_temp_s2 exists already");
            }
          }
          if (!GrouperDdlUtils.assertIndexExists("grouper_loader_log", "grouper_loader_log_temp_s3_idx")) {
            new GcDbAccess().sql("CREATE INDEX grouper_loader_log_temp_s3_idx ON grouper_loader_log (status,last_updated)").executeSql();
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index grouper_loader_log_temp_s3");
            }
          } else {
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", index grouper_loader_log_temp_s3 exists already");
            }
          }
          if (!GrouperDdlUtils.assertIndexExists("grouper_loader_log", "grouper_loader_log_temp_s4_idx")) {
            new GcDbAccess().sql("CREATE INDEX grouper_loader_log_temp_s4_idx ON grouper_loader_log (parent_job_name)").executeSql();
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index grouper_loader_log_temp_s4");
            }
          } else {
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", index grouper_loader_log_temp_s4 exists already");
            }
          }
        } catch (Throwable t) {
          String message = "Could not perform upgrade task V10 on grouper_loader_log adding indexes for GRP-5195!  Skipping this upgrade task, install the indexes manually";
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
    return GrouperVersion.valueOfIgnoreCase("4.8.0");
  }

}
