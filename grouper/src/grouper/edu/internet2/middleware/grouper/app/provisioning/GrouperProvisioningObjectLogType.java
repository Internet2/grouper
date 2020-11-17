package edu.internet2.middleware.grouper.app.provisioning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMembership;

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
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Grouper provisioning", grouperProvisioner.retrieveGrouperProvisioningData().retrieveGrouperProvisioningGroups(), "groups");
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Grouper provisioning", grouperProvisioner.retrieveGrouperProvisioningData().retrieveGrouperProvisioningEntities(), "entities");
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Grouper provisioning", grouperProvisioner.retrieveGrouperProvisioningData().retrieveGrouperProvisioningMemberships(), "memberships");

      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Target provisioning", grouperProvisioner.retrieveGrouperProvisioningData().retrieveTargetProvisioningGroups(), "groups");
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Target provisioning", grouperProvisioner.retrieveGrouperProvisioningData().retrieveTargetProvisioningEntities(), "entities");
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Target provisioning", grouperProvisioner.retrieveGrouperProvisioningData().retrieveTargetProvisioningMemberships(), "memberships");

      appendSyncObjects(grouperProvisioner, logMessage, "Sync objects");
    }
  }, 
  targetAttributeManipulation {

    @Override
    void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog,
        GrouperProvisioner grouperProvisioner, StringBuilder logMessage) {
      
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Target provisioning", grouperProvisioner.retrieveGrouperProvisioningData().retrieveTargetProvisioningGroups(), "groups");
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Target provisioning", grouperProvisioner.retrieveGrouperProvisioningData().retrieveTargetProvisioningEntities(), "entities");
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Target provisioning", grouperProvisioner.retrieveGrouperProvisioningData().retrieveTargetProvisioningMemberships(), "memberships");

    }
  }, 
  retrieveIncrementalDataFromGrouper {

    @Override
    void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog,
        GrouperProvisioner grouperProvisioner, StringBuilder logMessage) {
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Grouper provisioning", grouperProvisioner.retrieveGrouperProvisioningData().retrieveGrouperProvisioningGroups(), "groups");
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Grouper provisioning", grouperProvisioner.retrieveGrouperProvisioningData().retrieveGrouperProvisioningEntities(), "entities");
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Grouper provisioning", grouperProvisioner.retrieveGrouperProvisioningData().retrieveGrouperProvisioningMemberships(), "memberships");
      
    }
  }, 
  missingGroups {

    @Override
    void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog,
        GrouperProvisioner grouperProvisioner, StringBuilder logMessage) {
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Missing groups", grouperProvisioner.retrieveGrouperProvisioningDataGrouper().getGrouperProvisioningObjectsMissing().getProvisioningGroups(), "groups");
      
    }
  }, 
  missingGroupsForCreate {

    @Override
    void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog,
        GrouperProvisioner grouperProvisioner, StringBuilder logMessage) {
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Missing groups for create", grouperProvisioner.retrieveGrouperProvisioningDataGrouper().getGrouperProvisioningObjectsMissing().getProvisioningGroups(), "groups");

    }
  }, 
  missingGrouperTargetGroups {

    @Override
    void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog,
        GrouperProvisioner grouperProvisioner, StringBuilder logMessage) {
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Missing grouper target groups", grouperProvisioner.retrieveGrouperProvisioningDataChanges().getGrouperTargetObjectsMissing().getProvisioningGroups(), "groups");

    }
  }, 
  missingTargetGroupsRetrieved {

    @Override
    void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog,
        GrouperProvisioner grouperProvisioner, StringBuilder logMessage) {
      
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Missing target groups retrieved", grouperProvisioner.retrieveGrouperProvisioningDataTarget().getTargetProvisioningObjectsMissingRetrieved().getProvisioningGroups(), "groups");
    }
  },
  missingGrouperTargetGroupsForCreate {

    @Override
    void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog,
        GrouperProvisioner grouperProvisioner, StringBuilder logMessage) {
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Missing grouper target groups for create", grouperProvisioner.retrieveGrouperProvisioningDataChanges().getGrouperTargetObjectsMissing().getProvisioningGroups(), "groups");
      
    }
  }, 
  missingTargetGroupsCreated {

    @Override
    void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog,
        GrouperProvisioner grouperProvisioner, StringBuilder logMessage) {
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Missing target groups created", grouperProvisioner.retrieveGrouperProvisioningDataTarget().getTargetProvisioningObjectsMissingCreated().getProvisioningGroups(), "groups");

    }
  }, 
  missingEntities {

    @Override
    void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog,
        GrouperProvisioner grouperProvisioner, StringBuilder logMessage) {
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Missing entities", grouperProvisioner.retrieveGrouperProvisioningDataGrouper().getGrouperProvisioningObjectsMissing().getProvisioningEntities(), "entities");
      
    }
  }, 
  missingEntitiesForCreate {

    @Override
    void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog,
        GrouperProvisioner grouperProvisioner, StringBuilder logMessage) {
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Missing entities for create", grouperProvisioner.retrieveGrouperProvisioningDataGrouper().getGrouperProvisioningObjectsMissing().getProvisioningEntities(), "entities");
      
    }
  }, 
  missingTargetEntities {

    @Override
    void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog,
        GrouperProvisioner grouperProvisioner, StringBuilder logMessage) {
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Missing target entities", grouperProvisioner.retrieveGrouperProvisioningDataChanges().getGrouperTargetObjectsMissing().getProvisioningEntities(), "entities");
      
    }
  }, 
  missingTargetEntitiesRetrieved {

    @Override
    void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog,
        GrouperProvisioner grouperProvisioner, StringBuilder logMessage) {
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Missing target entities retrieved", grouperProvisioner.retrieveGrouperProvisioningDataTarget().getTargetProvisioningObjectsMissingRetrieved().getProvisioningEntities(), "entities");
 
    }
  }, 
  missingGrouperTargetEntitiesForCreate {

    @Override
    void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog,
        GrouperProvisioner grouperProvisioner, StringBuilder logMessage) {
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Missing grouper target entities for create", grouperProvisioner.retrieveGrouperProvisioningDataChanges().getGrouperTargetObjectsMissing().getProvisioningEntities(), "entities");

    }
  }, 
  missingTargetEntitiesCreated {

    @Override
    void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog,
        GrouperProvisioner grouperProvisioner, StringBuilder logMessage) {
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Missing target entities created", grouperProvisioner.retrieveGrouperProvisioningDataTarget().getTargetProvisioningObjectsMissingCreated().getProvisioningEntities(), "entities");

    }
  }, 
  linkData {

    @Override
    void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog,
        GrouperProvisioner grouperProvisioner, StringBuilder logMessage) {

      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Grouper target objects changed in link", 
          grouperProvisioner.retrieveGrouperProvisioningDataGrouperTarget().getGrouperTargetObjectsChangedInLink().getProvisioningGroups(), "groups");
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Grouper target objects changed in link", 
          grouperProvisioner.retrieveGrouperProvisioningDataGrouperTarget().getGrouperTargetObjectsChangedInLink().getProvisioningEntities(), "entities");
      appendSyncObjects(grouperProvisioner, logMessage, "Sync objects");
      
    }
  },
  retrieveSubjectLink {

    @Override
    void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog,
        GrouperProvisioner grouperProvisioner, StringBuilder logMessage) {
      // TODO this is not the list of subject links...
      //appendSyncObjectsOfTypeEntity(grouperProvisioner, logMessage, "Sync objects", grouperProvisioner.retrieveGrouperProvisioningDataIndex().getMemberUuidToProvisioningEntityWrapper(), "members");

    }
  }, 
  manipulateGrouperTargetAttributes {

    @Override
    void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog,
        GrouperProvisioner grouperProvisioner, StringBuilder logMessage) {
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Grouper target", grouperProvisioner.retrieveGrouperProvisioningData().retrieveGrouperTargetGroups(), "groups");
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Grouper target", grouperProvisioner.retrieveGrouperProvisioningData().retrieveGrouperTargetEntities(), "entities");

    }
  }, 
  translateGrouperGroupsEntitiesToTarget {

    @Override
    void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog,
        GrouperProvisioner grouperProvisioner, StringBuilder logMessage) {
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Grouper target", grouperProvisioner.retrieveGrouperProvisioningData().retrieveGrouperTargetGroups(), "groups");
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Grouper target", grouperProvisioner.retrieveGrouperProvisioningData().retrieveGrouperTargetEntities(), "entities");

    }
  }, 
  translateGrouperMembershipsToTarget {

    @Override
    void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog,
        GrouperProvisioner grouperProvisioner, StringBuilder logMessage) {
      if (grouperProvisioner.retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.groupAttributes) {

        appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Grouper target", grouperProvisioner.retrieveGrouperProvisioningData().retrieveGrouperTargetGroups(), "grouperTargetGroup", "groups");

      } else if (grouperProvisioner.retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.entityAttributes) {

        appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Grouper target", grouperProvisioner.retrieveGrouperProvisioningData().retrieveGrouperProvisioningEntities(), "grouperTargetEntity", "entities");

      } else {
        appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Grouper target", grouperProvisioner.retrieveGrouperProvisioningData().retrieveGrouperProvisioningMemberships(), "memberships");
      }

    }
  }, 
  compareTargetObjects {

    @Override
    void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog,
        GrouperProvisioner grouperProvisioner, StringBuilder logMessage) {
      appendProvisioningObjects(grouperProvisioner, logMessage, "Target inserts", grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectInserts());
      appendProvisioningObjects(grouperProvisioner, logMessage, "Target updates", grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectUpdates());
      appendProvisioningObjects(grouperProvisioner, logMessage, "Target deletes", grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectDeletes());

    }
  }, 
  matchingIdGrouperGroupsEntities {

    @Override
    void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog,
        GrouperProvisioner grouperProvisioner, StringBuilder logMessage) {
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Grouper target", grouperProvisioner.retrieveGrouperProvisioningData().retrieveGrouperTargetGroups(), "groups");
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Grouper target", grouperProvisioner.retrieveGrouperProvisioningData().retrieveGrouperTargetEntities(), "entities");
      
    }
  }, 
  matchingIdGrouperMemberships {

    @Override
    void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog,
        GrouperProvisioner grouperProvisioner, StringBuilder logMessage) {
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Grouper target", grouperProvisioner.retrieveGrouperProvisioningData().retrieveGrouperTargetMemberships(), "memberships");

    }
  }, 
  matchingIdTargetObjects {

    @Override
    void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog,
        GrouperProvisioner grouperProvisioner, StringBuilder logMessage) {
      
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Target provisioning", grouperProvisioner.retrieveGrouperProvisioningData().retrieveTargetProvisioningGroups(), "groups");
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Target provisioning", grouperProvisioner.retrieveGrouperProvisioningData().retrieveTargetProvisioningEntities(), "entities");
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Target provisioning", grouperProvisioner.retrieveGrouperProvisioningData().retrieveTargetProvisioningMemberships(), "memberships");

      
    }
  },
  matchingIdGrouperObjects {

    @Override
    void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog,
        GrouperProvisioner grouperProvisioner, StringBuilder logMessage) {
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Grouper target", grouperProvisioner.retrieveGrouperProvisioningData().retrieveGrouperTargetGroups(), "groups");
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Grouper target", grouperProvisioner.retrieveGrouperProvisioningData().retrieveGrouperTargetEntities(), "entities");
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Grouper target", grouperProvisioner.retrieveGrouperProvisioningData().retrieveGrouperTargetMemberships(), "entities");

    }
  }, 
  
  logIncomingData {

    @Override
    void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog,
        GrouperProvisioner grouperProvisioner, StringBuilder logMessage) {
      appendIncomingData(grouperProvisioner, logMessage, "Incoming data", "(with recalc from target)",     
          grouperProvisioner.retrieveGrouperProvisioningDataIncrementalInput().getGrouperIncrementalDataToProcessWithRecalc());
      appendIncomingData(grouperProvisioner, logMessage, "Incoming data", "(without recalc from target)",     
          grouperProvisioner.retrieveGrouperProvisioningDataIncrementalInput().getGrouperIncrementalDataToProcessWithoutRecalc());
      
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
      if ((!(bean instanceof ProvisioningGroup)) && (!(bean instanceof ProvisioningEntity))  && (!(bean instanceof ProvisioningMembership))) {
        if ("grouperTargetGroup".equals(field)) {
          bean = ((ProvisioningGroupWrapper)bean).getGrouperTargetGroup();
        } else if ("grouperTargetEntity".equals(field)) {
          bean = ((ProvisioningEntityWrapper)bean).getGrouperTargetEntity();
        } else if (field != null) {
          throw new RuntimeException("Not expecting field '" + field + "'");
        }
      }
      logMessage.append(objectCount).append(". ").append(bean == null ? "null" : bean.toString()).append("\n");
      if (objectCount >= 10) {
        break;
      }
      objectCount++;
    }
  }

  private static void appendIncomingData(GrouperProvisioner grouperProvisioner, StringBuilder logMessage, String label,
      String type, GrouperIncrementalDataToProcess grouperIncrementalDataToProcess) {
    if (logMessage.charAt(logMessage.length()-1) != '\n') {
      logMessage.append("\n");
    }
    logMessage.append(label).append(" ").append(type).append(":\n");
    if (grouperIncrementalDataToProcess != null) {
      appendList(logMessage, "groupUuidsForGroupOnly", grouperIncrementalDataToProcess.getGroupUuidsForGroupOnly());
      appendList(logMessage, "groupUuidsForGroupMembershipSync", grouperIncrementalDataToProcess.getGroupUuidsForGroupMembershipSync());
      appendList(logMessage, "memberUuidsForEntityOnly", grouperIncrementalDataToProcess.getMemberUuidsForEntityOnly());
      appendList(logMessage, "memberUuidsForEntityMembershipSync", grouperIncrementalDataToProcess.getMemberUuidsForEntityMembershipSync());
      appendList(logMessage, "groupUuidsMemberUuidsFieldIdsForMembershipSync", grouperIncrementalDataToProcess.getGroupUuidsMemberUuidsFieldIdsForMembershipSync());
    }
  }
  private static void appendList(StringBuilder logMessage, String label, Collection<?> someList) {
    if (GrouperUtil.length(someList) > 0) {
      logMessage.append(" - ").append(label).append(" (").append(GrouperUtil.length(someList)).append("): ");
      int count = 0;
      for (Object item : someList) {
        if (count > 0) {
          logMessage.append(", ");
        }
        if (item instanceof String) {
          logMessage.append(item);
        } else if (item instanceof MultiKey) {
          MultiKey itemMultiKey = (MultiKey)item;
          logMessage.append("[");
          for (int i=0;i<itemMultiKey.size();i++) {
            if (i>0) {
              logMessage.append(",");
            }
            logMessage.append(itemMultiKey.getKey(i));
          }
          logMessage.append("]");
        }
        count++;
        if (count > 5) {
          break;
        }
      }
      logMessage.append("\n");
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
    appendSyncObjectsOfTypeGroup(grouperProvisioner, logMessage, label, grouperProvisioner.retrieveGrouperProvisioningDataIndex().getGroupUuidToProvisioningGroupWrapper(), "groups");
    appendSyncObjectsOfTypeEntity(grouperProvisioner, logMessage, label, grouperProvisioner.retrieveGrouperProvisioningDataIndex().getMemberUuidToProvisioningEntityWrapper(), "members");
    appendSyncObjectsOfTypeMembership(grouperProvisioner, logMessage, label, grouperProvisioner.retrieveGrouperProvisioningDataIndex().getGroupUuidMemberUuidToProvisioningMembershipWrapper(), "memberships");
  }

  private static void appendSyncObjectsOfTypeMembership(GrouperProvisioner grouperProvisioner, StringBuilder logMessage, String label,
      Map<MultiKey, ProvisioningMembershipWrapper> memberIdToWrapper, String type) {
    if (logMessage.charAt(logMessage.length()-1) != '\n') {
      logMessage.append("\n");
    }
    List<GcGrouperSyncMembership> gcGrouperSyncMemberships = new ArrayList<GcGrouperSyncMembership>();
    
    for (ProvisioningMembershipWrapper provisioningMembershipWrapper : GrouperUtil.nonNull(memberIdToWrapper).values()) {
      GcGrouperSyncMembership gcGrouperSyncMembership = provisioningMembershipWrapper.getGcGrouperSyncMembership();
      if (gcGrouperSyncMembership != null) {
        gcGrouperSyncMemberships.add(gcGrouperSyncMembership);
      }
    }
    
    logMessage.append(label).append(" ").append(type).append(" (")
      .append(GrouperUtil.length(gcGrouperSyncMemberships)).append(")");
    if (GrouperUtil.length(gcGrouperSyncMemberships) == 0) {
      return;
    }
    logMessage.append(":\n");
    int objectCount = 0;
    for (Object bean : GrouperUtil.nonNull(gcGrouperSyncMemberships)) {
      if (objectCount > 10) {
        break;
      }
      logMessage.append(objectCount).append(". ").append(bean == null ? "null" : bean.toString()).append("\n");
      objectCount++;
    }
  }


  private static void appendSyncObjectsOfTypeEntity(GrouperProvisioner grouperProvisioner, StringBuilder logMessage, String label,
      Map<String, ProvisioningEntityWrapper> memberIdToWrapper, String type) {
    if (logMessage.charAt(logMessage.length()-1) != '\n') {
      logMessage.append("\n");
    }
    List<GcGrouperSyncMember> gcGrouperSyncMembers = new ArrayList<GcGrouperSyncMember>();
    
    for (ProvisioningEntityWrapper provisioningEntityWrapper : GrouperUtil.nonNull(memberIdToWrapper).values()) {
      GcGrouperSyncMember gcGrouperSyncMember = provisioningEntityWrapper.getGcGrouperSyncMember();
      if (gcGrouperSyncMember != null) {
        gcGrouperSyncMembers.add(gcGrouperSyncMember);
      }
    }
    
    logMessage.append(label).append(" ").append(type).append(" (")
      .append(GrouperUtil.length(gcGrouperSyncMembers)).append(")");
    if (GrouperUtil.length(gcGrouperSyncMembers) == 0) {
      return;
    }
    logMessage.append(":\n");
    int objectCount = 0;
    for (Object bean : GrouperUtil.nonNull(gcGrouperSyncMembers)) {
      if (objectCount > 10) {
        break;
      }
      logMessage.append(objectCount).append(". ").append(bean == null ? "null" : bean.toString()).append("\n");
      objectCount++;
    }
  }

  private static void appendSyncObjectsOfTypeGroup(GrouperProvisioner grouperProvisioner, StringBuilder logMessage, String label,
      Map<String, ProvisioningGroupWrapper> groupIdToWrapper, String type) {
    if (logMessage.charAt(logMessage.length()-1) != '\n') {
      logMessage.append("\n");
    }
    List<GcGrouperSyncGroup> gcGrouperSyncGroups = new ArrayList<GcGrouperSyncGroup>();
    
    for (ProvisioningGroupWrapper provisioningGroupWrapper : GrouperUtil.nonNull(groupIdToWrapper).values()) {
      GcGrouperSyncGroup gcGrouperSyncGroup = provisioningGroupWrapper.getGcGrouperSyncGroup();
      if (gcGrouperSyncGroup != null) {
        gcGrouperSyncGroups.add(gcGrouperSyncGroup);
      }
    }
    
    logMessage.append(label).append(" ").append(type).append(" (")
      .append(GrouperUtil.length(gcGrouperSyncGroups)).append(")");
    if (GrouperUtil.length(gcGrouperSyncGroups) == 0) {
      return;
    }
    logMessage.append(":\n");
    int objectCount = 0;
    for (Object bean : GrouperUtil.nonNull(gcGrouperSyncGroups)) {
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
