package edu.internet2.middleware.grouper.app.loader;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class LdapToSqlSyncDaemon extends OtherJobBase {

  /**
   * logger 
   */
  private static final Log LOG = GrouperUtil.getLog(LdapToSqlSyncDaemon.class);

  
  public LdapToSqlSyncDaemon() {
  }

  private OtherJobInput otherJobInput = null;

  private Map<String, Object> debugMap = null;

  private GrouperSession grouperSession = null;
  
  private String jobName = null;
  
  private String databaseExternalSystemId = null;
  
  @Override
  public OtherJobOutput run(OtherJobInput theOtherJobInput) {
    
    this.otherJobInput = theOtherJobInput;
    
    debugMap = new LinkedHashMap<String, Object>();
    
    grouperSession = GrouperSession.startRootSession();
    
    jobName = otherJobInput.getJobName();
    
    // jobName = OTHER_JOB_csvSync
    jobName = jobName.substring("OTHER_JOB_".length(), jobName.length());

//    // notification, summary
//    emailTypeString = GrouperLoaderConfig
//        .retrieveConfig().propertyValueStringRequired("otherJob." + jobName + ".emailType");
//    debugMap.put("emailType", emailTypeString);
    
    
    otherJobInput.getHib3GrouperLoaderLog().addUpdateCount(1);
    otherJobInput.getHib3GrouperLoaderLog().setJobMessage(GrouperUtil.mapToString(debugMap));
    return null;
  }
  
}
