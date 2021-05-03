package edu.internet2.middleware.grouper.app.provisioning;

import org.quartz.DisallowConcurrentExecution;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;

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
        final Hib3GrouperLoaderLog hib3GrouperLoaderLog = otherJobInput.getHib3GrouperLoaderLog();
        
        runFullSync(provisionerConfigId, hib3GrouperLoaderLog);

        return null;
      }
    });
    
    return null;
  }

  public static void runFullSync(String provisionerConfigId,
      final Hib3GrouperLoaderLog hib3GrouperLoaderLog) {
    final GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner(provisionerConfigId);
    
    grouperProvisioner.getGcGrouperSyncHeartbeat().insertHeartbeatLogic(new Runnable() {

      @Override
      public void run() {
        
        GrouperProvisioningOutput grouperProvisioningOutput = grouperProvisioner.getGrouperProvisioningOutput();
        if (grouperProvisioningOutput != null) {
          grouperProvisioningOutput.copyToHib3LoaderLog(hib3GrouperLoaderLog);
          hib3GrouperLoaderLog.store();
        }
      }
      
    });

    GrouperProvisioningOutput grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull);
    grouperProvisioningOutput.copyToHib3LoaderLog(hib3GrouperLoaderLog);
    hib3GrouperLoaderLog.store();
  }

  public static void main(String[] args) {
    
  }

}
