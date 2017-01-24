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

import javax.servlet.http.HttpServletRequest;

import edu.internet2.middleware.grouper.ui.GrouperUiFilter;

/**
 * request container for grouper in the j2ee request object, under 
 * the attribute name "grouperRequestContainer"
 * @author mchyzer
 *
 */
public class GrouperRequestContainer {

  /**
   * provisioning container
   */
  private ProvisioningContainer provisioningContainer;
  
  /**
   * @return the provisioningContainer
   */
  public ProvisioningContainer getProvisioningContainer() {
    if (this.provisioningContainer == null) {
      this.provisioningContainer = new ProvisioningContainer();
    }
    return this.provisioningContainer;
  }

  /**
   * @param provisioningContainer1 the provisioningContainer to set
   */
  public void setProvisioningContainer(ProvisioningContainer provisioningContainer1) {
    this.provisioningContainer = provisioningContainer1;
  }

  /**
   * container for public requests
   */
  private PublicContainer publicContainer;  

  /**
   * container for public requests
   * @return the publicContainer
   */
  public PublicContainer getPublicContainer() {
    if (this.publicContainer == null) {
      this.publicContainer = new PublicContainer();
    }
    return this.publicContainer;
  }

  /**
   * container for public requests
   * @param publicContainer1 the publicContainer to set
   */
  public void setPublicContainer(PublicContainer publicContainer1) {
    this.publicContainer = publicContainer1;
  }

  /**
   * container for inviting external users
   */
  private InviteExternalContainer inviteExternalContainer;
  
  /**
   * container for inviting external users
   * @return the inviteExternalContainer
   */
  public InviteExternalContainer getInviteExternalContainer() {
    if (this.inviteExternalContainer == null) {
      this.inviteExternalContainer = new InviteExternalContainer();
    }
    return this.inviteExternalContainer;
  }

  /**
   * container for inviting external users
   * @param inviteExternalContainer1 the inviteExternalContainer to set
   */
  public void setInviteExternalContainer(InviteExternalContainer inviteExternalContainer1) {
    this.inviteExternalContainer = inviteExternalContainer1;
  }

  /**
   * admin stuff
   */
  private AdminContainer adminContainer;
  
  /**
   * @return the adminContainer
   */
  public AdminContainer getAdminContainer() {
    if (this.adminContainer == null) {
      this.adminContainer = new AdminContainer();
    }
    return this.adminContainer;
  }

  
  /**
   * @param adminContainer1 the adminContainer to set
   */
  public void setAdminContainer(AdminContainer adminContainer1) {
    this.adminContainer = adminContainer1;
  }

  /**
   * data for importing members into groups
   */
  private GroupImportContainer groupImportContainer;

  /**
   * data for importing members into groups
   * @return group import
   */
  public GroupImportContainer getGroupImportContainer() {
    if (this.groupImportContainer == null) {
      this.groupImportContainer = new GroupImportContainer();
    }
    return this.groupImportContainer;
  }

  /**
   * data for importing members into groups
   * @param groupImportContainer1
   */
  public void setGroupImportContainer(GroupImportContainer groupImportContainer1) {
    this.groupImportContainer = groupImportContainer1;
  }

  /** 
   * attribute def container 
   */
  private AttributeDefContainer attributeDefContainer;
  
  /**
   * attribute def container 
   * @return attribute def container
   */
  public AttributeDefContainer getAttributeDefContainer() {
    if (this.attributeDefContainer == null) {
      this.attributeDefContainer = new AttributeDefContainer();
    }
    return this.attributeDefContainer;
  }
  
  /**
   * 
   * @param attributeDefContainer1
   */
  public void setAttributeDefContainer(AttributeDefContainer attributeDefContainer1) {
    this.attributeDefContainer = attributeDefContainer1;
  }

  /**
   * subject container
   */
  private SubjectContainer subjectContainer;

  /**
   * subject container lazy loaded
   * @return subject container
   */
  public SubjectContainer getSubjectContainer() {
    if (this.subjectContainer == null) {
      this.subjectContainer = new SubjectContainer();
    }
    return this.subjectContainer;
  }

  /**
   * 
   * @param subjectContainer1
   */
  public void setSubjectContainer(SubjectContainer subjectContainer1) {
    this.subjectContainer = subjectContainer1;
  }

  /**
   * common request bean
   */
  private CommonRequestContainer commonRequestContainer;
  
  /**
   * common request bean
   * @return common request bean
   */
  public CommonRequestContainer getCommonRequestContainer() {
    if (this.commonRequestContainer == null) {
      this.commonRequestContainer = new CommonRequestContainer();
    }
    return this.commonRequestContainer;
  }

  /**
   * common request bean
   * @param commonRequestBean1
   */
  public void setCommonRequestContainer(CommonRequestContainer commonRequestBean1) {
    this.commonRequestContainer = commonRequestBean1;
  }

  /**
   * current gui audit entry  being displayed
   */
  private GuiAuditEntry guiAuditEntry = null;

  /**
   * current gui audit entry  being displayed
   * @return audit
   */
  public GuiAuditEntry getGuiAuditEntry() {
    return this.guiAuditEntry;
  }

  /**
   * current gui audit entry  being displayed
   * @param guiAuditEntry1
   */
  public void setGuiAuditEntry(GuiAuditEntry guiAuditEntry1) {
    this.guiAuditEntry = guiAuditEntry1;
  }

  /**
   * retrieveFromSession, cannot be null
   * @return the app state in request scope
   */
  public static GrouperRequestContainer retrieveFromRequestOrCreate() {
    HttpServletRequest httpServletRequest = GrouperUiFilter.retrieveHttpServletRequest();
    String attributeName = "grouperRequestContainer";
    GrouperRequestContainer grouperRequestContainer = 
      (GrouperRequestContainer)httpServletRequest.getAttribute(attributeName);
    if (grouperRequestContainer == null) {
      grouperRequestContainer = new GrouperRequestContainer();
      httpServletRequest.setAttribute(attributeName, grouperRequestContainer);
    }
    return grouperRequestContainer;
  }

  /**
   * gui container for membership data
   */
  private MembershipGuiContainer membershipGuiContainer;
  
  /**
   * gui container for membership data
   * @return gui container
   */
  public MembershipGuiContainer getMembershipGuiContainer() {
    if (this.membershipGuiContainer == null) {
      this.membershipGuiContainer = new MembershipGuiContainer();
    }
    return this.membershipGuiContainer;
  }

  /**
   * gui container for membership data
   * @param guiMembershipContainer1
   */
  public void setMembershipGuiContainer(MembershipGuiContainer guiMembershipContainer1) {
    this.membershipGuiContainer = guiMembershipContainer1;
  }

  /**
   * container for my stems screens
   */
  private MyStemsContainer myStemsContainer = null;
  
  /**
   * container for my stems screens
   * @return my stems
   */
  public MyStemsContainer getMyStemsContainer() {
    if (this.myStemsContainer == null) {
      this.myStemsContainer = new MyStemsContainer();
    }
    return this.myStemsContainer;
  }

  /**
   * container for my stems screens
   * @param myStemsContainer1
   */
  public void setMyStemsContainer(MyStemsContainer myStemsContainer1) {
    this.myStemsContainer = myStemsContainer1;
  }

  /**
   * container for my groups screens
   */
  private MyGroupsContainer myGroupsContainer = null;
  
  /**
   * container for my groups screens
   * @return my groups container
   */
  public MyGroupsContainer getMyGroupsContainer() {
    if (this.myGroupsContainer == null) {
      this.myGroupsContainer = new MyGroupsContainer();
    }
    return this.myGroupsContainer;
  }

  /**
   * container for my groups screens
   * @param myGroupsContainer1
   */
  public void setMyGroupsContainer(MyGroupsContainer myGroupsContainer1) {
    this.myGroupsContainer = myGroupsContainer1;
  }

  /**
   * service container
   */
  private ServiceContainer serviceContainer;
  
  /**
   * service container lazy load if null
   * @return the serviceContainer
   */
  public ServiceContainer getServiceContainer() {
    if (this.serviceContainer == null) {
      this.serviceContainer = new ServiceContainer();
    }
    return this.serviceContainer;
  }
  
  /**
   * @param serviceContainer1 the serviceContainer to set
   */
  public void setServiceContainer(ServiceContainer serviceContainer1) {
    this.serviceContainer = serviceContainer1;
  }

  /**
   * container for index screen and general components
   */
  private IndexContainer indexContainer = null;

  /**
   * container for index screen and general components, lazy load, create if null
   * @return the index container
   */
  public IndexContainer getIndexContainer() {
    if (this.indexContainer == null) {
      this.indexContainer = new IndexContainer();
    }
    return this.indexContainer;
  }

  /**
   * container for index screen and general components
   * @param indexContainer1
   */
  public void setIndexContainer(IndexContainer indexContainer1) {
    this.indexContainer = indexContainer1;
  }
  
  /**
   * container for group screens
   */
  private GroupContainer groupContainer;
  
  /**
   * container for group screens
   * @return container for group screens
   */
  public GroupContainer getGroupContainer() {
    if (this.groupContainer == null) {
      this.groupContainer = new GroupContainer();
    }
    return this.groupContainer;
  }
  
  /**
   * container for group screens
   * @param theGroupContainer
   */
  public void setGroupContainer(GroupContainer theGroupContainer) {
    this.groupContainer = theGroupContainer;
  }
  
  /**
   * container for stem screens
   */
  private StemContainer stemContainer;
  
  /**
   * container for stem screens
   * @return container for stem screens
   */
  public StemContainer getStemContainer() {
    if (this.stemContainer == null) {
      this.stemContainer = new StemContainer();
    }
    return this.stemContainer;
  }
  
  /**
   * container for stem screens
   * @param theStemContainer
   */
  public void setStemContainer(StemContainer theStemContainer) {
    this.stemContainer = theStemContainer;
  }
  
  /**
   * container for rules
   */
  private RulesContainer rulesContainer;
  
  /**
   * container for rules screens
   * @return container for rules screens
   */
  public RulesContainer getRulesContainer() {
    if (this.rulesContainer == null) {
      this.rulesContainer = new RulesContainer();
    }
    return this.rulesContainer;
  }
  
  /**
   * container for rules screens
   * @param theRulesContainer
   */
  public void setRulesContainer(RulesContainer theRulesContainer) {
    this.rulesContainer = theRulesContainer;
  }
  
}
