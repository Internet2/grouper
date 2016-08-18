/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperAtlassianConnector.db.v5;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdGroup;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClient.util.HttpCallMethod;
import edu.internet2.middleware.grouperClient.util.HttpCallResponse;


/**
 *
 */
public class AtlassianCwdGroupV5 implements AtlassianCwdGroup {

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
   * @return the id
   */
  public String getIdString() {
    return this.groupName;
  }
  
  /**
   * id col
   * @param id1 the id to set
   */
  public void setId(Long id1) {
    this.id = id1;
  }

  /**
   * group_name col
   */
  private String groupName;
  
  /**
   * group_name col
   * @return the groupName
   */
  public String getGroupName() {
    return this.groupName;
  }

  
  /**
   * group_name col
   * @param groupName1 the groupName to set
   */
  public void setGroupName(String groupName1) {
    this.groupName = groupName1;
  }



  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "AtlassianGroupbaseV5 [id=" + this.id + ", groupName=" + this.groupName + "]";
  }


  /**
   * note, you need to commit this (or at least flush) so the id is incremented
   */
  public void initNewObject() {

  }
  
  /**
   * get all groups
   * @return the groups or null by map of groupname to group
   */
  public static Map<String, AtlassianCwdGroup> retrieveGroups() {
    
    List<AtlassianCwdGroupV5> resultList = new ArrayList<AtlassianCwdGroupV5>();
    
    // {  
    //   "header":"Showing 15 of 15 matching groups",
    //   "total":15,
    //   "groups":[  
    //      {  
    //         "name":"ben-financials",
    //         "html":"ben-financials"
    //      }
    //    ]
    // }
    HttpCallResponse httpCallResponse = GrouperClientUtils.httpCall("/rest/api/2/groups/picker?maxResults=10000", "jira", HttpCallMethod.GET, null);

    httpCallResponse.assertResponseCodes(200);
    
    JSONObject jsonObject = JSONObject.fromObject( httpCallResponse.getResponseBody() );

    JSONArray jsonArray = jsonObject.getJSONArray("groups");
    
    for (int i=0;i<jsonArray.size();i++) {

      JSONObject group = jsonArray.getJSONObject(i);

      String groupName = group.getString("name");

      AtlassianCwdGroupV5 atlassianCwdGroupV5 = new AtlassianCwdGroupV5();
      atlassianCwdGroupV5.setGroupName(groupName);
      resultList.add(atlassianCwdGroupV5);
      
//      String urlSuffix = "/rest/api/2/group?groupname=" + GrouperClientUtils.escapeUrlEncode(groupName);
//      
//      //  {  
//      //    "name":"ben-financials",
//      //    "self":"https://jira-test.apps.upenn.edu/jira/rest/api/2/group?groupname=ben-financials",
//      //    "users":{  
//      //       "size":84,
//      //       "items":[  
//      //
//      //       ],
//      //       "max-results":50,
//      //       "start-index":0,
//      //       "end-index":0
//      //    },
//      //    "expand":"users"
//      // }
//      response = GrouperClientUtils.httpCall(urlSuffix, "jira", "GET", null, responseCode);
//
//      if (responseCode[0] != 200) {
//        throw new RuntimeException("Expected 200 but got " + responseCode + ", body: " + response);
//      }
//      
//      jsonObject = JSONObject.fromObject( response );

      

    }
    
    Map<String, AtlassianCwdGroup> resultMap = new LinkedHashMap<String, AtlassianCwdGroup>();
    for (AtlassianCwdGroupV5 atlassianCwdGroup : resultList) {
      resultMap.put(atlassianCwdGroup.getGroupName(), atlassianCwdGroup);
    }
    return resultMap;
  }

  /**
   * store this record insert or update
   */
  public void store() {

    if (GrouperClientUtils.isBlank(this.groupName)) {
      throw new RuntimeException("why is group name blank?");
    }
    
    String urlSuffix = "/rest/api/2/group?groupname=" + GrouperClientUtils.escapeUrlEncode(this.groupName);
    
    //  {  
    //    "name":"ben-financials",
    //    "self":"https://jira-test.apps.upenn.edu/jira/rest/api/2/group?groupname=ben-financials",
    //    "users":{  
    //       "size":84,
    //       "items":[  
    //
    //       ],
    //       "max-results":50,
    //       "start-index":0,
    //       "end-index":0
    //    },
    //    "expand":"users"
    // }
    HttpCallResponse httpCallResponse = GrouperClientUtils.httpCall(urlSuffix, "jira", HttpCallMethod.POST, null);
  
    httpCallResponse.assertResponseCodes(201, 400);

  }

  /**
   * delete this record
   */
  public void delete() {
    
    if (GrouperClientUtils.isBlank(this.groupName)) {
      throw new RuntimeException("why is group name blank?");
    }
    
    String urlSuffix = "/rest/api/2/group?groupname=" + GrouperClientUtils.escapeUrlEncode(this.groupName);
    
    //  {  
    //    "name":"ben-financials",
    //    "self":"https://jira-test.apps.upenn.edu/jira/rest/api/2/group?groupname=ben-financials",
    //    "users":{  
    //       "size":84,
    //       "items":[  
    //
    //       ],
    //       "max-results":50,
    //       "start-index":0,
    //       "end-index":0
    //    },
    //    "expand":"users"
    // }
    HttpCallResponse httpCallResponse = GrouperClientUtils.httpCall(urlSuffix, "jira", HttpCallMethod.DELETE, null);

    httpCallResponse.assertResponseCodes(200, 404);

  }


  /**
   * @see edu.internet2.middleware.grouperClient.jdbc.GcSqlAssignPrimaryKey#gcSqlAssignNewPrimaryKeyForInsert()
   */
  public void gcSqlAssignNewPrimaryKeyForInsert() {
    throw new RuntimeException("This should not be called");
  }
  

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((this.groupName == null) ? 0 : this.groupName.hashCode());
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
    AtlassianCwdGroupV5 other = (AtlassianCwdGroupV5) obj;
    if (this.groupName == null) {
      if (other.groupName != null)
        return false;
    } else if (!this.groupName.equals(other.groupName))
      return false;
    return true;
  }


  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdGroup#getActiveBoolean()
   */
  public Boolean getActiveBoolean() {
    return null;
  }


  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdGroup#setActiveBoolean(java.lang.Boolean)
   */
  public void setActiveBoolean(Boolean active1) {
  }


  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdGroup#getLocalBoolean()
   */
  public Boolean getLocalBoolean() {
    return null;
  }


  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdGroup#setLocalBoolean(java.lang.Boolean)
   */
  public void setLocalBoolean(Boolean local1) {
  }


  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdGroup#getUpdatedDate()
   */
  public Timestamp getUpdatedDate() {
    return null;
  }


  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdGroup#setUpdatedDate(java.sql.Timestamp)
   */
  public void setUpdatedDate(Timestamp updatedDate1) {
  }


  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdGroup#getDirectoryId()
   */
  public Long getDirectoryId() {
    return null;
  }


  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdGroup#setDirectoryId(java.lang.Long)
   */
  public void setDirectoryId(Long directoryId1) {
  }


  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdGroup#getLowerGroupName()
   */
  public String getLowerGroupName() {
    return null;
  }


  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdGroup#setLowerGroupName(java.lang.String)
   */
  public void setLowerGroupName(String lowerGroupName1) {
  }


  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdGroup#getCreatedDate()
   */
  public Timestamp getCreatedDate() {
    return null;
  }


  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdGroup#setCreatedDate(java.sql.Timestamp)
   */
  public void setCreatedDate(Timestamp createdDate1) {
  }


  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdGroup#setLowerDescription(java.lang.String)
   */
  public void setLowerDescription(String lowerDescription1) {
  }


  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdGroup#getDescription()
   */
  public String getDescription() {
    return null;
  }


  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdGroup#setDescription(java.lang.String)
   */
  public void setDescription(String description1) {
  }


  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdGroup#getGroupType()
   */
  public String getGroupType() {
    return null;
  }


  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdGroup#setGroupType(java.lang.String)
   */
  public void setGroupType(String groupType1) {
  }
  
}
