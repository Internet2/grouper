/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperRemedy.digitalMarketplace;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import edu.internet2.middleware.grouperClient.api.GcGetMembers;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;
import edu.internet2.middleware.grouperRemedy.GrouperRemedyLog;
import edu.internet2.middleware.grouperRemedy.GrouperRemedyUtils;


/**
 *
 */
public class GrouperWsCommandsForDigitalMarketplace {

  /**
   * 
   */
  public GrouperWsCommandsForDigitalMarketplace() {
  }

  /**
   * cache of users in grouper
   */
  private static ExpirableCache<Boolean, Map<String, String[]>> retrieveGrouperUsersCache = null;
  
  /**
   * lazy load return the cache of users
   * @return the cache
   */
  private static ExpirableCache<Boolean, Map<String, String[]>> retrieveGrouperUsersCache() {
    if (retrieveGrouperUsersCache == null) {
      int expireMinutes = GrouperClientConfig.retrieveConfig().propertyValueInt("grouperRemedy.cacheGrouperUsersForMinutes", 60);
      retrieveGrouperUsersCache = new ExpirableCache(expireMinutes);
    }
    return retrieveGrouperUsersCache;
  }
  
  /**
   * return null if not configured to have such a group, otherwise the cached members of the group.  note, this is not a copy of the
   * cache, it can be edited
   * @return a map of remedyUserName to array of [subjectId, sourceId, remedySubjectAttributeValue, remedyUserName]
   */
  public static Map<String, String[]> retrieveGrouperUsers() {
    
    Map<String, String[]> result = retrieveGrouperUsersCache().get(Boolean.TRUE);
    if (result != null) {
      return result;
    }

    String remedyGrouperRequireGroupName = GrouperClientConfig.retrieveConfig().propertyValueString("grouperRemedyDigitalWorkplace.requireGroup");
    if (GrouperClientUtils.isBlank(remedyGrouperRequireGroupName)) {
      return null;
    }

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    long startTimeNanos = System.nanoTime();

    debugMap.put("method", "retrieveGrouperUsers");

    try {
      result = new HashMap<String, String[]>();
      
      GcGetMembers gcGetMembers = new GcGetMembers().addGroupName(remedyGrouperRequireGroupName);
      
      for (String sourceId : GrouperRemedyUtils.configSourcesForSubjects()) {
        gcGetMembers.addSourceId(sourceId);
      }
  
      String configSubjectAttributeForRemedyUsername = GrouperRemedyUtils.configSubjectAttributeForRemedyUsername();
      
      debugMap.put("configSubjectAttributeForRemedyUsername", configSubjectAttributeForRemedyUsername);
      
      if (GrouperClientUtils.equals("id", configSubjectAttributeForRemedyUsername)) {
  
        // in the utils method below this is what it expects
        configSubjectAttributeForRemedyUsername = "subject__id"; 
      } else {
        gcGetMembers.addSubjectAttributeName(configSubjectAttributeForRemedyUsername);
      }
      WsGetMembersResults wsGetMembersResults = gcGetMembers.execute();
      WsGetMembersResult wsGetMembersResult = wsGetMembersResults.getResults()[0];
      
      debugMap.put("resultSize", GrouperClientUtils.length(wsGetMembersResult.getWsSubjects()));

      String subjectSuffix = GrouperClientUtils.defaultIfBlank(GrouperClientConfig.retrieveConfig().propertyValueString("grouperRemedy.subjectIdSuffix"), "");

      debugMap.put("subjectSuffix", subjectSuffix);

      for (WsSubject wsSubject: GrouperClientUtils.nonNull(wsGetMembersResult.getWsSubjects(), WsSubject.class)) {
        String usernameValueWithoutSuffix = GrouperClientUtils.subjectAttributeValue(wsSubject, wsGetMembersResults.getSubjectAttributeNames(), configSubjectAttributeForRemedyUsername);
        String remedyUsername = usernameValueWithoutSuffix + subjectSuffix;
        String[] userArray = new String[]{wsSubject.getId(), wsSubject.getSourceId(), usernameValueWithoutSuffix, remedyUsername};
        result.put(remedyUsername, userArray);
      }
      
      retrieveGrouperUsersCache().put(Boolean.TRUE, result);
    } finally {
      GrouperRemedyLog.remedyLog(debugMap, startTimeNanos);
    }
    
    return result;
  }

}
