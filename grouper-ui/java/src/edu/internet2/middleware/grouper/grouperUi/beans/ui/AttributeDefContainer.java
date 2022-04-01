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

import java.util.Set;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefScope;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignAction;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiAttributeAssign;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiAttributeAssignFinderResults;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiAttributeDef;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiAttributeDefName;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiMembershipSubjectContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiPaging;
import edu.internet2.middleware.grouper.grouperUi.serviceLogic.UiV2AttributeDef;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUserData;
import edu.internet2.middleware.grouper.userData.GrouperUserDataApi;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 * attribute definition container in new ui
 * @author mchyzer
 */
public class AttributeDefContainer {

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
 
   /**
    * actions associated with this attribute def
    */
   private Set<AttributeAssignAction> attributeAssignActions;
   
  /**
   * actions associated with this attribute def
   * @return
   */
  public Set<AttributeAssignAction> getAttributeAssignActions() {
    return attributeAssignActions;
  }

  /**
   * actions associated with this attribute def
   * @param attributeAssignActions1
   */
  public void setAttributeAssignActions(Set<AttributeAssignAction> attributeAssignActions1) {
    this.attributeAssignActions = attributeAssignActions1;
  }

  /**
   * gui attribute def names e.g. results for attribute def name list on attribute def tab
   */
  private Set<GuiAttributeDefName> guiAttributeDefNames;
  
  /**
   * gui attribute def names e.g. results for attribute def name list on attribute def tab
   * @return the guiAttributeDefNames
   */
  public Set<GuiAttributeDefName> getGuiAttributeDefNames() {
    return this.guiAttributeDefNames;
  }
  
  /**
   * gui attribute def names e.g. results for attribute def name list on attribute def tab
   * @param guiAttributeDefNames1 the guiAttributeDefNames to set
   */
  public void setGuiAttributeDefNames(Set<GuiAttributeDefName> guiAttributeDefNames1) {
    this.guiAttributeDefNames = guiAttributeDefNames1;
  }

  /**
   * search gui attribute def results
   */
  private Set<GuiAttributeDef> guiAttributeDefSearchResults;

  /**
   * search gui attribute def results
   * @return the guiAttributeDefSearchResults
   */
  public Set<GuiAttributeDef> getGuiAttributeDefSearchResults() {
    return this.guiAttributeDefSearchResults;
  }
  
  /**
   * search gui attribute def results
   * @param guiAttributeDefSearchResults1 the guiAttributeDefSearchResults to set
   */
  public void setGuiAttributeDefSearchResults(
      Set<GuiAttributeDef> guiAttributeDefSearchResults1) {
    this.guiAttributeDefSearchResults = guiAttributeDefSearchResults1;
  }
  
  /**
   * if we have attribute def id to use for e.g. create attribute def name
   */
  private String objectAttributeDefId;
  
  /**
   * if we have a attribute def id to use for e.g. create attribute def name
   * @return object attribute def id
   */
  public String getObjectAttributeDefId() {
    return this.objectAttributeDefId;
  }

  /**
   * if we have attribute def id to use for e.g. create attribute def name
   * @param objectStemId1
   */
  public void setObjectAttributeDefId(String objectAttributeDefId1) {
    this.objectAttributeDefId = objectAttributeDefId1;
  }

  /**
   * gui attribute def from url
   */
  private GuiAttributeDef guiAttributeDef;
  
  /**
   * if the logged in user can admin group, lazy loaded
   */
  private Boolean canAdmin;
  /**
   * if the logged in user can read group, lazy loaded
   */
  private Boolean canRead;
  /**
   * if the logged in user can update group, lazy loaded
   */
  private Boolean canUpdate;
  /**
   * if the logged in user can view group, lazy loaded
   */
  private Boolean canView;
  /**
   * if show add member on the folder privileges screen
   */
  private boolean showAddMember = false;

  /**
   * keep track of the paging on the stem screen
   */
  private GuiPaging guiPaging = null;

  /**
   * subjects and what privs they have on this stem
   */
  private Set<GuiMembershipSubjectContainer> privilegeGuiMembershipSubjectContainers;

  /**
   * how many failures
   */
  private int failureCount;

  /**
   * how many successes
   */
  private int successCount;

  /**
   * gui paging for privileges
   */
  private GuiPaging privilegeGuiPaging;

  /**
   * if the stem is a favorite for the logged in user
   */
  private Boolean favorite;

  /**
   * gui attribute def from url
   * @return gui attribute def
   */
  public GuiAttributeDef getGuiAttributeDef() {
    return this.guiAttributeDef;
  }

  /**
   * gui attribute def from url
   * @param guiAttributeDef1
   */
  public void setGuiAttributeDef(GuiAttributeDef guiAttributeDef1) {
    this.guiAttributeDef = guiAttributeDef1;
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
              return AttributeDefContainer.this.getGuiAttributeDef().getAttributeDef().getPrivilegeDelegate().canHavePrivilege(loggedInSubject, AttributeDefPrivilege.ATTR_ADMIN.getName(), false);
            }
          });
    }
    
    return this.canAdmin;
  }

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
              return AttributeDefContainer.this.getGuiAttributeDef().getAttributeDef().getPrivilegeDelegate().canHavePrivilege(loggedInSubject, AttributeDefPrivilege.ATTR_READ.getName(), false);
            }
          });
    }
    
    return this.canRead;
  }

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
              return AttributeDefContainer.this.getGuiAttributeDef().getAttributeDef().getPrivilegeDelegate().canHavePrivilege(loggedInSubject, AttributeDefPrivilege.ATTR_UPDATE.getName(), false);
            }
          });
    }
    
    return this.canUpdate;
  }

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
              return AttributeDefContainer.this.getGuiAttributeDef().getAttributeDef().getPrivilegeDelegate().canHavePrivilege(loggedInSubject, AttributeDefPrivilege.ATTR_VIEW.getName(), false);
            }
          });
    }
    
    return this.canView;
  }

  /**
   * if show add member on the folder privileges screen
   * @return the showAddMember
   */
  public boolean isShowAddMember() {
    return this.showAddMember;
  }

  /**
   * if show add member on the folder privileges screen
   * @param showAddMember1 the showAddMember to set
   */
  public void setShowAddMember(boolean showAddMember1) {
    this.showAddMember = showAddMember1;
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
   * gui paging
   * @param guiPaging1
   */
  public void setGuiPaging(GuiPaging guiPaging1) {
    this.guiPaging = guiPaging1;
  }

  /**
   * subjects and what privs they have on this stem
   * @return membership subject containers
   */
  public Set<GuiMembershipSubjectContainer> getPrivilegeGuiMembershipSubjectContainers() {
    return this.privilegeGuiMembershipSubjectContainers;
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
   * how many failures
   * @return failures
   */
  public int getFailureCount() {
    return this.failureCount;
  }

  /**
   * how many successes
   * @return successes
   */
  public int getSuccessCount() {
    return this.successCount;
  }

  /**
   * how many failures
   * @param failuresCount1
   */
  public void setFailureCount(int failuresCount1) {
    this.failureCount = failuresCount1;
  }

  /**
   * how many successes
   * @param successCount1
   */
  public void setSuccessCount(int successCount1) {
    this.successCount = successCount1;
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
   * gui paging for privileges
   * @param privilegeGuiPaging1
   */
  public void setPrivilegeGuiPaging(GuiPaging privilegeGuiPaging1) {
    this.privilegeGuiPaging = privilegeGuiPaging1;
  }

  /**
   * if the stem is a favorite for the logged in user
   * @return if favorite
   */
  public boolean isFavorite() {
    
    if (this.favorite == null) {
      
      final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
      this.favorite = (Boolean)GrouperSession.callbackGrouperSession(
          GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
            
            @Override
            public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
              
              Set<AttributeDef> favorites = GrouperUtil.nonNull(
                  GrouperUserDataApi.favoriteAttributeDefs(GrouperUiUserData.grouperUiGroupNameForUserData(), loggedInSubject));
              return favorites.contains(AttributeDefContainer.this.getGuiAttributeDef().getAttributeDef());
                  
            }
          });
    }
    
    return this.favorite;
  }

  /**
   * if entities get admin when added to a group
   * @return true if entities get admin when added to a group
   */
  public boolean isConfigDefaultAttributeDefsCreateGrantAllAdmin() {
    return GrouperConfig.retrieveConfig()
        .propertyValueBoolean("attributeDefs.create.grant.all.attrAdmin", false);
  }

  /**
   * if entities get attrRead when added to a group
   * @return true if entities get attrRead when added to a group
   */
  public boolean isConfigDefaultAttributeDefsCreateGrantAllAttrRead() {
    return GrouperConfig.retrieveConfig()
        .propertyValueBoolean("attributeDefs.create.grant.all.attrDefAttrRead", false);
  }

  /**
   * if entities get attrUpdate when added to a group
   * @return true if entities get attrUpdate when added to a group
   */
  public boolean isConfigDefaultAttributeDefsCreateGrantAllAttrUpdate() {
    return GrouperConfig.retrieveConfig()
        .propertyValueBoolean("attributeDefs.create.grant.all.attrDefAttrUpdate", false);
  }

  /**
   * if entities get optin when added to a group
   * @return true if entities get optin when added to a group
   */
  public boolean isConfigDefaultAttributeDefsCreateGrantAllOptin() {
    return GrouperConfig.retrieveConfig()
        .propertyValueBoolean("attributeDefs.create.grant.all.attrOptin", false);
  }

  /**
   * if entities get optout when added to a group
   * @return true if entities get optout when added to a group
   */
  public boolean isConfigDefaultAttributeDefsCreateGrantAllOptout() {
    return GrouperConfig.retrieveConfig()
        .propertyValueBoolean("attributeDefs.create.grant.all.attrOptout", false);
  }

  /**
   * if entities get read when added to a group
   * @return true if entities get read when added to a group
   */
  public boolean isConfigDefaultAttributeDefsCreateGrantAllRead() {
    return GrouperConfig.retrieveConfig()
        .propertyValueBoolean("attributeDefs.create.grant.all.attrRead", false);
  }

  /**
   * if entities get update when added to a group
   * @return true if entities get update when added to a group
   */
  public boolean isConfigDefaultAttributeDefsCreateGrantAllUpdate() {
    return GrouperConfig.retrieveConfig()
        .propertyValueBoolean("attributeDefs.create.grant.all.attrUpdate", false);
  }

  /**
   * if entities get view when added to a group
   * @return true if entities get view when added to a group
   */
  public boolean isConfigDefaultAttributeDefsCreateGrantAllView() {
    return GrouperConfig.retrieveConfig()
        .propertyValueBoolean("attributeDefs.create.grant.all.attrView", false);
  }
  
  /**
   * attributes assigned to this attribute def.
   */
  private Set<GuiAttributeAssign> guiAttributeAssigns;
  
  /**
   * attributes assigned to this attribute def.
   * @return attributes assigned to this attribute def.
   */
  public Set<GuiAttributeAssign> getGuiAttributeAssigns() {
    return guiAttributeAssigns;
  }
  
  /**
   * attributes assigned to this attribute def.
   * @param guiAttributeAssigns
   */
  public void setGuiAttributeAssigns(Set<GuiAttributeAssign> guiAttributeAssigns) {
    this.guiAttributeAssigns = guiAttributeAssigns;
  }
  
  /**
   * owners of this attribute def
   */
  private GuiAttributeAssignFinderResults guiAttributeAssignFinderResults;

  private AttributeDefScope attributeDefScope;

  private boolean showAttributeDefMarkerSection;

  /**
   * owners of this attribute def
   * @return
   */
  public GuiAttributeAssignFinderResults getGuiAttributeAssignFinderResults() {
    return guiAttributeAssignFinderResults;
  }

  /**
   * owners of this attribute def
   * @param guiAttributeAssignFinderResults
   */
  public void setGuiAttributeAssignFinderResults(GuiAttributeAssignFinderResults guiAttributeAssignFinderResults) {
    this.guiAttributeAssignFinderResults = guiAttributeAssignFinderResults;
  }

    /**
     * if deletion of an attribute def is disallowed from the UI
     * @return true if UI does not allow deleting of an attributeDef
     */
    public boolean isConfigPreventUiDeletion() {
        return GrouperUiConfig.retrieveConfig()
                .propertyValueBoolean(UiV2AttributeDef.PROPERTY_PREVENT_DELETE_IN_UI, false);
    }

    /**
     * set marker attribute def scope.
     * @param attributeDefScope
     */
    public void setAttributeDefScope(AttributeDefScope attributeDefScope) {
      this.attributeDefScope = attributeDefScope;
    }

    /**
     * get marker attribute def scope
     * @return
     */
    public AttributeDefScope getAttributeDefScope() {
      return this.attributeDefScope;
    }

    public void setShowAttributeDefMarkerSection(boolean showAttributeDefMarkerSection) {
      this.showAttributeDefMarkerSection = showAttributeDefMarkerSection;
    }

    
    public boolean isShowAttributeDefMarkerSection() {
      return showAttributeDefMarkerSection;
    }
    
    
}
