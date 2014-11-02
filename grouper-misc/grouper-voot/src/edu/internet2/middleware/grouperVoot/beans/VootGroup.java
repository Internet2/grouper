/*******************************************************************************
 * Copyright 2012 Internet2
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
 ******************************************************************************/

package edu.internet2.middleware.grouperVoot.beans;

import edu.internet2.middleware.grouper.Group;

/**
 * VOOT group bean that gets transformed to json.
 * 
 * @author mchyzer
 * @author <andrea.biancini@gmail.com>
 */
public class VootGroup {

  /** System name of group */
  private String id;

  /** Display name of group */
  private String name;

  /** Description of group */
  private String description;

  /** VOOT memebershib role being "manager", "admin" or "member". */
  private String voot_membership_role;
  
  /**
   * Default constructor. 
   */
  public VootGroup() {
    // Do nothing
  }

  /**
   * Construct with group.
   * 
   * @param group the group to be used to value fields.
   */
  public VootGroup(Group group) {
    this.setId(group.getName());
    this.setName(group.getDisplayName());
    this.setDescription(group.getDescription());
  }

  /**
   * Return the VOOT membership role (being either "manager", "admin" or "member").
   * 
   * @return the VOOT membership role.
   */
  public String getVoot_membership_role() {
    return this.voot_membership_role;
  }

  /**
   * Set the VOOT membership role (being either "manager", "admin" or "member").
   * 
   * @param the VOOT membership role.
   */
  public void setVoot_membership_role(String voot_membership_role1) {
    this.voot_membership_role = voot_membership_role1;
  }

  /**
   * Get system name (or unique ID) of the group.
   * 
   * @return system name of the group.
   */
  public String getId() {
    return this.id;
  }

  /**
   * Set system name (or unique ID) of the group.
   * 
   * @param id system name of the group.
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Get the display name of the group.
   * 
   * @return display name of the group.
   */
  public String getName() {
    return this.name;
  }

  /**
   * Set the display name of the group.
   * 
   * @param name display name of the group.
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Get description of the group.
   * 
   * @return description of the group.
   */
  public String getDescription() {
    return this.description;
  }

  /**
   * Set description of the group.
   * 
   * @param description description of the group.
   */
  public void setDescription(String description) {
    this.description = description;
  }
  
  /**
   * Enum to represnet group roles.
   * 
   * @author Andrea Biancini <andrea.biancini@gmail.com>
   */
  public enum GroupRoles {
      MANAGER ("manager"),
      ADMIN ("admin"),
      MEMBER ("member");

      private final String role;       

      private GroupRoles(String role) {
          this.role = role;
      }

      public String toString(){
         return role;
      }
  }
}

