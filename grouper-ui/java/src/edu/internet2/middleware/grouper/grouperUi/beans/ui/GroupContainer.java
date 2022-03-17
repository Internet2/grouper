/*******************************************************************************
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
 ******************************************************************************/
package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.emory.mathcs.backport.java.util.Collections;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConfig;
import edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConfigService;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefScope;
import edu.internet2.middleware.grouper.attr.AttributeDefScopeType;
import edu.internet2.middleware.grouper.attr.AttributeDefValueType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiAttributeAssign;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiMembershipSubjectContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiPITMembershipView;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiSubject;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiPaging;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiSorting;
import edu.internet2.middleware.grouper.hooks.examples.MembershipCannotAddSelfToGroupHook;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.customUi.CustomUiEngine;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUserData;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;
import edu.internet2.middleware.grouper.userData.GrouperUserDataApi;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * group container in new ui
 * @author mchyzer
 */
public class GroupContainer {
  
  /** logger */
  protected static final Log LOG = LogFactory.getLog(GroupContainer.class);

 /**
  * group types for edit to show on details and group edit screen
  */
  private List<GroupTypeForEdit> groupTypesForEdit;
  
  /**
   * regex pattern for group types for edit. group types are configured in grouper.properties
   */
  private static Pattern groupTypeForEditPattern = Pattern.compile("^groupScreen\\.attribute\\.([^.]+)\\.[a-zA-Z0-9]+$");
  
  /**
   * get list of group types for view only
   * @return
   */
  public List<GroupTypeForEdit> getGroupTypesForView() {
    return GrouperUtil.nonNull(getGroupTypes(true));
  }
  
  
  /**
   * get list of group attributes for edit
   * @return
   */
  public List<GroupTypeForEdit> getGroupTypesForEdit() {
    return GrouperUtil.nonNull(getGroupTypes(false));
  }
  
  /**
   * @param checkOnlyReadPrivileges
   * @return list of group types
   */
  private List<GroupTypeForEdit> getGroupTypes(boolean checkOnlyReadPrivileges) {
    
    if (!GrouperConfig.retrieveConfig().propertyValueBoolean("groupScreen.attribute.enabled", true)) {
      return new ArrayList<>();
    }
    
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    Boolean canAttributeReadUpdateOnGroup = (Boolean)GrouperSession.callbackGrouperSession(
        GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
          
          @Override
          public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
            
            if (checkOnlyReadPrivileges) {
              return GroupContainer.this.getGuiGroup().getGroup().canHavePrivilege(loggedInSubject, AccessPrivilege.GROUP_ATTR_READ.getName(), false);
            }
            
            return GroupContainer.this.getGuiGroup().getGroup().canHavePrivilege(loggedInSubject, AccessPrivilege.GROUP_ATTR_UPDATE.getName(), false) &&
                GroupContainer.this.getGuiGroup().getGroup().canHavePrivilege(loggedInSubject, AccessPrivilege.GROUP_ATTR_READ.getName(), false);
          }
        });
    
    if (!canAttributeReadUpdateOnGroup) {
      return new ArrayList<>();
    }
    
    if (this.groupTypesForEdit == null) {
      Map<String, String> properties = GrouperConfig.retrieveConfig().propertiesMap(groupTypeForEditPattern);
      
      if (GrouperUtil.length(properties) > 0) {
      
        List<GroupTypeForEdit> groupTypeForEdits = new ArrayList<GroupTypeForEdit>();
        
        Map<String, String> attributeDefNameToConfigId = new HashMap<>();
        
        Set<String> topLevelMarkersSelected = new HashSet<>();
        
        for (String key : properties.keySet()) {
          
          if (key.endsWith(".attributeName")) {
            Matcher matcher = groupTypeForEditPattern.matcher(key);
            matcher.matches();
            String configId = matcher.group(1);
            String attributeName = properties.get("groupScreen.attribute." + configId + ".attributeName");
            if (!StringUtils.isBlank(attributeName)) {
              String label = StringUtils.defaultIfBlank(properties.get("groupScreen.attribute." + configId + ".label"), attributeName);
              String description = properties.get("groupScreen.attribute." + configId + ".description");
              int index = GrouperUtil.intValue(properties.get("groupScreen.attribute." + configId + ".index"), 100);
              
              
              AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(attributeName, false);
              if (attributeDefName == null) {
                LOG.warn(attributeName + " is configured for groupScreen.attribute but it couldn't be found.");
                continue;
              }
              
              attributeDefNameToConfigId.put(attributeDefName.getName(), configId);
              
              AttributeDef attributeDef = attributeDefName.getAttributeDef();
              
              if (attributeDef.isMultiAssignable() || attributeDef.isMultiValued()) {
                LOG.warn(attributeDef.getName() + " is multiAssignable or multiValued and it's not supported for group types.");
                continue;
              }
              
              Boolean canAttributeReadUpdate = (Boolean)GrouperSession.callbackGrouperSession(
                  GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
                    
                    @Override
                    public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
                      
                      if (checkOnlyReadPrivileges) {
                        return attributeDef.getPrivilegeDelegate().canAttrRead(loggedInSubject);
                      }
                      
                      return attributeDef.getPrivilegeDelegate().canAttrRead(loggedInSubject) && 
                          attributeDef.getPrivilegeDelegate().canAttrUpdate(loggedInSubject);
                    }
                  });
              
              if (!canAttributeReadUpdate) {
                continue;
              }

              GroupTypeForEdit groupTypeForEdit = new GroupTypeForEdit();
              groupTypeForEdit.setAttributeDefName(attributeDefName);
              groupTypeForEdit.setConfigId(configId);
              
              Set<AttributeAssign> attributeAssignments = null;
              //TODO convert form element type to enum
              if (attributeDef.isAssignToGroup() && attributeDef.getValueType() == AttributeDefValueType.marker) {
                
                attributeAssignments = GroupContainer.this.getGuiGroup().getGroup().getAttributeDelegate().retrieveAssignments(attributeDefName);
                if (attributeAssignments.size() > 1) {
                  LOG.warn(GroupContainer.this.getGuiGroup().getGroup().getName() + " has more than 1 assignment for " + attributeDefName.getName());
                  continue;
                }
                
                groupTypeForEdit.setFormElementType("CHECKBOX");
                if (attributeAssignments.size() > 0) {
                  groupTypeForEdit.setValue("true");
                  topLevelMarkersSelected.add(attributeDefName.getName());
                }
              } else if (attributeDef.isAssignToGroup() && attributeDef.getValueType() == AttributeDefValueType.string) {
                
                attributeAssignments = GroupContainer.this.getGuiGroup().getGroup().getAttributeDelegate().retrieveAssignments(attributeDefName);
                if (attributeAssignments.size() > 1) {
                  LOG.warn(GroupContainer.this.getGuiGroup().getGroup().getName() + " has more than 1 assignment for " + attributeDefName.getName());
                  continue;
                }
                
                groupTypeForEdit.setFormElementType("TEXTFIELD");
                
                if (attributeAssignments.size() > 0) {
                  String valueString = attributeAssignments.iterator().next().getValueDelegate().retrieveValueString();
                  groupTypeForEdit.setValue(valueString);
                }
                
              } else if (attributeDef.isAssignToGroupAssn()) {
                
                AttributeDefScope attributeDefScopeForAttributeDef = null;
                Set<AttributeDefScope> attributeDefScopes = attributeDef.getAttributeDefScopeDelegate().retrieveAttributeDefScopes();
                for (AttributeDefScope attributeDefScope: GrouperUtil.nonNull(attributeDefScopes)) {
                  if (attributeDefScope.getAttributeDefScopeType() == AttributeDefScopeType.nameEquals
                      && attributeDef.getValueType() == AttributeDefValueType.string
                      && StringUtils.isNotBlank(attributeDefScope.getScopeString()) ) {
                    attributeDefScopeForAttributeDef = attributeDefScope;
                    break;
                  }
                }
                
                if (attributeDefScopeForAttributeDef == null) {
                  LOG.warn(GroupContainer.this.getGuiGroup().getGroup().getName() + " cannot find a valid nameEquals scope for attribute " + attributeDefName.getName() 
                  + ", attributeDef: "+attributeDefName.getAttributeDef().getName());
                  continue;
                }
                  
                AttributeDefName markerAttributeDefName = AttributeDefNameFinder.findByName(attributeDefScopeForAttributeDef.getScopeString(), false);
                if (markerAttributeDefName == null) {
                  LOG.warn(GroupContainer.this.getGuiGroup().getGroup().getName() + " has an invalid scope string " + attributeDefName.getName() 
                    + ", attributeDef: "+attributeDefName.getAttributeDef().getName());
                  continue;
                }
                
                attributeAssignments = GroupContainer.this.getGuiGroup().getGroup().getAttributeDelegate()
                    .retrieveAssignments(markerAttributeDefName);
                if (attributeAssignments.size() > 1) {
                  LOG.warn(GroupContainer.this.getGuiGroup().getGroup().getName() + " has more than 1 assignment for " + attributeDefName.getName());
                  continue;
                }
                
                if (!topLevelMarkersSelected.contains(markerAttributeDefName.getName())) {
                  groupTypeForEdit.setInitiallyVisible(false);
                }
                
                groupTypeForEdit.setMarkerAttributeDefName(markerAttributeDefName);
                
                if (attributeDefNameToConfigId.containsKey(markerAttributeDefName.getName())) {
                  groupTypeForEdit.setMarkerConfigId(attributeDefNameToConfigId.get(markerAttributeDefName.getName()));
                }
                
                groupTypeForEdit.setFormElementType("TEXTFIELD");
                groupTypeForEdit.setScopeString(attributeDefScopeForAttributeDef.getScopeString());
                if (attributeAssignments.size() > 0) {
                  // get the assignment on the assignment which is the name value pair
//                  AttributeAssign attributeAssign = attributeAssignments.iterator().next();
//                  attributeAssignments = attributeAssign.getAttributeDelegate()
//                      .retrieveAssignments(attributeDefName);
//                  String valueString = attributeAssign.getValueDelegate().retrieveValueString();
                  
                  attributeAssignments = attributeAssignments.iterator().next().getAttributeDelegate()
                      .retrieveAssignments(attributeDefName);
                  if (attributeAssignments.size() > 0) {
                    String valueString = attributeAssignments.iterator().next().getValueDelegate().retrieveValueString();
                    groupTypeForEdit.setValue(valueString);
                  }
                  
                }
                
              }
              
              if (StringUtils.isBlank(groupTypeForEdit.getFormElementType())) {
                continue;
              }
              
              if (StringUtils.isBlank(groupTypeForEdit.getValue()) && checkOnlyReadPrivileges) {
                continue; // no need to show on screen when value is blank and it's for group details page
              }
              
              groupTypeForEdit.setAttributeName(attributeName);
              groupTypeForEdit.setIndex(index);
              groupTypeForEdit.setDescription(description);
              if (!label.endsWith(":")) {
                label = label + ":";
              }
              groupTypeForEdit.setLabel(label);
              
              groupTypeForEdits.add(groupTypeForEdit);
              
            }
            
          }
          
        }
        
        this.groupTypesForEdit = groupTypeForEdits;
        if (this.groupTypesForEdit != null) {
          Collections.sort(this.groupTypesForEdit, new Comparator<GroupTypeForEdit>() {

            @Override
            public int compare(GroupTypeForEdit arg0, GroupTypeForEdit arg1) {
              return Integer.compare(arg0.getIndex(), arg1.getIndex());
            }
          });
        }
        
      }
      
    }
    
    return this.groupTypesForEdit;
  }
  
  /**
   * 
   * @return if has custom ui attribute
   */
  public boolean isHasCustomUi() {
    return (Boolean)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        return CustomUiEngine.retrieveCustomUiConfigurationConfigId(GroupContainer.this.getGuiGroup().getGroup(), true) != null;
      }
    });
    
  }
  
  /**
   * if can view privilege inheritance
   * @return true if can
   */
  public boolean isCanReadPrivilegeInheritance() {

    //at least you have to be able to admin privileges on this folder
    if (!this.isCanAdmin()) {
      return false;
    }
    
    return GrouperRequestContainer.retrieveFromRequestOrCreate().getRulesContainer().isCanReadPrivilegeInheritance();
  }
  
  
  private Map<Integer, String> customCompositeIndexesAndUiKeys;

  /**
   * if displaying composite, this is the owner
   */
  private GuiGroup compositeOwnerGuiGroup;

  /**
   * if displaying a composite, this is the left factor
   */
  private GuiGroup compositeLeftFactorGuiGroup;
  
  /**
   * if displaying a composite, this is the right factor
   */
  private GuiGroup compositeRightFactorGuiGroup;
  
  /**
   * if displaying composite, this is the owner
   * @return the compositeOwnerGuiGroup
   */
  public GuiGroup getCompositeOwnerGuiGroup() {
    return this.compositeOwnerGuiGroup;
  }
  
  /**
   * if displaying composite, this is the owner
   * @param compositeOwnerGuiGroup1 the compositeOwnerGuiGroup to set
   */
  public void setCompositeOwnerGuiGroup(GuiGroup compositeOwnerGuiGroup1) {
    this.compositeOwnerGuiGroup = compositeOwnerGuiGroup1;
  }
  
  /**
   * if displaying a composite, this is the left factor
   * @return the compositeLeftFactorGuiGroup
   */
  public GuiGroup getCompositeLeftFactorGuiGroup() {
    return this.compositeLeftFactorGuiGroup;
  }
  
  /**
   * if displaying a composite, this is the left factor
   * @param compositeLeftFactorGuiGroup1 the compositeLeftFactorGuiGroup to set
   */
  public void setCompositeLeftFactorGuiGroup(GuiGroup compositeLeftFactorGuiGroup1) {
    this.compositeLeftFactorGuiGroup = compositeLeftFactorGuiGroup1;
  }

  
  /**
   * if displaying a composite, this is the right factor
   * @return the compositeRightFactorGuiGroup
   */
  public GuiGroup getCompositeRightFactorGuiGroup() {
    return this.compositeRightFactorGuiGroup;
  }

  /**
   * @param compositeRightFactorGuiGroup1 the compositeRightFactorGuiGroup to set
   */
  public void setCompositeRightFactorGuiGroup(GuiGroup compositeRightFactorGuiGroup1) {
    this.compositeRightFactorGuiGroup = compositeRightFactorGuiGroup1;
  }

  /**
   * if extended results on audit display
   */
  private boolean auditExtendedResults = false;

  /**
   * if extended results on audit display
   * @return if extended results
   */
  public boolean isAuditExtendedResults() {
    return this.auditExtendedResults;
  }

  /**
   * if extended results on audit display
   * @param auditExtendedResults1
   */
  public void setAuditExtendedResults(boolean auditExtendedResults1) {
    this.auditExtendedResults = auditExtendedResults1;
  }

  /** audit type **/
  private String auditType;
  
  /**
   * audit type
   * @return
   */
  public String getAuditType() {
    return this.auditType;
  }


  /**
   * audit type
   * @param auditType1
   */
  public void setAuditType(String auditType1) {
    this.auditType = auditType1;
  }

  /**
   * sorting, e.g. for the audit screen
   */
  private GuiSorting guiSorting;
  
  /**
   * sorting, e.g. for the audit screen
   * @return the sorting
   */
  public GuiSorting getGuiSorting() {
    return this.guiSorting;
  }

  /**
   * sorting, e.g. for the audit screen
   * @param guiSorting1
   */
  public void setGuiSorting(GuiSorting guiSorting1) {
    this.guiSorting = guiSorting1;
  }

  /**
   * audit entries for group
   */
  private Set<GuiAuditEntry> guiAuditEntries;

  
  
  /**
   * audit entries for group
   * @return audit entries
   */
  public Set<GuiAuditEntry> getGuiAuditEntries() {
    return this.guiAuditEntries;
  }

  /**
   * audit entries for group
   * @param guiAuditEntries1
   */
  public void setGuiAuditEntries(Set<GuiAuditEntry> guiAuditEntries1) {
    this.guiAuditEntries = guiAuditEntries1;
  }

  /**
   * how many successes
   */
  private int successCount;
  
  /**
   * how many failures
   */
  private int failureCount;
  
  /**
   * how many successes
   * @return successes
   */
  public int getSuccessCount() {
    return this.successCount;
  }

  /**
   * how many successes
   * @param successCount1
   */
  public void setSuccessCount(int successCount1) {
    this.successCount = successCount1;
  }

  /**
   * how many failures
   * @return failures
   */
  public int getFailureCount() {
    return this.failureCount;
  }

  /**
   * how many failures
   * @param failuresCount1
   */
  public void setFailureCount(int failuresCount1) {
    this.failureCount = failuresCount1;
  }

  /**
   * if entities get admin when added to a group
   * @return true if entities get admin when added to a group
   */
  public boolean isConfigDefaultGroupsCreateGrantAllAdmin() {
    return GrouperConfig.retrieveConfig()
        .propertyValueBoolean("groups.create.grant.all.admin", false);
  }

  /**
   * if entities get update when added to a group
   * @return true if entities get update when added to a group
   */
  public boolean isConfigDefaultGroupsCreateGrantAllUpdate() {
    return GrouperConfig.retrieveConfig()
        .propertyValueBoolean("groups.create.grant.all.update", false);
  }
  
  /**
   * if entities get read when added to a group
   * @return true if entities get read when added to a group
   */
  public boolean isConfigDefaultGroupsCreateGrantAllRead() {
    return GrouperConfig.retrieveConfig()
        .propertyValueBoolean("groups.create.grant.all.read", false);
  }

  /**
   * if entities get view when added to a group
   * @return true if entities get view when added to a group
   */
  public boolean isConfigDefaultGroupsCreateGrantAllView() {
    return GrouperConfig.retrieveConfig()
        .propertyValueBoolean("groups.create.grant.all.view", false);
  }

  /**
   * if entities get optin when added to a group
   * @return true if entities get optin when added to a group
   */
  public boolean isConfigDefaultGroupsCreateGrantAllOptin() {
    return GrouperConfig.retrieveConfig()
        .propertyValueBoolean("groups.create.grant.all.optin", false);
  }

  /**
   * if entities get optout when added to a group
   * @return true if entities get optout when added to a group
   */
  public boolean isConfigDefaultGroupsCreateGrantAllOptout() {
    return GrouperConfig.retrieveConfig()
        .propertyValueBoolean("groups.create.grant.all.optout", false);
  }
  /**
   * if entities get attrRead when added to a group
   * @return true if entities get attrRead when added to a group
   */
  public boolean isConfigDefaultGroupsCreateGrantAllAttrRead() {
    return GrouperConfig.retrieveConfig()
        .propertyValueBoolean("groups.create.grant.all.attrRead", false);
  }
  /**
   * if entities get attrUpdate when added to a group
   * @return true if entities get attrUpdate when added to a group
   */
  public boolean isConfigDefaultGroupsCreateGrantAllAttrUpdate() {
    return GrouperConfig.retrieveConfig()
        .propertyValueBoolean("groups.create.grant.all.attrUpdate", false);
  }
  
  /**
   * number of members added
   */
  private int countAdded;

  /**
   * number of members removed
   */
  private int countRemoved;
  
  /**
   * number of members
   */
  private int countTotal;
  
  /**
   * number of unresolvable subjects
   */
  private int countUnresolvableSubjects;

  /**
   * number of members
   * @return the countTotal
   */
  public int getCountTotal() {
    return this.countTotal;
  }
  
  /**
   * number of members
   * @param countTotal1 the countTotal to set
   */
  public void setCountTotal(int countTotal1) {
    this.countTotal = countTotal1;
  }

  
  /**
   * @return the countUnresolvableSubjects
   */
  public int getCountUnresolvableSubjects() {
    return countUnresolvableSubjects;
  }

  
  /**
   * @param countUnresolvableSubjects the countUnresolvableSubjects to set
   */
  public void setCountUnresolvableSubjects(int countUnresolvableSubjects) {
    this.countUnresolvableSubjects = countUnresolvableSubjects;
  }

  /**
   * number of members added
   * @return the countAdded
   */
  public int getCountAdded() {
    return this.countAdded;
  }

  
  /**
   * number of members added
   * @param countAdded1 the countAdded to set
   */
  public void setCountAdded(int countAdded1) {
    this.countAdded = countAdded1;
  }
  
  /**
   * number of members removed
   * @return the countRemoved
   */
  public int getCountRemoved() {
    return this.countRemoved;
  }

  
  /**
   * number of members removed
   * @param countRemoved1 the countRemoved to set
   */
  public void setCountRemoved(int countRemoved1) {
    this.countRemoved = countRemoved1;
  }

  /**
   * when searching for subjects to add to the group, list them here
   */
  private Set<GuiSubject> guiSubjectsAddMember;
  
  /**
   * when searching for subjects to add to the group, list them here
   * @return the gui subjects
   */
  public Set<GuiSubject> getGuiSubjectsAddMember() {
    return this.guiSubjectsAddMember;
  }

  /**
   * when searching for subjects to add to the group, list them here
   * @param guiSubjectsAddMember1
   */
  public void setGuiSubjectsAddMember(Set<GuiSubject> guiSubjectsAddMember1) {
    this.guiSubjectsAddMember = guiSubjectsAddMember1;
  }
  
  /**
   * list of loader managed groups
   */
  private Set<GuiLoaderManagedGroup> guiLoaderManagedGroups;
  
  /**
   * @return
   */
  public Set<GuiLoaderManagedGroup> getGuiLoaderManagedGroups() {
    return this.guiLoaderManagedGroups;
  }
  
  public void setGuiLoaderManagedGroups(Set<GuiLoaderManagedGroup> guiLoaderManagedGroups1) {
    this.guiLoaderManagedGroups = guiLoaderManagedGroups1;
  }

  /**
   * gui group shown on screen
   */
  private GuiGroup guiGroup;

  /**
   * gui group shown on screen
   * @return group
   */
  public GuiGroup getGuiGroup() {
    return this.guiGroup;
  }

  /**
   * gui group shown on screen
   * @param guiGroup1
   */
  public void setGuiGroup(GuiGroup guiGroup1) {
    this.guiGroup = guiGroup1;
  }

  /**
   * if the logged in user can admin group, lazy loaded
   */
  private Boolean canAdmin;
  
  /**
   * if should show join group
   */
  private Boolean showJoinGroup;
  
  /**
   * if should show add member button
   */
  private boolean showAddMember = true;
  
  /**
   * if should show add member button
   * @return the showAddMember
   */
  public boolean isShowAddMember() {
    return this.showAddMember;
  }
  
  /**
   * if should show add member button
   * @param showAddMember1 the showAddMember to set
   */
  public void setShowAddMember(boolean showAddMember1) {
    this.showAddMember = showAddMember1;
  }


  /**
   * if shuld show join group
   * @return true if should show join group
   */
  public boolean isShowJoinGroup() {
    if (this.showJoinGroup == null) {

      final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

      this.showJoinGroup = GroupContainer.this.getGuiGroup().getGroup().canHavePrivilege(loggedInSubject, AccessPrivilege.OPTIN.getName(), false);
      
    }
    return this.showJoinGroup;
  }
  
  /**
   * if the group is a favorite for the logged in user
   */
  private Boolean favorite;

  /**
   * if the logged in user can optin 
   */
  private Boolean canOptin;
  
  /**
   * if the logged in user can optin 
   * @return is can optin
   */
  public boolean isCanOptin() {
    if (this.canOptin == null) {
      
      final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
      
      this.canOptin = (Boolean)GrouperSession.callbackGrouperSession(
          GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
            
            @Override
            public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
              return GroupContainer.this.getGuiGroup().getGroup().canHavePrivilege(loggedInSubject, AccessPrivilege.OPTIN.getName(), false);
            }
          });
    }
    return this.canOptin;
  }
  
  /**
   * can logged in subject join the current group
   * @return
   */
  public boolean isCanJoin() {
    
    // can optin or subject is in one of the workflows allowedGroupId
    if (isCanOptin()) {
      return true;
    }
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    Boolean canJoin = (Boolean)GrouperSession.callbackGrouperSession(
        GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
          
          @Override
          public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
            
            List<GrouperWorkflowConfig> workflowConfigs = GrouperWorkflowConfigService.getWorkflowConfigs(GroupContainer.this.getGuiGroup().getGroup());
            
            for (GrouperWorkflowConfig workflowConfig: workflowConfigs) {
              if (workflowConfig.canSubjectInitiateWorkflow(loggedInSubject)) {
                return true;
              }
            }
            return false;
            
          }
        });
    
    return canJoin;
  }

  /**
   * if cannot add self is enabled
   * @return true if cannot add self is enabled
   */
  public boolean isCannotAddSelfEnabled() {
    return MembershipCannotAddSelfToGroupHook.cannotAddSelfEnabled();
  }
  
  /**
   * if the current group has cannotAddSelf
   * @return is can optin
   */
  public boolean isCannotAddSelfAssignedToGroup() {
    return MembershipCannotAddSelfToGroupHook.cannotAddSelfAssignedToGroup(GroupContainer.this.getGuiGroup().getGroup());
  }

  /**
   * if the current user can assign cannotAddSelf
   * @return is can optin
   */
  public boolean isCannotAddSelfUserCanEdit() {
    return MembershipCannotAddSelfToGroupHook.cannotAddSelfUserCanEdit(this.getGuiGroup().getGroup(), GrouperUiFilter.retrieveSubjectLoggedIn());
  }

  /**
   * if the current user can assign cannotAddSelf
   * @return is can optin
   */
  public boolean isCannotAddSelfUserCanView() {
    return MembershipCannotAddSelfToGroupHook.cannotAddSelfUserCanView(this.getGuiGroup().getGroup(), GrouperUiFilter.retrieveSubjectLoggedIn());
  }

  /**
   * if the logged in user can optout
   */
  private Boolean canOptout;
  
  /**
   * if the logged in user can optout 
   * @return is can optout
   */
  public boolean isCanOptout() {
    if (this.canOptout == null) {
      
      final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
      
      this.canOptout = (Boolean)GrouperSession.callbackGrouperSession(
          GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
            
            @Override
            public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
              return GroupContainer.this.getGuiGroup().getGroup().canHavePrivilege(loggedInSubject, AccessPrivilege.OPTOUT.getName(), false);
            }
          });
    }
    return this.canOptout;
  }
  
  /**
   * if the logged in user can admin, lazy loaded
   * @return if can admin
   */
  public boolean isCanAdmin() {
    
    if (this.canAdmin == null) {
      
      final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
      
      this.canAdmin = (Boolean)GrouperSession.callbackGrouperSession(
          GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
            
            @Override
            public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
              return GroupContainer.this.getGuiGroup().getGroup().canHavePrivilege(loggedInSubject, AccessPrivilege.ADMIN.getName(), false);
            }
          });
    }
    
    return this.canAdmin;
  }

  /**
   * if direct member
   */
  private Boolean directMember;
  
  /**
   * if the logged in user is a direct member
   * @return if direct member
   */
  public boolean isDirectMember() {
    
    if (this.directMember == null) {
      
      final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
      
      this.directMember = (Boolean)GrouperSession.callbackGrouperSession(
          GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
            
            @Override
            public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
              return GroupContainer.this.getGuiGroup().getGroup().hasImmediateMember(loggedInSubject);
            }
          });
    }
    
    return this.directMember;
  }

  /**
   * if the logged in user can view group, lazy loaded
   */
  private Boolean canView;
  
  /**
   * if the logged in user can view, lazy loaded
   * @return if can view
   */
  public boolean isCanView() {
    
    if (this.canView == null) {
      
      final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
      
      this.canView = (Boolean)GrouperSession.callbackGrouperSession(
          GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
            
            @Override
            public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
              return GroupContainer.this.getGuiGroup().getGroup().canHavePrivilege(loggedInSubject, AccessPrivilege.VIEW.getName(), false);
            }
          });
    }
    
    return this.canView;
  }

  /**
   * if the logged in user can read group, lazy loaded
   */
  private Boolean canRead;
  
  /**
   * if the logged in user can read, lazy loaded
   * @return if can read
   */
  public boolean isCanRead() {
    
    if (this.canRead == null) {
      
      final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
      
      this.canRead = (Boolean)GrouperSession.callbackGrouperSession(
          GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
            
            @Override
            public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
              return GroupContainer.this.getGuiGroup().getGroup().canHavePrivilege(loggedInSubject, AccessPrivilege.READ.getName(), false);
            }
          });
    }
    
    return this.canRead;
  }

  /**
   * if the logged in user can update group, lazy loaded
   */
  private Boolean canUpdate;
  
  /**
   * if the logged in user can read attributes, lazy loaded
   */
  private Boolean canReadAttributes;
  
  /**
   * if the logged in user can update attributes, lazy loaded
   */
  private Boolean canUpdateAttributes;
  
  /**
   * keep track of the paging on the stem screen
   */
  private GuiPaging guiPaging = null;
  /**
   * subjects and what privs they have on this stem
   */
  private Set<GuiMembershipSubjectContainer> privilegeGuiMembershipSubjectContainers;
  /**
   * gui paging for privileges
   */
  private GuiPaging privilegeGuiPaging;
  /**
   * groups, stems, etc in this stem which are children, only in the current page
   */
  private Set<GuiMembershipSubjectContainer> guiMembershipSubjectContainers;

  /**
   * search results when looking for a group to add the subject to
   */
  private Set<GuiGroup> guiGroups;
  
  /**
   * if the logged in user can update, lazy loaded
   * @return if can update
   */
  public boolean isCanUpdate() {
    
    if (this.canUpdate == null) {
      
      final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
      
      this.canUpdate = (Boolean)GrouperSession.callbackGrouperSession(
          GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
            
            @Override
            public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
              return GroupContainer.this.getGuiGroup().getGroup().canHavePrivilege(loggedInSubject, AccessPrivilege.UPDATE.getName(), false);
            }
          });
    }
    
    return this.canUpdate;
  }
  
  /**
   * if the logged in user can read attributes, lazy loaded
   * @return if can update
   */
  public boolean isCanReadAttributes() {
    
    if (this.canReadAttributes == null) {
      
      final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
      
      this.canReadAttributes = (Boolean)GrouperSession.callbackGrouperSession(
          GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
            
            @Override
            public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
              return GroupContainer.this.getGuiGroup().getGroup().canHavePrivilege(loggedInSubject, AccessPrivilege.GROUP_ATTR_READ.getName(), false);
            }
          });
    }
    
    return this.canReadAttributes;
  }
  
  /**
   * if the logged in user can update attributes, lazy loaded
   * @return if can update
   */
  public boolean isCanUpdateAttributes() {
    
    if (this.canUpdateAttributes == null) {
      
      final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
      
      this.canUpdateAttributes = (Boolean)GrouperSession.callbackGrouperSession(
          GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
            
            @Override
            public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
              return GroupContainer.this.getGuiGroup().getGroup().canHavePrivilege(loggedInSubject, AccessPrivilege.GROUP_ATTR_UPDATE.getName(), false);
            }
          });
    }
    
    return this.canUpdateAttributes;
  }

  /**
   * keep track of the paging on the stem screen
   * @return the paging object, init if not there...
   */
  public GuiPaging getGuiPaging() {
    if (this.guiPaging == null) {
      this.guiPaging = new GuiPaging();
    }
    return this.guiPaging;
  }

  /**
   * subjects and what privs they have on this stem
   * @return membership subject containers
   */
  public Set<GuiMembershipSubjectContainer> getPrivilegeGuiMembershipSubjectContainers() {
    return this.privilegeGuiMembershipSubjectContainers;
  }

  /**
   * gui paging for privileges, lazy load if null
   * @return gui paging for privs
   */
  public GuiPaging getPrivilegeGuiPaging() {
    if (this.privilegeGuiPaging == null) {
      this.privilegeGuiPaging = new GuiPaging();
    }
    return this.privilegeGuiPaging;
  }

  /**
   * paging
   * @param guiPaging1
   */
  public void setGuiPaging(GuiPaging guiPaging1) {
    this.guiPaging = guiPaging1;
  }

  /**
   * clear this out to requery
   * @param privilegeGuiMembershipSubjectContainers1
   */
  public void setPrivilegeGuiMembershipSubjectContainers(
      Set<GuiMembershipSubjectContainer> privilegeGuiMembershipSubjectContainers1) {
    this.privilegeGuiMembershipSubjectContainers = privilegeGuiMembershipSubjectContainers1;
  }

  /**
   * gui paging for privileges
   * @param privilegeGuiPaging1
   */
  public void setPrivilegeGuiPaging(GuiPaging privilegeGuiPaging1) {
    this.privilegeGuiPaging = privilegeGuiPaging1;
  }

  /**
   * memberships in group
   * @return subjects and memberships
   */
  public Set<GuiMembershipSubjectContainer> getGuiMembershipSubjectContainers() {
    return this.guiMembershipSubjectContainers;
  }

  /**
   * assign the membership containers
   * @param guiMembershipSubjectContainers1
   */
  public void setGuiMembershipSubjectContainers(
      Set<GuiMembershipSubjectContainer> guiMembershipSubjectContainers1) {
    this.guiMembershipSubjectContainers = guiMembershipSubjectContainers1;
  }

  /**
   * if the group is a favorite for the logged in user
   * @return if favorite
   */
  public boolean isFavorite() {
    
    if (this.favorite == null) {
      
      final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

      this.favorite = (Boolean)GrouperSession.callbackGrouperSession(
          GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
            
            @Override
            public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
              
              Set<Group> favorites = GrouperUtil.nonNull(
                  GrouperUserDataApi.favoriteGroups(GrouperUiUserData.grouperUiGroupNameForUserData(), loggedInSubject));
              return favorites.contains(GroupContainer.this.getGuiGroup().getGroup());
                  
            }
          });
    }
    
    return this.favorite;
  }

  /**
   * search results when looking for a group to add the subject to
   * @return the gui groups
   */
  public Set<GuiGroup> getGuiGroups() {
    return this.guiGroups;
  }

  /**
   * search results when looking for a group to add the subject to
   * @param guiGroupsAddMember1
   */
  public void setGuiGroups(Set<GuiGroup> guiGroupsAddMember1) {
    this.guiGroups = guiGroupsAddMember1;
  }
  
  /**
   * attributes assigned to this group.
   */
  private Set<GuiAttributeAssign> guiAttributeAssigns;
  
  /**
   * attributes assigned to this group.
   * @return
   */
  public Set<GuiAttributeAssign> getGuiAttributeAssigns() {
    return guiAttributeAssigns;
  }
  
  /**
   * attributes assigned to this group.
   * @param guiAttributeAssigns
   */
  public void setGuiAttributeAssigns(Set<GuiAttributeAssign> guiAttributeAssigns) {
    this.guiAttributeAssigns = guiAttributeAssigns;
  }
    
  private boolean showEnabledStatus;
  
  private boolean showPointInTimeAudit;

  
  /**
   * @return the showEnabledStatus
   */
  public boolean isShowEnabledStatus() {
    return showEnabledStatus;
  }

  
  /**
   * @param showEnabledStatus the showEnabledStatus to set
   */
  public void setShowEnabledStatus(boolean showEnabledStatus) {
    this.showEnabledStatus = showEnabledStatus;
  }

  
  /**
   * @return the showPointInTimeAudit
   */
  public boolean isShowPointInTimeAudit() {
    return showPointInTimeAudit;
  }

  
  /**
   * @param showPointInTimeAudit the showPointInTimeAudit to set
   */
  public void setShowPointInTimeAudit(boolean showPointInTimeAudit) {
    this.showPointInTimeAudit = showPointInTimeAudit;
  }

  
  /**
   * @return the customCompositeUiKeys
   */
  public Map<Integer, String> getCustomCompositeUiKeys() {
    
    if (customCompositeIndexesAndUiKeys == null) {
      this.customCompositeIndexesAndUiKeys = GrouperUiUtils.getCustomCompositeUiKeys();
    }
    
    return customCompositeIndexesAndUiKeys;
  }
  
  private Set<GuiPITMembershipView> guiPITMembershipViews;

  
  /**
   * @return the guiPITMembershipViews
   */
  public Set<GuiPITMembershipView> getGuiPITMembershipViews() {
    return guiPITMembershipViews;
  }

  
  /**
   * @param guiPITMembershipViews the guiPITMembershipViews to set
   */
  public void setGuiPITMembershipViews(Set<GuiPITMembershipView> guiPITMembershipViews) {
    this.guiPITMembershipViews = guiPITMembershipViews;
  }
}
