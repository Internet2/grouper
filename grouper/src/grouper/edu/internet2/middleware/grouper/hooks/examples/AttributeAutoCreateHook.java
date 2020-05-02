package edu.internet2.middleware.grouper.hooks.examples;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.serviceLifecycle.GrouperGracePeriodChangeLogConsumer;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.hooks.AttributeAssignHooks;
import edu.internet2.middleware.grouper.hooks.beans.HooksAttributeAssignBean;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHookType;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;


/**
 * this will only run after grouper is started up
 * @author mchyzer
 *
 */
public class AttributeAutoCreateHook extends AttributeAssignHooks {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(AttributeAutoCreateHook.class);

  public static void clearCache() {
    nameOfAttributeDefNameToAttributeDefName.clear();
    autoAssignAttributes.clear();
    uuidsOfAutoCreateAttributes.clear();
  }
  
  /**
   * 
   * @return the stem name
   */
  public static String attributeAutoCreateStemName() {
    return GrouperConfig.retrieveConfig().propertyValueString("grouper.rootStemForBuiltinObjects", "etc") + ":attribute:attributeAutoCreate";
  }

  public static final String GROUPER_ATTRIBUTE_AUTO_CREATE_MARKER_DEF = "grouperAttributeAutoCreateMarkerDef";

  public static final String GROUPER_ATTRIBUTE_AUTO_CREATE_MARKER = "grouperAttributeAutoCreateMarker";

  public static final String GROUPER_ATTRIBUTE_AUTO_CREATE_VALUE_DEF = "grouperAttributeAutoCreateValueDef";
  
  public static final String GROUPER_ATTRIBUTE_AUTO_CREATE_ATTR_IF_NAME = "grouperAttributeAutoCreateIfName";

  public static final String GROUPER_ATTRIBUTE_AUTO_CREATE_ATTR_THEN_NAMES_ON_ASSIGN = "grouperAttributeAutoCreateThenNamesOnAssign";
  
  public static void main(String[] args) {

  }

  /**
   * only register once
   */
  private static boolean registered = false;
  
  /**
   * see if this is configured in the grouper.properties, if so, register this hook
   */
  public static void registerHookIfNecessary() {
    
    if (registered) {
      return;
    }
    
    if (GrouperConfig.retrieveConfig().propertyValueBoolean("grouperHook.attributeAssign.autoAssign.autoRegister", true)) {
      //register this hook
      GrouperHooksUtils.addHookManual(GrouperHookType.ATTRIBUTE_ASSIGN.getPropertyFileKey(), 
          AttributeAutoCreateHook.class);
    }
    
    registered = true;

  }


  /**
   * uuid of marker attribute
   */
  private static ExpirableCache<Boolean, Set<String>> uuidsOfAutoCreateAttributes = new ExpirableCache<Boolean, Set<String>>(60);

  /**
   * group ids which have grace periods
   */
  private static ExpirableCache<String, AttributeDefName> nameOfAttributeDefNameToAttributeDefName = new ExpirableCache<String, AttributeDefName>(60);

  /**
   * id of attribute def name to the list to assign
   */
  private static ExpirableCache<Boolean, Map<String, List<AttributeDefName>>> autoAssignAttributes = new ExpirableCache<Boolean, Map<String, List<AttributeDefName>>>(5);

  public Map<String, List<AttributeDefName>> autoAssignAttributeIds() {

    Map<String, List<AttributeDefName>> result = autoAssignAttributes.get(Boolean.TRUE);
    if (result == null) {
      synchronized (GrouperGracePeriodChangeLogConsumer.class) {
        result = autoAssignAttributes.get(Boolean.TRUE);
        if (result == null) {
          
          result = new HashMap<String, List<AttributeDefName>>();
          
          String groupQuery = "select gaaagv_ifNames.value_string if_name, gaaagv_thenNames.value_string then_names "
              + "from grouper_aval_asn_asn_attrdef_v gaaagv_ifNames, grouper_aval_asn_asn_attrdef_v gaaagv_thenNames "
              + "where gaaagv_ifNames.id_of_attr_def_assigned_to = gaaagv_thenNames.id_of_attr_def_assigned_to  "
              + "and gaaagv_ifNames.attribute_def_name_name2 = '" + attributeAutoCreateStemName() + ":" + GROUPER_ATTRIBUTE_AUTO_CREATE_ATTR_IF_NAME + "' "
              + "and gaaagv_thenNames.attribute_def_name_name2 = '" + attributeAutoCreateStemName() + ":" + GROUPER_ATTRIBUTE_AUTO_CREATE_ATTR_THEN_NAMES_ON_ASSIGN + "'";
          
          List<Object[]> rows = new GcDbAccess().sql(groupQuery).selectList(Object[].class);
          for (Object[] row : GrouperUtil.nonNull(rows)) {

            String ifName = null;
            String thenNames = null;
            try {
              ifName = GrouperUtil.trim((String)row[0]);
              thenNames = (String)row[1];
              List<String> thenNamesList = GrouperUtil.splitTrimToList(thenNames, ",");
              
              //resolve ifName
              AttributeDefName attributeDefNameIf = nameOfAttributeDefNameToAttributeDefName.get(ifName);
              
              if (attributeDefNameIf == null) {
                  
                attributeDefNameIf = AttributeDefNameFinder.findByName(ifName, true);
                nameOfAttributeDefNameToAttributeDefName.put(ifName, attributeDefNameIf);
              }  
              List<AttributeDefName> thenAttributeDefNames = new ArrayList<AttributeDefName>();
              for (String thenName : thenNamesList) {
                AttributeDefName attributeDefNameThen = nameOfAttributeDefNameToAttributeDefName.get(thenName);
                
                if (attributeDefNameThen == null) {
                    
                  attributeDefNameThen = AttributeDefNameFinder.findByName(thenName, true);
                  nameOfAttributeDefNameToAttributeDefName.put(thenName, attributeDefNameThen);
                }
                thenAttributeDefNames.add(attributeDefNameThen);
              }
              
              result.put(attributeDefNameIf.getId(), thenAttributeDefNames);
            } catch (Exception e) {
              LOG.error("Error on if: '" + ifName + "', and then: '" + thenNames + "'", e);
            }
          }
          
          autoAssignAttributes.put(Boolean.TRUE, result);
          
        }
      }
    }
    return result;
  }


  @Override
  public void attributeAssignPostCommitInsert(final HooksContext hooksContext,
      final HooksAttributeAssignBean postCommitInsertBean) {
   
   if (!GrouperStartup.isFinishedStartupSuccessfully()) {
     return;
   }
    
   try {
     
     GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {

        AttributeAssign attributeAssign = postCommitInsertBean.getAttributeAssign(); 
        {
          //see if we are assigning things that affect this hook!
          Set<String> uuidsOfAutoCreate = uuidsOfAutoCreateAttributes.get(Boolean.TRUE);
          if (uuidsOfAutoCreate == null) {
            AttributeDefName marker = AttributeDefNameFinder.findByName(attributeAutoCreateStemName() + ":" + GROUPER_ATTRIBUTE_AUTO_CREATE_MARKER, false);
            AttributeDefName ifName = AttributeDefNameFinder.findByName(attributeAutoCreateStemName() + ":" + GROUPER_ATTRIBUTE_AUTO_CREATE_ATTR_IF_NAME, false);
            AttributeDefName thenNames = AttributeDefNameFinder.findByName(attributeAutoCreateStemName() + ":" + GROUPER_ATTRIBUTE_AUTO_CREATE_ATTR_THEN_NAMES_ON_ASSIGN, false);
            if (marker != null && ifName != null && thenNames != null) {
              uuidsOfAutoCreate = new HashSet<String>();
              uuidsOfAutoCreate.add(marker.getId());
              uuidsOfAutoCreate.add(ifName.getId());
              uuidsOfAutoCreate.add(thenNames.getId());
              uuidsOfAutoCreateAttributes.put(Boolean.TRUE, uuidsOfAutoCreate);
            }
          }
          // on a new assignment, clear the cache
          if (uuidsOfAutoCreate != null && uuidsOfAutoCreate.contains(attributeAssign.getAttributeDefNameId())) {
            //TODO notify the central database table to clear caches
            nameOfAttributeDefNameToAttributeDefName.clear();
          }
        }
        
        Map<String, List<AttributeDefName>> autoAssignAttributeIdsMap = autoAssignAttributeIds();
       
        if (autoAssignAttributeIdsMap.containsKey(attributeAssign.getAttributeDefNameId())) {
          List<AttributeDefName> attributeDefNames = autoAssignAttributeIdsMap.get(attributeAssign.getAttributeDefNameId());
          for (AttributeDefName attributeDefName : attributeDefNames) {
            if (!attributeAssign.getAttributeDelegate().hasAttribute(attributeDefName)) {
              attributeAssign.getAttributeDelegate().assignAttribute(attributeDefName);
            }
          }
        }

        return null;
      }
    });
     
   } catch (Exception e) {
     // lets not fail something
     LOG.error("error on attribute def name uuid: " + postCommitInsertBean.getAttributeAssign() == null ? null : postCommitInsertBean.getAttributeAssign().getAttributeDefNameId(), e);
   }
  }

}
