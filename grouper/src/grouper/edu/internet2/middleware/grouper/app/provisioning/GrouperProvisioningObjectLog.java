package edu.internet2.middleware.grouper.app.provisioning;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
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
  
  public void debug(GrouperProvisioningObjectLogType state) {
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
    state.logState(this, this.grouperProvisioner, logMessage);
    
    if (logMessage.charAt(logMessage.length()-1) == '\n') {
      logMessage.setLength(logMessage.length() - 1);
    }
    if (LOG.isDebugEnabled()) {
      LOG.debug(logMessage);      
    } else {
      LOG.error(logMessage);
    }
  }

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperProvisioningObjectLog.class);
  
}
