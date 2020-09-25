package edu.internet2.middleware.grouper.app.provisioning;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;

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
    if (!grouperProvisioner.retrieveGrouperProvisioningConfiguration().isLogAllObjectsVerbose()) {
      return;
    }
    StringBuilder logMessage = new StringBuilder("Provisioner '").append(this.grouperProvisioner.getConfigId())
        .append("' state '").append(state)
        .append("' type '").append(this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGrouperProvisioningType())
        .append("': ");
    logMessage.append(GrouperUtil.toStringForLog(this.grouperProvisioner.getDebugMap()));
    if (StringUtils.equals("retrieveDataPass1", state)) {
      appendProvisioningObjects(logMessage, "Grouper provisioning", this.grouperProvisioner.retrieveGrouperProvisioningData().getGrouperProvisioningObjects());
    }
    if ((StringUtils.equals("retrieveDataPass2", state) && this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGrouperProvisioningType().isIncrementalSync()) || 
        ((StringUtils.equals("retrieveDataPass1", state) && !this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGrouperProvisioningType().isIncrementalSync()))) {
      appendProvisioningObjects(logMessage, "Target provisioning", this.grouperProvisioner.retrieveGrouperProvisioningData().getTargetProvisioningObjects());
    }
    if (StringUtils.equals("incrementalMissingGroups", state)) {
      appendProvisioningObjectsOfType(logMessage, "Incremental missing groups", this.grouperProvisioner.retrieveGrouperProvisioningData().getGrouperProvisioningObjectsMissing().getProvisioningGroups(), "groups");
    } else if (StringUtils.equals("incrementalMissingGroupsForCreate", state)) {
      appendProvisioningObjectsOfType(logMessage, "Incremental missing groups for create", this.grouperProvisioner.retrieveGrouperProvisioningData().getGrouperProvisioningObjectsCreatedPass1().getProvisioningGroups(), "groups");
    } else if (StringUtils.equals("incrementalMissingTargetGroups", state)) {
      appendProvisioningObjectsOfType(logMessage, "Incremental missing target groups", this.grouperProvisioner.retrieveGrouperProvisioningData().getGrouperTargetObjectsMissing().getProvisioningGroups(), "groups");
    } else if (StringUtils.equals("incrementalMissingTargetGroupsRetrieved", state)) {
      appendProvisioningObjectsOfType(logMessage, "Incremental missing target groups retrieved", this.grouperProvisioner.retrieveGrouperProvisioningData().getTargetProvisioningObjectsMissingRetrieved().getProvisioningGroups(), "groups");
    } else if (StringUtils.equals("incrementalMissingTargetGroupsForCreate", state)) {
      appendProvisioningObjectsOfType(logMessage, "Incremental missing grouper target groups for create", this.grouperProvisioner.retrieveGrouperProvisioningData().getGrouperTargetObjectsMissingForCreate().getProvisioningGroups(), "groups");
    } else if (StringUtils.equals("incrementalMissingTargetGroupsCreated", state)) {
      appendProvisioningObjectsOfType(logMessage, "Incremental missing target groups created", this.grouperProvisioner.retrieveGrouperProvisioningData().getTargetProvisioningObjectsMissingCreated().getProvisioningGroups(), "groups");
    } else if (StringUtils.equals("incrementalMissingEntities", state)) {
      appendProvisioningObjectsOfType(logMessage, "Incremental missing groups", this.grouperProvisioner.retrieveGrouperProvisioningData().getGrouperProvisioningObjectsMissing().getProvisioningEntities(), "entities");
    } else if (StringUtils.equals("incrementalMissingEntitiesForCreate", state)) {
      appendProvisioningObjectsOfType(logMessage, "Incremental missing groups for create", this.grouperProvisioner.retrieveGrouperProvisioningData().getGrouperProvisioningObjectsCreatedPass1().getProvisioningEntities(), "entities");
    } else if (StringUtils.equals("incrementalMissingTargetEntities", state)) {
      appendProvisioningObjectsOfType(logMessage, "Incremental missing target groups", this.grouperProvisioner.retrieveGrouperProvisioningData().getGrouperTargetObjectsMissing().getProvisioningEntities(), "entities");
    } else if (StringUtils.equals("incrementalMissingTargetEntitiesRetrieved", state)) {
      appendProvisioningObjectsOfType(logMessage, "Incremental missing target groups retrieved", this.grouperProvisioner.retrieveGrouperProvisioningData().getTargetProvisioningObjectsMissingRetrieved().getProvisioningEntities(), "entities");
    } else if (StringUtils.equals("incrementalMissingTargetEntitiesForCreate", state)) {
      appendProvisioningObjectsOfType(logMessage, "Incremental missing grouper target groups for create", this.grouperProvisioner.retrieveGrouperProvisioningData().getGrouperTargetObjectsMissingForCreate().getProvisioningEntities(), "entities");
    } else if (StringUtils.equals("incrementalMissingTargetEntitiesCreated", state)) {
      appendProvisioningObjectsOfType(logMessage, "Incremental missing target groups created", this.grouperProvisioner.retrieveGrouperProvisioningData().getTargetProvisioningObjectsMissingCreated().getProvisioningEntities(), "entities");

    } else if (StringUtils.equals("linkData", state)) {
      appendSyncObjects(logMessage, "Sync objects");
    } else if (StringUtils.equals("retrieveSubjectLink", state)) {
      appendSyncObjectsOfType(logMessage, "Sync objects", this.grouperProvisioner.retrieveGrouperProvisioningData().getMemberUuidToSyncMember(), "members");
    } else if (StringUtils.equals("translateGrouperToTarget", state)) {
      appendProvisioningObjects(logMessage, "Grouper target", this.grouperProvisioner.retrieveGrouperProvisioningData().getGrouperTargetObjects());
    } else if (StringUtils.equals("compareTargetObjects", state)) {
      appendProvisioningObjects(logMessage, "Target inserts", this.grouperProvisioner.retrieveGrouperProvisioningData().getTargetObjectInserts());
      appendProvisioningObjects(logMessage, "Target updates", this.grouperProvisioner.retrieveGrouperProvisioningData().getTargetObjectUpdates());
      appendProvisioningObjects(logMessage, "Target deletes", this.grouperProvisioner.retrieveGrouperProvisioningData().getTargetObjectDeletes());
    } else if (StringUtils.equals("targetIdTargetObjects", state)) {
      appendProvisioningObjects(logMessage, "Grouper target", this.grouperProvisioner.retrieveGrouperProvisioningData().getGrouperTargetObjects());
      appendProvisioningObjects(logMessage, "Target provisioning", this.grouperProvisioner.retrieveGrouperProvisioningData().getTargetProvisioningObjects());
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

  /**
   * 
   * @param string
   * @param grouperProvisioningObjects
   */
  private void appendSyncObjects(StringBuilder logMessage, String label) {
    appendSyncObjectsOfType(logMessage, label, this.grouperProvisioner.retrieveGrouperProvisioningData().getGroupUuidToSyncGroup(), "groups");
    appendSyncObjectsOfType(logMessage, label, this.grouperProvisioner.retrieveGrouperProvisioningData().getMemberUuidToSyncMember(), "members");
    appendSyncObjectsOfType(logMessage, label, this.grouperProvisioner.retrieveGrouperProvisioningData().getGroupUuidMemberUuidToSyncMembership(), "memberships");
  }

  private void appendSyncObjectsOfType(StringBuilder logMessage, String label,
      Map idToSyncObject, String type) {
    if (logMessage.charAt(logMessage.length()-1) != '\n') {
      logMessage.append("\n");
    }
    logMessage.append(label).append(" ").append(type).append(" (")
      .append(GrouperUtil.length(idToSyncObject)).append(")");
    if (GrouperUtil.length(idToSyncObject) == 0) {
      return;
    }
    logMessage.append(":\n");
    int objectCount = 0;
    for (Object id : GrouperUtil.nonNull(idToSyncObject).keySet()) {
      Object bean = idToSyncObject.get(id);
      if (objectCount++ > 10) {
        logMessage.append(objectCount).append(". ").append(bean == null ? "null" : bean.toString()).append("\n");
        break;
      }
      logMessage.append(objectCount).append(". ").append(bean == null ? "null" : bean.toString()).append("\n");
    }
  }

  private void appendProvisioningObjectsOfType(StringBuilder logMessage, String label,
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
