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
package edu.internet2.middleware.tierApiAuthzServer.interfaces.beans.groups;

import java.util.Collection;

import edu.internet2.middleware.tierApiAuthzServer.contentType.AsasRestContentType;
import edu.internet2.middleware.tierApiAuthzServer.corebeans.AsasGroup;
import edu.internet2.middleware.tierApiAuthzServer.util.StandardApiServerUtils;
import edu.internet2.middleware.tierApiAuthzServer.version.TaasWsVersion;


/**
 * Group in authz standard api
 * @author mchyzer
 *
 */
public class AsasApiGroup {

  /**
   * convert the api beans to the transport beans
   * @param asasApiGroups
   * @return the api bean
   */
  public static AsasGroup[] convertToArray(Collection<AsasApiGroup> asasApiGroups) {
    
    if (asasApiGroups == null) {
      return null;
    }
    AsasGroup[] asasGroups = new AsasGroup[asasApiGroups.size()];
    
    int index = 0;
    for (AsasApiGroup asasApiGroup : asasApiGroups) {
      AsasGroup asasGroup = convertToAsasGroup(asasApiGroup);
      asasGroups[index] = asasGroup;
      index++;
    }
    
    return asasGroups;
  }

  /**
   * convert the api beans to the transport beans
   * @param asasApiGroups
   * @return the api bean
   */
  public static AsasGroup convertToAsasGroup(AsasApiGroup asasApiGroup) {
    if (asasApiGroup == null) {
      return null;
    }
    AsasGroup asasGroup = new AsasGroup();
    asasGroup.setDescription(asasApiGroup.getDescription());
    asasGroup.setDisplayName(asasApiGroup.getDisplayName());
    asasGroup.setId(asasApiGroup.getId());
    asasGroup.setName(asasApiGroup.getName());
    asasGroup.setStatus(asasApiGroup.getStatus());
    
    String groupUriBase = "/" 
        + TaasWsVersion.retrieveCurrentClientVersion().name() + "/groups/name" 
        + StandardApiServerUtils.escapeUrlEncode(":")
        + StandardApiServerUtils.escapeUrlEncode(asasApiGroup.getName());
    
    String groupUriSuffix = "." + AsasRestContentType.retrieveContentType().name();
    
    asasGroup.setUri(groupUriBase + groupUriSuffix);

    asasGroup.setParentFolderUri("/" 
        + TaasWsVersion.retrieveCurrentClientVersion().name() + "/folders/name" 
        + StandardApiServerUtils.escapeUrlEncode(":")
        + StandardApiServerUtils.escapeUrlEncode(StandardApiServerUtils.pathParentFolderName(asasApiGroup.getName()) + groupUriSuffix));
    
    asasGroup.setAdminsUri(groupUriBase + "/admins" + groupUriSuffix);
    asasGroup.setMembersUri(groupUriBase + "/members" + groupUriSuffix);
    asasGroup.setOptinsUri(groupUriBase + "/optins" + groupUriSuffix);
    asasGroup.setOptoutsUri(groupUriBase + "/optouts" + groupUriSuffix);
    asasGroup.setReadersUri(groupUriBase + "/readers" + groupUriSuffix);
    asasGroup.setUpdatersUri(groupUriBase + "/updaters" + groupUriSuffix);

    return asasGroup;
  }

  /** id of the group */
  private String id;
  
  /** name of group */
  private String name;
  
  /** display name of group */
  private String displayName;

  /** description of group */
  private String description;
  
  /** status: active or inactive */
  private String status;
  
  /**
   * id of the group
   * @return the id
   */
  public String getId() {
    return this.id;
  }

  
  /**
   * id of the group
   * @param id1 the id to set
   */
  public void setId(String id1) {
    this.id = id1;
  }

  
  /**
   * name of group
   * @return the name
   */
  public String getName() {
    return this.name;
  }

  
  /**
   * name of group
   * @param name1 the name to set
   */
  public void setName(String name1) {
    this.name = name1;
  }

  
  /**
   * display name of group
   * @return the displayName
   */
  public String getDisplayName() {
    return this.displayName;
  }

  
  /**
   * display name of group
   * @param displayName1 the displayName to set
   */
  public void setDisplayName(String displayName1) {
    this.displayName = displayName1;
  }

  
  /**
   * description of group
   * @return the description
   */
  public String getDescription() {
    return this.description;
  }

  
  /**
   * description of group
   * @param description1 the description to set
   */
  public void setDescription(String description1) {
    this.description = description1;
  }

  
  /**
   * status: active or inactive
   * @return the status
   */
  public String getStatus() {
    return this.status;
  }

  
  /**
   * status: active or inactive
   * @param status1 the status to set
   */
  public void setStatus(String status1) {
    this.status = status1;
  }
  
  
  
}
