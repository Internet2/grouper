package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiSubject;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction.GuiMessageType;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.UserLifecycleEventContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.UserLifecycleEventsContainer;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.userLifecycle.UserLifecycleAttributeNames;
import edu.internet2.middleware.grouper.userLifecycle.UserLifecycleService;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.subject.Subject;

public class UiV2UserLifecycleEvents {
  
  /** logger */
  protected static final Log LOG = edu.internet2.middleware.grouper.util.GrouperUtil.getLog(UiV2UserLifecycleEvents.class);

  /**
   * view users lifecycle events
   * @param request
   * @param response
   */
  public void viewUserLifecycleEvents(final HttpServletRequest request, final HttpServletResponse response) {

    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
      
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
      
      showUserLifecycleEvents();
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * remove members
   * @param request
   * @param response
   */
  public void removeMembers(final HttpServletRequest request, final HttpServletResponse response) {

    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
      
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      Set<String> membershipsIds = new HashSet<String>();
      
      for (int i=0;i<1000;i++) {
        String membershipId = request.getParameter("membershipRow_" + i + "[]");
        if (!StringUtils.isBlank(membershipId)) {
          membershipsIds.add(membershipId);
        }
      }

      if (membershipsIds.size() == 0) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, TextContainer.retrieveFromRequest().getText().get("miscellaneousUserLifecycleEventsListNoMembershipsSelected")));
        return;
      }
      
      int successes = 0;
      int failures = 0;
      
      Set<Group> successRemoveGroups = new LinkedHashSet<Group>();
      
      for (String membershipId : membershipsIds) {
        try {
          final Membership membership = GrouperDAOFactory.getFactory().getMembership().findByUuid(membershipId, true, false);
  
          final Group group = membership.getOwnerGroup();

          boolean allowed = (Boolean)GrouperSession.callbackGrouperSession(GrouperSession.staticGrouperSession().internal_getRootSession(), 
              new GrouperSessionHandler() {
            
            @Override
            public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
              if (group.canHavePrivilege(loggedInSubject, AccessPrivilege.ADMIN.getName(), false)) {
                return true;
              }
              return false;
            }
          });
          
          if (!allowed) {
            failures++;
          } else {
            group.deleteMember(membership.getMember(), false);
            successes++;
            successRemoveGroups.add(group);
          }
          
        } catch (Exception e) {
          LOG.warn("Error with membership: " + membershipId + ", user: " + loggedInSubject, e);
          failures++;
        }
      }
  
      GrouperRequestContainer.retrieveFromRequestOrCreate().getUserLifecycleEventsContainer().setSuccessCount(successes);
      GrouperRequestContainer.retrieveFromRequestOrCreate().getUserLifecycleEventsContainer().setFailureCount(failures);
      
      if (failures > 0) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, TextContainer.retrieveFromRequest().getText().get("miscellaneousUserLifecycleEventsListDeleteMembersErrors")));
      } else {
        boolean membershipsMayPropagate = GrouperUtil.checkIfMembershipsMayPropagate(successRemoveGroups);

        if (membershipsMayPropagate) {
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, TextContainer.retrieveFromRequest().getText().get("miscellaneousUserLifecycleEventsListDeleteMembersSuccessesButPropagating")));
        } else {
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, TextContainer.retrieveFromRequest().getText().get("miscellaneousUserLifecycleEventsListDeleteMembersSuccesses"))); 
        }
      }

      showUserLifecycleEvents();
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * keep members
   * @param request
   * @param response
   */
  public void keepMembers(final HttpServletRequest request, final HttpServletResponse response) {

    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
      
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

      Set<String> membershipsIds = new HashSet<String>();
      
      for (int i=0;i<1000;i++) {
        String membershipId = request.getParameter("membershipRow_" + i + "[]");
        if (!StringUtils.isBlank(membershipId)) {
          membershipsIds.add(membershipId);
        }
      }

      if (membershipsIds.size() == 0) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, TextContainer.retrieveFromRequest().getText().get("miscellaneousUserLifecycleEventsListNoMembershipsSelected")));
        return;
      }
      
      GrouperSession.callbackGrouperSession(GrouperSession.staticGrouperSession().internal_getRootSession(), 
          new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          int successes = 0;
          int failures = 0;
          
          final Member loggedInMember = MemberFinder.findBySubject(grouperSession, loggedInSubject, true);
          
          for (String membershipId : membershipsIds) {
            try {
              final Membership membership = GrouperDAOFactory.getFactory().getMembership().findByUuid(membershipId, true, false);
      
              final Group group = membership.getOwnerGroup();

              boolean allowed = group.canHavePrivilege(loggedInSubject, AccessPrivilege.ADMIN.getName(), false);
              
              if (!allowed) {
                failures++;
              } else {

                HibernateSession.callbackHibernateSession(
                    GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT,
                    new HibernateHandler() {
                      public Object callback(HibernateHandlerBean hibernateHandlerBean)
                          throws GrouperDAOException {

                        Set<AttributeAssign> inFlightAssignments = membership.getAttributeDelegate().retrieveAssignments(UserLifecycleAttributeNames.retrieveInFlightAttributeDefNameMarker());
                        for (AttributeAssign inFlightAssignment : inFlightAssignments) {
                          
                          Long eventId = inFlightAssignment.getAttributeValueDelegate().retrieveValueInteger(UserLifecycleAttributeNames.userLifecycleStemName() + ":" + UserLifecycleAttributeNames.USER_LIFECYCLE_MSHIP_IN_FLIGHT_LIFECYCLE_EVENT_ID);
                          inFlightAssignment.delete();
                          
                          if (eventId != null) {
                            AttributeAssign attributeAssign = membership.getAttributeDelegate().addAttribute(UserLifecycleAttributeNames.retrieveHistoryAttributeDefNameMarker()).getAttributeAssign();
                            AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(UserLifecycleAttributeNames.userLifecycleStemName() + ":" + UserLifecycleAttributeNames.USER_LIFECYCLE_MSHIP_HISTORY_LIFECYCLE_EVENT_ID, true);
                            attributeAssign.getAttributeValueDelegate().assignValueInteger(attributeDefName.getName(), eventId);
                            
                            attributeDefName = AttributeDefNameFinder.findByName(UserLifecycleAttributeNames.userLifecycleStemName() + ":" + UserLifecycleAttributeNames.USER_LIFECYCLE_MSHIP_HISTORY_ADDED_MICROS, true);
                            attributeAssign.getAttributeValueDelegate().assignValueInteger(attributeDefName.getName(), System.currentTimeMillis() * 1000);

                            attributeDefName = AttributeDefNameFinder.findByName(UserLifecycleAttributeNames.userLifecycleStemName() + ":" + UserLifecycleAttributeNames.USER_LIFECYCLE_MSHIP_HISTORY_APPROVED_BY, true);
                            attributeAssign.getAttributeValueDelegate().assignValueInteger(attributeDefName.getName(), loggedInMember.getInternalId());

                            attributeAssign.saveOrUpdate();
                          }
                        }
                        
                        return null;
                      }
                    });
                
                successes++;
              }
              
            } catch (Exception e) {
              LOG.warn("Error with membership: " + membershipId + ", user: " + loggedInSubject, e);
              failures++;
            }
          } 
          
          GrouperRequestContainer.retrieveFromRequestOrCreate().getUserLifecycleEventsContainer().setSuccessCount(successes);
          GrouperRequestContainer.retrieveFromRequestOrCreate().getUserLifecycleEventsContainer().setFailureCount(failures);
          
          return null;
        }
      });
      
      if (GrouperRequestContainer.retrieveFromRequestOrCreate().getUserLifecycleEventsContainer().getFailureCount() > 0) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, TextContainer.retrieveFromRequest().getText().get("miscellaneousUserLifecycleEventsListKeepMembersErrors")));
      } else {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, TextContainer.retrieveFromRequest().getText().get("miscellaneousUserLifecycleEventsListKeepMembersSuccesses"))); 
      }
      
      showUserLifecycleEvents();
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  private void showUserLifecycleEvents() {
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

    UserLifecycleEventsContainer userLifecycleEventsContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getUserLifecycleEventsContainer();
    List<UserLifecycleEventContainer> userLifecycleEventContainers = new ArrayList<>();
    userLifecycleEventsContainer.setUserLifecycleEventContainers(userLifecycleEventContainers);
    
    Collection<Object[]> membershipEvents = UserLifecycleService.retrieveInFlightMembershipEventsSecure();
    
    if (membershipEvents.size() > 0) {
      Set<String> groupIds = new LinkedHashSet<>();
      Set<MultiKey> sourceIdsAndSubjectIds = new LinkedHashSet<>();
      for (Object[] membershipEvent : membershipEvents) {
        String groupId = (String)membershipEvent[1];
        String subjectId = (String)membershipEvent[2];
        String sourceId = (String)membershipEvent[3];
        if (!StringUtils.isBlank(groupId)) {
          groupIds.add(groupId);
        }
        
        sourceIdsAndSubjectIds.add(new MultiKey(sourceId, subjectId));
      }
      
      Set<Group> groups = GrouperDAOFactory.getFactory().getGroup().findByUuidsSecure(groupIds, null);
      Set<GuiGroup> guiGroups = GuiGroup.convertFromGroups(groups);
      Map<String, GuiGroup> guiGroupsById = new LinkedHashMap<>();
      for (GuiGroup guiGroup : guiGroups) {
        guiGroupsById.put(guiGroup.getGroup().getId(), guiGroup);
      }
      
      Map<MultiKey, Subject> sourceIdsAndSubjectIdsToSubject = SubjectFinder.findBySourceIdsAndSubjectIds(sourceIdsAndSubjectIds, false);
      
      for (Object[] membershipEvent : membershipEvents) {
        UserLifecycleEventContainer userLifecycleEventContainer = new UserLifecycleEventContainer();
        String membershipId = (String)membershipEvent[0];
        String groupId = (String)membershipEvent[1];
        String subjectId = (String)membershipEvent[2];
        String sourceId = (String)membershipEvent[3];
        Long eventTimeMicros = (Long)membershipEvent[5];
        Long membershipRemovalMicros = (Long)membershipEvent[6];
        String eventDescription = (String)membershipEvent[7];

        userLifecycleEventContainer.setMembershipId(membershipId);
        userLifecycleEventContainer.setGuiGroup(guiGroupsById.get(groupId));
        userLifecycleEventContainer.setGuiSubject(new GuiSubject(sourceIdsAndSubjectIdsToSubject.get(new MultiKey(sourceId, subjectId))));
        userLifecycleEventContainer.setEventDate(new Date(eventTimeMicros / 1000));
        userLifecycleEventContainer.setEventDescription(eventDescription);
        
        if (membershipRemovalMicros != null) {
          userLifecycleEventContainer.setMembershipRemovalDate(new Date(membershipRemovalMicros / 1000));
        }
        
        userLifecycleEventContainers.add(userLifecycleEventContainer);
      }
    }
    
    guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
        "/WEB-INF/grouperUi2/userLifecycle/userLifecycleEventsList.jsp"));
  }
}
