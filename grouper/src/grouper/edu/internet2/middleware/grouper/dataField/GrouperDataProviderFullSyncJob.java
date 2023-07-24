package edu.internet2.middleware.grouper.dataField;

import java.util.Map;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.quartz.DisallowConcurrentExecution;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.util.GrouperUtil;

@DisallowConcurrentExecution
public class GrouperDataProviderFullSyncJob extends OtherJobBase {
  
  private static final Log LOG = GrouperUtil.getLog(GrouperDataProviderFullSyncJob.class);
  
  @Override
  public OtherJobOutput run(final OtherJobInput otherJobInput) {
    
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        
        String jobName = otherJobInput.getJobName();
        String daemonName = jobName.substring(GrouperLoaderType.GROUPER_OTHER_JOB_PREFIX.length(), jobName.length());
        String key = "otherJob."+daemonName+".dataProviderConfigId";
        String dataProviderConfigId = GrouperLoaderConfig.retrieveConfig().propertyValueString(key);
        
        try {
          Map<String, Object> debugMap = GrouperDataEngine.loadFull(dataProviderConfigId);
          otherJobInput.getHib3GrouperLoaderLog().setJobMessage("Finished successfully running full sync for dataProviderConfigId=" + dataProviderConfigId + "\n" + GrouperUtil.mapToString(debugMap));
        } catch (Exception e) {
          LOG.warn("Error while running full sync for dataProviderConfigId=" + dataProviderConfigId, e);
          otherJobInput.getHib3GrouperLoaderLog().setJobMessage("Finished running full sync for dataProviderConfigId=" + dataProviderConfigId + " with an error: " + ExceptionUtils.getFullStackTrace(e));
          throw e;
        } finally {
          otherJobInput.getHib3GrouperLoaderLog().store();
        }

        return null;
      }
    });
    
    return null;
  }
}
