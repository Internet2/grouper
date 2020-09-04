package edu.internet2.middleware.grouper.app.provisioning;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * provisioning log
 */
public class GrouperProvisioningObjectLog {
  
  public GrouperProvisioningObjectLog() {
    
  }
  private GrouperProvisioner grouperProvisioner = null;
  public GrouperProvisioningObjectLog(GrouperProvisioner grouperProvisioner) {
    this.grouperProvisioner = grouperProvisioner;
  }
  
  public void debug(String state) {
    if (LOG.isDebugEnabled()) {
      if (grouperProvisioner.retrieveProvisioningConfiguration().isLogAllObjectsVerbose()) {
        
        StringBuilder logMessage = new StringBuilder("Provisioner '").append(this.grouperProvisioner.getConfigId()).append("' object log after state: '").append(state).append("': ");
        logMessage.append(GrouperUtil.toStringForLog(this.grouperProvisioner.getDebugMap()));
        if (StringUtils.equals("retrieveAllData", state)) {
          appendProvisioningObjects(logMessage, "Grouper provisioning", this.grouperProvisioner.getGrouperProvisioningData().getGrouperProvisioningObjects());
          appendProvisioningObjects(logMessage, "Target provisioning", this.grouperProvisioner.getGrouperProvisioningData().getTargetProvisioningObjects());
        }
        if (StringUtils.equals("translateGrouperToCommon", state)) {
          appendProvisioningObjects(logMessage, "Grouper common", this.grouperProvisioner.getGrouperProvisioningData().getGrouperCommonObjects());
        }
        if (StringUtils.equals("translateTargetToCommon", state)) {
          appendProvisioningObjects(logMessage, "Target common", this.grouperProvisioner.getGrouperProvisioningData().getTargetCommonObjects());
        }
        if (StringUtils.equals("compareCommonObjects", state)) {
          appendProvisioningObjects(logMessage, "Common inserts", this.grouperProvisioner.getGrouperProvisioningData().getCommonObjectInserts());
          appendProvisioningObjects(logMessage, "Common updates", this.grouperProvisioner.getGrouperProvisioningData().getCommonObjectUpdates());
          appendProvisioningObjects(logMessage, "Common deletes", this.grouperProvisioner.getGrouperProvisioningData().getCommonObjectDeletes());
        }
        if (StringUtils.equals("translateCommonToTarget", state)) {
          appendProvisioningObjects(logMessage, "Target inserts", this.grouperProvisioner.getGrouperProvisioningData().getTargetObjectInserts());
          appendProvisioningObjects(logMessage, "Target updates", this.grouperProvisioner.getGrouperProvisioningData().getTargetObjectUpdates());
          appendProvisioningObjects(logMessage, "Target deletes", this.grouperProvisioner.getGrouperProvisioningData().getTargetObjectDeletes());
        }
        if (StringUtils.equals("translateCommonToTarget", state)) {
          
        }
        if (logMessage.charAt(logMessage.length()-1) == '\n') {
          logMessage.setLength(logMessage.length() - 1);
        }
        LOG.debug(logMessage);
      }
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
