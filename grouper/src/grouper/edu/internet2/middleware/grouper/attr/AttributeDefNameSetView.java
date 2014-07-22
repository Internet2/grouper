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
package edu.internet2.middleware.grouper.attr;

import java.util.Set;

import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * map to the attribute def name set view for testing
 * @author mchyzer
 *
 */
public class AttributeDefNameSetView {

  //*****  START GENERATED WITH GenerateFieldConstants.java *****//

  /** constant for field name for: depth */
  public static final String FIELD_DEPTH = "depth";

  /** constant for field name for: id */
  public static final String FIELD_ID = "id";

  /** constant for field name for: ifHasAttrDefNameId */
  public static final String FIELD_IF_HAS_ATTR_DEF_NAME_ID = "ifHasAttrDefNameId";

  /** constant for field name for: ifHasAttrDefNameName */
  public static final String FIELD_IF_HAS_ATTR_DEF_NAME_NAME = "ifHasAttrDefNameName";

  /** constant for field name for: parentId */
  public static final String FIELD_PARENT_ATTR_DEF_NAME_SET_ID = "parentAttrDefNameSetId";

  /** constant for field name for: thenHasAttrDefNameId */
  public static final String FIELD_THEN_HAS_ATTR_DEF_NAME_ID = "thenHasAttrDefNameId";

  /** constant for field name for: thenHasAttrDefNameName */
  public static final String FIELD_THEN_HAS_ATTR_DEF_NAME_NAME = "thenHasAttrDefNameName";

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
      FIELD_DEPTH, FIELD_ID, FIELD_IF_HAS_ATTR_DEF_NAME_ID, FIELD_IF_HAS_ATTR_DEF_NAME_NAME, 
      FIELD_PARENT_ATTR_DEF_NAME_SET_ID, 
      FIELD_THEN_HAS_ATTR_DEF_NAME_ID, FIELD_THEN_HAS_ATTR_DEF_NAME_NAME, FIELD_TYPE,
      FIELD_PARENT_IF_HAS_NAME, FIELD_PARENT_THEN_HAS_NAME);

  /**
   * fields which are included in clone method
   */
  @SuppressWarnings("unused")
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_DEPTH, FIELD_ID, FIELD_IF_HAS_ATTR_DEF_NAME_ID, FIELD_IF_HAS_ATTR_DEF_NAME_NAME, 
      FIELD_PARENT_ATTR_DEF_NAME_SET_ID, FIELD_THEN_HAS_ATTR_DEF_NAME_ID, 
      FIELD_THEN_HAS_ATTR_DEF_NAME_NAME, FIELD_TYPE, FIELD_PARENT_IF_HAS_NAME,
      FIELD_PARENT_THEN_HAS_NAME);

  //*****  END GENERATED WITH GenerateFieldConstants.java *****//

  /** name of the set attribute def name */
  private String ifHasAttrDefNameName;
  
  /** name of the member attribute def name */
  private String thenHasAttrDefNameName;
  
  /** number of hops in the directed graph */
  private int depth;
  
  /** id of the set record */
  private String id;
  
  /** id of the set attribute def name */
  private String ifHasAttrDefNameId;
  
  /** id of the member attribute def name */
  private String thenHasAttrDefNameId;

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
  private AttributeDefAssignmentType type = AttributeDefAssignmentType.immediate;

  /**
   * @return membership type (immediate, effective, or self)
   */
  public AttributeDefAssignmentType getType() {
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
  public void setType(AttributeDefAssignmentType type1) {
    this.type = type1;
  }

  /**
   * membership type -- self, immediate, or effective 
   * set group set assignment type
   * @param type1
   */
  public void setTypeDb(String type1) {
    this.type = AttributeDefAssignmentType.valueOfIgnoreCase(type1, false);
  }

  
  /**
   * name of the set attribute def name
   * @return the ifHasAttrDefNameName
   */
  public String getIfHasAttrDefNameName() {
    return this.ifHasAttrDefNameName;
  }

  
  /**
   * name of the set attribute def name
   * @param ifHasAttrDefNameName1 the ifHasAttrDefNameName to set
   */
  public void setIfHasAttrDefNameName(String ifHasAttrDefNameName1) {
    this.ifHasAttrDefNameName = ifHasAttrDefNameName1;
  }

  
  /**
   * name of the member attribute def name
   * @return the thenHasAttrDefNameName
   */
  public String getThenHasAttrDefNameName() {
    return this.thenHasAttrDefNameName;
  }

  
  /**
   * name of the member attribute def name
   * @param thenHasAttrDefNameName1 the thenHasAttrDefNameName to set
   */
  public void setThenHasAttrDefNameName(String thenHasAttrDefNameName1) {
    this.thenHasAttrDefNameName = thenHasAttrDefNameName1;
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
  public String getIfHasAttrDefNameId() {
    return this.ifHasAttrDefNameId;
  }

  
  /**
   * id of the set attribute def name
   * @param ifHasAttrDefNameId1 the ifHasAttrDefNameId to set
   */
  public void setIfHasAttrDefNameId(String ifHasAttrDefNameId1) {
    this.ifHasAttrDefNameId = ifHasAttrDefNameId1;
  }

  
  /**
   * @return the thenHasAttrDefNameId
   */
  public String getThenHasAttrDefNameId() {
    return thenHasAttrDefNameId;
  }

  
  /**
   * id of the member attribute def name
   * @param thenHasAttrDefNameId1 the thenHasAttrDefNameId to set
   */
  public void setThenHasAttrDefNameId(String thenHasAttrDefNameId1) {
    this.thenHasAttrDefNameId = thenHasAttrDefNameId1;
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
