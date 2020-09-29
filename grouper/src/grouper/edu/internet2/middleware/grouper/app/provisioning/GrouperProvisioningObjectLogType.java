package edu.internet2.middleware.grouper.app.provisioning;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import edu.internet2.middleware.grouper.util.GrouperUtil;

public enum GrouperProvisioningObjectLogType {
  configure {

    @Override
    void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog, GrouperProvisioner grouperProvisioner, StringBuilder logMessage) {
      appendProvisioner(grouperProvisioner, logMessage, "Provisioner");
      appendConfiguration(grouperProvisioner, logMessage, "Configuration");
      appendTargetDaoCapabilities(grouperProvisioner, logMessage, "Target Dao Capabilities");
      appendTargetDaoBehaviors(grouperProvisioner, logMessage, "Provisioner Behaviors");
      
    }
  }, 
  retrieveAllDataFromGrouperAndTarget {

    @Override
    void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog,
        GrouperProvisioner grouperProvisioner, StringBuilder logMessage) {
      appendProvisioningObjects(grouperProvisioner, logMessage, "Grouper provisioning", grouperProvisioner.retrieveGrouperProvisioningData().getGrouperProvisioningObjects());
      appendProvisioningObjects(grouperProvisioner, logMessage, "Target provisioning", grouperProvisioner.retrieveGrouperProvisioningData().getTargetProvisioningObjects());
      
    }
  }, 
  targetAttributeManipulation {

    @Override
    void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog,
        GrouperProvisioner grouperProvisioner, StringBuilder logMessage) {
      appendProvisioningObjects(grouperProvisioner, logMessage, "Target provisioning", grouperProvisioner.retrieveGrouperProvisioningData().getTargetProvisioningObjects());    
    }
  }, 
  retrieveIncrementalDataFromGrouper {

    @Override
    void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog,
        GrouperProvisioner grouperProvisioner, StringBuilder logMessage) {
      appendProvisioningObjects(grouperProvisioner, logMessage, "Grouper provisioning", grouperProvisioner.retrieveGrouperProvisioningData().getGrouperProvisioningObjects());
      
    }
  }, 
  missingGroups {

    @Override
    void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog,
        GrouperProvisioner grouperProvisioner, StringBuilder logMessage) {
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Missing groups", grouperProvisioner.retrieveGrouperProvisioningData().getGrouperProvisioningObjectsMissing().getProvisioningGroups(), "groups");
      
    }
  }, 
  missingGroupsForCreate {

    @Override
    void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog,
        GrouperProvisioner grouperProvisioner, StringBuilder logMessage) {
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Missing groups for create", grouperProvisioner.retrieveGrouperProvisioningData().getGrouperProvisioningObjectsMissing().getProvisioningGroups(), "groups");

    }
  }, 
  missingGrouperTargetGroups {

    @Override
    void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog,
        GrouperProvisioner grouperProvisioner, StringBuilder logMessage) {
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Missing grouper target groups", grouperProvisioner.retrieveGrouperProvisioningData().getGrouperTargetObjectsMissing().getProvisioningGroups(), "groups");

    }
  }, 
  missingTargetGroupsRetrieved {

    @Override
    void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog,
        GrouperProvisioner grouperProvisioner, StringBuilder logMessage) {
      
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Missing target groups retrieved", grouperProvisioner.retrieveGrouperProvisioningData().getTargetProvisioningObjectsMissingRetrieved().getProvisioningGroups(), "groups");
    }
  },
  missingGrouperTargetGroupsForCreate {

    @Override
    void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog,
        GrouperProvisioner grouperProvisioner, StringBuilder logMessage) {
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Missing grouper target groups for create", grouperProvisioner.retrieveGrouperProvisioningData().getGrouperTargetObjectsMissing().getProvisioningGroups(), "groups");
      
    }
  }, 
  missingTargetGroupsCreated {

    @Override
    void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog,
        GrouperProvisioner grouperProvisioner, StringBuilder logMessage) {
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Missing target groups created", grouperProvisioner.retrieveGrouperProvisioningData().getTargetProvisioningObjectsMissingCreated().getProvisioningGroups(), "groups");

    }
  }, 
  missingEntities {

    @Override
    void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog,
        GrouperProvisioner grouperProvisioner, StringBuilder logMessage) {
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Missing entities", grouperProvisioner.retrieveGrouperProvisioningData().getGrouperProvisioningObjectsMissing().getProvisioningEntities(), "entities");
      
    }
  }, 
  missingEntitiesForCreate {

    @Override
    void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog,
        GrouperProvisioner grouperProvisioner, StringBuilder logMessage) {
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Missing entities for create", grouperProvisioner.retrieveGrouperProvisioningData().getGrouperProvisioningObjectsMissing().getProvisioningEntities(), "entities");
      
    }
  }, 
  missingTargetEntities {

    @Override
    void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog,
        GrouperProvisioner grouperProvisioner, StringBuilder logMessage) {
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Missing target entities", grouperProvisioner.retrieveGrouperProvisioningData().getGrouperTargetObjectsMissing().getProvisioningEntities(), "entities");
      
    }
  }, 
  missingTargetEntitiesRetrieved {

    @Override
    void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog,
        GrouperProvisioner grouperProvisioner, StringBuilder logMessage) {
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Missing target entities retrieved", grouperProvisioner.retrieveGrouperProvisioningData().getTargetProvisioningObjectsMissingRetrieved().getProvisioningEntities(), "entities");
 
    }
  }, 
  missingGrouperTargetEntitiesForCreate {

    @Override
    void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog,
        GrouperProvisioner grouperProvisioner, StringBuilder logMessage) {
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Missing grouper target entities for create", grouperProvisioner.retrieveGrouperProvisioningData().getGrouperTargetObjectsMissing().getProvisioningEntities(), "entities");

    }
  }, 
  missingTargetEntitiesCreated {

    @Override
    void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog,
        GrouperProvisioner grouperProvisioner, StringBuilder logMessage) {
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Missing target entities created", grouperProvisioner.retrieveGrouperProvisioningData().getTargetProvisioningObjectsMissingCreated().getProvisioningEntities(), "entities");

    }
  }, 
  linkData {

    @Override
    void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog,
        GrouperProvisioner grouperProvisioner, StringBuilder logMessage) {

      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Grouper target objects changed in link", 
          grouperProvisioner.retrieveGrouperProvisioningData().getGrouperTargetObjectsChangedInLink().getProvisioningGroups(), "groups");
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Grouper target objects changed in link", 
          grouperProvisioner.retrieveGrouperProvisioningData().getGrouperTargetObjectsChangedInLink().getProvisioningEntities(), "entities");
      appendSyncObjects(grouperProvisioner, logMessage, "Sync objects");
      
    }
  },
  retrieveSubjectLink {

    @Override
    void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog,
        GrouperProvisioner grouperProvisioner, StringBuilder logMessage) {
      appendSyncObjectsOfType(grouperProvisioner, logMessage, "Sync objects", grouperProvisioner.retrieveGrouperProvisioningData().getMemberUuidToSyncMember(), "members");

    }
  }, 
  translateGrouperGroupsEntitiesToTarget {

    @Override
    void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog,
        GrouperProvisioner grouperProvisioner, StringBuilder logMessage) {
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Grouper target", grouperProvisioner.retrieveGrouperProvisioningData().getGrouperTargetObjects().getProvisioningGroups(), "groups");
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Grouper target", grouperProvisioner.retrieveGrouperProvisioningData().getGrouperTargetObjects().getProvisioningEntities(), "entities");

    }
  }, 
  translateGrouperMembershipsToTarget {

    @Override
    void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog,
        GrouperProvisioner grouperProvisioner, StringBuilder logMessage) {
      if (grouperProvisioner.retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.groupAttributes) {

        appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Grouper target", grouperProvisioner.retrieveGrouperProvisioningData().getGroupTargetIdToProvisioningGroupWrapper().values(), "grouperTargetGroup", "groups");

      } else if (grouperProvisioner.retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.entityAttributes) {

        appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Grouper target", grouperProvisioner.retrieveGrouperProvisioningData().getGrouperTargetObjects().getProvisioningEntities(), "grouperTargetEntity", "entities");

      } else {
        appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Grouper target", grouperProvisioner.retrieveGrouperProvisioningData().getGrouperTargetObjects().getProvisioningMemberships(), "memberships");
      }

    }
  }, 
  compareTargetObjects {

    @Override
    void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog,
        GrouperProvisioner grouperProvisioner, StringBuilder logMessage) {
      appendProvisioningObjects(grouperProvisioner, logMessage, "Target inserts", grouperProvisioner.retrieveGrouperProvisioningData().getTargetObjectInserts());
      appendProvisioningObjects(grouperProvisioner, logMessage, "Target updates", grouperProvisioner.retrieveGrouperProvisioningData().getTargetObjectUpdates());
      appendProvisioningObjects(grouperProvisioner, logMessage, "Target deletes", grouperProvisioner.retrieveGrouperProvisioningData().getTargetObjectDeletes());

    }
  }, 
  targetIdGrouperGroupsEntities {

    @Override
    void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog,
        GrouperProvisioner grouperProvisioner, StringBuilder logMessage) {
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Grouper target", grouperProvisioner.retrieveGrouperProvisioningData().getGrouperTargetObjects().getProvisioningGroups(), "groups");
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Grouper target", grouperProvisioner.retrieveGrouperProvisioningData().getGrouperTargetObjects().getProvisioningEntities(), "entities");
      
    }
  }, 
  targetIdGrouperMemberships {

    @Override
    void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog,
        GrouperProvisioner grouperProvisioner, StringBuilder logMessage) {
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Grouper target", grouperProvisioner.retrieveGrouperProvisioningData().getGrouperTargetObjects().getProvisioningMemberships(), "memberships");

    }
  }, 
  targetIdTargetObjects {

    @Override
    void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog,
        GrouperProvisioner grouperProvisioner, StringBuilder logMessage) {
      appendProvisioningObjects(grouperProvisioner, logMessage, "Target provisioning", grouperProvisioner.retrieveGrouperProvisioningData().getTargetProvisioningObjects());
      
    }
  }, 
  targetIdGrouperObjects {

    @Override
    void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog,
        GrouperProvisioner grouperProvisioner, StringBuilder logMessage) {
      appendProvisioningObjects(grouperProvisioner, logMessage, "Grouper target", grouperProvisioner.retrieveGrouperProvisioningData().getGrouperTargetObjects());

    }
  };

  abstract void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog, GrouperProvisioner grouperProvisioner, StringBuilder logMessage);

  private static void appendConfiguration(GrouperProvisioner grouperProvisioner, StringBuilder logMessage, String label) {
    if (logMessage.charAt(logMessage.length()-1) != '\n') {
      logMessage.append("\n");
    }
    logMessage.append(label).append(" (");
    logMessage.append(grouperProvisioner.retrieveGrouperProvisioningConfiguration().toString());
    logMessage.append(")\n");
  }

  private static void appendProvisioner(GrouperProvisioner grouperProvisioner, StringBuilder logMessage, String label) {
    if (logMessage.charAt(logMessage.length()-1) != '\n') {
      logMessage.append("\n");
    }
    logMessage.append(label).append(" (");
    logMessage.append(grouperProvisioner.toString());
    logMessage.append(")\n");
  }

  /**
   * 
   * @param string
   * @param grouperProvisioningObjects
   */
  private static void appendProvisioningObjects(GrouperProvisioner grouperProvisioner, StringBuilder logMessage, String label,
      GrouperProvisioningLists grouperProvisioningObjects) {
    appendProvisioningObjectsOfType(grouperProvisioner, logMessage, label, grouperProvisioningObjects.getProvisioningGroups(), "groups");
    appendProvisioningObjectsOfType(grouperProvisioner, logMessage, label, grouperProvisioningObjects.getProvisioningEntities(), "entities");
    appendProvisioningObjectsOfType(grouperProvisioner, logMessage, label, grouperProvisioningObjects.getProvisioningMemberships(), "memberships");
  }

  private static void appendProvisioningObjectsOfType(GrouperProvisioner grouperProvisioner, StringBuilder logMessage, String label,
      Collection beans, String field, String type) {
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
      if ("grouperTargetGroup".equals(field)) {
        bean = ((ProvisioningGroupWrapper)bean).getGrouperTargetGroup();
      } else if ("grouperTargetEntity".equals(field)) {
        bean = ((ProvisioningEntityWrapper)bean).getGrouperTargetEntity();
      } else if (field != null) {
        throw new RuntimeException("Not expecting field '" + field + "'");
      }
      logMessage.append(objectCount).append(". ").append(bean == null ? "null" : bean.toString()).append("\n");
      if (objectCount >= 10) {
        break;
      }
      objectCount++;
    }
  }

  private static void appendProvisioningObjectsOfType(GrouperProvisioner grouperProvisioner, StringBuilder logMessage, String label,
      List beans, String type) {
    appendProvisioningObjectsOfType(grouperProvisioner, logMessage, label,
        beans, null, type);
  }

  /**
   * 
   * @param string
   * @param grouperProvisioningObjects
   */
  private static void appendSyncObjects(GrouperProvisioner grouperProvisioner, StringBuilder logMessage, String label) {
    appendSyncObjectsOfType(grouperProvisioner, logMessage, label, grouperProvisioner.retrieveGrouperProvisioningData().getGroupUuidToSyncGroup(), "groups");
    appendSyncObjectsOfType(grouperProvisioner, logMessage, label, grouperProvisioner.retrieveGrouperProvisioningData().getMemberUuidToSyncMember(), "members");
    appendSyncObjectsOfType(grouperProvisioner, logMessage, label, grouperProvisioner.retrieveGrouperProvisioningData().getGroupUuidMemberUuidToSyncMembership(), "memberships");
  }

  private static void appendSyncObjectsOfType(GrouperProvisioner grouperProvisioner, StringBuilder logMessage, String label,
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
      if (objectCount > 10) {
        break;
      }
      logMessage.append(objectCount).append(". ").append(bean == null ? "null" : bean.toString()).append("\n");
      objectCount++;
    }
  }

  private static void appendTargetDaoBehaviors(GrouperProvisioner grouperProvisioner, StringBuilder logMessage, String label) {
    if (logMessage.charAt(logMessage.length()-1) != '\n') {
      logMessage.append("\n");
    }
    logMessage.append(label).append(" (");
    logMessage.append(grouperProvisioner.retrieveGrouperProvisioningBehavior().toString());
    logMessage.append(")\n");
  }

  private static void appendTargetDaoCapabilities(GrouperProvisioner grouperProvisioner, StringBuilder logMessage, String label) {
    if (logMessage.charAt(logMessage.length()-1) != '\n') {
      logMessage.append("\n");
    }
    logMessage.append(label).append(" (");
    logMessage.append(grouperProvisioner.retrieveGrouperTargetDaoAdapter().getWrappedDao().getGrouperProvisionerDaoCapabilities().toString());
    logMessage.append(")\n");
  }

}
