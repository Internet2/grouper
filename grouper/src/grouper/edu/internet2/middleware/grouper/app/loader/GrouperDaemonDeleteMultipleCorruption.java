/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.app.loader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignValueFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 *
 */
public class GrouperDaemonDeleteMultipleCorruption {

  /**
   * logger 
   */
  private static final Log LOG = GrouperUtil.getLog(GrouperDaemonDeleteMultipleCorruption.class);

  /**
   * 
   */
  public GrouperDaemonDeleteMultipleCorruption() {
  }

  /**
   * 
   * @param logOnly
   * @return summary
   */
  public static String fixValues(boolean logOnly) {
    return fixValues(null, logOnly, null);
  }

  /**
   * 
   * @param jobMessage
   * @param logOnly
   * @param error
   * @return summary
   */
  public static String fixValues(StringBuilder jobMessage, boolean logOnly, boolean[] error) {
    if (jobMessage == null) {
      jobMessage = new StringBuilder();
    }
    if (error == null || error.length < 1) {
      error = new boolean[]{false};
    }
    error[0]=false;
    StringBuilder response = new StringBuilder();
    boolean loggerInitted = GrouperLoaderLogger.initializeThreadLocalMap(GrouperDaemonDeleteOldRecords.LOG_LABEL);
    
    try {
      GrouperLoaderLogger.addLogEntry(GrouperDaemonDeleteOldRecords.LOG_LABEL, "fixValuesLogOnly", logOnly);
      
      //find value problems
      List<Object[]> listOfObjects = HibernateSession.byHqlStatic()
          .createQuery("SELECT distinct gaav, gaa, gadn, gad FROM AttributeAssignValue gaav, AttributeAssign gaa, "
              + " AttributeDefName gadn, AttributeDef gad WHERE gaav.attributeAssignId = gaa.id "
              + " AND gaa.attributeDefNameId = gadn.id AND gadn.attributeDefId = gad.id AND gad.multiValuedDb = 'F' "
              + " AND EXISTS (SELECT 1 FROM AttributeAssignValue gaav2 "
              + " WHERE gaav2.id != gaav.id AND gaav2.attributeAssignId = gaav.attributeAssignId)")
          .list(Object[].class);

      jobMessage.append("fixValuesCount: " + GrouperUtil.length(listOfObjects)/2);
      
      if (GrouperUtil.length(listOfObjects) == 0) {
        GrouperLoaderLogger.addLogEntry(GrouperDaemonDeleteOldRecords.LOG_LABEL, "fixValuesCount", 0);
        return "No duplicate values found";
      }
      
      Set<String> attributeAssignIds = new HashSet<String>();
      
      for (Object[] listOfObject: listOfObjects) {
        
        AttributeAssignValue attributeAssignValue = (AttributeAssignValue)listOfObject[0];
        
        attributeAssignIds.add(attributeAssignValue.getAttributeAssignId());
        
      }

      GrouperLoaderLogger.addLogEntry(GrouperDaemonDeleteOldRecords.LOG_LABEL, "fixValuesCount", GrouperUtil.length(attributeAssignIds));

      //loop through each attribute assign id
      for (String attributeAssignId : attributeAssignIds) {
        
        int indexWithNewestDate = -1;
        
        List<Object[]> listOfObjectsForAttributeAssignId = new ArrayList<Object[]>();
        
        for (Object[] listOfObject : listOfObjects) {
          
          AttributeAssignValue attributeAssignValue = (AttributeAssignValue)listOfObject[0];
          
          if (StringUtils.equals(attributeAssignId, attributeAssignValue.getAttributeAssignId())) {
            
            listOfObjectsForAttributeAssignId.add(listOfObject);
            
            if (indexWithNewestDate == -1) {
              indexWithNewestDate = 0;
            } else {
              Object[] listOfObjectWithPreviousNewestDate = listOfObjectsForAttributeAssignId.get(indexWithNewestDate);
              AttributeAssignValue attributeAssignValueWithPreviousNewestDate = (AttributeAssignValue)listOfObjectWithPreviousNewestDate[0];
              if (attributeAssignValue.getCreatedOnDb() != null && 
                  (attributeAssignValueWithPreviousNewestDate.getCreatedOnDb() == null ||
                      attributeAssignValue.getCreatedOnDb() > attributeAssignValueWithPreviousNewestDate.getCreatedOnDb())) {
                indexWithNewestDate = listOfObjectsForAttributeAssignId.size()-1;
              }
            }
          }
        }
        
        Object[] listOfObjectWithNewestDate = listOfObjectsForAttributeAssignId.get(indexWithNewestDate);
        
        AttributeAssignValue attributeAssignValueWithNewestDate = (AttributeAssignValue)listOfObjectWithNewestDate[0];
        @SuppressWarnings("unused")
        AttributeAssign attributeAssignWithNewestDate = (AttributeAssign)listOfObjectWithNewestDate[1];
        AttributeDefName attributeDefNameWithNewestDate = (AttributeDefName)listOfObjectWithNewestDate[2];
        @SuppressWarnings("unused")
        AttributeDef attributeDefWithNewestDate = (AttributeDef)listOfObjectWithNewestDate[3];
        
        //now we know the objects and which to keep
        for (Object[] listOfObject : listOfObjectsForAttributeAssignId) {

          AttributeAssignValue attributeAssignValueCurrent = (AttributeAssignValue)listOfObject[0];
          @SuppressWarnings("unused")
          AttributeAssign attributeAssignWithCurrent = (AttributeAssign)listOfObject[1];
          @SuppressWarnings("unused")
          AttributeDefName attributeDefNameWithCurrent = (AttributeDefName)listOfObject[2];
          @SuppressWarnings("unused")
          AttributeDef attributeDefWithCurrent = (AttributeDef)listOfObject[3];

          // we arent removing the newest one
          if (StringUtils.equals(attributeAssignValueCurrent.getId(), attributeAssignValueWithNewestDate.getId())) {
            continue;
          }
          
          String attributeAssignOwnerString = null;
          try {
            attributeAssignOwnerString = attributeAssignWithCurrent.retrieveAttributeAssignable().toString();
          } catch (Exception e) {
            //ignore
            LOG.error("error with attributeAssign.retrieveAttributeAssignable(): " + attributeAssignWithCurrent.getId(), e);
          }
          
          String query = "delete from grouper_attribute_assign_value where id = '" + attributeAssignValueCurrent.getId() + "'";
          String message = "Redundant grouper_attribute_assign_value " + (logOnly ? "needs to be" : "is" ) + " deleted: "
              + "[" + query + "],"
                  + " note most recent attribute assign value is: " + attributeAssignValueWithNewestDate.getId() + " "
                      + ", created date (of one to delete): " + attributeAssignValueCurrent.getCreatedOn() + ", most recent created date: " 
                      + attributeAssignValueWithNewestDate.getCreatedOn() 
                      + ", attributeDefName: " + attributeDefNameWithNewestDate.getName()
                      + ", value: '" + attributeAssignValueCurrent.valueString(false) + "'"
                      + ", attributeAssignId: " + attributeAssignId
                      + ", attributeAssignOwner: " + attributeAssignOwnerString;
          LOG.error(message );
          GrouperUtil.appendIfNotBlank(response, null, "\n", message, null);
          
          //ok, we either log or delete
          if (!logOnly) {
            
            try {
              attributeAssignValueCurrent.delete();
            } catch (Exception e) {
              LOG.error("Error deleting attribute assign value: " + attributeAssignValueCurrent, e);
              GrouperUtil.appendIfNotBlank(response, null, "\n", "ERROR deleting attribute assign value!", null);
              GrouperLoaderLogger.addLogEntry(GrouperDaemonDeleteOldRecords.LOG_LABEL, "cantDeleteValue_" + attributeAssignValueCurrent.getId(), 
                  true);
              error[0]=true;
            }
          }
        }
        
      }
      
      return response.toString();
    } finally {
      if (loggerInitted) {
        GrouperLoaderLogger.doTheLogging(GrouperDaemonDeleteOldRecords.LOG_LABEL);
      }
    }
    
  }
  /**
   * 
   * @param logOnly
   * @return summary
   */
  public static String fixAssigns(boolean logOnly) {
    return fixAssigns(null, logOnly, null);
  }

  /**
   * 
   * @param attributeAssign
   * @return the multikey
   */
  private static MultiKey multiKeyFromAttributeAssign(AttributeAssign attributeAssign) {
    //    + " ((gaa2.attributeAssignType = 'group' AND gaa2.ownerGroupId = gaa.ownerGroupId) "
    //    + " OR (gaa2.attributeAssignType = 'stem' AND gaa2.ownerStemId = gaa.ownerStemId) "
    //    + " OR (gaa2.attributeAssignType = 'member' AND gaa2.ownerMemberId = gaa.ownerMemberId) "
    //    + " OR (gaa2.attributeAssignType = 'attr_def' AND gaa2.ownerAttributeDefId = gaa.ownerAttributeDefId) "
    //    + " OR (gaa2.attributeAssignType = 'imm_mem' AND gaa2.ownerMembershipId = gaa.ownerMembershipId) "
    //    + " OR (gaa2.attributeAssignType = 'any_mem' AND gaa2.ownerMemberId = gaa.ownerMemberId AND gaa2.ownerGroupId = gaa.ownerGroupId) "
    //    + " OR (gaa2.attributeAssignType in ('any_mem_asgn', 'attr_def_asgn', 'group_asgn', 'imm_mem_asgn', 'mem_asgn', 'stem_asgn') AND gaa2.ownerAttributeAssignId = gaa.ownerAttributeAssignId) "
    List<Object> keys = new ArrayList<Object>();
    //    + " AND gaa2.attributeAssignActionId = gaa.attributeAssignActionId"
    keys.add(attributeAssign.getAttributeAssignActionId());
    //    + " AND gaa2.attributeDefNameId = gaa.attributeDefNameId"
    keys.add(attributeAssign.getAttributeDefNameId());
    //    + " AND gaa2.attributeAssignType = gaa.attributeAssignType"
    keys.add(attributeAssign.getAttributeAssignTypeDb());
    //    + " OR (gaa2.attributeAssignType = 'stem' AND gaa2.ownerStemId = gaa.ownerStemId) "
    keys.add(attributeAssign.getOwnerAttributeAssignId());
    keys.add(attributeAssign.getOwnerAttributeDefId());
    keys.add(attributeAssign.getOwnerGroupId());
    keys.add(attributeAssign.getOwnerMemberId());
    keys.add(attributeAssign.getOwnerMembershipId());
    keys.add(attributeAssign.getOwnerStemId());
    
    MultiKey multiKey = new MultiKey(keys.toArray());
    return multiKey;
  }
  
  /**
   * 
   * @param jobMessage
   * @param logOnly
   * @param error
   * @return summary
   */
  public static String fixAssigns(StringBuilder jobMessage, boolean logOnly, boolean[] error) {
    if (jobMessage == null) {
      jobMessage = new StringBuilder();
    }
    if (error == null || error.length < 1) {
      error = new boolean[]{false};
    }
    error[0]=false;
    StringBuilder response = new StringBuilder();
    boolean loggerInitted = GrouperLoaderLogger.initializeThreadLocalMap(GrouperDaemonDeleteOldRecords.LOG_LABEL);
    
    try {
      GrouperLoaderLogger.addLogEntry(GrouperDaemonDeleteOldRecords.LOG_LABEL, "fixAssignsLogOnly", logOnly);
      
      //find value problems
      List<Object[]> listOfObjects = HibernateSession.byHqlStatic()
          .createQuery("SELECT distinct gaa, gadn, gad FROM AttributeAssign gaa, "
              + " AttributeDefName gadn, AttributeDef gad WHERE "
              + " gaa.attributeDefNameId = gadn.id AND gadn.attributeDefId = gad.id AND gad.multiAssignableDb = 'F' "
              + " AND EXISTS (SELECT 1 FROM AttributeAssign gaa2 "
              + " WHERE gaa2.id != gaa.id"
              + " AND gaa2.attributeAssignActionId = gaa.attributeAssignActionId"
              + " AND gaa2.attributeDefNameId = gaa.attributeDefNameId"
              + " AND gaa2.attributeAssignTypeDb = gaa.attributeAssignTypeDb"
              + " AND ((gaa2.attributeAssignTypeDb = 'group' AND gaa2.ownerGroupId = gaa.ownerGroupId) "
              + " OR (gaa2.attributeAssignTypeDb = 'stem' AND gaa2.ownerStemId = gaa.ownerStemId) "
              + " OR (gaa2.attributeAssignTypeDb = 'member' AND gaa2.ownerMemberId = gaa.ownerMemberId) "
              + " OR (gaa2.attributeAssignTypeDb = 'attr_def' AND gaa2.ownerAttributeDefId = gaa.ownerAttributeDefId) "
              + " OR (gaa2.attributeAssignTypeDb = 'imm_mem' AND gaa2.ownerMembershipId = gaa.ownerMembershipId) "
              + " OR (gaa2.attributeAssignTypeDb = 'any_mem' AND gaa2.ownerMemberId = gaa.ownerMemberId AND gaa2.ownerGroupId = gaa.ownerGroupId) "
              + " OR (gaa2.attributeAssignTypeDb in ('any_mem_asgn', 'attr_def_asgn', 'group_asgn', 'imm_mem_asgn', 'mem_asgn', 'stem_asgn') AND gaa2.ownerAttributeAssignId = gaa.ownerAttributeAssignId) )"
              + ")")
          .list(Object[].class);

      if (GrouperUtil.length(listOfObjects) == 0) {
        jobMessage.append("fixAssignsCount: 0");
        GrouperLoaderLogger.addLogEntry(GrouperDaemonDeleteOldRecords.LOG_LABEL, "fixAssignsCount", 0);
        return "No duplicate assigns found";
      }
      
      Map<MultiKey, List<Object[]>> multikeyToAttributeAssigns = new HashMap<MultiKey, List<Object[]>>();
      
      for (Object[] listOfObject: listOfObjects) {
        
        AttributeAssign attributeAssign = (AttributeAssign)listOfObject[0];
        MultiKey multiKey = multiKeyFromAttributeAssign(attributeAssign);
        List<Object[]> listOfObjectsForMultikey = multikeyToAttributeAssigns.get(multiKey);
        if (listOfObjectsForMultikey == null) {
          listOfObjectsForMultikey = new ArrayList<Object[]>();
          multikeyToAttributeAssigns.put(multiKey, listOfObjectsForMultikey);
        }
        listOfObjectsForMultikey.add(listOfObject);
      }

      GrouperLoaderLogger.addLogEntry(GrouperDaemonDeleteOldRecords.LOG_LABEL, "fixAssignsCount", GrouperUtil.length(multikeyToAttributeAssigns));
      jobMessage.append("fixAssignsCount: " + GrouperUtil.length(multikeyToAttributeAssigns));

      //loop through each attribute assign id
      for (MultiKey attributeAssignOwner : multikeyToAttributeAssigns.keySet()) {
        
        int indexWithNewestDate = -1;
        
        List<Object[]> listOfObjectsForOwner = multikeyToAttributeAssigns.get(attributeAssignOwner);
        
        int index = 0;
        for (Object[] listOfObject : listOfObjectsForOwner) {
          
          AttributeAssign attributeAssign = (AttributeAssign)listOfObject[0];
          
          if (indexWithNewestDate == -1) {
            indexWithNewestDate = 0;
          } else {
            Object[] listOfObjectWithPreviousNewestDate = listOfObjectsForOwner.get(indexWithNewestDate);
            AttributeAssign attributeAssignWithPreviousNewestDate = (AttributeAssign)listOfObjectWithPreviousNewestDate[0];
            if (attributeAssign.getCreatedOnDb() != null && 
                (attributeAssignWithPreviousNewestDate.getCreatedOnDb() == null ||
                    attributeAssign.getCreatedOnDb() > attributeAssignWithPreviousNewestDate.getCreatedOnDb())) {
              indexWithNewestDate = index;
            }
          }
          
          index++;
        }
        
        Object[] listOfObjectWithNewestDate = listOfObjectsForOwner.get(indexWithNewestDate);
        
        AttributeAssign attributeAssignWithNewestDate = (AttributeAssign)listOfObjectWithNewestDate[0];
        AttributeDefName attributeDefNameWithNewestDate = (AttributeDefName)listOfObjectWithNewestDate[1];
        @SuppressWarnings("unused")
        AttributeDef attributeDefWithNewestDate = (AttributeDef)listOfObjectWithNewestDate[2];
        
        //now we know the objects and which to keep
        for (Object[] listOfObject : listOfObjectsForOwner) {

          AttributeAssign attributeAssignCurrent = (AttributeAssign)listOfObject[0];
          
          //is still there?
          if (GrouperDAOFactory.getFactory().getAttributeAssign().findById(attributeAssignCurrent.getId(), false, false) == null) {
            continue;
          }
          
          @SuppressWarnings("unused")
          AttributeDefName attributeDefNameCurrent = (AttributeDefName)listOfObject[1];
          @SuppressWarnings("unused")
          AttributeDef attributeDefWithCurrent = (AttributeDef)listOfObject[2];

          // we arent removing the newest one
          if (StringUtils.equals(attributeAssignCurrent.getId(), attributeAssignWithNewestDate.getId())) {
            continue;
          }
          
          try {
            logAttributeAssign(attributeAssignCurrent, attributeAssignWithNewestDate, logOnly, attributeDefNameWithNewestDate, response);
          } catch (Exception e) {
            LOG.error("Cant log attributeAssign: " + attributeAssignCurrent, e);
          }
          
          
          //ok, we either log or delete
          if (!logOnly) {
            
            try {
              attributeAssignCurrent.delete();
            } catch (Exception e) {
              LOG.error("Error deleting attribute assign: " + attributeAssignCurrent, e);
              GrouperUtil.appendIfNotBlank(response, null, "\n", "ERROR deleting attribute assign!", null);
              GrouperLoaderLogger.addLogEntry(GrouperDaemonDeleteOldRecords.LOG_LABEL, "cantDeleteAssign_" + attributeAssignCurrent.getId(), 
                  true);
              error[0]=true;
            }
          }
        }
        
      }
      
      return response.toString();
    } finally {
      if (loggerInitted) {
        GrouperLoaderLogger.doTheLogging(GrouperDaemonDeleteOldRecords.LOG_LABEL);
      }
    }
  }
    
  /**
   * 
   * @param logOnly
   * @param assignForValues
   * @param response
   */
  private static void logAttributeAssignValues(AttributeAssign assignForValues,  
      boolean logOnly, StringBuilder response) {

    for (AttributeAssignValue attributeAssignValue : new AttributeAssignValueFinder().addAttributeAssignId(assignForValues.getId()).findAttributeAssignValues()) {
      String query = "delete from grouper_attribute_assign_value where id = '" + attributeAssignValue.getId() + "'";
      String message = (logOnly ? "Needs to be" : "Is" ) + " deleted value: "
          + "[" + query + "], value: '" + attributeAssignValue.valueString(false) + "'";
      LOG.error(message );
      GrouperUtil.appendIfNotBlank(response, null, "\n", message, null);
      
    }
    
  }  
  
  
  /**
   * 
   * @param attributeAssignCurrent
   * @param attributeAssignWithNewestDate
   * @param logOnly
   * @param attributeDefNameWithNewestDate
   * @param response
   */
  private static void logAttributeAssign(AttributeAssign attributeAssignCurrent, AttributeAssign attributeAssignWithNewestDate, 
      boolean logOnly, AttributeDefName attributeDefNameWithNewestDate, StringBuilder response) {

    if (attributeDefNameWithNewestDate == null) {
      attributeDefNameWithNewestDate = AttributeDefNameFinder.findByIdAsRoot(attributeAssignCurrent.getAttributeDefNameId(), false);
    }
    
    if (!attributeAssignCurrent.getAttributeAssignType().isAssignmentOnAssignment()) {
      for (AttributeAssign attributeAssign : new AttributeAssignFinder().addOwnerAttributeAssignId(attributeAssignCurrent.getId()).findAttributeAssigns()) {
        logAttributeAssign(attributeAssign, null, logOnly, null, response);
      }
    }
    
    logAttributeAssignValues(attributeAssignCurrent, logOnly, response);
    
    String attributeAssignOwnerString = null;
    try {
      attributeAssignOwnerString = attributeAssignCurrent.retrieveAttributeAssignable().toString();
    } catch (Exception e) {
      //ignore
      LOG.error("error with attributeAssign.retrieveAttributeAssignable(): " + attributeAssignCurrent.getId(), e);
    }
  
    String query = "delete from grouper_attribute_assign where id = '" + attributeAssignCurrent.getId() + "'";
    String message = "Redundant grouper_attribute_assign " + (logOnly ? "needs to be" : "is" ) + " deleted: "
        + "[" + query + "],"
            + (attributeAssignWithNewestDate == null ? "" : " note most recent attribute assign is: " + attributeAssignWithNewestDate.getId())
                + ", created date (of one to delete): " + attributeAssignCurrent.getCreatedOn() + ", most recent created date: " 
                + attributeAssignWithNewestDate.getCreatedOn() 
            + (attributeDefNameWithNewestDate == null ? "" : ", attributeDefName: " + attributeDefNameWithNewestDate.getName())
                + ", attributeAssignOwner: " + attributeAssignOwnerString;
    LOG.error(message );
    GrouperUtil.appendIfNotBlank(response, null, "\n", message, null);
  }  

}
