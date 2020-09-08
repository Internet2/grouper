package edu.internet2.middleware.grouper.app.provisioning;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * provisioning log
 */
public class GrouperProvisioningObjectLog {
  
  private StringBuilder objectLog = new StringBuilder();
  
  public GrouperProvisioningObjectLog() {
    
  }
  private GrouperProvisioner grouperProvisioner = null;
  public GrouperProvisioningObjectLog(GrouperProvisioner grouperProvisioner) {
    this.grouperProvisioner = grouperProvisioner;
  }
  
  public void debug(String state) {
    if (!grouperProvisioner.retrieveProvisioningConfiguration().isLogAllObjectsVerbose()) {
      return;
    }
    StringBuilder logMessage = new StringBuilder("Provisioner '").append(this.grouperProvisioner.getConfigId()).append("' type '").append(this.grouperProvisioner.getGrouperProvisioningType()).append("' state '").append(state).append("': ");
    logMessage.append(GrouperUtil.toStringForLog(this.grouperProvisioner.getDebugMap()));
    if (StringUtils.equals("retrieveDataPass1", state)) {
      appendProvisioningObjects(logMessage, "Grouper provisioning", this.grouperProvisioner.getGrouperProvisioningData().getGrouperProvisioningObjects());
    }
    if ((StringUtils.equals("retrieveDataPass2", state) && !grouperProvisioner.getGrouperProvisioningType().isFullSync()) || 
        ((StringUtils.equals("retrieveDataPass1", state) && grouperProvisioner.getGrouperProvisioningType().isFullSync()))) {
      if (!grouperProvisioner.getGrouperProvisioningType().isFullSync()) {
        appendProvisioningObjects(logMessage, "Target provisioning", this.grouperProvisioner.getGrouperProvisioningData().getTargetProvisioningObjects());
      }
    }
    if (StringUtils.equals("translateGrouperToTarget", state)) {
      appendProvisioningObjects(logMessage, "Grouper target", this.grouperProvisioner.getGrouperProvisioningData().getGrouperTargetObjects());
    }
    if (StringUtils.equals("compareTargetObjects", state)) {
      appendProvisioningObjects(logMessage, "Target inserts", this.grouperProvisioner.getGrouperProvisioningData().getTargetObjectInserts());
      appendProvisioningObjects(logMessage, "Target updates", this.grouperProvisioner.getGrouperProvisioningData().getTargetObjectUpdates());
      appendProvisioningObjects(logMessage, "Target deletes", this.grouperProvisioner.getGrouperProvisioningData().getTargetObjectDeletes());
    }
    if (StringUtils.equals("targetIdTargetObjects", state)) {
      appendProvisioningObjects(logMessage, "Grouper target", this.grouperProvisioner.getGrouperProvisioningData().getGrouperTargetObjects());
      appendProvisioningObjects(logMessage, "Target provisioning", this.grouperProvisioner.getGrouperProvisioningData().getTargetProvisioningObjects());
    }
    if (logMessage.charAt(logMessage.length()-1) == '\n') {
      logMessage.setLength(logMessage.length() - 1);
    }
    if (LOG.isDebugEnabled()) {
      LOG.debug(logMessage);      
    } else {
      LOG.error(logMessage);
    }
  }

  /**
   * 
   * @param string
   * @param grouperProvisioningObjects
   */
  private void appendProvisioningObjects(StringBuilder logMessage, String label,
      GrouperProvisioningLists grouperProvisioningObjects) {
    appendProvisioningObjectsOfType(logMessage, label, grouperProvisioningObjects.getProvisioningGroups(), "groups");
    appendProvisioningObjectsOfType(logMessage, label, grouperProvisioningObjects.getProvisioningEntities(), "entities");
    appendProvisioningObjectsOfType(logMessage, label, grouperProvisioningObjects.getProvisioningMemberships(), "memberships");
  }

  public void appendProvisioningObjectsOfType(StringBuilder logMessage, String label,
      List beans, String type) {
    if (logMessage.charAt(logMessage.length()-1) != '\n') {
      logMessage.append("\n");
    }
    logMessage.append(label).append(" ").append(type).append(" (")
      .append(GrouperUtil.length(beans)).append(")");
    if (GrouperUtil.length(beans) == 0) {
      return;
    }
    logMessage.append(":\n");
    int objectCount = 0;
    for (Object bean : GrouperUtil.nonNull(beans)) {
      if (objectCount++ > 10) {
        logMessage.append(objectCount).append(". ").append(bean == null ? "null" : bean.toString()).append("\n");
        break;
      }
      logMessage.append(objectCount).append(". ").append(bean == null ? "null" : bean.toString()).append("\n");
    }
  }

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperProvisioningObjectLog.class);
  
}
