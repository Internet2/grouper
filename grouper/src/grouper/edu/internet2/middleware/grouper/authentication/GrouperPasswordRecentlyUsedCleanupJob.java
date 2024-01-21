package edu.internet2.middleware.grouper.authentication;

import org.apache.commons.logging.Log;
import org.quartz.DisallowConcurrentExecution;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderStatus;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.util.GrouperUtil;

@DisallowConcurrentExecution
public class GrouperPasswordRecentlyUsedCleanupJob extends OtherJobBase {
  
  /**
   * logger 
   */
  private static final Log LOG = GrouperUtil.getLog(GrouperPasswordRecentlyUsedCleanupJob.class);
  
  /**
   * run the daemon
   * @param args
   */
  public static void main(String[] args) {
    runDaemonStandalone();
  }

  @Override
  public OtherJobOutput run(OtherJobInput otherJobInput) {
    
    int totalDeleted = GrouperPasswordRecentlyUsedCleanupJob.cleanupOldEntriesFromGrouperPasswordRecentlyUsed();
    
    otherJobInput.getHib3GrouperLoaderLog().setJobMessage("Deleted "+totalDeleted+" entries from grouper password recently used table. ");

    LOG.info("GrouperPasswordRecentlyUsedCleanupJob finished successfully.");
    
    return null;
  }
  
  
  /**
   * run standalone
   */
  public static void runDaemonStandalone() {
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        Hib3GrouperLoaderLog hib3GrouperLoaderLog = new Hib3GrouperLoaderLog();
        
        hib3GrouperLoaderLog.setHost(GrouperUtil.hostname());
        String jobName = "OTHER_JOB_cleanupGrouperPasswordRecentlyUsed";

        hib3GrouperLoaderLog.setJobName(jobName);
        hib3GrouperLoaderLog.setJobType(GrouperLoaderType.OTHER_JOB.name());
        hib3GrouperLoaderLog.setStatus(GrouperLoaderStatus.STARTED.name());
        hib3GrouperLoaderLog.store();
        
        OtherJobInput otherJobInput = new OtherJobInput();
        otherJobInput.setJobName(jobName);
        otherJobInput.setHib3GrouperLoaderLog(hib3GrouperLoaderLog);
        otherJobInput.setGrouperSession(grouperSession);
        new GrouperPasswordRecentlyUsedCleanupJob().run(otherJobInput);
        return null;
      }
    });
  }
  
  public static int cleanupOldEntriesFromGrouperPasswordRecentlyUsed() {
    
    return GrouperDAOFactory.getFactory().getGrouperPasswordRecentlyUsed().cleanupOldEntriesFromGrouperPasswordRecentlyUsedTable();
    
  }

}
