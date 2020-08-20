/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.app.zoom;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;


/**
 *
 */
public class CustomUiZoom {

  /**
   * 
   */
  public CustomUiZoom() {
  }

  /**
   * cche key is sourceId and subjectId
   * cache this for a minute so when one screen loads it does one operation
   */
  private static ExpirableCache<MultiKey, Map<String, Object>> zoomUserCache = new ExpirableCache<MultiKey, Map<String, Object>>(1);

  /**
   * 
   * @param configId 
   * @param sourceId
   * @param subjectId
   * @return map of attributes, userFound(boolean), accountEnabled(boolean),assignedPlans(Set), assignedPlansString(String comma space separated),mail,onPremisesImmutableId(String),
   *     onPremisesLastSyncDateTime(String),onPremisesSamAccountName(String),"
   *     proxyAddresses(Set),proxyAddressesString(String comma space separated),showInAddressList(boolean),userPrincipalName(String),userType(String),provisionedPlans(Set),
   *     provisionedPlansString(String comma space separated), summary(String)
   */
  public static Map<String, Object> customUiZoomUserAnalysis(String configId, String sourceId, String subjectId) {

    MultiKey multiKey = new MultiKey(configId, sourceId, subjectId);
    Map<String, Object> variableMap = zoomUserCache.get(multiKey);
    
    if (variableMap == null) {
      variableMap = customUiZoomUserAnalysisWs(configId, sourceId, subjectId);
      zoomUserCache.put(multiKey, variableMap);
    }
    return variableMap;
    
  }
  
  /**
   * 
   * @param configId 
   * @param sourceId
   * @param subjectId
   * @return map of attributes, hasEmail(boolean), userFound(boolean), userActive(boolean), id(string), first_name(string), last_name(string), 
   * email(string), type(int), role_name(string),  personal_meeting_url(string), timezone(string), verified(int), group_ids (array[string]), 
   * account_id(string), status(string e.g. active) or null if not found, summary(String)
   */
  public static Map<String, Object> customUiZoomUserAnalysisWs(String configId, String sourceId, String subjectId) {
    
    Map<String, Object> variableMap = new LinkedHashMap<String, Object>();
    
    String email = GrouperZoomLocalCommands.convertSourceIdSubjectIdToEmail(configId, sourceId, subjectId);
    
    variableMap.put("hasEmail", StringUtils.isNotBlank(email));
    variableMap.put("userActive", false);
    
    Map<String, Object> zoomUser = null;
    
    if (StringUtils.isNotBlank(email)) {

      zoomUser = GrouperZoomCommands.retrieveUser(configId, email);
      
    }
    variableMap.put("userFound", zoomUser != null);

    if (zoomUser != null) {
      
      variableMap.putAll(zoomUser);
      
      variableMap.put("userActive", "active".equals(zoomUser.get("status")));

      // convert groupIds to groupNames set
      String[] groupIdsArray = (String[])zoomUser.get("group_ids");
      
      Integer typeInt = (Integer)zoomUser.get("type");
      if (typeInt != null && 1==typeInt.intValue()) {
        variableMap.put("typeString", "basic");
      } else if (typeInt != null && 2==typeInt.intValue()) {
          variableMap.put("typeString", "licensed");
      } else if (typeInt != null && 3==typeInt.intValue()) {
        variableMap.put("typeString", "onprem");
      }
      
      Set<String> groupNamesSet = new HashSet<String>();

      variableMap.put("groupNamesSet", groupNamesSet);
      
      // get them all
      Map<String, Map<String, Object>> groupMap = GrouperZoomCommands.retrieveGroups(configId);

      for (int i=0;i<GrouperUtil.length(groupIdsArray); i++) {
        String groupId = groupIdsArray[i];
        Map<String, Object> zoomGroup = groupMap.get(groupId);
        String groupName = (String)zoomGroup.get("name");
        groupNamesSet.add(groupName);
      }
      
      variableMap.put("groupNamesSet", groupNamesSet);
      variableMap.put("groupNamesString", GrouperUtil.join(groupNamesSet.iterator(), ", ") );
               
      String masterAccountId = GrouperLoaderConfig.retrieveConfig().propertyValueString("zoom." + configId + ".masterAccountId");
        
      final boolean masterAccount = StringUtils.equals(masterAccountId, (String)zoomUser.get("account_id"));
      variableMap.put("masterAccount", masterAccount);

      if (!masterAccount) {
        Map<String, Map<String, Object>> accountMap = GrouperZoomCommands.retrieveAccounts(configId);
        if (accountMap != null) {
          
          Map<String, Object> account = accountMap.get(zoomUser.get("account_id"));
          if (account != null) {
            variableMap.put("account_name", account.get("account_name"));
          }
        }
      }
    }

    {
      StringBuilder summary = new StringBuilder();

      summary.append("hasEmail: ").append(variableMap.get("hasEmail"))
        .append(", userFound: ").append(variableMap.get("userFound"))
        .append(", userActive: ").append(variableMap.get("userActive"));
      
      if (zoomUser != null) {
        summary.append(", id: ").append(variableMap.get("id"))
          .append(", type: ").append(variableMap.get("type"))
          .append(", typeString: ").append(variableMap.get("typeString"))
          .append(", role_name: ").append(variableMap.get("role_name"))
          .append(", verified: ").append(variableMap.get("verified"))
          .append(", account_id: ").append(variableMap.get("account_id"))
          .append(", masterAccount: ").append(variableMap.get("masterAccount"))
          .append(", account_name: ").append(variableMap.get("account_name"))
          .append(", status: ").append(variableMap.get("status"))
          .append(", created_at: ").append(variableMap.get("created_at"))
          .append(", last_login_time: ").append(variableMap.get("last_login_time"))
          .append(", groupNames: ").append(variableMap.get("groupNamesString"));
        
      }
      variableMap.put("summary", summary.toString());
    }
    return variableMap;
  }

}
