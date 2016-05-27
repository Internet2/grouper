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
 * Multiple groups
 * @author mchyzer
 *
 */
public class AsasUserContainer extends AsasResponseBeanBase {

  /**
   * constructor
   */
  public AsasUserContainer() {
    super();
    this.getMeta().setResourceType("User");
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



  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    AsasUserContainer asasUserContainer = new AsasUserContainer();
    asasUserContainer.setId("whatevs");
    
    String string = StandardApiServerUtils.indent(AsasRestContentType.json.writeString(asasUserContainer), true);
    
    System.out.println(string);
    
    
  }
  
  
  
}
