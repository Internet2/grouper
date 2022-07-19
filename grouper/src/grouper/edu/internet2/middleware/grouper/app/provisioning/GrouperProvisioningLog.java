package edu.internet2.middleware.grouper.app.provisioning;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * provisioning log
 */
public class GrouperProvisioningLog {

  public String prefixLogLinesWithInstanceId(String logMessage) {
    GrouperUtil.whitespaceNormalizeNewLines(logMessage);
    String logPrefix = "Provisioner '" + this.grouperProvisioner.getConfigId() + "' (" + this.grouperProvisioner.getInstanceId() + ") ";
    if (!logMessage.startsWith(logPrefix)) {
      logMessage = logPrefix + logMessage;
    }
    String logMessageString = GrouperUtil.replace(logMessage.toString(), "\n", "\n(" + this.grouperProvisioner.getInstanceId() + "): ");
    return logMessageString;
  }
  
  /**
   * reference back up to the provisioner
   */
  private GrouperProvisioner grouperProvisioner = null;
  
  /**
   * reference back up to the provisioner
   * @return the provisioner
   */
  public GrouperProvisioner getGrouperProvisioner() {
    return this.grouperProvisioner;
  }

  /**
   * reference back up to the provisioner
   * @param grouperProvisioner1
   */
  public void setGrouperProvisioner(GrouperProvisioner grouperProvisioner1) {
    this.grouperProvisioner = grouperProvisioner1;
  }


  /**
   * type of log (label) to count so we dont log too much
   */
  private Map<String, Integer> errorTypeToCountLogged = new HashMap<String, Integer>();
  
  /**
   * type of log (label) to count so we dont log too much
   * @return the map
   */
  public Map<String, Integer> getErrorTypeToCountLogged() {
    return errorTypeToCountLogged;
  }

  /**
   * type of log (label) to count so we dont log too much
   * @param errorTypeToCountLogged
   */
  public void setErrorTypeToCountLogged(Map<String, Integer> errorTypeToCountLogged) {
    this.errorTypeToCountLogged = errorTypeToCountLogged;
  }

  /**
   * debug log
   * @param debugMap
   */
  public static void debugLog(Map<String, Object> debugMap) {
    debugLog(GrouperUtil.mapToString(debugMap));
  }

  /**
   * debug log
   * @param string
   */
  public static void debugLog(String string) {
    if (!LOG.isDebugEnabled()) {
      return;
    }
    LOG.debug(string);
  }

  /**
   * @return if debug enabled
   */
  public static boolean isDebugEnabled() {
    return LOG.isDebugEnabled();
  }

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperProvisioningLog.class);

  /**
   * if the threshold of this error label is less than the max
   * @param errorLabelForCounts
   * @return true or false
   */
  public boolean shouldLogError(String errorLabelForCounts) {
    Integer errorCountByLabel = this.errorTypeToCountLogged.get(errorLabelForCounts);
    if (errorCountByLabel == null) {
      errorCountByLabel = 0;
    }
    errorCountByLabel++;
    
    this.errorTypeToCountLogged.put(errorLabelForCounts, errorCountByLabel);
    
    if (errorCountByLabel <= this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getLogMaxErrorsPerType()) {
      return true;
    }
    return false;
  }
  
}
