package edu.internet2.middleware.grouper.app.deprovisioning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.hibernate.type.StringType;
import org.hibernate.type.Type;
import org.quartz.DisallowConcurrentExecution;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignable;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbEvent;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbEventContainer;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbEventType;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.pit.PITAttributeAssign;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncDao;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncHeartbeat;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncJob;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.StringUtils;

@DisallowConcurrentExecution
public class GrouperDeprovisioningDaemonLogic extends OtherJobBase {
  
  private static final Log LOG = GrouperUtil.getLog(GrouperDeprovisioningDaemonLogic.class);
  
  /**
   * this method retrieves all deprovisioning attributes assignments and values for all folders. 
   * Also it returns all folders underneath folders with deprovisioning assigned.
   * @return
   */
  public static Map<String, Map<String, GrouperDeprovisioningObjectAttributes>> retrieveAllFoldersOfInterestForDeprovisioning() {
      
    Map<String, Map<String, GrouperDeprovisioningObjectAttributes>> results = new HashMap<String, Map<String, GrouperDeprovisioningObjectAttributes>>();

    {
      String sql = "SELECT " +
          "    gs.id, " +
          "    gs.name, " + 
          "    gadn_config.name, " + 
          "    gaav_config.value_string, " + 
          "    gaav_affiliation.value_string as affiliation, " + 
          "    gaa_marker.id " + 
          "FROM " + 
          "    grouper_stems gs, " + 
          "    grouper_attribute_assign gaa_marker, " + 
          "    grouper_attribute_assign gaa_affiliation, " + 
          "    grouper_attribute_assign gaa_config, " + 
          "    grouper_attribute_assign_value gaav_affiliation, " + 
          "    grouper_attribute_assign_value gaav_config, " + 
          "    grouper_attribute_def_name gadn_marker, " + 
          "    grouper_attribute_def_name gadn_affiliation, " + 
          "    grouper_attribute_def_name gadn_config " + 
          "WHERE " + 
          "    gs.id = gaa_marker.owner_stem_id " + 
          "    AND gaa_marker.attribute_def_name_id = gadn_marker.id " + 
          "    AND gadn_marker.name = ? " + 
          "    AND gaa_marker.id = gaa_affiliation.owner_attribute_assign_id " + 
          "    AND gaa_affiliation.attribute_def_name_id = gadn_affiliation.id " + 
          "    AND gadn_affiliation.name = ? " + 
          "    AND gaav_affiliation.attribute_assign_id = gaa_affiliation.id " + 
          "    AND gaa_marker.id = gaa_config.owner_attribute_assign_id " + 
          "    AND gaav_config.attribute_assign_id = gaa_config.id " + 
          "    AND gadn_config.id = gaa_config.attribute_def_name_id " + 
          "    AND gaa_marker.enabled = 'T' " + 
          "    AND gaa_affiliation.enabled = 'T' " + 
          "    AND gaa_config.enabled = 'T' ";
      
      List<Object> paramsInitial = new ArrayList<Object>();

      paramsInitial.add(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameBase().getName());
      paramsInitial.add(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameAffiliation().getName());
      
      List<Type> typesInitial = new ArrayList<Type>();
      typesInitial.add(StringType.INSTANCE);
      typesInitial.add(StringType.INSTANCE);
  
      List<String[]> queryResults = HibernateSession.bySqlStatic().listSelect(String[].class, sql, paramsInitial, typesInitial);
      for (String[] queryResult : queryResults) {
        String stemId = queryResult[0];
        String stemName = queryResult[1];
        String configName = queryResult[2];
        String configValue = queryResult[3];
        String affiliation = queryResult[4];
        String markerAttributeAssignId = queryResult[5];
        
        Map<String, GrouperDeprovisioningObjectAttributes> stemToDeprovisioningAttributes = results.get(affiliation);
        if (stemToDeprovisioningAttributes == null) {
          stemToDeprovisioningAttributes = new HashMap<String, GrouperDeprovisioningObjectAttributes>();
          results.put(affiliation, stemToDeprovisioningAttributes);
        }
        
        GrouperDeprovisioningObjectAttributes grouperDeprovisioningAttributes = stemToDeprovisioningAttributes.get(stemName);
        if (grouperDeprovisioningAttributes == null) {
          grouperDeprovisioningAttributes = new GrouperDeprovisioningObjectAttributes(stemId, stemName, markerAttributeAssignId);
          stemToDeprovisioningAttributes.put(stemName, grouperDeprovisioningAttributes);
          grouperDeprovisioningAttributes.setOwnedByStem(true);
        }
        
        if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameAffiliation().getName())) {
          grouperDeprovisioningAttributes.setAffiliation(configValue);
        } else if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameAllowAddsWhileDeprovisioned().getName())) {
          grouperDeprovisioningAttributes.setAllowAddsWhileDeprovisioned(configValue);
        } else if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameAutoChangeLoader().getName())) {
          grouperDeprovisioningAttributes.setAutoChangeLoader(configValue);
        } else if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameAutoSelectForRemoval().getName())) {
          grouperDeprovisioningAttributes.setAutoSelectForRemoval(configValue);
        } else if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameDeprovision().getName())) {
          grouperDeprovisioningAttributes.setDeprovision(configValue);
        } else if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameDirectAssignment().getName())) {
          grouperDeprovisioningAttributes.setDirectAssign(configValue);
        } else if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameEmailAddresses().getName())) {
          grouperDeprovisioningAttributes.setEmailAddresses(configValue);
        } else if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameEmailBody().getName())) {
          grouperDeprovisioningAttributes.setEmailBody(configValue);
        } else if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameInheritedFromFolderId().getName())) {
          grouperDeprovisioningAttributes.setOwnerStemId(configValue);
        } else if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameMailToGroup().getName())) {
          grouperDeprovisioningAttributes.setMailToGroup(configValue);
        } else if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameSendEmail().getName())) {
          grouperDeprovisioningAttributes.setSendEmail(configValue);
        } else if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameShowForRemoval().getName())) {
          grouperDeprovisioningAttributes.setShowForRemoval(configValue);
        } else if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameStemScope().getName())) {
          grouperDeprovisioningAttributes.setStemScope(configValue);
        }
      }
    }
    
    // now see if there are any other folders to add to the map that don't have attributes but are under a parent folder that has a direct assign
    // they won't necessarily need attributes but they'll need to be checked.

    {
      String sql = "SELECT distinct   " + 
          "      gs_if_has_stem.id,   " + 
          "      gs_if_has_stem.name" +
          "  FROM   " + 
          "      grouper_stems gs,   " + 
          "      grouper_attribute_assign gaa_marker,   " + 
          "      grouper_attribute_assign gaa_direct,   " + 
          "      grouper_attribute_assign_value gaav_direct,   " + 
          "      grouper_attribute_def_name gadn_marker," + 
          "      grouper_attribute_def_name gadn_direct,   " + 
          "      grouper_stem_set gss,   " + 
          "      grouper_stems gs_if_has_stem   " + 
          "  WHERE   " + 
          "      gs.id = gaa_marker.owner_stem_id   " + 
          "      AND gaa_marker.attribute_def_name_id = gadn_marker.id   " + 
          "      AND gadn_marker.name = ?  " + 
          "      AND gaa_marker.id = gaa_direct.owner_attribute_assign_id   " + 
          "      AND gaa_direct.attribute_def_name_id = gadn_direct.id   " + 
          "      AND gadn_direct.name = ? " + 
          "      AND gaav_direct.attribute_assign_id = gaa_direct.id   " + 
          "      AND gaav_direct.value_string = 'true'   " + 
          "      AND gs.id = gss.then_has_stem_id   " + 
          "      AND gss.if_has_stem_id = gs_if_has_stem.id   " +
          "      AND gaa_marker.enabled = 'T'" + 
          "      AND gaa_direct.enabled = 'T' ";
      
      List<Object> paramsInitial = new ArrayList<Object>();
      
      paramsInitial.add(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameBase().getName());
      paramsInitial.add(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameDirectAssignment().getName());
      
      List<Type> typesInitial = new ArrayList<Type>();
      typesInitial.add(StringType.INSTANCE);
      typesInitial.add(StringType.INSTANCE);
  
      List<String[]> queryResults = HibernateSession.bySqlStatic().listSelect(String[].class, sql, paramsInitial, typesInitial);

      for (String affiliation: results.keySet()) {
        
        Map<String, GrouperDeprovisioningObjectAttributes> stemToDeprovisioningAttributes = results.get(affiliation);
        
        for (String[] queryResult : queryResults) {
          String stemId = queryResult[0];
          String stemName = queryResult[1];
          
          if (stemToDeprovisioningAttributes.get(stemName) == null) {
            
            Set<String> parentStemNames = GrouperUtil.findParentStemNames(stemName);
            for (String parentName: parentStemNames) {
              if (stemToDeprovisioningAttributes.containsKey(parentName)) {
                stemToDeprovisioningAttributes.put(stemName, new GrouperDeprovisioningObjectAttributes(stemId, stemName, null));
                stemToDeprovisioningAttributes.get(stemName).setOwnedByStem(true);
                break;
              }
            }
            
          }
          
        }
        
      }
    }
    
    return results;
    
  }
  
  /**
   * this method retrieves all deprovisioning attributes assignments and values for all groups. 
   * Also it returns all groups underneath folders with deprovisioning attributes assigned.
   * @return
   */
  public static Map<String, Map<String, GrouperDeprovisioningObjectAttributes>> retrieveAllGroupsOfInterestForDeprovisioning(Map<String, 
      Map<String, GrouperDeprovisioningObjectAttributes>> allStemsOfInterestForDeprovisioning) {
    
    Map<String, Map<String, GrouperDeprovisioningObjectAttributes>> results = new HashMap<String, Map<String, GrouperDeprovisioningObjectAttributes>>();

    {
      String sql = "SELECT " +
          "    gg.id, " + 
          "    gg.name, " + 
          "    gadn_config.name, " + 
          "    gaav_config.value_string, " + 
          "    gaav_affiliation.value_string as affiliation, " + 
          "    gaa_marker.id " + 
          "FROM " + 
          "    grouper_groups gg, " + 
          "    grouper_attribute_assign gaa_marker, " + 
          "    grouper_attribute_assign gaa_affiliation, " + 
          "    grouper_attribute_assign gaa_config, " + 
          "    grouper_attribute_assign_value gaav_affiliation, " + 
          "    grouper_attribute_assign_value gaav_config, " + 
          "    grouper_attribute_def_name gadn_marker, " + 
          "    grouper_attribute_def_name gadn_affiliation, " + 
          "    grouper_attribute_def_name gadn_config " + 
          "WHERE " + 
          "    gg.id = gaa_marker.owner_group_id " + 
          "    AND gaa_marker.attribute_def_name_id = gadn_marker.id " + 
          "    AND gadn_marker.name = ? " + 
          "    AND gaa_marker.id = gaa_affiliation.owner_attribute_assign_id " + 
          "    AND gaa_affiliation.attribute_def_name_id = gadn_affiliation.id " + 
          "    AND gadn_affiliation.name = ? " + 
          "    AND gaav_affiliation.attribute_assign_id = gaa_affiliation.id " + 
          "    AND gaa_marker.id = gaa_config.owner_attribute_assign_id " + 
          "    AND gaav_config.attribute_assign_id = gaa_config.id " + 
          "    AND gadn_config.id = gaa_config.attribute_def_name_id " + 
          "    AND gaa_marker.enabled = 'T' " + 
          "    AND gaa_affiliation.enabled = 'T' " + 
          "    AND gaa_config.enabled = 'T' ";
      
      List<Object> paramsInitial = new ArrayList<Object>();

      paramsInitial.add(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameBase().getName());
      paramsInitial.add(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameAffiliation().getName());
      
      List<Type> typesInitial = new ArrayList<Type>();
      typesInitial.add(StringType.INSTANCE);
      typesInitial.add(StringType.INSTANCE);
  
      List<String[]> queryResults = HibernateSession.bySqlStatic().listSelect(String[].class, sql, paramsInitial, typesInitial);
      for (String[] queryResult : queryResults) {
        String groupId = queryResult[0];
        String groupName = queryResult[1];
        String configName = queryResult[2];
        String configValue = queryResult[3];
        String affiliation = queryResult[4];
        String markerAttributeAssignId = queryResult[5];
        
        Map<String, GrouperDeprovisioningObjectAttributes> groupToDeprovisioningAttributes = results.get(affiliation);
        if (groupToDeprovisioningAttributes == null) {
          groupToDeprovisioningAttributes = new HashMap<String, GrouperDeprovisioningObjectAttributes>();
          results.put(affiliation, groupToDeprovisioningAttributes);
        }
        
        GrouperDeprovisioningObjectAttributes grouperDeprovisioningAttributes = groupToDeprovisioningAttributes.get(groupName);
        if (grouperDeprovisioningAttributes == null) {
          grouperDeprovisioningAttributes = new GrouperDeprovisioningObjectAttributes(groupId, groupName, markerAttributeAssignId);
          groupToDeprovisioningAttributes.put(groupName, grouperDeprovisioningAttributes);
          grouperDeprovisioningAttributes.setOwnedByGroup(true);
        }
        
        if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameAffiliation().getName())) {
          grouperDeprovisioningAttributes.setAffiliation(configValue);
        } else if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameAllowAddsWhileDeprovisioned().getName())) {
          grouperDeprovisioningAttributes.setAllowAddsWhileDeprovisioned(configValue);
        } else if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameAutoChangeLoader().getName())) {
          grouperDeprovisioningAttributes.setAutoChangeLoader(configValue);
        } else if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameAutoSelectForRemoval().getName())) {
          grouperDeprovisioningAttributes.setAutoSelectForRemoval(configValue);
        } else if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameDeprovision().getName())) {
          grouperDeprovisioningAttributes.setDeprovision(configValue);
        } else if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameDirectAssignment().getName())) {
          grouperDeprovisioningAttributes.setDirectAssign(configValue);
        } else if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameEmailAddresses().getName())) {
          grouperDeprovisioningAttributes.setEmailAddresses(configValue);
        } else if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameEmailBody().getName())) {
          grouperDeprovisioningAttributes.setEmailBody(configValue);
        } else if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameInheritedFromFolderId().getName())) {
          grouperDeprovisioningAttributes.setOwnerStemId(configValue);
        } else if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameMailToGroup().getName())) {
          grouperDeprovisioningAttributes.setMailToGroup(configValue);
        } else if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameSendEmail().getName())) {
          grouperDeprovisioningAttributes.setSendEmail(configValue);
        } else if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameShowForRemoval().getName())) {
          grouperDeprovisioningAttributes.setShowForRemoval(configValue);
        } else if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameStemScope().getName())) {
          grouperDeprovisioningAttributes.setStemScope(configValue);
        }
        
      }
    }
    
    // now see if there are any other groups to add to the map that don't have attributes but are under a parent folder that has a direct assign
    // they won't necessarily need attributes but they'll need to be checked.

    {
      String sql = "SELECT distinct   " + 
          "      gg.id,   " + 
          "      gg.name" + 
          "  FROM   " + 
          "      grouper_stems gs,   " + 
          "      grouper_attribute_assign gaa_marker,   " + 
          "      grouper_attribute_assign gaa_direct,   " + 
          "      grouper_attribute_assign_value gaav_direct,   " + 
          "      grouper_attribute_def_name gadn_marker," + 
          "      grouper_attribute_def_name gadn_direct,   " + 
          "      grouper_stem_set gss,   " + 
          "      grouper_groups gg   " + 
          "  WHERE   " + 
          "      gs.id = gaa_marker.owner_stem_id   " + 
          "      AND gaa_marker.attribute_def_name_id = gadn_marker.id   " + 
          "      AND gadn_marker.name = ?  " + 
          "      AND gaa_marker.id = gaa_direct.owner_attribute_assign_id   " + 
          "      AND gaa_direct.attribute_def_name_id = gadn_direct.id   " + 
          "      AND gadn_direct.name = ? " + 
          "      AND gaav_direct.attribute_assign_id = gaa_direct.id   " +
          "      AND gaav_direct.value_string = 'true'   " + 
          "      AND gs.id = gss.then_has_stem_id   " + 
          "      AND gss.if_has_stem_id = gg.parent_stem   " +
          "      AND gaa_marker.enabled = 'T'" + 
          "      AND gaa_direct.enabled = 'T' ";
      
      List<Object> paramsInitial = new ArrayList<Object>();
      
      paramsInitial.add(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameBase().getName());
      paramsInitial.add(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameDirectAssignment().getName());
      
      List<Type> typesInitial = new ArrayList<Type>();
      typesInitial.add(StringType.INSTANCE);
      typesInitial.add(StringType.INSTANCE);
  
      List<String[]> queryResults = HibernateSession.bySqlStatic().listSelect(String[].class, sql, paramsInitial, typesInitial);
      
     
      Set<String> combinedAffiliations = new HashSet<String>();
      combinedAffiliations.addAll(allStemsOfInterestForDeprovisioning.keySet());
      combinedAffiliations.addAll(results.keySet());
      
      for (String affiliationName: combinedAffiliations) {
        
        Map<String, GrouperDeprovisioningObjectAttributes> groupToDeprovisioningAttributes = results.get(affiliationName);
        
        if (groupToDeprovisioningAttributes == null) {
          groupToDeprovisioningAttributes = new HashMap<String, GrouperDeprovisioningObjectAttributes>();
          results.put(affiliationName, groupToDeprovisioningAttributes);
        }
        
        for (String[] queryResult : queryResults) {
          String groupId = queryResult[0];
          String groupName = queryResult[1];
          
          if (!objectNameHasAnAncestorWithThisAffiliation(groupName, affiliationName, allStemsOfInterestForDeprovisioning)) {
            continue;
          }
          
          if (groupToDeprovisioningAttributes.get(groupName) == null) {
            groupToDeprovisioningAttributes.put(groupName, new GrouperDeprovisioningObjectAttributes(groupId, groupName, null));
            groupToDeprovisioningAttributes.get(groupName).setOwnedByGroup(true);
          }
          
        }
        
      }
      
    }
    
    return results;
    
  }
  
  private static boolean objectNameHasAnAncestorWithThisAffiliation(String objectName, String affiliationName, 
      Map<String, Map<String, GrouperDeprovisioningObjectAttributes>> stemsOfInterestForDeprovisioning) {
   
    Set<String> parentStemNames = GrouperUtil.findParentStemNames(objectName);
    boolean foundAncestorForAffiliation = false;
    for (String parentName: parentStemNames) {
      
      Map<String, GrouperDeprovisioningObjectAttributes> stemNameToObjectAttributes = stemsOfInterestForDeprovisioning.get(affiliationName);
      if (stemNameToObjectAttributes == null) {
        continue;
      }
      
      if (stemNameToObjectAttributes.containsKey(parentName)) {
        foundAncestorForAffiliation = true;
        break;
      }
    }
    
    return foundAncestorForAffiliation;
  }
  
  /**
   * 
   */
  public static Map<String, Object> fullSyncLogic() {
    
    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("deprovisioning");
    
    gcGrouperSync.setSyncEngine(GcGrouperSync.DEPROVISIONING);
    gcGrouperSync.getGcGrouperSyncDao().store();
    GcGrouperSyncJob gcGrouperSyncJob = gcGrouperSync.getGcGrouperSyncJobDao().jobRetrieveOrCreateBySyncType("full");
    gcGrouperSyncJob.waitForRelatedJobsToFinishThenRun(true);
    
    GcGrouperSyncHeartbeat gcGrouperSyncHeartbeat = new GcGrouperSyncHeartbeat();
    gcGrouperSyncHeartbeat.setGcGrouperSyncJob(gcGrouperSyncJob);
    gcGrouperSyncHeartbeat.setFullSync(true);
    gcGrouperSyncHeartbeat.addHeartbeatLogic(new Runnable() {
      @Override
      public void run() {
        
      }
    });
    if (!gcGrouperSyncHeartbeat.isStarted()) {
      gcGrouperSyncHeartbeat.runHeartbeatThread();
    }
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    
    RuntimeException runtimeException = null;
    
    try {
      
    
      Map<String, Map<String, GrouperDeprovisioningObjectAttributes>> allFoldersOfInterestForDeprovisioning = retrieveAllFoldersOfInterestForDeprovisioning();
      Map<String, Map<String, GrouperDeprovisioningObjectAttributes>> allGroupsOfInterestForDeprovisioning = retrieveAllGroupsOfInterestForDeprovisioning(allFoldersOfInterestForDeprovisioning);
      
      
      Set<String> affiliationsToProcess = new HashSet<String>();
      affiliationsToProcess.addAll(allFoldersOfInterestForDeprovisioning.keySet());
      affiliationsToProcess.addAll(allGroupsOfInterestForDeprovisioning.keySet());
      
      for (String affiliationName: affiliationsToProcess) {
        
        Map<String, GrouperDeprovisioningObjectAttributes> grouperDeprovisioningFolderAttributes = allFoldersOfInterestForDeprovisioning.get(affiliationName);
        Map<String, GrouperDeprovisioningObjectAttributes> grouperDeprovisioningGroupAttributes = allGroupsOfInterestForDeprovisioning.get(affiliationName);
        
        Set<GrouperDeprovisioningObjectAttributes> grouperDeprovisioningAttributesToProcess = new HashSet<GrouperDeprovisioningObjectAttributes>();
        if (grouperDeprovisioningFolderAttributes != null) {        
          grouperDeprovisioningAttributesToProcess.addAll(grouperDeprovisioningFolderAttributes.values());
        }
        if (grouperDeprovisioningGroupAttributes != null) {
          grouperDeprovisioningAttributesToProcess.addAll(grouperDeprovisioningGroupAttributes.values());
        }
        
        propagateAttributes(affiliationName, grouperDeprovisioningAttributesToProcess, grouperDeprovisioningFolderAttributes, debugMap);
        
      }
    
    } catch (RuntimeException re) {
      runtimeException = re;
    } finally {
      GcGrouperSyncHeartbeat.endAndWaitForThread(gcGrouperSyncHeartbeat);
      debugMap.put("finalLog", true);
      synchronized (GrouperDeprovisioningDaemonLogic.class) {
        try {
          if (gcGrouperSyncJob != null) {
            gcGrouperSyncJob.assignHeartbeatAndEndJob();
          }
        } catch (RuntimeException re2) {
          debugMap.put("exception2", GrouperClientUtils.getFullStackTrace(re2));
          if (runtimeException == null) {
            throw re2;
          }
          
        }
      }
      
      if (LOG.isDebugEnabled()) {
        LOG.debug(GrouperUtil.mapToString(debugMap));
      }
      
    }
    
    return debugMap;
    
  }
  
  /**
   * @param affiliationName
   * @param grouperDeprovisioningAttributesToProcess
   * @param grouperDeprovisioningFolderAttributes
   */
  public static void propagateAttributes(String affiliationName, Set<GrouperDeprovisioningObjectAttributes> grouperDeprovisioningAttributesToProcess,
      Map<String, GrouperDeprovisioningObjectAttributes> grouperDeprovisioningFolderAttributes, Map<String, Object> debugMap) {
    
    AttributeDefName attributeDefNameBase = GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameBase();
    AttributeDefName attributeDefNameDirectAssign = GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameDirectAssignment();
    
    AttributeDefName attributeDefNameAffiliation = GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameAffiliation(); 
    AttributeDefName attributeDefNameAllowAddsWhileDeprovisioned = GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameAllowAddsWhileDeprovisioned();
    AttributeDefName attributeDefNameAutoChangeLoader = GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameAutoChangeLoader();
    AttributeDefName attributeDefNameAutoSelectForRemoval = GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameAutoSelectForRemoval();
    AttributeDefName attributeDefNameDeprovision = GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameDeprovision();
    AttributeDefName attributeDefNameDirectAssignment = GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameDirectAssignment();

    AttributeDefName attributeDefNameEmailAddresses = GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameEmailAddresses();
    AttributeDefName attributeDefNameEmailBody = GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameEmailBody();
    AttributeDefName attributeDefNameInheritedFromFolderId = GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameInheritedFromFolderId();
    AttributeDefName attributeDefNameMailToGroup = GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameMailToGroup();

    
    AttributeDefName attributeDefNameSendEmail = GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameSendEmail();
    AttributeDefName attributeDefNameShowForRemoval = GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameShowForRemoval();
    AttributeDefName attributeDefNameStemScope = GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameStemScope();
    
    int deprovisioningAttributesFoldersDeleted = 0;
    int deprovisioningAttributesFoldersAddedOrUpdated = 0;
    int deprovisioningAttributesGroupsDeleted = 0;
    int deprovisioningAttributesGroupsAddedOrUpdated = 0;
    
    // get a map of all child -> parent, maybe this is cheaper than having to recalculate it multiple times per object
    Map<String, String> childToParent = new HashMap<String, String>();
    
    for (GrouperDeprovisioningObjectAttributes grouperDeprovisioningObjectAttribute : grouperDeprovisioningAttributesToProcess) {
      
      String objectName = grouperDeprovisioningObjectAttribute.getName();
      while (true) {
        
        String parentStemName = GrouperUtil.parentStemNameFromName(objectName);
        
        if (parentStemName != null) {
          if (childToParent.containsKey(objectName)) {
            break;
          }
          childToParent.put(objectName, parentStemName);
          objectName = parentStemName;
        } else {
          break;
        }
      }
      
      
    }
    
    
    for (GrouperDeprovisioningObjectAttributes grouperDeprovisioningObjectAttribute : GrouperUtil.nonNull(grouperDeprovisioningFolderAttributes).values()) {
      
      String objectName = grouperDeprovisioningObjectAttribute.getName();
      while (true) {
        
        String parentStemName = GrouperUtil.parentStemNameFromName(objectName);
        
        if (parentStemName != null) {
          if (childToParent.containsKey(objectName)) {
            break;
          }
          childToParent.put(objectName, parentStemName);
          objectName = parentStemName;
        } else {
          break;
        }
      }

    }
    
    // go through each group/folder and recompute what the attributes should be by looking at ancestor folders and if it doesn't match what's in the db, then update db
    for (GrouperDeprovisioningObjectAttributes grouperDeprovisionObjectAttribute : grouperDeprovisioningAttributesToProcess) {

      if ("true".equalsIgnoreCase(grouperDeprovisionObjectAttribute.getDirectAssign())) {
        continue;
      }
      
      GrouperDeprovisioningObjectAttributes ancestorGrouperDeprovisioningObjectAttribute = null;
      
      int depth = 0;
      String currObjectName = grouperDeprovisionObjectAttribute.getName();
      while (true) {
        depth++;
        GrouperUtil.assertion(depth < 1000, "Endless loop.");
        currObjectName = childToParent.get(currObjectName);
        if (currObjectName == null) {
          break;
        }
        
        GrouperDeprovisioningObjectAttributes currGrouperDeprovisioningObjectAttribute = GrouperUtil.nonNull(grouperDeprovisioningFolderAttributes).get(currObjectName);
        if (currGrouperDeprovisioningObjectAttribute != null && "true".equalsIgnoreCase(currGrouperDeprovisioningObjectAttribute.getDirectAssign())) {
          
          if (depth > 1 && "ONE".equalsIgnoreCase(currGrouperDeprovisioningObjectAttribute.getStemScope())) {
            // not applicable, continue going up the hierarchy
            continue;
          }
          
          ancestorGrouperDeprovisioningObjectAttribute = currGrouperDeprovisioningObjectAttribute;
          break;
        }
      }
      
      if (ancestorGrouperDeprovisioningObjectAttribute == null) {
        if (!GrouperUtil.isEmpty(grouperDeprovisionObjectAttribute.getAffiliation())) {
          // delete the marker
          
          AttributeAssignable object;
          
          if (grouperDeprovisionObjectAttribute.isOwnedByGroup()) {
            object = GroupFinder.findByName(GrouperSession.staticGrouperSession(), grouperDeprovisionObjectAttribute.getName(), false);
          } else {
            object = StemFinder.findByName(GrouperSession.staticGrouperSession(), grouperDeprovisionObjectAttribute.getName(), false);
          }
          
          if (object == null) {
            // guess it was deleted?
            continue;
          }
          
          if (LOG.isInfoEnabled()) {
            LOG.info("For affiliation= " + affiliationName + " and group/stem= " + grouperDeprovisionObjectAttribute.getName() + " deleting marker attribute");
          }
          
          if (StringUtils.isNotBlank(grouperDeprovisionObjectAttribute.getMarkerAttributeAssignId())) {
            
            AttributeAssign markerAttributeAssign = GrouperDAOFactory.getFactory().getAttributeAssign().findById(grouperDeprovisionObjectAttribute.getMarkerAttributeAssignId(), false);
            if (markerAttributeAssign != null) {
              markerAttributeAssign.delete();
            }
          }
          
          if (grouperDeprovisionObjectAttribute.isOwnedByGroup()) {
            deprovisioningAttributesGroupsDeleted++;
            
          } else {
            deprovisioningAttributesFoldersDeleted++;
          }
        }
      } else {
        
        String existingAffiliation = grouperDeprovisionObjectAttribute.getAffiliation();
        String existingAllowsAddsWhileDeprovisioned = grouperDeprovisionObjectAttribute.getAllowAddsWhileDeprovisioned();
        String existingAutoChangeLoader = grouperDeprovisionObjectAttribute.getAutoChangeLoader();
        String existingAutoSelectForRemoval = grouperDeprovisionObjectAttribute.getAutoSelectForRemoval();
        String existingDeprovision = grouperDeprovisionObjectAttribute.getDeprovision();
        String existingDirectAssign = grouperDeprovisionObjectAttribute.getDirectAssign();
        String existingEmailAddresses = grouperDeprovisionObjectAttribute.getEmailAddresses();
        String existingEmailBody = grouperDeprovisionObjectAttribute.getEmailBody();
        String existingMailToGroup = grouperDeprovisionObjectAttribute.getMailToGroup();
        String existingOwnerStemId = grouperDeprovisionObjectAttribute.getOwnerStemId();
        String existingSendEmail = grouperDeprovisionObjectAttribute.getSendEmail();
        String existingShowForRemoval = grouperDeprovisionObjectAttribute.getShowForRemoval();
        String existingStemScope = grouperDeprovisionObjectAttribute.getStemScope();
        
        String actualDirectAssign = "false";
        
        String actualAffiliation = ancestorGrouperDeprovisioningObjectAttribute.getAffiliation();
        String actualAllowsAddsWhileDeprovisioned = ancestorGrouperDeprovisioningObjectAttribute.getAllowAddsWhileDeprovisioned();
        String actualAutoChangeLoader = ancestorGrouperDeprovisioningObjectAttribute.getAutoChangeLoader();
        String actualAutoSelectForRemoval = ancestorGrouperDeprovisioningObjectAttribute.getAutoSelectForRemoval();
        String actualDeprovision = ancestorGrouperDeprovisioningObjectAttribute.getDeprovision();
        String actualEmailAddresses = ancestorGrouperDeprovisioningObjectAttribute.getEmailAddresses();
        String actualEmailBody = ancestorGrouperDeprovisioningObjectAttribute.getEmailBody();
        String actualMailToGroup = ancestorGrouperDeprovisioningObjectAttribute.getMailToGroup();
        String actualOwnerStemId = ancestorGrouperDeprovisioningObjectAttribute.getId();
        String actualSendEmail = ancestorGrouperDeprovisioningObjectAttribute.getSendEmail();
        String actualShowForRemoval = ancestorGrouperDeprovisioningObjectAttribute.getShowForRemoval();
        String actualStemScope = ancestorGrouperDeprovisioningObjectAttribute.getStemScope();
        
        if (!GrouperUtil.equals(existingDirectAssign, actualDirectAssign) ||
            !GrouperUtil.equals(existingAffiliation, actualAffiliation) ||
            !GrouperUtil.equals(existingAllowsAddsWhileDeprovisioned, actualAllowsAddsWhileDeprovisioned) ||
            !GrouperUtil.equals(existingAutoChangeLoader, actualAutoChangeLoader) ||
            !GrouperUtil.equals(existingAutoSelectForRemoval, actualAutoSelectForRemoval) ||
            !GrouperUtil.equals(existingEmailAddresses, actualEmailAddresses) ||
            !GrouperUtil.equals(existingEmailBody, actualEmailBody) ||
            !GrouperUtil.equals(existingMailToGroup, actualMailToGroup) ||
            !GrouperUtil.equals(existingOwnerStemId, actualOwnerStemId) ||
            !GrouperUtil.equals(existingSendEmail, actualSendEmail) ||
            !GrouperUtil.equals(existingShowForRemoval, actualShowForRemoval) ||
            !GrouperUtil.equals(existingStemScope, actualStemScope) ||
            !GrouperUtil.equals(existingDeprovision, actualDeprovision)) {

          AttributeAssignable object;
          
          if (grouperDeprovisionObjectAttribute.isOwnedByGroup()) {
            object = GroupFinder.findByName(GrouperSession.staticGrouperSession(), grouperDeprovisionObjectAttribute.getName(), false);
          } else {
            object = StemFinder.findByName(GrouperSession.staticGrouperSession(), grouperDeprovisionObjectAttribute.getName(), false);
          }
          
          if (object == null) {
            // guess it was deleted?
            continue;
          }
          
          HibernateSession.callbackHibernateSession(
              GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT,
              new HibernateHandler() {

                public Object callback(HibernateHandlerBean hibernateHandlerBean)
                    throws GrouperDAOException {
          
                  
                  AttributeAssign markerAssign = null;
                  
                  if (StringUtils.isNotBlank(grouperDeprovisionObjectAttribute.getMarkerAttributeAssignId())) {
                    markerAssign = GrouperDAOFactory.getFactory().getAttributeAssign().findById(grouperDeprovisionObjectAttribute.getMarkerAttributeAssignId(), false);
                  }
                  
                  
                  if (markerAssign == null) {
                    markerAssign = object.getAttributeDelegate().internal_addAttributeHelper(null, attributeDefNameBase, false, null).getAttributeAssign();
                  }
                  
                  if (!GrouperUtil.equals(existingDirectAssign, actualDirectAssign)) {
                    if (LOG.isInfoEnabled()) {
                      LOG.info("For " + affiliationName + " and group/stem =" + grouperDeprovisionObjectAttribute.getName() + " updating directAssign to: " + actualDirectAssign);
                    }
                    markerAssign.getAttributeValueDelegate().assignValue(attributeDefNameDirectAssign.getName(), actualDirectAssign);
                  }
                  
                  if (!GrouperUtil.equals(existingAllowsAddsWhileDeprovisioned, actualAllowsAddsWhileDeprovisioned)) {
                    if (LOG.isInfoEnabled()) {
                      LOG.info("For " + affiliationName + " and group/stem=" + grouperDeprovisionObjectAttribute.getName() + " updating allowsAddsWhileDeprovisioned to: " + actualAllowsAddsWhileDeprovisioned);
                    }
                    
                    if (StringUtils.isBlank(actualAllowsAddsWhileDeprovisioned)) {
                      markerAssign.getAttributeDelegate().removeAttribute(attributeDefNameAllowAddsWhileDeprovisioned);
                    } else {
                      markerAssign.getAttributeValueDelegate().assignValue(attributeDefNameAllowAddsWhileDeprovisioned.getName(), actualAllowsAddsWhileDeprovisioned);
                    }
                  }
                  
                  if (!GrouperUtil.equals(existingAffiliation, actualAffiliation)) {
                    if (LOG.isInfoEnabled()) {
                      LOG.info("For " + affiliationName + " and group/stem=" + grouperDeprovisionObjectAttribute.getName() + " updating affiliation to: " + actualAffiliation);
                    }
                    
                    if (StringUtils.isBlank(actualAffiliation)) {
                      markerAssign.getAttributeDelegate().removeAttribute(attributeDefNameAffiliation);
                    } else {
                      markerAssign.getAttributeValueDelegate().assignValue(attributeDefNameAffiliation.getName(), actualAffiliation);
                    }
                  }
                  
                  if (!GrouperUtil.equals(existingAutoChangeLoader, actualAutoChangeLoader)) {
                    if (LOG.isInfoEnabled()) {
                      LOG.info("For " + affiliationName + " and group/stem=" + grouperDeprovisionObjectAttribute.getName() + " updating autoChangeLoader to: " + actualAutoChangeLoader);
                    }
                    if (StringUtils.isBlank(actualAutoChangeLoader)) {
                      markerAssign.getAttributeDelegate().removeAttribute(attributeDefNameAutoChangeLoader);
                    } else {
                      markerAssign.getAttributeValueDelegate().assignValue(attributeDefNameAutoChangeLoader.getName(), actualAutoChangeLoader);
                    }
                  }
                  
                  if (!GrouperUtil.equals(existingAutoSelectForRemoval, actualAutoSelectForRemoval)) {
                    if (LOG.isInfoEnabled()) {
                      LOG.info("For " + affiliationName + " and group/stem=" + grouperDeprovisionObjectAttribute.getName() + " updating autoSelectForRemoval to: " + actualAutoSelectForRemoval);
                    }
                    if (StringUtils.isBlank(actualAutoSelectForRemoval)) {
                      markerAssign.getAttributeDelegate().removeAttribute(attributeDefNameAutoSelectForRemoval);
                    } else {
                      markerAssign.getAttributeValueDelegate().assignValue(attributeDefNameAutoSelectForRemoval.getName(), actualAutoSelectForRemoval);
                    }
                  }
                  
                  if (!GrouperUtil.equals(existingEmailAddresses, actualEmailAddresses)) {
                    if (LOG.isInfoEnabled()) {
                      LOG.info("For " + affiliationName + " and group/stem=" + grouperDeprovisionObjectAttribute.getName() + " updating emailAddresses to: " + actualEmailAddresses);
                    }
                    if (StringUtils.isBlank(actualEmailAddresses)) {
                      markerAssign.getAttributeDelegate().removeAttribute(attributeDefNameEmailAddresses);
                    } else {
                      markerAssign.getAttributeValueDelegate().assignValue(attributeDefNameEmailAddresses.getName(), actualEmailAddresses);
                    }
                  }
                  
                  if (!GrouperUtil.equals(existingEmailBody, actualEmailBody)) {
                    if (LOG.isInfoEnabled()) {
                      LOG.info("For " + affiliationName + " and group/stem=" + grouperDeprovisionObjectAttribute.getName() + " updating emailBody to: " + actualEmailBody);
                    }
                    if (StringUtils.isBlank(actualEmailBody)) {
                      markerAssign.getAttributeDelegate().removeAttribute(attributeDefNameEmailBody);
                    } else {
                      markerAssign.getAttributeValueDelegate().assignValue(attributeDefNameEmailBody.getName(), actualEmailBody);
                    }
                  }
                  
                  if (!GrouperUtil.equals(existingMailToGroup, actualMailToGroup)) {
                    if (LOG.isInfoEnabled()) {
                      LOG.info("For " + affiliationName + " and group/stem=" + grouperDeprovisionObjectAttribute.getName() + " updating mailToGroup to: " + actualMailToGroup);
                    }
                    if (StringUtils.isBlank(actualMailToGroup)) {
                      markerAssign.getAttributeDelegate().removeAttribute(attributeDefNameMailToGroup);
                    } else {
                      markerAssign.getAttributeValueDelegate().assignValue(attributeDefNameMailToGroup.getName(), actualMailToGroup);
                    }
                  }
                  
                  if (!GrouperUtil.equals(existingOwnerStemId, actualOwnerStemId)) {
                    if (LOG.isInfoEnabled()) {
                      LOG.info("For " + affiliationName + " and group/stem=" + grouperDeprovisionObjectAttribute.getName() + " updating ownerStemId to: " + actualOwnerStemId);
                    }
                    if (StringUtils.isBlank(actualOwnerStemId)) {
                      markerAssign.getAttributeDelegate().removeAttribute(attributeDefNameInheritedFromFolderId);
                    } else {
                      markerAssign.getAttributeValueDelegate().assignValue(attributeDefNameInheritedFromFolderId.getName(), actualOwnerStemId);
                    }
                  }
                  
                  if (!GrouperUtil.equals(existingSendEmail, actualSendEmail)) {
                    if (LOG.isInfoEnabled()) {
                      LOG.info("For " + affiliationName + " and group/stem=" + grouperDeprovisionObjectAttribute.getName() + " updating sendEmail to: " + actualSendEmail);
                    }
                    if (StringUtils.isBlank(actualSendEmail)) {
                      markerAssign.getAttributeDelegate().removeAttribute(attributeDefNameSendEmail);
                    } else {
                      markerAssign.getAttributeValueDelegate().assignValue(attributeDefNameSendEmail.getName(), actualSendEmail);
                    }
                  }
                  
                  if (!GrouperUtil.equals(existingShowForRemoval, actualShowForRemoval)) {
                    if (LOG.isInfoEnabled()) {
                      LOG.info("For " + affiliationName + " and group/stem=" + grouperDeprovisionObjectAttribute.getName() + " updating showForRemoval to: " + actualShowForRemoval);
                    }
                    if (StringUtils.isBlank(actualShowForRemoval)) {
                      markerAssign.getAttributeDelegate().removeAttribute(attributeDefNameShowForRemoval);
                    } else {
                      markerAssign.getAttributeValueDelegate().assignValue(attributeDefNameShowForRemoval.getName(), actualShowForRemoval);
                    }
                  }
                  
                  if (!GrouperUtil.equals(existingStemScope, actualStemScope)) {
                    if (LOG.isInfoEnabled()) {
                      LOG.info("For " + affiliationName + " and group/stem=" + grouperDeprovisionObjectAttribute.getName() + " updating stemScope to: " + actualStemScope);
                    }
                    if (StringUtils.isBlank(actualStemScope)) {
                      markerAssign.getAttributeDelegate().removeAttribute(attributeDefNameStemScope);
                    } else {
                      markerAssign.getAttributeValueDelegate().assignValue(attributeDefNameStemScope.getName(), actualStemScope);
                    }
                  }

                  if (!GrouperUtil.equals(existingDeprovision, actualDeprovision)) {
                    if (LOG.isInfoEnabled()) {
                      LOG.info("For " + affiliationName + " and group/stem=" + grouperDeprovisionObjectAttribute.getName() + " updating deprovision to: " + actualDeprovision);
                    }
                    if (StringUtils.isBlank(actualDeprovision)) {
                      markerAssign.getAttributeDelegate().removeAttribute(attributeDefNameDeprovision);
                    } else {
                      markerAssign.getAttributeValueDelegate().assignValue(attributeDefNameDeprovision.getName(), actualDeprovision);
                    }
                  }
                  
                  return null;
                }
              });
          
          if (grouperDeprovisionObjectAttribute.isOwnedByGroup()) {
            deprovisioningAttributesGroupsAddedOrUpdated++;
           
          } else {
            deprovisioningAttributesFoldersAddedOrUpdated++;
          }
        }
      }
    }
    
    if (deprovisioningAttributesGroupsAddedOrUpdated > 0) {
      debugMap.put(affiliationName+"_deprovisioningAttributesGroupsAddedOrUpdated", deprovisioningAttributesGroupsAddedOrUpdated);
    }
    
    if (deprovisioningAttributesFoldersAddedOrUpdated > 0) {
      debugMap.put(affiliationName+"_deprovisioningAttributesFoldersAddedOrUpdated", deprovisioningAttributesFoldersAddedOrUpdated);
    }
    
    if (deprovisioningAttributesGroupsDeleted > 0) {
      debugMap.put(affiliationName+"_deprovisioningAttributesGroupsDeleted", deprovisioningAttributesGroupsDeleted);
    }
    
    if (deprovisioningAttributesFoldersDeleted > 0) {
      debugMap.put(affiliationName+"_deprovisioningAttributesFoldersDeleted", deprovisioningAttributesFoldersDeleted);
    }
  }
  
  public static Map<String, GrouperDeprovisioningObjectAttributes> retrieveDeprovisioningAttributesByGroup(String groupId) {
    
    Map<String, GrouperDeprovisioningObjectAttributes> results = new HashMap<String, GrouperDeprovisioningObjectAttributes>();

    {
      String sql = "SELECT " +
          "    gg.name, " + 
          "    gadn_config.name, " + 
          "    gaav_config.value_string, " + 
          "    gaav_affiliation.value_string as affiliation, " + 
          "    gaa_marker.id " +  
          "FROM " + 
          "    grouper_groups gg, " + 
          "    grouper_attribute_assign gaa_marker, " + 
          "    grouper_attribute_assign gaa_affiliation, " + 
          "    grouper_attribute_assign gaa_config, " + 
          "    grouper_attribute_assign_value gaav_affiliation, " + 
          "    grouper_attribute_assign_value gaav_config, " + 
          "    grouper_attribute_def_name gadn_marker, " + 
          "    grouper_attribute_def_name gadn_affiliation, " + 
          "    grouper_attribute_def_name gadn_config " +
          "WHERE " + 
          "    gg.id = ? " + 
          "    AND gg.id = gaa_marker.owner_group_id " + 
          "    AND gaa_marker.attribute_def_name_id = gadn_marker.id " + 
          "    AND gadn_marker.name = ? " + 
          "    AND gaa_marker.id = gaa_affiliation.owner_attribute_assign_id " + 
          "    AND gaa_affiliation.attribute_def_name_id = gadn_affiliation.id " + 
          "    AND gadn_affiliation.name = ? " + 
          "    AND gaav_affiliation.attribute_assign_id = gaa_affiliation.id " + 
          "    AND gaa_marker.id = gaa_config.owner_attribute_assign_id " + 
          "    AND gaav_config.attribute_assign_id = gaa_config.id " + 
          "    AND gadn_config.id = gaa_config.attribute_def_name_id " + 
          "    AND gaa_marker.enabled = 'T' " + 
          "    AND gaa_affiliation.enabled = 'T' " + 
          "    AND gaa_config.enabled = 'T' ";
      
      
      List<Object> paramsInitial = new ArrayList<Object>();
      
      paramsInitial.add(groupId);
      paramsInitial.add(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameBase().getName());
      paramsInitial.add(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameAffiliation().getName());
      
      List<Type> typesInitial = new ArrayList<Type>();
      typesInitial.add(StringType.INSTANCE);
      typesInitial.add(StringType.INSTANCE);
      typesInitial.add(StringType.INSTANCE);
  
      List<String[]> queryResults = HibernateSession.bySqlStatic().listSelect(String[].class, sql, paramsInitial, typesInitial);
      
      GrouperDeprovisioningObjectAttributes grouperDeprovisioningAttributes = null;
      
      for (String[] queryResult : queryResults) {
        String groupName = queryResult[0];
        String configName = queryResult[1];
        String configValue = queryResult[2];
        String affiliation = queryResult[3];
        String markerAttributeAssignId = queryResult[4];
        
        grouperDeprovisioningAttributes = results.get(affiliation);
        if (grouperDeprovisioningAttributes == null) {
          grouperDeprovisioningAttributes = new GrouperDeprovisioningObjectAttributes(groupId, groupName, markerAttributeAssignId);
          results.put(affiliation, grouperDeprovisioningAttributes);
        }
        
        grouperDeprovisioningAttributes.setOwnedByGroup(true);
        
        if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameAffiliation().getName())) {
          grouperDeprovisioningAttributes.setAffiliation(configValue);
        } else if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameAllowAddsWhileDeprovisioned().getName())) {
          grouperDeprovisioningAttributes.setAllowAddsWhileDeprovisioned(configValue);
        } else if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameAutoChangeLoader().getName())) {
          grouperDeprovisioningAttributes.setAutoChangeLoader(configValue);
        } else if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameAutoSelectForRemoval().getName())) {
          grouperDeprovisioningAttributes.setAutoSelectForRemoval(configValue);
        } else if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameDeprovision().getName())) {
          grouperDeprovisioningAttributes.setDeprovision(configValue);
        } else if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameDirectAssignment().getName())) {
          grouperDeprovisioningAttributes.setDirectAssign(configValue);
        } else if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameEmailAddresses().getName())) {
          grouperDeprovisioningAttributes.setEmailAddresses(configValue);
        } else if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameEmailBody().getName())) {
          grouperDeprovisioningAttributes.setEmailBody(configValue);
        } else if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameInheritedFromFolderId().getName())) {
          grouperDeprovisioningAttributes.setOwnerStemId(configValue);
        } else if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameMailToGroup().getName())) {
          grouperDeprovisioningAttributes.setMailToGroup(configValue);
        } else if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameSendEmail().getName())) {
          grouperDeprovisioningAttributes.setSendEmail(configValue);
        } else if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameShowForRemoval().getName())) {
          grouperDeprovisioningAttributes.setShowForRemoval(configValue);
        } else if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameStemScope().getName())) {
          grouperDeprovisioningAttributes.setStemScope(configValue);
        }
        
      }
    }
    
    
    return results;
    
  }
  
  
  public static Map<String, GrouperDeprovisioningObjectAttributes> retrieveDeprovisioningAttributesByStem(String stemId) {
    
    Map<String, GrouperDeprovisioningObjectAttributes> results = new HashMap<String, GrouperDeprovisioningObjectAttributes>();

    {
      String sql = "SELECT " +
          "    gs.name, " + 
          "    gadn_config.name, " + 
          "    gaav_config.value_string, " + 
          "    gaav_affiliation.value_string as affiliation, " + 
          "    gaa_marker.id " + 
          "FROM " + 
          "    grouper_stems gs, " + 
          "    grouper_attribute_assign gaa_marker, " +
          "    grouper_attribute_assign gaa_affiliation, " + 
          "    grouper_attribute_assign gaa_config, " + 
          "    grouper_attribute_assign_value gaav_affiliation, " + 
          "    grouper_attribute_assign_value gaav_config, " + 
          "    grouper_attribute_def_name gadn_marker, " + 
          "    grouper_attribute_def_name gadn_affiliation, " + 
          "    grouper_attribute_def_name gadn_config " + 
          "WHERE " + 
          "    gs.id = ? " + 
          "    AND gs.id = gaa_marker.owner_stem_id " + 
          "    AND gaa_marker.attribute_def_name_id = gadn_marker.id " + 
          "    AND gadn_marker.name = ? " + 
          "    AND gaa_marker.id = gaa_affiliation.owner_attribute_assign_id " + 
          "    AND gaa_affiliation.attribute_def_name_id = gadn_affiliation.id " + 
          "    AND gadn_affiliation.name = ? " + 
          "    AND gaav_affiliation.attribute_assign_id = gaa_affiliation.id " + 
          "    AND gaa_marker.id = gaa_config.owner_attribute_assign_id " + 
          "    AND gaav_config.attribute_assign_id = gaa_config.id " + 
          "    AND gadn_config.id = gaa_config.attribute_def_name_id " + 
          "    AND gaa_marker.enabled = 'T' " + 
          "    AND gaa_affiliation.enabled = 'T' " + 
          "    AND gaa_config.enabled = 'T' ";
      
      
      List<Object> paramsInitial = new ArrayList<Object>();
      
      paramsInitial.add(stemId);
      paramsInitial.add(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameBase().getName());
      paramsInitial.add(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameAffiliation().getName());
      
      List<Type> typesInitial = new ArrayList<Type>();
      typesInitial.add(StringType.INSTANCE);
      typesInitial.add(StringType.INSTANCE);
      typesInitial.add(StringType.INSTANCE);
  
      List<String[]> queryResults = HibernateSession.bySqlStatic().listSelect(String[].class, sql, paramsInitial, typesInitial);
      
      GrouperDeprovisioningObjectAttributes grouperDeprovisioningAttributes = null;
      
      for (String[] queryResult : queryResults) {
        String stemName = queryResult[0];
        String configName = queryResult[1];
        String configValue = queryResult[2];
        String affiliation = queryResult[3];
        String markerAttributeAssignId = queryResult[4];
        
        grouperDeprovisioningAttributes = results.get(affiliation);
        if (grouperDeprovisioningAttributes == null) {
          grouperDeprovisioningAttributes = new GrouperDeprovisioningObjectAttributes(stemId, stemName, markerAttributeAssignId);
          results.put(affiliation, grouperDeprovisioningAttributes);
        }
        
        grouperDeprovisioningAttributes.setOwnedByStem(true);
        
        if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameAffiliation().getName())) {
          grouperDeprovisioningAttributes.setAffiliation(configValue);
        } else if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameAllowAddsWhileDeprovisioned().getName())) {
          grouperDeprovisioningAttributes.setAllowAddsWhileDeprovisioned(configValue);
        } else if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameAutoChangeLoader().getName())) {
          grouperDeprovisioningAttributes.setAutoChangeLoader(configValue);
        } else if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameAutoSelectForRemoval().getName())) {
          grouperDeprovisioningAttributes.setAutoSelectForRemoval(configValue);
        } else if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameDeprovision().getName())) {
          grouperDeprovisioningAttributes.setDeprovision(configValue);
        } else if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameDirectAssignment().getName())) {
          grouperDeprovisioningAttributes.setDirectAssign(configValue);
        } else if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameEmailAddresses().getName())) {
          grouperDeprovisioningAttributes.setEmailAddresses(configValue);
        } else if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameEmailBody().getName())) {
          grouperDeprovisioningAttributes.setEmailBody(configValue);
        } else if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameInheritedFromFolderId().getName())) {
          grouperDeprovisioningAttributes.setOwnerStemId(configValue);
        } else if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameMailToGroup().getName())) {
          grouperDeprovisioningAttributes.setMailToGroup(configValue);
        } else if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameSendEmail().getName())) {
          grouperDeprovisioningAttributes.setSendEmail(configValue);
        } else if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameShowForRemoval().getName())) {
          grouperDeprovisioningAttributes.setShowForRemoval(configValue);
        } else if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameStemScope().getName())) {
          grouperDeprovisioningAttributes.setStemScope(configValue);
        }
      }
    }
    
    
    return results;
    
  }
  
  /**
   * get deprovisioning attributes for a folder and its parents
   * @param stemName
   * @return the attributes
   */
  public static Map<String, Map<String, GrouperDeprovisioningObjectAttributes>> retrieveFolderAndAncestorDeprovisioningAttributesByFolder(String stemName) {
    
    Map<String, Map<String, GrouperDeprovisioningObjectAttributes>> results = new HashMap<String, Map<String, GrouperDeprovisioningObjectAttributes>>();

    {
      String sql = "SELECT " +
          "    gs_then_has_stem.id, " + 
          "    gs_then_has_stem.name, " + 
          "    gadn_config.name, " + 
          "    gaav_config.value_string, " + 
          "    gaav_affiliation.value_string as affiliation, " + 
          "    gaa_marker.id " + 
          "FROM " + 
          "    grouper_stems gs, " + 
          "    grouper_stem_set gss, " + 
          "    grouper_stems gs_then_has_stem, " +
          "    grouper_attribute_assign gaa_marker, " + 
          "    grouper_attribute_assign gaa_affiliation, " + 
          "    grouper_attribute_assign gaa_config, " + 
          "    grouper_attribute_assign_value gaav_affiliation, " + 
          "    grouper_attribute_assign_value gaav_config, " + 
          "    grouper_attribute_def_name gadn_marker, " + 
          "    grouper_attribute_def_name gadn_affiliation, " + 
          "    grouper_attribute_def_name gadn_config " + 
          "WHERE " + 
          "    gs.name = ? " +
          "    AND gs.id = gss.if_has_stem_id " +
          "    AND gss.then_has_stem_id = gs_then_has_stem.id " + 
          "    AND gss.then_has_stem_id = gaa_marker.owner_stem_id " + 
          "    AND gaa_marker.attribute_def_name_id = gadn_marker.id " + 
          "    AND gadn_marker.name = ? " + 
          "    AND gaa_marker.id = gaa_affiliation.owner_attribute_assign_id " + 
          "    AND gaa_affiliation.attribute_def_name_id = gadn_affiliation.id " + 
          "    AND gadn_affiliation.name = ? " + 
          "    AND gaav_affiliation.attribute_assign_id = gaa_affiliation.id " + 
          "    AND gaa_marker.id = gaa_config.owner_attribute_assign_id " + 
          "    AND gaav_config.attribute_assign_id = gaa_config.id " + 
          "    AND gadn_config.id = gaa_config.attribute_def_name_id " + 
          "    AND gaa_marker.enabled = 'T' " + 
          "    AND gaa_affiliation.enabled = 'T' " + 
          "    AND gaa_config.enabled = 'T' ";
      
      List<Object> paramsInitial = new ArrayList<Object>();
      paramsInitial.add(stemName);
      paramsInitial.add(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameBase().getName());
      paramsInitial.add(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameAffiliation().getName());
      
      List<Type> typesInitial = new ArrayList<Type>();
      typesInitial.add(StringType.INSTANCE);
      typesInitial.add(StringType.INSTANCE);
      typesInitial.add(StringType.INSTANCE);
      
      List<String[]> queryResults = HibernateSession.bySqlStatic().listSelect(String[].class, sql, paramsInitial, typesInitial);
      for (String[] queryResult : queryResults) {
        String stemId = queryResult[0];
        String folderName = queryResult[1];
        String configName = queryResult[2];
        String configValue = queryResult[3];
        String affiliation = queryResult[4];
        String markerAttributeAssignId = queryResult[5];
        
        Map<String, GrouperDeprovisioningObjectAttributes> stemToDeprovisioningAttributes = results.get(affiliation);
        if (stemToDeprovisioningAttributes == null) {
          stemToDeprovisioningAttributes = new HashMap<String, GrouperDeprovisioningObjectAttributes>();
          results.put(affiliation, stemToDeprovisioningAttributes);
        }
        
        GrouperDeprovisioningObjectAttributes grouperDeprovisioningAttributes = stemToDeprovisioningAttributes.get(folderName);
        if (grouperDeprovisioningAttributes == null) {
          grouperDeprovisioningAttributes = new GrouperDeprovisioningObjectAttributes(stemId, folderName, markerAttributeAssignId);
          stemToDeprovisioningAttributes.put(folderName, grouperDeprovisioningAttributes);
          grouperDeprovisioningAttributes.setOwnedByStem(true);
        }
        
        if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameAffiliation().getName())) {
          grouperDeprovisioningAttributes.setAffiliation(configValue);
        } else if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameAllowAddsWhileDeprovisioned().getName())) {
          grouperDeprovisioningAttributes.setAllowAddsWhileDeprovisioned(configValue);
        } else if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameAutoChangeLoader().getName())) {
          grouperDeprovisioningAttributes.setAutoChangeLoader(configValue);
        } else if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameAutoSelectForRemoval().getName())) {
          grouperDeprovisioningAttributes.setAutoSelectForRemoval(configValue);
        } else if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameDeprovision().getName())) {
          grouperDeprovisioningAttributes.setDeprovision(configValue);
        } else if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameDirectAssignment().getName())) {
          grouperDeprovisioningAttributes.setDirectAssign(configValue);
        } else if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameEmailAddresses().getName())) {
          grouperDeprovisioningAttributes.setEmailAddresses(configValue);
        } else if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameEmailBody().getName())) {
          grouperDeprovisioningAttributes.setEmailBody(configValue);
        } else if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameInheritedFromFolderId().getName())) {
          grouperDeprovisioningAttributes.setOwnerStemId(configValue);
        } else if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameMailToGroup().getName())) {
          grouperDeprovisioningAttributes.setMailToGroup(configValue);
        } else if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameSendEmail().getName())) {
          grouperDeprovisioningAttributes.setSendEmail(configValue);
        } else if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameShowForRemoval().getName())) {
          grouperDeprovisioningAttributes.setShowForRemoval(configValue);
        } else if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameStemScope().getName())) {
          grouperDeprovisioningAttributes.setStemScope(configValue);
        }
        
      }
    }
    
    return results;
  }
  
  
  /**
   * @return stem id if is/was a direct folder assignment
   */
  public static String retrieveStemIdIfDirectStemAssignmentByPITMarkerAttributeAssignId(String markerAttributeAssignId) {

    String sql = "SELECT gps.source_id " +
        "FROM " + 
        "    grouper_pit_stems gps, " +
        "    grouper_pit_attribute_assign gpaa_marker, " + 
        "    grouper_pit_attribute_assign gpaa_affiliation, " + 
        "    grouper_pit_attribute_assign gpaa_direct, " + 
        "    grouper_pit_attr_assn_value gpaav_affiliation, " + 
        "    grouper_pit_attr_assn_value gpaav_direct, " + 
        "    grouper_pit_attr_def_name gpadn_marker, " + 
        "    grouper_pit_attr_def_name gpadn_affiliation, " + 
        "    grouper_pit_attr_def_name gpadn_direct " + 
        "WHERE " + 
        "    gpaa_marker.id = ? " +
        "    AND gpaa_marker.owner_stem_id = gps.id " +
        "    AND gpaa_marker.attribute_def_name_id = gpadn_marker.id " + 
        "    AND gpadn_marker.name = ? " + 
        "    AND gpaa_marker.id = gpaa_affiliation.owner_attribute_assign_id " + 
        "    AND gpaa_affiliation.attribute_def_name_id = gpadn_affiliation.id " + 
        "    AND gpadn_affiliation.name = ? " + 
        "    AND gpaav_affiliation.attribute_assign_id = gpaa_affiliation.id " + 
        "    AND gpaa_marker.id = gpaa_direct.owner_attribute_assign_id " + 
        "    AND gpaa_direct.attribute_def_name_id = gpadn_direct.id " + 
        "    AND gpadn_direct.name = ? " + 
        "    AND gpaav_direct.attribute_assign_id = gpaa_direct.id " +
        "    AND gpaav_direct.value_string = 'true' ";
    
    List<Object> paramsInitial = new ArrayList<Object>();
    paramsInitial.add(markerAttributeAssignId);
    
    paramsInitial.add(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameBase().getName());
    paramsInitial.add(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameAffiliation().getName());
    paramsInitial.add(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameDirectAssignment().getName());
    
    List<Type> typesInitial = new ArrayList<Type>();
    typesInitial.add(StringType.INSTANCE);
    typesInitial.add(StringType.INSTANCE);
    typesInitial.add(StringType.INSTANCE);
    typesInitial.add(StringType.INSTANCE);

    List<String> stemIds = HibernateSession.bySqlStatic().listSelect(String.class, sql, paramsInitial, typesInitial);
    if (stemIds.size() > 0) {
      return stemIds.get(0);
    }
    
    return null;
  }
  
  /**
   * @return group id if is/was a direct group assignment
   */
  public static String retrieveGroupIdIfDirectGroupAssignmentByPITMarkerAttributeAssignId(String markerAttributeAssignId) {

    String sql = "SELECT gpg.source_id " +
        "FROM " + 
        "    grouper_pit_groups gpg, " +
        "    grouper_pit_attribute_assign gpaa_marker, " + 
        "    grouper_pit_attribute_assign gpaa_direct, " + 
        "    grouper_pit_attr_assn_value gpaav_direct, " + 
        "    grouper_pit_attr_def_name gpadn_marker, " + 
        "    grouper_pit_attr_def_name gpadn_direct " + 
        "WHERE " + 
        "    gpaa_marker.id = ? " +
        "    AND gpaa_marker.owner_group_id = gpg.id " +
        "    AND gpaa_marker.attribute_def_name_id = gpadn_marker.id " + 
        "    AND gpadn_marker.name = ? " + 
        "    AND gpaa_marker.id = gpaa_direct.owner_attribute_assign_id " + 
        "    AND gpaa_direct.attribute_def_name_id = gpadn_direct.id " + 
        "    AND gpadn_direct.name = ? " + 
        "    AND gpaav_direct.attribute_assign_id = gpaa_direct.id " +
        "    AND gpaav_direct.value_string = 'true' ";
    
    List<Object> paramsInitial = new ArrayList<Object>();
    
    paramsInitial.add(markerAttributeAssignId);
    paramsInitial.add(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameBase().getName());
    paramsInitial.add(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameDirectAssignment().getName());
    
    List<Type> typesInitial = new ArrayList<Type>();
    typesInitial.add(StringType.INSTANCE);
    typesInitial.add(StringType.INSTANCE);
    typesInitial.add(StringType.INSTANCE);

    List<String> groupIds = HibernateSession.bySqlStatic().listSelect(String.class, sql, paramsInitial, typesInitial);
    if (groupIds.size() > 0) {
      return groupIds.get(0);
    }
    
    return null;
  }
  
  
  /**
   * get deprovisioning attributes for a folder and its folder children
   * @param childStemId
   * @return the attributes
   */
  public static Map<String, Map<String, GrouperDeprovisioningObjectAttributes>> retrieveChildDeprovisioningFolderAttributesByFolder(String parentStemId) {

    Map<String, Map<String, GrouperDeprovisioningObjectAttributes>> results = new HashMap<String, Map<String, GrouperDeprovisioningObjectAttributes>>();

    {
      String sql = "SELECT " +
          "    gs_if_has_stem.id, " + 
          "    gs_if_has_stem.name, " +
          "    gadn_config.name, " + 
          "    gaav_config.value_string, " + 
          "    gaav_affiliation.value_string as affiliation, " + 
          "    gaa_marker.id " + 
          "FROM " + 
          "    grouper_stems gs, " + 
          "    grouper_stem_set gss, " +
          "    grouper_stems gs_if_has_stem, " +
          "    grouper_attribute_assign gaa_marker, " + 
          "    grouper_attribute_assign gaa_affiliation, " + 
          "    grouper_attribute_assign gaa_config, " + 
          "    grouper_attribute_assign_value gaav_affiliation, " + 
          "    grouper_attribute_assign_value gaav_config, " + 
          "    grouper_attribute_def_name gadn_marker, " + 
          "    grouper_attribute_def_name gadn_affiliation, " + 
          "    grouper_attribute_def_name gadn_config " + 
          "WHERE " + 
          "    gs.id = ? " + 
          "    AND gs.id = gss.then_has_stem_id " +
          "    AND gss.if_has_stem_id = gs_if_has_stem.id " + 
          "    AND gss.if_has_stem_id = gaa_marker.owner_stem_id " + 
          "    AND gaa_marker.attribute_def_name_id = gadn_marker.id " + 
          "    AND gadn_marker.name = ? " + 
          "    AND gaa_marker.id = gaa_affiliation.owner_attribute_assign_id " + 
          "    AND gaa_affiliation.attribute_def_name_id = gadn_affiliation.id " + 
          "    AND gadn_affiliation.name = ? " + 
          "    AND gaav_affiliation.attribute_assign_id = gaa_affiliation.id " + 
          "    AND gaa_marker.id = gaa_config.owner_attribute_assign_id " + 
          "    AND gaav_config.attribute_assign_id = gaa_config.id " + 
          "    AND gadn_config.id = gaa_config.attribute_def_name_id " + 
          "    AND gaa_marker.enabled = 'T' " + 
          "    AND gaa_affiliation.enabled = 'T' " + 
          "    AND gaa_config.enabled = 'T' ";
      
      List<Object> paramsInitial = new ArrayList<Object>();
      
      paramsInitial.add(parentStemId);
      
      paramsInitial.add(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameBase().getName());
      paramsInitial.add(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameAffiliation().getName());
      
      List<Type> typesInitial = new ArrayList<Type>();
      typesInitial.add(StringType.INSTANCE);
      typesInitial.add(StringType.INSTANCE);
      typesInitial.add(StringType.INSTANCE);
  
      List<String[]> queryResults = HibernateSession.bySqlStatic().listSelect(String[].class, sql, paramsInitial, typesInitial);
      for (String[] queryResult : queryResults) {
        String stemId = queryResult[0];
        String stemName = queryResult[1];
        String configName = queryResult[2];
        String configValue = queryResult[3];
        String affiliation = queryResult[4];
        String markerAttributeAssignId = queryResult[5];
        
        
        Map<String, GrouperDeprovisioningObjectAttributes> stemToDeprovisioningAttributes = results.get(affiliation);
        if (stemToDeprovisioningAttributes == null) {
          stemToDeprovisioningAttributes = new HashMap<String, GrouperDeprovisioningObjectAttributes>();
          results.put(affiliation, stemToDeprovisioningAttributes);
        }
        
        GrouperDeprovisioningObjectAttributes grouperDeprovisioningAttributes = stemToDeprovisioningAttributes.get(stemName);
    
        if (grouperDeprovisioningAttributes == null) {
          grouperDeprovisioningAttributes = new GrouperDeprovisioningObjectAttributes(stemId, stemName, markerAttributeAssignId);
          stemToDeprovisioningAttributes.put(stemName, grouperDeprovisioningAttributes);
          grouperDeprovisioningAttributes.setOwnedByStem(true);
        }
        
        if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameAffiliation().getName())) {
          grouperDeprovisioningAttributes.setAffiliation(configValue);
        } else if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameAllowAddsWhileDeprovisioned().getName())) {
          grouperDeprovisioningAttributes.setAllowAddsWhileDeprovisioned(configValue);
        } else if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameAutoChangeLoader().getName())) {
          grouperDeprovisioningAttributes.setAutoChangeLoader(configValue);
        } else if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameAutoSelectForRemoval().getName())) {
          grouperDeprovisioningAttributes.setAutoSelectForRemoval(configValue);
        } else if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameDeprovision().getName())) {
          grouperDeprovisioningAttributes.setDeprovision(configValue);
        } else if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameDirectAssignment().getName())) {
          grouperDeprovisioningAttributes.setDirectAssign(configValue);
        } else if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameEmailAddresses().getName())) {
          grouperDeprovisioningAttributes.setEmailAddresses(configValue);
        } else if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameEmailBody().getName())) {
          grouperDeprovisioningAttributes.setEmailBody(configValue);
        } else if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameInheritedFromFolderId().getName())) {
          grouperDeprovisioningAttributes.setOwnerStemId(configValue);
        } else if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameMailToGroup().getName())) {
          grouperDeprovisioningAttributes.setMailToGroup(configValue);
        } else if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameSendEmail().getName())) {
          grouperDeprovisioningAttributes.setSendEmail(configValue);
        } else if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameShowForRemoval().getName())) {
          grouperDeprovisioningAttributes.setShowForRemoval(configValue);
        } else if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameStemScope().getName())) {
          grouperDeprovisioningAttributes.setStemScope(configValue);
        }
        
      }

    }
    
    
    return results;
  }
  
  public static void populateFolderChildrenOfAFolderWhichMayOrMayNotHaveAttributes(String parentStemId, Map<String, Map<String, GrouperDeprovisioningObjectAttributes>> mapOfAffiliationToFolderNameToAttributes) {
    
    String sql = "SELECT " + 
        "    gs.id, " + 
        "    gs.name " +
        "FROM " + 
        "    grouper_stem_set gss, " +
        "    grouper_stems gs " + 
        "WHERE " + 
        "    gss.then_has_stem_id = ?" +
        "    AND gss.if_has_stem_id = gs.id ";
    
    List<Object> paramsInitial = new ArrayList<Object>();
    paramsInitial.add(parentStemId);

    List<Type> typesInitial = new ArrayList<Type>();
    typesInitial.add(StringType.INSTANCE);

    List<String[]> queryResults = HibernateSession.bySqlStatic().listSelect(String[].class, sql, paramsInitial, typesInitial);
    
    for (String affiliation: mapOfAffiliationToFolderNameToAttributes.keySet()) {
      
      Map<String, GrouperDeprovisioningObjectAttributes> groupToDeprovisioningAttributes = mapOfAffiliationToFolderNameToAttributes.get(affiliation);
      
      if (groupToDeprovisioningAttributes == null) {
        groupToDeprovisioningAttributes = new HashMap<String, GrouperDeprovisioningObjectAttributes>();
        mapOfAffiliationToFolderNameToAttributes.put(affiliation, groupToDeprovisioningAttributes);
      }
      
      for (String[] queryResult : queryResults) {
        String stemId = queryResult[0];
        String stemName = queryResult[1];
        
        if (groupToDeprovisioningAttributes.get(stemName) == null) {
          groupToDeprovisioningAttributes.put(stemName, new GrouperDeprovisioningObjectAttributes(stemId, stemName, null));
          groupToDeprovisioningAttributes.get(stemName).setOwnedByGroup(true);
        }
      }
    }
      
  }
  
  public static void populateGroupChildrenOfAFolderWhichMayOrMayNotHaveAttributes(String parentStemId, Map<String, Map<String, GrouperDeprovisioningObjectAttributes>> mapOfAffliationToGroupNameToAttributes) {
    
    String sql = "SELECT " + 
        "    gg.id, " + 
        "    gg.name " +
        "FROM " + 
        "    grouper_stem_set gss, " +
        "    grouper_groups gg " + 
        "WHERE " + 
        "    gss.then_has_stem_id = ?" +
        "    AND gss.if_has_stem_id = gg.parent_stem ";
    
    List<Object> paramsInitial = new ArrayList<Object>();
    paramsInitial.add(parentStemId);

    List<Type> typesInitial = new ArrayList<Type>();
    typesInitial.add(StringType.INSTANCE);

    List<String[]> queryResults = HibernateSession.bySqlStatic().listSelect(String[].class, sql, paramsInitial, typesInitial);
    
    for (String affiliation: mapOfAffliationToGroupNameToAttributes.keySet()) {
      
      Map<String, GrouperDeprovisioningObjectAttributes> groupToDeprovisioningAttributes = mapOfAffliationToGroupNameToAttributes.get(affiliation);
      
      if (groupToDeprovisioningAttributes == null) {
        groupToDeprovisioningAttributes = new HashMap<String, GrouperDeprovisioningObjectAttributes>();
        mapOfAffliationToGroupNameToAttributes.put(affiliation, groupToDeprovisioningAttributes);
      }
      
      for (String[] queryResult : queryResults) {
        String groupId = queryResult[0];
        String groupName = queryResult[1];
        
        if (groupToDeprovisioningAttributes.get(groupName) == null) {
          groupToDeprovisioningAttributes.put(groupName, new GrouperDeprovisioningObjectAttributes(groupId, groupName, null));
          groupToDeprovisioningAttributes.get(groupName).setOwnedByGroup(true);
        }
      }
    }
      
  }
  
  
  /**
   * get deprovisioning attributes for groups under a folder
   * @param childStemId
   * @return the attributes
   */
  public static Map<String, Map<String, GrouperDeprovisioningObjectAttributes>> retrieveChildDeprovisioningGroupAttributesByFolder(String parentStemId) {

    Map<String, Map<String, GrouperDeprovisioningObjectAttributes>> results = new HashMap<String, Map<String, GrouperDeprovisioningObjectAttributes>>();

    {
      String sql = "SELECT " +
          "    gg.id, " + 
          "    gg.name, " + 
          "    gadn_config.name, " + 
          "    gaav_config.value_string, " + 
          "    gaav_affiliation.value_string as affiliation, " + 
          "    gaa_marker.id " + 
          "FROM " + 
          "    grouper_stems gs, " + 
          "    grouper_stem_set gss, " +
          "    grouper_groups gg, " + 
          "    grouper_attribute_assign gaa_marker, " + 
          "    grouper_attribute_assign gaa_affiliation, " + 
          "    grouper_attribute_assign gaa_config, " +
          "    grouper_attribute_assign_value gaav_affiliation, " + 
          "    grouper_attribute_assign_value gaav_config, " + 
          "    grouper_attribute_def_name gadn_marker, " +
          "    grouper_attribute_def_name gadn_affiliation, " +
          "    grouper_attribute_def_name gadn_config " + 
          "WHERE " +
          "    gs.id = ? " +
          "    AND gs.id = gss.then_has_stem_id " +
          "    AND gss.if_has_stem_id = gg.parent_stem " +
          "    AND gg.id = gaa_marker.owner_group_id " + 
          "    AND gaa_marker.attribute_def_name_id = gadn_marker.id " + 
          "    AND gadn_marker.name = ? " + 
          "    AND gaa_marker.id = gaa_affiliation.owner_attribute_assign_id " + 
          "    AND gaa_affiliation.attribute_def_name_id = gadn_affiliation.id " + 
          "    AND gadn_affiliation.name = ? " + 
          "    AND gaav_affiliation.attribute_assign_id = gaa_affiliation.id " + 
          "    AND gaa_marker.id = gaa_config.owner_attribute_assign_id " + 
          "    AND gaav_config.attribute_assign_id = gaa_config.id " + 
          "    AND gadn_config.id = gaa_config.attribute_def_name_id " + 
          "    AND gaa_marker.enabled = 'T' " + 
          "    AND gaa_affiliation.enabled = 'T' " + 
          "    AND gaa_config.enabled = 'T' ";
      
     
      List<Object> paramsInitial = new ArrayList<Object>();

      paramsInitial.add(parentStemId);
      paramsInitial.add(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameBase().getName());
      paramsInitial.add(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameAffiliation().getName());
      
      List<Type> typesInitial = new ArrayList<Type>();
      typesInitial.add(StringType.INSTANCE);
      typesInitial.add(StringType.INSTANCE);
      typesInitial.add(StringType.INSTANCE);
  
      List<String[]> queryResults = HibernateSession.bySqlStatic().listSelect(String[].class, sql, paramsInitial, typesInitial);
      for (String[] queryResult : queryResults) {
        String groupId = queryResult[0];
        String groupName = queryResult[1];
        String configName = queryResult[2];
        String configValue = queryResult[3];
        String affiliation = queryResult[4];
        String markerAttributeAssignId = queryResult[5];
        
        Map<String, GrouperDeprovisioningObjectAttributes> groupToDeprovisioningAttributes = results.get(affiliation);
        if (groupToDeprovisioningAttributes == null) {
          groupToDeprovisioningAttributes = new HashMap<String, GrouperDeprovisioningObjectAttributes>();
          results.put(affiliation, groupToDeprovisioningAttributes);
        }
        
        GrouperDeprovisioningObjectAttributes grouperDeprovisioningAttributes = groupToDeprovisioningAttributes.get(groupName);
        if (grouperDeprovisioningAttributes == null) {
          grouperDeprovisioningAttributes = new GrouperDeprovisioningObjectAttributes(groupId, groupName, markerAttributeAssignId);
          groupToDeprovisioningAttributes.put(groupName, grouperDeprovisioningAttributes);
          grouperDeprovisioningAttributes.setOwnedByGroup(true);
        }
        
        if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameAffiliation().getName())) {
          grouperDeprovisioningAttributes.setAffiliation(configValue);
        } else if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameAllowAddsWhileDeprovisioned().getName())) {
          grouperDeprovisioningAttributes.setAllowAddsWhileDeprovisioned(configValue);
        } else if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameAutoChangeLoader().getName())) {
          grouperDeprovisioningAttributes.setAutoChangeLoader(configValue);
        } else if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameAutoSelectForRemoval().getName())) {
          grouperDeprovisioningAttributes.setAutoSelectForRemoval(configValue);
        } else if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameDeprovision().getName())) {
          grouperDeprovisioningAttributes.setDeprovision(configValue);
        } else if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameDirectAssignment().getName())) {
          grouperDeprovisioningAttributes.setDirectAssign(configValue);
        } else if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameEmailAddresses().getName())) {
          grouperDeprovisioningAttributes.setEmailAddresses(configValue);
        } else if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameEmailBody().getName())) {
          grouperDeprovisioningAttributes.setEmailBody(configValue);
        } else if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameInheritedFromFolderId().getName())) {
          grouperDeprovisioningAttributes.setOwnerStemId(configValue);
        } else if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameMailToGroup().getName())) {
          grouperDeprovisioningAttributes.setMailToGroup(configValue);
        } else if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameSendEmail().getName())) {
          grouperDeprovisioningAttributes.setSendEmail(configValue);
        } else if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameShowForRemoval().getName())) {
          grouperDeprovisioningAttributes.setShowForRemoval(configValue);
        } else if (configName.equals(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameStemScope().getName())) {
          grouperDeprovisioningAttributes.setStemScope(configValue);
        }
        
      }
     
    }
    
    return results;
  }
  
  
  private void populateAttributesAssignedToGroupsIncremental() {
    
    for (String groupId: groupIdToNamesAddAndAttributeChange.keySet()) {
     
      Map<String, GrouperDeprovisioningObjectAttributes> groupDeprovisioningAttributes =  retrieveDeprovisioningAttributesByGroup(groupId);
      
      for (String affiliation: groupDeprovisioningAttributes.keySet()) {
        
        Map<String, GrouperDeprovisioningObjectAttributes> groupNameToAttributes = groupsWithAttributesToProcess.get(affiliation);
        
        if (groupNameToAttributes == null) {
          groupNameToAttributes = new HashMap<String, GrouperDeprovisioningObjectAttributes>();
          groupsWithAttributesToProcess.put(affiliation, groupNameToAttributes);
        }
        
        groupNameToAttributes.put(groupIdToNamesAddAndAttributeChange.get(groupId), groupDeprovisioningAttributes.get(affiliation));
        
      }
      
    }
    
  }
  
  private void populateAttributesAssignedToStemsIncremental() {
    
    for (String stemId: stemIdToNamesAddAndAttributeChange.keySet()) {
     
      Map<String, GrouperDeprovisioningObjectAttributes> stemDeprovisioningAttributes =  retrieveDeprovisioningAttributesByStem(stemId);
      
      for (String affiliation: stemDeprovisioningAttributes.keySet()) {
        
        Map<String, GrouperDeprovisioningObjectAttributes> stemNameToAttributes = foldersWithAttributesToProcess.get(affiliation);
        
        if (stemNameToAttributes == null) {
          stemNameToAttributes = new HashMap<String, GrouperDeprovisioningObjectAttributes>();
          foldersWithAttributesToProcess.put(affiliation, stemNameToAttributes);
        }
        
        stemNameToAttributes.put(stemIdToNamesAddAndAttributeChange.get(stemId), stemDeprovisioningAttributes.get(affiliation));
        
      }
      
    }
    
  }
  
  private void populateAncestorsIncremental() {
    
    Set<String> stemNamesToProcess = new HashSet<String>();
    
    for (String stemId: stemIdToNamesAddAndAttributeChange.keySet()) {
      
      String stemName = stemIdToNamesAddAndAttributeChange.get(stemId);
      
      String parentFolderName = GrouperUtil.parentStemNameFromName(stemName);
      
      if (StringUtils.isNotBlank(parentFolderName)) {
        stemNamesToProcess.add(parentFolderName);
      }
      
      
    }
    
    for (String groupId: groupIdToNamesAddAndAttributeChange.keySet()) {
      
      String groupName = groupIdToNamesAddAndAttributeChange.get(groupId);
      
      String parentFolderName = GrouperUtil.parentStemNameFromName(groupName);
      
      stemNamesToProcess.add(parentFolderName);
      
    }
    
    
    GrouperUtil.stemRemoveAncestorStemsOfChildStem(stemNamesToProcess);
    
    for (String stemName: stemNamesToProcess) {
      
      Map<String, Map<String, GrouperDeprovisioningObjectAttributes>> affiliationToFolderNameToDeprovisioningAttributes = retrieveFolderAndAncestorDeprovisioningAttributesByFolder(stemName);
      
      addFromOneMapOfMapsToAnother(affiliationToFolderNameToDeprovisioningAttributes, ancestorStemsDeprovisioningAttributes);
      
    }
    
  }
  
  private void populateChildrenWithAttributesIncremental() {
    
    Set<String> stemNamesToProcess = new HashSet<String>();
    
    for (String stemId: stemIdsToNamesAttributeChange.keySet()) {
      
      String stemName = stemIdsToNamesAttributeChange.get(stemId);
      
      stemNamesToProcess.add(stemName);
      
    }
    
    GrouperUtil.stemRemoveChildStemsOfTopStemName(stemNamesToProcess);
    
    for (String stemId: stemIdsToNamesAttributeChange.keySet()) {
      
      String stemName = stemIdsToNamesAttributeChange.get(stemId);
      if (stemNamesToProcess.contains(stemName)) {
        
        Map<String, Map<String, GrouperDeprovisioningObjectAttributes>> typeToFolderNameToDeprovisioningAttributes = retrieveChildDeprovisioningFolderAttributesByFolder(stemId);
            
        addFromOneMapOfMapsToAnother(typeToFolderNameToDeprovisioningAttributes, childrenStemsDeprovisioningAttributes);
        
        Map<String, Map<String, GrouperDeprovisioningObjectAttributes>> typeToGroupNameToDeprovisioningAttributes = retrieveChildDeprovisioningGroupAttributesByFolder(stemId);
        
        addFromOneMapOfMapsToAnother(typeToGroupNameToDeprovisioningAttributes, childrenGroupsDeprovisioningAttributes);
        
      }
      
    }
   
    
  }
  
  private void populateChildrenWhichMayOrMayNotHaveAttributes() {
    
    Set<String> stemNamesToProcess = new HashSet<String>();
    
    for (String stemId: stemIdsToNamesAttributeChange.keySet()) {
      
      String stemName = stemIdsToNamesAttributeChange.get(stemId);
      
      stemNamesToProcess.add(stemName);
      
    }
    
    GrouperUtil.stemRemoveChildStemsOfTopStemName(stemNamesToProcess);
    
    for (String stemId: stemIdsToNamesAttributeChange.keySet()) {
      
      String stemName = stemIdsToNamesAttributeChange.get(stemId);
      if (stemNamesToProcess.contains(stemName)) {
        
        populateGroupChildrenOfAFolderWhichMayOrMayNotHaveAttributes(stemId, this.groupsWithOrWithoutAttributesToProcess);
        populateFolderChildrenOfAFolderWhichMayOrMayNotHaveAttributes(stemId, this.foldersWithOrWithoutAttributesToProcess);
        
      }
      
    }
  }
  
  private void populateEventObjectsWhichDoNotHaveAttributes() {
    
    for (String affiliation: groupsWithOrWithoutAttributesToProcess.keySet()) {
      
      Map<String, GrouperDeprovisioningObjectAttributes> groupToDeprovisioningAttributes = groupsWithOrWithoutAttributesToProcess.get(affiliation);
      
      for (String groupId: groupIdToNamesAddAndAttributeChange.keySet()) {
       
        String groupName = groupIdToNamesAddAndAttributeChange.get(groupId);
        
        if (groupToDeprovisioningAttributes.get(groupName) == null) {
          groupToDeprovisioningAttributes.put(groupName, new GrouperDeprovisioningObjectAttributes(groupId, groupName, null));
          groupToDeprovisioningAttributes.get(groupName).setOwnedByGroup(true);
        }
      }
    }
    
    for (String affiliation: foldersWithOrWithoutAttributesToProcess.keySet()) {
      
      Map<String, GrouperDeprovisioningObjectAttributes> stemToDeprovisioningAttributes = foldersWithOrWithoutAttributesToProcess.get(affiliation);
      
      for (String stemId: stemIdToNamesAddAndAttributeChange.keySet()) {
       
        String stemName = stemIdToNamesAddAndAttributeChange.get(stemId);
        
        if (stemToDeprovisioningAttributes.get(stemName) == null) {
          stemToDeprovisioningAttributes.put(stemName, new GrouperDeprovisioningObjectAttributes(stemId, stemName, null));
          stemToDeprovisioningAttributes.get(stemName).setOwnedByStem(true);
        }
      }
    }
    
  }
  
  private void populateIdsAndNamesToWorkOn() {
    
    Set<String> queriedPITAttributeAssignIds = new HashSet<String>();
    Set<String> queriedStemIds = new HashSet<String>();
    Set<String> queriedGroupIds = new HashSet<String>();
    
    for (EsbEventContainer esbEventContainer : eventsToProcess) {
      
      EsbEvent esbEvent = esbEventContainer.getEsbEvent();
      EsbEventType esbEventType = esbEventContainer.getEsbEventType();
      
      if (esbEventType == EsbEventType.GROUP_ADD) {
        
       groupIdsToNamesAdd.put(esbEvent.getGroupId(), esbEvent.getGroupName());
        
      } else if (esbEventType == EsbEventType.STEM_ADD) {
        
        stemIdsToNamesAdd.put(esbEvent.getId(), esbEvent.getName());
        
      } else if ((esbEventType == EsbEventType.ATTRIBUTE_ASSIGN_VALUE_ADD || esbEventType == EsbEventType.ATTRIBUTE_ASSIGN_VALUE_DELETE )
          // || esbEventType == EsbEventType.ATTRIBUTE_ASSIGN_DELETE || esbEventType == EsbEventType.ATTRIBUTE_ASSIGN_ADD) 
          &&
          esbEvent.getAttributeDefNameName().startsWith(GrouperDeprovisioningSettings.deprovisioningStemName())) {
        
        String attributeAssignId = esbEvent.getAttributeAssignId();
//        if (esbEventType == EsbEventType.ATTRIBUTE_ASSIGN_DELETE || esbEventType == EsbEventType.ATTRIBUTE_ASSIGN_ADD) {
//          attributeAssignId = esbEvent.getId();
//        }
        
        PITAttributeAssign pitAttributeAssign = GrouperDAOFactory.getFactory().getPITAttributeAssign().findBySourceIdMostRecent(attributeAssignId, false);

        if (pitAttributeAssign != null) {
          // query pit to see if this is for a folder and for this object type and is direct
          if (!queriedPITAttributeAssignIds.contains(pitAttributeAssign.getOwnerAttributeAssignId())) {
            queriedPITAttributeAssignIds.add(pitAttributeAssign.getOwnerAttributeAssignId());
            
            String stemId = retrieveStemIdIfDirectStemAssignmentByPITMarkerAttributeAssignId(pitAttributeAssign.getOwnerAttributeAssignId());
  
            if (stemId != null) {
              
              if (queriedStemIds.contains(stemId)) {
                continue;
              }
              
              queriedStemIds.add(stemId);
              Stem stem = GrouperDAOFactory.getFactory().getStem().findByUuid(stemId, false);
              
              if (stem == null) {
                continue;
              }
              
              stemIdsToNamesAttributeChange.put(stemId, stem.getName());
              
            } else {
              String groupId = retrieveGroupIdIfDirectGroupAssignmentByPITMarkerAttributeAssignId(pitAttributeAssign.getOwnerAttributeAssignId());
              
              if (groupId != null && queriedGroupIds.contains(groupId)) {
                continue;
              }
              queriedGroupIds.add(groupId);
              
              Group group = groupId != null ? GrouperDAOFactory.getFactory().getGroup().findByUuid(groupId, false) : null;

              if (group != null) {
                groupIdsToNamesAttributeChange.put(groupId, group.getName());
              }
              
              
            }
          }
        }
        
      }
      
    }
    
  }

  
  /**
   * all stems that have events and their attributes
   */
  private Map<String, Map<String, GrouperDeprovisioningObjectAttributes>> foldersWithAttributesToProcess = new HashMap<String, Map<String, GrouperDeprovisioningObjectAttributes>>();
  
  /**
   * all stems that have type attributes or have an ancestor that have type attributes
   */
  private Map<String, Map<String, GrouperDeprovisioningObjectAttributes>> foldersWithOrWithoutAttributesToProcess = new HashMap<String, Map<String, GrouperDeprovisioningObjectAttributes>>();
  
  /**
   * all ancestor stems of stems and groups that have type attributes
   */
  private Map<String, Map<String, GrouperDeprovisioningObjectAttributes>> ancestorStemsDeprovisioningAttributes = new HashMap<String, Map<String, GrouperDeprovisioningObjectAttributes>>();
  
  /**
   * all children stems of stems that have attribute change
   */
  private Map<String, Map<String, GrouperDeprovisioningObjectAttributes>> childrenStemsDeprovisioningAttributes = new HashMap<String, Map<String, GrouperDeprovisioningObjectAttributes>>();
  
  /**
   * all children groups of stems that have attribute change
   */
  private Map<String, Map<String, GrouperDeprovisioningObjectAttributes>> childrenGroupsDeprovisioningAttributes = new HashMap<String, Map<String, GrouperDeprovisioningObjectAttributes>>();

  /**
   * all groups that have events and their attributes
   */
  private Map<String, Map<String, GrouperDeprovisioningObjectAttributes>> groupsWithAttributesToProcess = new HashMap<String, Map<String, GrouperDeprovisioningObjectAttributes>>();
  
  /**
   * all groups that have type attributes or have an ancestor that have type attributes
   */
  private Map<String, Map<String, GrouperDeprovisioningObjectAttributes>> groupsWithOrWithoutAttributesToProcess = new HashMap<String, Map<String, GrouperDeprovisioningObjectAttributes>>();
  
  /**
   * events to process
   */
  private List<EsbEventContainer> eventsToProcess;
  
  
  /**
   * map of group id to name where group was just added
   */
  private Map<String, String> groupIdsToNamesAdd = new HashMap<String, String>();

  /**
   * map of stem id to name where stem was just added
   */
  private Map<String, String> stemIdsToNamesAdd = new HashMap<String, String>();

  /**
   * map of group id to name where an object type attribute changed
   */
  private Map<String, String> groupIdsToNamesAttributeChange = new HashMap<String, String>();
  
  /**
   * map of stem id to name where an object type attribute changed
   */
  private Map<String, String> stemIdsToNamesAttributeChange = new HashMap<String, String>();
  
  /**
   * all the group id to name of adds and attribute changes
   */
  private Map<String, String> groupIdToNamesAddAndAttributeChange = new HashMap<String, String>();

  /**
   * all the stem id to name of adds and attribute changes
   */
  private Map<String, String> stemIdToNamesAddAndAttributeChange = new HashMap<String, String>();
  
  
  
  public void incrementalLogic(List<EsbEventContainer> esbEventContainers) {
    
    eventsToProcess = esbEventContainers;
    
    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("deprovisioning");
    
    gcGrouperSync.setSyncEngine(GcGrouperSync.DEPROVISIONING);
    gcGrouperSync.getGcGrouperSyncDao().store();
    GcGrouperSyncJob gcGrouperSyncJob = gcGrouperSync.getGcGrouperSyncJobDao().jobRetrieveOrCreateBySyncType("incremental");
    gcGrouperSyncJob.waitForRelatedJobsToFinishThenRun(false);
    
    GcGrouperSyncHeartbeat gcGrouperSyncHeartbeat = new GcGrouperSyncHeartbeat();
    gcGrouperSyncHeartbeat.setGcGrouperSyncJob(gcGrouperSyncJob);
    gcGrouperSyncHeartbeat.setFullSync(false);
    gcGrouperSyncHeartbeat.addHeartbeatLogic(new Runnable() {
      @Override
      public void run() {
        
      }
    });
    if (!gcGrouperSyncHeartbeat.isStarted()) {
      gcGrouperSyncHeartbeat.runHeartbeatThread();
    }
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    
    RuntimeException runtimeException = null;
    
    try {
     
      populateIdsAndNamesToWorkOn();
      
      groupIdToNamesAddAndAttributeChange.putAll(groupIdsToNamesAdd);
      groupIdToNamesAddAndAttributeChange.putAll(groupIdsToNamesAttributeChange);
      
      populateAttributesAssignedToGroupsIncremental();
      
      stemIdToNamesAddAndAttributeChange.putAll(stemIdsToNamesAdd);
      stemIdToNamesAddAndAttributeChange.putAll(stemIdsToNamesAttributeChange);
      
      populateAttributesAssignedToStemsIncremental();
      
      populateAncestorsIncremental();
      
      populateChildrenWithAttributesIncremental();
      
      addFromOneMapOfMapsToAnother(groupsWithAttributesToProcess, groupsWithOrWithoutAttributesToProcess);
      addFromOneMapOfMapsToAnother(childrenGroupsDeprovisioningAttributes, groupsWithOrWithoutAttributesToProcess);
      
      addFromOneMapOfMapsToAnother(foldersWithAttributesToProcess, foldersWithOrWithoutAttributesToProcess);
      addFromOneMapOfMapsToAnother(childrenStemsDeprovisioningAttributes, foldersWithOrWithoutAttributesToProcess);
      
      
      Set<String> affiliationsToProcess = new HashSet<String>();
      
      affiliationsToProcess.addAll(groupsWithOrWithoutAttributesToProcess.keySet());
      affiliationsToProcess.addAll(foldersWithOrWithoutAttributesToProcess.keySet());
      affiliationsToProcess.addAll(ancestorStemsDeprovisioningAttributes.keySet());
      
      
      if (affiliationsToProcess.size() == 0) {
        return;
      }
      
      for(String ancestorType: affiliationsToProcess) {
            
        if (!groupsWithOrWithoutAttributesToProcess.containsKey(ancestorType)) {
          groupsWithOrWithoutAttributesToProcess.put(ancestorType, new HashMap<String, GrouperDeprovisioningObjectAttributes>()); 
        }
        
        if (!foldersWithOrWithoutAttributesToProcess.containsKey(ancestorType)) {
          foldersWithOrWithoutAttributesToProcess.put(ancestorType, new HashMap<String, GrouperDeprovisioningObjectAttributes>()); 
        }
        
      }
      
      populateEventObjectsWhichDoNotHaveAttributes();
      
      populateChildrenWhichMayOrMayNotHaveAttributes();

      for (String affiliation: affiliationsToProcess) {
        
        Map<String, GrouperDeprovisioningObjectAttributes> grouperDeprovisioningFolderAttributes = foldersWithOrWithoutAttributesToProcess.get(affiliation);
        Map<String, GrouperDeprovisioningObjectAttributes> grouperDeprovisioningGroupAttributes = groupsWithOrWithoutAttributesToProcess.get(affiliation);
        
        Set<GrouperDeprovisioningObjectAttributes> grouperDeprovisioningAttributesToProcess = new HashSet<GrouperDeprovisioningObjectAttributes>();
        if (grouperDeprovisioningFolderAttributes != null) {        
          grouperDeprovisioningAttributesToProcess.addAll(grouperDeprovisioningFolderAttributes.values());
        }
        if (grouperDeprovisioningGroupAttributes != null) {
          grouperDeprovisioningAttributesToProcess.addAll(grouperDeprovisioningGroupAttributes.values());
        }
        
        propagateAttributes(affiliation, grouperDeprovisioningAttributesToProcess, ancestorStemsDeprovisioningAttributes.get(affiliation), debugMap);
        
      }
    } catch (RuntimeException re) {
      runtimeException = re;
    } finally {
      GcGrouperSyncHeartbeat.endAndWaitForThread(gcGrouperSyncHeartbeat);
      debugMap.put("finalLog", true);
      synchronized (GrouperDeprovisioningDaemonLogic.class) {
        try {
          if (gcGrouperSyncJob != null) {
            gcGrouperSyncJob.assignHeartbeatAndEndJob();
          }
        } catch (RuntimeException re2) {
          debugMap.put("exception2", GrouperClientUtils.getFullStackTrace(re2));
          if (runtimeException == null) {
            throw re2;
          }
          
        }
      }
      
      if (LOG.isDebugEnabled()) {
        LOG.debug(GrouperUtil.mapToString(debugMap));
      }
      
    }
    
  }
  
  private static void addFromOneMapOfMapsToAnother(Map<String, Map<String, GrouperDeprovisioningObjectAttributes>> mapToAddFrom, Map<String, Map<String, GrouperDeprovisioningObjectAttributes>> mapToAddTo) {
    
    for (String affiliation: mapToAddFrom.keySet()) {
      Map<String, GrouperDeprovisioningObjectAttributes> existingFolderNamesToAttributes = mapToAddTo.get(affiliation);
      if (existingFolderNamesToAttributes == null) {
        existingFolderNamesToAttributes = new HashMap<String, GrouperDeprovisioningObjectAttributes>();
        mapToAddTo.put(affiliation, existingFolderNamesToAttributes);
      }
      
      existingFolderNamesToAttributes.putAll(mapToAddFrom.get(affiliation));
      
    }
    
  }

  @Override
  public OtherJobOutput run(OtherJobInput otherJobInput) {
    try {
      Map<String, Object> debugMap = fullSyncLogic();
      otherJobInput.getHib3GrouperLoaderLog().setJobMessage("Finished successfully running deprovisioning full sync logic daemon. \n "+GrouperUtil.mapToString(debugMap));
    } catch (Exception e) {
      LOG.warn("Error while running deprovisioning full sync daemon", e);
      otherJobInput.getHib3GrouperLoaderLog().setJobMessage("Finished running deprovisioning full sync logic daemon with an error: " + ExceptionUtils.getFullStackTrace(e));
    } finally {
      otherJobInput.getHib3GrouperLoaderLog().store();
    }
    return null;
  }

}
