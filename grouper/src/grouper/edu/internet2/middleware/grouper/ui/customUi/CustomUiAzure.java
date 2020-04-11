/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.ui.customUi;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.morphString.Morph;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectUtils;


/**
 * 
 * <pre>grouper.properties
 * 
 * grouper.azureConnector.myAzure.loginEndpoint = https://login.microsoftonline.com
 * grouper.azureConnector.myAzure.DirectoryID = 6c4dxxx0d
 * grouper.azureConnector.myAzure.client_id = fd805xxxxdfb
 * grouper.azureConnector.myAzure.client_secret = ******************
 * grouper.azureConnector.myAzure.resource = https://graph.microsoft.com
 * grouper.azureConnector.myAzure.graphEndpoint = https://graph.microsoft.com
 * grouper.azureConnector.myAzure.graphVersion = v1.0
 * grouper.azureConnector.myAzure.groupLookupAttribute = displayName
 * grouper.azureConnector.myAzure.groupLookupValueFormat = ${group.getName()}
 * </pre>
 */
public class CustomUiAzure extends CustomUiUserQueryBase {

  /**
   * cache of config key to expires on and encrypted bearer token
   */
  private static ExpirableCache<String, MultiKey> configKeyToExpiresOnAndBearerToken = new ExpirableCache<String, MultiKey>(60);
  
  /**
   * get bearer token for azure config id
   * @param configId
   * @return the bearer token
   */
  public String retrieveBearerTokenForAzureConfigId(String configId) {
    
    long startedNanos = System.nanoTime();
        
    MultiKey expiresOnAndEncryptedBearerToken = configKeyToExpiresOnAndBearerToken.get(configId);

    String encryptedBearerToken = null;
    if (expiresOnAndEncryptedBearerToken != null) {
      long expiresOnSeconds = (Long)expiresOnAndEncryptedBearerToken.getKey(0);
      encryptedBearerToken = (String)expiresOnAndEncryptedBearerToken.getKey(1);
      if (expiresOnSeconds * 1000 > System.currentTimeMillis()) {
        // use it
        this.debugMapPut("azureCachedAccessToken", true);
        return Morph.decrypt(encryptedBearerToken);
      }
    }
    try {
      // we need to get another one
      HttpClient httpClient = new HttpClient();
      String loginEndpoint = GrouperConfig.retrieveConfig().propertyValueStringRequired("grouper.azureConnector." + configId + ".loginEndpoint");
      String directoryId = GrouperConfig.retrieveConfig().propertyValueStringRequired("grouper.azureConnector." + configId + ".DirectoryID");
      final String url = loginEndpoint + "/" + directoryId + "/oauth2/token";
      PostMethod postMethod = new PostMethod(url);
      
      String clientId = GrouperConfig.retrieveConfig().propertyValueStringRequired("grouper.azureConnector." + configId + ".client_id");
      postMethod.addParameter("client_id", clientId);
  
      String clientSecret = GrouperConfig.retrieveConfig().propertyValueStringRequired("grouper.azureConnector." + configId + ".client_secret");
      clientSecret = Morph.decryptIfFile(clientSecret);
      postMethod.addParameter("client_secret", clientSecret);
  
      postMethod.addParameter("grant_type", "client_credentials");
  
      String resource = GrouperConfig.retrieveConfig().propertyValueStringRequired("grouper.azureConnector." + configId + ".resource");
      postMethod.addParameter("resource", resource);
  
      int code = -1;
      String json = null;
  
      try {
        code = httpClient.executeMethod(postMethod);
        // System.out.println(code + ", " + postMethod.getResponseBodyAsString());
        
        json = postMethod.getResponseBodyAsString();
      } catch (Exception e) {
        throw new RuntimeException("Error connecting to '" + url + "'", e);
      }
  
      if (code != 200) {
        throw new RuntimeException("Cant get access token from '" + url + "' " + json);
      }
      
      JSONObject jsonObject = JSONObject.fromObject(json);
      long expiresOn = GrouperUtil.longValue(jsonObject.getString("expires_on"));
      String accessToken = jsonObject.getString("access_token");
  
      expiresOnAndEncryptedBearerToken = new MultiKey(expiresOn, Morph.encrypt(accessToken));
      configKeyToExpiresOnAndBearerToken.put(configId, expiresOnAndEncryptedBearerToken);
      return accessToken;
    } catch (RuntimeException re) {
      
      this.debugMapPut("azureTokenError", GrouperUtil.getFullStackTrace(re));
      throw re;

    } finally {
      this.debugMapPut("azureTokenTookMillis", (System.nanoTime()-startedNanos)/1000000);
    }
  }

  /**
   * 
   * @param configId 
   * @param group
   * @param subject
   * @return true if membership
   */
  public boolean hasAzureMembershipByGroup(final String configId, final Group group, final Subject subject) {

    String azureGroupId = retrieveAzureGroupIdFromGroup(configId, group);
    
    return hasAzureMembershipByAzureGroupId(configId, azureGroupId, subject);
  }

  /**
   * 
   * @param configId 
   * @param azureGroupId
   * @param subject
   * @return true if membership
   */
  public boolean hasAzureMembershipByAzureGroupId(String configId, String azureGroupId, Subject subject) {

    long startedNanos = System.nanoTime();

    try {
      if (StringUtils.isBlank(azureGroupId)) {
        throw new RuntimeException("azure group id is blank");
      }
  
      String bearerToken = retrieveBearerTokenForAzureConfigId(configId);
      String graphEndpoint = GrouperConfig.retrieveConfig().propertyValueStringRequired("grouper.azureConnector." + configId + ".graphEndpoint");
  
      String graphVersion = GrouperConfig.retrieveConfig().propertyValueStringRequired("grouper.azureConnector." + configId + ".graphVersion");
      String subjectIdValueFormat = GrouperConfig.retrieveConfig().propertyValueStringRequired("grouper.azureConnector." + configId + ".subjectIdValueFormat");
  
      String requireSubjectAttribute = GrouperConfig.retrieveConfig().propertyValueStringRequired("grouper.azureConnector." + configId + ".requireSubjectAttribute");
      
      if (!StringUtils.isBlank(requireSubjectAttribute)) {
        if (StringUtils.isBlank(subject.getAttributeValue(requireSubjectAttribute))) {
          return false;
        }
      }
      
      String subjectId  = CustomUiUtil.substituteExpressionLanguage(subjectIdValueFormat, null, null, null, subject, null);
      
      if (StringUtils.isBlank(subjectId)) {
        throw new RuntimeException("Cant find subject lookup value: '" + subjectIdValueFormat + "', " + SubjectUtils.subjectToString(subject));
      }
      
      // //    GetMethod getMethod = new GetMethod("https://graph.microsoft.com/v1.0/users/smadan%40upenn.edu/memberOf?$filter=id%20eq%20'bf5c1726-4a6c-474f-b9d8-a58908c11cb8'");
      String url = graphEndpoint + "/" + graphVersion + "/users/" + GrouperUtil.escapeUrlEncode(subjectId) + "/memberOf?$filter=id%20eq%20'" + azureGroupId + "'";
      
      this.debugMapPut("azureMemUrl", url);
      
      GetMethod getMethod = new GetMethod(url);
      HttpClient httpClient = new HttpClient();
  
      getMethod.addRequestHeader("Content-Type", "application/json");
      getMethod.addRequestHeader("Authorization", "Bearer " + bearerToken);
      
      int code = -1;
      String json = null;
  
      try {
        code = httpClient.executeMethod(getMethod);
        // System.out.println(code + ", " + postMethod.getResponseBodyAsString());
        
        json = getMethod.getResponseBodyAsString();
      } catch (Exception e) {
        throw new RuntimeException("Error connecting to '" + url + "'", e);
      }
  
      if (code == 404) {
        return false;
      }
      
      if (code != 200) {
        throw new RuntimeException("Cant get group from '" + url + "' " + json);
      }
      
      JSONObject jsonObject = JSONObject.fromObject(json);
      JSONArray jsonArray = jsonObject.has("value") ? jsonObject.getJSONArray("value") : null;
      if (jsonArray != null && jsonArray.size() >= 1) {
        jsonObject = (JSONObject)jsonArray.get(0);
        return azureGroupId.equals(jsonObject.getString("id"));
      }
  
      return false;
    } catch (RuntimeException re) {
      
      this.debugMapPut("azureMemError", GrouperUtil.getFullStackTrace(re));
      throw re;

    } finally {
      this.debugMapPut("azureMemTookMillis", (System.nanoTime()-startedNanos)/1000000);
    }
    
  }
  
  /**
   * 
   * @param configId 
   * @param group
   * @return azureGroupId
   */
  public String retrieveAzureGroupIdFromGroup(String configId, Group group) {
    
    long startedNanos = System.nanoTime();

    try {
      String bearerToken = retrieveBearerTokenForAzureConfigId(configId);
      String graphEndpoint = GrouperConfig.retrieveConfig().propertyValueStringRequired("grouper.azureConnector." + configId + ".graphEndpoint");
  
      String graphVersion = GrouperConfig.retrieveConfig().propertyValueStringRequired("grouper.azureConnector." + configId + ".graphVersion");
      String groupLookupAttribute = GrouperConfig.retrieveConfig().propertyValueStringRequired("grouper.azureConnector." + configId + ".groupLookupAttribute");
      String groupLookupValueFormat = GrouperConfig.retrieveConfig().propertyValueStringRequired("grouper.azureConnector." + configId + ".groupLookupValueFormat");
      
      String groupLookupValue = CustomUiUtil.substituteExpressionLanguage(groupLookupValueFormat, group, null, null, null, null);
      
      if (StringUtils.isBlank(groupLookupValue)) {
        throw new RuntimeException("Cant find group lookup value: '" + groupLookupValueFormat + "', " + group);
      }
      
      String url = graphEndpoint + "/" + graphVersion + "/groups?$filter=" + groupLookupAttribute + "%20eq%20'" + groupLookupValue + "'";
      
      this.debugMapPut("azureGroupUrl", url);

      GetMethod getMethod = new GetMethod(url);
      HttpClient httpClient = new HttpClient();
  
      getMethod.addRequestHeader("Content-Type", "application/json");
      getMethod.addRequestHeader("Authorization", "Bearer " + bearerToken);
      
      int code = -1;
      String json = null;
  
      try {
        code = httpClient.executeMethod(getMethod);
        // System.out.println(code + ", " + postMethod.getResponseBodyAsString());
        
        json = getMethod.getResponseBodyAsString();
      } catch (Exception e) {
        throw new RuntimeException("Error connecting to '" + url + "'", e);
      }
  
      if (code != 200) {
        throw new RuntimeException("Cant get group from '" + url + "' " + json);
      }
      
      if (code != 200) {
        if (code == 404) {
          System.out.println("No");
        } else {
          System.out.println("Error! " + json);
        }
      } else {
        
        JSONObject jsonObject = JSONObject.fromObject(json);
        JSONArray jsonArray = jsonObject.has("value") ? jsonObject.getJSONArray("value") : null;
        if (jsonArray != null && jsonArray.size() == 1) {
          jsonObject = (JSONObject)jsonArray.get(0);
          return jsonObject.getString("id");
        }
      }
  
      throw new RuntimeException("Cant find group in azure: " + group + ", " + url);
    } catch (RuntimeException re) {
      
      this.debugMapPut("azureGroupError", GrouperUtil.getFullStackTrace(re));
      throw re;

    } finally {
      this.debugMapPut("azureGroupTookMillis", (System.nanoTime()-startedNanos)/1000000);
    }
  }
  
  public static void main(String[] args) throws Exception {
    GrouperStartup.startup();
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Subject subject1 = SubjectFinder.findById("10021368", true);
    Subject subject2 = SubjectFinder.findById("13228666", true);
    Subject subject3 = SubjectFinder.findById("10002177", true);
    Subject subject4 = SubjectFinder.findById("15251428", true);
    
    
    Group group = GroupFinder.findByName(grouperSession, "penn:isc:ait:apps:O365:twoStepProd:o365_two_step_prod", true);
    
    CustomUiAzure customUiAzure = new CustomUiAzure();
    
//    System.out.println("Azure id: " + retrieveAzureGroupIdFromGroup("pennAzure", group));
    System.out.println(customUiAzure.hasAzureMembershipByGroup("pennAzure", group, subject1));
    System.out.println(customUiAzure.hasAzureMembershipByGroup("pennAzure", group, subject2));
    System.out.println(customUiAzure.hasAzureMembershipByGroup("pennAzure", group, subject3));
    System.out.println(customUiAzure.hasAzureMembershipByGroup("pennAzure", group, subject4));
    
//    List<String> pennid = LdapSessionUtils.ldapSession().list(String.class, "oneProdAd", "DC=one,DC=upenn,DC=edu", LdapSearchScope.SUBTREE_SCOPE, 
//        "(&(objectclass=user)(employeeID=" + subject4.getId() + ")(memberof=CN=penn:isc:ait:apps:O365:twoStepProd:o365_two_step_prod,OU=Grouper,OU=365Groups,DC=one,DC=upenn,DC=edu))",
//        "employeeID");
//    
//    System.out.println("Ldap: " + (pennid != null && pennid.size() > 0 && !StringUtils.isBlank(pennid.get(0))));

//    List<String> pennid = LdapSessionUtils.ldapSession().list(String.class, "oneProdAd", "DC=one,DC=upenn,DC=edu", LdapSearchScope.SUBTREE_SCOPE, 
//        "(&(objectclass=user)(employeeID=" + subject4.getId() + ")(memberof=CN=penn:isc:ait:apps:O365:twoStepProd:o365_two_step_prod,OU=Grouper,OU=365Groups,DC=one,DC=upenn,DC=edu))",
//        "employeeID");

    
    GrouperSession.stopQuietly(grouperSession);
//    System.out.println(retrieveBearerTokenForAzureConfigId("pennAzure"));
    
//    HttpClient httpClient = new HttpClient();
//    PostMethod postMethod = new PostMethod("https://login.microsoftonline.com/6c4d949d-b91c-4c45-9aae-66d76443110d/oauth2/token");
//    postMethod.addParameter("client_id", "fd805aeb-265f-4f61-92b4-42b57fc14dfb");
//    postMethod.addParameter("client_secret", "***************");
////    postMethod.addParameter("redirect_uri", "urn:ietf:wg:oauth:2.0:oob");
//    postMethod.addParameter("grant_type", "client_credentials");
//    postMethod.addParameter("resource", "https://graph.microsoft.com");
////    postMethod.addParameter("state", "32");
//    int code = httpClient.executeMethod(postMethod);
//    // System.out.println(code + ", " + postMethod.getResponseBodyAsString());
//    
//    String json = postMethod.getResponseBodyAsString();
//    JSONObject jsonObject = JSONObject.fromObject(json);
//    long expiresOn = GrouperUtil.longValue(jsonObject.getString("expires_on"));
//    String accessToken = jsonObject.getString("access_token");
    
    //System.out.println(accessToken);
    
    // {"token_type":"Bearer","expires_in":"3599","ext_expires_in":"3599","expires_on":"1583377478","not_before":"1583373578",
    // "resource":"00000002-0000-0000-c000-000000000000",
    // "access_token":"eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsIng1dCI6IkhsQzBSMTJza3h****************dEO3M0tvFa3K5ARF4HIc88Nqovx6jmEf5Mzy3AZE
    // YbE-uC3WEQOMfFi5Q5g"}
    
//    HttpClient httpClient = new HttpClient();
//
//    GetMethod getMethod = new GetMethod("https://graph.microsoft.com/v1.0/groups?$filter=displayName%20eq%20'penn:isc:ait:apps:O365:twoStepProd:o365_two_step_prod'");
    
    //  200, {"@odata.context":"https://graph.microsoft.com/v1.0/$metadata#groups","value":[{"id":"bf5c1726-4a6c-474f-b9d8-a58908c11cb8",
    //  "displayName":"penn:isc:ait:apps:O365:twoStepProd:o365_two_step_prod","groupTypes":[],
    
    // 200, {"@odata.context":"https://graph.microsoft.com/v1.0/$metadata#groups/$entity","id":"bf5c1726-4a6c-474f-b9d8-a58908c11cb8","deletedDateTime":null,"classification":null,
    // "createdDateTime":"2018-11-17T19:23:51Z","creationOptions":[],"description":null,"displayName":"penn:isc:ait:apps:O365:twoStepProd:o365_two_step_prod",
    // "groupTypes":[],"isAssignableToRole":null,"mail":null,"mailEnabled":false,"mailNickname":"penn_isc_ait_apps_O365_twoStepProd_o365_two_step_prod"}
//    GetMethod getMethod = new GetMethod("https://graph.microsoft.com/v1.0/groups/bf5c1726-4a6c-474f-b9d8-a58908c11cb8/members?$top=10");
//    GetMethod getMethod = new GetMethod("https://graph.microsoft.com/beta/groups/bf5c1726-4a6c-474f-b9d8-a58908c11cb8/members");
//    GetMethod getMethod = new GetMethod("https://graph.microsoft.com/v1.0/users/2462cf6a-15c2-4ef3-84ed-3d1e65b60e6d/memberOf");
//    GetMethod getMethod = new GetMethod("https://graph.microsoft.com/v1.0/users/smadan%40upenn.edu/memberOf?$filter=id%20eq%20'bf5c1726-4a6c-474f-b9d8-a58908c11cb8'");
//    GetMethod getMethod = new GetMethod("https://graph.microsoft.com/v1.0/users/smadan%40upenn.edu");
//    getMethod.addRequestHeader("Content-Type", "application/json");
////    getMethod.addRequestHeader("Authorization", "Bearer eyJ0eXAiOiJKV1QiLCJub25jZSI6***********ho9LhQ");
//    getMethod.addRequestHeader("Authorization", "Bearer " + accessToken);
//    code = httpClient.executeMethod(getMethod);
//
//    System.out.println(code + ", " + getMethod.getResponseBodyAsString());
//
//    if (code != 200) {
//      if (code == 404) {
//        System.out.println("No");
//      } else {
//        System.out.println("Error! " + getMethod.getResponseBodyAsString());
//      }
//    } else {
//      
//      json = getMethod.getResponseBodyAsString();
//      jsonObject = JSONObject.fromObject(json);
//      JSONArray jsonArray = jsonObject.has("value") ? jsonObject.getJSONArray("value") : null;
//      if (jsonArray == null || jsonArray.size() == 0) {
//        System.out.println("No");
//      } else {
//        jsonObject = (JSONObject)jsonArray.get(0);
//        System.out.println(jsonObject.getString("id"));
//      }
//    }

    
    
//    if (code != 200) {
//      if (code == 404) {
//        System.out.println("No");
//      } else {
//        System.out.println("Error! " + getMethod.getResponseBodyAsString());
//      }
//    } else {
//      
//      json = getMethod.getResponseBodyAsString();
//      jsonObject = JSONObject.fromObject(json);
//      JSONArray jsonArray = jsonObject.has("value") ? jsonObject.getJSONArray("value") : null;
//      if (jsonArray == null || jsonArray.size() == 0) {
//        System.out.println("No");
//      } else {
//        jsonObject = (JSONObject)jsonArray.get(0);
//        if ("bf5c1726-4a6c-474f-b9d8-a58908c11cb8".equals(jsonObject.getString("id"))) {
//          System.out.println("Yes");
//        } else {
//          System.out.println("No");
//        }
//      }
//    }
    
    // {"@odata.context":"https://graph.microsoft.com/v1.0/$metadata#directoryObjects",
    // "value":[{"@odata.type":"#microsoft.graph.group",
    // "id":"bf5c1726-4a6c-474f-b9d8-a58908c11cb8",
    // "displayName":"penn:isc:ait:apps:O365:twoStepProd:o365_two_step_prod","groupTypes":[],"isAssignableToRole":null,"mail":null,


  }
  
  /**
   * 
   */
  public CustomUiAzure() {
  }

}
