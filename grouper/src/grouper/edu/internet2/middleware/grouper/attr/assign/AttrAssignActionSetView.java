/**
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
public class AttrAssignActionSetView {

  //*****  START GENERATED WITH GenerateFieldConstants.java *****//

  /** constant for field name for: depth */
  public static final String FIELD_DEPTH = "depth";

  /** constant for field name for: id */
  public static final String FIELD_ID = "id";

  /** constant for field name for: ifHasAttrAssnActionId */
  public static final String FIELD_IF_HAS_ATTR_ASSN_ACTION_ID = "ifHasAttrAssnActionId";

  /** constant for field name for: ifHasAttrAssnActionName */
  public static final String FIELD_IF_HAS_ATTR_ASSN_ACTION_NAME = "ifHasAttrAssnActionName";

  /** constant for field name for: parentAttrActionSetId */
  public static final String FIELD_PARENT_ATTR_ACTION_SET_ID = "parentAttrActionSetId";

  /** constant for field name for: thenHasAttrAssnActionId */
  public static final String FIELD_THEN_HAS_ATTR_ASSN_ACTION_ID = "thenHasAttrAssnActionId";

  /** constant for field name for: thenHasAttrDefNameName */
  public static final String FIELD_THEN_HAS_ATTR_ASSN_ACTION_NAME = "thenHasAttrAssnActionName";

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
      FIELD_DEPTH, FIELD_ID, FIELD_IF_HAS_ATTR_ASSN_ACTION_ID, FIELD_IF_HAS_ATTR_ASSN_ACTION_NAME, 
      FIELD_PARENT_ATTR_ACTION_SET_ID, 
      FIELD_THEN_HAS_ATTR_ASSN_ACTION_ID, FIELD_THEN_HAS_ATTR_ASSN_ACTION_NAME, FIELD_TYPE,
      FIELD_PARENT_IF_HAS_NAME, FIELD_PARENT_THEN_HAS_NAME);

  /**
   * fields which are included in clone method
   */
  @SuppressWarnings("unused")
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_DEPTH, FIELD_ID, FIELD_IF_HAS_ATTR_ASSN_ACTION_ID, FIELD_IF_HAS_ATTR_ASSN_ACTION_NAME, 
      FIELD_PARENT_ATTR_ACTION_SET_ID, FIELD_THEN_HAS_ATTR_ASSN_ACTION_ID, 
      FIELD_THEN_HAS_ATTR_ASSN_ACTION_NAME, FIELD_TYPE, FIELD_PARENT_IF_HAS_NAME,
      FIELD_PARENT_THEN_HAS_NAME);

  //*****  END GENERATED WITH GenerateFieldConstants.java *****//

  /** name of the set attribute def name */
  private String ifHasAttrAssnActionName;
  
  /** name of the member attribute def name */
  private String thenHasAttrAssnActionName;
  
  /** number of hops in the directed graph */
  private int depth;
  
  /** id of the set record */
  private String id;
  
  /** id of the set attribute def name */
  private String ifHasAttrAssnActionId;
  
  /** id of the member attribute def name */
  private String thenHasAttrAssnActionId;

  /** id of the attribute def name set record which is the immediate record this derives from
   * (everything but last hop) */
  private String parentAttrDefNameSetId;
  
  /** name of the attribute of the parent where if it has this name, then it has another name */
  private String parentIfHasName;
  
  /** name of the attribute of the parent where it has this name, if it have the ifName */
  private String parentThenHasName;
  
  /**
   * membership type -- self, immediate, or effective 
   */
  private AttributeAssignActionType type = AttributeAssignActionType.immediate;

  /**
   * @return membership type (immediate, effective, or self)
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
   * membership type -- self, immediate, or effective 
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
   * name of the set attribute def name
   * @return the ifHasAttrDefNameName
   */
  public String getIfHasAttrAssnActionName() {
    return this.ifHasAttrAssnActionName;
  }

  
  /**
   * name of the set attribute def name
   * @param ifHasAttrDefNameName1 the ifHasAttrDefNameName to set
   */
  public void setIfHasAttrAssnActionName(String ifHasAttrDefNameName1) {
    this.ifHasAttrAssnActionName = ifHasAttrDefNameName1;
  }

  
  /**
   * name of the member attribute def name
   * @return the thenHasAttrDefNameName
   */
  public String getThenHasAttrAssnActionName() {
    return this.thenHasAttrAssnActionName;
  }

  
  /**
   * name of the member attribute def name
   * @param thenHasAttrDefNameName1 the thenHasAttrDefNameName to set
   */
  public void setThenHasAttrAssnActionName(String thenHasAttrDefNameName1) {
    this.thenHasAttrAssnActionName = thenHasAttrDefNameName1;
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
   * id of the set attribute def name
   * @return the ifHasAttrDefNameId
   */
  public String getIfHasAttrAssnActionId() {
    return this.ifHasAttrAssnActionId;
  }

  
  /**
   * id of the set attribute def name
   * @param ifHasAttrDefNameId1 the ifHasAttrDefNameId to set
   */
  public void setIfHasAttrAssnActionId(String ifHasAttrDefNameId1) {
    this.ifHasAttrAssnActionId = ifHasAttrDefNameId1;
  }

  
  /**
   * @return the thenHasAttrDefNameId
   */
  public String getThenHasAttrAssnActionId() {
    return thenHasAttrAssnActionId;
  }

  
  /**
   * id of the member attribute def name
   * @param thenHasAttrDefNameId1 the thenHasAttrDefNameId to set
   */
  public void setThenHasAttrAssnActionId(String thenHasAttrDefNameId1) {
    this.thenHasAttrAssnActionId = thenHasAttrDefNameId1;
  }

  
  /**
   * id of the attribute def name set record which is the immediate record this derives from
   * (everything but last hop)
   * @return the parentAttrDefNameSetId
   */
  public String getParentAttrDefNameSetId() {
    return this.parentAttrDefNameSetId;
  }

  
  /**
   * id of the attribute def name set record which is the immediate record this derives from
   * (everything but last hop)
   * @param parentAttrDefNameSetId1 the parentAttrDefNameSetId to set
   */
  public void setParentAttrDefNameSetId(String parentAttrDefNameSetId1) {
    this.parentAttrDefNameSetId = parentAttrDefNameSetId1;
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
