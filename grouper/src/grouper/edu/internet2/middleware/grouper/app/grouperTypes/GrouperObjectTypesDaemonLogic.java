package edu.internet2.middleware.grouper.app.grouperTypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.hibernate.type.StringType;
import org.hibernate.type.Type;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
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

public class GrouperObjectTypesDaemonLogic {
  
  private static final Log LOG = GrouperUtil.getLog(GrouperObjectTypesDaemonLogic.class);
  
  /**
   * this method retrieves all types attributes assignments and values for all folders. 
   * Also it returns all folders underneath folders with types assigned.
   * @return
   */
  public static Map<String, Map<String, GrouperObjectTypeObjectAttributes>> retrieveAllFoldersOfInterestForTypes() {
      
    Map<String, Map<String, GrouperObjectTypeObjectAttributes>> results = new HashMap<String, Map<String, GrouperObjectTypeObjectAttributes>>();

    {
      String sql = "SELECT " +
          "    gs.id, " +
          "    gs.name, " + 
          "    gadn_config.name, " + 
          "    gaav_config.value_string, " + 
          "    gaav_type.value_string as object_type, " + 
          "    gaa_marker.id " + 
          "FROM " + 
          "    grouper_stems gs, " + 
          "    grouper_attribute_assign gaa_marker, " + 
          "    grouper_attribute_assign gaa_type, " + 
          "    grouper_attribute_assign gaa_config, " + 
          "    grouper_attribute_assign_value gaav_type, " + 
          "    grouper_attribute_assign_value gaav_config, " + 
          "    grouper_attribute_def_name gadn_marker, " + 
          "    grouper_attribute_def_name gadn_type, " + 
          "    grouper_attribute_def_name gadn_config " + 
          "WHERE " + 
          "    gs.id = gaa_marker.owner_stem_id " + 
          "    AND gaa_marker.attribute_def_name_id = gadn_marker.id " + 
          "    AND gadn_marker.name = ? " + 
          "    AND gaa_marker.id = gaa_type.owner_attribute_assign_id " + 
          "    AND gaa_type.attribute_def_name_id = gadn_type.id " + 
          "    AND gadn_type.name = ? " + 
          "    AND gaav_type.attribute_assign_id = gaa_type.id " + 
          "    AND gaa_marker.id = gaa_config.owner_attribute_assign_id " + 
          "    AND gaav_config.attribute_assign_id = gaa_config.id " + 
          "    AND gadn_config.id = gaa_config.attribute_def_name_id " + 
          "    AND gaa_marker.enabled = 'T' " + 
          "    AND gaa_type.enabled = 'T' " + 
          "    AND gaa_config.enabled = 'T' ";
      
      List<Object> paramsInitial = new ArrayList<Object>();

      paramsInitial.add(GrouperObjectTypesAttributeNames.retrieveAttributeDefNameBase().getName());
      paramsInitial.add(GrouperObjectTypesAttributeNames.retrieveAttributeDefNameTypeName().getName());
      
      List<Type> typesInitial = new ArrayList<Type>();
      typesInitial.add(StringType.INSTANCE);
      typesInitial.add(StringType.INSTANCE);
  
      List<String[]> queryResults = HibernateSession.bySqlStatic().listSelect(String[].class, sql, paramsInitial, typesInitial);
      for (String[] queryResult : queryResults) {
        String stemId = queryResult[0];
        String stemName = queryResult[1];
        String configName = queryResult[2];
        String configValue = queryResult[3];
        String objectType = queryResult[4];
        String markerAttributeAssignId = queryResult[5];
        
        Map<String, GrouperObjectTypeObjectAttributes> stemToObjectTypeAttributes = results.get(objectType);
        if (stemToObjectTypeAttributes == null) {
          stemToObjectTypeAttributes = new HashMap<String, GrouperObjectTypeObjectAttributes>();
          results.put(objectType, stemToObjectTypeAttributes);
        }
        
        GrouperObjectTypeObjectAttributes grouperObjectTypeAttributes = stemToObjectTypeAttributes.get(stemName);
        if (grouperObjectTypeAttributes == null) {
          grouperObjectTypeAttributes = new GrouperObjectTypeObjectAttributes(stemId, stemName, markerAttributeAssignId);
          stemToObjectTypeAttributes.put(stemName, grouperObjectTypeAttributes);
          grouperObjectTypeAttributes.setOwnedByStem(true);
        }
        
        if (configName.equals(GrouperObjectTypesAttributeNames.retrieveAttributeDefNameDataOwner().getName())) {
          grouperObjectTypeAttributes.setObjectTypeDataOwner(configValue);
        } else if (configName.equals(GrouperObjectTypesAttributeNames.retrieveAttributeDefNameMemberDescription().getName())) {
          grouperObjectTypeAttributes.setObjectTypeMemberDescription(configValue);
        } else if (configName.equals(GrouperObjectTypesAttributeNames.retrieveAttributeDefNameTypeName().getName())) {
          grouperObjectTypeAttributes.setObjectTypeName(configValue);
        } else if (configName.equals(GrouperObjectTypesAttributeNames.retrieveAttributeDefNameOwnerStemId().getName())) {
          grouperObjectTypeAttributes.setObjectTypeOwnerStemId(configValue);
        } else if (configName.equals(GrouperObjectTypesAttributeNames.retrieveAttributeDefNameServiceName().getName())) {
          grouperObjectTypeAttributes.setObjectTypeServiceName(configValue);
        } else if (configName.equals(GrouperObjectTypesAttributeNames.retrieveAttributeDefNameDirectAssignment().getName())) {
          grouperObjectTypeAttributes.setObjectTypeDirectAssign(configValue);
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
      
      paramsInitial.add(GrouperObjectTypesAttributeNames.retrieveAttributeDefNameBase().getName());
      paramsInitial.add(GrouperObjectTypesAttributeNames.retrieveAttributeDefNameDirectAssignment().getName());
      
      List<Type> typesInitial = new ArrayList<Type>();
      typesInitial.add(StringType.INSTANCE);
      typesInitial.add(StringType.INSTANCE);
  
      List<String[]> queryResults = HibernateSession.bySqlStatic().listSelect(String[].class, sql, paramsInitial, typesInitial);

      for (String typeName: results.keySet()) {
        
        Map<String, GrouperObjectTypeObjectAttributes> stemToObjectTypeAttributes = results.get(typeName);
        
        for (String[] queryResult : queryResults) {
          String stemId = queryResult[0];
          String stemName = queryResult[1];
          
          if (stemToObjectTypeAttributes.get(stemName) == null) {
            
            Set<String> parentStemNames = GrouperUtil.findParentStemNames(stemName);
            for (String parentName: parentStemNames) {
              if (stemToObjectTypeAttributes.containsKey(parentName)) {
                stemToObjectTypeAttributes.put(stemName, new GrouperObjectTypeObjectAttributes(stemId, stemName, null));
                stemToObjectTypeAttributes.get(stemName).setOwnedByStem(true);
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
   * this method retrieves all types attributes assignments and values for all groups. 
   * Also it returns all groups underneath folders with types assigned.
   * @return
   */
  public static Map<String, Map<String, GrouperObjectTypeObjectAttributes>> retrieveAllGroupsOfInterestForTypes(Map<String, 
      Map<String, GrouperObjectTypeObjectAttributes>> allStemsOfInterestForTypes) {
    
    Map<String, Map<String, GrouperObjectTypeObjectAttributes>> results = new HashMap<String, Map<String, GrouperObjectTypeObjectAttributes>>();

    {
      String sql = "SELECT " +
          "    gg.id, " + 
          "    gg.name, " + 
          "    gadn_config.name, " + 
          "    gaav_config.value_string, " + 
          "    gaav_type.value_string as object_type, " + 
          "    gaa_marker.id " + 
          "FROM " + 
          "    grouper_groups gg, " + 
          "    grouper_attribute_assign gaa_marker, " + 
          "    grouper_attribute_assign gaa_type, " + 
          "    grouper_attribute_assign gaa_config, " + 
          "    grouper_attribute_assign_value gaav_type, " + 
          "    grouper_attribute_assign_value gaav_config, " + 
          "    grouper_attribute_def_name gadn_marker, " + 
          "    grouper_attribute_def_name gadn_type, " + 
          "    grouper_attribute_def_name gadn_config " + 
          "WHERE " + 
          "    gg.id = gaa_marker.owner_group_id " + 
          "    AND gaa_marker.attribute_def_name_id = gadn_marker.id " + 
          "    AND gadn_marker.name = ? " + 
          "    AND gaa_marker.id = gaa_type.owner_attribute_assign_id " + 
          "    AND gaa_type.attribute_def_name_id = gadn_type.id " + 
          "    AND gadn_type.name = ? " + 
          "    AND gaav_type.attribute_assign_id = gaa_type.id " + 
          "    AND gaa_marker.id = gaa_config.owner_attribute_assign_id " + 
          "    AND gaav_config.attribute_assign_id = gaa_config.id " + 
          "    AND gadn_config.id = gaa_config.attribute_def_name_id " + 
          "    AND gaa_marker.enabled = 'T' " + 
          "    AND gaa_type.enabled = 'T' " + 
          "    AND gaa_config.enabled = 'T' ";
      
      List<Object> paramsInitial = new ArrayList<Object>();

      paramsInitial.add(GrouperObjectTypesAttributeNames.retrieveAttributeDefNameBase().getName());
      paramsInitial.add(GrouperObjectTypesAttributeNames.retrieveAttributeDefNameTypeName().getName());
      
      List<Type> typesInitial = new ArrayList<Type>();
      typesInitial.add(StringType.INSTANCE);
      typesInitial.add(StringType.INSTANCE);
  
      List<String[]> queryResults = HibernateSession.bySqlStatic().listSelect(String[].class, sql, paramsInitial, typesInitial);
      for (String[] queryResult : queryResults) {
        String groupId = queryResult[0];
        String groupName = queryResult[1];
        String configName = queryResult[2];
        String configValue = queryResult[3];
        String objectType = queryResult[4];
        String markerAttributeAssignId = queryResult[5];
        
        Map<String, GrouperObjectTypeObjectAttributes> groupToObjectTypeAttributes = results.get(objectType);
        if (groupToObjectTypeAttributes == null) {
          groupToObjectTypeAttributes = new HashMap<String, GrouperObjectTypeObjectAttributes>();
          results.put(objectType, groupToObjectTypeAttributes);
        }
        
        GrouperObjectTypeObjectAttributes grouperObjectTypeAttributes = groupToObjectTypeAttributes.get(groupName);
        if (grouperObjectTypeAttributes == null) {
          grouperObjectTypeAttributes = new GrouperObjectTypeObjectAttributes(groupId, groupName, markerAttributeAssignId);
          groupToObjectTypeAttributes.put(groupName, grouperObjectTypeAttributes);
          grouperObjectTypeAttributes.setOwnedByGroup(true);
        }
        
        if (configName.equals(GrouperObjectTypesAttributeNames.retrieveAttributeDefNameDataOwner().getName())) {
          grouperObjectTypeAttributes.setObjectTypeDataOwner(configValue);
        } else if (configName.equals(GrouperObjectTypesAttributeNames.retrieveAttributeDefNameMemberDescription().getName())) {
          grouperObjectTypeAttributes.setObjectTypeMemberDescription(configValue);
        } else if (configName.equals(GrouperObjectTypesAttributeNames.retrieveAttributeDefNameTypeName().getName())) {
          grouperObjectTypeAttributes.setObjectTypeName(configValue);
        } else if (configName.equals(GrouperObjectTypesAttributeNames.retrieveAttributeDefNameOwnerStemId().getName())) {
          grouperObjectTypeAttributes.setObjectTypeOwnerStemId(configValue);
        } else if (configName.equals(GrouperObjectTypesAttributeNames.retrieveAttributeDefNameServiceName().getName())) {
          grouperObjectTypeAttributes.setObjectTypeServiceName(configValue);
        } else if (configName.equals(GrouperObjectTypesAttributeNames.retrieveAttributeDefNameDirectAssignment().getName())) {
          grouperObjectTypeAttributes.setObjectTypeDirectAssign(configValue);
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
      
      paramsInitial.add(GrouperObjectTypesAttributeNames.retrieveAttributeDefNameBase().getName());
      paramsInitial.add(GrouperObjectTypesAttributeNames.retrieveAttributeDefNameDirectAssignment().getName());
      
      List<Type> typesInitial = new ArrayList<Type>();
      typesInitial.add(StringType.INSTANCE);
      typesInitial.add(StringType.INSTANCE);
  
      List<String[]> queryResults = HibernateSession.bySqlStatic().listSelect(String[].class, sql, paramsInitial, typesInitial);
      
     
      Set<String> combinedTypeNames = new HashSet<String>();
      combinedTypeNames.addAll(allStemsOfInterestForTypes.keySet());
      combinedTypeNames.addAll(results.keySet());
      
      for (String typeName: combinedTypeNames) {
        
        Map<String, GrouperObjectTypeObjectAttributes> groupToObjectTypeAttributes = results.get(typeName);
        
        if (groupToObjectTypeAttributes == null) {
          groupToObjectTypeAttributes = new HashMap<String, GrouperObjectTypeObjectAttributes>();
          results.put(typeName, groupToObjectTypeAttributes);
        }
        
        for (String[] queryResult : queryResults) {
          String groupId = queryResult[0];
          String groupName = queryResult[1];
          
          if (!objectNameHasAnAncestorWithThisType(groupName, typeName, allStemsOfInterestForTypes)) {
            continue;
          }
          
          if (groupToObjectTypeAttributes.get(groupName) == null) {
            groupToObjectTypeAttributes.put(groupName, new GrouperObjectTypeObjectAttributes(groupId, groupName, null));
            groupToObjectTypeAttributes.get(groupName).setOwnedByGroup(true);
          }
          
        }
        
      }
      
    }
    
    return results;
    
  }
  
  
  private static boolean objectNameHasAnAncestorWithThisType(String objectName, String typeName, 
      Map<String, Map<String, GrouperObjectTypeObjectAttributes>> stemsOfInterestForTypes) {
   
    Set<String> parentStemNames = GrouperUtil.findParentStemNames(objectName);
    boolean foundAncestorForType = false;
    for (String parentName: parentStemNames) {
      
      Map<String, GrouperObjectTypeObjectAttributes> stemNameToObjectAttributes = stemsOfInterestForTypes.get(typeName);
      if (stemNameToObjectAttributes == null) {
        continue;
      }
      
      if (stemNameToObjectAttributes.containsKey(parentName)) {
        foundAncestorForType = true;
        break;
      }
    }
    
    return foundAncestorForType;
  }
  
  public static Map<String, GrouperObjectTypeObjectAttributes> retrieveObjectTypeAttributesByGroup(String groupId) {
    
    Map<String, GrouperObjectTypeObjectAttributes> results = new HashMap<String, GrouperObjectTypeObjectAttributes>();

    {
      String sql = "SELECT " +
          "    gg.name, " + 
          "    gadn_config.name, " + 
          "    gaav_config.value_string, " + 
          "    gaav_type.value_string as object_type, " + 
          "    gaa_marker.id " +  
          "FROM " + 
          "    grouper_groups gg, " + 
          "    grouper_attribute_assign gaa_marker, " + 
          "    grouper_attribute_assign gaa_type, " + 
          "    grouper_attribute_assign gaa_config, " + 
          "    grouper_attribute_assign_value gaav_type, " + 
          "    grouper_attribute_assign_value gaav_config, " + 
          "    grouper_attribute_def_name gadn_marker, " + 
          "    grouper_attribute_def_name gadn_type, " + 
          "    grouper_attribute_def_name gadn_config " + 
          "WHERE " + 
          "    gg.id = ? " + 
          "    AND gg.id = gaa_marker.owner_group_id " + 
          "    AND gaa_marker.attribute_def_name_id = gadn_marker.id " + 
          "    AND gadn_marker.name = ? " + 
          "    AND gaa_marker.id = gaa_type.owner_attribute_assign_id " + 
          "    AND gaa_type.attribute_def_name_id = gadn_type.id " + 
          "    AND gadn_type.name = ? " + 
          "    AND gaav_type.attribute_assign_id = gaa_type.id " + 
          "    AND gaa_marker.id = gaa_config.owner_attribute_assign_id " + 
          "    AND gaav_config.attribute_assign_id = gaa_config.id " + 
          "    AND gadn_config.id = gaa_config.attribute_def_name_id " + 
          "    AND gaa_marker.enabled = 'T' " + 
          "    AND gaa_type.enabled = 'T' " + 
          "    AND gaa_config.enabled = 'T' ";
      
      
      List<Object> paramsInitial = new ArrayList<Object>();
      
      paramsInitial.add(groupId);
      paramsInitial.add(GrouperObjectTypesAttributeNames.retrieveAttributeDefNameBase().getName());
      paramsInitial.add(GrouperObjectTypesAttributeNames.retrieveAttributeDefNameTypeName().getName());
      
      List<Type> typesInitial = new ArrayList<Type>();
      typesInitial.add(StringType.INSTANCE);
      typesInitial.add(StringType.INSTANCE);
      typesInitial.add(StringType.INSTANCE);
  
      List<String[]> queryResults = HibernateSession.bySqlStatic().listSelect(String[].class, sql, paramsInitial, typesInitial);
      
      GrouperObjectTypeObjectAttributes grouperObjectTypeAttributes = null;
      
      for (String[] queryResult : queryResults) {
        String groupName = queryResult[0];
        String configName = queryResult[1];
        String configValue = queryResult[2];
        String objectType = queryResult[3];
        String markerAttributeAssignId = queryResult[4];
        
        grouperObjectTypeAttributes = results.get(objectType);
        if (grouperObjectTypeAttributes == null) {
          grouperObjectTypeAttributes = new GrouperObjectTypeObjectAttributes(groupId, groupName, markerAttributeAssignId);
          results.put(objectType, grouperObjectTypeAttributes);
        }
        
        grouperObjectTypeAttributes.setOwnedByGroup(true);
        
        if (configName.equals(GrouperObjectTypesAttributeNames.retrieveAttributeDefNameDataOwner().getName())) {
          grouperObjectTypeAttributes.setObjectTypeDataOwner(configValue);
        } else if (configName.equals(GrouperObjectTypesAttributeNames.retrieveAttributeDefNameMemberDescription().getName())) {
          grouperObjectTypeAttributes.setObjectTypeMemberDescription(configValue);
        } else if (configName.equals(GrouperObjectTypesAttributeNames.retrieveAttributeDefNameTypeName().getName())) {
          grouperObjectTypeAttributes.setObjectTypeName(configValue);
        } else if (configName.equals(GrouperObjectTypesAttributeNames.retrieveAttributeDefNameOwnerStemId().getName())) {
          grouperObjectTypeAttributes.setObjectTypeOwnerStemId(configValue);
        } else if (configName.equals(GrouperObjectTypesAttributeNames.retrieveAttributeDefNameServiceName().getName())) {
          grouperObjectTypeAttributes.setObjectTypeServiceName(configValue);
        } else if (configName.equals(GrouperObjectTypesAttributeNames.retrieveAttributeDefNameDirectAssignment().getName())) {
          grouperObjectTypeAttributes.setObjectTypeDirectAssign(configValue);
        }
      }
    }
    
    
    return results;
    
  }
  
  
  public static Map<String, GrouperObjectTypeObjectAttributes> retrieveObjectTypeAttributesByStem(String stemId) {
    
    Map<String, GrouperObjectTypeObjectAttributes> results = new HashMap<String, GrouperObjectTypeObjectAttributes>();

    {
      String sql = "SELECT " +
          "    gs.name, " + 
          "    gadn_config.name, " + 
          "    gaav_config.value_string, " + 
          "    gaav_type.value_string as object_type, " + 
          "    gaa_marker.id " + 
          "FROM " + 
          "    grouper_stems gs, " + 
          "    grouper_attribute_assign gaa_marker, " +
          "    grouper_attribute_assign gaa_type, " + 
          "    grouper_attribute_assign gaa_config, " + 
          "    grouper_attribute_assign_value gaav_type, " + 
          "    grouper_attribute_assign_value gaav_config, " + 
          "    grouper_attribute_def_name gadn_marker, " + 
          "    grouper_attribute_def_name gadn_type, " + 
          "    grouper_attribute_def_name gadn_config " + 
          "WHERE " + 
          "    gs.id = ? " + 
          "    AND gs.id = gaa_marker.owner_stem_id " + 
          "    AND gaa_marker.attribute_def_name_id = gadn_marker.id " + 
          "    AND gadn_marker.name = ? " + 
          "    AND gaa_marker.id = gaa_type.owner_attribute_assign_id " + 
          "    AND gaa_type.attribute_def_name_id = gadn_type.id " + 
          "    AND gadn_type.name = ? " + 
          "    AND gaav_type.attribute_assign_id = gaa_type.id " + 
          "    AND gaa_marker.id = gaa_config.owner_attribute_assign_id " + 
          "    AND gaav_config.attribute_assign_id = gaa_config.id " + 
          "    AND gadn_config.id = gaa_config.attribute_def_name_id " + 
          "    AND gaa_marker.enabled = 'T' " + 
          "    AND gaa_type.enabled = 'T' " + 
          "    AND gaa_config.enabled = 'T' ";
      
      
      List<Object> paramsInitial = new ArrayList<Object>();
      
      paramsInitial.add(stemId);
      paramsInitial.add(GrouperObjectTypesAttributeNames.retrieveAttributeDefNameBase().getName());
      paramsInitial.add(GrouperObjectTypesAttributeNames.retrieveAttributeDefNameTypeName().getName());
      
      List<Type> typesInitial = new ArrayList<Type>();
      typesInitial.add(StringType.INSTANCE);
      typesInitial.add(StringType.INSTANCE);
      typesInitial.add(StringType.INSTANCE);
  
      List<String[]> queryResults = HibernateSession.bySqlStatic().listSelect(String[].class, sql, paramsInitial, typesInitial);
      
      GrouperObjectTypeObjectAttributes grouperObjectTypeAttributes = null;
      
      for (String[] queryResult : queryResults) {
        String groupName = queryResult[0];
        String configName = queryResult[1];
        String configValue = queryResult[2];
        String objectType = queryResult[3];
        String markerAttributeAssignId = queryResult[4];
        
        grouperObjectTypeAttributes = results.get(objectType);
        if (grouperObjectTypeAttributes == null) {
          grouperObjectTypeAttributes = new GrouperObjectTypeObjectAttributes(stemId, groupName, markerAttributeAssignId);
          results.put(objectType, grouperObjectTypeAttributes);
        }
        
        grouperObjectTypeAttributes.setOwnedByGroup(true);
        
        if (configName.equals(GrouperObjectTypesAttributeNames.retrieveAttributeDefNameDataOwner().getName())) {
          grouperObjectTypeAttributes.setObjectTypeDataOwner(configValue);
        } else if (configName.equals(GrouperObjectTypesAttributeNames.retrieveAttributeDefNameMemberDescription().getName())) {
          grouperObjectTypeAttributes.setObjectTypeMemberDescription(configValue);
        } else if (configName.equals(GrouperObjectTypesAttributeNames.retrieveAttributeDefNameTypeName().getName())) {
          grouperObjectTypeAttributes.setObjectTypeName(configValue);
        } else if (configName.equals(GrouperObjectTypesAttributeNames.retrieveAttributeDefNameOwnerStemId().getName())) {
          grouperObjectTypeAttributes.setObjectTypeOwnerStemId(configValue);
        } else if (configName.equals(GrouperObjectTypesAttributeNames.retrieveAttributeDefNameServiceName().getName())) {
          grouperObjectTypeAttributes.setObjectTypeServiceName(configValue);
        } else if (configName.equals(GrouperObjectTypesAttributeNames.retrieveAttributeDefNameDirectAssignment().getName())) {
          grouperObjectTypeAttributes.setObjectTypeDirectAssign(configValue);
        }
      }
    }
    
    
    return results;
    
  }
  
  /**
   * get object type attributes for a folder and its parents
   * @param stemName
   * @return the attributes
   */
  public static Map<String, Map<String, GrouperObjectTypeObjectAttributes>> retrieveFolderAndAncestorObjectTypesAttributesByFolder(String stemName) {
    
    Map<String, Map<String, GrouperObjectTypeObjectAttributes>> results = new HashMap<String, Map<String, GrouperObjectTypeObjectAttributes>>();

    {
      String sql = "SELECT " +
          "    gs_then_has_stem.id, " + 
          "    gs_then_has_stem.name, " + 
          "    gadn_config.name, " + 
          "    gaav_config.value_string, " + 
          "    gaav_type.value_string as object_type, " + 
          "    gaa_marker.id " + 
          "FROM " + 
          "    grouper_stems gs, " + 
          "    grouper_stem_set gss, " + 
          "    grouper_stems gs_then_has_stem, " +
          "    grouper_attribute_assign gaa_marker, " + 
          "    grouper_attribute_assign gaa_type, " + 
          "    grouper_attribute_assign gaa_config, " + 
          "    grouper_attribute_assign_value gaav_type, " + 
          "    grouper_attribute_assign_value gaav_config, " + 
          "    grouper_attribute_def_name gadn_marker, " + 
          "    grouper_attribute_def_name gadn_type, " + 
          "    grouper_attribute_def_name gadn_config " + 
          "WHERE " + 
          "    gs.name = ? " +
          "    AND gs.id = gss.if_has_stem_id " +
          "    AND gss.then_has_stem_id = gs_then_has_stem.id " + 
          "    AND gss.then_has_stem_id = gaa_marker.owner_stem_id " + 
          "    AND gaa_marker.attribute_def_name_id = gadn_marker.id " + 
          "    AND gadn_marker.name = ? " + 
          "    AND gaa_marker.id = gaa_type.owner_attribute_assign_id " + 
          "    AND gaa_type.attribute_def_name_id = gadn_type.id " + 
          "    AND gadn_type.name = ? " + 
          "    AND gaav_type.attribute_assign_id = gaa_type.id " + 
          "    AND gaa_marker.id = gaa_config.owner_attribute_assign_id " + 
          "    AND gaav_config.attribute_assign_id = gaa_config.id " + 
          "    AND gadn_config.id = gaa_config.attribute_def_name_id " + 
          "    AND gaa_marker.enabled = 'T' " + 
          "    AND gaa_type.enabled = 'T' " + 
          "    AND gaa_config.enabled = 'T' ";
      
      List<Object> paramsInitial = new ArrayList<Object>();
      paramsInitial.add(stemName);
      paramsInitial.add(GrouperObjectTypesAttributeNames.retrieveAttributeDefNameBase().getName());
      paramsInitial.add(GrouperObjectTypesAttributeNames.retrieveAttributeDefNameTypeName().getName());
      
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
        String objectType = queryResult[4];
        String markerAttributeAssignId = queryResult[5];
        
        Map<String, GrouperObjectTypeObjectAttributes> stemToObjectTypeAttributes = results.get(objectType);
        if (stemToObjectTypeAttributes == null) {
          stemToObjectTypeAttributes = new HashMap<String, GrouperObjectTypeObjectAttributes>();
          results.put(objectType, stemToObjectTypeAttributes);
        }
        
        GrouperObjectTypeObjectAttributes grouperObjectTypeAttributes = stemToObjectTypeAttributes.get(folderName);
        if (grouperObjectTypeAttributes == null) {
          grouperObjectTypeAttributes = new GrouperObjectTypeObjectAttributes(stemId, folderName, markerAttributeAssignId);
          stemToObjectTypeAttributes.put(folderName, grouperObjectTypeAttributes);
          grouperObjectTypeAttributes.setOwnedByStem(true);
        }
        
        if (configName.equals(GrouperObjectTypesAttributeNames.retrieveAttributeDefNameDataOwner().getName())) {
          grouperObjectTypeAttributes.setObjectTypeDataOwner(configValue);
        } else if (configName.equals(GrouperObjectTypesAttributeNames.retrieveAttributeDefNameMemberDescription().getName())) {
          grouperObjectTypeAttributes.setObjectTypeMemberDescription(configValue);
        } else if (configName.equals(GrouperObjectTypesAttributeNames.retrieveAttributeDefNameTypeName().getName())) {
          grouperObjectTypeAttributes.setObjectTypeName(configValue);
        } else if (configName.equals(GrouperObjectTypesAttributeNames.retrieveAttributeDefNameOwnerStemId().getName())) {
          grouperObjectTypeAttributes.setObjectTypeOwnerStemId(configValue);
        } else if (configName.equals(GrouperObjectTypesAttributeNames.retrieveAttributeDefNameServiceName().getName())) {
          grouperObjectTypeAttributes.setObjectTypeServiceName(configValue);
        } else if (configName.equals(GrouperObjectTypesAttributeNames.retrieveAttributeDefNameDirectAssignment().getName())) {
          grouperObjectTypeAttributes.setObjectTypeDirectAssign(configValue);
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
        "    grouper_pit_attribute_assign gpaa_type, " + 
        "    grouper_pit_attribute_assign gpaa_direct, " + 
        "    grouper_pit_attr_assn_value gpaav_type, " + 
        "    grouper_pit_attr_assn_value gpaav_direct, " + 
        "    grouper_pit_attr_def_name gpadn_marker, " + 
        "    grouper_pit_attr_def_name gpadn_type, " + 
        "    grouper_pit_attr_def_name gpadn_direct " + 
        "WHERE " + 
        "    gpaa_marker.id = ? " +
        "    AND gpaa_marker.owner_stem_id = gps.id " +
        "    AND gpaa_marker.attribute_def_name_id = gpadn_marker.id " + 
        "    AND gpadn_marker.name = ? " + 
        "    AND gpaa_marker.id = gpaa_type.owner_attribute_assign_id " + 
        "    AND gpaa_type.attribute_def_name_id = gpadn_type.id " + 
        "    AND gpadn_type.name = ? " + 
        "    AND gpaav_type.attribute_assign_id = gpaa_type.id " + 
        "    AND gpaa_marker.id = gpaa_direct.owner_attribute_assign_id " + 
        "    AND gpaa_direct.attribute_def_name_id = gpadn_direct.id " + 
        "    AND gpadn_direct.name = ? " + 
        "    AND gpaav_direct.attribute_assign_id = gpaa_direct.id " +
        "    AND gpaav_direct.value_string = 'true' ";
    
    List<Object> paramsInitial = new ArrayList<Object>();
    paramsInitial.add(markerAttributeAssignId);
    
    paramsInitial.add(GrouperObjectTypesAttributeNames.retrieveAttributeDefNameBase().getName());
    paramsInitial.add(GrouperObjectTypesAttributeNames.retrieveAttributeDefNameTypeName().getName());
    paramsInitial.add(GrouperObjectTypesAttributeNames.retrieveAttributeDefNameDirectAssignment().getName());
    
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
        "    grouper_pit_attribute_assign gpaa_type, " + 
        "    grouper_pit_attribute_assign gpaa_direct, " + 
        "    grouper_pit_attr_assn_value gpaav_type, " + 
        "    grouper_pit_attr_assn_value gpaav_direct, " + 
        "    grouper_pit_attr_def_name gpadn_marker, " + 
        "    grouper_pit_attr_def_name gpadn_type, " + 
        "    grouper_pit_attr_def_name gpadn_direct " + 
        "WHERE " + 
        "    gpaa_marker.id = ? " +
        "    AND gpaa_marker.owner_group_id = gpg.id " +
        "    AND gpaa_marker.attribute_def_name_id = gpadn_marker.id " + 
        "    AND gpadn_marker.name = ? " + 
        "    AND gpaa_marker.id = gpaa_type.owner_attribute_assign_id " + 
        "    AND gpaa_type.attribute_def_name_id = gpadn_type.id " + 
        "    AND gpadn_type.name = ? " + 
        "    AND gpaav_type.attribute_assign_id = gpaa_type.id " + 
        "    AND gpaa_marker.id = gpaa_direct.owner_attribute_assign_id " + 
        "    AND gpaa_direct.attribute_def_name_id = gpadn_direct.id " + 
        "    AND gpadn_direct.name = ? " + 
        "    AND gpaav_direct.attribute_assign_id = gpaa_direct.id " +
        "    AND gpaav_direct.value_string = 'true' ";
    
    List<Object> paramsInitial = new ArrayList<Object>();
    paramsInitial.add(markerAttributeAssignId);
    
    paramsInitial.add(GrouperObjectTypesAttributeNames.retrieveAttributeDefNameBase().getName());
    paramsInitial.add(GrouperObjectTypesAttributeNames.retrieveAttributeDefNameTypeName().getName());
    paramsInitial.add(GrouperObjectTypesAttributeNames.retrieveAttributeDefNameDirectAssignment().getName());
    
    List<Type> typesInitial = new ArrayList<Type>();
    typesInitial.add(StringType.INSTANCE);
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
   * get object type attributes for a folder and its folder children
   * @param childStemId
   * @return the attributes
   */
  public static Map<String, Map<String, GrouperObjectTypeObjectAttributes>> retrieveChildObjectTypesFolderAttributesByFolder(String parentStemId) {

    Map<String, Map<String, GrouperObjectTypeObjectAttributes>> results = new HashMap<String, Map<String, GrouperObjectTypeObjectAttributes>>();

    {
      
      String sql = "SELECT " +
          "    gs_if_has_stem.id, " + 
          "    gs_if_has_stem.name, " +
          "    gadn_config.name, " + 
          "    gaav_config.value_string, " + 
          "    gaav_type.value_string as object_type, " + 
          "    gaa_marker.id " + 
          "FROM " + 
          "    grouper_stems gs, " + 
          "    grouper_stem_set gss, " +
          "    grouper_stems gs_if_has_stem, " +
          "    grouper_attribute_assign gaa_marker, " + 
          "    grouper_attribute_assign gaa_type, " + 
          "    grouper_attribute_assign gaa_config, " + 
          "    grouper_attribute_assign_value gaav_type, " + 
          "    grouper_attribute_assign_value gaav_config, " + 
          "    grouper_attribute_def_name gadn_marker, " + 
          "    grouper_attribute_def_name gadn_type, " + 
          "    grouper_attribute_def_name gadn_config " + 
          "WHERE " + 
          "    gs.id = ? " + 
          "    AND gs.id = gss.then_has_stem_id " +
          "    AND gss.if_has_stem_id = gs_if_has_stem.id " + 
          "    AND gss.if_has_stem_id = gaa_marker.owner_stem_id " + 
          "    AND gaa_marker.attribute_def_name_id = gadn_marker.id " + 
          "    AND gadn_marker.name = ? " + 
          "    AND gaa_marker.id = gaa_type.owner_attribute_assign_id " + 
          "    AND gaa_type.attribute_def_name_id = gadn_type.id " + 
          "    AND gadn_type.name = ? " + 
          "    AND gaav_type.attribute_assign_id = gaa_type.id " + 
          "    AND gaa_marker.id = gaa_config.owner_attribute_assign_id " + 
          "    AND gaav_config.attribute_assign_id = gaa_config.id " + 
          "    AND gadn_config.id = gaa_config.attribute_def_name_id " + 
          "    AND gaa_marker.enabled = 'T' " + 
          "    AND gaa_type.enabled = 'T' " + 
          "    AND gaa_config.enabled = 'T' ";
      
      List<Object> paramsInitial = new ArrayList<Object>();
      
      paramsInitial.add(parentStemId);
      paramsInitial.add(GrouperObjectTypesAttributeNames.retrieveAttributeDefNameBase().getName());
      paramsInitial.add(GrouperObjectTypesAttributeNames.retrieveAttributeDefNameTypeName().getName());
      
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
        String objectType = queryResult[4];
        String markerAttributeAssignId = queryResult[5];
        
        
        Map<String, GrouperObjectTypeObjectAttributes> stemToObjectTypeAttributes = results.get(objectType);
        if (stemToObjectTypeAttributes == null) {
          stemToObjectTypeAttributes = new HashMap<String, GrouperObjectTypeObjectAttributes>();
          results.put(objectType, stemToObjectTypeAttributes);
        }
        
        GrouperObjectTypeObjectAttributes grouperObjectTypeAttributes = stemToObjectTypeAttributes.get(stemName);
    
        if (grouperObjectTypeAttributes == null) {
          grouperObjectTypeAttributes = new GrouperObjectTypeObjectAttributes(stemId, stemName, markerAttributeAssignId);
          stemToObjectTypeAttributes.put(stemName, grouperObjectTypeAttributes);
          grouperObjectTypeAttributes.setOwnedByStem(true);
        }
        
        if (configName.equals(GrouperObjectTypesAttributeNames.retrieveAttributeDefNameDataOwner().getName())) {
          grouperObjectTypeAttributes.setObjectTypeDataOwner(configValue);
        } else if (configName.equals(GrouperObjectTypesAttributeNames.retrieveAttributeDefNameMemberDescription().getName())) {
          grouperObjectTypeAttributes.setObjectTypeMemberDescription(configValue);
        } else if (configName.equals(GrouperObjectTypesAttributeNames.retrieveAttributeDefNameTypeName().getName())) {
          grouperObjectTypeAttributes.setObjectTypeName(configValue);
        } else if (configName.equals(GrouperObjectTypesAttributeNames.retrieveAttributeDefNameOwnerStemId().getName())) {
          grouperObjectTypeAttributes.setObjectTypeOwnerStemId(configValue);
        } else if (configName.equals(GrouperObjectTypesAttributeNames.retrieveAttributeDefNameServiceName().getName())) {
          grouperObjectTypeAttributes.setObjectTypeServiceName(configValue);
        } else if (configName.equals(GrouperObjectTypesAttributeNames.retrieveAttributeDefNameDirectAssignment().getName())) {
          grouperObjectTypeAttributes.setObjectTypeDirectAssign(configValue);
        }
      }

    }
    
    
    return results;
  }
  
  public static void populateFolderChildrenOfAFolderWhichMayOrMayNotHaveAttributes(String parentStemId, Map<String, Map<String, GrouperObjectTypeObjectAttributes>> mapOfTypeToFolderNameToAttributes) {
    
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
    
    for (String typeName: mapOfTypeToFolderNameToAttributes.keySet()) {
      
      Map<String, GrouperObjectTypeObjectAttributes> groupToObjectTypeAttributes = mapOfTypeToFolderNameToAttributes.get(typeName);
      
      if (groupToObjectTypeAttributes == null) {
        groupToObjectTypeAttributes = new HashMap<String, GrouperObjectTypeObjectAttributes>();
        mapOfTypeToFolderNameToAttributes.put(typeName, groupToObjectTypeAttributes);
      }
      
      for (String[] queryResult : queryResults) {
        String stemId = queryResult[0];
        String stemName = queryResult[1];
        
        if (groupToObjectTypeAttributes.get(stemName) == null) {
          groupToObjectTypeAttributes.put(stemName, new GrouperObjectTypeObjectAttributes(stemId, stemName, null));
          groupToObjectTypeAttributes.get(stemName).setOwnedByGroup(true);
        }
      }
    }
      
  }
  
  public static void populateGroupChildrenOfAFolderWhichMayOrMayNotHaveAttributes(String parentStemId, Map<String, Map<String, GrouperObjectTypeObjectAttributes>> mapOfTypeToGroupNameToAttributes) {
    
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
    
    for (String typeName: mapOfTypeToGroupNameToAttributes.keySet()) {
      
      Map<String, GrouperObjectTypeObjectAttributes> groupToObjectTypeAttributes = mapOfTypeToGroupNameToAttributes.get(typeName);
      
      if (groupToObjectTypeAttributes == null) {
        groupToObjectTypeAttributes = new HashMap<String, GrouperObjectTypeObjectAttributes>();
        mapOfTypeToGroupNameToAttributes.put(typeName, groupToObjectTypeAttributes);
      }
      
      for (String[] queryResult : queryResults) {
        String groupId = queryResult[0];
        String groupName = queryResult[1];
        
        if (groupToObjectTypeAttributes.get(groupName) == null) {
          groupToObjectTypeAttributes.put(groupName, new GrouperObjectTypeObjectAttributes(groupId, groupName, null));
          groupToObjectTypeAttributes.get(groupName).setOwnedByGroup(true);
        }
      }
    }
      
  }
  
  
  /**
   * get object types attributes for groups under a folder
   * @param childStemId
   * @return the attributes
   */
  public static Map<String, Map<String, GrouperObjectTypeObjectAttributes>> retrieveChildObjectTypesGroupAttributesByFolder(String parentStemId) {

    Map<String, Map<String, GrouperObjectTypeObjectAttributes>> results = new HashMap<String, Map<String, GrouperObjectTypeObjectAttributes>>();

    {
      String sql = "SELECT " +
          "    gg.id, " + 
          "    gg.name, " + 
          "    gadn_config.name, " + 
          "    gaav_config.value_string, " + 
          "    gaav_type.value_string as object_type, " + 
          "    gaa_marker.id " + 
          "FROM " + 
          "    grouper_stems gs, " + 
          "    grouper_stem_set gss, " +
          "    grouper_groups gg, " + 
          "    grouper_attribute_assign gaa_marker, " + 
          "    grouper_attribute_assign gaa_type, " + 
          "    grouper_attribute_assign gaa_config, " + 
          "    grouper_attribute_assign_value gaav_type, " + 
          "    grouper_attribute_assign_value gaav_config, " + 
          "    grouper_attribute_def_name gadn_marker, " + 
          "    grouper_attribute_def_name gadn_type, " + 
          "    grouper_attribute_def_name gadn_config " + 
          "WHERE " +
          "    gs.id = ? " +
          "    AND gs.id = gss.then_has_stem_id " +
          "    AND gss.if_has_stem_id = gg.parent_stem " +
          "    AND gg.id = gaa_marker.owner_group_id " + 
          "    AND gaa_marker.attribute_def_name_id = gadn_marker.id " + 
          "    AND gadn_marker.name = ? " + 
          "    AND gaa_marker.id = gaa_type.owner_attribute_assign_id " + 
          "    AND gaa_type.attribute_def_name_id = gadn_type.id " + 
          "    AND gadn_type.name = ? " + 
          "    AND gaav_type.attribute_assign_id = gaa_type.id " + 
          "    AND gaa_marker.id = gaa_config.owner_attribute_assign_id " + 
          "    AND gaav_config.attribute_assign_id = gaa_config.id " + 
          "    AND gadn_config.id = gaa_config.attribute_def_name_id " + 
          "    AND gaa_marker.enabled = 'T' " + 
          "    AND gaa_type.enabled = 'T' " + 
          "    AND gaa_config.enabled = 'T' ";
      
     
      List<Object> paramsInitial = new ArrayList<Object>();

      paramsInitial.add(parentStemId);
      paramsInitial.add(GrouperObjectTypesAttributeNames.retrieveAttributeDefNameBase().getName());
      paramsInitial.add(GrouperObjectTypesAttributeNames.retrieveAttributeDefNameTypeName().getName());
      
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
        String objectType = queryResult[4];
        String markerAttributeAssignId = queryResult[5];
        
        Map<String, GrouperObjectTypeObjectAttributes> groupToObjectTypeAttributes = results.get(objectType);
        if (groupToObjectTypeAttributes == null) {
          groupToObjectTypeAttributes = new HashMap<String, GrouperObjectTypeObjectAttributes>();
          results.put(objectType, groupToObjectTypeAttributes);
        }
        
        GrouperObjectTypeObjectAttributes grouperObjectTypeAttributes = groupToObjectTypeAttributes.get(groupName);
        if (grouperObjectTypeAttributes == null) {
          grouperObjectTypeAttributes = new GrouperObjectTypeObjectAttributes(groupId, groupName, markerAttributeAssignId);
          groupToObjectTypeAttributes.put(groupName, grouperObjectTypeAttributes);
          grouperObjectTypeAttributes.setOwnedByGroup(true);
        }
        
        if (configName.equals(GrouperObjectTypesAttributeNames.retrieveAttributeDefNameDataOwner().getName())) {
          grouperObjectTypeAttributes.setObjectTypeDataOwner(configValue);
        } else if (configName.equals(GrouperObjectTypesAttributeNames.retrieveAttributeDefNameMemberDescription().getName())) {
          grouperObjectTypeAttributes.setObjectTypeMemberDescription(configValue);
        } else if (configName.equals(GrouperObjectTypesAttributeNames.retrieveAttributeDefNameTypeName().getName())) {
          grouperObjectTypeAttributes.setObjectTypeName(configValue);
        } else if (configName.equals(GrouperObjectTypesAttributeNames.retrieveAttributeDefNameOwnerStemId().getName())) {
          grouperObjectTypeAttributes.setObjectTypeOwnerStemId(configValue);
        } else if (configName.equals(GrouperObjectTypesAttributeNames.retrieveAttributeDefNameServiceName().getName())) {
          grouperObjectTypeAttributes.setObjectTypeServiceName(configValue);
        } else if (configName.equals(GrouperObjectTypesAttributeNames.retrieveAttributeDefNameDirectAssignment().getName())) {
          grouperObjectTypeAttributes.setObjectTypeDirectAssign(configValue);
        }
      }
     
    }
    
    return results;
  }
  
  
  private void populateAttributesAssignedToGroupsIncremental() {
    
    for (String groupId: groupIdToNamesAddAndAttributeChange.keySet()) {
     
      Map<String, GrouperObjectTypeObjectAttributes> groupTypeAttributes =  retrieveObjectTypeAttributesByGroup(groupId);
      
      for (String type: groupTypeAttributes.keySet()) {
        
        Map<String, GrouperObjectTypeObjectAttributes> groupNameToAttributes = groupsWithAttributesToProcess.get(type);
        
        if (groupNameToAttributes == null) {
          groupNameToAttributes = new HashMap<String, GrouperObjectTypeObjectAttributes>();
          groupsWithAttributesToProcess.put(type, groupNameToAttributes);
        }
        
        groupNameToAttributes.put(groupIdToNamesAddAndAttributeChange.get(groupId), groupTypeAttributes.get(type));
        
      }
      
    }
    
  }
  
  private void populateAttributesAssignedToStemsIncremental() {
    
    for (String stemId: stemIdToNamesAddAndAttributeChange.keySet()) {
     
      Map<String, GrouperObjectTypeObjectAttributes> stemTypeAttributes =  retrieveObjectTypeAttributesByStem(stemId);
      
      for (String type: stemTypeAttributes.keySet()) {
        
        Map<String, GrouperObjectTypeObjectAttributes> stemNameToAttributes = foldersWithAttributesToProcess.get(type);
        
        if (stemNameToAttributes == null) {
          stemNameToAttributes = new HashMap<String, GrouperObjectTypeObjectAttributes>();
          foldersWithAttributesToProcess.put(type, stemNameToAttributes);
        }
        
        stemNameToAttributes.put(stemIdToNamesAddAndAttributeChange.get(stemId), stemTypeAttributes.get(type));
        
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
      
      Map<String, Map<String, GrouperObjectTypeObjectAttributes>> typeToFolderNameToObjectTypeAttributes = retrieveFolderAndAncestorObjectTypesAttributesByFolder(stemName);
      
      addFromOneMapOfMapsToAnother(typeToFolderNameToObjectTypeAttributes, ancestorStemsTypeAttributes);
      
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
        
        Map<String, Map<String, GrouperObjectTypeObjectAttributes>> typeToFolderNameToObjectTypeAttributes = retrieveChildObjectTypesFolderAttributesByFolder(stemId);
            
        addFromOneMapOfMapsToAnother(typeToFolderNameToObjectTypeAttributes, childrenStemsTypeAttributes);
        
        Map<String, Map<String, GrouperObjectTypeObjectAttributes>> typeToGroupNameToObjectTypeAttributes = retrieveChildObjectTypesGroupAttributesByFolder(stemId);
        
        addFromOneMapOfMapsToAnother(typeToGroupNameToObjectTypeAttributes, childrenGroupsTypeAttributes);
        
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
    
    for (String typeName: groupsWithOrWithoutAttributesToProcess.keySet()) {
      
      Map<String, GrouperObjectTypeObjectAttributes> groupToObjectTypeAttributes = groupsWithOrWithoutAttributesToProcess.get(typeName);
      
      for (String groupId: groupIdToNamesAddAndAttributeChange.keySet()) {
       
        String groupName = groupIdToNamesAddAndAttributeChange.get(groupId);
        
        if (groupToObjectTypeAttributes.get(groupName) == null) {
          groupToObjectTypeAttributes.put(groupName, new GrouperObjectTypeObjectAttributes(groupId, groupName, null));
          groupToObjectTypeAttributes.get(groupName).setOwnedByGroup(true);
        }
      }
    }
    
    for (String typeName: foldersWithOrWithoutAttributesToProcess.keySet()) {
      
      Map<String, GrouperObjectTypeObjectAttributes> stemToObjectTypeAttributes = foldersWithOrWithoutAttributesToProcess.get(typeName);
      
      for (String stemId: stemIdToNamesAddAndAttributeChange.keySet()) {
       
        String stemName = stemIdToNamesAddAndAttributeChange.get(stemId);
        
        if (stemToObjectTypeAttributes.get(stemName) == null) {
          stemToObjectTypeAttributes.put(stemName, new GrouperObjectTypeObjectAttributes(stemId, stemName, null));
          stemToObjectTypeAttributes.get(stemName).setOwnedByStem(true);
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
        
        //TODO make sure esbEVent.getName is the stem name and getId is the stem id
        stemIdsToNamesAdd.put(esbEvent.getId(), esbEvent.getName());
        
      } else if ((esbEventType == EsbEventType.ATTRIBUTE_ASSIGN_VALUE_ADD || esbEventType == EsbEventType.ATTRIBUTE_ASSIGN_VALUE_DELETE) &&
          esbEvent.getAttributeDefNameName().startsWith(GrouperObjectTypesSettings.objectTypesStemName())) {
        
        PITAttributeAssign pitAttributeAssign = GrouperDAOFactory.getFactory().getPITAttributeAssign().findBySourceIdMostRecent(esbEvent.getAttributeAssignId(), false);

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
  private Map<String, Map<String, GrouperObjectTypeObjectAttributes>> foldersWithAttributesToProcess = new HashMap<String, Map<String, GrouperObjectTypeObjectAttributes>>();
  
  /**
   * all stems that have type attributes or have an ancestor that have type attributes
   */
  private Map<String, Map<String, GrouperObjectTypeObjectAttributes>> foldersWithOrWithoutAttributesToProcess = new HashMap<String, Map<String, GrouperObjectTypeObjectAttributes>>();
  
  /**
   * all ancestor stems of stems and groups that have type attributes
   */
  private Map<String, Map<String, GrouperObjectTypeObjectAttributes>> ancestorStemsTypeAttributes = new HashMap<String, Map<String, GrouperObjectTypeObjectAttributes>>();
  
  /**
   * all children stems of stems that have attribute change
   */
  private Map<String, Map<String, GrouperObjectTypeObjectAttributes>> childrenStemsTypeAttributes = new HashMap<String, Map<String, GrouperObjectTypeObjectAttributes>>();
  
  /**
   * all children groups of stems that have attribute change
   */
  private Map<String, Map<String, GrouperObjectTypeObjectAttributes>> childrenGroupsTypeAttributes = new HashMap<String, Map<String, GrouperObjectTypeObjectAttributes>>();

  /**
   * all stems (ids to names) with attribute type changes (does not include stem add events)
   */
  private Map<String, String> stemIdToNameMapThatNeedsChildrenRetrieved = new HashMap<String, String>();
  
  /**
   * all groups that have events and their attributes
   */
  private Map<String, Map<String, GrouperObjectTypeObjectAttributes>> groupsWithAttributesToProcess = new HashMap<String, Map<String, GrouperObjectTypeObjectAttributes>>();
  
  /**
   * all groups that have type attributes or have an ancestor that have type attributes
   */
  private Map<String, Map<String, GrouperObjectTypeObjectAttributes>> groupsWithOrWithoutAttributesToProcess = new HashMap<String, Map<String, GrouperObjectTypeObjectAttributes>>();
  
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
    
    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("objectType");
    
    gcGrouperSync.setSyncEngine(GcGrouperSync.OBJECT_TYPE_PROPAGATION);
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
      addFromOneMapOfMapsToAnother(childrenGroupsTypeAttributes, groupsWithOrWithoutAttributesToProcess);
      
      addFromOneMapOfMapsToAnother(foldersWithAttributesToProcess, foldersWithOrWithoutAttributesToProcess);
      addFromOneMapOfMapsToAnother(childrenStemsTypeAttributes, foldersWithOrWithoutAttributesToProcess);
      
      
      Set<String> typesToProcess = new HashSet<String>();
      
      typesToProcess.addAll(groupsWithOrWithoutAttributesToProcess.keySet());
      typesToProcess.addAll(foldersWithOrWithoutAttributesToProcess.keySet());
      typesToProcess.addAll(ancestorStemsTypeAttributes.keySet());
      
      
      if (typesToProcess.size() == 0) {
        return;
      }
      
      for(String ancestorType: typesToProcess) {
            
        if (!groupsWithOrWithoutAttributesToProcess.containsKey(ancestorType)) {
          groupsWithOrWithoutAttributesToProcess.put(ancestorType, new HashMap<String, GrouperObjectTypeObjectAttributes>()); 
        }
        
        if (!foldersWithOrWithoutAttributesToProcess.containsKey(ancestorType)) {
          foldersWithOrWithoutAttributesToProcess.put(ancestorType, new HashMap<String, GrouperObjectTypeObjectAttributes>()); 
        }
        
      }
      
      populateEventObjectsWhichDoNotHaveAttributes();
      
      populateChildrenWhichMayOrMayNotHaveAttributes();

      for (String typeName: typesToProcess) {
        
        Map<String, GrouperObjectTypeObjectAttributes> grouperObjectTypesFolderAttributes = foldersWithOrWithoutAttributesToProcess.get(typeName);
        Map<String, GrouperObjectTypeObjectAttributes> grouperObjectTypesGroupAttributes = groupsWithOrWithoutAttributesToProcess.get(typeName);
        
        Set<GrouperObjectTypeObjectAttributes> grouperObjectTypesAttributesToProcess = new HashSet<GrouperObjectTypeObjectAttributes>();
        if (grouperObjectTypesFolderAttributes != null) {        
          grouperObjectTypesAttributesToProcess.addAll(grouperObjectTypesFolderAttributes.values());
        }
        if (grouperObjectTypesGroupAttributes != null) {
          grouperObjectTypesAttributesToProcess.addAll(grouperObjectTypesGroupAttributes.values());
        }
        
        propagateObjectTypesAttributes(typeName, grouperObjectTypesAttributesToProcess, ancestorStemsTypeAttributes.get(typeName), debugMap);
        
      }
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
      
    }
    
  }
  
  private static void addFromOneMapOfMapsToAnother(Map<String, Map<String, GrouperObjectTypeObjectAttributes>> mapToAddFrom, Map<String, Map<String, GrouperObjectTypeObjectAttributes>> mapToAddTo) {
    
    for (String typeName: mapToAddFrom.keySet()) {
      Map<String, GrouperObjectTypeObjectAttributes> existingFolderNamesToAttributes = mapToAddTo.get(typeName);
      if (existingFolderNamesToAttributes == null) {
        existingFolderNamesToAttributes = new HashMap<String, GrouperObjectTypeObjectAttributes>();
        mapToAddTo.put(typeName, existingFolderNamesToAttributes);
      }
      
      existingFolderNamesToAttributes.putAll(mapToAddFrom.get(typeName));
      
    }
    
  }
  
  /**
   * 
   */
  public static void fullSyncLogic() {
    
    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("objectType");
    
    gcGrouperSync.setSyncEngine(GcGrouperSync.OBJECT_TYPE_PROPAGATION);
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
      
    
      Map<String, Map<String, GrouperObjectTypeObjectAttributes>> allFoldersOfInterestForTypes = retrieveAllFoldersOfInterestForTypes();
      Map<String, Map<String, GrouperObjectTypeObjectAttributes>> allGroupsOfInterestForTypes = retrieveAllGroupsOfInterestForTypes(allFoldersOfInterestForTypes);
      
      
      Set<String> typesToProcess = new HashSet<String>();
      typesToProcess.addAll(allFoldersOfInterestForTypes.keySet());
      typesToProcess.addAll(allGroupsOfInterestForTypes.keySet());
      
      
      for (String typeName: typesToProcess) {
        
        Map<String, GrouperObjectTypeObjectAttributes> grouperObjectTypesFolderAttributes = allFoldersOfInterestForTypes.get(typeName);
        Map<String, GrouperObjectTypeObjectAttributes> grouperObjectTypesGroupAttributes = allGroupsOfInterestForTypes.get(typeName);
        
        Set<GrouperObjectTypeObjectAttributes> grouperObjectTypesAttributesToProcess = new HashSet<GrouperObjectTypeObjectAttributes>();
        if (grouperObjectTypesFolderAttributes != null) {        
          grouperObjectTypesAttributesToProcess.addAll(grouperObjectTypesFolderAttributes.values());
        }
        if (grouperObjectTypesGroupAttributes != null) {
          grouperObjectTypesAttributesToProcess.addAll(grouperObjectTypesGroupAttributes.values());
        }
        
        propagateObjectTypesAttributes(typeName, grouperObjectTypesAttributesToProcess, grouperObjectTypesFolderAttributes, debugMap);
        
      }
    
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
      
    }
    
  }
  
  
  /**
   * @param typeName
   * @param grouperObjectTypesAttributesToProcess
   * @param grouperObjectTypesFolderAttributes
   */
  public static void propagateObjectTypesAttributes(String typeName, Set<GrouperObjectTypeObjectAttributes> grouperObjectTypesAttributesToProcess,
      Map<String, GrouperObjectTypeObjectAttributes> grouperObjectTypesFolderAttributes, Map<String, Object> debugMap) {
    
    AttributeDefName attributeDefNameBase = GrouperObjectTypesAttributeNames.retrieveAttributeDefNameBase();
    AttributeDefName attributeDefNameDirectAssign = GrouperObjectTypesAttributeNames.retrieveAttributeDefNameDirectAssignment();
        
    AttributeDefName attributeDefNameDataOwner = GrouperObjectTypesAttributeNames.retrieveAttributeDefNameDataOwner(); 
    AttributeDefName attributeDefNameMembersDescription = GrouperObjectTypesAttributeNames.retrieveAttributeDefNameMemberDescription();
    AttributeDefName attributeDefNameOwnerStemId = GrouperObjectTypesAttributeNames.retrieveAttributeDefNameOwnerStemId();
    AttributeDefName attributeDefNameServiceName = GrouperObjectTypesAttributeNames.retrieveAttributeDefNameServiceName();
    AttributeDefName attributeDefNameObjectTypeName = GrouperObjectTypesAttributeNames.retrieveAttributeDefNameTypeName();
    
    int objectTypesAttributesFoldersDeleted = 0;
    int objectTypesAttributesFoldersAddedOrUpdated = 0;
    int objectTypesAttributesGroupsDeleted = 0;
    int objectTypesAttributesGroupsAddedOrUpdated = 0;
    
    // get a map of all child -> parent, maybe this is cheaper than having to recalculate it multiple times per object
    Map<String, String> childToParent = new HashMap<String, String>();
    
    for (GrouperObjectTypeObjectAttributes grouperObjectTypesObjectAttribute : grouperObjectTypesAttributesToProcess) {
      
      String objectName = grouperObjectTypesObjectAttribute.getName();
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
    
    
    for (GrouperObjectTypeObjectAttributes grouperObjectTypesObjectAttribute : GrouperUtil.nonNull(grouperObjectTypesFolderAttributes).values()) {
      
      String objectName = grouperObjectTypesObjectAttribute.getName();
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
    for (GrouperObjectTypeObjectAttributes grouperObjectTypesObjectAttribute : grouperObjectTypesAttributesToProcess) {

      if ("true".equalsIgnoreCase(grouperObjectTypesObjectAttribute.getObjectTypeDirectAssign())) {
        continue;
      }
      
      GrouperObjectTypeObjectAttributes ancestorGrouperObjectTypesObjectAttribute = null;
      
      int depth = 0;
      String currObjectName = grouperObjectTypesObjectAttribute.getName();
      while (true) {
        depth++;
        GrouperUtil.assertion(depth < 1000, "Endless loop.");
        currObjectName = childToParent.get(currObjectName);
        if (currObjectName == null) {
          break;
        }
        
        GrouperObjectTypeObjectAttributes currGrouperObjectTypesObjectAttribute = GrouperUtil.nonNull(grouperObjectTypesFolderAttributes).get(currObjectName);
        if (currGrouperObjectTypesObjectAttribute != null && "true".equalsIgnoreCase(currGrouperObjectTypesObjectAttribute.getObjectTypeDirectAssign())) {
          
          ancestorGrouperObjectTypesObjectAttribute = currGrouperObjectTypesObjectAttribute;
          break;
        }
      }
      
      if (ancestorGrouperObjectTypesObjectAttribute == null) {
        if (!GrouperUtil.isEmpty(grouperObjectTypesObjectAttribute.getObjectTypeName())) {
          // delete the marker
          
          AttributeAssignable object;
          
          if (grouperObjectTypesObjectAttribute.isOwnedByGroup()) {
            object = GroupFinder.findByName(GrouperSession.staticGrouperSession(), grouperObjectTypesObjectAttribute.getName(), false);
          } else {
            object = StemFinder.findByName(GrouperSession.staticGrouperSession(), grouperObjectTypesObjectAttribute.getName(), false);
          }
          
          if (object == null) {
            // guess it was deleted?
            continue;
          }
          
          LOG.info("For type= " + typeName + " and group/stem= " + grouperObjectTypesObjectAttribute.getName() + " deleting marker attribute");
          
          if (StringUtils.isNotBlank(grouperObjectTypesObjectAttribute.getMarkerAttributeAssignId())) {
            
            AttributeAssign markerAttributeAssign = GrouperDAOFactory.getFactory().getAttributeAssign().findById(grouperObjectTypesObjectAttribute.getMarkerAttributeAssignId(), false);
            if (markerAttributeAssign != null) {
              markerAttributeAssign.delete();
            }
          }
          
          if (grouperObjectTypesObjectAttribute.isOwnedByGroup()) {
            objectTypesAttributesGroupsDeleted++;
            
          } else {
            objectTypesAttributesFoldersDeleted++;
          }
        }
      } else {
        
        String existingDirectAssign = grouperObjectTypesObjectAttribute.getObjectTypeDirectAssign();
        String existingDataOwner = grouperObjectTypesObjectAttribute.getObjectTypeDataOwner();
        String existingMemberDescription = grouperObjectTypesObjectAttribute.getObjectTypeMemberDescription();
        String existingOwnerStemId = grouperObjectTypesObjectAttribute.getObjectTypeOwnerStemId();
        String existingServiceName = grouperObjectTypesObjectAttribute.getObjectTypeServiceName();
        String existingTypeName = grouperObjectTypesObjectAttribute.getObjectTypeName();
        
        String actualDirectAssign = "false";
        String actualDataOwner = ancestorGrouperObjectTypesObjectAttribute.getObjectTypeDataOwner();
        String actualMemberDescription = ancestorGrouperObjectTypesObjectAttribute.getObjectTypeMemberDescription();
        String actualOwnerStemId = ancestorGrouperObjectTypesObjectAttribute.getId();
        String actualServiceName = ancestorGrouperObjectTypesObjectAttribute.getObjectTypeServiceName();
        String actualTypeName = ancestorGrouperObjectTypesObjectAttribute.getObjectTypeName();
        
        if (!GrouperUtil.equals(existingDirectAssign, actualDirectAssign) ||
            !GrouperUtil.equals(existingDataOwner, actualDataOwner) ||
            !GrouperUtil.equals(existingMemberDescription, actualMemberDescription) ||
            !GrouperUtil.equals(existingOwnerStemId, actualOwnerStemId) ||
            !GrouperUtil.equals(existingServiceName, actualServiceName) ||
            !GrouperUtil.equals(existingTypeName, actualTypeName)) {

          AttributeAssignable object;
          
          if (grouperObjectTypesObjectAttribute.isOwnedByGroup()) {
            object = GroupFinder.findByName(GrouperSession.staticGrouperSession(), grouperObjectTypesObjectAttribute.getName(), false);
          } else {
            object = StemFinder.findByName(GrouperSession.staticGrouperSession(), grouperObjectTypesObjectAttribute.getName(), false);
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
                  
                  if (StringUtils.isNotBlank(grouperObjectTypesObjectAttribute.getMarkerAttributeAssignId())) {
                    markerAssign = GrouperDAOFactory.getFactory().getAttributeAssign().findById(grouperObjectTypesObjectAttribute.getMarkerAttributeAssignId(), false);
                  }
                  
                  
                  if (markerAssign == null) {
                    markerAssign = object.getAttributeDelegate().internal_addAttributeHelper(null, attributeDefNameBase, false, null).getAttributeAssign();
                  }
                  
                  if (!GrouperUtil.equals(existingDirectAssign, actualDirectAssign)) {
                    LOG.info("For " + typeName + " and group/stem =" + grouperObjectTypesObjectAttribute.getName() + " updating objectTypesDirectAssign to: " + actualDirectAssign);
                    markerAssign.getAttributeValueDelegate().assignValue(attributeDefNameDirectAssign.getName(), actualDirectAssign);
                  }
                  
                  if (!GrouperUtil.equals(existingDataOwner, actualDataOwner)) {
                    LOG.info("For " + typeName + " and group/stemstemName=" + grouperObjectTypesObjectAttribute.getName() + " updating objectTypeDataOwner to: " + actualDataOwner);
                    
                    if (StringUtils.isBlank(actualDataOwner)) {
                      markerAssign.getAttributeDelegate().removeAttribute(attributeDefNameDataOwner);
                    } else {
                      markerAssign.getAttributeValueDelegate().assignValue(attributeDefNameDataOwner.getName(), actualDataOwner);
                    }
                  }
                  
                  if (!GrouperUtil.equals(existingMemberDescription, actualMemberDescription)) {
                    LOG.info("For " + typeName + " and group/stem=" + grouperObjectTypesObjectAttribute.getName() + " updating objectTypeMemberDescription to: " + actualMemberDescription);
                    if (StringUtils.isBlank(actualMemberDescription)) {
                      markerAssign.getAttributeDelegate().removeAttribute(attributeDefNameMembersDescription);
                    } else {
                      markerAssign.getAttributeValueDelegate().assignValue(attributeDefNameMembersDescription.getName(), actualMemberDescription);
                    }
                  }
                  
                  if (!GrouperUtil.equals(existingOwnerStemId, actualOwnerStemId)) {
                    LOG.info("For " + typeName + " and group/stem=" + grouperObjectTypesObjectAttribute.getName() + " updating objectTypeOwnerStemId to: " + actualOwnerStemId);
                    if (StringUtils.isBlank(actualOwnerStemId)) {
                      markerAssign.getAttributeDelegate().removeAttribute(attributeDefNameOwnerStemId);
                    } else {
                      markerAssign.getAttributeValueDelegate().assignValue(attributeDefNameOwnerStemId.getName(), actualOwnerStemId);
                    }
                  }
                  
                  if (!GrouperUtil.equals(existingServiceName, actualServiceName)) {
                    LOG.info("For " + typeName + " and group/stem=" + grouperObjectTypesObjectAttribute.getName() + " updating objectTypeServiceName to: " + actualServiceName);
                    if (StringUtils.isBlank(actualServiceName)) {
                      markerAssign.getAttributeDelegate().removeAttribute(attributeDefNameServiceName);
                    } else {
                      markerAssign.getAttributeValueDelegate().assignValue(attributeDefNameServiceName.getName(), actualServiceName);
                    }
                  }
                  
                  if (!GrouperUtil.equals(existingTypeName, actualTypeName)) {
                    LOG.info("For " + typeName + " and group/stem=" + grouperObjectTypesObjectAttribute.getName() + " updating objectTypeTypeName to: " + actualTypeName);
                    if (StringUtils.isBlank(actualTypeName)) {
                      markerAssign.getAttributeDelegate().removeAttribute(attributeDefNameObjectTypeName);
                    } else {
                      markerAssign.getAttributeValueDelegate().assignValue(attributeDefNameObjectTypeName.getName(), actualTypeName);
                    }
                  }
                  
                  return null;
                }
              });
          
          if (grouperObjectTypesObjectAttribute.isOwnedByGroup()) {
            objectTypesAttributesGroupsAddedOrUpdated++;
           
          } else {
            objectTypesAttributesFoldersAddedOrUpdated++;
          }
        }
      }
    }
    
    if (objectTypesAttributesGroupsAddedOrUpdated > 0) {
      debugMap.put(typeName+"_objectTypesAttributesGroupsAddedOrUpdated", objectTypesAttributesGroupsAddedOrUpdated);
    }
    
    if (objectTypesAttributesFoldersAddedOrUpdated > 0) {
      debugMap.put(typeName+"_objectTypesAttributesFoldersAddedOrUpdated", objectTypesAttributesFoldersAddedOrUpdated);
    }
    
    if (objectTypesAttributesGroupsDeleted > 0) {
      debugMap.put(typeName+"_objectTypesAttributesGroupsDeleted", objectTypesAttributesGroupsDeleted);
    }
    
    if (objectTypesAttributesFoldersDeleted > 0) {
      debugMap.put(typeName+"_objectTypesAttributesFoldersDeleted", objectTypesAttributesFoldersDeleted);
    }
  }

}



