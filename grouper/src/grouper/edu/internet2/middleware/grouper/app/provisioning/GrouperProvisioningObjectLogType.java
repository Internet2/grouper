package edu.internet2.middleware.grouper.app.provisioning;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
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

      GrouperProvisioningLists targetProvisioningData = grouperProvisioner.retrieveGrouperProvisioningData().getTargetProvisioningLists();
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Target provisioning", targetProvisioningData.getProvisioningGroups(), "groups");
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Target provisioning", targetProvisioningData.getProvisioningEntities(), "entities");
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Target provisioning", targetProvisioningData.getProvisioningMemberships(), "memberships");

    }
  }, 
  retrieveTargetDataIncremental {

    @Override
    void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog,
        GrouperProvisioner grouperProvisioner, StringBuilder logMessage, Object... data) {
      
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Target data request group only", 
          grouperProvisioner.retrieveGrouperProvisioningDataIncrementalInput().getTargetDaoRetrieveIncrementalDataRequest().getTargetGroupsForGroupOnly(), "groups");
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Target data request group mships", 
          grouperProvisioner.retrieveGrouperProvisioningDataIncrementalInput().getTargetDaoRetrieveIncrementalDataRequest().getTargetGroupsForGroupMembershipSync(), "groups");
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Target data request entity only", 
          grouperProvisioner.retrieveGrouperProvisioningDataIncrementalInput().getTargetDaoRetrieveIncrementalDataRequest().getTargetEntitiesForEntityOnly(), "entities");
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Target data request entity mships", 
          grouperProvisioner.retrieveGrouperProvisioningDataIncrementalInput().getTargetDaoRetrieveIncrementalDataRequest().getTargetEntitiesForEntityMembershipSync(), "entities");
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Target data request mships", 
          grouperProvisioner.retrieveGrouperProvisioningDataIncrementalInput().getTargetDaoRetrieveIncrementalDataRequest().getTargetMembershipObjectsForMembershipSync(), "memberships");

      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Target provisioning", grouperProvisioner.retrieveGrouperProvisioningData().retrieveTargetProvisioningGroups(), "groups");
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Target provisioning", grouperProvisioner.retrieveGrouperProvisioningData().retrieveTargetProvisioningEntities(), "entities");
      appendProvisioningObjectsOfType(grouperProvisioner, logMessage, "Target provisioning", grouperProvisioner.retrieveGrouperProvisioningData().retrieveTargetProvisioningMemberships(), "memberships");
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
      appendSyncObjectsOfTypeGcSyncMember(grouperProvisioner, logMessage, "Sync objects", gcSyncMembersChangedInSubjectLink, "members");

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
      appendIncomingData(grouperProvisioner, logMessage, "Incoming data unprocessed", "(with recalc from target)",     
          grouperProvisioner.retrieveGrouperProvisioningDataIncrementalInput().getGrouperIncrementalDataToProcessWithRecalc());
      appendIncomingData(grouperProvisioner, logMessage, "Incoming data unprocessed", "(without recalc from target)",     
          grouperProvisioner.retrieveGrouperProvisioningDataIncrementalInput().getGrouperIncrementalDataToProcessWithoutRecalc());
      
    }
    
  },
  logIncomingDataToProcess {

    @Override
    void logState(GrouperProvisioningObjectLog grouperProvisioningObjectLog,
        GrouperProvisioner grouperProvisioner, StringBuilder logMessage, Object... data) {
      appendIncomingData(grouperProvisioner, logMessage, "Incoming data to process", "(with recalc from target)",     
          grouperProvisioner.retrieveGrouperProvisioningDataIncrementalInput().getGrouperIncrementalDataToProcessWithRecalc());
      appendIncomingData(grouperProvisioner, logMessage, "Incoming data to process", "(without recalc from target)",     
          grouperProvisioner.retrieveGrouperProvisioningDataIncrementalInput().getGrouperIncrementalDataToProcessWithoutRecalc());
      
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
      } else if ((!(bean instanceof ProvisioningGroup)) && (!(bean instanceof ProvisioningEntity))  && (!(bean instanceof ProvisioningMembership))) {
        if ("grouperTargetGroup".equals(field)) {
          bean = ((ProvisioningGroupWrapper)bean).getGrouperTargetGroup();
        } else if ("grouperTargetEntity".equals(field)) {
          bean = ((ProvisioningEntityWrapper)bean).getGrouperTargetEntity();
        } else if (field != null) {
          throw new RuntimeException("Not expecting field '" + field + "'");
        }
      } else {
        if (bean instanceof ProvisioningGroup) {
          ProvisioningGroupWrapper provisioningGroupWrapper = ((ProvisioningGroup)bean).getProvisioningGroupWrapper();
          if (provisioningGroupWrapper != null) {
            ProvisioningGroup grouperTargetGroup = provisioningGroupWrapper.getGrouperTargetGroup();
            if (grouperTargetGroup != null) {
              bean = grouperTargetGroup;
            }
          }
        }
        
        if (bean instanceof ProvisioningEntity) {
          ProvisioningEntityWrapper provisioningEntityWrapper = ((ProvisioningEntity)bean).getProvisioningEntityWrapper();
          if (provisioningEntityWrapper != null) {
            ProvisioningEntity grouperTargetEntity = provisioningEntityWrapper.getGrouperTargetEntity();
            if (grouperTargetEntity != null) {
              bean = grouperTargetEntity;
            }
          }
        }
        
        if (bean instanceof ProvisioningMembership) {
          ProvisioningMembershipWrapper provisioningMembershipWrapper = ((ProvisioningMembership)bean).getProvisioningMembershipWrapper();
          if (provisioningMembershipWrapper != null) {
            ProvisioningMembership grouperTargetMembership = provisioningMembershipWrapper.getGrouperTargetMembership();
            if (grouperTargetMembership != null) {
              bean = grouperTargetMembership;
            }
          }
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
      appendList(logMessage, "groupUuidsMemberUuidsMembershipSync", grouperIncrementalDataToProcess.getGroupUuidsMemberUuidsForMembershipSync());
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
    appendSyncObjectsOfTypeGroup(grouperProvisioner, logMessage, label, grouperProvisioner.retrieveGrouperProvisioningDataIndex().getGroupUuidToProvisioningGroupWrapper(), "groups");
    appendSyncObjectsOfTypeEntity(grouperProvisioner, logMessage, label, grouperProvisioner.retrieveGrouperProvisioningDataIndex().getMemberUuidToProvisioningEntityWrapper(), "members");
    appendSyncObjectsOfTypeMembership(grouperProvisioner, logMessage, label, grouperProvisioner.retrieveGrouperProvisioningDataIndex().getGroupUuidMemberUuidToProvisioningMembershipWrapper(), "memberships");
  }

  private static void appendSyncObjectsOfTypeMembership(GrouperProvisioner grouperProvisioner, StringBuilder logMessage, String label,
      Map<MultiKey, ProvisioningMembershipWrapper> memberIdToWrapper, String type) {
    if (logMessage.charAt(logMessage.length()-1) != '\n') {
      logMessage.append("\n");
    }
    Set<GcGrouperSyncMembership> gcGrouperSyncMemberships = new LinkedHashSet<GcGrouperSyncMembership>();
    
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
    Set<GcGrouperSyncMember> gcGrouperSyncMembers = new HashSet<GcGrouperSyncMember>();
    
    for (ProvisioningEntityWrapper provisioningEntityWrapper : GrouperUtil.nonNull(memberIdToWrapper).values()) {
      GcGrouperSyncMember gcGrouperSyncMember = provisioningEntityWrapper.getGcGrouperSyncMember();
      if (gcGrouperSyncMember != null) {
        gcGrouperSyncMembers.add(gcGrouperSyncMember);
      }
    }
    appendSyncObjectsOfTypeGcSyncMember(grouperProvisioner, logMessage, label, gcGrouperSyncMembers, type);
  }

  private static void appendSyncObjectsOfTypeGcSyncMember(GrouperProvisioner grouperProvisioner, StringBuilder logMessage, String label,
      Set<GcGrouperSyncMember> gcGrouperSyncMembers, String type) {
    if (logMessage.charAt(logMessage.length()-1) != '\n') {
      logMessage.append("\n");
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
    Set<GcGrouperSyncGroup> gcGrouperSyncGroups = new LinkedHashSet<GcGrouperSyncGroup>();
    
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
