package edu.internet2.middleware.grouper.userLifecycle;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignValueFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignValueFinder.AttributeAssignValueFinderResult;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;

public class UserLifecycleAttributeNames {
  
  /** attribute def cache */
  private static ExpirableCache<String, AttributeDef> attributeDefCache = new ExpirableCache<String, AttributeDef>(5);
  /** attribute def name cache */
  private static ExpirableCache<String, AttributeDefName> attributeDefNameCache = new ExpirableCache<String, AttributeDefName>(5);
  
  /**
   * marker def
   */
  public static final String USER_LIFECYCLE_POLICY_GROUP_MARKER_DEF = "userLifecyclePolicyGroupMarkerDef";
  
  /**
   * value def
   */
  public static final String USER_LIFECYCLE_POLICY_GROUP_VALUE_DEF = "userLifecyclePolicyGroupValueDef";

  
  public static final String USER_LIFECYCLE_POLICY_GROUP_MARKER = "userLifecyclePolicyGroupMarker";
  
  /**
   * config id of the policy on the group
   */
  public static final String USER_LIFECYCLE_POLICY_GROUP_VALUE_CONFIG_ID = "userLifecyclePolicyGroupValueConfigId";
  
  
  
  
  public static final String USER_LIFECYCLE_MSHIP_IN_FLIGHT_MARKER_DEF = "userLifecycleMshipInFlightMarkerDef";

  public static final String USER_LIFECYCLE_MSHIP_IN_FLIGHT_VALUE_DEF = "userLifecycleMshipInFlightValueDef";

  public static final String USER_LIFECYCLE_MSHIP_IN_FLIGHT_MARKER = "userLifecycleMshipInFlightMarker";
  
  public static final String USER_LIFECYCLE_MSHIP_IN_FLIGHT_LIFECYCLE_EVENT_ID = "userLifecycleMshipInFlightLifecycleEventId";

  public static final String USER_LIFECYCLE_MSHIP_IN_FLIGHT_ADDED_MICROS = "userLifecycleMshipInFlightAddedMicros";

  /**
   * value of this is going to be in the future from 1970 when the membership should expire
   */
  public static final String USER_LIFECYCLE_MSHIP_IN_FLIGHT_MICROS_EXPIRE = "userLifecycleMshipInFlightMicrosExpire"; 
  
  
  
  public static final String USER_LIFECYCLE_MSHIP_HISTORY_MARKER_DEF = "userLifecycleMshipHistoryMarkerDef";

  public static final String USER_LIFECYCLE_MSHIP_HISTORY_VALUE_DEF = "userLifecycleMshipHistoryValueDef";

  public static final String USER_LIFECYCLE_MSHIP_HISTORY_MARKER = "userLifecycleMshipHistoryMarker";
  
  public static final String USER_LIFECYCLE_MSHIP_HISTORY_LIFECYCLE_EVENT_ID = "userLifecycleMshipHistoryLifecycleEventId";

  public static final String USER_LIFECYCLE_MSHIP_HISTORY_ADDED_MICROS = "userLifecycleMshipHistoryAddedMicros";
  public static final String USER_LIFECYCLE_MSHIP_HISTORY_APPROVED_BY = "userLifecycleMshipHistoryApprovedBy";
  
  
  /**
   * 
   * @return root stem name where user lifecycle attribute definitions are stored
   */
  public static String userLifecycleStemName() {
    return GrouperConfig.retrieveConfig().propertyValueString("grouper.rootStemForBuiltinObjects", "etc") + ":attribute:userLifecycle";
  }
  
  /**
   * attribute value def assigned to group
   * @return the attribute def name
   */
  public static AttributeDef retrieveAttributeDefBaseDef() {
    
    AttributeDef attributeDef = retrieveAttributeDefFromDbOrCache(
        userLifecycleStemName() + ":" + USER_LIFECYCLE_POLICY_GROUP_MARKER_DEF);
  
    if (attributeDef == null) {
      throw new RuntimeException("Why cant userLifecyclePolicyGroupMarkerDef base def be found?");
    }
    return attributeDef;
  }

  /**
   * cache this.  note, not sure if its necessary
   * @param name 
   * @return attribute def
   */
  private static AttributeDef retrieveAttributeDefFromDbOrCache(final String name) {
    
    AttributeDef attributeDef = attributeDefCache.get(name);
  
    if (attributeDef == null) {
      
      attributeDef = (AttributeDef)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
  
        @Override
        public Object callback(GrouperSession grouperSession)
            throws GrouperSessionException {
          
          return AttributeDefFinder.findByName(name, false, new QueryOptions().secondLevelCache(false));
          
        }
        
      });
      if (attributeDef == null) {
        return null;
      }
      attributeDefCache.put(name, attributeDef);
    }
    
    return attributeDef;
  }
  
  public static AttributeAssign getAttributeAssignMatchingPolicyConfigId(Set<AttributeAssign> attributeAssigns, String policyConfigId) {
    
    for (AttributeAssign attributeAssign: GrouperUtil.nonNull(attributeAssigns)) {
      
      AttributeAssignValue attributeAssignValue = attributeAssign.getAttributeValueDelegate().retrieveAttributeAssignValue(userLifecycleStemName()+":"+USER_LIFECYCLE_POLICY_GROUP_VALUE_CONFIG_ID);
      if (attributeAssignValue == null || StringUtils.isBlank(attributeAssignValue.getValueString())) {
        return null;
      } 
      
      String policyConfigIdFromDb = attributeAssignValue.getValueString();
      if (StringUtils.equals(policyConfigIdFromDb, policyConfigId)) {
        return attributeAssign;
      }
    }
    return null;
  }
  
  public static String getExistingPolicyConfigId(Set<AttributeAssign> attributeAssigns) {
    
    for (AttributeAssign attributeAssign: GrouperUtil.nonNull(attributeAssigns)) {
      
      AttributeAssignValue attributeAssignValue = attributeAssign.getAttributeValueDelegate().retrieveAttributeAssignValue(userLifecycleStemName()+":"+USER_LIFECYCLE_POLICY_GROUP_VALUE_CONFIG_ID);
      if (attributeAssignValue == null || StringUtils.isBlank(attributeAssignValue.getValueString())) {
        return null;
      } 
      
      String policyConfigIdFromDb = attributeAssignValue.getValueString();
      return policyConfigIdFromDb;
    }
    return null;
  }

  /**
   * user lifecycle policy group config id
   * @return the attribute def name
   */
  public static AttributeDefName retrieveAttributeDefNameUserLifecyclePolicyGroupValueConfigId() {
    
    AttributeDefName attributeDefName = retrieveAttributeDefNameFromDbOrCache(
        userLifecycleStemName() + ":" + USER_LIFECYCLE_POLICY_GROUP_VALUE_CONFIG_ID);
  
    if (attributeDefName == null) {
      throw new RuntimeException("Why cant user lifecycle policy group config id attribute def name be found?");
    }
    return attributeDefName;
  
  }


  /**
   * attribute def name assigned to group
   * @return the attribute def name
   */
  public static AttributeDefName retrieveAttributeDefNameMarker() {
    
    AttributeDefName attributeDefName = retrieveAttributeDefNameFromDbOrCache(
        userLifecycleStemName() + ":" + USER_LIFECYCLE_POLICY_GROUP_MARKER);
  
    if (attributeDefName == null) {
      throw new RuntimeException("Why cant userLifecyclePolicyGroupMarker be found?");
    }
    return attributeDefName;
  }
  
  /**
   * attribute def name assigned
   * @return the attribute def name
   */
  public static AttributeDefName retrieveInFlightAttributeDefNameMarker() {
    
    AttributeDefName attributeDefName = retrieveAttributeDefNameFromDbOrCache(
        userLifecycleStemName() + ":" + USER_LIFECYCLE_MSHIP_IN_FLIGHT_MARKER);
  
    if (attributeDefName == null) {
      throw new RuntimeException("Why cant userLifecycleMshipInFlightMarker be found?");
    }
    return attributeDefName;
  }

  /**
   * cache this.  note, not sure if its necessary
   */
  private static AttributeDefName retrieveAttributeDefNameFromDbOrCache(final String name) {
    
    AttributeDefName attributeDefName = attributeDefNameCache.get(name);
  
    if (attributeDefName == null) {
      
      attributeDefName = (AttributeDefName)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
  
        @Override
        public Object callback(GrouperSession grouperSession)
            throws GrouperSessionException {
          
          return AttributeDefNameFinder.findByName(name, false, new QueryOptions().secondLevelCache(false));
          
        }
        
      });
      if (attributeDefName == null) {
        return null;
      }
      attributeDefNameCache.put(name, attributeDefName);
    }
    
    return attributeDefName;
  }

  /**
   * attribute value def assigned to stem or group
   * @return the attribute def name
   */
  public static AttributeDef retrieveAttributeDefNameValueDef() {
    
    AttributeDef attributeDef = retrieveAttributeDefFromDbOrCache(
        userLifecycleStemName() + ":" + USER_LIFECYCLE_POLICY_GROUP_VALUE_DEF);
  
    if (attributeDef == null) {
      throw new RuntimeException("Why cant userLifecyclePolicyGroupValueDef attribute value def be found?");
    }
    return attributeDef;
  }

  /**
   * cache settings
   */
  private static ExpirableCache<String, Map<String, Set<String>>> groupIdToAttributeDefNameUserLifecycleSettings = new ExpirableCache<String, Map<String, Set<String>>>(1);
  
  /**
   * cache these for a minute
   * @param group
   * @return attribute def names with sets of values
   */
  public static Map<String, Set<String>> retrieveAttributeValuesForGroup(Group group) {
    
    Map<String, Set<String>> result = groupIdToAttributeDefNameUserLifecycleSettings.get(group.getId());
    if (result != null) {
      return result;
    }
    
    synchronized (group.getId().intern()) {
      result = groupIdToAttributeDefNameUserLifecycleSettings.get(group.getId());
      if (result != null) {
        return result;
      }
      AttributeAssignValueFinderResult attributeAssignValueFinderResult = new AttributeAssignValueFinder()
          .addOwnerGroupOfAssignAssign(group)
          .addAttributeDefNameId(retrieveAttributeDefNameMarker().getId())
          .assignAttributeCheckReadOnAttributeDef(false)
          .findAttributeAssignValuesResult();
      
      Map<String, Map<String, Set<String>>> attributeAssignIdToattributeDefNameToValueSets = attributeAssignValueFinderResult.retrieveAssignIdsToAttributeDefNamesAndValueSetsStrings(group.getId());
    
      if (GrouperUtil.length(attributeAssignIdToattributeDefNameToValueSets) == 0) {
        result = new HashMap<String, Set<String>>();
      } else if (GrouperUtil.length(attributeAssignIdToattributeDefNameToValueSets) > 1) {
        throw new RuntimeException("Why is there more than 1 assignment for customUI? " + group.getName());
      } else {
    
        result = attributeAssignIdToattributeDefNameToValueSets.values().iterator().next();
      }
      
      groupIdToAttributeDefNameUserLifecycleSettings.put(group.getId(), result);
    }
    
    return result;
  }
}
