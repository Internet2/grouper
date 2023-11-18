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
package edu.internet2.middleware.grouperClient.ws.beans;

import edu.internet2.middleware.grouperClient.util.GrouperClientCommonUtils;

/**
 * If sending in attribute assignments in batch, this is one of the entries
 * @author mchyzer
 *
 */
public class WsAssignAttributeBatchEntry {


  
  /**
   * Type of owner, from enum AttributeAssignType, e.g.
   * group, member, stem, any_mem, imm_mem, attr_def, group_asgn, mem_asgn, 
   * stem_asgn, any_mem_asgn, imm_mem_asgn, attr_def_asgn  
   */
  private String attributeAssignType;
  
  /**
   * Type of owner, from enum AttributeAssignType, e.g.
   * group, member, stem, any_mem, imm_mem, attr_def, group_asgn, mem_asgn, 
   * stem_asgn, any_mem_asgn, imm_mem_asgn, attr_def_asgn  
   * @return type
   */
  public String getAttributeAssignType() {
    return this.attributeAssignType;
  }

  /**
   * Type of owner, from enum AttributeAssignType, e.g.
   * group, member, stem, any_mem, imm_mem, attr_def, group_asgn, mem_asgn, 
   * stem_asgn, any_mem_asgn, imm_mem_asgn, attr_def_asgn  
   * @param attributeAssignType1
   */
  public void setAttributeAssignType(String attributeAssignType1) {
    this.attributeAssignType = attributeAssignType1;
  }

  /**
   * attribute def names to assign to the owners
   */
  private WsAttributeDefNameLookup wsAttributeDefNameLookup;

  
  
  /**
   * attribute def names to assign to the owners
   * @return attribute def name
   */
  public WsAttributeDefNameLookup getWsAttributeDefNameLookup() {
    return this.wsAttributeDefNameLookup;
  }

  /**
   * attribute def names to assign to the owners
   * @param wsAttributeDefNameLookup1
   */
  public void setWsAttributeDefNameLookup(WsAttributeDefNameLookup wsAttributeDefNameLookup1) {
    this.wsAttributeDefNameLookup = wsAttributeDefNameLookup1;
  }

  /**
   * operation to perform for attribute on owners, from enum AttributeAssignOperation
   * assign_attr, add_attr, remove_attr
   */
  private String attributeAssignOperation;

  
  
  /**
   * operation to perform for attribute on owners, from enum AttributeAssignOperation
   * assign_attr, add_attr, remove_attr
   * @return operation
   */
  public String getAttributeAssignOperation() {
    return this.attributeAssignOperation;
  }

  /**
   * operation to perform for attribute on owners, from enum AttributeAssignOperation
   * assign_attr, add_attr, remove_attr
   * @param attributeAssignOperation1
   */
  public void setAttributeAssignOperation(String attributeAssignOperation1) {
    this.attributeAssignOperation = attributeAssignOperation1;
  }

  /**
   * are the values to assign, replace, remove, etc.  If removing, and id is specified, will
   * only remove values with that id.
   */
  private WsAttributeAssignValue[] values;

  
  
  /**
   * are the values to assign, replace, remove, etc.  If removing, and id is specified, will
   * only remove values with that id.
   * @return values
   */
  public WsAttributeAssignValue[] getValues() {
    return this.values;
  }

  /**
   * are the values to assign, replace, remove, etc.  If removing, and id is specified, will
   * only remove values with that id.
   * @param values1
   */
  public void setValues(WsAttributeAssignValue[] values1) {
    this.values = values1;
  }

  /**
   * notes on the assignment (optional)
   */
  private String assignmentNotes;
  
  
  
  /**
   * notes on the assignment (optional)
   * @return assignment notes
   */
  public String getAssignmentNotes() {
    return this.assignmentNotes;
  }

  /**
   * notes on the assignment (optional)
   * @param assignmentNotes1
   */
  public void setAssignmentNotes(String assignmentNotes1) {
    this.assignmentNotes = assignmentNotes1;
  }

  /**
   * enabled time, or null for enabled now.  yyyy/MM/dd HH:mm:ss.SSS
   */
  private String assignmentEnabledTime;
  
  /**
   * enabled time, or null for enabled now.  yyyy/MM/dd HH:mm:ss.SSS
   * @return enabled time, or null for enabled now
   */
  public String getAssignmentEnabledTime() {
    return this.assignmentEnabledTime;
  }

  /**
   * enabled time, or null for enabled now.  yyyy/MM/dd HH:mm:ss.SSS
   * @param assignmentEnabledTime1
   */
  public void setAssignmentEnabledTime(String assignmentEnabledTime1) {
    this.assignmentEnabledTime = assignmentEnabledTime1;
  }

  /**
   * disabled time, or null for not disabled.  yyyy/MM/dd HH:mm:ss.SSS
   */
  private String assignmentDisabledTime;
  
  
  
  /**
   * disabled time, or null for not disabled.  yyyy/MM/dd HH:mm:ss.SSS
   * @return disabled time
   */
  public String getAssignmentDisabledTime() {
    return this.assignmentDisabledTime;
  }

  /**
   * disabled time, or null for not disabled.  yyyy/MM/dd HH:mm:ss.SSS
   * @param assignmentDisabledTime1
   */
  public void setAssignmentDisabledTime(String assignmentDisabledTime1) {
    this.assignmentDisabledTime = assignmentDisabledTime1;
  }

  /**
   * really only for permissions, if the assignee can delegate to someone else.  TRUE|FALSE|GRANT
   */
  private String delegatable;
  
  
  
  /**
   * really only for permissions, if the assignee can delegate to someone else.  TRUE|FALSE|GRANT
   * @return delegatable
   */
  public String getDelegatable() {
    return this.delegatable;
  }

  /**
   * really only for permissions, if the assignee can delegate to someone else.  TRUE|FALSE|GRANT
   * @param delegatable1
   */
  public void setDelegatable(String delegatable1) {
    this.delegatable = delegatable1;
  }

  /**
   * operation to perform for attribute value on attribute
   * assignments: assign_value, add_value, remove_value, replace_values
   */
  private String attributeAssignValueOperation;
  
  /**
   * operation to perform for attribute value on attribute
   * assignments: assign_value, add_value, remove_value, replace_values
   * @return operation
   */
  public String getAttributeAssignValueOperation() {
    return this.attributeAssignValueOperation;
  }

  /**
   * operation to perform for attribute value on attribute
   * assignments: assign_value, add_value, remove_value, replace_values
   * @param attributeAssignValueOperation1
   */
  public void setAttributeAssignValueOperation(
      String attributeAssignValueOperation1) {
    this.attributeAssignValueOperation = attributeAssignValueOperation1;
  }

  /**
   * lookup to remove etc
   */
  private WsAttributeAssignLookup wsAttributeAssignLookup;
  
  /**
   * lookup to remove etc
   * @return lookup
   */
  public WsAttributeAssignLookup getWsAttributeAssignLookup() {
    return this.wsAttributeAssignLookup;
  }

  /**
   * lookup to remove etc
   * @param wsAttributeAssignLookup1
   */
  public void setWsAttributeAssignLookup(WsAttributeAssignLookup wsAttributeAssignLookup1) {
    this.wsAttributeAssignLookup = wsAttributeAssignLookup1;
  }

  /**
   * group to assign attribute to
   */
  private WsGroupLookup wsOwnerGroupLookup; 
  
  /**
   * group to assign attribute to
   * @return group lookup
   */
  public WsGroupLookup getWsOwnerGroupLookup() {
    return this.wsOwnerGroupLookup;
  }

  /**
   * group to assign attribute to
   * @param wsOwnerGroupLookup1
   */
  public void setWsOwnerGroupLookup(WsGroupLookup wsOwnerGroupLookup1) {
    this.wsOwnerGroupLookup = wsOwnerGroupLookup1;
  }

  /**
   * stem to assign attribute to
   */
  private WsStemLookup wsOwnerStemLookup;
  
  /**
   * stem to assign attribute to
   * @return stem lookup
   */
  public WsStemLookup getWsOwnerStemLookup() {
    return this.wsOwnerStemLookup;
  }

  /**
   * stem to assign attribute to
   * @param wsOwnerStemLookup1
   */
  public void setWsOwnerStemLookup(WsStemLookup wsOwnerStemLookup1) {
    this.wsOwnerStemLookup = wsOwnerStemLookup1;
  }

  /**
   * subject of the member to assign to
   */
  private WsSubjectLookup wsOwnerSubjectLookup;
  
  
  
  /**
   * subject of the member to assign to
   * @return subject
   */
  public WsSubjectLookup getWsOwnerSubjectLookup() {
    return this.wsOwnerSubjectLookup;
  }

  /**
   * subject of the member to assign to
   * @param wsOwnerSubjectLookup1
   */
  public void setWsOwnerSubjectLookup(WsSubjectLookup wsOwnerSubjectLookup1) {
    this.wsOwnerSubjectLookup = wsOwnerSubjectLookup1;
  }

  /**
   * immediate membership to assign to
   */
  private WsMembershipLookup wsOwnerMembershipLookup;
  
  /**
   * immediate membership to assign to
   * @return immediate membership
   */
  public WsMembershipLookup getWsOwnerMembershipLookup() {
    return this.wsOwnerMembershipLookup;
  }

  /**
   * immediate membership to assign to
   * @param wsOwnerMembershipLookup1
   */
  public void setWsOwnerMembershipLookup(WsMembershipLookup wsOwnerMembershipLookup1) {
    this.wsOwnerMembershipLookup = wsOwnerMembershipLookup1;
  }

  /**
   * effective membership to assign to
   */
  private WsMembershipAnyLookup wsOwnerMembershipAnyLookup;
  
  /**
   * effective membership to assign to
   * @return effective memberships
   */
  public WsMembershipAnyLookup getWsOwnerMembershipAnyLookup() {
    return this.wsOwnerMembershipAnyLookup;
  }

  /**
   * effective membership to assign to
   * @param wsOwnerMembershipAnyLookup1
   */
  public void setWsOwnerMembershipAnyLookup(WsMembershipAnyLookup wsOwnerMembershipAnyLookup1) {
    this.wsOwnerMembershipAnyLookup = wsOwnerMembershipAnyLookup1;
  }

  /**
   * attribute definition to assign to
   */
  private WsAttributeDefLookup wsOwnerAttributeDefLookup; 
  
  
  
  /**
   * attribute definition to assign to
   * @return attribute definition to assign to
   */
  public WsAttributeDefLookup getWsOwnerAttributeDefLookup() {
    return this.wsOwnerAttributeDefLookup;
  }

  /**
   * attribute definition to assign to
   * @param wsOwnerAttributeDefLookup1
   */
  public void setWsOwnerAttributeDefLookup(WsAttributeDefLookup wsOwnerAttributeDefLookup1) {
    this.wsOwnerAttributeDefLookup = wsOwnerAttributeDefLookup1;
  }

  /**
   * if you know the assign ids you want or the index of the backreference id, put it here for an assignment on an assignment
   */
  private WsAttributeAssignLookup wsOwnerAttributeAssignLookup;
  
  /**
   * if you know the assign ids you want or the index of the backreference id, put it here for an assignment on an assignment
   * @return id or index
   */
  public WsAttributeAssignLookup getWsOwnerAttributeAssignLookup() {
    return this.wsOwnerAttributeAssignLookup;
  }

  /**
   * if you know the assign ids you want or the index of the backreference id, put it here for an assignment on an assignment
   * @param wsOwnerAttributeAssignLookup1
   */
  public void setWsOwnerAttributeAssignLookup(
      WsAttributeAssignLookup wsOwnerAttributeAssignLookup1) {
    this.wsOwnerAttributeAssignLookup = wsOwnerAttributeAssignLookup1;
  }
  
  /**
   * action to assign, or "assign" is the default if blank
   */
  private String action;

  /**
   * action to assign, or "assign" is the default if blank
   * @return action
   */
  public String getAction() {
    return this.action;
  }

  /**
   * action to assign, or "assign" is the default if blank
   * @param action1
   */
  public void setAction(String action1) {
    this.action = action1;
  }
  
}
