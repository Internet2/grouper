package edu.internet2.middleware.grouper.app.provisioning;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * provisioning log
 */
public class GrouperProvisioningObjectLog {
  
  private Map<String, Object> alreadyLogged = new HashMap<String, Object>();
  
  public GrouperProvisioningObjectLog() {
    
  }
  private GrouperProvisioner grouperProvisioner = null;
  public GrouperProvisioningObjectLog(GrouperProvisioner grouperProvisioner) {
    this.grouperProvisioner = grouperProvisioner;
  }

  public void error(String error, Throwable throwable) {
    if (!LOG.isErrorEnabled()) {
      return;
    }
    if (throwable != null) {
      error += "\n" + GrouperUtil.getFullStackTrace(throwable);
    }
    String logMessageString = this.grouperProvisioner.retrieveGrouperProvisioningLog().prefixLogLinesWithInstanceId(error); 
    LOG.error(logMessageString);      
  }

  public void debug(GrouperProvisioningObjectLogType state, Object... data) {
    if (!grouperProvisioner.retrieveGrouperProvisioningConfiguration().isLogAllObjectsVerbose()) {
      return;
    }
    StringBuilder logMessage = new StringBuilder("Provisioner '").append(this.grouperProvisioner.getConfigId())
        .append("' (").append(this.grouperProvisioner.getInstanceId()).append(")")
        .append(" state '").append(state)
        .append("' type '").append(this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGrouperProvisioningType())
        .append("': ");
    
    Map<String, Object> debugLog = new LinkedHashMap<String, Object>(this.grouperProvisioner.getDebugMap());
    
    // remove known things:
    debugLog.remove("provisionerClass");
    debugLog.remove("configId");
    debugLog.remove("provisioningType");

    {
      // remove things already printed if same value
      Iterator<String> iterator = debugLog.keySet().iterator();
      while (iterator.hasNext()) {
        String key = iterator.next();
        Object value = debugLog.get(key);
        Object alreadyLoggedValue = alreadyLogged.get(key);
        boolean isAlreadyLogged = alreadyLogged.containsKey(key) && GrouperUtil.equals(value, alreadyLoggedValue);
        if (isAlreadyLogged) {
          iterator.remove();
        } else {
          alreadyLogged.put(key, value);
        }
      }
      logMessage.append(GrouperUtil.toStringForLog(debugLog));
    }
    state.logState(this, this.grouperProvisioner, logMessage, data);
    
    if (logMessage.charAt(logMessage.length()-1) == '\n') {
      logMessage.setLength(logMessage.length() - 1);
    }
    // put id on each line
    String logMessageString = this.grouperProvisioner.retrieveGrouperProvisioningLog().prefixLogLinesWithInstanceId(logMessage.toString()); 
    if (this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().isLogAllObjectsVerboseToLogFile()) {
      // should be debug but its ok if not
      if (LOG.isDebugEnabled()) {
        LOG.debug(logMessageString);      
      } else if (LOG.isInfoEnabled()) {
        LOG.info(logMessageString);      
      } else if (LOG.isWarnEnabled()) {
        LOG.warn(logMessageString);      
      } else if (LOG.isErrorEnabled()) {
        LOG.error(logMessageString);      
      } else if (LOG.isFatalEnabled()) {
        LOG.fatal(logMessageString);      
      }
    }
    if (this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().isLogAllObjectsVerboseToDaemonDbLog()) {
      this.objectLog.append(new Timestamp(System.currentTimeMillis()).toString()).append(": ").append(logMessageString).append("\n\n");
    }
  }

  private StringBuilder objectLog = new StringBuilder();
  
  /**
   * put this in daemon logs
   * @return object log
   */
  public StringBuilder getObjectLog() {
    return objectLog;
  }

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperProvisioningObjectLog.class);
  
}
