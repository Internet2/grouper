package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeNames;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeValue;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningJob;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningObjectMetadata;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningObjectMetadataItem;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningService;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningSettings;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTarget;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.audit.AuditTypeBuiltin;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.ProvisioningMessage;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiObjectBase;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiStem;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiSubject;
import edu.internet2.middleware.grouper.grouperUi.beans.api.provisioning.GuiGrouperProvisioningAttributeValue;
import edu.internet2.middleware.grouper.grouperUi.beans.api.provisioning.GuiGrouperSyncObject;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiPaging;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction.GuiMessageType;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.ProvisioningContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.tags.GrouperPagingTag2;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncDao;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncLog;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMembership;
import edu.internet2.middleware.subject.Subject;

public class UiV2Provisioning {
  
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(UiV2Provisioning.class);
  
  /**
   * view provisioning settings for a folder
   * @param request
   * @param response
   */
  public void viewProvisioningOnFolder(final HttpServletRequest request, final HttpServletResponse response) {

    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Stem stem = null;
    
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      stem = UiV2Stem.retrieveStemHelper(request, true).getStem();
     
      if (stem == null) {
        return;
      }
      final Stem STEM = stem;
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
            
      if (!PrivilegeHelper.isWheelOrRoot(loggedInSubject)) {
        throw new RuntimeException("Cannot access provisioning.");
      }
      
      final ProvisioningContainer provisioningContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getProvisioningContainer();
      
      //switch over to admin so attributes work
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          if (!checkProvisioning()) {
            return null;
          }
            
          // we need to show assigned + non assigned both.
          List<GrouperProvisioningAttributeValue> allProvisioningAttributeValues = new ArrayList<GrouperProvisioningAttributeValue>();
          
          // add ones that are already assigned
          List<GrouperProvisioningAttributeValue> attributeValuesForStem = GrouperProvisioningService.getProvisioningAttributeValues(STEM);
          allProvisioningAttributeValues.addAll(attributeValuesForStem);
          
          // add ones that are not assigned
          List<String> allTargetNames = new ArrayList<String>(GrouperProvisioningSettings.getTargets(true).keySet());
          
          // remove target names that are already configured
          for (GrouperProvisioningAttributeValue attributeValue: attributeValuesForStem) {
            
            String targetAlreadyConfigured = attributeValue.getTargetName();
            if (GrouperProvisioningSettings.getTargets(true).containsKey(targetAlreadyConfigured)) {
              allTargetNames.remove(targetAlreadyConfigured);
            }
          }
          
          //allTargetNames now only contains that are not configured
          for (String targetNotConfigured: allTargetNames) {
            GrouperProvisioningAttributeValue notConfiguredAttributeValue = new GrouperProvisioningAttributeValue();
            notConfiguredAttributeValue.setTargetName(GrouperProvisioningSettings.getTargets(true).get(targetNotConfigured).getName());
            notConfiguredAttributeValue.setDoProvision(false);
            allProvisioningAttributeValues.add(notConfiguredAttributeValue);
          }
          
          // convert from raw to gui
          List<GuiGrouperProvisioningAttributeValue> guiGrouperProvisioningAttributeValues = GuiGrouperProvisioningAttributeValue.convertFromGrouperProvisioningAttributeValues(allProvisioningAttributeValues);
          
          
          for (GuiGrouperProvisioningAttributeValue guiGrouperProvisioningAttributeValue: guiGrouperProvisioningAttributeValues) {
            
            GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner(guiGrouperProvisioningAttributeValue.getGrouperProvisioningAttributeValue().getTargetName());
            GrouperProvisioningObjectMetadata grouperProvisioningObjectMetadata = grouperProvisioner.retrieveGrouperProvisioningObjectMetadata();
            
            List<GrouperProvisioningObjectMetadataItem> itemsToShow = new ArrayList<GrouperProvisioningObjectMetadataItem>();
            
            for (GrouperProvisioningObjectMetadataItem metadataItem: grouperProvisioningObjectMetadata.getGrouperProvisioningObjectMetadataItems()) {
              
              if (guiGrouperProvisioningAttributeValue.getGrouperProvisioningAttributeValue()
                    .getMetadataNameValues().containsKey(metadataItem.getName())) {
                Object value = guiGrouperProvisioningAttributeValue.getGrouperProvisioningAttributeValue()
                    .getMetadataNameValues().get(metadataItem.getName());
                metadataItem.setDefaultValue(value);
                itemsToShow.add(metadataItem);
              }
              
            }
            
            guiGrouperProvisioningAttributeValue.setMetadataItems(itemsToShow);
            
          }
          
          provisioningContainer.setGuiGrouperProvisioningAttributeValues(guiGrouperProvisioningAttributeValues);
          
          GuiStem guiStem = GrouperRequestContainer.retrieveFromRequestOrCreate().getStemContainer().getGuiStem();
          addProvisioningBreadcrumbs(guiStem, null, null, null, null);
          
          guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
              "/WEB-INF/grouperUi2/provisioning/provisioningFolderSettingsView.jsp"));
          
          return null;
        }
      });
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  private void addProvisioningBreadcrumbs(GuiObjectBase guiObjectBase, String targetName, String methodName,
      String keyForObjectIdentifier, String valueForObjectIdentifier) {
    
    String provisioningBreadcrumb = TextContainer.retrieveFromRequest().getText().get("guiBreadcrumbsProvisioningLabel");
    StringBuilder bullets = new StringBuilder();

    if (targetName == null) {
      bullets.append("<li class='active'>" + provisioningBreadcrumb + "</li>");
    } else {
      bullets.append("<li><a href=\"#\" onclick=\"return guiV2link('operation=UiV2Provisioning.");
      
      bullets.append(methodName);
      bullets.append("&");
      bullets.append(keyForObjectIdentifier);
      bullets.append("=");
      bullets.append(valueForObjectIdentifier);
      bullets.append("');\">");
      bullets.append(provisioningBreadcrumb);
      bullets.append("</a>");
      bullets.append("<span class=\"divider\"><i class='fa fa-angle-right'></i></span>");
      bullets.append("</li>");
      
      bullets.append("<li class='active'>" + targetName + "</li>");
    }
    
    guiObjectBase.setShowBreadcrumbLink(true);
    guiObjectBase.setAdditionalBreadcrumbBullets(bullets.toString());
  }
  
  /**
   * view provisioner configuration for a group
   * @param request
   * @param response
   */
  public void viewProvisioningConfigurationOnGroup(final HttpServletRequest request, final HttpServletResponse response) {

    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Group group = null;
    
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.VIEW).getGroup();
     
      if (group == null) {
        return;
      }
      
      if (!PrivilegeHelper.isWheelOrRoot(loggedInSubject)) {
        throw new RuntimeException("Cannot access provisioning.");
      }
      
      final Group GROUP = group;
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      final String targetName = request.getParameter("provisioningTargetName");
      
      if (StringUtils.isBlank(targetName)) {
        throw new RuntimeException("provisioningTargetName cannot be blank");
      }
            
      //switch over to admin so attributes work
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          if (!checkProvisioning()) {
            return null;
          }
            
          setGrouperProvisioningAttributeValues(GROUP, targetName);
          
          GuiGroup guiGroup = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup();
          addProvisioningBreadcrumbs(guiGroup, targetName, "viewProvisioningOnGroup", "groupId", GROUP.getId());
          
          guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
            "/WEB-INF/grouperUi2/provisioning/provisioningGroupSettingsView.jsp"));
          
          return null;
        }
      });
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * view provisioners configured for a group
   * @param request
   * @param response
   */
  public void viewProvisioningOnGroup(final HttpServletRequest request, final HttpServletResponse response) {

    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Group group = null;
    
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.VIEW).getGroup();
     
      if (group == null) {
        return;
      }
      
      if (!PrivilegeHelper.isWheelOrRoot(loggedInSubject)) {
        throw new RuntimeException("Cannot access provisioning.");
      }
      
      final Group GROUP = group;
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
            
      //switch over to admin so attributes work
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          if (!checkProvisioning()) {
            return null;
          }
            
          setGrouperProvisioningAttributeValues(GROUP, null);
          
          GuiGroup guiGroup = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup();
          addProvisioningBreadcrumbs(guiGroup, null, null, null, null);
          
          guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
            "/WEB-INF/grouperUi2/provisioning/provisioningGroupProvisioners.jsp"));
          
          return null;
        }
      });
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * view provisioning settings for a subject
   * @param request
   * @param response
   */
  public void viewProvisioningOnSubject(final HttpServletRequest request, final HttpServletResponse response) {

    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Subject subject = null;
    
    try {
  
      if (!PrivilegeHelper.isWheelOrRoot(loggedInSubject)) {
        throw new RuntimeException("Cannot access provisioning.");
      }
      
      grouperSession = GrouperSession.start(loggedInSubject);
  
      subject = UiV2Subject.retrieveSubjectHelper(request, true);

      if (subject == null) {
        return;
      }
      
      final Subject SUBJECT = subject;
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
            
      final ProvisioningContainer provisioningContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getProvisioningContainer();
      
      //switch over to admin so attributes work
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          if (!checkProvisioning()) {
            return null;
          }
          
          Member member = MemberFinder.findBySubject(theGrouperSession, SUBJECT, true);
          List<GcGrouperSyncMember> gcGrouperSyncMembers = GrouperProvisioningService.retrieveGcGrouperSyncMembers(member.getId());
          
          Map<String, List<GuiGrouperSyncObject>> provisionerNameToGuiGrouperSyncObject = new HashMap<String, List<GuiGrouperSyncObject>>();
          
          for (GcGrouperSyncMember gcGrouperSyncMember: gcGrouperSyncMembers) {
            
            String targetName = gcGrouperSyncMember.getGrouperSync().getProvisionerName();

            GuiGrouperSyncObject guiGrouperSyncObject = new GuiGrouperSyncObject();
            guiGrouperSyncObject.setGcGrouperSyncMember(gcGrouperSyncMember);
            guiGrouperSyncObject.setTargetName(targetName);
            
            List<GuiGrouperSyncObject> guiGrouperSyncObjects = provisionerNameToGuiGrouperSyncObject.getOrDefault(targetName, new ArrayList<GuiGrouperSyncObject>());
            guiGrouperSyncObjects.add(guiGrouperSyncObject);
            provisionerNameToGuiGrouperSyncObject.put(targetName, guiGrouperSyncObjects);
            
          }
          
          List<GrouperProvisioningAttributeValue> provisioningAttributeValues = GrouperProvisioningService.getProvisioningAttributeValues(member);
          
          for (GrouperProvisioningAttributeValue grouperProvisioningAttributeValue: provisioningAttributeValues) {
            
            Map<String, Object> metadataNameValues = grouperProvisioningAttributeValue.getMetadataNameValues();
            if (metadataNameValues != null && metadataNameValues.size() > 0) {
              String targetName = grouperProvisioningAttributeValue.getTargetName();
              if (provisionerNameToGuiGrouperSyncObject.containsKey(targetName)) {
                List<GuiGrouperSyncObject> guiGrouperSyncObjects = provisionerNameToGuiGrouperSyncObject.get(targetName);
                for (GuiGrouperSyncObject guiGrouperSyncObject: guiGrouperSyncObjects) {
                  guiGrouperSyncObject.setHasDirectSettings(true);
                }
              } else {
                GuiGrouperSyncObject guiGrouperSyncObject = new GuiGrouperSyncObject();
                guiGrouperSyncObject.setTargetName(targetName);
                guiGrouperSyncObject.setHasDirectSettings(true);
                
                List<GuiGrouperSyncObject> guiGrouperSyncObjects = new ArrayList<GuiGrouperSyncObject>();
                guiGrouperSyncObjects.add(guiGrouperSyncObject);
                
                provisionerNameToGuiGrouperSyncObject.put(targetName, guiGrouperSyncObjects);
              }
              
            }
          }
          
          Collection<List<GuiGrouperSyncObject>> listOfListOfGrouperSyncObjects = provisionerNameToGuiGrouperSyncObject.values();
          List<GuiGrouperSyncObject> guiGrouperSyncObjects = listOfListOfGrouperSyncObjects.stream().flatMap(List::stream).collect(Collectors.toList());
          
          provisioningContainer.setGuiGrouperSyncObjects(guiGrouperSyncObjects);
          
          GuiSubject guiSubject = GrouperRequestContainer.retrieveFromRequestOrCreate().getSubjectContainer().getGuiSubject();
          addProvisioningBreadcrumbs(guiSubject, null, null, null, null);
          
          guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
              "/WEB-INF/grouperUi2/provisioning/provisioningSubjectProvisionersTable.jsp"));
          
          return null;
        }
      });
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * view details of a single grouper_sync_member table for a subject from subject screen
   * @param request
   * @param response
   */
  public void viewProvisioningTargetDetailsOnSubject(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final Subject subject = UiV2Subject.retrieveSubjectHelper(request, true);

      if (subject == null) {
        return;
      }
      
      if (!PrivilegeHelper.isWheelOrRoot(loggedInSubject)) {
        throw new RuntimeException("Cannot access provisioning.");
      }
      
      final String targetName = request.getParameter("provisioningTargetName");
      
      if (StringUtils.isBlank(targetName)) {
        throw new RuntimeException("provisioningTargetName cannot be blank");
      }

      final String groupSyncMemberId = request.getParameter("groupSyncMemberId");
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
            
      final ProvisioningContainer provisioningContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getProvisioningContainer();
      
      //switch over to admin so attributes work
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          if (!checkProvisioning()) {
            return null;
          }

          GuiGrouperSyncObject guiGrouperSyncObject = new GuiGrouperSyncObject();
          guiGrouperSyncObject.setTargetName(targetName);
          
          if (StringUtils.isNotBlank(groupSyncMemberId)) {       
            GcGrouperSyncMember gcGrouperSyncMember = GcGrouperSyncDao.retrieveByProvisionerName(null, targetName).getGcGrouperSyncMemberDao().memberRetrieveById(groupSyncMemberId);
            guiGrouperSyncObject.setGcGrouperSyncMember(gcGrouperSyncMember);
          }

          List<GrouperProvisioningObjectMetadataItem> provisioningObjectMetadataItems = GrouperProvisioner.retrieveProvisioner(targetName).retrieveGrouperProvisioningObjectMetadata().getGrouperProvisioningObjectMetadataItems();
          List<GrouperProvisioningObjectMetadataItem> itemsToShow = new ArrayList<GrouperProvisioningObjectMetadataItem>();
          
          Member member = MemberFinder.findBySubject(theGrouperSession, subject, true);
          GrouperProvisioningAttributeValue provisioningAttributeValue = GrouperProvisioningService.getProvisioningAttributeValue(member, targetName);
          if (provisioningAttributeValue != null && provisioningAttributeValue.getMetadataNameValues() != null &&
            provisioningAttributeValue.getMetadataNameValues().size() > 0) {
            
            guiGrouperSyncObject.setHasDirectSettings(true);
            
            for (GrouperProvisioningObjectMetadataItem metadataItem: provisioningObjectMetadataItems) {
              
              if (provisioningAttributeValue.getMetadataNameValues().containsKey(metadataItem.getName())) {
                metadataItem.setDefaultValue(provisioningAttributeValue.getMetadataNameValues().get(metadataItem.getName()));
                itemsToShow.add(metadataItem);
              }
            }
            
            provisioningContainer.setGrouperProvisioningObjectMetadataItems(itemsToShow);
          }
          
          provisioningContainer.setGuiGrouperSyncObject(guiGrouperSyncObject);
          
          GuiSubject guiSubject = GrouperRequestContainer.retrieveFromRequestOrCreate().getSubjectContainer().getGuiSubject();
          addProvisioningBreadcrumbs(guiSubject, null, null, null, null);
          
          guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
              "/WEB-INF/grouperUi2/provisioning/provisioningSubjectTargetDetails.jsp"));
          
          return null;
        }
      });
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * view details of a single grouper_sync_membership table for a membership from group screen
   * @param request
   * @param response
   */
  public void viewProvisioningTargetDetailsOnGroupMembership(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final Group group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.VIEW).getGroup();
      
      if (group == null) {
        return;
      }
      
      final Subject subject = UiV2Subject.retrieveSubjectHelper(request, true);

      if (subject == null) {
        return;
      }
      
      if (!PrivilegeHelper.isWheelOrRoot(loggedInSubject)) {
        throw new RuntimeException("Cannot access provisioning.");
      }
      
      final Group GROUP = group;
      
      final String targetName = request.getParameter("provisioningTargetName");
      
      if (StringUtils.isBlank(targetName)) {
        throw new RuntimeException("provisioningTargetName cannot be blank");
      }

      final String groupSyncMembershipId = request.getParameter("groupSyncMembershipId");
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
            
      final ProvisioningContainer provisioningContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getProvisioningContainer();
      
      //switch over to admin so attributes work
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          if (!checkProvisioning()) {
            return null;
          }
          
          GuiGrouperSyncObject guiGrouperSyncObject = new GuiGrouperSyncObject();
          guiGrouperSyncObject.setTargetName(targetName);
          
          if (StringUtils.isNotBlank(groupSyncMembershipId)) {
            GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, targetName);
            GcGrouperSyncMembership gcGrouperSyncMembership = gcGrouperSync.getGcGrouperSyncMembershipDao().membershipRetrieveById(groupSyncMembershipId);
            
            if (gcGrouperSyncMembership != null) {
              
              GcGrouperSyncGroup gcGrouperSyncGroup = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveById(gcGrouperSyncMembership.getGrouperSyncGroupId());
              gcGrouperSyncMembership.setGrouperSyncGroup(gcGrouperSyncGroup);
              
              GcGrouperSyncMember gcGrouperSyncMember = gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveById(gcGrouperSyncMembership.getGrouperSyncMemberId());
              gcGrouperSyncMembership.setGrouperSyncMember(gcGrouperSyncMember);
            }
            guiGrouperSyncObject.setGcGrouperSyncMembership(gcGrouperSyncMembership);
          }

          List<GrouperProvisioningObjectMetadataItem> provisioningObjectMetadataItems = GrouperProvisioner.retrieveProvisioner(targetName).retrieveGrouperProvisioningObjectMetadata().getGrouperProvisioningObjectMetadataItems();
          List<GrouperProvisioningObjectMetadataItem> itemsToShow = new ArrayList<GrouperProvisioningObjectMetadataItem>();
          
          Member member = MemberFinder.findBySubject(theGrouperSession, subject, true);
          GrouperProvisioningAttributeValue provisioningAttributeValue = GrouperProvisioningService.getProvisioningAttributeValue(group, member, targetName);
          if (provisioningAttributeValue != null && provisioningAttributeValue.getMetadataNameValues() != null &&
            provisioningAttributeValue.getMetadataNameValues().size() > 0) {
            
            guiGrouperSyncObject.setHasDirectSettings(true);
            
            for (GrouperProvisioningObjectMetadataItem metadataItem: provisioningObjectMetadataItems) {
              
              if (provisioningAttributeValue.getMetadataNameValues().containsKey(metadataItem.getName())) {
                metadataItem.setDefaultValue(provisioningAttributeValue.getMetadataNameValues().get(metadataItem.getName()));
                itemsToShow.add(metadataItem);
              }
            }
            
            provisioningContainer.setGrouperProvisioningObjectMetadataItems(itemsToShow);
          }
          
          provisioningContainer.setGuiGrouperSyncObject(guiGrouperSyncObject);
          
          addProvisioningBreadcrumbs(new GuiGroup(GROUP), null, null, null, null);
          
          guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
              "/WEB-INF/grouperUi2/provisioning/provisioningGroupMembershipDetails.jsp"));
          
          return null;
        }
      });
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * view provisioning table for a membership from group screen
   * @param request
   * @param response
   */
  public void viewProvisioningOnGroupMembership(final HttpServletRequest request, final HttpServletResponse response) {

    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      final Group group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.VIEW).getGroup();
     
      if (group == null) {
        return;
      }
      
      final Subject subject = UiV2Subject.retrieveSubjectHelper(request, true);

      if (subject == null) {
        return;
      }
      
      if (!PrivilegeHelper.isWheelOrRoot(loggedInSubject)) {
        throw new RuntimeException("Cannot access provisioning.");
      }
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
            
      final ProvisioningContainer provisioningContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getProvisioningContainer();
      
      //switch over to admin so attributes work
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          if (!checkProvisioning()) {
            return null;
          }
          
          Member member = MemberFinder.findBySubject(theGrouperSession, subject, true);
          List<GcGrouperSyncMembership> gcGrouperSyncMemberships = GrouperProvisioningService.retrieveGcGrouperSyncMemberships(member.getId(), group.getId());
          
          Map<String, List<GuiGrouperSyncObject>> provisionerNameToGuiGrouperSyncObject = new HashMap<String, List<GuiGrouperSyncObject>>();
          
          for (GcGrouperSyncMembership gcGrouperSyncMembership: gcGrouperSyncMemberships) {
            String targetName = gcGrouperSyncMembership.getGrouperSync().getProvisionerName();
            
            GuiGrouperSyncObject guiGrouperSyncObject = new GuiGrouperSyncObject();
            guiGrouperSyncObject.setGcGrouperSyncMembership(gcGrouperSyncMembership);
            guiGrouperSyncObject.setTargetName(targetName);
            
            List<GuiGrouperSyncObject> guiGrouperSyncObjects = provisionerNameToGuiGrouperSyncObject.getOrDefault(targetName, new ArrayList<GuiGrouperSyncObject>());
            guiGrouperSyncObjects.add(guiGrouperSyncObject);
            provisionerNameToGuiGrouperSyncObject.put(targetName, guiGrouperSyncObjects);
            
          }
          
          
          List<GrouperProvisioningAttributeValue> provisioningAttributeValues = GrouperProvisioningService.getProvisioningAttributeValues(group, member);
          
          for (GrouperProvisioningAttributeValue grouperProvisioningAttributeValue: provisioningAttributeValues) {
            
            Map<String, Object> metadataNameValues = grouperProvisioningAttributeValue.getMetadataNameValues();
            if (metadataNameValues != null && metadataNameValues.size() > 0) {
              String targetName = grouperProvisioningAttributeValue.getTargetName();
              if (provisionerNameToGuiGrouperSyncObject.containsKey(targetName)) {
                List<GuiGrouperSyncObject> guiGrouperSyncObjects = provisionerNameToGuiGrouperSyncObject.get(targetName);
                for (GuiGrouperSyncObject guiGrouperSyncObject: guiGrouperSyncObjects) {
                  guiGrouperSyncObject.setHasDirectSettings(true);
                }
              } else {
                GuiGrouperSyncObject guiGrouperSyncObject = new GuiGrouperSyncObject();
                guiGrouperSyncObject.setTargetName(targetName);
                guiGrouperSyncObject.setHasDirectSettings(true);
                
                List<GuiGrouperSyncObject> guiGrouperSyncObjects = new ArrayList<GuiGrouperSyncObject>();
                guiGrouperSyncObjects.add(guiGrouperSyncObject);
                
                provisionerNameToGuiGrouperSyncObject.put(targetName, guiGrouperSyncObjects);
              }
              
            }
          }
          
          Collection<List<GuiGrouperSyncObject>> listOfListOfGrouperSyncObjects = provisionerNameToGuiGrouperSyncObject.values();
          List<GuiGrouperSyncObject> guiGrouperSyncObjects = listOfListOfGrouperSyncObjects.stream().flatMap(List::stream).collect(Collectors.toList());
          
          provisioningContainer.setGuiGrouperSyncObjects(guiGrouperSyncObjects);
          
          GuiSubject guiSubject = GrouperRequestContainer.retrieveFromRequestOrCreate().getSubjectContainer().getGuiSubject();
          GuiGroup guiGroup = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup();
          GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().setGuiGroup(guiGroup);
          addProvisioningBreadcrumbs(guiGroup, null, null, null, null);
          
          guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
              "/WEB-INF/grouperUi2/provisioning/provisioningGroupMembershipTable.jsp"));
          
          return null;
        }
      });
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * view details of a single grouper_sync_membership table for a membership from subject screen
   * @param request
   * @param response
   */
  public void viewProvisioningTargetDetailsOnSubjectMembership(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final Group group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.VIEW).getGroup();
      
      if (group == null) {
        return;
      }
      
      final Subject subject = UiV2Subject.retrieveSubjectHelper(request, true);

      if (subject == null) {
        return;
      }
      
      if (!PrivilegeHelper.isWheelOrRoot(loggedInSubject)) {
        throw new RuntimeException("Cannot access provisioning.");
      }
      
      final String targetName = request.getParameter("provisioningTargetName");
      
      if (StringUtils.isBlank(targetName)) {
        throw new RuntimeException("provisioningTargetName cannot be blank");
      }

      final String groupSyncMembershipId = request.getParameter("groupSyncMembershipId");
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
            
      final ProvisioningContainer provisioningContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getProvisioningContainer();
      
      //switch over to admin so attributes work
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          if (!checkProvisioning()) {
            return null;
          }
          
          GuiGrouperSyncObject guiGrouperSyncObject = new GuiGrouperSyncObject();
          guiGrouperSyncObject.setTargetName(targetName);
          
          if (StringUtils.isNotBlank(groupSyncMembershipId)) {
            GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, targetName);
            GcGrouperSyncMembership gcGrouperSyncMembership = gcGrouperSync.getGcGrouperSyncMembershipDao().membershipRetrieveById(groupSyncMembershipId);
            
            if (gcGrouperSyncMembership != null) {
              
              GcGrouperSyncGroup gcGrouperSyncGroup = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveById(gcGrouperSyncMembership.getGrouperSyncGroupId());
              gcGrouperSyncMembership.setGrouperSyncGroup(gcGrouperSyncGroup);
              
              GcGrouperSyncMember gcGrouperSyncMember = gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveById(gcGrouperSyncMembership.getGrouperSyncMemberId());
              gcGrouperSyncMembership.setGrouperSyncMember(gcGrouperSyncMember);
            }
            guiGrouperSyncObject.setGcGrouperSyncMembership(gcGrouperSyncMembership);
          }

          List<GrouperProvisioningObjectMetadataItem> provisioningObjectMetadataItems = GrouperProvisioner.retrieveProvisioner(targetName).retrieveGrouperProvisioningObjectMetadata().getGrouperProvisioningObjectMetadataItems();
          List<GrouperProvisioningObjectMetadataItem> itemsToShow = new ArrayList<GrouperProvisioningObjectMetadataItem>();
          
          Member member = MemberFinder.findBySubject(theGrouperSession, subject, true);
          GrouperProvisioningAttributeValue provisioningAttributeValue = GrouperProvisioningService.getProvisioningAttributeValue(group, member, targetName);
          if (provisioningAttributeValue != null && provisioningAttributeValue.getMetadataNameValues() != null &&
            provisioningAttributeValue.getMetadataNameValues().size() > 0) {
            
            guiGrouperSyncObject.setHasDirectSettings(true);
            
            for (GrouperProvisioningObjectMetadataItem metadataItem: provisioningObjectMetadataItems) {
              
              if (provisioningAttributeValue.getMetadataNameValues().containsKey(metadataItem.getName())) {
                metadataItem.setDefaultValue(provisioningAttributeValue.getMetadataNameValues().get(metadataItem.getName()));
                itemsToShow.add(metadataItem);
              }
            }
            
            provisioningContainer.setGrouperProvisioningObjectMetadataItems(itemsToShow);
          }
          
          provisioningContainer.setGuiGrouperSyncObject(guiGrouperSyncObject);
          
          
          GuiSubject guiSubject = GrouperRequestContainer.retrieveFromRequestOrCreate().getSubjectContainer().getGuiSubject();
          addProvisioningBreadcrumbs(guiSubject, null, null, null, null);
          
          guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
              "/WEB-INF/grouperUi2/provisioning/provisioningSubjectMembershipDetails.jsp"));
          
          return null;
        }
      });
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * view provisioning settings for a membership
   * @param request
   * @param response
   */
  public void viewProvisioningOnSubjectMembership(final HttpServletRequest request, final HttpServletResponse response) {

    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      final Group group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.VIEW).getGroup();
     
      if (group == null) {
        return;
      }
      
      final Subject subject = UiV2Subject.retrieveSubjectHelper(request, true);

      if (subject == null) {
        return;
      }

      if (!PrivilegeHelper.isWheelOrRoot(loggedInSubject)) {
        throw new RuntimeException("Cannot access provisioning.");
      }
      
      final Group GROUP = group;
      final Subject SUBJECT = subject;
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
            
      final ProvisioningContainer provisioningContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getProvisioningContainer();
      
      //switch over to admin so attributes work
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          if (!checkProvisioning()) {
            return null;
          }
          
          Member member = MemberFinder.findBySubject(theGrouperSession, SUBJECT, true);
          List<GcGrouperSyncMembership> gcGrouperSyncMemberships = GrouperProvisioningService.retrieveGcGrouperSyncMemberships(member.getId(), GROUP.getId());

          Map<String, List<GuiGrouperSyncObject>> provisionerNameToGuiGrouperSyncObject = new HashMap<String, List<GuiGrouperSyncObject>>();
          
          for (GcGrouperSyncMembership gcGrouperSyncMembership: gcGrouperSyncMemberships) {
            String targetName = gcGrouperSyncMembership.getGrouperSync().getProvisionerName();
            
            GuiGrouperSyncObject guiGrouperSyncObject = new GuiGrouperSyncObject();
            guiGrouperSyncObject.setGcGrouperSyncMembership(gcGrouperSyncMembership);
            guiGrouperSyncObject.setTargetName(targetName);
            
            List<GuiGrouperSyncObject> guiGrouperSyncObjects = provisionerNameToGuiGrouperSyncObject.getOrDefault(targetName, new ArrayList<GuiGrouperSyncObject>());
            guiGrouperSyncObjects.add(guiGrouperSyncObject);
            provisionerNameToGuiGrouperSyncObject.put(targetName, guiGrouperSyncObjects);
            
          }
          
          List<GrouperProvisioningAttributeValue> provisioningAttributeValues = GrouperProvisioningService.getProvisioningAttributeValues(group, member);
          
          for (GrouperProvisioningAttributeValue grouperProvisioningAttributeValue: provisioningAttributeValues) {
            
            Map<String, Object> metadataNameValues = grouperProvisioningAttributeValue.getMetadataNameValues();
            if (metadataNameValues != null && metadataNameValues.size() > 0) {
              String targetName = grouperProvisioningAttributeValue.getTargetName();
              if (provisionerNameToGuiGrouperSyncObject.containsKey(targetName)) {
                List<GuiGrouperSyncObject> guiGrouperSyncObjects = provisionerNameToGuiGrouperSyncObject.get(targetName);
                for (GuiGrouperSyncObject guiGrouperSyncObject: guiGrouperSyncObjects) {
                  guiGrouperSyncObject.setHasDirectSettings(true);
                }
              } else {
                GuiGrouperSyncObject guiGrouperSyncObject = new GuiGrouperSyncObject();
                guiGrouperSyncObject.setHasDirectSettings(true);
                guiGrouperSyncObject.setTargetName(targetName);
                
                List<GuiGrouperSyncObject> guiGrouperSyncObjects = new ArrayList<GuiGrouperSyncObject>();
                guiGrouperSyncObjects.add(guiGrouperSyncObject);
                
                provisionerNameToGuiGrouperSyncObject.put(targetName, guiGrouperSyncObjects);
              }
              
            }
          }
          
          Collection<List<GuiGrouperSyncObject>> listOfListOfGrouperSyncObjects = provisionerNameToGuiGrouperSyncObject.values();
          List<GuiGrouperSyncObject> guiGrouperSyncObjects = listOfListOfGrouperSyncObjects.stream().flatMap(List::stream).collect(Collectors.toList());
          
          provisioningContainer.setGuiGrouperSyncObjects(guiGrouperSyncObjects);
          
          GuiSubject guiSubject = GrouperRequestContainer.retrieveFromRequestOrCreate().getSubjectContainer().getGuiSubject();
          addProvisioningBreadcrumbs(guiSubject, null, null, null, null);
          
          guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
              "/WEB-INF/grouperUi2/provisioning/provisioningSubjectMembershipTable.jsp"));
          
          return null;
        }
      });
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  private final void setGrouperProvisioningAttributeValues(Group group, String targetName) {
    
    List<GrouperProvisioningAttributeValue> provisioningAttributeValues = new ArrayList<GrouperProvisioningAttributeValue>();
    
    if (StringUtils.isBlank(targetName)) {
      provisioningAttributeValues = GrouperProvisioningService.getProvisioningAttributeValues(group);
    } else {
      GrouperProvisioningAttributeValue grouperProvisioningAttributeValue = GrouperProvisioningService.getProvisioningAttributeValue(group, targetName);
      provisioningAttributeValues.add(grouperProvisioningAttributeValue);
    }
    
    // convert from raw to gui
    List<GuiGrouperProvisioningAttributeValue> guiGrouperProvisioningAttributeValues = GuiGrouperProvisioningAttributeValue.convertFromGrouperProvisioningAttributeValues(provisioningAttributeValues);
    
    for (GuiGrouperProvisioningAttributeValue guiGrouperProvisioningAttributeValue: guiGrouperProvisioningAttributeValues) {
      String provisionerName = guiGrouperProvisioningAttributeValue.getGrouperProvisioningAttributeValue().getTargetName();
      GcGrouperSyncGroup gcGrouperSyncGroup = GrouperProvisioningService.retrieveGcGrouperGroup(group.getId(), provisionerName);
      
      if (gcGrouperSyncGroup != null) {
        guiGrouperProvisioningAttributeValue.setInTarget(gcGrouperSyncGroup.isInTarget());
        guiGrouperProvisioningAttributeValue.setLastTimeWorkWasDone(gcGrouperSyncGroup.getLastTimeWorkWasDone());
      }
      
      List<GrouperProvisioningObjectMetadataItem> provisioningObjectMetadataItems = GrouperProvisioner.retrieveProvisioner(provisionerName).retrieveGrouperProvisioningObjectMetadata().getGrouperProvisioningObjectMetadataItems();
      
      List<GrouperProvisioningObjectMetadataItem> itemsToShow = new ArrayList<GrouperProvisioningObjectMetadataItem>();
      
      for (GrouperProvisioningObjectMetadataItem metadataItem: provisioningObjectMetadataItems) {
        
        if (guiGrouperProvisioningAttributeValue.getGrouperProvisioningAttributeValue()
              .getMetadataNameValues().containsKey(metadataItem.getName())) {
          metadataItem.setDefaultValue(guiGrouperProvisioningAttributeValue.getGrouperProvisioningAttributeValue()
              .getMetadataNameValues().get(metadataItem.getName()));
          itemsToShow.add(metadataItem);
        }
      }
      
      if (itemsToShow.size() > 0) {
        guiGrouperProvisioningAttributeValue.setHasDirectSettings(true);
      }
      
      guiGrouperProvisioningAttributeValue.setMetadataItems(itemsToShow);
    }
    
    final ProvisioningContainer provisioningContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getProvisioningContainer();
    provisioningContainer.setGuiGrouperProvisioningAttributeValues(guiGrouperProvisioningAttributeValues);
  } 
  
  
  /**
   * edit provisioning settings for a folder
   * @param request
   * @param response
   */
  public void editProvisioningOnFolder(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Stem stem = null;
    
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      stem = UiV2Stem.retrieveStemHelper(request, true).getStem();
      
      if (stem == null) {
        return;
      }
      
      if (!PrivilegeHelper.isWheelOrRoot(loggedInSubject)) {
        throw new RuntimeException("Cannot access provisioning.");
      }
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      final ProvisioningContainer provisioningContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getProvisioningContainer();
      
      //switch over to admin so attributes work
      boolean shouldContinue = (Boolean)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          if (!checkProvisioning()) {
            return false;
          }
          
          if (!provisioningContainer.isCanWriteProvisioning()) {
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
                TextContainer.retrieveFromRequest().getText().get("provisioningNotAllowedToWriteStem")));
            return false;
          }
  
          return true;
        }
      });
      
      if (!shouldContinue) {
        return;
      }
      
      final Stem STEM = stem;
      String previousTargetName = request.getParameter("provisioningPreviousTargetName");
      final String targetName = request.getParameter("provisioningTargetName");
      
      //switch over to admin so attributes work
      GrouperProvisioningAttributeValue provisioningAttributeValue = (GrouperProvisioningAttributeValue)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          if (StringUtils.isNotBlank(targetName)) {
            provisioningContainer.setTargetName(targetName);
            
            return GrouperProvisioningService.getProvisioningAttributeValue(STEM, targetName);
          }
          
          return null;
        }
      });
      
      if (provisioningAttributeValue == null) {
        provisioningAttributeValue = new GrouperProvisioningAttributeValue();
      }
      
      if (StringUtils.equals(targetName, previousTargetName)) {
        String configurationType = request.getParameter("provisioningHasConfigurationName");
        if (!StringUtils.isBlank(configurationType)) {
          boolean isDirect = GrouperUtil.booleanValue(configurationType, false);
          provisioningAttributeValue.setDirectAssignment(isDirect);
        }
      }
      
      if (StringUtils.isNotBlank(targetName)) {
        
        List<GrouperProvisioningObjectMetadataItem> metadataItems = new ArrayList<GrouperProvisioningObjectMetadataItem>();
        
        Map<String, Object> metadataNameValues = provisioningAttributeValue.getMetadataNameValues();
        
        List<GrouperProvisioningObjectMetadataItem> provisioningObjectMetadataItems = GrouperProvisioner.retrieveProvisioner(targetName).retrieveGrouperProvisioningObjectMetadata().getGrouperProvisioningObjectMetadataItems();
        
        for (GrouperProvisioningObjectMetadataItem metadataItem: provisioningObjectMetadataItems) {
          if (metadataItem.isShowForFolder()) {
            Object value = metadataNameValues.getOrDefault(metadataItem.getName(), metadataItem.getDefaultValue());
            metadataItem.setDefaultValue(value);
            metadataItems.add(metadataItem);
          }
        }
        
        provisioningContainer.setGrouperProvisioningObjectMetadataItems(metadataItems);
      }

      provisioningContainer.setGrouperProvisioningAttributeValue(provisioningAttributeValue);
            
      //switch over to admin so attributes work
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          GuiStem guiStem = GrouperRequestContainer.retrieveFromRequestOrCreate().getStemContainer().getGuiStem();
          
          addProvisioningBreadcrumbs(guiStem, null, null, null, null);
          
          guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
              "/WEB-INF/grouperUi2/provisioning/provisioningFolderSettingsEdit.jsp"));
          
          return null;
        }
      });
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

  }
  
  /**
   * edit provisioning settings for a subject membership
   * @param request
   * @param response
   */
  public void editProvisioningOnSubjectMembership(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final Group group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.VIEW).getGroup();
     
      if (group == null) {
        return;
      }
      
      final Subject subject = UiV2Subject.retrieveSubjectHelper(request, true);

      if (subject == null) {
        return;
      }

      if (!PrivilegeHelper.isWheelOrRoot(loggedInSubject)) {
        throw new RuntimeException("Cannot access provisioning.");
      }
      
      final Member member = MemberFinder.findBySubject(grouperSession, subject, true);
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      final ProvisioningContainer provisioningContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getProvisioningContainer();
      
      //switch over to admin so attributes work
      boolean shouldContinue = (Boolean)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          if (!checkProvisioning()) {
            return false;
          }
          
          return true;
        }
      });
      
      if (!shouldContinue) {
        return;
      }
      
      String previousTargetName = request.getParameter("provisioningPreviousTargetName");
      final String targetName = request.getParameter("provisioningTargetName");
      
      //switch over to admin so attributes work
      GrouperProvisioningAttributeValue provisioningAttributeValue = (GrouperProvisioningAttributeValue)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          if (StringUtils.isNotBlank(targetName)) {
            provisioningContainer.setTargetName(targetName);
            
            return GrouperProvisioningService.getProvisioningAttributeValue(group, member, targetName);
          }
          
          return null;
        }
      });
      
      if (provisioningAttributeValue == null) {
        provisioningAttributeValue = new GrouperProvisioningAttributeValue();
      }
      
      if (StringUtils.equals(targetName, previousTargetName)) {
       
      }
      
      if (StringUtils.isNotBlank(targetName)) {
        
        List<GrouperProvisioningObjectMetadataItem> metadataItems = new ArrayList<GrouperProvisioningObjectMetadataItem>();
        
        Map<String, Object> metadataNameValues = provisioningAttributeValue.getMetadataNameValues();
        
        GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner(targetName);
        GrouperProvisioningObjectMetadata grouperProvisioningObjectMetadata = grouperProvisioner.retrieveGrouperProvisioningObjectMetadata();
        List<GrouperProvisioningObjectMetadataItem> provisioningObjectMetadataItems = grouperProvisioningObjectMetadata.getGrouperProvisioningObjectMetadataItems();
        
        for (GrouperProvisioningObjectMetadataItem metadataItem: provisioningObjectMetadataItems) {
          if (metadataItem.isShowForMembership()) {
            Object value = metadataNameValues.getOrDefault(metadataItem.getName(), metadataItem.getDefaultValue());
            metadataItem.setDefaultValue(value);
            metadataItems.add(metadataItem);
          }
        }
        
        if (metadataItems.size() == 0) {
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.info, 
              TextContainer.retrieveFromRequest().getText().get("provisioningNoMetadataAttached")));
          return;
        }
        
        provisioningContainer.setGrouperProvisioningObjectMetadataItems(metadataItems);
      }
      
      provisioningContainer.setGrouperProvisioningAttributeValue(provisioningAttributeValue);
            
      //switch over to admin so attributes work
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          GuiSubject guiSubject = GrouperRequestContainer.retrieveFromRequestOrCreate().getSubjectContainer().getGuiSubject();
          addProvisioningBreadcrumbs(guiSubject, null, null, null, null);
          
          guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
              "/WEB-INF/grouperUi2/provisioning/provisioningSubjectMembershipSettingsEdit.jsp"));
          
          return null;
        }
      });
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

  }
  
  /**
   * edit provisioning settings for a group membership
   * @param request
   * @param response
   */
  public void editProvisioningOnGroupMembership(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final Group group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.VIEW).getGroup();
     
      if (group == null) {
        return;
      }
      
      final Subject subject = UiV2Subject.retrieveSubjectHelper(request, true);

      if (subject == null) {
        return;
      }

      if (!PrivilegeHelper.isWheelOrRoot(loggedInSubject)) {
        throw new RuntimeException("Cannot access provisioning.");
      }
      
      final Member member = MemberFinder.findBySubject(grouperSession, subject, true);
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      final ProvisioningContainer provisioningContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getProvisioningContainer();
      
      //switch over to admin so attributes work
      boolean shouldContinue = (Boolean)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          if (!checkProvisioning()) {
            return false;
          }
          
          return true;
        }
      });
      
      if (!shouldContinue) {
        return;
      }
      
      String previousTargetName = request.getParameter("provisioningPreviousTargetName");
      final String targetName = request.getParameter("provisioningTargetName");
      
      //switch over to admin so attributes work
      GrouperProvisioningAttributeValue provisioningAttributeValue = (GrouperProvisioningAttributeValue)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          if (StringUtils.isNotBlank(targetName)) {
            provisioningContainer.setTargetName(targetName);
            
            return GrouperProvisioningService.getProvisioningAttributeValue(group, member, targetName);
          }
          
          return null;
        }
      });
      
      if (provisioningAttributeValue == null) {
        provisioningAttributeValue = new GrouperProvisioningAttributeValue();
      }
      
      if (StringUtils.isNotBlank(targetName)) {
        
        List<GrouperProvisioningObjectMetadataItem> metadataItems = new ArrayList<GrouperProvisioningObjectMetadataItem>();
        
        Map<String, Object> metadataNameValues = provisioningAttributeValue.getMetadataNameValues();
        
        GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner(targetName);
        GrouperProvisioningObjectMetadata grouperProvisioningObjectMetadata = grouperProvisioner.retrieveGrouperProvisioningObjectMetadata();
        List<GrouperProvisioningObjectMetadataItem> provisioningObjectMetadataItems = grouperProvisioningObjectMetadata.getGrouperProvisioningObjectMetadataItems();
        
        for (GrouperProvisioningObjectMetadataItem metadataItem: provisioningObjectMetadataItems) {
          if (metadataItem.isShowForMembership()) {
            Object value = metadataNameValues.getOrDefault(metadataItem.getName(), metadataItem.getDefaultValue());
            metadataItem.setDefaultValue(value);
            metadataItems.add(metadataItem);
          }
        }
        
        if (metadataItems.size() == 0) {
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.info, 
              TextContainer.retrieveFromRequest().getText().get("provisioningNoMetadataAttached")));
          return;
        }
        
        provisioningContainer.setGrouperProvisioningObjectMetadataItems(metadataItems);
      }
      
      provisioningContainer.setGrouperProvisioningAttributeValue(provisioningAttributeValue);
            
      //switch over to admin so attributes work
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          GuiSubject guiSubject = GrouperRequestContainer.retrieveFromRequestOrCreate().getSubjectContainer().getGuiSubject();
          addProvisioningBreadcrumbs(guiSubject, null, null, null, null);
          
          guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
              "/WEB-INF/grouperUi2/provisioning/provisioningGroupMembershipSettingsEdit.jsp"));
          
          return null;
        }
      });
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

  }
  
  /**
   * edit provisioning settings for a subject
   * @param request
   * @param response
   */
  public void editProvisioningOnSubject(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Subject subject = null;
    
    try {
  
      if (!PrivilegeHelper.isWheelOrRoot(loggedInSubject)) {
        throw new RuntimeException("Cannot access provisioning.");
      }
      
      grouperSession = GrouperSession.start(loggedInSubject);
  
      subject = UiV2Subject.retrieveSubjectHelper(request, true);

      if (subject == null) {
        return;
      }
      
      final Member member = MemberFinder.findBySubject(grouperSession, subject, true);
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      final ProvisioningContainer provisioningContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getProvisioningContainer();
      
      //switch over to admin so attributes work
      boolean shouldContinue = (Boolean)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          if (!checkProvisioning()) {
            return false;
          }
          
          return true;
        }
      });
      
      if (!shouldContinue) {
        return;
      }
      
      String previousTargetName = request.getParameter("provisioningPreviousTargetName");
      final String targetName = request.getParameter("provisioningTargetName");
      
      //switch over to admin so attributes work
      GrouperProvisioningAttributeValue provisioningAttributeValue = (GrouperProvisioningAttributeValue)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          if (StringUtils.isNotBlank(targetName)) {
            provisioningContainer.setTargetName(targetName);
            
            return GrouperProvisioningService.getProvisioningAttributeValue(member, targetName);
          }
          
          return null;
        }
      });
      
      if (provisioningAttributeValue == null) {
        provisioningAttributeValue = new GrouperProvisioningAttributeValue();
      }
      
      if (StringUtils.isNotBlank(targetName)) {
        
        List<GrouperProvisioningObjectMetadataItem> metadataItems = new ArrayList<GrouperProvisioningObjectMetadataItem>();
        
        Map<String, Object> metadataNameValues = provisioningAttributeValue.getMetadataNameValues();
        
        GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner(targetName);
        GrouperProvisioningObjectMetadata grouperProvisioningObjectMetadata = grouperProvisioner.retrieveGrouperProvisioningObjectMetadata();
        List<GrouperProvisioningObjectMetadataItem> provisioningObjectMetadataItems = grouperProvisioningObjectMetadata.getGrouperProvisioningObjectMetadataItems();
        
        for (GrouperProvisioningObjectMetadataItem metadataItem: provisioningObjectMetadataItems) {
          if (metadataItem.isShowForMember()) {
            Object value = metadataNameValues.getOrDefault(metadataItem.getName(), metadataItem.getDefaultValue());
            metadataItem.setDefaultValue(value);
            metadataItems.add(metadataItem);
          }
        }
        
        if (metadataItems.size() == 0) {
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.info, 
              TextContainer.retrieveFromRequest().getText().get("provisioningNoMetadataAttached")));
          return;
        }
        
        provisioningContainer.setGrouperProvisioningObjectMetadataItems(metadataItems);
      }
      
      provisioningContainer.setGrouperProvisioningAttributeValue(provisioningAttributeValue);
            
      //switch over to admin so attributes work
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          GuiSubject guiSubject = GrouperRequestContainer.retrieveFromRequestOrCreate().getSubjectContainer().getGuiSubject();
          addProvisioningBreadcrumbs(guiSubject, null, null, null, null);
          
          guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
              "/WEB-INF/grouperUi2/provisioning/provisioningSubjectSettingsEdit.jsp"));
          
          return null;
        }
      });
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

  }
  
  /**
   * edit provisioning settings for a group
   * @param request
   * @param response
   */
  public void editProvisioningOnGroup(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Group group = null;
    
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.UPDATE).getGroup();
      
      if (group != null) {
        group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.READ).getGroup();
      }
      
      if (group == null) {
        return;
      }
      
      if (!PrivilegeHelper.isWheelOrRoot(loggedInSubject)) {
        throw new RuntimeException("Cannot access provisioning.");
      }
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      final ProvisioningContainer provisioningContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getProvisioningContainer();
      
      //switch over to admin so attributes work
      boolean shouldContinue = (Boolean)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          if (!checkProvisioning()) {
            return false;
          }
          
          if (!provisioningContainer.isCanWriteProvisioning()) {
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
                TextContainer.retrieveFromRequest().getText().get("provisioningNotAllowedToWriteGroup")));
            return false;
          }
  
          return true;
        }
      });
      
      if (!shouldContinue) {
        return;
      }
      
      final Group GROUP = group;
      String previousTargetName = request.getParameter("provisioningPreviousTargetName");
      final String targetName = request.getParameter("provisioningTargetName");
      
      //switch over to admin so attributes work
      GrouperProvisioningAttributeValue provisioningAttributeValue = (GrouperProvisioningAttributeValue)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          if (StringUtils.isNotBlank(targetName)) {
            provisioningContainer.setTargetName(targetName);
            
            return GrouperProvisioningService.getProvisioningAttributeValue(GROUP, targetName);
          }
          
          return null;
        }
      });
      
      if (provisioningAttributeValue == null) {
        provisioningAttributeValue = new GrouperProvisioningAttributeValue();
      }
      
      if (StringUtils.equals(targetName, previousTargetName)) {
        String configurationType = request.getParameter("provisioningHasConfigurationName");
        if (!StringUtils.isBlank(configurationType)) {
          boolean isDirect = GrouperUtil.booleanValue(configurationType, false);
          provisioningAttributeValue.setDirectAssignment(isDirect);
        }
      }
      
      if (StringUtils.isNotBlank(targetName)) {
        
        List<GrouperProvisioningObjectMetadataItem> metadataItems = new ArrayList<GrouperProvisioningObjectMetadataItem>();
        
        Map<String, Object> metadataNameValues = provisioningAttributeValue.getMetadataNameValues();
        
        GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner(targetName);
        GrouperProvisioningObjectMetadata grouperProvisioningObjectMetadata = grouperProvisioner.retrieveGrouperProvisioningObjectMetadata();
        List<GrouperProvisioningObjectMetadataItem> provisioningObjectMetadataItems = grouperProvisioningObjectMetadata.getGrouperProvisioningObjectMetadataItems();
        
        for (GrouperProvisioningObjectMetadataItem metadataItem: provisioningObjectMetadataItems) {
          if (metadataItem.isShowForGroup()) {
            Object value = metadataNameValues.getOrDefault(metadataItem.getName(), metadataItem.getDefaultValue());
            metadataItem.setDefaultValue(value);
            metadataItems.add(metadataItem);
          }
        }
        
        provisioningContainer.setGrouperProvisioningObjectMetadataItems(metadataItems);
      }
      
      provisioningContainer.setGrouperProvisioningAttributeValue(provisioningAttributeValue);
            
      //switch over to admin so attributes work
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          GuiGroup guiGroup = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup();
          
          addProvisioningBreadcrumbs(guiGroup, null, null, null, null);
          
          guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
              "/WEB-INF/grouperUi2/provisioning/provisioningGroupSettingsEdit.jsp"));
          
          return null;
        }
      });
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

  }
  
  private boolean setMetadataValues(final HttpServletRequest request, 
      final Map<String, Object> metadataNameValuesToPopulate, List<GrouperProvisioningObjectMetadataItem> metadataItems) {
    
    boolean errors = false;
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    for (GrouperProvisioningObjectMetadataItem metadataItem: metadataItems) {

      String name = metadataItem.getName();
      String value = request.getParameter(name);
      
      if (metadataItem.isRequired() && StringUtils.isBlank(value)) {
        String labelKey = metadataItem.getLabelKey();
        String label = GrouperTextContainer.textOrNull(labelKey);
        if (StringUtils.isBlank(label)) {
          label = labelKey;
        }
        String errorMessage = TextContainer.retrieveFromRequest().getText().get("provisioningMetadataItemRequired");
        errorMessage = errorMessage.replace("$$metadataLabel$$", label);
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, "#"+name+"_id", errorMessage));
        errors = true;
      }
      
      if (StringUtils.isNotBlank(value)) {
        try {          
          Object convertedValue = metadataItem.getValueType().convert(value);
          metadataNameValuesToPopulate.put(name, convertedValue);
        } catch (Exception e) {
          String errorMessage = TextContainer.retrieveFromRequest().getText().get("provisioningMetadataValueNotCorrectTypeRequired");
          errorMessage = errorMessage.replace("$$value$$", "'"+value+"'");
          errorMessage = errorMessage.replace("$$type$$", metadataItem.getValueType().name());
          guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, "#"+name+"_id", errorMessage));
          errors = true;
        }
        
      }
      
    }
    
    return errors;
  }
  
  /**
   * save changes to provisioning settings for a folder
   * @param request
   * @param response
   */
  public void editProvisioningOnFolderSave(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Stem stem = null;
    
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      stem = UiV2Stem.retrieveStemHelper(request, true).getStem();
      
      if (stem == null) {
        return;
      }
      
      if (!PrivilegeHelper.isWheelOrRoot(loggedInSubject)) {
        throw new RuntimeException("Cannot access provisioning.");
      }
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      final ProvisioningContainer provisioningContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getProvisioningContainer();
      
      //switch over to admin so attributes work
      boolean shouldContinue = (Boolean)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          if (!checkProvisioning()) {
            return false;
          }
          
          if (!provisioningContainer.isCanWriteProvisioning()) {
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
                TextContainer.retrieveFromRequest().getText().get("provisioningNotAllowedToWriteStem")));
            return false;
          }
  
          return true;
        }
      });
      
      if (!shouldContinue) {
        return;
      }
      
      final String targetName = request.getParameter("provisioningTargetName");
      
      String configurationType = request.getParameter("provisioningHasConfigurationName");
      String shouldDoProvisionString = request.getParameter("provisioningProvisionName");
      String stemScopeString = request.getParameter("provisioningStemScopeName");
      
      final Stem STEM = stem;
      final boolean isDirect = GrouperUtil.booleanValue(configurationType, false);
      
      if (StringUtils.isBlank(targetName)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, "#provisioningTargetNameId",
            TextContainer.retrieveFromRequest().getText().get("provisioningTargetNameRequired")));
        return;
      }
      
      if (!GrouperProvisioningSettings.getTargets(true).containsKey(targetName)) {
        throw new RuntimeException("Invalid target "+targetName);
      }
      
      GrouperProvisioningTarget provisioningTarget = GrouperProvisioningSettings.getTargets(true).get(targetName);
      
      if (!GrouperProvisioningService.isTargetEditable(provisioningTarget, loggedInSubject, STEM)) {
        throw new RuntimeException("Not Allowed!!!");
      }
       
      final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
      attributeValue.setDirectAssignment(isDirect);
      attributeValue.setDoProvision(GrouperUtil.booleanValue(shouldDoProvisionString, true));
      attributeValue.setTargetName(targetName);
      attributeValue.setStemScopeString(stemScopeString);
      
      GrouperProvisioningObjectMetadata provisioningObjectMetadata = GrouperProvisioner.retrieveProvisioner(targetName).retrieveGrouperProvisioningObjectMetadata();
      List<GrouperProvisioningObjectMetadataItem> metadataItems = provisioningObjectMetadata.getGrouperProvisioningObjectMetadataItems();
      List<GrouperProvisioningObjectMetadataItem> metadataItemsForFolder = metadataItems.stream()
          .filter(metadataItem -> metadataItem.isShowForFolder())
          .collect(Collectors.toList());
      
      Map<String, Object> metadataNameValues = new HashMap<String, Object>();
      
      boolean errors = setMetadataValues(request, metadataNameValues, metadataItemsForFolder);
      if (errors) return;
      
      Map<String, String> validateMetadataInputForFolder = provisioningObjectMetadata.validateMetadataInputForFolder(metadataNameValues);
      
      if (validateMetadataInputForFolder != null && validateMetadataInputForFolder.size() > 0) {
        for (String name: validateMetadataInputForFolder.keySet()) {
          String errorMessage = validateMetadataInputForFolder.get(name);
          guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, "#"+name+"_id", errorMessage));
          errors = true;
        }
      }
      
      if (errors) return;
      
      attributeValue.setMetadataNameValues(metadataNameValues);

      //switch over to admin so attributes work
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          if (isDirect) {
            
            final boolean[] FINISHED = new boolean[]{false};
            final RuntimeException[] RUNTIME_EXCEPTION = new RuntimeException[1];
            Thread thread = new Thread(new Runnable() {
      
              public void run() {
      
                try {
                  
                  GrouperSession.startRootSession();      
                  GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, STEM);
                  FINISHED[0] = true;
                  
                } catch (RuntimeException re) {
                  //log incase thread didnt finish when screen was drawing
                  LOG.error("Error updating provisioning stem parts", re);
                  RUNTIME_EXCEPTION[0] = re;
                }
                
              }
              
            });
      
            thread.start();
            
            try {
              thread.join(30000);
            } catch (InterruptedException ie) {
              throw new RuntimeException(ie);
            }
      
            if (RUNTIME_EXCEPTION[0] != null) {
              throw RUNTIME_EXCEPTION[0];
            }
            
            guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2Provisioning.viewProvisioningOnFolder&stemId=" + STEM.getId() + "')"));
            
            if (FINISHED[0]) {
              guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success,
                  TextContainer.retrieveFromRequest().getText().get("provisioningEditSaveSuccess")));
            } else {
              guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
                  TextContainer.retrieveFromRequest().getText().get("provisioningEditSaveSuccessNotFinished")));
            }
            
          } else {
            GrouperProvisioningService.copyConfigFromParent(STEM, targetName);
            guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2Provisioning.viewProvisioningOnFolder&stemId=" + STEM.getId() + "')"));
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success,
                TextContainer.retrieveFromRequest().getText().get("provisioningEditSaveSuccess")));
          }
          
          return null;
        }
      });
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * save changes to provisioning settings for a subject
   * @param request
   * @param response
   */
  public void editProvisioningOnSubjectSave(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Subject subject = null;
    
    try {
  
      if (!PrivilegeHelper.isWheelOrRoot(loggedInSubject)) {
        throw new RuntimeException("Cannot access provisioning.");
      }
      
      grouperSession = GrouperSession.start(loggedInSubject);
  
      subject = UiV2Subject.retrieveSubjectHelper(request, true);

      if (subject == null) {
        return;
      }
      
      final Subject SUBJECT = subject;
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      final ProvisioningContainer provisioningContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getProvisioningContainer();
      
      //switch over to admin so attributes work
      boolean shouldContinue = (Boolean)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          if (!checkProvisioning()) {
            return false;
          }
          
          return true;
        }
      });
      
      if (!shouldContinue) {
        return;
      }
      
      final String targetName = request.getParameter("provisioningTargetName");
      
      if (StringUtils.isBlank(targetName)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, "#provisioningTargetNameId",
            TextContainer.retrieveFromRequest().getText().get("provisioningTargetNameRequired")));
        return;
      }
      
      if (!GrouperProvisioningSettings.getTargets(true).containsKey(targetName)) {
        throw new RuntimeException("Invalid target "+targetName);
      }
      
      GrouperProvisioningTarget provisioningTarget = GrouperProvisioningSettings.getTargets(true).get(targetName);
      
      if (!GrouperProvisioningService.isTargetEditable(provisioningTarget, loggedInSubject, null)) {
        throw new RuntimeException("Not Allowed!!!");
      }
       
      final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
      attributeValue.setTargetName(targetName);
      
      GrouperProvisioningObjectMetadata provisioningObjectMetadata = GrouperProvisioner.retrieveProvisioner(targetName).retrieveGrouperProvisioningObjectMetadata();
      List<GrouperProvisioningObjectMetadataItem> metadataItems = provisioningObjectMetadata.getGrouperProvisioningObjectMetadataItems();
      List<GrouperProvisioningObjectMetadataItem> metadataItemsForSubject = metadataItems.stream()
          .filter(metadataItem -> metadataItem.isShowForMember())
          .collect(Collectors.toList());
      
      Map<String, Object> metadataNameValues = new HashMap<String, Object>();
      
      boolean errors = setMetadataValues(request, metadataNameValues, metadataItemsForSubject);
      if (errors) return;
      
      Map<String, String> validateMetadataInputForFolder = provisioningObjectMetadata.validateMetadataInputForFolder(metadataNameValues);
      
      if (validateMetadataInputForFolder != null && validateMetadataInputForFolder.size() > 0) {
        for (String name: validateMetadataInputForFolder.keySet()) {
          String errorMessage = validateMetadataInputForFolder.get(name);
          guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, "#"+name+"_id", errorMessage));
          errors = true;
        }
      }
      
      if (errors) return;
      
      attributeValue.setMetadataNameValues(metadataNameValues);

      //switch over to admin so attributes work
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          Member member = MemberFinder.findBySubject(theGrouperSession, SUBJECT, true);
          GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, member);
          
          return null;
        }
      });
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2Provisioning.viewProvisioningOnSubject&subjectId=" + subject.getId() + "')"));
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success,
          TextContainer.retrieveFromRequest().getText().get("provisioningEditSaveSuccess")));
      
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * save changes to provisioning settings for a subject membership
   * @param request
   * @param response
   */
  public void editProvisioningOnSubjectMembershipSave(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Subject subject = null;
    Group group = null;
    
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
      
      group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.VIEW).getGroup();
     
      if (group == null) {
        return;
      }
      
      subject = UiV2Subject.retrieveSubjectHelper(request, true);

      if (subject == null) {
        return;
      }

      if (!PrivilegeHelper.isWheelOrRoot(loggedInSubject)) {
        throw new RuntimeException("Cannot access provisioning.");
      }
      
      final Group GROUP = group;
      final Subject SUBJECT = subject;
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      final ProvisioningContainer provisioningContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getProvisioningContainer();
      
      //switch over to admin so attributes work
      boolean shouldContinue = (Boolean)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          if (!checkProvisioning()) {
            return false;
          }
          
          return true;
        }
      });
      
      if (!shouldContinue) {
        return;
      }
      
      final String targetName = request.getParameter("provisioningTargetName");
      
      if (StringUtils.isBlank(targetName)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, "#provisioningTargetNameId",
            TextContainer.retrieveFromRequest().getText().get("provisioningTargetNameRequired")));
        return;
      }
      
      if (!GrouperProvisioningSettings.getTargets(true).containsKey(targetName)) {
        throw new RuntimeException("Invalid target "+targetName);
      }
      
      GrouperProvisioningTarget provisioningTarget = GrouperProvisioningSettings.getTargets(true).get(targetName);
      
      if (!GrouperProvisioningService.isTargetEditable(provisioningTarget, loggedInSubject, null)) {
        throw new RuntimeException("Not Allowed!!!");
      }
       
      final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
      attributeValue.setTargetName(targetName);
      
      GrouperProvisioningObjectMetadata provisioningObjectMetadata = GrouperProvisioner.retrieveProvisioner(targetName).retrieveGrouperProvisioningObjectMetadata();
      List<GrouperProvisioningObjectMetadataItem> metadataItems = provisioningObjectMetadata.getGrouperProvisioningObjectMetadataItems();
      List<GrouperProvisioningObjectMetadataItem> metadataItemsForMembership = metadataItems.stream()
          .filter(metadataItem -> metadataItem.isShowForMembership())
          .collect(Collectors.toList());
      
      Map<String, Object> metadataNameValues = new HashMap<String, Object>();
      
      boolean errors = setMetadataValues(request, metadataNameValues, metadataItemsForMembership);
      if (errors) return;
      
      Map<String, String> validateMetadataInputForFolder = provisioningObjectMetadata.validateMetadataInputForFolder(metadataNameValues);
      
      if (validateMetadataInputForFolder != null && validateMetadataInputForFolder.size() > 0) {
        for (String name: validateMetadataInputForFolder.keySet()) {
          String errorMessage = validateMetadataInputForFolder.get(name);
          guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, "#"+name+"_id", errorMessage));
          errors = true;
        }
      }
      
      if (errors) return;
      
      attributeValue.setMetadataNameValues(metadataNameValues);

      //switch over to admin so attributes work
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          Member member = MemberFinder.findBySubject(theGrouperSession, SUBJECT, true);
          
         GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, GROUP, member);
         
          
          return null;
        }
      });
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2Provisioning.viewProvisioningOnSubjectMembership&subjectId=" + subject.getId() + "&groupId="+GROUP.getId()+"')"));
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success,
          TextContainer.retrieveFromRequest().getText().get("provisioningEditSaveSuccess")));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * save changes to provisioning settings for a group membership
   * @param request
   * @param response
   */
  public void editProvisioningOnGroupMembershipSave(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Subject subject = null;
    Group group = null;
    
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
      
      group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.VIEW).getGroup();
     
      if (group == null) {
        return;
      }
      
      subject = UiV2Subject.retrieveSubjectHelper(request, true);

      if (subject == null) {
        return;
      }

      if (!PrivilegeHelper.isWheelOrRoot(loggedInSubject)) {
        throw new RuntimeException("Cannot access provisioning.");
      }
      
      final Group GROUP = group;
      final Subject SUBJECT = subject;
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      final ProvisioningContainer provisioningContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getProvisioningContainer();
      
      //switch over to admin so attributes work
      boolean shouldContinue = (Boolean)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          if (!checkProvisioning()) {
            return false;
          }
          
          return true;
        }
      });
      
      if (!shouldContinue) {
        return;
      }
      
      final String targetName = request.getParameter("provisioningTargetName");
      
      if (StringUtils.isBlank(targetName)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, "#provisioningTargetNameId",
            TextContainer.retrieveFromRequest().getText().get("provisioningTargetNameRequired")));
        return;
      }
      
      if (!GrouperProvisioningSettings.getTargets(true).containsKey(targetName)) {
        throw new RuntimeException("Invalid target "+targetName);
      }
      
      GrouperProvisioningTarget provisioningTarget = GrouperProvisioningSettings.getTargets(true).get(targetName);
      
      if (!GrouperProvisioningService.isTargetEditable(provisioningTarget, loggedInSubject, null)) {
        throw new RuntimeException("Not Allowed!!!");
      }
       
      final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
      attributeValue.setTargetName(targetName);
      
      GrouperProvisioningObjectMetadata provisioningObjectMetadata = GrouperProvisioner.retrieveProvisioner(targetName).retrieveGrouperProvisioningObjectMetadata();
      List<GrouperProvisioningObjectMetadataItem> metadataItems = provisioningObjectMetadata.getGrouperProvisioningObjectMetadataItems();
      List<GrouperProvisioningObjectMetadataItem> metadataItemsForMembership = metadataItems.stream()
          .filter(metadataItem -> metadataItem.isShowForMembership())
          .collect(Collectors.toList());
      
      Map<String, Object> metadataNameValues = new HashMap<String, Object>();
      
      boolean errors = setMetadataValues(request, metadataNameValues, metadataItemsForMembership);
      if (errors) return;
      
      Map<String, String> validateMetadataInputForFolder = provisioningObjectMetadata.validateMetadataInputForFolder(metadataNameValues);
      
      if (validateMetadataInputForFolder != null && validateMetadataInputForFolder.size() > 0) {
        for (String name: validateMetadataInputForFolder.keySet()) {
          String errorMessage = validateMetadataInputForFolder.get(name);
          guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, "#"+name+"_id", errorMessage));
          errors = true;
        }
      }
      
      if (errors) return;
      
      attributeValue.setMetadataNameValues(metadataNameValues);

      //switch over to admin so attributes work
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          Member member = MemberFinder.findBySubject(theGrouperSession, SUBJECT, true);
          
          GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, GROUP, member);
          
          return null;
        }
      });
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2Provisioning.viewProvisioningOnGroupMembership&subjectId=" + subject.getId() + "&groupId="+GROUP.getId()+"')"));
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success,
          TextContainer.retrieveFromRequest().getText().get("provisioningEditSaveSuccess")));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * save changes to provisioning settings for a group
   * @param request
   * @param response
   */
  public void editProvisioningOnGroupSave(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Group group = null;
    
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
      
      group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.UPDATE).getGroup();
      
      if (group != null) {
        group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.READ).getGroup();
      }
      
      if (group == null) {
        return;
      }
      
      if (!PrivilegeHelper.isWheelOrRoot(loggedInSubject)) {
        throw new RuntimeException("Cannot access provisioning.");
      }
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      final ProvisioningContainer provisioningContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getProvisioningContainer();
      
      //switch over to admin so attributes work
      boolean shouldContinue = (Boolean)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          if (!checkProvisioning()) {
            return false;
          }
          
          if (!provisioningContainer.isCanWriteProvisioning()) {
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
                TextContainer.retrieveFromRequest().getText().get("provisioningNotAllowedToWriteGroup")));
            return false;
          }
  
          return true;
        }
      });
      
      if (!shouldContinue) {
        return;
      }
      
      final String targetName = request.getParameter("provisioningTargetName");
      
      String configurationType = request.getParameter("provisioningHasConfigurationName");
      String shouldDoProvisionString = request.getParameter("provisioningProvisionName");
      
      final Group GROUP = group;
      final boolean isDirect = GrouperUtil.booleanValue(configurationType, false);
      
      if (StringUtils.isBlank(targetName)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, "#provisioningTargetNameId",
            TextContainer.retrieveFromRequest().getText().get("provisioningTargetNameRequired")));
        return;
      }
      
      if (!GrouperProvisioningSettings.getTargets(true).containsKey(targetName)) {
        throw new RuntimeException("Invalid target "+targetName);
      }
      
      GrouperProvisioningTarget provisioningTarget = GrouperProvisioningSettings.getTargets(true).get(targetName);
      
      if (!GrouperProvisioningService.isTargetEditable(provisioningTarget, loggedInSubject, GROUP)) {
        throw new RuntimeException("Not Allowed!!!");
      }
       
      final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
      attributeValue.setDirectAssignment(isDirect);
      attributeValue.setDoProvision(GrouperUtil.booleanValue(shouldDoProvisionString, true));
      attributeValue.setTargetName(targetName);
      
      GrouperProvisioningObjectMetadata provisioningObjectMetadata = GrouperProvisioner.retrieveProvisioner(targetName).retrieveGrouperProvisioningObjectMetadata();
      List<GrouperProvisioningObjectMetadataItem> metadataItems = provisioningObjectMetadata.getGrouperProvisioningObjectMetadataItems();
      List<GrouperProvisioningObjectMetadataItem> metadataItemsForGroup = metadataItems.stream()
          .filter(metadataItem -> metadataItem.isShowForGroup())
          .collect(Collectors.toList());
      
      Map<String, Object> metadataNameValues = new HashMap<String, Object>();
      
      boolean errors = setMetadataValues(request, metadataNameValues, metadataItemsForGroup);
      if (errors) return;
      
      Map<String, String> validateMetadataInputForFolder = provisioningObjectMetadata.validateMetadataInputForFolder(metadataNameValues);
      
      if (validateMetadataInputForFolder != null && validateMetadataInputForFolder.size() > 0) {
        for (String name: validateMetadataInputForFolder.keySet()) {
          String errorMessage = validateMetadataInputForFolder.get(name);
          guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, "#"+name+"_id", errorMessage));
          errors = true;
        }
      }
      
      if (errors) return;
      
      attributeValue.setMetadataNameValues(metadataNameValues);

      //switch over to admin so attributes work
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          if (isDirect) {
            GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, GROUP);
          } else {
            GrouperProvisioningService.copyConfigFromParent(GROUP, targetName);
          }
          
          return null;
        }
      });
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2Provisioning.viewProvisioningOnGroup&groupId=" + group.getId() + "')"));
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success,
          TextContainer.retrieveFromRequest().getText().get("provisioningEditSaveSuccess")));
      
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * @param request
   * @param response
   */
  public void runDaemon(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
        
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

      final ProvisioningContainer provisioningContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getProvisioningContainer();

      if (!provisioningContainer.isCanRunDaemon()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      final boolean[] DONE = new boolean[]{false};
      
      Thread thread = new Thread(new Runnable() {

        @Override
        public void run() {
          GrouperSession grouperSession = GrouperSession.startRootSession();
          try {
            GrouperProvisioningJob.runDaemonStandalone();
            DONE[0] = true;
          } catch (RuntimeException re) {
            LOG.error("Error in running daemon", re);
          } finally {
            GrouperSession.stopQuietly(grouperSession);
          }
          
        }
        
      });

      thread.start();
      
      try {
        thread.join(45000);
      } catch (Exception e) {
        throw new RuntimeException("Exception in thread", e);
      }

      if (DONE[0]) {

        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
                TextContainer.retrieveFromRequest().getText().get("provisioningSuccessDaemonRan")));
        
      } else {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.info, 
            TextContainer.retrieveFromRequest().getText().get("provisioningInfoDaemonInRunning")));

      }
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * @param request
   * @param response
   */
  public void viewProvisioningTargetDetailsOnFolder(final HttpServletRequest request, final HttpServletResponse response) {

    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Stem stem = null;
    
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      stem = UiV2Stem.retrieveStemHelper(request, true).getStem();
      
      if (stem == null) {
        return;
      }
      
      if (!PrivilegeHelper.isWheelOrRoot(loggedInSubject)) {
        throw new RuntimeException("Cannot access provisioning.");
      }
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      final ProvisioningContainer provisioningContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getProvisioningContainer();
      
      //switch over to admin so attributes work
      boolean shouldContinue = (Boolean)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          if (!checkProvisioning()) {
            return false;
          }
          
          if (!provisioningContainer.isCanWriteProvisioning()) {
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
                TextContainer.retrieveFromRequest().getText().get("provisioningNotAllowedToWriteStem")));
            return false;
          }
  
          return true;
        }
      });
      
      if (!shouldContinue) {
        return;
      }
      
      final Stem STEM = stem;
      final String targetName = request.getParameter("provisioningTargetName");
      
      if (StringUtils.isBlank(targetName)) {
        throw new RuntimeException("provisioningTargetName cannot be blank");
      }
      
      //switch over to admin so attributes work
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          provisioningContainer.setTargetName(targetName);
          
          long groupsCount = GrouperProvisioningService.retrieveNumberOfGroupsInTargetInStem(STEM.getId(), targetName);
          long usersCount = GrouperProvisioningService.retrieveNumberOfUsersInTargetInStem(STEM.getId(), targetName);
          long membershipsCount = GrouperProvisioningService.retrieveNumberOfMembershipsInTargetInStem(STEM.getId(), targetName);
          
          provisioningContainer.setGroupsCount(groupsCount);
          provisioningContainer.setUsersCount(usersCount);
          provisioningContainer.setMembershipsCount(membershipsCount);
          return null;
          
        }
      });
            
      //switch over to admin so attributes work
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          GuiStem guiStem = GrouperRequestContainer.retrieveFromRequestOrCreate().getStemContainer().getGuiStem();
          
          addProvisioningBreadcrumbs(guiStem, targetName, "viewProvisioningOnFolder", "stemId", STEM.getId());
          
          guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
              "/WEB-INF/grouperUi2/provisioning/provisioningFolderTargetDetails.jsp"));
          
          return null;
        }
      });
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  
  /**
   * run sync job for a group
   * @param request
   * @param response
   */
  public void runGroupSync(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    Group group;
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.UPDATE).getGroup();
      
      if (group != null) {
        group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.READ).getGroup();
      }
      
      if (group == null) {
        return;
      }
      
      if (!PrivilegeHelper.isWheelOrRoot(loggedInSubject)) {
        throw new RuntimeException("Cannot access provisioning.");
      }
      
      ProvisioningContainer provisioningContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getProvisioningContainer();
      
      if (!provisioningContainer.isCanRunDaemon()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      final String targetName = request.getParameter("provisioningTargetName");
            
      if (StringUtils.isBlank(targetName)) {
        throw new RuntimeException("provisioningTargetName cannot be blank");
      }
      
      ProvisioningMessage provisioningMessage = new ProvisioningMessage();
      provisioningMessage.setGroupIdsForSync(new String[] {group.getId()});
      provisioningMessage.setBlocking(true);
      provisioningMessage.send(targetName);
      
      AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.PROVISIONER_SYNC_RUN_GROUP, "groupId", group.getId(), "provisionerName", targetName);
      auditEntry.setDescription("Ran provisioner sync for "+targetName+" on group " + group.getName());
      provisionerSaveAudit(auditEntry);
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success,
          TextContainer.retrieveFromRequest().getText().get("provisioningGroupSyncSuccess")));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * 
   * @param auditEntry
   */
  private static void provisionerSaveAudit(final AuditEntry auditEntry) {
    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT,
          new HibernateHandler() {
              public Object callback(HibernateHandlerBean hibernateHandlerBean)
                  throws GrouperDAOException {
                auditEntry.saveOrUpdate(true);
                return null;
              }
        });

  }
  
  
  /**
   * @param request
   * @param response
   */
  public void viewProvisioningTargetLogsOnGroup(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Group group = null;
    
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.UPDATE).getGroup();
      
      if (group == null) {
        return;
      }
      
      if (!PrivilegeHelper.isWheelOrRoot(loggedInSubject)) {
        throw new RuntimeException("Cannot access provisioning.");
      }
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      final ProvisioningContainer provisioningContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getProvisioningContainer();
      
      //switch over to admin so attributes work
      boolean shouldContinue = (Boolean)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          if (!checkProvisioning()) {
            return false;
          }
          
          if (!provisioningContainer.isCanWriteProvisioning()) {
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
                TextContainer.retrieveFromRequest().getText().get("provisioningNotAllowedToWriteGroup")));
            return false;
          }
  
          return true;
        }
      });
      
      if (!shouldContinue) {
        return;
      }
      
      final Group GROUP = group;
      final String targetName = request.getParameter("provisioningTargetName");
      
      if (StringUtils.isBlank(targetName)) {
        throw new RuntimeException("provisioningTargetName cannot be blank");
      }
      
      //switch over to admin so attributes work
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          provisioningContainer.setTargetName(targetName);
          
          GuiPaging guiPaging = provisioningContainer.getGuiPaging();
          QueryOptions queryOptions = new QueryOptions();

          GrouperPagingTag2.processRequest(request, guiPaging, queryOptions, "uiV2.provisioning.logs.default.page.size");

          List<GcGrouperSyncLog> gcGrouperSyncLogs = GrouperProvisioningService.retrieveGcGrouperSyncLogs(targetName, GROUP.getUuid(), queryOptions);
          provisioningContainer.setGcGrouperSyncLogs(gcGrouperSyncLogs);
          
          setGrouperProvisioningAttributeValues(GROUP, targetName);
          
          guiPaging.setTotalRecordCount(queryOptions.getQueryPaging().getTotalRecordCount());
          return null;
          
        }
      });
            
      //switch over to admin so attributes work
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          GuiGroup guiGroup = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup();
          
          addProvisioningBreadcrumbs(guiGroup, targetName, "viewProvisioningOnGroup", "groupId", GROUP.getId());
          
          guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
              "/WEB-INF/grouperUi2/provisioning/provisioningGroupLogs.jsp"));
          
          return null;
        }
      });
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  
  /**
   * @param request
   * @param response
   */
  public void viewProvisioningTargetDetailsOnGroup(final HttpServletRequest request, final HttpServletResponse response) {

    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Group group = null;
    
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.UPDATE).getGroup();
      
      if (group == null) {
        return;
      }
      
      if (!PrivilegeHelper.isWheelOrRoot(loggedInSubject)) {
        throw new RuntimeException("Cannot access provisioning.");
      }
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      final ProvisioningContainer provisioningContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getProvisioningContainer();
      
      //switch over to admin so attributes work
      boolean shouldContinue = (Boolean)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          if (!checkProvisioning()) {
            return false;
          }
          
          if (!provisioningContainer.isCanWriteProvisioning()) {
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
                TextContainer.retrieveFromRequest().getText().get("provisioningNotAllowedToWriteGroup")));
            return false;
          }
  
          return true;
        }
      });
      
      if (!shouldContinue) {
        return;
      }
      
      final Group GROUP = group;
      final String targetName = request.getParameter("provisioningTargetName");
      
      if (StringUtils.isBlank(targetName)) {
        throw new RuntimeException("provisioningTargetName cannot be blank");
      }
      
      //switch over to admin so attributes work
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          provisioningContainer.setTargetName(targetName);
          
          GcGrouperSyncGroup gcGrouperSyncGroup = GrouperProvisioningService.retrieveGcGrouperGroup(GROUP.getId(), targetName);
          long usersCount = GrouperProvisioningService.retrieveNumberOfUsersInTargetInGroup(GROUP.getId(), targetName);
          provisioningContainer.setUsersCount(usersCount);
          provisioningContainer.setGcGrouperSyncGroup(gcGrouperSyncGroup);
          
          setGrouperProvisioningAttributeValues(GROUP, targetName);
          
          return null;
          
        }
      });
            
      //switch over to admin so attributes work
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          GuiGroup guiGroup = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup();
          
          addProvisioningBreadcrumbs(guiGroup, targetName, "viewProvisioningOnGroup", "groupId", GROUP.getId());
          
          guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
              "/WEB-INF/grouperUi2/provisioning/provisioningGroupTargetDetails.jsp"));
          
          return null;
        }
      });
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  
  /**
   * make sure attribute def is there and enabled etc
   * @return true if k
   */
  private boolean checkProvisioning() {
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

    if (!GrouperProvisioningSettings.provisioningInUiEnabled()) {
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
          TextContainer.retrieveFromRequest().getText().get("provisioningNotEnabledError")));
      return false;
    }

    AttributeDef attributeDefBase = null;
    try {
      
      attributeDefBase = GrouperProvisioningAttributeNames.retrieveAttributeDefBaseDef();

    } catch (RuntimeException e) {
      if (attributeDefBase == null) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("provisioningAttributeNotFoundError")));
        return false;
      }
      throw e;
    }
    
    return true;
  }
  
  

}
