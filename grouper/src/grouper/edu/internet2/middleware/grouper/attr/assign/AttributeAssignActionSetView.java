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
package edu.internet2.middleware.grouper.attr.assign;

import java.util.Set;

import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * map to the attribute assign action set view for testing
 * @author mchyzer
 *
 */
public class AttributeAssignActionSetView {

  //*****  START GENERATED WITH GenerateFieldConstants.java *****//

  /** constant for field name for: depth */
  public static final String FIELD_DEPTH = "depth";

  /** constant for field name for: id */
  public static final String FIELD_ID = "id";

  /** constant for field name for: ifHasAttrAssignActionId */
  public static final String FIELD_IF_HAS_ATTR_ASSIGN_ACTION_ID = "ifHasAttrAssignActionId";

  /** constant for field name for: ifHasAttrAssignActionName */
  public static final String FIELD_IF_HAS_ATTR_ASSIGN_ACTION_NAME = "ifHasAttrAssignActionName";

  /** constant for field name for: parentId */
  public static final String FIELD_PARENT_ATTR_ASSIGN_ACTION_SET_ID = "parentAttrAssignActionSetId";

  /** constant for field name for: thenHasAttrAssignActionId */
  public static final String FIELD_THEN_HAS_ATTR_ASSIGN_ACTION_ID = "thenHasAttrAssignActionId";

  /** constant for field name for: thenHasAttrAssignActionName */
  public static final String FIELD_THEN_HAS_ATTR_ASSIGN_ACTION_NAME = "thenHasAttrAssignActionName";

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
      FIELD_DEPTH, FIELD_ID, FIELD_IF_HAS_ATTR_ASSIGN_ACTION_ID, FIELD_IF_HAS_ATTR_ASSIGN_ACTION_NAME, 
      FIELD_PARENT_ATTR_ASSIGN_ACTION_SET_ID, 
      FIELD_THEN_HAS_ATTR_ASSIGN_ACTION_ID, FIELD_THEN_HAS_ATTR_ASSIGN_ACTION_NAME, FIELD_TYPE,
      FIELD_PARENT_IF_HAS_NAME, FIELD_PARENT_THEN_HAS_NAME);

  /**
   * fields which are included in clone method
   */
  @SuppressWarnings("unused")
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_DEPTH, FIELD_ID, FIELD_IF_HAS_ATTR_ASSIGN_ACTION_ID, FIELD_IF_HAS_ATTR_ASSIGN_ACTION_NAME, 
      FIELD_PARENT_ATTR_ASSIGN_ACTION_SET_ID, FIELD_THEN_HAS_ATTR_ASSIGN_ACTION_ID, 
      FIELD_THEN_HAS_ATTR_ASSIGN_ACTION_NAME, FIELD_TYPE, FIELD_PARENT_IF_HAS_NAME,
      FIELD_PARENT_THEN_HAS_NAME);

  //*****  END GENERATED WITH GenerateFieldConstants.java *****//

  /** name of the set attribute assign action */
  private String ifHasAttrAssignActionName;
  
  /** name of the member attribute assign action */
  private String thenHasAttrAssignActionName;
  
  /** number of hops in the directed graph */
  private int depth;
  
  /** id of the set record */
  private String id;
  
  /** id of the set attribute assign action */
  private String ifHasAttrAssignActionId;
  
  /** id of the member attribute assign action */
  private String thenHasAttrAssignActionId;

  /** id of the attribute assign action set record which is the immediate record this derives from
   * (everything but last hop) */
  private String parentAttrAssignActionSetId;
  
  /** name of the attribute of the parent where if it has this name, then it has another name */
  private String parentIfHasName;
  
  /** name of the attribute of the parent where it has this name, if it have the ifName */
  private String parentThenHasName;
  
  /**
   * membership type -- self, immediate, or effective 
   */
  private AttributeAssignActionType type = AttributeAssignActionType.immediate;

  /**
   * @return set type (immediate, effective, or self)
   */
  public AttributeAssignActionType getType() {
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
   * set type -- self, immediate, or effective 
   * set group set assignment type
   * @param type1
   */
  public void setType(AttributeAssignActionType type1) {
    this.type = type1;
  }

  /**
   * membership type -- self, immediate, or effective 
   * set group set assignment type
   * @param type1
   */
  public void setTypeDb(String type1) {
    this.type = AttributeAssignActionType.valueOfIgnoreCase(type1, false);
  }

  
  /**
   * name of the set attribute assign action name
   * @return the ifHasAttrAssignActionName
   */
  public String getIfHasAttrAssignActionName() {
    return this.ifHasAttrAssignActionName;
  }

  
  /**
   * name of the set attribute assign action
   * @param ifHasAttrAssignActionName1 the ifHasAttrAssignActionName to set
   */
  public void setIfHasAttrAssignActionName(String ifHasAttrAssignActionName1) {
    this.ifHasAttrAssignActionName = ifHasAttrAssignActionName1;
  }

  
  /**
   * name of the member attribute assign action
   * @return the thenHasAttrAssignActionName
   */
  public String getThenHasAttrAssignActionName() {
    return this.thenHasAttrAssignActionName;
  }

  
  /**
   * name of the member attribute assign action
   * @param thenHasAttrAssignActionName1 the thenHasAttrAssignActionName to set
   */
  public void setThenHasAttrAssignActionName(String thenHasAttrAssignActionName1) {
    this.thenHasAttrAssignActionName = thenHasAttrAssignActionName1;
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
   * id of the set attribute assign action
   * @return the ifHasAttrAssignActionId
   */
  public String getIfHasAttrAssignActionId() {
    return this.ifHasAttrAssignActionId;
  }

  
  /**
   * id of the set attribute assign action
   * @param ifHasAttrAssignActionId1 the ifHasAttrAssignActionId to set
   */
  public void setIfHasAttrAssignActionId(String ifHasAttrAssignActionId1) {
    this.ifHasAttrAssignActionId = ifHasAttrAssignActionId1;
  }

  
  /**
   * @return the thenHasAttrAssignActionId
   */
  public String getThenHasAttrAssignActionId() {
    return thenHasAttrAssignActionId;
  }

  
  /**
   * id of the member attribute assign action
   * @param thenHasAttrAssignActionId1 the thenHasAttrAssignActionId to set
   */
  public void setThenHasAttrAssignActionId(String thenHasAttrAssignActionId1) {
    this.thenHasAttrAssignActionId = thenHasAttrAssignActionId1;
  }

  
  /**
   * id of the attribute assign action set record which is the immediate record this derives from
   * (everything but last hop)
   * @return the parentAttrAssignActionSetId
   */
  public String getParentAttrAssignActionSetId() {
    return this.parentAttrAssignActionSetId;
  }

  /**
   * id of the attribute assign action set record which is the immediate record this derives from
   * (everything but last hop)
   * @param parentAttrAssignActionSetId1 the parentAttrAssignActionSetId to set
   */
  public void setParentAttrAssignActionSetId(String parentAttrAssignActionSetId1) {
    this.parentAttrAssignActionSetId = parentAttrAssignActionSetId1;
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
