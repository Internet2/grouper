package edu.internet2.middleware.grouper.app.attestation;

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

import edu.emory.mathcs.backport.java.util.Collections;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesDaemonLogic;
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
import edu.internet2.middleware.grouper.misc.GrouperObject;
import edu.internet2.middleware.grouper.pit.PITAttributeAssign;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncDao;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncHeartbeat;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncJob;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.StringUtils;

public class GrouperAttestationDaemonLogic extends OtherJobBase {
  

private static final Log LOG = GrouperUtil.getLog(GrouperAttestationDaemonLogic.class);
  
  /**
   * this method retrieves all attestation attributes assignments and values for all folders. 
   * Also it returns all folders underneath folders with attestation attributes assigned.
   * @return
   */
  public static Map<String, GrouperAttestationObjectAttributes> retrieveAllFoldersOfInterestForAttestation() {
      
    Map<String, GrouperAttestationObjectAttributes> results = new HashMap<String, GrouperAttestationObjectAttributes>();

    {
      String sql = "SELECT " +
          "    gs.id, " +
          "    gs.name, " + 
          "    gadn_config.name, " + 
          "    gaav_config.value_string, " +
          "    gaa_marker.id " + 
          "FROM " + 
          "    grouper_stems gs, " + 
          "    grouper_attribute_assign gaa_marker, " + 
          "    grouper_attribute_assign gaa_attestation, " + 
          "    grouper_attribute_assign gaa_config, " + 
          "    grouper_attribute_assign_value gaav_attestation, " + 
          "    grouper_attribute_assign_value gaav_config, " + 
          "    grouper_attribute_def_name gadn_marker, " + 
          "    grouper_attribute_def_name gadn_attestation, " + 
          "    grouper_attribute_def_name gadn_config " + 
          "WHERE " + 
          "    gs.id = gaa_marker.owner_stem_id " + 
          "    AND gaa_marker.attribute_def_name_id = gadn_marker.id " + 
          "    AND gadn_marker.name = ? " + 
          "    AND gaa_marker.id = gaa_attestation.owner_attribute_assign_id " + 
          "    AND gaa_attestation.attribute_def_name_id = gadn_attestation.id " + 
          "    AND gadn_attestation.name = ? " + 
          "    AND gaav_attestation.attribute_assign_id = gaa_attestation.id " + 
          "    AND gaav_attestation.value_string = 'group' " + 
          "    AND gaa_marker.id = gaa_config.owner_attribute_assign_id " + 
          "    AND gaav_config.attribute_assign_id = gaa_config.id " + 
          "    AND gadn_config.id = gaa_config.attribute_def_name_id " + 
          "    AND gaa_marker.enabled = 'T' " + 
          "    AND gaa_attestation.enabled = 'T' " + 
          "    AND gaa_config.enabled = 'T' ";
      
      List<Object> paramsInitial = new ArrayList<Object>();

      paramsInitial.add(GrouperAttestationJob.retrieveAttributeDefNameValueDef().getName());
      paramsInitial.add(GrouperAttestationJob.retrieveAttributeDefNameType().getName());
      
      List<Type> typesInitial = new ArrayList<Type>();
      typesInitial.add(StringType.INSTANCE);
      typesInitial.add(StringType.INSTANCE);
  
      List<String[]> queryResults = HibernateSession.bySqlStatic().listSelect(String[].class, sql, paramsInitial, typesInitial);
      for (String[] queryResult : queryResults) {
        String stemId = queryResult[0];
        String stemName = queryResult[1];
        String configName = queryResult[2];
        String configValue = queryResult[3];
        String markerAttributeAssignId = queryResult[4];
        
        GrouperAttestationObjectAttributes attestationAttributes = results.get(stemName);
        if (attestationAttributes == null) {
          attestationAttributes = new GrouperAttestationObjectAttributes(stemId, stemName, markerAttributeAssignId);
          attestationAttributes.setOwnedByStem(true);
          attestationAttributes.setAttestationDirectAssign("true");
          results.put(stemName, attestationAttributes);
        }
        
        if (configName.equals(GrouperAttestationJob.retrieveAttributeDefNameDateCertified().getName())) {
          attestationAttributes.setDateCertified(configValue);
        } else if (configName.equals(GrouperAttestationJob.retrieveAttributeDefNameMinCertifiedDate().getName())) {
          attestationAttributes.setMinDateCertified(configValue);
        } else if (configName.equals(GrouperAttestationJob.retrieveAttributeDefNameHasAttestation().getName())) {
          attestationAttributes.setHasAttestation(configValue);
        } else if (configName.equals(GrouperAttestationJob.retrieveAttributeDefNameStemScope().getName())) {
          attestationAttributes.setStemScope(configValue);
        } else if (configName.equals(GrouperAttestationJob.retrieveAttributeDefNameDaysUntilRecertify().getName())) {
          attestationAttributes.setDaysUntilRecertify(configValue);
        }
      }
    }
    
    return results;
    
  }
  
  /**
   * this method retrieves all attestation attributes assignments and values for all groups. 
   * Also it returns all groups underneath folders with attestation assigned.
   * @return
   */
  public static Map<String, GrouperAttestationObjectAttributes> retrieveAllGroupsOfInterestForAttestation(Map<String, GrouperAttestationObjectAttributes> allFoldersOfInterestForAttestation) {
    
    Map<String, GrouperAttestationObjectAttributes> results = new HashMap<String, GrouperAttestationObjectAttributes>();

    {
      String sql = "SELECT " +
          "    gg.id, " + 
          "    gg.name, " + 
          "    gadn_config.name, " + 
          "    gaav_config.value_string, " + 
          "    gaa_marker.id " + 
          "FROM " + 
          "    grouper_groups gg, " + 
          "    grouper_attribute_assign gaa_marker, " + 
          "    grouper_attribute_assign gaa_config, " + 
          "    grouper_attribute_assign_value gaav_config, " + 
          "    grouper_attribute_def_name gadn_marker, " + 
          "    grouper_attribute_def_name gadn_config " + 
          "WHERE " + 
          "    gg.id = gaa_marker.owner_group_id " + 
          "    AND gaa_marker.attribute_def_name_id = gadn_marker.id " + 
          "    AND gadn_marker.name = ? " + 
          "    AND gaa_marker.id = gaa_config.owner_attribute_assign_id " + 
          "    AND gaav_config.attribute_assign_id = gaa_config.id " + 
          "    AND gadn_config.id = gaa_config.attribute_def_name_id " + 
          "    AND gaa_marker.enabled = 'T' " + 
          "    AND gaa_config.enabled = 'T' ";
      
      List<Object> paramsInitial = new ArrayList<Object>();

      paramsInitial.add(GrouperAttestationJob.retrieveAttributeDefNameValueDef().getName());
      
      List<Type> typesInitial = new ArrayList<Type>();
      typesInitial.add(StringType.INSTANCE);
  
      List<String[]> queryResults = HibernateSession.bySqlStatic().listSelect(String[].class, sql, paramsInitial, typesInitial);
      for (String[] queryResult : queryResults) {
        String groupId = queryResult[0];
        String groupName = queryResult[1];
        String configName = queryResult[2];
        String configValue = queryResult[3];
        String markerAttributeAssignId = queryResult[4];
        
        GrouperAttestationObjectAttributes attestationAttributes = results.get(groupName);
        if (attestationAttributes == null) {
          attestationAttributes = new GrouperAttestationObjectAttributes(groupId, groupName, markerAttributeAssignId);
          attestationAttributes.setOwnedByGroup(true);
          results.put(groupName, attestationAttributes);
        }
        
        if (configName.equals(GrouperAttestationJob.retrieveAttributeDefNameDateCertified().getName())) {
          attestationAttributes.setDateCertified(configValue);
        } else if (configName.equals(GrouperAttestationJob.retrieveAttributeDefNameDirectAssignment().getName())) {
          attestationAttributes.setAttestationDirectAssign(configValue);
        } else if (configName.equals(GrouperAttestationJob.retrieveAttributeDefNameCalculatedDaysLeft().getName())) {
          attestationAttributes.setCalculatedDaysLeft(configValue);
        } else if (configName.equals(GrouperAttestationJob.retrieveAttributeDefNameHasAttestation().getName())) {
          attestationAttributes.setHasAttestation(configValue);
        } else if (configName.equals(GrouperAttestationJob.retrieveAttributeDefNameStemScope().getName())) {
          attestationAttributes.setStemScope(configValue);
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
          "      grouper_attribute_assign gaa_attestation,   " + 
          "      grouper_attribute_assign_value gaav_direct,   " + 
          "      grouper_attribute_def_name gadn_marker," + 
          "      grouper_attribute_def_name gadn_direct,   " + 
          "      grouper_stem_set gss,   " + 
          "      grouper_groups gg,   " + 
          "      grouper_attribute_assign_value gaav_attestation, " + 
          "      grouper_attribute_def_name gadn_attestation " + 
          "  WHERE   " + 
          "      gaa_marker.id = gaa_attestation.owner_attribute_assign_id " + 
          "      AND gaa_attestation.attribute_def_name_id = gadn_attestation.id " + 
          "      AND gadn_attestation.name = ? " + 
          "      AND gaav_attestation.attribute_assign_id = gaa_attestation.id " + 
          "      AND gaav_attestation.value_string = 'group' " + 
          "      AND gaa_attestation.enabled = 'T' " + 
          "      AND gs.id = gaa_marker.owner_stem_id   " + 
          "      AND gaa_marker.attribute_def_name_id = gadn_marker.id   " + 
          "      AND gadn_marker.name = ?  " + 
          "      AND gaa_marker.id = gaa_direct.owner_attribute_assign_id   " + 
          "      AND gaa_direct.attribute_def_name_id = gadn_direct.id   " + 
          "      AND gaav_direct.attribute_assign_id = gaa_direct.id   " +
          "      AND gaav_direct.value_string = 'true'   " + 
          "      AND gs.id = gss.then_has_stem_id   " + 
          "      AND gss.if_has_stem_id = gg.parent_stem   " +
          "      AND gaa_marker.enabled = 'T'" + 
          "      AND gaa_direct.enabled = 'T' ";
      
      List<Object> paramsInitial = new ArrayList<Object>();
      
      paramsInitial.add(GrouperAttestationJob.retrieveAttributeDefNameType().getName());
      paramsInitial.add(GrouperAttestationJob.retrieveAttributeDefNameValueDef().getName());
      
      List<Type> typesInitial = new ArrayList<Type>();
      typesInitial.add(StringType.INSTANCE);
      typesInitial.add(StringType.INSTANCE);
  
      List<String[]> queryResults = HibernateSession.bySqlStatic().listSelect(String[].class, sql, paramsInitial, typesInitial);
      
      for (String[] queryResult : queryResults) {
        String groupId = queryResult[0];
        String groupName = queryResult[1];
        
        if (!groupHasAnAncestorWithAttestationAttributes(groupName, allFoldersOfInterestForAttestation)) {
          continue;
        }
        
        if (results.get(groupName) == null) {
          results.put(groupName, new GrouperAttestationObjectAttributes(groupId, groupName, null));
          results.get(groupName).setOwnedByGroup(true);
        }
      }
   
    }
    
    return results;
    
  }
  
  private static boolean groupHasAnAncestorWithAttestationAttributes(String groupName, 
      Map<String, GrouperAttestationObjectAttributes> allFoldersOfInterestForAttestation) {
   
    Set<String> parentStemNames = GrouperUtil.findParentStemNames(groupName);
    ArrayList<String> orderedParentNames = new ArrayList<String>(parentStemNames);
    Collections.reverse(orderedParentNames);
    
    String groupDirectParentStemName = GrouperUtil.parentStemNameFromName(groupName);
    
    boolean foundAncestor = false;
    for (String parentName: orderedParentNames) {
      
      GrouperAttestationObjectAttributes parentAttestationObjectAttributes = allFoldersOfInterestForAttestation.get(parentName);
      
      if (parentAttestationObjectAttributes == null) {
        continue;
      }

      if (GrouperUtil.equals(groupDirectParentStemName, parentName)) {
        foundAncestor = true;
        break;
      } else {
        if (GrouperUtil.equalsIgnoreCase(parentAttestationObjectAttributes.getStemScope(), "sub")) {
          foundAncestor = true;
          break;
        } else {
          break;
        }
      }
    }
    
    return foundAncestor;
  }
  
  
  /**
   * 
   */
  public static Map<String, Object> fullSyncLogic() {
    
    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("attestation");
    
    gcGrouperSync.setSyncEngine(GcGrouperSync.ATTESTATION_PROPAGATION);
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
      
    
      Map<String, GrouperAttestationObjectAttributes> allFoldersOfInterestForAttestation = retrieveAllFoldersOfInterestForAttestation();
      Map<String, GrouperAttestationObjectAttributes> allGroupsOfInterestForAttestation = retrieveAllGroupsOfInterestForAttestation(allFoldersOfInterestForAttestation);
      
      Set<GrouperAttestationObjectAttributes> grouperAttestationObjectAttributesToProcess = new HashSet<GrouperAttestationObjectAttributes>();
      grouperAttestationObjectAttributesToProcess.addAll(allGroupsOfInterestForAttestation.values());
      
      propagateAttestationAttributes(grouperAttestationObjectAttributesToProcess, allFoldersOfInterestForAttestation, debugMap);
    
    } catch (RuntimeException re) {
      runtimeException = re;
    } finally {
      GcGrouperSyncHeartbeat.endAndWaitForThread(gcGrouperSyncHeartbeat);
      debugMap.put("finalLog", true);
      synchronized (GrouperAttestationDaemonLogic.class) {
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
   * @param grouperAttestationAttributesToProcess
   * @param grouperAttestationFolderAttributes
   * @param debugMap
   */
  public static void propagateAttestationAttributes(Set<GrouperAttestationObjectAttributes> grouperAttestationAttributesToProcess,
      Map<String, GrouperAttestationObjectAttributes> grouperAttestationFolderAttributes, Map<String, Object> debugMap) {
    
    
    AttributeDefName attributeDefNameBase = GrouperAttestationJob.retrieveAttributeDefNameValueDef(); 
    AttributeDefName attributeDefNameDirectAssign = GrouperAttestationJob.retrieveAttributeDefNameDirectAssignment();
    
        
    AttributeDefName attributeDefNameDateCertified = GrouperAttestationJob.retrieveAttributeDefNameDateCertified();
    AttributeDefName attributeDefNameCalculatedDaysLeft = GrouperAttestationJob.retrieveAttributeDefNameCalculatedDaysLeft();
    AttributeDefName attributeDefNameHasAttestation = GrouperAttestationJob.retrieveAttributeDefNameHasAttestation();
    
    int objectTypesAttributesFoldersDeleted = 0;
    int objectTypesAttributesFoldersAddedOrUpdated = 0;
    int objectTypesAttributesGroupsDeleted = 0;
    int objectTypesAttributesGroupsAddedOrUpdated = 0;
    
    // get a map of all child -> parent, maybe this is cheaper than having to recalculate it multiple times per object
    Map<String, String> childToParent = new HashMap<String, String>();
    
    for (GrouperAttestationObjectAttributes grouperAttestationObjectAttribute : grouperAttestationAttributesToProcess) {
      
      String objectName = grouperAttestationObjectAttribute.getName();
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
    
    
    for (GrouperAttestationObjectAttributes grouperAttestationObjectAttribute : GrouperUtil.nonNull(grouperAttestationFolderAttributes).values()) {
      
      String objectName = grouperAttestationObjectAttribute.getName();
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
    for (GrouperAttestationObjectAttributes grouperAttestationObjectAttribute : grouperAttestationAttributesToProcess) {

      if ("true".equalsIgnoreCase(grouperAttestationObjectAttribute.getAttestationDirectAssign())) {
        continue;
      }
      
      GrouperAttestationObjectAttributes ancestorGrouperAttestationObjectAttribute = null;
      
      int depth = 0;
      String currObjectName = grouperAttestationObjectAttribute.getName();
      while (true) {
        depth++;
        GrouperUtil.assertion(depth < 1000, "Endless loop.");
        currObjectName = childToParent.get(currObjectName);
        if (currObjectName == null) {
          break;
        }
        
        GrouperAttestationObjectAttributes currGrouperAttestationObjectAttribute = GrouperUtil.nonNull(grouperAttestationFolderAttributes).get(currObjectName);
        if (currGrouperAttestationObjectAttribute != null && "true".equalsIgnoreCase(currGrouperAttestationObjectAttribute.getAttestationDirectAssign())) {
          
          ancestorGrouperAttestationObjectAttribute = currGrouperAttestationObjectAttribute;
          break;
        }
      }
      
      if (ancestorGrouperAttestationObjectAttribute == null) {
        // delete the marker
        
        AttributeAssignable object;
        
        if (grouperAttestationObjectAttribute.isOwnedByGroup()) {
          object = GroupFinder.findByName(GrouperSession.staticGrouperSession(), grouperAttestationObjectAttribute.getName(), false);
        } else {
          object = StemFinder.findByName(GrouperSession.staticGrouperSession(), grouperAttestationObjectAttribute.getName(), false);
        }
        
        if (object == null) {
          // guess it was deleted?
          continue;
        }
        
        LOG.info("For group/stem= " + grouperAttestationObjectAttribute.getName() + " deleting attestation marker attribute");
        
        if (StringUtils.isNotBlank(grouperAttestationObjectAttribute.getMarkerAttributeAssignId())) {
          
          AttributeAssign markerAttributeAssign = GrouperDAOFactory.getFactory().getAttributeAssign().findById(grouperAttestationObjectAttribute.getMarkerAttributeAssignId(), false);
          if (markerAttributeAssign != null) {
            markerAttributeAssign.delete();
          }
        }
        
        if (grouperAttestationObjectAttribute.isOwnedByGroup()) {
          objectTypesAttributesGroupsDeleted++;
          
        } else {
          objectTypesAttributesFoldersDeleted++;
        }
      } else {

        String existingAttestationDirectAssign = grouperAttestationObjectAttribute.getAttestationDirectAssign();
        String existingHasAttesttion = grouperAttestationObjectAttribute.getHasAttestation();
        
        String existingDateCertified = grouperAttestationObjectAttribute.getDateCertified();
        
        String actualAttestationDirectAssign = "false";
        String actualHasAttestation = ancestorGrouperAttestationObjectAttribute.getHasAttestation();
        String actualMinDateCertified = ancestorGrouperAttestationObjectAttribute.getMinDateCertified();
        String actualDaysUntilRecertify = ancestorGrouperAttestationObjectAttribute.getDaysUntilRecertify();
        
        boolean resetExistingDateCertified = false;
        
        if (StringUtils.isNotBlank(actualMinDateCertified)) {
          
          long actualMinDateCertifiedTime = GrouperUtil.stringToTimestamp(actualMinDateCertified).getTime();
          
          if (System.currentTimeMillis() - actualMinDateCertifiedTime < 24 * 60 * 60 * 1000L) {
            
            if (StringUtils.isBlank(existingDateCertified)) {
              resetExistingDateCertified = true;
            } else {
              
              long existingDateCertifiedTime = GrouperUtil.stringToTimestamp(existingDateCertified).getTime();
              
              if (existingDateCertifiedTime < actualMinDateCertifiedTime &&
                  !StringUtils.equals(actualMinDateCertified, existingDateCertified)) {
                resetExistingDateCertified = true;
              }
            }
          }
              
        }
        
        if (!GrouperUtil.equals(existingAttestationDirectAssign, actualAttestationDirectAssign) ||
            !GrouperUtil.equals(existingHasAttesttion, actualHasAttestation)  ||
            resetExistingDateCertified) {

          AttributeAssignable object;
          
          if (grouperAttestationObjectAttribute.isOwnedByGroup()) {
            object = GroupFinder.findByName(GrouperSession.staticGrouperSession(), grouperAttestationObjectAttribute.getName(), false);
          } else {
            object = StemFinder.findByName(GrouperSession.staticGrouperSession(), grouperAttestationObjectAttribute.getName(), false);
          }
          
          if (object == null) {
            // guess it was deleted?
            continue;
          }
          
          if (!groupHasAnAncestorWithAttestationAttributes( ((GrouperObject)object).getName(), grouperAttestationFolderAttributes)) {
            continue;
          }

          final boolean RESET_EXISTING_CERTIFIED_DATE = resetExistingDateCertified;
          HibernateSession.callbackHibernateSession(
              GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT,
              new HibernateHandler() {
                
                public Object callback(HibernateHandlerBean hibernateHandlerBean)
                    throws GrouperDAOException {
                  
                  AttributeAssign markerAssign = null;
                  
                  if (StringUtils.isNotBlank(grouperAttestationObjectAttribute.getMarkerAttributeAssignId())) {
                    markerAssign = GrouperDAOFactory.getFactory().getAttributeAssign().findById(grouperAttestationObjectAttribute.getMarkerAttributeAssignId(), false);
                  }
                  
                  if (markerAssign == null) {
                    markerAssign = object.getAttributeDelegate().internal_assignAttributeHelper(null, attributeDefNameBase, false, null, null).getAttributeAssign();
                  }
                  
                  if (!GrouperUtil.equals(existingAttestationDirectAssign, actualAttestationDirectAssign)) {
                    if (LOG.isInfoEnabled()) {
                      LOG.info("For group/stem =" + grouperAttestationObjectAttribute.getName() + " updating attestationDirectAssignment to: " + actualAttestationDirectAssign);
                    }
                    
                    markerAssign.getAttributeValueDelegate().assignValue(attributeDefNameDirectAssign.getName(), actualAttestationDirectAssign);
                  }
                  
                  if (!GrouperUtil.equals(existingHasAttesttion, actualHasAttestation)) {
                    if (LOG.isInfoEnabled()) {
                      LOG.info("For group/stem = " + grouperAttestationObjectAttribute.getName() + " updating attestationHasAttestation to: " + actualHasAttestation);
                    }
                    
                    if (StringUtils.isBlank(actualHasAttestation)) {
                      markerAssign.getAttributeDelegate().removeAttribute(attributeDefNameHasAttestation);
                    } else {
                      markerAssign.getAttributeValueDelegate().assignValue(attributeDefNameHasAttestation.getName(), actualHasAttestation);
                    }
                  }
                  
                  if (RESET_EXISTING_CERTIFIED_DATE) {
                    markerAssign.getAttributeValueDelegate().assignValue(attributeDefNameDateCertified.getName(), actualMinDateCertified);
                    markerAssign.getAttributeValueDelegate().assignValue(attributeDefNameCalculatedDaysLeft.getName(), actualDaysUntilRecertify);
                  }
                  
                  
                  return null;
                }
              });
          
          if (grouperAttestationObjectAttribute.isOwnedByGroup()) {
            objectTypesAttributesGroupsAddedOrUpdated++;
           
          } else {
            objectTypesAttributesFoldersAddedOrUpdated++;
          }
        }
      }
    }
    
    if (objectTypesAttributesGroupsAddedOrUpdated > 0) {
      debugMap.put("attestationAttributesGroupsAddedOrUpdated", objectTypesAttributesGroupsAddedOrUpdated);
    }
    
    if (objectTypesAttributesFoldersAddedOrUpdated > 0) {
      debugMap.put("attestationTypesAttributesFoldersAddedOrUpdated", objectTypesAttributesFoldersAddedOrUpdated);
    }
    
    if (objectTypesAttributesGroupsDeleted > 0) {
      debugMap.put("attestationTypesAttributesGroupsDeleted", objectTypesAttributesGroupsDeleted);
    }
    
    if (objectTypesAttributesFoldersDeleted > 0) {
      debugMap.put("attestationTypesAttributesFoldersDeleted", objectTypesAttributesFoldersDeleted);
    }
  }
  
  /**
   * events to process
   */
  private List<EsbEventContainer> eventsToProcess;
  
  /**
   * map of group id to name where group was just added
   */
  private Map<String, String> groupIdsToNamesAdd = new HashMap<String, String>();

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
   * all groups that have events and their attributes
   */
  private Map<String, GrouperAttestationObjectAttributes> groupsWithAttributesToProcess = new HashMap<String, GrouperAttestationObjectAttributes>();
  
  /**
   * all stems that have events and their attributes
   */
  private Map<String, GrouperAttestationObjectAttributes> foldersWithAttributesToProcess = new HashMap<String, GrouperAttestationObjectAttributes>();
  
  /**
   * all children groups of stems that have attribute change
   */
  private Map<String, GrouperAttestationObjectAttributes> childrenGroupsAttestationAttributes = new HashMap<String, GrouperAttestationObjectAttributes>();

  /**
   * all groups that have attestation attributes or have an ancestor that have attestation attributes
   */
  private Map<String, GrouperAttestationObjectAttributes> groupsWithOrWithoutAttributesToProcess = new HashMap<String, GrouperAttestationObjectAttributes>();
  
  /**
   * all ancestor stems of stems and groups that have attestation attributes
   */
  private Map<String, GrouperAttestationObjectAttributes> ancestorStemsAttestationAttributes = new HashMap<String, GrouperAttestationObjectAttributes>();
  
  private void populateIdsAndNamesToWorkOn() {
    
    Set<String> queriedPITAttributeAssignIds = new HashSet<String>();
    Set<String> queriedStemIds = new HashSet<String>();
    Set<String> queriedGroupIds = new HashSet<String>();
    
    for (EsbEventContainer esbEventContainer : eventsToProcess) {
      
      EsbEvent esbEvent = esbEventContainer.getEsbEvent();
      EsbEventType esbEventType = esbEventContainer.getEsbEventType();
      
      if (esbEventType == EsbEventType.GROUP_ADD) {
        
       groupIdsToNamesAdd.put(esbEvent.getGroupId(), esbEvent.getGroupName());
        
      } else if ((esbEventType == EsbEventType.ATTRIBUTE_ASSIGN_VALUE_ADD || esbEventType == EsbEventType.ATTRIBUTE_ASSIGN_VALUE_DELETE) &&
          esbEvent.getAttributeDefNameName().startsWith(GrouperAttestationJob.attestationStemName())) {
        
        PITAttributeAssign pitAttributeAssign = GrouperDAOFactory.getFactory().getPITAttributeAssign()
            .findBySourceIdMostRecent(esbEvent.getAttributeAssignId(), false);

        if (pitAttributeAssign != null) {
          // query pit to see if this is for a folder and is direct and type of attestation is group
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
   * @return stem id if is/was a direct folder assignment
   */
  public static String retrieveStemIdIfDirectStemAssignmentByPITMarkerAttributeAssignId(String markerAttributeAssignId) {

    String sql = "SELECT gps.source_id " +
        "FROM " + 
        "    grouper_pit_stems gps, " +
        "    grouper_pit_attribute_assign gpaa_marker, " + 
        "    grouper_pit_attribute_assign gpaa_attestation, " + 
        "    grouper_pit_attribute_assign gpaa_direct, " + 
        "    grouper_pit_attr_assn_value gpaav_attestation, " + 
        "    grouper_pit_attr_assn_value gpaav_direct, " + 
        "    grouper_pit_attr_def_name gpadn_marker, " + 
        "    grouper_pit_attr_def_name gpadn_attestation, " + 
        "    grouper_pit_attr_def_name gpadn_direct " + 
        "WHERE " + 
        "    gpaa_marker.id = ? " +
        "    AND gpaa_marker.owner_stem_id = gps.id " +
        "    AND gpaa_marker.attribute_def_name_id = gpadn_marker.id " + 
        "    AND gpadn_marker.name = ? " + 
        "    AND gpaa_marker.id = gpaa_attestation.owner_attribute_assign_id " + 
        "    AND gpaa_attestation.attribute_def_name_id = gpadn_attestation.id " + 
        "    AND gpadn_attestation.name = ? " + 
        "    AND gpaav_attestation.attribute_assign_id = gpaa_attestation.id " + 
        "    AND gpaav_attestation.value_string = 'group' " + 
        "    AND gpaa_marker.id = gpaa_direct.owner_attribute_assign_id " + 
        "    AND gpaa_direct.attribute_def_name_id = gpadn_direct.id " + 
        "    AND gpadn_direct.name = ? " +
        "    AND gpaav_direct.attribute_assign_id = gpaa_direct.id " +
        "    AND gpaav_direct.value_string = 'true' ";
    
    List<Object> paramsInitial = new ArrayList<Object>();
    paramsInitial.add(markerAttributeAssignId);
    
    paramsInitial.add(GrouperAttestationJob.retrieveAttributeDefNameValueDef().getName());
    paramsInitial.add(GrouperAttestationJob.retrieveAttributeDefNameType().getName());
    paramsInitial.add(GrouperAttestationJob.retrieveAttributeDefNameDirectAssignment().getName());
    
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
    paramsInitial.add(GrouperAttestationJob.retrieveAttributeDefNameValueDef().getName());
    paramsInitial.add(GrouperAttestationJob.retrieveAttributeDefNameDirectAssignment().getName());
    
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
  
  public static GrouperAttestationObjectAttributes retrieveAttestationAttributesByGroup(String groupId) {
    
    String sql = "SELECT " +
        "    gg.name, " + 
        "    gadn_config.name, " + 
        "    gaav_config.value_string, " + 
        "    gaa_marker.id " +  
        "FROM " + 
        "    grouper_groups gg, " + 
        "    grouper_attribute_assign gaa_marker, " + 
        "    grouper_attribute_assign gaa_config, " + 
        "    grouper_attribute_assign_value gaav_config, " + 
        "    grouper_attribute_def_name gadn_marker, " + 
        "    grouper_attribute_def_name gadn_config " + 
        "WHERE " + 
        "    gg.id = ? " + 
        "    AND gg.id = gaa_marker.owner_group_id " + 
        "    AND gaa_marker.attribute_def_name_id = gadn_marker.id " + 
        "    AND gadn_marker.name = ? " + 
        "    AND gaa_marker.id = gaa_config.owner_attribute_assign_id " + 
        "    AND gaav_config.attribute_assign_id = gaa_config.id " + 
        "    AND gadn_config.id = gaa_config.attribute_def_name_id " + 
        "    AND gaa_marker.enabled = 'T' " + 
        "    AND gaa_config.enabled = 'T' ";
    
    
    List<Object> paramsInitial = new ArrayList<Object>();
    
    paramsInitial.add(groupId);
    paramsInitial.add(GrouperAttestationJob.retrieveAttributeDefNameValueDef().getName());
    
    List<Type> typesInitial = new ArrayList<Type>();
    typesInitial.add(StringType.INSTANCE);
    typesInitial.add(StringType.INSTANCE);

    List<String[]> queryResults = HibernateSession.bySqlStatic().listSelect(String[].class, sql, paramsInitial, typesInitial);
    
    GrouperAttestationObjectAttributes grouperAttestationAttributes = null;
    
    for (String[] queryResult : queryResults) {
      String groupName = queryResult[0];
      String configName = queryResult[1];
      String configValue = queryResult[2];
      String markerAttributeAssignId = queryResult[3];
      
      if (grouperAttestationAttributes == null) {
        grouperAttestationAttributes = new GrouperAttestationObjectAttributes(groupId, groupName, markerAttributeAssignId);
        grouperAttestationAttributes.setOwnedByGroup(true);
      }
      
      if (configName.equals(GrouperAttestationJob.retrieveAttributeDefNameDateCertified().getName())) {
        grouperAttestationAttributes.setDateCertified(configValue);
      } else if (configName.equals(GrouperAttestationJob.retrieveAttributeDefNameDirectAssignment().getName())) {
        grouperAttestationAttributes.setAttestationDirectAssign(configValue);
      } else if (configName.equals(GrouperAttestationJob.retrieveAttributeDefNameCalculatedDaysLeft().getName())) {
        grouperAttestationAttributes.setCalculatedDaysLeft(configValue);
      } else if (configName.equals(GrouperAttestationJob.retrieveAttributeDefNameHasAttestation().getName())) {
        grouperAttestationAttributes.setHasAttestation(configValue);
      } else if (configName.equals(GrouperAttestationJob.retrieveAttributeDefNameStemScope().getName())) {
        grouperAttestationAttributes.setStemScope(configValue);
      }
      
    }
    
    return grouperAttestationAttributes;
    
  }
  
  public static GrouperAttestationObjectAttributes retrieveAttestationAttributesByStem(String stemId) {
    
    String sql = "SELECT " +
        "    gs.name, " + 
        "    gadn_config.name, " + 
        "    gaav_config.value_string, " + 
        "    gaa_marker.id " +
        "FROM " + 
        "    grouper_stems gs, " + 
        "    grouper_attribute_assign gaa_marker, " +
        "    grouper_attribute_assign gaa_attestation, " + 
        "    grouper_attribute_assign gaa_config, " + 
        "    grouper_attribute_assign_value gaav_attestation, " + 
        "    grouper_attribute_assign_value gaav_config, " + 
        "    grouper_attribute_def_name gadn_marker, " + 
        "    grouper_attribute_def_name gadn_attestation, " + 
        "    grouper_attribute_def_name gadn_config " + 
        "WHERE " + 
        "    gs.id = ? " + 
        "    AND gs.id = gaa_marker.owner_stem_id " + 
        "    AND gaa_marker.attribute_def_name_id = gadn_marker.id " + 
        "    AND gadn_marker.name = ? " + 
        "    AND gaa_marker.id = gaa_attestation.owner_attribute_assign_id " + 
        "    AND gaa_attestation.attribute_def_name_id = gadn_attestation.id " + 
        "    AND gadn_attestation.name = ? " + 
        "    AND gaav_attestation.value_string = 'group' " +
        "    AND gaav_attestation.attribute_assign_id = gaa_attestation.id " + 
        "    AND gaa_marker.id = gaa_config.owner_attribute_assign_id " + 
        "    AND gaav_config.attribute_assign_id = gaa_config.id " + 
        "    AND gadn_config.id = gaa_config.attribute_def_name_id " + 
        "    AND gaa_marker.enabled = 'T' " + 
        "    AND gaa_attestation.enabled = 'T' " + 
        "    AND gaa_config.enabled = 'T' ";
    
    
    List<Object> paramsInitial = new ArrayList<Object>();
    
    paramsInitial.add(stemId);
    paramsInitial.add(GrouperAttestationJob.retrieveAttributeDefNameValueDef().getName());
    paramsInitial.add(GrouperAttestationJob.retrieveAttributeDefNameType().getName());
    
    List<Type> typesInitial = new ArrayList<Type>();
    typesInitial.add(StringType.INSTANCE);
    typesInitial.add(StringType.INSTANCE);
    typesInitial.add(StringType.INSTANCE);

    List<String[]> queryResults = HibernateSession.bySqlStatic().listSelect(String[].class, sql, paramsInitial, typesInitial);
    
    GrouperAttestationObjectAttributes grouperAttestationAttributes = null;
    
    for (String[] queryResult : queryResults) {
      String stemName = queryResult[0];
      String configName = queryResult[1];
      String configValue = queryResult[2];
      String markerAttributeAssignId = queryResult[3];
      
      if (grouperAttestationAttributes == null) {
        grouperAttestationAttributes = new GrouperAttestationObjectAttributes(stemId, stemName, markerAttributeAssignId);
        grouperAttestationAttributes.setOwnedByStem(true);
        grouperAttestationAttributes.setAttestationDirectAssign("true");
      }
      
      if (configName.equals(GrouperAttestationJob.retrieveAttributeDefNameDateCertified().getName())) {
        grouperAttestationAttributes.setDateCertified(configValue);
      } else if (configName.equals(GrouperAttestationJob.retrieveAttributeDefNameMinCertifiedDate().getName())) {
        grouperAttestationAttributes.setMinDateCertified(configValue);
      } else if (configName.equals(GrouperAttestationJob.retrieveAttributeDefNameHasAttestation().getName())) {
        grouperAttestationAttributes.setHasAttestation(configValue);
      } else if (configName.equals(GrouperAttestationJob.retrieveAttributeDefNameStemScope().getName())) {
        grouperAttestationAttributes.setStemScope(configValue);
      } else if (configName.equals(GrouperAttestationJob.retrieveAttributeDefNameDaysUntilRecertify().getName())) {
        grouperAttestationAttributes.setDaysUntilRecertify(configValue);
      }
      
    }
    
    return grouperAttestationAttributes;
    
  }
  
  private void populateAttributesAssignedToGroupsIncremental() {
    
    for (String groupId: groupIdToNamesAddAndAttributeChange.keySet()) {
     
      GrouperAttestationObjectAttributes groupAttestationAttributes =  retrieveAttestationAttributesByGroup(groupId);
      
      if (groupAttestationAttributes != null) {
        groupsWithAttributesToProcess.put(groupIdToNamesAddAndAttributeChange.get(groupId), groupAttestationAttributes);
      }
      
    }
    
  }
  
  private void populateAttributesAssignedToStemsIncremental() {
    
    for (String stemId: stemIdsToNamesAttributeChange.keySet()) {
     
      GrouperAttestationObjectAttributes stemAttestationAttributes =  retrieveAttestationAttributesByStem(stemId);
      
      if (stemAttestationAttributes != null) {
        foldersWithAttributesToProcess.put(stemIdsToNamesAttributeChange.get(stemId), stemAttestationAttributes);
      }
      
    }
    
  }
  
  /**
   * get attestation attributes for a folder and its parents
   * @param stemName
   * @return the attributes
   */
  public static Map<String, GrouperAttestationObjectAttributes> retrieveFolderAndAncestorAttestationAttributesByFolder(String stemName) {
    
    Map<String, GrouperAttestationObjectAttributes> results = new HashMap<String, GrouperAttestationObjectAttributes>();

    {
      String sql = "SELECT " +
          "    gs_then_has_stem.id, " + 
          "    gs_then_has_stem.name, " + 
          "    gadn_config.name, " + 
          "    gaav_config.value_string, " + 
          "    gaa_marker.id " + 
          "FROM " + 
          "    grouper_stems gs, " + 
          "    grouper_stem_set gss, " + 
          "    grouper_stems gs_then_has_stem, " +
          "    grouper_attribute_assign gaa_marker, " + 
          "    grouper_attribute_assign gaa_attestation, " + 
          "    grouper_attribute_assign gaa_config, " + 
          "    grouper_attribute_assign_value gaav_attestation, " + 
          "    grouper_attribute_assign_value gaav_config, " + 
          "    grouper_attribute_def_name gadn_marker, " + 
          "    grouper_attribute_def_name gadn_attestation, " + 
          "    grouper_attribute_def_name gadn_config " + 
          "WHERE " + 
          "    gs.name = ? " +
          "    AND gs.id = gss.if_has_stem_id " +
          "    AND gss.then_has_stem_id = gs_then_has_stem.id " + 
          "    AND gss.then_has_stem_id = gaa_marker.owner_stem_id " + 
          "    AND gaa_marker.attribute_def_name_id = gadn_marker.id " + 
          "    AND gadn_marker.name = ? " + 
          "    AND gaa_marker.id = gaa_attestation.owner_attribute_assign_id " + 
          "    AND gaa_attestation.attribute_def_name_id = gadn_attestation.id " + 
          "    AND gadn_attestation.name = ? " + 
          "    AND gaav_attestation.value_string = 'group' " +
          "    AND gaav_attestation.attribute_assign_id = gaa_attestation.id " + 
          "    AND gaa_marker.id = gaa_config.owner_attribute_assign_id " + 
          "    AND gaav_config.attribute_assign_id = gaa_config.id " + 
          "    AND gadn_config.id = gaa_config.attribute_def_name_id " + 
          "    AND gaa_marker.enabled = 'T' " + 
          "    AND gaa_attestation.enabled = 'T' " + 
          "    AND gaa_config.enabled = 'T' ";
      
      List<Object> paramsInitial = new ArrayList<Object>();
      paramsInitial.add(stemName);
      paramsInitial.add(GrouperAttestationJob.retrieveAttributeDefNameValueDef().getName());
      paramsInitial.add(GrouperAttestationJob.retrieveAttributeDefNameType().getName());
      
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
        String markerAttributeAssignId = queryResult[4];
        
        
        GrouperAttestationObjectAttributes grouperAttestationAttributes = results.get(folderName);
        if (grouperAttestationAttributes == null) {
          grouperAttestationAttributes = new GrouperAttestationObjectAttributes(stemId, folderName, markerAttributeAssignId);
          results.put(folderName, grouperAttestationAttributes);
          grouperAttestationAttributes.setOwnedByStem(true);
          grouperAttestationAttributes.setAttestationDirectAssign("true");
        }
        
        if (configName.equals(GrouperAttestationJob.retrieveAttributeDefNameDateCertified().getName())) {
          grouperAttestationAttributes.setDateCertified(configValue);
        } else if (configName.equals(GrouperAttestationJob.retrieveAttributeDefNameMinCertifiedDate().getName())) {
          grouperAttestationAttributes.setMinDateCertified(configValue);
        } else if (configName.equals(GrouperAttestationJob.retrieveAttributeDefNameHasAttestation().getName())) {
          grouperAttestationAttributes.setHasAttestation(configValue);
        } else if (configName.equals(GrouperAttestationJob.retrieveAttributeDefNameStemScope().getName())) {
          grouperAttestationAttributes.setStemScope(configValue);
        } else if (configName.equals(GrouperAttestationJob.retrieveAttributeDefNameDaysUntilRecertify().getName())) {
          grouperAttestationAttributes.setDaysUntilRecertify(configValue);
        }
        
      }
    }
    
    return results;
  }
  
  private void populateAncestorsIncremental() {
    
    Set<String> stemNamesToProcess = new HashSet<String>();
    
    for (String stemId: stemIdsToNamesAttributeChange.keySet()) {
      
      String stemName = stemIdsToNamesAttributeChange.get(stemId);
      
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
      
      Map<String, GrouperAttestationObjectAttributes> folderNameToAttestationAttributes = retrieveFolderAndAncestorAttestationAttributesByFolder(stemName);
      ancestorStemsAttestationAttributes.putAll(folderNameToAttestationAttributes);
      
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
        
        Map<String, GrouperAttestationObjectAttributes> groupNameToAttestationAttributes = retrieveChildAttestationAttributesGroupAttributesByFolder(stemId);
        childrenGroupsAttestationAttributes.putAll(groupNameToAttestationAttributes);
        
      }
      
    }
    
  }
  
  /**
   * get attestation attributes for groups under a folder
   * @param childStemId
   * @return the attributes
   */
  public static Map<String, GrouperAttestationObjectAttributes> retrieveChildAttestationAttributesGroupAttributesByFolder(String parentStemId) {

    Map<String, GrouperAttestationObjectAttributes> results = new HashMap<String, GrouperAttestationObjectAttributes>();

    {
      String sql = "SELECT " +
          "    gg.id, " + 
          "    gg.name, " + 
          "    gadn_config.name, " + 
          "    gaav_config.value_string, " + 
          "    gaa_marker.id " + 
          "FROM " + 
          "    grouper_stems gs, " + 
          "    grouper_stem_set gss, " +
          "    grouper_groups gg, " + 
          "    grouper_attribute_assign gaa_marker, " + 
          "    grouper_attribute_assign gaa_config, " + 
          "    grouper_attribute_assign_value gaav_config, " + 
          "    grouper_attribute_def_name gadn_marker, " + 
          "    grouper_attribute_def_name gadn_config " + 
          "WHERE " +
          "    gs.id = ? " +
          "    AND gs.id = gss.then_has_stem_id " +
          "    AND gss.if_has_stem_id = gg.parent_stem " +
          "    AND gg.id = gaa_marker.owner_group_id " + 
          "    AND gaa_marker.attribute_def_name_id = gadn_marker.id " + 
          "    AND gadn_marker.name = ? " + 
          "    AND gaa_marker.id = gaa_config.owner_attribute_assign_id " + 
          "    AND gaav_config.attribute_assign_id = gaa_config.id " + 
          "    AND gadn_config.id = gaa_config.attribute_def_name_id " + 
          "    AND gaa_marker.enabled = 'T' " + 
          "    AND gaa_config.enabled = 'T' ";
     
      List<Object> paramsInitial = new ArrayList<Object>();

      paramsInitial.add(parentStemId);
      paramsInitial.add(GrouperAttestationJob.retrieveAttributeDefNameValueDef().getName());
      
      List<Type> typesInitial = new ArrayList<Type>();
      typesInitial.add(StringType.INSTANCE);
      typesInitial.add(StringType.INSTANCE);
  
      List<String[]> queryResults = HibernateSession.bySqlStatic().listSelect(String[].class, sql, paramsInitial, typesInitial);
      for (String[] queryResult : queryResults) {
        String groupId = queryResult[0];
        String groupName = queryResult[1];
        String configName = queryResult[2];
        String configValue = queryResult[3];
        String markerAttributeAssignId = queryResult[4];
        
        GrouperAttestationObjectAttributes grouperAttestationAttributes = results.get(groupName);
        if (grouperAttestationAttributes == null) {
          grouperAttestationAttributes = new GrouperAttestationObjectAttributes(groupId, groupName, markerAttributeAssignId);
          results.put(groupName, grouperAttestationAttributes);
          grouperAttestationAttributes.setOwnedByGroup(true);
        }
        
        if (configName.equals(GrouperAttestationJob.retrieveAttributeDefNameDateCertified().getName())) {
          grouperAttestationAttributes.setDateCertified(configValue);
        } else if (configName.equals(GrouperAttestationJob.retrieveAttributeDefNameDirectAssignment().getName())) {
          grouperAttestationAttributes.setAttestationDirectAssign(configValue);
        } else if (configName.equals(GrouperAttestationJob.retrieveAttributeDefNameCalculatedDaysLeft().getName())) {
          grouperAttestationAttributes.setCalculatedDaysLeft(configValue);
        } else if (configName.equals(GrouperAttestationJob.retrieveAttributeDefNameHasAttestation().getName())) {
          grouperAttestationAttributes.setHasAttestation(configValue);
        } else if (configName.equals(GrouperAttestationJob.retrieveAttributeDefNameStemScope().getName())) {
          grouperAttestationAttributes.setStemScope(configValue);
        }
        
      }
     
    }
    
    return results;
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
        
      }
      
    }
  }
  
  public static void populateGroupChildrenOfAFolderWhichMayOrMayNotHaveAttributes(String parentStemId, Map<String, GrouperAttestationObjectAttributes> mapOfGroupNameToAttributes) {
    
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
    
    for (String[] queryResult : queryResults) {
      String groupId = queryResult[0];
      String groupName = queryResult[1];
      
      if (mapOfGroupNameToAttributes.get(groupName) == null) {
        mapOfGroupNameToAttributes.put(groupName, new GrouperAttestationObjectAttributes(groupId, groupName, null));
        mapOfGroupNameToAttributes.get(groupName).setOwnedByGroup(true);
      }
    }
      
  }
  
  private void populateEventObjectsWhichDoNotHaveAttributes() {
      
    for (String groupId: groupIdToNamesAddAndAttributeChange.keySet()) {
     
      String groupName = groupIdToNamesAddAndAttributeChange.get(groupId);
      
      if (groupsWithOrWithoutAttributesToProcess.get(groupName) == null) {
        groupsWithOrWithoutAttributesToProcess.put(groupName, new GrouperAttestationObjectAttributes(groupId, groupName, null));
        groupsWithOrWithoutAttributesToProcess.get(groupName).setOwnedByGroup(true);
      }
    }
    
  }
  
  public void incrementalLogic(List<EsbEventContainer> esbEventContainers) {
    
    eventsToProcess = esbEventContainers;
    
    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("attestation");
    
    gcGrouperSync.setSyncEngine(GcGrouperSync.ATTESTATION_PROPAGATION);
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
      
      populateAttributesAssignedToStemsIncremental();
      
      populateAncestorsIncremental();
      
      populateChildrenWithAttributesIncremental();
      
      groupsWithOrWithoutAttributesToProcess.putAll(groupsWithAttributesToProcess);
      groupsWithOrWithoutAttributesToProcess.putAll(childrenGroupsAttestationAttributes);
      
      populateEventObjectsWhichDoNotHaveAttributes();
      
      populateChildrenWhichMayOrMayNotHaveAttributes();
      
      Set<GrouperAttestationObjectAttributes> grouperAttestationObjectAttributesToProcess = new HashSet<GrouperAttestationObjectAttributes>();
      grouperAttestationObjectAttributesToProcess.addAll(groupsWithOrWithoutAttributesToProcess.values());
      
      propagateAttestationAttributes(grouperAttestationObjectAttributesToProcess, ancestorStemsAttestationAttributes, debugMap);

    } catch (RuntimeException re) {
      runtimeException = re;
    } finally {
      GcGrouperSyncHeartbeat.endAndWaitForThread(gcGrouperSyncHeartbeat);
      debugMap.put("finalLog", true);
      synchronized (GrouperObjectTypesDaemonLogic.class) {
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
  

  @Override
  public OtherJobOutput run(OtherJobInput otherJobInput) {
    try {
      Map<String, Object> debugMap = fullSyncLogic();
      otherJobInput.getHib3GrouperLoaderLog().setJobMessage("Finished successfully running attestation full sync logic daemon. \n "+GrouperUtil.mapToString(debugMap));
    } catch (Exception e) {
      LOG.warn("Error while running attestation full sync daemon", e);
      otherJobInput.getHib3GrouperLoaderLog().setJobMessage("Finished running object attestation full sync logic daemon with an error: " + ExceptionUtils.getFullStackTrace(e));
    } finally {
      otherJobInput.getHib3GrouperLoaderLog().store();
    }
    return null;
  }
  
  public static void main(String[] args) {
    GrouperSession.startRootSession();
    fullSyncLogic();
  }

}

