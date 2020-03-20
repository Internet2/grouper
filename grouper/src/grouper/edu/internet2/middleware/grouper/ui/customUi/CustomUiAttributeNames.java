/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.ui.customUi;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignValueFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignValueFinder.AttributeAssignValueFinderResult;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;


/**
 *
 */
public class CustomUiAttributeNames {

  /** attribute def cache */
  private static ExpirableCache<String, AttributeDef> attributeDefCache = new ExpirableCache<String, AttributeDef>(5);
  /** attribute def name cache */
  private static ExpirableCache<String, AttributeDefName> attributeDefNameCache = new ExpirableCache<String, AttributeDefName>(5);

  /**
   * marker def
   */
  public static final String CUSTOM_UI_DEF = "customUiDef";
  
  /**
   * value def
   */
  public static final String CUSTOM_UI_VALUE_DEF = "customUiValueDef";
  
  /**
   * marker name
   */
  public static final String CUSTOM_UI_MARKER = "customUi";
  
  /**
   * json of user queries
   */
  public static final String CUSTOM_UI_USER_QUERY_CONFIG_BEANS = "customUiUserQueryConfigBeans";

  /**
   * json of text config beans
   */
  public static final String CUSTOM_UI_TEXT_CONFIG_BEANS = "customUiTextConfigBeans";

  /**
   * 
   */
  public CustomUiAttributeNames() {
  }

  /**
   * 
   * @return custom ui stem name
   */
  public static String customUiStemName() {
    return GrouperConfig.retrieveConfig().propertyValueString("grouper.rootStemForBuiltinObjects", "etc") + ":attribute:customUi";
  }
  
  
  
  /**
   * attribute value def assigned to group
   * @return the attribute def name
   */
  public static AttributeDef retrieveAttributeDefBaseDef() {
    
    AttributeDef attributeDef = retrieveAttributeDefFromDbOrCache(
        customUiStemName() + ":" + CUSTOM_UI_DEF);
  
    if (attributeDef == null) {
      throw new RuntimeException("Why cant custom UI def base def be found?");
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

  /**
   * custom ui text config beans
   * @return the attribute def name
   */
  public static AttributeDefName retrieveAttributeDefNameTextConfigBeans() {
    
    AttributeDefName attributeDefName = retrieveAttributeDefNameFromDbOrCache(
        customUiStemName() + ":" + CUSTOM_UI_TEXT_CONFIG_BEANS);
  
    if (attributeDefName == null) {
      throw new RuntimeException("Why cant custom UI text config beans attribute def name be found?");
    }
    return attributeDefName;
  
  }

  /**
   * custom ui overall bean
   * @return the attribute def name
   */
  public static AttributeDefName retrieveAttributeDefNameUserQueryConfigBeans() {
    
    AttributeDefName attributeDefName = retrieveAttributeDefNameFromDbOrCache(
        customUiStemName() + ":" + CUSTOM_UI_USER_QUERY_CONFIG_BEANS);
  
    if (attributeDefName == null) {
      throw new RuntimeException("Why cant custom UI user query config beans attribute def name be found?");
    }
    return attributeDefName;
  
  }

  /**
   * attribute def name assigned to group
   * @return the attribute def name
   */
  public static AttributeDefName retrieveAttributeDefNameMarker() {
    
    AttributeDefName attributeDefName = retrieveAttributeDefNameFromDbOrCache(
         customUiStemName() + ":" + CUSTOM_UI_MARKER);
  
    if (attributeDefName == null) {
      throw new RuntimeException("Why cant custom ui marker be found?");
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
        customUiStemName() + ":" + CUSTOM_UI_VALUE_DEF);
  
    if (attributeDef == null) {
      throw new RuntimeException("Why cant custom UI def attribute value def be found?");
    }
    return attributeDef;
  }

  /**
   * cache settings
   */
  private static ExpirableCache<String, Map<String, Set<String>>> groupIdToAttributeDefNameCustomUiSettings = new ExpirableCache<String, Map<String, Set<String>>>(1);
  
  /**
   * cache these for a minute
   * @param group
   * @return attribute def names with sets of values
   */
  public static Map<String, Set<String>> retrieveAttributeValuesForGroup(Group group) {
    
    Map<String, Set<String>> result = groupIdToAttributeDefNameCustomUiSettings.get(group.getId());
    if (result != null) {
      return result;
    }
    
    synchronized (group.getId().intern()) {
      result = groupIdToAttributeDefNameCustomUiSettings.get(group.getId());
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
      
      groupIdToAttributeDefNameCustomUiSettings.put(group.getId(), result);
    }
    
    return result;
  }

  
}
