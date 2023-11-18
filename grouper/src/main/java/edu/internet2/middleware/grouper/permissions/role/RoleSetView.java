/**
 * Copyright 2014 Internet2
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
 */
/**
 * 
 */
package edu.internet2.middleware.grouper.permissions.role;

import java.util.Set;

import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * map to the role set view for testing
 * @author mchyzer
 *
 */
public class RoleSetView {

  //*****  START GENERATED WITH GenerateFieldConstants.java *****//

  /** constant for field name for: depth */
  public static final String FIELD_DEPTH = "depth";

  /** constant for field name for: id */
  public static final String FIELD_ID = "id";

  /** constant for field name for: ifHasRoleId */
  public static final String FIELD_IF_HAS_ROLE_ID = "ifHasRoleId";

  /** constant for field name for: ifHasRoleName */
  public static final String FIELD_IF_HAS_ROLE_NAME = "ifHasRoleName";

  /** constant for field name for: parentId */
  public static final String FIELD_PARENT_ROLE_SET_ID = "parentRoleSetId";

  /** constant for field name for: thenHasRoleId */
  public static final String FIELD_THEN_HAS_ROLE_ID = "thenHasRoleId";

  /** constant for field name for: thenHasRoleName */
  public static final String FIELD_THEN_HAS_ROLE_NAME = "thenHasRoleName";

  /** constant for field name for: type */
  public static final String FIELD_TYPE = "type";

  /** constant for field name for: parentIfHasName */
  public static final String FIELD_PARENT_IF_HAS_NAME = "parentIfHasName";

  /** constant for field name for: parentThenHasName */
  public static final String FIELD_PARENT_THEN_HAS_NAME = "parentThenHasName";

  /**
   * fields which are included in db version
   */
  @SuppressWarnings("unused")
  private static final Set<String> DB_VERSION_FIELDS = GrouperUtil.toSet(
      FIELD_DEPTH, FIELD_ID, FIELD_IF_HAS_ROLE_ID, FIELD_IF_HAS_ROLE_NAME, 
      FIELD_PARENT_ROLE_SET_ID, 
      FIELD_THEN_HAS_ROLE_ID, FIELD_THEN_HAS_ROLE_NAME, FIELD_TYPE,
      FIELD_PARENT_IF_HAS_NAME, FIELD_PARENT_THEN_HAS_NAME);

  /**
   * fields which are included in clone method
   */
  @SuppressWarnings("unused")
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_DEPTH, FIELD_ID, FIELD_IF_HAS_ROLE_ID, FIELD_IF_HAS_ROLE_NAME, 
      FIELD_PARENT_ROLE_SET_ID, FIELD_THEN_HAS_ROLE_ID, 
      FIELD_THEN_HAS_ROLE_NAME, FIELD_TYPE, FIELD_PARENT_IF_HAS_NAME,
      FIELD_PARENT_THEN_HAS_NAME);

  //*****  END GENERATED WITH GenerateFieldConstants.java *****//

  /** name of the set roleibute def name */
  private String ifHasRoleName;
  
  /** name of the member role */
  private String thenHasRoleName;
  
  /** number of hops in the directed graph */
  private int depth;
  
  /** id of the set record */
  private String id;
  
  /** id of the set role */
  private String ifHasRoleId;
  
  /** id of the member role */
  private String thenHasRoleId;

  /** id of the role set record which is the immediate record this derives from
   * (everything but last hop) */
  private String parentRoleSetId;
  
  /** name of the role of the parent where if it has this name, then it has another name */
  private String parentIfHasName;
  
  /** name of the role of the parent where it has this name, if it have the ifName */
  private String parentThenHasName;
  
  /**
   * membership type -- self, immediate, or effective 
   */
  private RoleHierarchyType type = RoleHierarchyType.immediate;

  /**
   * @return membership type (immediate, effective, or self)
   */
  public RoleHierarchyType getType() {
    return this.type;
  }

  /**
   * membership type -- self, immediate, or effective 
   * get string value of type for hibernate
   * @return type
   */
  public String getTypeDb() {
    return this.type == null ? null : this.type.name();
  }

  /**
   * membership type -- self, immediate, or effective 
   * set group set assignment type
   * @param type1
   */
  public void setType(RoleHierarchyType type1) {
    this.type = type1;
  }

  /**
   * membership type -- self, immediate, or effective 
   * set group set assignment type
   * @param type1
   */
  public void setTypeDb(String type1) {
    this.type = RoleHierarchyType.valueOfIgnoreCase(type1, false);
  }

  
  /**
   * name of the set role
   * @return the ifHasRoleName
   */
  public String getIfHasRoleName() {
    return this.ifHasRoleName;
  }

  
  /**
   * name of the set role
   * @param ifHasRoleName1 the ifHasRoleName to set
   */
  public void setIfHasRoleName(String ifHasRoleName1) {
    this.ifHasRoleName = ifHasRoleName1;
  }

  
  /**
   * name of the member role
   * @return the thenHasRoleName
   */
  public String getThenHasRoleName() {
    return this.thenHasRoleName;
  }

  
  /**
   * name of the member role
   * @param thenHasRoleName1 the thenHasRoleName to set
   */
  public void setThenHasRoleName(String thenHasRoleName1) {
    this.thenHasRoleName = thenHasRoleName1;
  }

  
  /**
   * number of hops in the directed graph
   * @return the depth
   */
  public int getDepth() {
    return this.depth;
  }

  
  /**
   * number of hops in the directed graph
   * @param depth1 the depth to set
   */
  public void setDepth(int depth1) {
    this.depth = depth1;
  }

  
  /**
   * id of the set record
   * @return the id
   */
  public String getId() {
    return id;
  }

  
  /**
   * id of the set record
   * @param id1 the id to set
   */
  public void setId(String id1) {
    this.id = id1;
  }

  
  /**
   * id of the set role
   * @return the ifHasRoleId
   */
  public String getIfHasRoleId() {
    return this.ifHasRoleId;
  }

  
  /**
   * id of the set role
   * @param ifHasRoleId1 the ifHasRoleId to set
   */
  public void setIfHasRoleId(String ifHasRoleId1) {
    this.ifHasRoleId = ifHasRoleId1;
  }

  
  /**
   * @return the thenHasRoleId
   */
  public String getThenHasRoleId() {
    return thenHasRoleId;
  }

  
  /**
   * id of the member role
   * @param thenHasRoleId1 the thenHasRoleId to set
   */
  public void setThenHasRoleId(String thenHasRoleId1) {
    this.thenHasRoleId = thenHasRoleId1;
  }

  
  /**
   * id of the role set record which is the immediate record this derives from
   * (everything but last hop)
   * @return the parentRoleSetId
   */
  public String getParentRoleSetId() {
    return this.parentRoleSetId;
  }

  
  /**
   * id of the role set record which is the immediate record this derives from
   * (everything but last hop)
   * @param parentRoleSetId1 the parentRoleSetId to set
   */
  public void setParentRoleSetId(String parentRoleSetId1) {
    this.parentRoleSetId = parentRoleSetId1;
  }

  
  /**
   * name of the attribute of the parent where if it has this name, then it has another name
   * @return the parentIfHasName
   */
  public String getParentIfHasName() {
    return this.parentIfHasName;
  }

  
  /**
   * name of the attribute of the parent where if it has this name, then it has another name
   * @param parentIfHasName1 the parentIfHasName to set
   */
  public void setParentIfHasName(String parentIfHasName1) {
    this.parentIfHasName = parentIfHasName1;
  }

  
  /**
   * name of the attribute of the parent where it has this name, if it have the ifName
   * @return the parentThenHasName
   */
  public String getParentThenHasName() {
    return this.parentThenHasName;
  }

  
  /**
   * name of the attribute of the parent where it has this name, if it have the ifName
   * @param parentThenHasName1 the parentThenHasName to set
   */
  public void setParentThenHasName(String parentThenHasName1) {
    this.parentThenHasName = parentThenHasName1;
  }

  
  
}
