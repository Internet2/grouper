package edu.internet2.middleware.grouper.app.provisioning;

import org.quartz.DisallowConcurrentExecution;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderStatus;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.util.GrouperUtil;

@DisallowConcurrentExecution
public class GrouperProvisioningFullSyncJob extends OtherJobBase {
  
  @Override
  public OtherJobOutput run(final OtherJobInput otherJobInput) {
    
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        
        String jobName = otherJobInput.getJobName();
        String daemonName = jobName.substring(GrouperLoaderType.GROUPER_OTHER_JOB_PREFIX.length(), jobName.length());
        String key = "otherJob."+daemonName+".provisionerConfigId";
        String provisionerConfigId = GrouperLoaderConfig.retrieveConfig().propertyValueString(key);
        
        GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner(provisionerConfigId);
        GrouperProvisioningOutput grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull);
        
        return null;
      }
    });
    
    return null;
  }
  
  public static void main(String[] args) {
    runDaemonStandalone();
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
        String jobName = "OTHER_JOB_fullProvisioningDaemon";
    
        hib3GrouperLoaderLog.setJobName(jobName);
        hib3GrouperLoaderLog.setJobType(GrouperLoaderType.OTHER_JOB.name());
        hib3GrouperLoaderLog.setStatus(GrouperLoaderStatus.STARTED.name());
        hib3GrouperLoaderLog.store();
        
        OtherJobInput otherJobInput = new OtherJobInput();
        otherJobInput.setJobName(jobName);
        otherJobInput.setHib3GrouperLoaderLog(hib3GrouperLoaderLog);
        otherJobInput.setGrouperSession(grouperSession);
        
        new GrouperProvisioningFullSyncJob().run(otherJobInput);
        return null;
      }
    });
      
  }

}
