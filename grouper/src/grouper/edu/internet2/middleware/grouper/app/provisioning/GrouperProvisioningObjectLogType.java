package edu.internet2.middleware.grouper.app.provisioning;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMembership;

public enum GrouperProvisioningObjectLogType {
  end {

    @Override
    void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog, GrouperProvisioner grouperProvisioner, StringBuilder logMessage, Object... data) {
      
    }
  }, 
  configure {

    @Override
    void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog, GrouperProvisioner grouperProvisioner, StringBuilder logMessage, Object... data) {
      appendProvisioner(grouperProvisioner, logMessage, "Provisioner");
      appendConfiguration(grouperProvisioner, logMessage, "Configuration");
      appendTargetDaoCapabilities(grouperProvisioner, logMessage, "Target Dao capabilities");
      appendTargetDaoBehaviors(grouperProvisioner, logMessage, "Provisioner behaviors");
      
    }
  }, 
  retrieveTargetData {

    @Override
    void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog,
        GrouperProvisioner grouperProvisioner, StringBuilder logMessage, Object... data) {

      GrouperProvisioningData grouperProvisioningData = grouperProvisioner.retrieveGrouperProvisioningData();
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Target provisioning", grouperProvisioningData.retrieveTargetProvisioningGroups(), "groups");
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Target provisioning", grouperProvisioningData.retrieveTargetProvisioningEntities(), "entities");
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Target provisioning", grouperProvisioningData.retrieveTargetProvisioningMemberships(), "memberships");

    }
  },
  retrieveIndividualTargetGroupsAndEntities {

    @Override
    void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog,
        GrouperProvisioner grouperProvisioner, StringBuilder logMessage, Object... data) {

      GrouperProvisioningData grouperProvisioningData = grouperProvisioner.retrieveGrouperProvisioningData();
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Target provisioning", grouperProvisioningData.retrieveTargetProvisioningGroups(), "groups");
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Target provisioning", grouperProvisioningData.retrieveTargetProvisioningEntities(), "entities");

    }
  }, 
  retrieveIndividualTargetMemberships {

    @Override
    void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog,
        GrouperProvisioner grouperProvisioner, StringBuilder logMessage, Object... data) {

      GrouperProvisioningData grouperProvisioningData = grouperProvisioner.retrieveGrouperProvisioningData();
      if (grouperProvisioner.retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.groupAttributes) {

        appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Target provisioning", grouperProvisioningData.retrieveTargetProvisioningGroups(), "groups");

      } else if (grouperProvisioner.retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.entityAttributes) {

        appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Target provisioning", grouperProvisioningData.retrieveTargetProvisioningEntities(), "entities");

      } else {

        appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Target provisioning", grouperProvisioningData.retrieveTargetProvisioningMemberships(), "memberships");
      }

    }
  }, 
  retrieveTargetDataGroupsAndEntities {

    @Override
    void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog,
        GrouperProvisioner grouperProvisioner, StringBuilder logMessage, Object... data) {
      
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Grouper target request from target group only", 
          grouperProvisioner.retrieveGrouperProvisioningDataIncrementalInput().getTargetDaoRetrieveIncrementalDataRequest().getTargetGroupsForGroupOnly(), "groups");
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Grouper target request from target entity only", 
          grouperProvisioner.retrieveGrouperProvisioningDataIncrementalInput().getTargetDaoRetrieveIncrementalDataRequest().getTargetEntitiesForEntityOnly(), "entities");

      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Target provisioning", grouperProvisioner.retrieveGrouperProvisioningData().retrieveTargetProvisioningGroups(), "groups");
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Target provisioning", grouperProvisioner.retrieveGrouperProvisioningData().retrieveTargetProvisioningEntities(), "entities");
    }
  }, 
  
  retrieveTargetDataMemberships {

    @Override
    void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog,
        GrouperProvisioner grouperProvisioner, StringBuilder logMessage, Object... data) {

      if (grouperProvisioner.retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.groupAttributes) {
        appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Target data request some group mships", 
            grouperProvisioner.retrieveGrouperProvisioningDataIncrementalInput().getTargetDaoRetrieveIncrementalDataRequest().getTargetGroupsForGroupSomeMembershipSync(), "groups");
          appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Target data request all group mships", 
              grouperProvisioner.retrieveGrouperProvisioningDataIncrementalInput().getTargetDaoRetrieveIncrementalDataRequest().getTargetGroupsForGroupAllMembershipSync(), "groups");
      }
      if (grouperProvisioner.retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.entityAttributes) {
        appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Target data request some entity mships", 
            grouperProvisioner.retrieveGrouperProvisioningDataIncrementalInput().getTargetDaoRetrieveIncrementalDataRequest().getTargetEntitiesForEntitySomeMembershipSync(), "entities");
        appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Target data request all entity mships", 
            grouperProvisioner.retrieveGrouperProvisioningDataIncrementalInput().getTargetDaoRetrieveIncrementalDataRequest().getTargetEntitiesForEntityAllMembershipSync(), "entities");
      }
      if (grouperProvisioner.retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.membershipObjects) {
        appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Target data request mships", 
            grouperProvisioner.retrieveGrouperProvisioningDataIncrementalInput().getTargetDaoRetrieveIncrementalDataRequest().getTargetMembershipObjectsForMembershipSync(), "memberships");
      }
      if (grouperProvisioner.retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.groupAttributes) {
        appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Target provisioning", grouperProvisioner.retrieveGrouperProvisioningData().retrieveTargetProvisioningGroups(), "groups");
      }
      if (grouperProvisioner.retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.entityAttributes) {
        appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Target provisioning", grouperProvisioner.retrieveGrouperProvisioningData().retrieveTargetProvisioningEntities(), "entities");
      }
      if (grouperProvisioner.retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.membershipObjects) {
        appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Target provisioning", grouperProvisioner.retrieveGrouperProvisioningData().retrieveTargetProvisioningMemberships(), "memberships");
      }
    }
  }, 

  manipulateGrouperTargetGroups {

    @Override
    void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog,
        GrouperProvisioner grouperProvisioner, StringBuilder logMessage, Object... data) {
      
      Set<ProvisioningGroup> groups = (Set<ProvisioningGroup>)data[0];
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Grouper target", groups, "groups");

    }
  }, 
  manipulateGrouperTargetEntities {

    @Override
    void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog,
        GrouperProvisioner grouperProvisioner, StringBuilder logMessage, Object... data) {
      
      Set<ProvisioningEntity> entities = (Set<ProvisioningEntity>)data[0];
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Grouper target", entities, "entities");

    }
  }, 
  manipulateGrouperTargetMemberships {

    @Override
    void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog,
        GrouperProvisioner grouperProvisioner, StringBuilder logMessage, Object... data) {
      
      Set<ProvisioningMembership> memberships = (Set<ProvisioningMembership>)data[0];
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Grouper target", memberships, "memberships");

    }
  }, 
  retrieveDataFromGrouper {

    @Override
    void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog,
        GrouperProvisioner grouperProvisioner, StringBuilder logMessage, Object... data) {
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Grouper provisioning", grouperProvisioner.retrieveGrouperProvisioningData().retrieveGrouperProvisioningGroups(), "groups");
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Grouper provisioning", grouperProvisioner.retrieveGrouperProvisioningData().retrieveGrouperProvisioningEntities(), "entities");
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Grouper provisioning", grouperProvisioner.retrieveGrouperProvisioningData().retrieveGrouperProvisioningMemberships(null), "memberships");
      appendSyncObjects(grouperProvisioner, logMessage, "Sync objects");
    }
  }, 
  missingGroupsForCreate {

    @Override
    void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog,
        GrouperProvisioner grouperProvisioner, StringBuilder logMessage, Object... data) {
      Collection<ProvisioningGroup> missingGroups = (Collection<ProvisioningGroup>)data[0];
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Missing groups for create", missingGroups, "groups");

    }
  }, 
  missingTargetGroupsRetrieved {

    @Override
    void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog,
        GrouperProvisioner grouperProvisioner, StringBuilder logMessage, Object... data) {
      Collection<ProvisioningGroup> missingGroups = (Collection<ProvisioningGroup>)data[0];

      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Missing target groups retrieved", missingGroups, "groups");
    }
  },
  missingTargetEntitiesRetrieved {

    @Override
    void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog,
        GrouperProvisioner grouperProvisioner, StringBuilder logMessage, Object... data) {
      Collection<ProvisioningEntity> missingEntities = (Collection<ProvisioningEntity>)data[0];

      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Missing target entities retrieved", missingEntities, "entities");
    }
  },
  missingGrouperTargetGroupsForCreate {

    @Override
    void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog,
        GrouperProvisioner grouperProvisioner, StringBuilder logMessage, Object... data) {
      Collection<ProvisioningGroup> missingGroups = (Collection<ProvisioningGroup>)data[0];

      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Missing grouper target groups for create", missingGroups, "groups");
      
    }
  }, 
  missingTargetGroupsCreated {

    @Override
    void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog,
        GrouperProvisioner grouperProvisioner, StringBuilder logMessage, Object... data) {
      Collection<ProvisioningGroup> missingGroups = (Collection<ProvisioningGroup>)data[0];
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Missing target groups created", missingGroups, "groups");

    }
  }, 
  missingEntitiesForCreate {

    @Override
    void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog,
        GrouperProvisioner grouperProvisioner, StringBuilder logMessage, Object... data) {
      Collection<ProvisioningGroup> missingGroups = (Collection<ProvisioningGroup>)data[0];
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Missing entities for create", missingGroups, "entities");
      
    }
  }, 
  missingGrouperTargetEntitiesForCreate {

    @Override
    void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog,
        GrouperProvisioner grouperProvisioner, StringBuilder logMessage, Object... data) {
      Collection<ProvisioningEntity> missingEntities = (Collection<ProvisioningEntity>)data[0];
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Missing grouper target entities for create", missingEntities, "entities");

    }
  }, 
  missingTargetEntitiesCreated {

    @Override
    void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog,
        GrouperProvisioner grouperProvisioner, StringBuilder logMessage, Object... data) {
      Collection<ProvisioningEntity> missingEntities = (Collection<ProvisioningEntity>)data[0];
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Missing target entities created", missingEntities, "entities");

    }
  }, 
  linkDataGroups {

    @Override
    void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog,
        GrouperProvisioner grouperProvisioner, StringBuilder logMessage, Object... data) {

      Collection<ProvisioningGroup> syncGroups = (Collection<ProvisioningGroup>)data[0];
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Grouper target objects changed in link", 
          syncGroups, "groups");
      
    }
  },
  linkDataEntities {

    @Override
    void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog,
        GrouperProvisioner grouperProvisioner, StringBuilder logMessage, Object... data) {

      Collection<ProvisioningEntity> syncEntities = (Collection<ProvisioningEntity>)data[0];
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Grouper target objects changed in link", 
          syncEntities, "entities");
      
    }
  },
  retrieveSubjectLink {

    @Override
    void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog,
        GrouperProvisioner grouperProvisioner, StringBuilder logMessage, Object... data) {
      Set<GcGrouperSyncMember> gcSyncMembersChangedInSubjectLink = (Set<GcGrouperSyncMember>)data[0];
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Sync objects", gcSyncMembersChangedInSubjectLink, "gcGrouperSyncMember", "members");

    }
  }, 
  translateGrouperGroupsToTarget {

    @Override
    void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog,
        GrouperProvisioner grouperProvisioner, StringBuilder logMessage, Object... data) {
      Collection<ProvisioningGroup> provisioningGroups = (Collection<ProvisioningGroup>)data[0];

      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Grouper target", provisioningGroups, "groups");

    }
  }, 
  translateGrouperEntitiesToTarget {

    @Override
    void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog,
        GrouperProvisioner grouperProvisioner, StringBuilder logMessage, Object... data) {
      Collection<ProvisioningEntity> provisioningEntities = (Collection<ProvisioningEntity>)data[0];

      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Grouper target", provisioningEntities, "entities");

    }
  }, 
  translateGrouperMembershipsToTarget {

    @Override
    void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog,
        GrouperProvisioner grouperProvisioner, StringBuilder logMessage, Object... data) {
      if (grouperProvisioner.retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.groupAttributes) {

        appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Grouper target", grouperProvisioner.retrieveGrouperProvisioningData().retrieveGrouperTargetGroups(), "grouperTargetGroup", "groups");

      } else if (grouperProvisioner.retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.entityAttributes) {

        appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Grouper target", grouperProvisioner.retrieveGrouperProvisioningData().retrieveGrouperTargetEntities(), "grouperTargetEntity", "entities");

      } else {
        appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Grouper target", grouperProvisioner.retrieveGrouperProvisioningData().retrieveGrouperTargetMemberships(null), "memberships");
      }

    }
  }, 
  compareTargetObjects {

    @Override
    void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog,
        GrouperProvisioner grouperProvisioner, StringBuilder logMessage, Object... data) {
      
      if (GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningDataChanges().getGrouperTargetObjectsMissing().getProvisioningGroups()) > 0) {
        if (logMessage.charAt(logMessage.length()-1) != '\n') {
          logMessage.append("\n Target inserts groups (").append(GrouperUtil.length(grouperProvisioner
              .retrieveGrouperProvisioningDataChanges().getGrouperTargetObjectsMissing().getProvisioningGroups())).append(")");
          logMessage.append(" these were inserted in workflow step 'missingGrouperTargetGroupsForCreate', look in the logs above for details");
        }
      }
      if (GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningDataChanges().getGrouperTargetObjectsMissing().getProvisioningEntities()) > 0) {
        if (logMessage.charAt(logMessage.length()-1) != '\n') {
          logMessage.append("\n Target inserts entities (").append(GrouperUtil.length(grouperProvisioner
              .retrieveGrouperProvisioningDataChanges().getGrouperTargetObjectsMissing().getProvisioningEntities())).append(")");
          logMessage.append(" these were inserted in workflow step 'missingGrouperTargetEntitiesForCreate', look in the logs above for details");
        }
      }
      
      appendProvisioningObjects(grouperProvisioner, logMessage, "Target inserts", grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectInserts());
      appendProvisioningObjects(grouperProvisioner, logMessage, "Target updates", grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectUpdates());
      appendProvisioningObjects(grouperProvisioner, logMessage, "Target deletes", grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectDeletes());
      appendProvisioningObjects(grouperProvisioner, logMessage, "Target replaces", grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectReplaces());

    }
  }, 
  
  retrieveTargetIncrementalMembershipsWithRecalcWhereContainerIsNotRecalc {
  
    @Override
    void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog,
        GrouperProvisioner grouperProvisioner, StringBuilder logMessage, Object... data) {
      Collection collection = (Collection)data[0];
      if (GrouperUtil.length(collection) > 0) {
        if (grouperProvisioner.retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.groupAttributes) {

          appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Target groups", collection, "grouperTargetGroup", "groups");

        } else if (grouperProvisioner.retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.entityAttributes) {

          appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Target entities", collection, "grouperTargetEntity", "entities");

        } else {
          appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Target memberships", collection, "memberships");
        }
      }
    }
    
  },
  matchingIdGrouperGroupsEntities {

    @Override
    void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog,
        GrouperProvisioner grouperProvisioner, StringBuilder logMessage, Object... data) {
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Grouper target", grouperProvisioner.retrieveGrouperProvisioningData().retrieveGrouperTargetGroups(), "groups");
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Grouper target", grouperProvisioner.retrieveGrouperProvisioningData().retrieveGrouperTargetEntities(), "entities");
      
    }
  }, 
  
  validateGrouperGroups {

    @Override
    void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog,
        GrouperProvisioner grouperProvisioner, StringBuilder logMessage, Object... data) {
      Set<ProvisioningGroup> invalidGroups = (Set<ProvisioningGroup>)data[0];
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Grouper target", invalidGroups, "groups");
      
    }
  }, 
  
  validateGrouperEntities {

    @Override
    void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog,
        GrouperProvisioner grouperProvisioner, StringBuilder logMessage, Object... data) {
      Set<ProvisioningEntity> invalidEntities = (Set<ProvisioningEntity>)data[0];
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Grouper target", invalidEntities, "entities");
      
    }
  }, 
  
  validateGrouperMemberships {

    @Override
    void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog,
        GrouperProvisioner grouperProvisioner, StringBuilder logMessage, Object... data) {

      Set<ProvisioningMembership> invalidMemberships = (Set<ProvisioningMembership>)data[0];
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Grouper target", invalidMemberships, "memberships");
      
    }
  }, 
  
  retrieveIndividualMissingGroups {
    
    @Override
    void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog,
        GrouperProvisioner grouperProvisioner, StringBuilder logMessage, Object... data) {
      Collection<ProvisioningGroup> targetGroups = (Collection<ProvisioningGroup>)data[0];
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Grouper target", targetGroups, "groups");
      
    }
    
  },
  retrieveIndividualMissingEntities {
    
    @Override
    void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog,
        GrouperProvisioner grouperProvisioner, StringBuilder logMessage, Object... data) {
      Collection<ProvisioningEntity> targetEntities = (Collection<ProvisioningEntity>)data[0];
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Grouper target", targetEntities, "entities");
      
    }
    
  },
  matchingIdGrouperMemberships {

    @Override
    void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog,
        GrouperProvisioner grouperProvisioner, StringBuilder logMessage, Object... data) {
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Grouper target", grouperProvisioner.retrieveGrouperProvisioningData().retrieveGrouperTargetMemberships(null), "memberships");

    }
  }, 
  logIncomingDataUnprocessed {

    @Override
    void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog,
        GrouperProvisioner grouperProvisioner, StringBuilder logMessage, Object... data) {
      
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Incoming data unprocessed", grouperProvisioner.retrieveGrouperProvisioningData().retrieveIncrementalGroups(), "groups");
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Incoming data unprocessed", grouperProvisioner.retrieveGrouperProvisioningData().retrieveIncrementalEntities(), "entities");
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Incoming data unprocessed", grouperProvisioner.retrieveGrouperProvisioningData().retrieveIncrementalMemberships(), "memberships");

    }
    
  },
  logIncomingDataToProcess {

    @Override
    void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog,
        GrouperProvisioner grouperProvisioner, StringBuilder logMessage, Object... data) {

      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Incoming data processed", grouperProvisioner.retrieveGrouperProvisioningData().retrieveIncrementalGroups(), "groups");
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Incoming data processed", grouperProvisioner.retrieveGrouperProvisioningData().retrieveIncrementalEntities(), "entities");
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Incoming data processed", grouperProvisioner.retrieveGrouperProvisioningData().retrieveIncrementalMemberships(), "memberships");

    }
    
  };

  abstract void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog, GrouperProvisioner grouperProvisioner, StringBuilder logMessage, Object... data);

  public static void appendConfiguration(GrouperProvisioner grouperProvisioner, StringBuilder logMessage, String label) {
    if (logMessage.charAt(logMessage.length()-1) != '\n') {
      logMessage.append("\n");
    }
    logMessage.append(label).append(" (");
    logMessage.append(grouperProvisioner.retrieveGrouperProvisioningConfiguration().toString());
    logMessage.append(")\n");
  }

  public static void appendProvisioner(GrouperProvisioner grouperProvisioner, StringBuilder logMessage, String label) {
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


  /**
   * 
   * @param string
   * @param grouperProvisioningObjects
   */
  private static void appendProvisioningObjects(GrouperProvisioner grouperProvisioner, StringBuilder logMessage, String label,
      GrouperProvisioningReplacesObjects grouperProvisioningObjects) {
    
    appendProvisioningObjectsOfType(grouperProvisioner, logMessage, label, grouperProvisioningObjects.getProvisioningMemberships().keySet(), "groups");
    
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
    int logAllObjectsVerboseCount = grouperProvisioner.retrieveGrouperProvisioningConfiguration().getLogAllObjectsVerboseCount();
    logAllObjectsVerboseCount = Math.min(logAllObjectsVerboseCount, 1000);
    
    Set<Object> loggedAlready = new HashSet<Object>();
    OUTER: for (boolean strong : new boolean[] {true, false}) {
      int remainingBeans = GrouperUtil.length(beans);
      for (Object bean : GrouperUtil.nonNull(beans)) {
        if (bean instanceof MultiKey) {
          MultiKey multiKey = (MultiKey)bean;
          StringBuilder beanString = new StringBuilder("[");
          for (int i=0;i<multiKey.size();i++) {
            if (i>0) {
              beanString.append(",");
            }
            beanString.append(GrouperUtil.stringValue(multiKey.getKey(i).toString()));
          }
          beanString.append("]");
          bean = beanString.toString();
        
        } else {
          if (bean instanceof ProvisioningGroup) {
            ProvisioningGroupWrapper provisioningGroupWrapper = ((ProvisioningGroup)bean).getProvisioningGroupWrapper();
            if (provisioningGroupWrapper != null && bean != provisioningGroupWrapper.getTargetProvisioningGroup()) {
              ProvisioningGroup grouperTargetGroup = provisioningGroupWrapper.getGrouperTargetGroup();
              if (grouperTargetGroup != null) {
                bean = grouperTargetGroup;
              }
            }
          }
          
          if (bean instanceof ProvisioningEntity) {
            ProvisioningEntityWrapper provisioningEntityWrapper = ((ProvisioningEntity)bean).getProvisioningEntityWrapper();
            if (provisioningEntityWrapper != null && bean != provisioningEntityWrapper.getTargetProvisioningEntity()) {
              ProvisioningEntity grouperTargetEntity = provisioningEntityWrapper.getGrouperTargetEntity();
              if (grouperTargetEntity != null) {
                bean = grouperTargetEntity;
              }
            }
          }
          
          if (bean instanceof ProvisioningMembership) {
            ProvisioningMembershipWrapper provisioningMembershipWrapper = ((ProvisioningMembership)bean).getProvisioningMembershipWrapper();
            if (provisioningMembershipWrapper != null && bean != provisioningMembershipWrapper.getTargetProvisioningMembership()) {
              ProvisioningMembership grouperTargetMembership = provisioningMembershipWrapper.getGrouperTargetMembership();
              if (grouperTargetMembership != null) {
                bean = grouperTargetMembership;
              }
            }
          }
            
        }
   
        if (loggedAlready.contains(bean)) {
          remainingBeans--;
          continue;
          
        }
        if ((strong || remainingBeans > logAllObjectsVerboseCount-objectCount)
            && grouperProvisioner.retrieveGrouperProvisioningConfiguration().isLogCertainObjects()
            && (
                bean instanceof ProvisioningMembership
                || ((bean instanceof ProvisioningEntity || bean instanceof ProvisioningEntityWrapper) && GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningConfiguration().getLogAllObjectsVerboseForTheseSubjectIds()) > 0)
                || ((bean instanceof ProvisioningGroup || bean instanceof ProvisioningGroupWrapper) && GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningConfiguration().getLogAllObjectsVerboseForTheseGroupNames()) > 0))) {
          
          boolean shouldLog = true;
          
          if (bean instanceof ProvisioningGroup) {
            shouldLog = ((ProvisioningGroup)bean).isLoggable(strong);
          } else if (bean instanceof ProvisioningEntity) {
            shouldLog = ((ProvisioningEntity)bean).isLoggable(strong);
          } else if (bean instanceof ProvisioningMembership) {
            shouldLog = ((ProvisioningMembership)bean).isLoggable(strong);
          } else if (bean instanceof ProvisioningGroupWrapper) {
            shouldLog = ((ProvisioningGroupWrapper)bean).getProvisioningStateGroup().isLoggable(strong);
          } else if (bean instanceof ProvisioningEntityWrapper) {
            shouldLog = ((ProvisioningEntityWrapper)bean).getProvisioningStateEntity().isLoggable(strong);
          } else if (bean instanceof ProvisioningMembershipWrapper) {
            shouldLog = ((ProvisioningMembershipWrapper)bean).getProvisioningStateMembership().isLoggable(strong);
          }

          if (!shouldLog) {
            remainingBeans--;
            continue;
          }
        }
        
        loggedAlready.add(bean);
        
        logMessage.append(objectCount).append(". ");
        if (bean == null) {
          logMessage.append("null");
        } else if ("gcGrouperSyncGroup".equals(field)) {
          logMessage.append(((ProvisioningGroupWrapper)bean).getGcGrouperSyncGroup().toString());
        } else if ("gcGrouperSyncMember".equals(field)) {
          logMessage.append(((ProvisioningEntityWrapper)bean).getGcGrouperSyncMember().toString());
        } else if ("gcGrouperSyncMembership".equals(field)) {
          logMessage.append(((ProvisioningMembershipWrapper)bean).getGcGrouperSyncMembership().toString());
        } else if ("grouperTargetGroup".equals(field)) {
          ProvisioningGroupWrapper provisioningGroupWrapper = null;
          if (bean instanceof ProvisioningGroupWrapper) {
            provisioningGroupWrapper = (ProvisioningGroupWrapper)bean;
          } else {
            provisioningGroupWrapper = ((ProvisioningGroup)bean).getProvisioningGroupWrapper();
          }
          logMessage.append(provisioningGroupWrapper.getGrouperTargetGroup().toString());
        } else if ("grouperTargetEntity".equals(field)) {
          ProvisioningEntityWrapper provisioningEntityWrapper = null;
          if (bean instanceof ProvisioningGroupWrapper) {
            provisioningEntityWrapper = (ProvisioningEntityWrapper)bean;
          } else {
            provisioningEntityWrapper = ((ProvisioningEntity)bean).getProvisioningEntityWrapper();
          }
          logMessage.append(provisioningEntityWrapper.getGrouperTargetEntity().toString());
        } else if ("grouperTargetMembership".equals(field)) {
          ProvisioningMembershipWrapper provisioningMembershipWrapper = null;
          if (bean instanceof ProvisioningGroupWrapper) {
            provisioningMembershipWrapper = (ProvisioningMembershipWrapper)bean;
          } else {
            provisioningMembershipWrapper = ((ProvisioningMembership)bean).getProvisioningMembershipWrapper();
          }
          logMessage.append(provisioningMembershipWrapper.getGrouperTargetMembership().toString());
        } else if (field != null) {
          throw new RuntimeException("Not expecting field '" + field + "'");
        } else {
          logMessage.append(bean.toString());
        }
        
        logMessage.append("\n");
        if (objectCount >= logAllObjectsVerboseCount) {
          break OUTER;
        }
        objectCount++;
        remainingBeans--;
      }
      
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
        GrouperIncrementalDataAction grouperIncrementalDataAction = null;
        if (item instanceof GrouperIncrementalDataItem) {
          grouperIncrementalDataAction = ((GrouperIncrementalDataItem)item).getGrouperIncrementalDataAction();
          item = ((GrouperIncrementalDataItem)item).getItem();
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
        if (grouperIncrementalDataAction != null) {
          logMessage.append(" (").append(grouperIncrementalDataAction.name()).append(")");
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
      Collection beans, String type) {
    appendProvisioningObjectsOfType(grouperProvisioner, logMessage, label,
        beans, null, type);
  }

  /**
   * 
   * @param string
   * @param grouperProvisioningObjects
   */
  private static void appendSyncObjects(GrouperProvisioner grouperProvisioner, StringBuilder logMessage, String label) {
    appendProvisioningObjectsOfType(grouperProvisioner, logMessage, label, 
        GrouperUtil.nonNull(grouperProvisioner.retrieveGrouperProvisioningDataIndex().getGroupUuidToProvisioningGroupWrapper()).values()
        , "gcGrouperSyncGroup", "groups");
    appendProvisioningObjectsOfType(grouperProvisioner, logMessage, label, 
        GrouperUtil.nonNull(grouperProvisioner.retrieveGrouperProvisioningDataIndex().getMemberUuidToProvisioningEntityWrapper()).values()
        , "gcGrouperSyncMember", "entities");
    appendProvisioningObjectsOfType(grouperProvisioner, logMessage, label, 
        GrouperUtil.nonNull(grouperProvisioner.retrieveGrouperProvisioningDataIndex().getGroupUuidMemberUuidToProvisioningMembershipWrapper()).values()
        , "gcGrouperSyncMembership", "memberships");
  }

  public static void appendTargetDaoBehaviors(GrouperProvisioner grouperProvisioner, StringBuilder logMessage, String label) {
    if (logMessage.charAt(logMessage.length()-1) != '\n') {
      logMessage.append("\n");
    }
    logMessage.append(label).append(" (");
    logMessage.append(grouperProvisioner.retrieveGrouperProvisioningBehavior().toString());
    logMessage.append(")\n");
  }

  public static void appendTargetDaoCapabilities(GrouperProvisioner grouperProvisioner, StringBuilder logMessage, String label) {
    if (logMessage.charAt(logMessage.length()-1) != '\n') {
      logMessage.append("\n");
    }
    logMessage.append(label).append(" (");
    logMessage.append(grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().getWrappedDao().getGrouperProvisionerDaoCapabilities().toString());
    logMessage.append(")\n");
  }

}
