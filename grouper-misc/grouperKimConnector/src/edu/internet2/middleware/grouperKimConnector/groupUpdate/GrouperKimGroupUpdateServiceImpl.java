/**
 * @author mchyzer
 * $Id: GrouperKimGroupUpdateServiceImpl.java,v 1.1 2009-12-13 06:57:50 mchyzer Exp $
 */
package edu.internet2.middleware.grouperKimConnector.groupUpdate;

import java.util.LinkedHashMap;
import java.util.Map;

import org.kuali.rice.kim.bo.group.dto.GroupInfo;
import org.kuali.rice.kim.service.GroupUpdateService;

import edu.internet2.middleware.grouperClient.api.GcAddMember;
import edu.internet2.middleware.grouperClient.api.GcDeleteMember;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.Log;
import edu.internet2.middleware.grouperKimConnector.util.GrouperKimUtils;


/**
 * Implementation of the group update service which maps to grouper
 * https://test.kuali.org/rice/rice-api-1.0-javadocs/index.html?org/kuali/rice/kim/service/GroupUpdateService.html
 */
public class GrouperKimGroupUpdateServiceImpl implements GroupUpdateService {

  /**
   * logger
   */
  private static final Log LOG = GrouperClientUtils.retrieveLog(GrouperKimGroupUpdateServiceImpl.class);
  
  /**
   * @see org.kuali.rice.kim.service.GroupUpdateService#addGroupToGroup(java.lang.String, java.lang.String)
   * boolean addGroupToGroup(java.lang.String childId,
   *                     java.lang.String parentId)
   *                     throws java.lang.UnsupportedOperationException
   *
   * Adds the group with the id supplied in childId as a member of the group with the id supplied in parentId. 
   */
  public boolean addGroupToGroup(String childId, String parentId)
      throws UnsupportedOperationException {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("childId", childId);
    debugMap.put("parentId", parentId);
    debugMap.put("operation", "addGroupToGroup");
    
    boolean hadException = false;
    
    try {
      WsAddMemberResults wsAddMemberResults = new GcAddMember().assignGroupUuid(parentId)
        .addSubjectLookup(new WsSubjectLookup(childId, "g:gsa", null)).execute();
      
      //we did one assignment, we have one result
      WsAddMemberResult wsAddMemberResult = wsAddMemberResults.getResults()[0];
      
      String resultCode = wsAddMemberResult.getResultMetadata().getResultCode();

      debugMap.put("resultCode", resultCode);

      //assignment was made
      if (GrouperClientUtils.equals("SUCCESS", resultCode)) {
        debugMap.put("returned", Boolean.TRUE);
        return true;
      }
      
      //assignment was already made
      if (GrouperClientUtils.equals("SUCCESS_ALREADY_EXISTED", resultCode)) {
        debugMap.put("returned", Boolean.FALSE);
        return false;
      }
      
      //we got a success, but we dont recognize the code... hmmm
      LOG.warn("Not expecting this resultCode: " + resultCode);
      
      //true or false?  who knows
      debugMap.put("returned", Boolean.FALSE);
      return false;
    } catch (RuntimeException re) {
      String errorPrefix = GrouperKimUtils.mapForLog(debugMap) + ", ";
      LOG.error(errorPrefix, re);
      GrouperClientUtils.injectInException(re, errorPrefix);
      throw re;
    } finally {
      if (LOG.isDebugEnabled() && !hadException) {
        LOG.debug(GrouperKimUtils.mapForLog(debugMap));
      }
    }
  }

  /**
   * boolean addPrincipalToGroup(java.lang.String principalId,
   *                          java.lang.String groupId)
   *                         throws java.lang.UnsupportedOperationException
   *
   *  Add the principal with the given principalId as a member of the group with the given groupId. 
   * @see org.kuali.rice.kim.service.GroupUpdateService#addPrincipalToGroup(java.lang.String, java.lang.String)
   */
  public boolean addPrincipalToGroup(String principalId, String groupId)
      throws UnsupportedOperationException {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("principalId", principalId);
    debugMap.put("groupId", groupId);
    debugMap.put("operation", "addPrincipalToGroup");
    
    boolean hadException = false;
    
    try {
      
      //lets see if there is a source to use
      String sourceId = GrouperClientUtils.propertiesValue("grouper.kim.plugin.subjectSourceId", false);
      sourceId = GrouperClientUtils.isBlank(sourceId) ? null : sourceId;
      
      WsAddMemberResults wsAddMemberResults = new GcAddMember().assignGroupUuid(groupId)
        .addSubjectLookup(new WsSubjectLookup(principalId, sourceId, null)).execute();
      
      //we did one assignment, we have one result
      WsAddMemberResult wsAddMemberResult = wsAddMemberResults.getResults()[0];
      
      String resultCode = wsAddMemberResult.getResultMetadata().getResultCode();

      debugMap.put("resultCode", resultCode);

      //assignment was made
      if (GrouperClientUtils.equals("SUCCESS", resultCode)) {
        debugMap.put("returned", Boolean.TRUE);
        return true;
      }
      
      //assignment was already made
      if (GrouperClientUtils.equals("SUCCESS_ALREADY_EXISTED", resultCode)) {
        debugMap.put("returned", Boolean.FALSE);
        return false;
      }
      
      //we got a success, but we dont recognize the code... hmmm
      LOG.warn("Not expecting this resultCode: " + resultCode);
      
      //true or false?  who knows
      debugMap.put("returned", Boolean.FALSE);
      return false;
    } catch (RuntimeException re) {
      String errorPrefix = GrouperKimUtils.mapForLog(debugMap) + ", ";
      LOG.error(errorPrefix, re);
      GrouperClientUtils.injectInException(re, errorPrefix);
      throw re;
    } finally {
      if (LOG.isDebugEnabled() && !hadException) {
        LOG.debug(GrouperKimUtils.mapForLog(debugMap));
      }
    }
  }

  /**
   * @see org.kuali.rice.kim.service.GroupUpdateService#createGroup(org.kuali.rice.kim.bo.group.dto.GroupInfo)
   */
  public GroupInfo createGroup(GroupInfo arg0) throws UnsupportedOperationException {
    return null;
  }

  /**
   * void removeAllGroupMembers(java.lang.String groupId)
   *                        throws java.lang.UnsupportedOperationException
   *
   * Removes all members from the group with the given groupId. 
   * @see org.kuali.rice.kim.service.GroupUpdateService#removeAllGroupMembers(java.lang.String)
   */
  public void removeAllGroupMembers(String groupId) throws UnsupportedOperationException {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("groupId", groupId);
    debugMap.put("operation", "removeAllGroupMembers");
    
    boolean hadException = false;
    
    try {
      
      //to remove all, we are adding nothing, and replacing the existing list with nothing, that removes them
      WsAddMemberResults wsAddMemberResults = new GcAddMember().assignGroupUuid(groupId)
        .assignReplaceAllExisting(true).execute();
      
      //here we dont have an individual result, just an overall result, and if we got this far, we are done
      debugMap.put("overallResultCode", wsAddMemberResults.getResultMetadata().getResultCode());

    } catch (RuntimeException re) {
      String errorPrefix = GrouperKimUtils.mapForLog(debugMap) + ", ";
      LOG.error(errorPrefix, re);
      GrouperClientUtils.injectInException(re, errorPrefix);
      throw re;
    } finally {
      if (LOG.isDebugEnabled() && !hadException) {
        LOG.debug(GrouperKimUtils.mapForLog(debugMap));
      }
    }
    
  }

  /**
   * boolean removeGroupFromGroup(java.lang.String childId,
   *                         java.lang.String parentId)
   *                         throws java.lang.UnsupportedOperationException
   *
   * Removes the group with the id supplied in childId from the group with the id supplied in parentId. 
   * @see org.kuali.rice.kim.service.GroupUpdateService#removeGroupFromGroup(java.lang.String, java.lang.String)
   */
  public boolean removeGroupFromGroup(String childId, String parentId)
      throws UnsupportedOperationException {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("childId", childId);
    debugMap.put("parentId", parentId);
    debugMap.put("operation", "removeGroupFromGroup");
    
    boolean hadException = false;
    
    try {
      WsDeleteMemberResults wsDeleteMemberResults = new GcDeleteMember()
        .addSubjectLookup(new WsSubjectLookup(childId, "g:gsa", null)).assignGroupUuid(parentId).execute();
      
      //we did one assignment, we have one result
      WsDeleteMemberResult wsDeleteMemberResult = wsDeleteMemberResults.getResults()[0];
      
      String resultCode = wsDeleteMemberResult.getResultMetadata().getResultCode();

      debugMap.put("resultCode", resultCode);

      //assignment was deleted
      if (GrouperClientUtils.equals("SUCCESS", resultCode) 
          || GrouperClientUtils.equals("SUCCESS_BUT_HAS_EFFECTIVE", resultCode)) {
        debugMap.put("returned", Boolean.TRUE);
        return true;
      }
      
      //immediate assignment didnt exist
      if (GrouperClientUtils.equals("SUCCESS_WASNT_IMMEDIATE", resultCode)
          || GrouperClientUtils.equals("SUCCESS_WASNT_IMMEDIATE_BUT_HAS_EFFECTIVE", resultCode)) {
        debugMap.put("returned", Boolean.FALSE);
        return false;
      }
      
      //we got a success, but we dont recognize the code... hmmm
      LOG.warn("Not expecting this resultCode: " + resultCode);
      
      //true or false?  who knows
      debugMap.put("returned", Boolean.FALSE);
      return false;
    } catch (RuntimeException re) {
      String errorPrefix = GrouperKimUtils.mapForLog(debugMap) + ", ";
      LOG.error(errorPrefix, re);
      GrouperClientUtils.injectInException(re, errorPrefix);
      throw re;
    } finally {
      if (LOG.isDebugEnabled() && !hadException) {
        LOG.debug(GrouperKimUtils.mapForLog(debugMap));
      }
    }
    
  }

  /**
   * boolean removePrincipalFromGroup(java.lang.String principalId,
   *                              java.lang.String groupId)
   *                              throws java.lang.UnsupportedOperationException
   *
   * Removes the member principal with the given principalId from the group with the given groupId. 
   * @see org.kuali.rice.kim.service.GroupUpdateService#removePrincipalFromGroup(java.lang.String, java.lang.String)
   */
  public boolean removePrincipalFromGroup(String principalId, String groupId)
      throws UnsupportedOperationException {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("principalId", principalId);
    debugMap.put("groupId", groupId);
    debugMap.put("operation", "removePrincipalFromGroup");
    
    boolean hadException = false;
    
    try {
      
      //lets see if there is a source to use
      String sourceId = GrouperClientUtils.propertiesValue("grouper.kim.plugin.subjectSourceId", false);
      sourceId = GrouperClientUtils.isBlank(sourceId) ? null : sourceId;
      
      WsDeleteMemberResults wsDeleteMemberResults = new GcDeleteMember()
        .addSubjectLookup(new WsSubjectLookup(principalId, sourceId, null)).assignGroupUuid(groupId).execute();
      
      //we did one assignment, we have one result
      WsDeleteMemberResult wsDeleteMemberResult = wsDeleteMemberResults.getResults()[0];
      
      String resultCode = wsDeleteMemberResult.getResultMetadata().getResultCode();

      debugMap.put("resultCode", resultCode);

      //assignment was deleted
      if (GrouperClientUtils.equals("SUCCESS", resultCode) 
          || GrouperClientUtils.equals("SUCCESS_BUT_HAS_EFFECTIVE", resultCode)) {
        debugMap.put("returned", Boolean.TRUE);
        return true;
      }
      
      //immediate assignment didnt exist
      if (GrouperClientUtils.equals("SUCCESS_WASNT_IMMEDIATE", resultCode)
          || GrouperClientUtils.equals("SUCCESS_WASNT_IMMEDIATE_BUT_HAS_EFFECTIVE", resultCode)) {
        debugMap.put("returned", Boolean.FALSE);
        return false;
      }
      
      //we got a success, but we dont recognize the code... hmmm
      LOG.warn("Not expecting this resultCode: " + resultCode);
      
      //true or false?  who knows
      debugMap.put("returned", Boolean.FALSE);
      return false;
    } catch (RuntimeException re) {
      String errorPrefix = GrouperKimUtils.mapForLog(debugMap) + ", ";
      LOG.error(errorPrefix, re);
      GrouperClientUtils.injectInException(re, errorPrefix);
      throw re;
    } finally {
      if (LOG.isDebugEnabled() && !hadException) {
        LOG.debug(GrouperKimUtils.mapForLog(debugMap));
      }
    }
  }

  /**
   * @see org.kuali.rice.kim.service.GroupUpdateService#updateGroup(java.lang.String, org.kuali.rice.kim.bo.group.dto.GroupInfo)
   */
  public GroupInfo updateGroup(String arg0, GroupInfo arg1)
      throws UnsupportedOperationException {
    return null;
  }

}
