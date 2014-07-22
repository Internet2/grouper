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
package edu.internet2.middleware.grouperClient.ws.beans;


/**
 * Result for finding a group
 * 
 * @author mchyzer
 * 
 */
public class WsGroupDetail {

  /** if this group has a direct composite member, T|F */
  private String hasComposite = null;

  /** left group if composite (note, detail will never be there) */
  private WsGroup leftGroup = null;

  /** right group if composite (note, detail will never be there) */
  private WsGroup rightGroup = null;

  /** types of this group */
  private String[] typeNames;

  /** attribute names, not including the ones listed in the group result or detail */
  private String[] attributeNames;

  /** attribute values, not including ones listed in the group result or detail */
  private String[] attributeValues;

  /** should be UNION, COMPLEMENT, INTERSECTION */
  private String compositeType;
  
  /** params */
  private WsParam[] params;

  /**
   * @return the params
   */
  public WsParam[] getParams() {
    return this.params;
  }
  
  /**
   * @param params1 the params to set
   */
  public void setParams(WsParam[] params1) {
    this.params = params1;
  }
  
  /**
   * should be UNION, COMPLEMENT, INTERSECTION
   * @return type
   */
  public String getCompositeType() {
    return this.compositeType;
  }

  /**
   * should be UNION, COMPLEMENT, INTERSECTION
   * @param compositeType1
   */
  public void setCompositeType(String compositeType1) {
    this.compositeType = compositeType1;
  }

  /**
   * types of this gruop
   * @return the typeNames
   */
  public String[] getTypeNames() {
    return this.typeNames;
  }

  /**
   * types of this group
   * @param typeNames1 the typeNames to set
   */
  public void setTypeNames(String[] typeNames1) {
    this.typeNames = typeNames1;
  }

  /**
   * attribute names, not including the ones listed in the group result or detail
   * @return the attributeNames
   */
  public String[] getAttributeNames() {
    return this.attributeNames;
  }

  /**
   * attribute names, not including the ones listed in the group result or detail
   * @param attributeNames1 the attributeNames to set
   */
  public void setAttributeNames(String[] attributeNames1) {
    this.attributeNames = attributeNames1;
  }

  /**
   * attribute names, not including the ones listed in the group result or detail
   * @return the attributeValues
   */
  public String[] getAttributeValues() {
    return this.attributeValues;
  }

  /**
   * attribute names, not including the ones listed in the group result or detail
   * @param attributeValues1 the attributeValues to set
   */
  public void setAttributeValues(String[] attributeValues1) {
    this.attributeValues = attributeValues1;
  }

  /**
   * no arg constructor
   */
  public WsGroupDetail() {
    // blank

  }

  /**
   * id of the subject that created this group
   */
  private String createSubjectId;

  /**
   * create time in format: yyyy/mm/dd hh24:mi:ss.SSS
   */
  private String createTime;

  /**
   * if a composite member of another group "T", else "F".
   * 
   * A composite group is composed of two groups and a set operator (stored in
   * grouper_composites table) (e.g. union, intersection, etc). A composite
   * group has no immediate members. All subjects in a composite group are
   * effective members.
   */
  private String isCompositeFactor;

  /**
   * Get (optional and questionable) modify source for this group.
   */
  private String modifySource;

  /**
   * Get subject that last modified this group.
   */
  private String modifySubjectId;

  /**
   * Get last modified time for this group. yyyy/mm/dd hh24:mi:ss.SSS
   */
  private String modifyTime;

  /**
   * id of the subject that created this group
   * 
   * @return the createSubjectId
   */
  public String getCreateSubjectId() {
    return this.createSubjectId;
  }

  /**
   * id of the subject that created this group
   * 
   * @param createSubjectId1
   *            the createSubjectId to set
   */
  public void setCreateSubjectId(String createSubjectId1) {
    this.createSubjectId = createSubjectId1;
  }

  /**
   * create time in format: yyyy/mm/dd hh24:mi:ss.SSS
   * 
   * @return the createTime
   */
  public String getCreateTime() {
    return this.createTime;
  }

  /**
   * create time in format: yyyy/mm/dd hh24:mi:ss.SSS
   * 
   * @param createTime1
   *            the createTime to set
   */
  public void setCreateTime(String createTime1) {
    this.createTime = createTime1;
  }

  /**
   * if a composite member of another group "T", else "F".
   * 
   * A composite group is composed of two groups and a set operator (stored in
   * grouper_composites table) (e.g. union, intersection, etc). A composite
   * group has no immediate members. All subjects in a composite group are
   * effective members.
   * 
   * @return the isCompositeFactor
   */
  public String getIsCompositeFactor() {
    return this.isCompositeFactor;
  }

  /**
   * if composite "T", else "F".
   * 
   * A composite group is composed of two groups and a set operator (stored in
   * grouper_composites table) (e.g. union, intersection, etc). A composite
   * group has no immediate members. All subjects in a composite group are
   * effective members.
   * 
   * @param isComposite1
   *            the isCompositeFactor to set
   */
  public void setIsCompositeFactor(String isComposite1) {
    this.isCompositeFactor = isComposite1;
  }

  /**
   * Get (optional and questionable) modify source for this group.
   * 
   * @return the modifySource
   */
  public String getModifySource() {
    return this.modifySource;
  }

  /**
   * Get (optional and questionable) modify source for this group.
   * 
   * @param modifySource1
   *            the modifySource to set
   */
  public void setModifySource(String modifySource1) {
    this.modifySource = modifySource1;
  }

  /**
   * Get subject that last modified this group.
   * 
   * @return the modifySubjectId
   */
  public String getModifySubjectId() {
    return this.modifySubjectId;
  }

  /**
   * Get subject that last modified this group.
   * 
   * @param modifySubjectId1
   *            the modifySubjectId to set
   */
  public void setModifySubjectId(String modifySubjectId1) {
    this.modifySubjectId = modifySubjectId1;
  }

  /**
   * Get last modified time for this group. yyyy/mm/dd hh24:mi:ss.SSS
   * 
   * @return the modifyTime
   */
  public String getModifyTime() {
    return this.modifyTime;
  }

  /**
   * Get last modified time for this group. yyyy/mm/dd hh24:mi:ss.SSS
   * 
   * @param modifyTime1
   *            the modifyTime to set
   */
  public void setModifyTime(String modifyTime1) {
    this.modifyTime = modifyTime1;
  }

  /**
   * if this group has a composite member, T|F
   * @return the hasComposite
   */
  public String getHasComposite() {
    return this.hasComposite;
  }

  /**
   * if this group has a composite member, T|F
   * @param hasComposite1 the hasComposite to set
   */
  public void setHasComposite(String hasComposite1) {
    this.hasComposite = hasComposite1;
  }

  /**
   * left group if composite (note, detail will never be there)
   * @return the leftGroup
   */
  public WsGroup getLeftGroup() {
    return this.leftGroup;
  }

  /**
   * left group if composite (note, detail will never be there)
   * @param leftGroup1 the leftGroup to set
   */
  public void setLeftGroup(WsGroup leftGroup1) {
    this.leftGroup = leftGroup1;
  }

  /**
   * left group if composite (note, detail will never be there)
   * @return the rightGroup
   */
  public WsGroup getRightGroup() {
    return this.rightGroup;
  }

  /**
   * left group if composite (note, detail will never be there)
   * @param rightGroup1 the rightGroup to set
   */
  public void setRightGroup(WsGroup rightGroup1) {
    this.rightGroup = rightGroup1;
  }
}
