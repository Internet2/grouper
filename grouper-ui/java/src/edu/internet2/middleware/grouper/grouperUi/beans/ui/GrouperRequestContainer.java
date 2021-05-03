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
import java.util.LinkedHashSet;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningDiagnosticsContainer;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiSource;
import edu.internet2.middleware.grouper.grouperUi.beans.permissionUpdate.PermissionUpdateRequestContainer;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.provider.SourceManager;

/**
 * request container for grouper in the j2ee request object, under 
 * the attribute name "grouperRequestContainer"
 * @author mchyzer
 *
 */
public class GrouperRequestContainer {

  /**
   * 
   * @return the request
   */
  public HttpServletRequest getHttpServletRequest() {
    return GrouperUiFilter.retrieveHttpServletRequest();
  }
  
  /**
   * sources
   * @return sources
   */
  public List<GuiSource> getGuiSources() {
    return new ArrayList<GuiSource>(GuiSource.convertFromSources(
        new LinkedHashSet<Source>(SourceManager.getInstance().getSources())));
  }

  /**
   * grouper loader container
   */
  private GrouperLoaderContainer grouperLoaderContainer;

  /**
   * @return the grouperLoaderContainer
   */
  public GrouperLoaderContainer getGrouperLoaderContainer() {
    if (this.grouperLoaderContainer == null) {
      this.grouperLoaderContainer = new GrouperLoaderContainer();
    }
    return this.grouperLoaderContainer;
  }

  /**
   * @param grouperLoaderContainer1 the grouperLoaderContainer to set
   */
  public void setGrouperLoaderContainer(GrouperLoaderContainer grouperLoaderContainer1) {
    this.grouperLoaderContainer = grouperLoaderContainer1;
  }

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
   * data for provisioner diagnostics
   */
  private GrouperProvisioningDiagnosticsContainer grouperProvisioningDiagnosticsContainer;

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
   * data for provisioner diagnostics
   * @return grouperProvisioningDiagnosticsContainer
   */
  public GrouperProvisioningDiagnosticsContainer getGrouperProvisioningDiagnosticsContainer() {
    if (this.grouperProvisioningDiagnosticsContainer == null) {
      throw new RuntimeException("Provisioning diagnostics container needs to be initted!");
    }
    return this.grouperProvisioningDiagnosticsContainer;
  }

  /**
   * data for provisioner diagnostics
   * @param grouperProvisioningDiagnosticsContainer
   */
  public void setGrouperProvisioningDiagnosticsContainer(
      GrouperProvisioningDiagnosticsContainer grouperProvisioningDiagnosticsContainer) {
    this.grouperProvisioningDiagnosticsContainer = grouperProvisioningDiagnosticsContainer;
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
   * attribute def name container 
   */
  private AttributeDefNameContainer attributeDefNameContainer;
  
  /**
   * attribute def name container 
   * @return attribute def name container
   */
  public AttributeDefNameContainer getAttributeDefNameContainer() {
    if (this.attributeDefNameContainer == null) {
      this.attributeDefNameContainer = new AttributeDefNameContainer();
    }
    return this.attributeDefNameContainer;
  }
  
  /**
   * @param attributeDefNameContainer1
   */
  public void setAttributeDefNameContainer(AttributeDefNameContainer attributeDefNameContainer1) {
    this.attributeDefNameContainer = attributeDefNameContainer1;
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
   * use static request container for gsh
   */
  private static ThreadLocal<Boolean> useStaticRequestContainer = new InheritableThreadLocal<Boolean>();
  
  /**
   * use static request container for gsh or testing
   * @param theUseStaticRequestContainer1
   * @return if newly assigned, otherwise false if already set to that value
   */
  public static boolean assignUseStaticRequestContainer(boolean theUseStaticRequestContainer1) {
    if (useStaticRequestContainer.get() != null && useStaticRequestContainer.get() == theUseStaticRequestContainer1) {
      return false;
    }
    useStaticRequestContainer.set(theUseStaticRequestContainer1);
    GrouperTextContainer.grouperRequestContainerThreadLocalClear();
    clearStaticRequestContainer();
    if (theUseStaticRequestContainer1) {
      GrouperTextContainer.grouperRequestContainerThreadLocalAssign(GrouperRequestContainer.retrieveFromRequestOrCreate());
    }
    return true;
  }

  /**
   * use static request container for gsh
   */
  private static ThreadLocal<GrouperRequestContainer> staticGrouperRequestContainer 
    = new InheritableThreadLocal<GrouperRequestContainer>();

  /**
   * clear out the static request container
   */
  public static void clearStaticRequestContainer() {
    staticGrouperRequestContainer.remove();
  }
  
  /**
   * retrieveFromSession, cannot be null
   * @return the app state in request scope
   */
  public static GrouperRequestContainer retrieveFromRequestOrCreate() {
    
    if (useStaticRequestContainer.get() != null && useStaticRequestContainer.get()) {
      GrouperRequestContainer grouperRequestContainer = staticGrouperRequestContainer.get();
      if (grouperRequestContainer == null) {
        grouperRequestContainer = new GrouperRequestContainer();
        staticGrouperRequestContainer.set(grouperRequestContainer);
      }
      return grouperRequestContainer;
    }
    
    HttpServletRequest httpServletRequest = GrouperUiFilter.retrieveHttpServletRequest();
    if (httpServletRequest == null) {
      throw new RuntimeException("This is not a UI environment.  Either pass the env var: GROUPER_UI=true, or set in grouper.hibernate.properties: grouper.is.ui=true");
    }
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
   * container for configuration
   */
  private ConfigurationContainer configurationContainer;

  /**
   * lazy load config container
   * @return config container
   */
  public ConfigurationContainer getConfigurationContainer() {
    if (this.configurationContainer == null) {
      this.configurationContainer = new ConfigurationContainer();
    }
    return this.configurationContainer;
  }

  /**
   * custom ui container
   */
  private CustomUiContainer customUiContainer;
  
  
  /**
   * custom ui container
   * @return the groupLiteContainer
   */
  public CustomUiContainer getCustomUiContainer() {
    if (this.customUiContainer == null) {
      this.customUiContainer = new CustomUiContainer();
    }
    return this.customUiContainer;
  }

  
  /**
   * custom ui container
   * @param groupLiteContainer1 the groupLiteContainer to set
   */
  public void setCustomUiContainer(CustomUiContainer groupLiteContainer1) {
    this.customUiContainer = groupLiteContainer1;
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
   * container for stem delete screens
   */
  private StemDeleteContainer stemDeleteContainer;
  
  /**
   * container for stem delete screens
   * @return container for stem delete screens
   */
  public StemDeleteContainer getStemDeleteContainer() {
    if (this.stemDeleteContainer == null) {
      this.stemDeleteContainer = new StemDeleteContainer();
    }
    return this.stemDeleteContainer;
  }
  
  /**
   * container for stem delete screens
   * @param theStemDeleteContainer
   */
  public void setStemDeleteContainer(StemDeleteContainer theStemDeleteContainer) {
    this.stemDeleteContainer = theStemDeleteContainer;
  }
  
  /**
   * container for rules
   */
  private RulesContainer rulesContainer;
  
  /**
   * container for deprovisioning users
   */
  private DeprovisioningContainer deprovisioningContainer;

  /**
   * container for attestation screen
   */
  private AttestationContainer attestationContainer;
  
  /**
   * container for template screen
   */
  private StemTemplateContainer stemTemplateContainer;
  
  /**
   * container for groups/stems object types
   */
  private ObjectTypeContainer objectTypeContainer;
  
  /**
   * container for groups workflow
   */
  private WorkflowContainer workflowContainer;
  
  
  /**
   * container for grouper reports
   */
  private GrouperReportContainer grouperReportContainer; 
  
  /**
   * container for subject resolution
   */
  private SubjectResolutionContainer subjectResolutionContainer;
  
  /**
   * container for subject sources
   */
  private SubjectSourceContainer subjectSourceContainer;
  
  /**
   * container for external systems
   */
  private ExternalSystemContainer externalSystemContainer;
  
  /**
   * container for external systems
   * @return the container
   */
  public ExternalSystemContainer getExternalSystemContainer() {
    
    if (this.externalSystemContainer == null) {
      this.externalSystemContainer = new ExternalSystemContainer();
    }
    
    return this.externalSystemContainer;
  }
  
  /**
   * container for gsh templates
   */
  private GshTemplateContainer gshTemplateContainer;
  
  /**
   * container for gsh templates
   * @return the container
   */
  public GshTemplateContainer getGshTemplateContainer() {
    if (this.gshTemplateContainer == null) {
      this.gshTemplateContainer = new GshTemplateContainer();
    }
    
    return this.gshTemplateContainer;
  }

  /**
   * container for provisioner configuration
   */
  private ProvisionerConfigurationContainer provisionerConfigurationContainer;
  
  /**
   * container for provisioner configuration
   * @return the container
   */
  public ProvisionerConfigurationContainer getProvisionerConfigurationContainer() {
    
    if (this.provisionerConfigurationContainer == null) {
      this.provisionerConfigurationContainer = new ProvisionerConfigurationContainer();
    }
    
    return this.provisionerConfigurationContainer;
  }

  /**
   * container for deprovisioning screen
   * @return the container
   */
  public DeprovisioningContainer getDeprovisioningContainer() {
    if (this.deprovisioningContainer == null) {
      this.deprovisioningContainer = new DeprovisioningContainer();
    }
    return this.deprovisioningContainer;
  }

  /**
   * lazy load the attestation container
   * @return the attestation container
   */
  public AttestationContainer getAttestationContainer() {
    if (this.attestationContainer == null) {
      this.attestationContainer = new AttestationContainer();
    }
    return this.attestationContainer;
  }
  
  /**
   * lazy load the template container
   * @return the template container
   */
  public StemTemplateContainer getStemTemplateContainer() {
    if (this.stemTemplateContainer == null) {
      this.stemTemplateContainer = new StemTemplateContainer();
    }
    return this.stemTemplateContainer;
  }
  
  /**
   * lazy load the object type container
   * @return the object type container
   */
  public ObjectTypeContainer getObjectTypeContainer() {
    if (this.objectTypeContainer == null) {
      this.objectTypeContainer = new ObjectTypeContainer();
    }
    return this.objectTypeContainer;
  }
  
  /**
   * lazy load the workflow container
   * @return the workflow container
   */
  public WorkflowContainer getWorkflowContainer() {
    if (this.workflowContainer == null) {
      this.workflowContainer = new WorkflowContainer();
    }
    return this.workflowContainer;
  }

  /** lazy load the grouper report container
   * @return the grouper report container
   */
  public GrouperReportContainer getGrouperReportContainer() {
    if (this.grouperReportContainer == null) {
      this.grouperReportContainer = new GrouperReportContainer();
    }
    return this.grouperReportContainer;
  }

  
  /**
   * lazy load the subject resolution container
   * @return
   */
  public SubjectResolutionContainer getSubjectResolutionContainer() {
    if (this.subjectResolutionContainer == null) {
      this.subjectResolutionContainer = new SubjectResolutionContainer();
    }
    return this.subjectResolutionContainer;
  }
  
  public SubjectSourceContainer getSubjectSourceContainer() {
    if (this.subjectSourceContainer == null) {
      this.subjectSourceContainer = new SubjectSourceContainer();
    }
    return this.subjectSourceContainer;
  }

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
  
  /**
   * container for permission screen
   */
  private PermissionContainer permissionContainer;

  /**
   * lazy load the permission container
   * @return container for permission screen
   */
  public PermissionContainer getPermissionContainer() {
    if (this.permissionContainer == null) {
      this.permissionContainer = new PermissionContainer();
    }
    return this.permissionContainer;
  }
  
  private PermissionUpdateRequestContainer permissionUpdateRequestContainer;
  
  public PermissionUpdateRequestContainer getPermissionUpdateRequestContainer() {
    
    if (this.permissionUpdateRequestContainer == null) {
      this.permissionUpdateRequestContainer = new PermissionUpdateRequestContainer();
    }
    return permissionUpdateRequestContainer;
  }
  
  /**
   * container for role inheritance screens
   */
  private RoleInheritanceContainer roleInheritanceContainer;

  /**
   * lazy load the role inheritance container
   * @return
   */
  public RoleInheritanceContainer getRoleInheritanceContainer() {
    
    if (this.roleInheritanceContainer == null) {
      this.roleInheritanceContainer = new RoleInheritanceContainer();
    }
    return roleInheritanceContainer;
  }

  /**
   * container for visualization screen
   */
  private VisualizationContainer visualizationContainer;

  /**
   * lazy load the visualization container
   * @return the visualization container
   */
  public VisualizationContainer getVisualizationContainer() {
    if (this.visualizationContainer == null) {
      this.visualizationContainer = new VisualizationContainer();
    }
    return this.visualizationContainer;
  }

}
