/*******************************************************************************
 * Copyright 2016 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
/**
 * 
 */
package edu.internet2.middleware.tierApiAuthzServer.corebeans;

import edu.internet2.middleware.tierApiAuthzServer.contentType.AsasRestContentType;
import edu.internet2.middleware.tierApiAuthzServer.util.StandardApiServerUtils;


/**
 * Has member
 * @author mchyzer
 *
 */
public class AsasGroupsMemberSearchContainer extends AsasResponseBeanBase {


  /** display name of group */
  private String displayName;

  /** system name of group */
  private String tierSystemName;

  
  /**
   * @return the displayName
   */
  public String getDisplayName() {
    return this.displayName;
  }

  
  /**
   * @param displayName1 the displayName to set
   */
  public void setDisplayName(String displayName1) {
    this.displayName = displayName1;
  }

  
  /**
   * @return the tierSystemName
   */
  public String getTierSystemName() {
    return this.tierSystemName;
  }

  
  /**
   * @param tierSystemName1 the tierSystemName to set
   */
  public void setTierSystemName(String tierSystemName1) {
    this.tierSystemName = tierSystemName1;
  }

 
  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    AsasGroupsMemberSearchContainer asasGroupMemberContainer = new AsasGroupsMemberSearchContainer();
    
    asasGroupMemberContainer.setUserName("johnsmith");
    
    String string = StandardApiServerUtils.indent(AsasRestContentType.json.writeString(asasGroupMemberContainer), true);
    
    System.out.println(string);
    
    
  }

  /**
   * constructor
   */
  public AsasGroupsMemberSearchContainer() {
    super();
    this.getMeta().setResourceType("GroupMember");
  }

  /**
   * user id
   */
  private String id;
  
  /**
   * tierNetId
   */
  private String tierNetId;

  /**
   * userName
   */
  private String userName;

  /**
   * name
   */
  private AsasName name;
  
  
 
  
  /**
   * @see edu.internet2.middleware.tierApiAuthzServer.corebeans.AsasResponseBeanBase#scimify()
   */
  @Override
  public void scimify() {
    super.scimify();
    this.setTierNetId(null);
    this.setTierSystemName(null);
  }


  /**
   * @return the id
   */
  public String getId() {
    return this.id;
  }



  
  /**
   * @param id1 the id to set
   */
  public void setId(String id1) {
    this.id = id1;
  }



  
  /**
   * @return the tierNetId
   */
  public String getTierNetId() {
    return this.tierNetId;
  }



  
  /**
   * @param tierNetId1 the tierNetId to set
   */
  public void setTierNetId(String tierNetId1) {
    this.tierNetId = tierNetId1;
  }



  
  /**
   * @return the userName
   */
  public String getUserName() {
    return this.userName;
  }



  
  /**
   * @param userName1 the userName to set
   */
  public void setUserName(String userName1) {
    this.userName = userName1;
  }



  
  /**
   * @return the name
   */
  public AsasName getName() {
    return this.name;
  }



  
  /**
   * @param name1 the name to set
   */
  public void setName(AsasName name1) {
    this.name = name1;
  }

  
}
