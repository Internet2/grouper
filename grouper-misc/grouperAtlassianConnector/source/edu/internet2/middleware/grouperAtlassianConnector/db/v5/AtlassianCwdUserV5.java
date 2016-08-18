/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperAtlassianConnector.db.v5;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdMembership;
import edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdUser;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClient.util.HttpCallMethod;
import edu.internet2.middleware.grouperClient.util.HttpCallResponse;


/**
 *
 */
public class AtlassianCwdUserV5 implements AtlassianCwdUser {

  /**
   * id col
   */
  private Long id;
  
  /**
   * id col
   * @return the id
   */
  public Long getId() {
    return this.id;
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((this.userName == null) ? 0 : this.userName.hashCode());
    return result;
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    AtlassianCwdUserV5 other = (AtlassianCwdUserV5) obj;
    if (this.userName == null) {
      if (other.userName != null)
        return false;
    } else if (!this.userName.equals(other.userName))
      return false;
    return true;
  }

  /**
   * id col
   * @param id1 the id to set
   */
  public void setId(Long id1) {
    this.id = id1;
  }

  /**
   * user_name col
   */
  private String userName;
  
  /**
   * user_name col
   * @return the userName
   */
  public String getUserName() {
    return this.userName;
  }

  /**
   * display name
   */
  private String displayName;
  
  /**
   * email address
   */
  private String emailAddress;
  
  /**
   * user_name col
   * @param userName1 the userName to set
   */
  public void setUserName(String userName1) {
    this.userName = userName1;
  }

  /**
   * password hash field
   */
  private String passwordHash;

  /**
   * passwordHash col
   * @return the passwordHash
   */
  public String getPasswordHash() {
    return this.passwordHash;
  }

  
  /**
   * passwordHash col
   * @param passwordHash1 the passwordHash to set
   */
  public void setActive(String passwordHash1) {
    this.passwordHash = passwordHash1;
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "AtlassianUserBase [id=" + this.id + ", userName=" + this.userName + "]";
  }

  /**
   * note, you need to commit this (or at least flush) so the id is incremented
   */
  public void initNewObject() {

  }
  
  /**
   * get all users
   * @return the users or null by map of username to user
   */
  public static Map<String, AtlassianCwdUser> retrieveUsers() {
    
    List<AtlassianCwdUserV5> resultList = new ArrayList<AtlassianCwdUserV5>();
    Set<String> usernames = new HashSet<String>();
    
    //the only way to get all users is to get the ones with usernames that start with "a", "b", etc and list them all
    
    for (char theChar = 'a'; theChar <= 'z'; theChar++) {

      // https://jira-test.apps.upenn.edu/jirarest/rest/api/2/user/search?username=a
      
      // [  
      //   {  
      //      "self":"https://jira-test.apps.upenn.edu/jira/rest/api/2/user?username=jsmith",
      //      "key":"jsmith",
      //      "name":"jsmith",
      //      "emailAddress":"jsmith@upenn.edu",
      //      "avatarUrls":{  
      //         "16x16":"https://jira-test.apps.upenn.edu/jira/secure/useravatar?size=xsmall&avatarId=10112",
      //         "24x24":"https://jira-test.apps.upenn.edu/jira/secure/useravatar?size=small&avatarId=10112",
      //         "32x32":"https://jira-test.apps.upenn.edu/jira/secure/useravatar?size=medium&avatarId=10112",
      //         "48x48":"https://jira-test.apps.upenn.edu/jira/secure/useravatar?avatarId=10112"
      //      },
      //      "displayName":"John Smith",
      //      "active":true,
      //      "timeZone":"US/Eastern",
      //      "locale":"en_US"
      //   }
      // ] 
       
      HttpCallResponse httpCallResponse = GrouperClientUtils.httpCall(
          "/rest/api/2/user/search?username=" + theChar, "jira", HttpCallMethod.GET, null);

      httpCallResponse.assertResponseCodes(200);
      
      JSONArray jsonArray = JSONArray.fromObject( httpCallResponse.getResponseBody() );
      //jmullock
      for (int i=0;i<jsonArray.size();i++) {

        JSONObject user = jsonArray.getJSONObject(i);

        String key = user.getString("key");

        //can you get the same result in multiple letter calls?
        if (usernames.contains(key)) {
          continue;
        }
        
        usernames.add(key);
        
        String emailAddress = user.getString("emailAddress");
        String displayName = user.getString("displayName");
        boolean active = true;
        if (user.containsKey("active")) {
          active = user.getBoolean("active");
        }
        
        AtlassianCwdUserV5 atlassianCwdUserV5 = new AtlassianCwdUserV5();
        
        //we arent really using "active" at this point
        atlassianCwdUserV5.setUserName(key);
        atlassianCwdUserV5.setActiveBoolean(active);
        atlassianCwdUserV5.setDisplayName(displayName);
        atlassianCwdUserV5.setEmailAddress(emailAddress);
        resultList.add(atlassianCwdUserV5);
      }
      
    }

    //this doesnt always get them all
    //get all memberships
    for (AtlassianCwdMembership atlassianCwdMembership : AtlassianCwdMembershipV5.retrieveMemberships()) {
      
      String userName = atlassianCwdMembership.getChildName();
      
      //can you get the same result in multiple letter calls?
      if (usernames.contains(userName)) {
        continue;
      }
      
      usernames.add(userName);
      
      // https://jira-test.apps.upenn.edu/jirarest/rest/api/2/user/search?username=a
      
      //  {  
      //    "self":"https://jira-test.apps.upenn.edu/jira/rest/api/2/user?username=mchyzer",
      //    "key":"mchyzer",
      //    "name":"mchyzer",
      //    "emailAddress":"mchyzer@isc.upenn.edu",
      //    "avatarUrls":{  
      //       "16x16":"https://jira-test.apps.upenn.edu/jira/secure/useravatar?size=xsmall&avatarId=10112",
      //       "24x24":"https://jira-test.apps.upenn.edu/jira/secure/useravatar?size=small&avatarId=10112",
      //       "32x32":"https://jira-test.apps.upenn.edu/jira/secure/useravatar?size=medium&avatarId=10112",
      //       "48x48":"https://jira-test.apps.upenn.edu/jira/secure/useravatar?avatarId=10112"
      //    },
      //    "displayName":"Chris Hyzer",
      //    "active":true,
      //    "timeZone":"US/Eastern",
      //    "locale":"en_US",
      //    "groups":{  
      //       "size":6,
      //       "items":[  
      //
      //       ]
      //    },
      //    "expand":"groups"
      //  }


       
      HttpCallResponse httpCallResponse = GrouperClientUtils.httpCall(
          "/rest/api/2/user?username=" + userName, "jira", HttpCallMethod.GET, null);

      httpCallResponse.assertResponseCodes(200);
      
      JSONObject jsonObject = JSONObject.fromObject( httpCallResponse.getResponseBody() );

      String key = jsonObject.getString("key");

      String emailAddress = jsonObject.getString("emailAddress");
      String displayName = jsonObject.getString("displayName");
      boolean active = true;
      if (jsonObject.containsKey("active")) {
        active = jsonObject.getBoolean("active");
      }
        
      AtlassianCwdUserV5 atlassianCwdUserV5 = new AtlassianCwdUserV5();
      
      //we arent really using "active" at this point
      atlassianCwdUserV5.setUserName(key);
      atlassianCwdUserV5.setActiveBoolean(active);
      atlassianCwdUserV5.setDisplayName(displayName);
      atlassianCwdUserV5.setEmailAddress(emailAddress);
      resultList.add(atlassianCwdUserV5);
    }
    
    Map<String, AtlassianCwdUser> resultMap = new LinkedHashMap<String, AtlassianCwdUser>();
    for (AtlassianCwdUserV5 atlassianCwdUser : resultList) {
      resultMap.put(atlassianCwdUser.getUserName(), atlassianCwdUser);
    }
    return resultMap;
  }

  /**
   * store this record insert or update
   */
  public void store() {
    
    if (GrouperClientUtils.isBlank(this.userName)) {
      throw new RuntimeException("userName is blank!");
    }
    
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("name", this.userName);
    if (!GrouperClientUtils.isBlank(this.emailAddress)) {
      jsonObject.put("emailAddress", this.emailAddress);
    }
    if (!GrouperClientUtils.isBlank(this.displayName)) {
      jsonObject.put("displayName", this.displayName);
    }
    String requestBody = jsonObject.toString();
    
    if (this.existsInAtlassian()) {
      
      //  https://developer.atlassian.com/static/rest/jira/6.1.html#d2e3504
      //  PUT    (200)  edit
      //  {
      //    "name": "eddie",
      //    "emailAddress": "eddie@atlassian.com",
      //    "displayName": "Eddie of Atlassian"
      //  }

      HttpCallResponse httpCallResponse = GrouperClientUtils.httpCall(
          "/rest/api/2/user?key=" + GrouperClientUtils.escapeUrlEncode(this.userName), 
          "jira", HttpCallMethod.PUT, requestBody);

      httpCallResponse.assertResponseCodes(200);
      
    } else {
      
      //  https://developer.atlassian.com/static/rest/jira/6.1.html#d2e3542
      //  /rest/api/2/user?username&key
      //  POST   (201)  create
      //  {
      //    "name": "charlie",
      //    "emailAddress": "charlie@atlassian.com",
      //    "displayName": "Charlie of Atlassian"
      //  }

      HttpCallResponse httpCallResponse = GrouperClientUtils.httpCall(
          "/rest/api/2/user?key=" + GrouperClientUtils.escapeUrlEncode(this.userName), 
          "jira", HttpCallMethod.POST, requestBody);

      httpCallResponse.assertResponseCodes(201);
      
    }
  }

  /**
   * see if user exists in atlassian
   * @return true if user exists
   */
  public boolean existsInAtlassian() {
    
    if (GrouperClientUtils.isBlank(this.userName)) {
      throw new RuntimeException("No username set in object!");
    }
    
    //https://jira-test.apps.upenn.edu/jirarest/rest/api/2/user?username=mchyzer
    //https://developer.atlassian.com/static/rest/jira/6.1.html#d2e3477
    HttpCallResponse httpCallResponse = GrouperClientUtils.httpCall("/rest/api/2/user?username=" 
        + GrouperClientUtils.escapeUrlEncode(this.userName), "jira", HttpCallMethod.GET, null);

    httpCallResponse.assertResponseCodes(200, 404);
    
    return httpCallResponse.getHttpResponseCode() == 200;
      
  }
  
  /**
   * delete this record
   */
  public void delete() {
    
    if (GrouperClientUtils.isBlank(this.userName)) {
      throw new RuntimeException("userName is blank!");
    }
    
    //  https://developer.atlassian.com/static/rest/jira/6.1.html#d2e3574
    //  DELETE    (204, 404)  edit
    //  /rest/api/2/user?username&key
    HttpCallResponse httpCallResponse = GrouperClientUtils.httpCall(
        "/rest/api/2/user?key=" + GrouperClientUtils.escapeUrlEncode(this.userName), 
        "jira", HttpCallMethod.DELETE, null);

    httpCallResponse.assertResponseCodes(204, 404);

  }


  /**
   * @see edu.internet2.middleware.grouperClient.jdbc.GcSqlAssignPrimaryKey#gcSqlAssignNewPrimaryKeyForInsert()
   */
  public void gcSqlAssignNewPrimaryKeyForInsert() {
    throw new RuntimeException("This should not be called");
  }

  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdUser#getActiveBoolean()
   */
  public Boolean getActiveBoolean() {
    return null;
  }

  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdUser#setActiveBoolean(java.lang.Boolean)
   */
  public void setActiveBoolean(Boolean active1) {
  }

  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdUser#getUpdatedDate()
   */
  public Timestamp getUpdatedDate() {
    return null;
  }

  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdUser#setUpdatedDate(java.sql.Timestamp)
   */
  public void setUpdatedDate(Timestamp updatedDate1) {
  }

  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdUser#getDirectoryId()
   */
  public Long getDirectoryId() {
    return null;
  }

  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdUser#setDirectoryId(java.lang.Long)
   */
  public void setDirectoryId(Long directoryId1) {
  }

  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdUser#getLowerUserName()
   */
  public String getLowerUserName() {
    return null;
  }

  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdUser#setLowerUserName(java.lang.String)
   */
  public void setLowerUserName(String lowerUserName1) {
  }

  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdUser#getCreatedDate()
   */
  public Timestamp getCreatedDate() {
    return null;
  }

  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdUser#setCreatedDate(java.sql.Timestamp)
   */
  public void setCreatedDate(Timestamp createdDate1) {
  }

  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdUser#getLowerDisplayName()
   */
  public String getLowerDisplayName() {
    return null;
  }

  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdUser#setLowerDisplayName(java.lang.String)
   */
  public void setLowerDisplayName(String lowerDisplayName1) {
  }

  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdUser#getLowerEmailAddress()
   */
  public String getLowerEmailAddress() {
    return null;
  }

  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdUser#setLowerEmailAddress(java.lang.String)
   */
  public void setLowerEmailAddress(String lowerEmailAddress1) {
  }

  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdUser#getDisplayName()
   */
  public String getDisplayName() {
    return this.displayName;
  }

  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdUser#setDisplayName(java.lang.String)
   */
  public void setDisplayName(String displayName1) {
    this.displayName = displayName1;
  }

  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdUser#getEmailAddress()
   */
  public String getEmailAddress() {
    return this.emailAddress;
  }

  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdUser#setEmailAddress(java.lang.String)
   */
  public void setEmailAddress(String emailAddress1) {
    this.emailAddress = emailAddress1;
  }

  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdUser#getExternalId()
   */
  public String getExternalId() {
    return null;
  }

  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdUser#setExternalId(java.lang.String)
   */
  public void setExternalId(String externalId1) {
  }
  
}
