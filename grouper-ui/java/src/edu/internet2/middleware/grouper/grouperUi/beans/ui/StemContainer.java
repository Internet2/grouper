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
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiAttributeAssign;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiMembershipSubjectContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiObjectBase;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiStem;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiPaging;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiSorting;
import edu.internet2.middleware.grouper.grouperUi.serviceLogic.UiV2Stem.StemSearchType;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUserData;
import edu.internet2.middleware.grouper.userData.GrouperUserDataApi;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * stem container in new ui
 * @author mchyzer
 *
 */
public class StemContainer {

  /**
   * if can view privilege inheritance
   * @return true if can
   */
  public boolean isCanReadPrivilegeInheritance() {

    //at least you have to be able to admin privileges on this folder
    if (!this.isCanAdminPrivileges()) {
      return false;
    }
    
    return GrouperRequestContainer.retrieveFromRequestOrCreate().getRulesContainer().isCanReadPrivilegeInheritance();
  }
  
  /**
   * if can update privilege inheritance
   * @return true if can
   */
  public boolean isCanUpdatePrivilegeInheritance() {

    //at least you have to be able to read attributes on this folder
    if (!this.isCanAdminPrivileges()) {
      return false;
    }
    
    return GrouperRequestContainer.retrieveFromRequestOrCreate().getRulesContainer().isCanUpdatePrivilegeInheritance();
  }
  
  /**
   * if show add inherited privileges
   */
  private boolean showAddInheritedPrivileges = false;
  
  /**
   * if show add inherited privileges
   * @return the showAddInheritedPrivileges
   */
  public boolean isShowAddInheritedPrivileges() {
    return this.showAddInheritedPrivileges;
  }
  
  /**
   * if show add inherited privileges
   * @param showAddInheritedPrivileges1 the showAddInheritedPrivileges to set
   */
  public void setShowAddInheritedPrivileges(boolean showAddInheritedPrivileges1) {
    this.showAddInheritedPrivileges = showAddInheritedPrivileges1;
  }

  /**
   * if show add member on the folder privileges screen
   */
  private boolean showAddMember = false;
  
  
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
   * if we have a stem id to use for e.g. create group
   */
  private String objectStemId;
  
  /**
   * if we have a stem id to use for e.g. create group
   * @return object stem id
   */
  public String getObjectStemId() {
    return this.objectStemId;
  }

  /**
   * if we have a stem id to use for e.g. create group
   * @param objectStemId1
   */
  public void setObjectStemId(String objectStemId1) {
    this.objectStemId = objectStemId1;
  }

  /**
   * instructions could be for creating groups or stems or whatever
   */
  private StemSearchType stemSearchType;
  
  /**
   * instructions could be for creating groups or stems or whatever
   * @return instructions
   */
  public StemSearchType getStemSearchType() {
    return this.stemSearchType;
  }

  /**
   * instructions could be for creating groups or stems or whatever
   * @param folderSearchResultsInstructions1
   */
  public void setStemSearchType(StemSearchType folderSearchResultsInstructions1) {
    this.stemSearchType = folderSearchResultsInstructions1;
  }

  /**
   * gui paging for privileges
   */
  private GuiPaging privilegeGuiPaging;
  
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
   * keep track of the paging on the stem screen
   */
  private GuiPaging guiPaging = null;
  
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
   * if the logged in user can create groups, lazy loaded
   */
  private Boolean canCreateGroups;
  
  /**
   * if the logged in user can read attributes, lazy loaded
   */
  private Boolean canReadAttributes;
  
  /**
   * if the logged in user can update attributes, lazy loaded
   */
  private Boolean canUpdateAttributes;
  
  /**
   * if the logged in user can create stems, lazy loaded
   */
  private Boolean canCreateStems;
  
  /**
   * if the logged in user can create groups, lazy loaded
   * @return if can admin create groups
   */
  public boolean isCanCreateGroups() {
    
    if (this.canCreateGroups == null) {
      
      final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
      
      this.canCreateGroups = (Boolean)GrouperSession.callbackGrouperSession(
          GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
            
            @Override
            public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
              return StemContainer.this.getGuiStem().getStem().canHavePrivilege(loggedInSubject, NamingPrivilege.CREATE.getName(), false);
            }
          });
      
    }
    
    return this.canCreateGroups;
  }
  
  /**
   * if the logged in user can create stems, lazy loaded
   * @return if can create folders
   */
  public boolean isCanCreateStems() {
    
    if (this.canCreateStems == null) {
      
      final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
      
      this.canCreateStems = (Boolean)GrouperSession.callbackGrouperSession(
          GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
            
            @Override
            public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
              return StemContainer.this.getGuiStem().getStem().canHavePrivilege(loggedInSubject, NamingPrivilege.CREATE.getName(), false);
            }
          });
      
    }
    
    return this.canCreateStems;
  }


  
  /**
   * if the logged in user can admin privileges, lazy loaded
   */
  private Boolean canAdminPrivileges;

  /**
   * if the logged in user can read attributes, lazy loaded
   * @return if can read attributes
   */
  public boolean isCanReadAttributes() {
    if (this.canReadAttributes == null) {
      
      final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
      
      this.canReadAttributes = (Boolean)GrouperSession.callbackGrouperSession(
          GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
            
            @Override
            public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
              return StemContainer.this.getGuiStem().getStem().canHavePrivilege(loggedInSubject, NamingPrivilege.STEM_ATTR_READ.getName(), false);
            }
          });
      
    }
    
    return this.canReadAttributes;

  }
  
  
  /**
   * if the logged in user can update attributes, lazy loaded
   * @return if can update attributes
   */
  public boolean isCanUpdateAttributes() {
    if (this.canUpdateAttributes == null) {
      
      final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
      
      this.canUpdateAttributes = (Boolean)GrouperSession.callbackGrouperSession(
          GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
            
            @Override
            public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
              return StemContainer.this.getGuiStem().getStem().canHavePrivilege(loggedInSubject, NamingPrivilege.STEM_ATTR_UPDATE.getName(), false);
            }
          });
      
    }
    
    return this.canUpdateAttributes;

  }
  
  
  /**
   * if the logged in user can admin privileges, lazy loaded
   * @return if can admin privileges
   */
  public boolean isCanAdminPrivileges() {
    
    if (this.canAdminPrivileges == null) {
      
      final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
      
      this.canAdminPrivileges = (Boolean)GrouperSession.callbackGrouperSession(
          GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
            
            @Override
            public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
              return StemContainer.this.getGuiStem().getStem().canHavePrivilege(loggedInSubject, NamingPrivilege.STEM_ADMIN.getName(), false);
            }
          });
      
    }
    
    return this.canAdminPrivileges;
  }

  /**
   * subjects and what privs they have on this stem
   */
  private Set<GuiMembershipSubjectContainer> privilegeGuiMembershipSubjectContainers;

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
   * groups, stems, etc in this stem which are children, only in the current page
   */
  private Set<GuiObjectBase> childGuiObjectsAbbreviated;

  /**
   * groups, stems, etc in this stem which are children, only in the current page
   * @return gui groups, stems, etc
   */
  public Set<GuiObjectBase> getChildGuiObjectsAbbreviated() {
    return this.childGuiObjectsAbbreviated;
  }

  /**
   * groups, stems, etc in this stem which are children, only in the current page
   * @param childGuiObjectsAbbreviated1
   */
  public void setChildGuiObjectsAbbreviated(Set<GuiObjectBase> childGuiObjectsAbbreviated1) {
    this.childGuiObjectsAbbreviated = childGuiObjectsAbbreviated1;
  }

  /**
   * gui stem shown on screen
   */
  private GuiStem guiStem;

  /**
   * if the stem is a favorite for the logged in user
   */
  private Boolean favorite;

  /**
   * filter text for the parent stem search
   */
  private String parentStemFilterText = null;

  /**
   * keep track of the paging on the parent stem search screen
   */
  private GuiPaging parentStemGuiPaging = null;

  /**
   * when searching for parent stems, these are the results
   */
  private Set<GuiStem> parentStemSearchResults = null;

  /**
   * if extended results on audit display
   */
  private boolean auditExtendedResults = false;

  /**
   * audit entries for group
   */
  private Set<GuiAuditEntry> guiAuditEntries;

  /**
   * sorting, e.g. for the audit screen
   */
  private GuiSorting guiSorting;

  /**
   * how many failures
   */
  private int failureCount;

  /**
   * how many successes
   */
  private int successCount;

  /**
   * when searching for parent stems, these are the results
   * @return stems
   */
  public Set<GuiStem> getParentStemSearchResults() {
    return this.parentStemSearchResults;
  }

  /**
   * when searching for parent stems, these are the results
   * @param parentStemSearchResults1
   */
  public void setParentStemSearchResults(Set<GuiStem> parentStemSearchResults1) {
    this.parentStemSearchResults = parentStemSearchResults1;
  }

  /**
   * when filtering parent stem list
   * @return
   */
  public String getParentStemFilterText() {
    return this.parentStemFilterText;
  }

  /**
   * when filtering parent stem list
   * @param parentStemFilterText1
   */
  public void setParentStemFilterText(String parentStemFilterText1) {
    this.parentStemFilterText = parentStemFilterText1;
  }

  /**
   * when paging parent stem list
   * @return parent stem gui paging
   */
  public GuiPaging getParentStemGuiPaging() {
    if (this.parentStemGuiPaging == null) {
      this.parentStemGuiPaging = new GuiPaging();
    }
    return this.parentStemGuiPaging;
  }

  /**
   * when paging parent stem list
   * @param parentStemGuiPaging1
   */
  public void setParentStemGuiPaging(GuiPaging parentStemGuiPaging1) {
    this.parentStemGuiPaging = parentStemGuiPaging1;
  }

  /**
   * gui stem shown on screen
   * @return stem
   */
  public GuiStem getGuiStem() {
    return this.guiStem;
  }

  /**
   * gui stem shown on screen
   * @param guiStem1
   */
  public void setGuiStem(GuiStem guiStem1) {
    this.guiStem = guiStem1;
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
              
              Set<Stem> favorites = GrouperUtil.nonNull(
                  GrouperUserDataApi.favoriteStems(GrouperUiUserData.grouperUiGroupNameForUserData(), loggedInSubject));
              return favorites.contains(StemContainer.this.getGuiStem().getStem());
                  
            }
          });
    }
    
    return this.favorite;
  }

  /**
   * audit entries for group
   * @return audit entries
   */
  public Set<GuiAuditEntry> getGuiAuditEntries() {
    return this.guiAuditEntries;
  }

  /**
   * sorting, e.g. for the audit screen
   * @return the sorting
   */
  public GuiSorting getGuiSorting() {
    return this.guiSorting;
  }

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

  /**
   * audit entries for group
   * @param guiAuditEntries1
   */
  public void setGuiAuditEntries(Set<GuiAuditEntry> guiAuditEntries1) {
    this.guiAuditEntries = guiAuditEntries1;
  }

  /**
   * sorting, e.g. for the audit screen
   * @param guiSorting1
   */
  public void setGuiSorting(GuiSorting guiSorting1) {
    this.guiSorting = guiSorting1;
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

  private GuiAttestation guiAttestation;

  public GuiAttestation getGuiAttestation() {
    return guiAttestation;
  }

  public void setGuiAttestation(GuiAttestation guiAttestation) {
    this.guiAttestation = guiAttestation;
  }
  
  /**
   * attributes assigned to this folder.
   */
  private Set<GuiAttributeAssign> guiAttributeAssigns;
  
  public Set<GuiAttributeAssign> getGuiAttributeAssigns() {
    return guiAttributeAssigns;
  }
  
  public void setGuiAttributeAssigns(Set<GuiAttributeAssign> guiAttributeAssigns) {
    this.guiAttributeAssigns = guiAttributeAssigns;
  }
  
}
