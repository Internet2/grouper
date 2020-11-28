package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeNames;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeValue;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningJob;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningService;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningSettings;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTarget;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.audit.AuditTypeBuiltin;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.ProvisioningMessage;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiObjectBase;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiStem;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiSubject;
import edu.internet2.middleware.grouper.grouperUi.beans.api.provisioning.GuiGrouperProvisioningAttributeValue;
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
import edu.internet2.middleware.grouper.messaging.GrouperBuiltinMessagingSystem;
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
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageQueueType;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageSendParam;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessagingEngine;
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
          provisioningContainer.setGuiGrouperProvisioningAttributeValues(GuiGrouperProvisioningAttributeValue.convertFromGrouperProvisioningAttributeValues(allProvisioningAttributeValues));
          
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
          
          Member member = MemberFinder.findBySubject(theGrouperSession, SUBJECT, false);
          if (member != null) {
            List<GcGrouperSyncMember> gcGrouperSyncMembers = GrouperProvisioningService.retrieveGcGrouperSyncMembers(member.getId());
            provisioningContainer.setGcGrouperSyncMembers(gcGrouperSyncMembers);
          }
          
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
    Subject subject = null;
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
      
      subject = UiV2Subject.retrieveSubjectHelper(request, true);

      if (subject == null) {
        return;
      }
      
      if (!PrivilegeHelper.isWheelOrRoot(loggedInSubject)) {
        throw new RuntimeException("Cannot access provisioning.");
      }
      
      final Subject SUBJECT = subject;
      
      final String targetName = request.getParameter("provisioningTargetName");
      
      if (StringUtils.isBlank(targetName)) {
        throw new RuntimeException("provisioningTargetName cannot be blank");
      }

      final String groupSyncMemberId = request.getParameter("groupSyncMemberId");
      
      if (StringUtils.isBlank(groupSyncMemberId)) {
        throw new RuntimeException("groupSyncMemberId cannot be blank");
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
          
          GcGrouperSyncMember gcGrouperSyncMember = GcGrouperSyncDao.retrieveByProvisionerName(null, targetName).getGcGrouperSyncMemberDao().memberRetrieveById(groupSyncMemberId);
          provisioningContainer.setGcGrouperSyncMember(gcGrouperSyncMember);
          
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
      
      final String targetName = request.getParameter("provisioningTargetName");
      
      if (StringUtils.isBlank(targetName)) {
        throw new RuntimeException("provisioningTargetName cannot be blank");
      }

      final String groupSyncMembershipId = request.getParameter("groupSyncMembershipId");
      
      if (StringUtils.isBlank(groupSyncMembershipId)) {
        throw new RuntimeException("groupSyncMembershipId cannot be blank");
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
          
          GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, targetName);
          GcGrouperSyncMembership gcGrouperSyncMembership = gcGrouperSync.getGcGrouperSyncMembershipDao().membershipRetrieveById(groupSyncMembershipId);
          provisioningContainer.setGcGrouperSyncMembership(gcGrouperSyncMembership);
          
          if (gcGrouperSyncMembership != null) {
            
            GcGrouperSyncGroup gcGrouperSyncGroup = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveById(gcGrouperSyncMembership.getGrouperSyncGroupId());
            gcGrouperSyncMembership.setGrouperSyncGroup(gcGrouperSyncGroup);
            
            GcGrouperSyncMember gcGrouperSyncMember = gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveById(gcGrouperSyncMembership.getGrouperSyncMemberId());
            gcGrouperSyncMembership.setGrouperSyncMember(gcGrouperSyncMember);
          }
          
          
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
  
    Group group = null;
    
    Subject subject = null;
    
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
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          if (!checkProvisioning()) {
            return null;
          }
          
          Member member = MemberFinder.findBySubject(theGrouperSession, SUBJECT, false);
          if (member != null) {
            List<GcGrouperSyncMembership> gcGrouperSyncMemberships = GrouperProvisioningService.retrieveGcGrouperSyncMemberships(member.getId(), GROUP.getId());
            provisioningContainer.setGcGrouperSyncMemberships(gcGrouperSyncMemberships);
          }
          
          GuiSubject guiSubject = GrouperRequestContainer.retrieveFromRequestOrCreate().getSubjectContainer().getGuiSubject();
          GuiGroup guiGroup = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup();
          GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().setGuiGroup(guiGroup);
          addProvisioningBreadcrumbs(guiGroup, null, null, null, null);
          //addProvisioningBreadcrumbs(guiGroup, n, "viewProvisioningOnGroup", "groupId", GROUP.getId());
          
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
    Group group = null;
    Subject subject = null;
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
      
      final String targetName = request.getParameter("provisioningTargetName");
      
      if (StringUtils.isBlank(targetName)) {
        throw new RuntimeException("provisioningTargetName cannot be blank");
      }

      final String groupSyncMembershipId = request.getParameter("groupSyncMembershipId");
      
      if (StringUtils.isBlank(groupSyncMembershipId)) {
        throw new RuntimeException("groupSyncMembershipId cannot be blank");
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
          
          GcGrouperSyncMembership gcGrouperSyncMembership = GcGrouperSyncDao.retrieveByProvisionerName(null, targetName).getGcGrouperSyncMembershipDao().membershipRetrieveById(groupSyncMembershipId);
          provisioningContainer.setGcGrouperSyncMembership(gcGrouperSyncMembership);
          
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
  
    Group group = null;
    
    Subject subject = null;
    
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
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          if (!checkProvisioning()) {
            return null;
          }
          
          Member member = MemberFinder.findBySubject(theGrouperSession, SUBJECT, false);
          if (member != null) {
            List<GcGrouperSyncMembership> gcGrouperSyncMemberships = GrouperProvisioningService.retrieveGcGrouperSyncMemberships(member.getId(), GROUP.getId());
            provisioningContainer.setGcGrouperSyncMemberships(gcGrouperSyncMemberships);
          }
          
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
        guiGrouperProvisioningAttributeValue.setProvisionable(gcGrouperSyncGroup.isProvisionable());
      }
      
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
      String message = provisioningMessage.toJson();  
      
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
       @Override
       public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        
          GrouperMessagingEngine.send(
               new GrouperMessageSendParam().assignGrouperMessageSystemName(GrouperBuiltinMessagingSystem.BUILTIN_NAME)
                .assignQueueType(GrouperMessageQueueType.queue)
                .assignQueueOrTopicName("grouperProvisioningControl_"+targetName)
                .assignAutocreateObjects(true)
                .addMessageBody(message));
          return null;
       }
      });
      
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
