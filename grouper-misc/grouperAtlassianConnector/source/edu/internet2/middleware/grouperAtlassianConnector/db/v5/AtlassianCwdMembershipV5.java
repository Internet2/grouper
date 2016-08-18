/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperAtlassianConnector.db.v5;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdGroup;
import edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdMembership;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClient.util.HttpCallMethod;
import edu.internet2.middleware.grouperClient.util.HttpCallResponse;


/**
 *
 */
public class AtlassianCwdMembershipV5 implements AtlassianCwdMembership {

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
   * id col
   * @param id1 the id to set
   */
  public void setId(Long id1) {
    this.id = id1;
  }

  /**
   * parent id
   * @return the parentId
   */
  public Long getParentId() {
    return null;
  }
  
  /**
   * parent id col as string
   * @return the id
   */
  public String getParentIdString() {
    return null;
  }

  /**
   * parent id
   * @param parentId1 the parentId to set
   */
  public void setParentId(Long parentId1) {
  }

  /**
   * child_id col
   * @return the childId
   */
  public Long getChildId() {
    return null;
  }

  /**
   * parent_name col
   */
  private String parentName;

  /**
   * @return the parentName
   */
  public String getParentName() {
    return this.parentName;
  }

  /**
   * @param parentName1 the parentName to set
   */
  public void setParentName(String parentName1) {
    this.parentName = parentName1;
  }

  /**
   * lower_parent_name col
   * @param lowerParentName1 the lowerParentName to set
   */
  public void setLowerParentName(String lowerParentName1) {
  }

  /**
   * child_name col
   */
  private String childName;
  
  /**
   * child_name col
   * @return the childName
   */
  public String getChildName() {
    return this.childName;
  }
  
  /**
   * child_name col
   * @param childName1 the childName to set
   */
  public void setChildName(String childName1) {
    this.childName = childName1;
  }

  /**
   * lower_child_name col
   * @param lowerChildName1 the lowerChildName to set
   */
  public void setLowerChildName(String lowerChildName1) {
  }


  /**
   * child_id col
   * @param childId1 the childId to set
   */
  public void setChildId(Long childId1) {
  }



  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "AtlassianCwdMembershipV5 [id=" + this.id + ", directoryId=" 
        + ", parentName=" + this.parentName + ", childName="
        + this.childName + "]";
  }


  /**
   * note, you need to commit this (or at least flush) so the id is incremented
   */
  public void initNewObject() {

  }
  
  /**
   * get all memberships
   * @return the memberships
   */
  public static List<AtlassianCwdMembership> retrieveMemberships() {

    List<AtlassianCwdMembership> resultList = new ArrayList<AtlassianCwdMembership>();
    
    //get all groups.  then get all memberships from all groups.  dont think there is a better way to do this
    Collection<AtlassianCwdGroup> atlassianCwdGroupSet = AtlassianCwdGroupV5.retrieveGroups().values();
    
    for (AtlassianCwdGroup atlassianCwdGroup : atlassianCwdGroupSet) {
      
      // https://developer.atlassian.com/static/rest/jira/6.1.html#d2e2900
      // GET  /rest/api/2/group?groupname&expand
      // /rest/api/2/group?groupname=ben-financials&expand=users
      
      //  {  
      //    "name":"ben-financials",
      //    "self":"https://jira-test.apps.upenn.edu/jira/rest/api/2/group?groupname=ben-financials",
      //    "users":{  
      //       "size":84,
      //       "items":[  
      //          {  
      //             "self":"https://jira-test.apps.upenn.edu/jira/rest/api/2/user?username=apejcic",
      //             "name":"apejcic",
      //             "key":"apejcic",
      //             "emailAddress":"apejcic@upenn.edu",
      //             "avatarUrls":{  
      //                "16x16":"https://jira-test.apps.upenn.edu/jira/secure/useravatar?size=xsmall&avatarId=10112",
      //                "24x24":"https://jira-test.apps.upenn.edu/jira/secure/useravatar?size=small&avatarId=10112",
      //                "32x32":"https://jira-test.apps.upenn.edu/jira/secure/useravatar?size=medium&avatarId=10112",
      //                "48x48":"https://jira-test.apps.upenn.edu/jira/secure/useravatar?avatarId=10112"
      //             },
      //             "displayName":"Aleksandar Pejcic",
      //             "active":true,
      //             "timeZone":"US/Eastern"
      //          }
      //        ]
      //     }
      //  }
      HttpCallResponse httpCallResponse = GrouperClientUtils.httpCall(
          "/rest/api/2/group?groupname=" + GrouperClientUtils.escapeUrlEncode(atlassianCwdGroup.getGroupName()) + "&expand=users", 
          "jira", HttpCallMethod.GET, null);

      httpCallResponse.assertResponseCodes(200);

      JSONObject jsonObject = JSONObject.fromObject( httpCallResponse.getResponseBody() );

      JSONObject usersObject = jsonObject.getJSONObject("users");

      JSONArray jsonArray = usersObject.getJSONArray("items");
      
      for (int i=0;i<jsonArray.size();i++) {

        JSONObject user = jsonArray.getJSONObject(i);

        String userName = user.getString("name");
      
        AtlassianCwdMembershipV5 atlassianCwdMembershipV5 = new AtlassianCwdMembershipV5();
        atlassianCwdMembershipV5.setParentName(atlassianCwdGroup.getGroupName());
        atlassianCwdMembershipV5.setChildName(userName);

        resultList.add(atlassianCwdMembershipV5);
      }
      
    }
    
    return resultList;
  }

  /**
   * store this record insert or update
   */
  public void store() {
    if (GrouperClientUtils.isBlank(this.childName) || GrouperClientUtils.isBlank(this.parentName)) {
      throw new RuntimeException("userName or parentName is blank!");
    }
    
    //  https://developer.atlassian.com/static/rest/jira/6.1.html#d2e2934
    //  POST    (201, 400)  add membership
    //  /rest/api/2/group/user?groupname
    //  NOTE: how to specify username?????   should it be  /rest/api/2/group/user?groupname&username??????
    
    //TODO note Im having trouble getting this one to work...
    // I receive 415: Unsupported Media Type
    
    HttpCallResponse httpCallResponse = GrouperClientUtils.httpCall(
        "/rest/api/2/group/user?groupname=" + GrouperClientUtils.escapeUrlEncode(this.parentName) 
        + "&" + "username=" + GrouperClientUtils.escapeUrlEncode(this.childName), 
        "jira", HttpCallMethod.DELETE, null);

    httpCallResponse.assertResponseCodes(201, 400);

  }

  /**
   * delete this record
   */
  public void delete() {
    
    if (GrouperClientUtils.isBlank(this.childName) || GrouperClientUtils.isBlank(this.parentName)) {
      throw new RuntimeException("userName or parentName is blank!");
    }
    
    //  https://developer.atlassian.com/static/rest/jira/6.1.html#d2e2961
    //  DELETE    (200)  edit
    //  /rest/api/2/group/user?groupname&username

    //TODO note Im having trouble getting this one to work...
    // I receive 415: Unsupported Media Type

    HttpCallResponse httpCallResponse = GrouperClientUtils.httpCall(
        "/rest/api/2/group/user?groupname=" + GrouperClientUtils.escapeUrlEncode(this.parentName) 
        + "&" + "username=" + GrouperClientUtils.escapeUrlEncode(this.childName), 
        "jira", HttpCallMethod.DELETE, null);

    httpCallResponse.assertResponseCodes(200);

  }


  /**
   * @see edu.internet2.middleware.grouperClient.jdbc.GcSqlAssignPrimaryKey#gcSqlAssignNewPrimaryKeyForInsert()
   */
  public void gcSqlAssignNewPrimaryKeyForInsert() {
    throw new RuntimeException("This shouldnt be called");
  }


  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdMembership#setDirectoryId(java.lang.Long)
   */
  public void setDirectoryId(Long directoryId1) {
  }
 
}
