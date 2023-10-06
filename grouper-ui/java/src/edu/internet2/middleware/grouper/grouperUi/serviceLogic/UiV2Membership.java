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
package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.FieldType;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningService;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningSettings;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTarget;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.audit.AuditType;
import edu.internet2.middleware.grouper.audit.AuditTypeFinder;
import edu.internet2.middleware.grouper.audit.UserAuditQuery;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiAttributeDef;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGcGrouperSyncMembership;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiMembership;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiMembershipSubjectContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiPITGroup;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiStem;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction.GuiMessageType;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GuiAuditEntry;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.MembershipGuiContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.membership.MembershipContainer;
import edu.internet2.middleware.grouper.membership.MembershipPath;
import edu.internet2.middleware.grouper.membership.MembershipPathGroup;
import edu.internet2.middleware.grouper.membership.MembershipPathNode;
import edu.internet2.middleware.grouper.membership.MembershipResult;
import edu.internet2.middleware.grouper.membership.MembershipSubjectContainer;
import edu.internet2.middleware.grouper.membership.MembershipType;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.pit.PITField;
import edu.internet2.middleware.grouper.pit.PITGroup;
import edu.internet2.middleware.grouper.pit.PITGroupSet;
import edu.internet2.middleware.grouper.pit.PITMember;
import edu.internet2.middleware.grouper.pit.PITMembership;
import edu.internet2.middleware.grouper.pit.PITMembershipView;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.subj.SubjectHelper;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUserData;
import edu.internet2.middleware.grouper.userData.GrouperUserDataApi;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMembership;
import edu.internet2.middleware.subject.Subject;

/**
 * operations on memberships
 * @author mchyzer
 *
 */
public class UiV2Membership {

  /**
   * get the field from the request
   * @param request
   * @return the field or null if not found
   */
  public static Field retrieveFieldHelper(HttpServletRequest request, boolean displayErrorIfProblem) {
  
    //initialize the bean
    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    
    MembershipGuiContainer guiMembershipContainer = grouperRequestContainer.getMembershipGuiContainer();

    Field field = null;
  
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

    String fieldId = request.getParameter("fieldId");
    String fieldName = request.getParameter("field");

    if (StringUtils.isBlank(fieldName)) {
      fieldName = request.getParameter("fieldName");
    }
    
    boolean addedError = false;
    
    if (StringUtils.isBlank(fieldId) && StringUtils.isBlank(fieldName)) {
      if (!displayErrorIfProblem) {
        return null;
      }
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
          TextContainer.retrieveFromRequest().getText().get("membershipCantFindFieldId")));
      addedError = true;
    }
    
    if (!StringUtils.isBlank(fieldId)) {
      field = FieldFinder.findById(fieldId, false);
    } else if (!StringUtils.isBlank(fieldName)) {
      field = FieldFinder.find(fieldName, false);
    }
    
    if (field != null) {
      guiMembershipContainer.setField(field);      

    } else {
      
      if (!addedError) {
        if (!displayErrorIfProblem) {
          return null;
        }
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("membershipCantFindField")));
        addedError = true;
      }
      
    }
  
    //go back to the main screen, cant find group
    if (addedError) {
      if (displayErrorIfProblem) {
        guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
            "/WEB-INF/grouperUi2/index/indexMain.jsp"));
      }
    }

    return field;
  }
  
  /**
   * trace membership
   * @param request
   * @param response
   */
  public void traceMembership(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;

    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);

      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

      Group group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.READ).getGroup();

      if (group == null) {
        return;
      }
  
      Subject subject = UiV2Subject.retrieveSubjectHelper(request, true);

      if (subject == null) {
        return;
      }
      
      Member member = MemberFinder.findBySubject(grouperSession, subject, false);

      if (member == null) {
        
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("membershipTraceNoMembershipFound")));
        
        return;
      }
      
      Field field = UiV2Membership.retrieveFieldHelper(request, true);
      
      if (field == null) {
        return;
      }      
      
      MembershipGuiContainer membershipGuiContainer = grouperRequestContainer.getMembershipGuiContainer();
      
      //see where to go back to
      if (StringUtils.equalsIgnoreCase(request.getParameter("backTo"), "subject")) {
        membershipGuiContainer.setTraceMembershipFromSubject(true);
      }
      if (StringUtils.equalsIgnoreCase(request.getParameter("backTo"), "membership")) {
        membershipGuiContainer.setTraceMembershipFromMembership(true);
      }
      
      membershipGuiContainer.setTraceMembershipTimelineShowUserAudit(GrouperUtil.booleanValue(request.getParameter("showUserAudit"), true));
      membershipGuiContainer.setTraceMembershipTimelineShowPITAudit(GrouperUtil.booleanValue(request.getParameter("showPITAudit"), true));
      membershipGuiContainer.setTraceMembershipTimelineShowProvisioningEvents(GrouperUtil.booleanValue(request.getParameter("showProvisioningEvents"), true));

      //this is a subobject
      grouperRequestContainer.getGroupContainer().getGuiGroup().setShowBreadcrumbLink(true);
      grouperRequestContainer.getSubjectContainer().getGuiSubject().setShowBreadcrumbLink(true);

      // point in time objects
      PITGroup pitGroup = GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdActive(group.getId(), false);
      PITMember pitMember = GrouperDAOFactory.getFactory().getPITMember().findBySourceIdActive(member.getId(), false);
      PITField pitField = GrouperDAOFactory.getFactory().getPITField().findBySourceIdActive(field.getId(), true);
      
      Set<PITGroup> pitGroupsForTimelineStates = new LinkedHashSet<PITGroup>();
      Set<String> memberIdsForTimelineAuditQuery = new LinkedHashSet<String>();
      memberIdsForTimelineAuditQuery.add(pitMember.getSourceId());
      
      MembershipPathGroup membershipPathGroup = MembershipPathGroup.analyze(group, member, field);
      traceMembershipHelperCurrent(membershipPathGroup, subject, memberIdsForTimelineAuditQuery, pitGroupsForTimelineStates);

      // this should always be the members field, but check just in case
      if (field.getId().equals(Group.getDefaultList().getUuid())) {
        
        if (GrouperUtil.nonNull(membershipPathGroup.getMembershipPaths()).size() == 0) {
          traceMembershipsHelperFormer(pitGroup, pitMember, pitField, memberIdsForTimelineAuditQuery, pitGroupsForTimelineStates);
        }
        
        if (GrouperUtil.booleanValue(request.getParameter("showTimeline"), false)) {
          traceMembershipsHelperTimeline(pitMember, pitField, memberIdsForTimelineAuditQuery, pitGroupsForTimelineStates);
        }
      }
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/membership/traceMembership.jsp"));

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  private void traceMembershipsHelperFormer(PITGroup pitGroup, PITMember pitMember, PITField pitField, Set<String> memberIdsForTimelineAuditQuery, Set<PITGroup> pitGroupsForTimelineStates) {
    
    if (pitGroup == null || pitMember == null) {
      // not in pit (yet?)
      return;
    }
    
    GrouperSession loggedInGrouperSession = GrouperSession.staticGrouperSession();

    List<Timestamp> endTimes = new ArrayList<Timestamp>();
    List<PITGroup> memberPITGroups = new ArrayList<PITGroup>();
    
    boolean proceed = (Boolean)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      @Override
      public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {

        Set<PITMembershipView> pitMemberships = GrouperDAOFactory.getFactory().getPITMembershipView().findAllByPITOwnerAndPITMemberAndPITField(pitGroup.getId(), pitMember.getId(), pitField.getId(), null, null, null);
        PITMembershipView latestPITMembership = null;
        
        for (PITMembershipView pitMembership : pitMemberships) {
          if (pitMembership.getEndTime() != null) {
            if (latestPITMembership == null || latestPITMembership.getEndTime().getTime() < pitMembership.getEndTime().getTime()) {
              latestPITMembership = pitMembership;
            }
          }
        }
        
        if (latestPITMembership == null) {
          // nothing to show
          return false;
        }
        
        boolean isWheelOrRoot = PrivilegeHelper.isWheelOrRoot(loggedInGrouperSession.getSubject());
        
        String pitGroupSetId = latestPITMembership.getGroupSetId();
        PITGroup previousMemberPITGroup = null;
        
        boolean firstNode = true;

        while (true) {
          PITGroupSet pitGroupSet = GrouperDAOFactory.getFactory().getPITGroupSet().findById(pitGroupSetId, true);
          PITGroup memberPITGroup = pitGroupSet.getMemberPITGroup();
                
          // check access
          if (!isWheelOrRoot) {
            Group memberGroup = GroupFinder.findByUuid(memberPITGroup.getSourceId(), false);
            if (memberGroup == null || !memberGroup.canHavePrivilege(loggedInGrouperSession.getSubject(), "read", false)) {
              // no access so return
              return false;
            }
          }
                      
          if (firstNode) {
            Set<PITMembership> immediatePITMemberships = GrouperDAOFactory.getFactory().getPITMembership().findAllByPITOwnerAndPITMemberAndPITField(memberPITGroup.getId(), pitMember.getId(), pitField.getId());
            PITMembership mostRecentImmediatePITMembership = null;
            for (PITMembership immediatePITMembership : immediatePITMemberships) {
              if (mostRecentImmediatePITMembership == null || 
                  immediatePITMembership.getEndTime() == null ||
                  (mostRecentImmediatePITMembership.getEndTime() != null && immediatePITMembership.getEndTime().getTime() > mostRecentImmediatePITMembership.getEndTime().getTime())) {
                mostRecentImmediatePITMembership = immediatePITMembership;
              }
            }
            
            endTimes.add(mostRecentImmediatePITMembership.getEndTime());
          } else {
            Set<PITGroupSet> immediatePITGroupSets = GrouperDAOFactory.getFactory().getPITGroupSet().findAllImmediateByPITOwnerAndPITMemberAndPITField(memberPITGroup.getId(), previousMemberPITGroup.getId(), pitField.getId());
            PITGroupSet mostRecentImmediatePITGroupSet = null;
            for (PITGroupSet immediatePITGroupSet : immediatePITGroupSets) {
              if (mostRecentImmediatePITGroupSet == null || 
                  immediatePITGroupSet.getEndTime() == null ||
                  (mostRecentImmediatePITGroupSet.getEndTime() != null && immediatePITGroupSet.getEndTime().getTime() > mostRecentImmediatePITGroupSet.getEndTime().getTime())) {
                mostRecentImmediatePITGroupSet = immediatePITGroupSet;
              }
            }
            
            endTimes.add(mostRecentImmediatePITGroupSet.getEndTime());
          }

          memberPITGroups.add(memberPITGroup);
         
          firstNode = false;
         
          if (pitGroupSet.getDepth() < 1) {
            break;
          }
          
          pitGroupSetId = pitGroupSet.getParentId();
          previousMemberPITGroup = memberPITGroup;
        }

        return true;
      }
    });
    
    if (!proceed) {
      return;
    }
    
    // text should be rendered as the logged in user to ensure subject privacy
    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    MembershipGuiContainer membershipGuiContainer = grouperRequestContainer.getMembershipGuiContainer();
    
    StringBuilder result = new StringBuilder();
    
    int pathLineNumber = 0;
    membershipGuiContainer.setLineNumber(pathLineNumber);
    
    for (int i = 0; i < memberPITGroups.size(); i++) {
      PITGroup memberPITGroup = memberPITGroups.get(i);
      Timestamp endTime = endTimes.get(i);
      
      pitGroupsForTimelineStates.add(memberPITGroup);
      
      for (PITMember currPITMember : GrouperDAOFactory.getFactory().getPITMember().findPITMembersBySubjectIdSourceAndType(memberPITGroup.getSourceId(), "g:gsa", "group")) {
        memberIdsForTimelineAuditQuery.add(currPITMember.getSourceId());
      }
                  
      if (i == 0) {
        if (endTime == null) {
          result.append(TextContainer.retrieveFromRequest().getText().get("pitMembershipTracePathFirstLineCurrentMembership")).append("\n");
        } else {
          membershipGuiContainer.setGuiAuditDateCurrent(endTime);
          result.append(TextContainer.retrieveFromRequest().getText().get("pitMembershipTracePathFirstLinePreviousMembership")).append("\n");
        }
      } else {
        if (endTime == null) {
          result.append(TextContainer.retrieveFromRequest().getText().get("pitMembershipTraceGroupMemberOfCurrentMembership")).append("\n");
        } else {
          membershipGuiContainer.setGuiAuditDateCurrent(endTime);
          result.append(TextContainer.retrieveFromRequest().getText().get("pitMembershipTraceGroupMemberOfPreviousMembership")).append("\n");
        }
      }
      
      pathLineNumber++;
      membershipGuiContainer.setLineNumber(pathLineNumber);

      membershipGuiContainer.setGuiPITGroupCurrent(new GuiPITGroup(memberPITGroup));
      
      result.append(TextContainer.retrieveFromRequest().getText().get("pitMembershipTraceGroupLine")).append("\n");
      
      pathLineNumber++;
      membershipGuiContainer.setLineNumber(pathLineNumber);
    }
    
    if (result.length() > 0) {
      grouperRequestContainer.getMembershipGuiContainer().setTracePITMembershipString(result.toString());
    }
  }
  
  private void traceMembershipsHelperTimeline(PITMember pitMember, PITField pitField, Set<String> memberIdsForTimelineAuditQuery, Set<PITGroup> pitGroupsForTimelineStates) {
    
    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    MembershipGuiContainer membershipGuiContainer = grouperRequestContainer.getMembershipGuiContainer();
    
    GrouperSession loggedInGrouperSession = GrouperSession.staticGrouperSession();

    AuditType addGroupMembershipAuditType = AuditTypeFinder.find("membership", "addGroupMembership", true);
    AuditType updateGroupMembershipAuditType = AuditTypeFinder.find("membership", "updateGroupMembership", true);
    AuditType deleteGroupMembershipAuditType = AuditTypeFinder.find("membership", "deleteGroupMembership", true);
    AuditType deleteGroupAuditType = AuditTypeFinder.find("group", "deleteGroup", true);
    
    int initialStatesCount = pitGroupsForTimelineStates.size();
    int traceAdditionalStatesCount = GrouperUiConfig.retrieveConfig().propertyValueInt("uiV2.membership.traceAdditionalStatesCount", 10);
    int maxStatesCount = initialStatesCount + traceAdditionalStatesCount;
    
    Set<PITGroup> pitGroupsForTimelineStatesWithAdditional = new LinkedHashSet<PITGroup>(pitGroupsForTimelineStates);
    Set<String> foundGroupIdsForAdditionalStates = new LinkedHashSet<String>();
    
    Map<String, GrouperProvisioningTarget> allProvisioningTargets = GrouperProvisioningSettings.getTargets(true);

    // refactor these into separate classes
    List<Timestamp> momentsOfInterest = new ArrayList<Timestamp>();
    Map<Timestamp, List<GuiAuditEntry>> eventsUserAudits = new LinkedHashMap<Timestamp, List<GuiAuditEntry>>();
    Map<Timestamp, List<PITGroup>> eventsPITAddMembershipGroup = new LinkedHashMap<Timestamp, List<PITGroup>>();
    Map<Timestamp, List<PITMembershipView>> eventsPITAddMembership = new LinkedHashMap<Timestamp, List<PITMembershipView>>();
    Map<Timestamp, List<PITGroup>> eventsPITDeleteMembershipGroup = new LinkedHashMap<Timestamp, List<PITGroup>>();
    Map<Timestamp, List<PITMembershipView>> eventsPITDeleteMembership = new LinkedHashMap<Timestamp, List<PITMembershipView>>();
    Map<Timestamp, List<GcGrouperSyncMembership>> eventsProvisioningInTargetStart = new LinkedHashMap<Timestamp, List<GcGrouperSyncMembership>>();
    Map<Timestamp, List<GcGrouperSyncMembership>> eventsProvisioningInTargetEnd = new LinkedHashMap<Timestamp, List<GcGrouperSyncMembership>>();
    Map<Timestamp, Map<PITGroup, Boolean>> states = new LinkedHashMap<Timestamp, Map<PITGroup, Boolean>>();
    Map<Timestamp, Timestamp> toDates = new LinkedHashMap<Timestamp, Timestamp>();
    
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      @Override
      public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {


        boolean isWheelOrRoot = PrivilegeHelper.isWheelOrRoot(loggedInGrouperSession.getSubject());

        TreeSet<Long> preliminaryMomentsOfInterest = new TreeSet<Long>();
        
        // moments of interest in the timeline are whenever the user was added or removed from any of these groups in the membership path
        for (PITGroup pitGroup : pitGroupsForTimelineStates) {
          Set<PITMembershipView> currentPITMemberships = GrouperDAOFactory.getFactory().getPITMembershipView().findAllByPITOwnerAndPITMemberAndPITField(pitGroup.getId(), pitMember.getId(), pitField.getId(), null, null, null);
          for (PITMembershipView currentPITMembership : currentPITMemberships) {
            if (currentPITMembership.getEndTime() != null) {
              if (currentPITMembership.getStartTime().getTime() > currentPITMembership.getEndTime().getTime()) {
                // no overlap in membership and group set so ignoring as not useful
                continue;
              }
              
              long roundedEndTimeToSecond = ((currentPITMembership.getEndTime().getTime() + 500) / 1000) * 1000;
              preliminaryMomentsOfInterest.add(roundedEndTimeToSecond);
            }
            
            {
              long roundedStartTimeToSecond = ((currentPITMembership.getStartTime().getTime() + 500) / 1000) * 1000;
              preliminaryMomentsOfInterest.add(roundedStartTimeToSecond);
            }
          }
        }
        
        if (preliminaryMomentsOfInterest.size() == 0) {
          return null;
        }
            
        int traceEventsTimeRangeInSeconds = GrouperUiConfig.retrieveConfig().propertyValueInt("uiV2.membership.traceEventsTimeRangeInSeconds", 90);
        List<Long> preliminaryMomentsOfInterestDescList = new ArrayList<Long>(preliminaryMomentsOfInterest.descendingSet());

        int count = 0;
        for (int i = 0; i < preliminaryMomentsOfInterestDescList.size(); i++) {
          if (count > 10) {
            break;
          }
          
          count++;
          
          long momentOfInterest = preliminaryMomentsOfInterestDescList.get(i);
          long fromLong = momentOfInterest - (traceEventsTimeRangeInSeconds * 1000L);
          long toLong = momentOfInterest + (traceEventsTimeRangeInSeconds * 1000L);
          
          while (true) {
            if ((i+1) >= preliminaryMomentsOfInterestDescList.size()) {
              break;
            }
            
            long nextMomentOfInterest = preliminaryMomentsOfInterestDescList.get(i + 1);
            long nextToLong = nextMomentOfInterest + (traceEventsTimeRangeInSeconds * 1000L);

            if (nextToLong >= fromLong) {
              // if there's overlap, then combine
              fromLong = nextMomentOfInterest - (traceEventsTimeRangeInSeconds * 1000L);
              i++;
            } else {
              break;
            }
          }
          
          Timestamp momentOfInterestTimestamp = new Timestamp(momentOfInterest);
          momentsOfInterest.add(momentOfInterestTimestamp);
          eventsUserAudits.put(momentOfInterestTimestamp, new ArrayList<GuiAuditEntry>());
          eventsPITAddMembership.put(momentOfInterestTimestamp, new ArrayList<PITMembershipView>());
          eventsPITAddMembershipGroup.put(momentOfInterestTimestamp, new ArrayList<PITGroup>());
          eventsPITDeleteMembership.put(momentOfInterestTimestamp, new ArrayList<PITMembershipView>());
          eventsPITDeleteMembershipGroup.put(momentOfInterestTimestamp, new ArrayList<PITGroup>());
          eventsProvisioningInTargetStart.put(momentOfInterestTimestamp, new ArrayList<GcGrouperSyncMembership>());
          eventsProvisioningInTargetEnd.put(momentOfInterestTimestamp, new ArrayList<GcGrouperSyncMembership>());
          states.put(momentOfInterestTimestamp, new LinkedHashMap<PITGroup, Boolean>());
                
          Timestamp fromDate = new Timestamp(fromLong);
          Timestamp toDate = new Timestamp(toLong);
          
          toDates.put(momentOfInterestTimestamp, toDate);
                
          if (membershipGuiContainer.isTraceMembershipTimelineShowUserAudit()) {
            UserAuditQuery userAuditQuery = new UserAuditQuery();
            userAuditQuery.setQueryOptions(new QueryOptions().sortAsc("lastUpdatedDb"));
            userAuditQuery.setFromDate(fromDate);
            userAuditQuery.setToDate(toDate);
  
            List<Criterion> extraCriterion = new ArrayList<Criterion>();
            
            for (AuditType auditType : GrouperUtil.toList(addGroupMembershipAuditType, updateGroupMembershipAuditType, deleteGroupMembershipAuditType)) {
              Criterion auditTypeCriterion = Restrictions.eq(AuditEntry.FIELD_AUDIT_TYPE_ID, auditType.getId());
              String auditEntryField = auditType.retrieveAuditEntryFieldForLabel("memberId");
              Criterion auditEntryFieldCriterion = Restrictions.in(auditEntryField, memberIdsForTimelineAuditQuery);
              Criterion andCriterion = HibUtils.listCrit(auditTypeCriterion, auditEntryFieldCriterion);
              extraCriterion.add(andCriterion);
            }
            
            // add any deleted groups that were deleted during this timeframe
            Set<String> deletedGroupIds = new LinkedHashSet<String>();
            for (PITGroup pitGroup : pitGroupsForTimelineStates) {
              if (!pitGroup.isActive()) {
                if (pitGroup.getEndTime().getTime() >= fromDate.getTime() && pitGroup.getEndTime().getTime() <= toDate.getTime()) {
                  deletedGroupIds.add(pitGroup.getSourceId());
                }
              }
            }
            
            if (deletedGroupIds.size() > 0) {
              Criterion auditTypeCriterion = Restrictions.eq(AuditEntry.FIELD_AUDIT_TYPE_ID, deleteGroupAuditType.getId());
              String auditEntryField = deleteGroupAuditType.retrieveAuditEntryFieldForLabel("id");
              Criterion auditEntryFieldCriterion = Restrictions.in(auditEntryField, deletedGroupIds);
              Criterion andCriterion = HibUtils.listCrit(auditTypeCriterion, auditEntryFieldCriterion);
              extraCriterion.add(andCriterion);
            }
            
            userAuditQuery.setExtraCriterion(HibUtils.listCritOr(extraCriterion));
                  
            List<AuditEntry> userAuditEntries = userAuditQuery.execute();
            for (AuditEntry userAudit : userAuditEntries) {
  
              GuiAuditEntry guiUserAudit = new GuiAuditEntry(userAudit);
              
              if (userAudit.getAuditTypeId().equals(addGroupMembershipAuditType.getId()) ||
                  userAudit.getAuditTypeId().equals(updateGroupMembershipAuditType.getId()) ||
                  userAudit.getAuditTypeId().equals(deleteGroupMembershipAuditType.getId())) {
                guiUserAudit.internal_setupMember();
              }
              
              guiUserAudit.internal_setupGroup();
              
              Group group = guiUserAudit.getGuiGroup().getGroup();
              
              if (!isWheelOrRoot && (group == null || !group.canHavePrivilege(loggedInGrouperSession.getSubject(), "read", false))) {
                // no access so return
                continue;
              }
        
              eventsUserAudits.get(momentOfInterestTimestamp).add(guiUserAudit);
              
              if (group != null) {
                String userAuditGroupId = group.getId();
                if (!foundGroupIdsForAdditionalStates.contains(userAuditGroupId) && pitGroupsForTimelineStatesWithAdditional.size() < maxStatesCount) {
                  foundGroupIdsForAdditionalStates.add(userAuditGroupId);
                  Set<PITGroup> pitGroups = GrouperDAOFactory.getFactory().getPITGroup().findBySourceId(userAuditGroupId, false);
                  if (pitGroups.size() > 0) {
                    pitGroupsForTimelineStatesWithAdditional.add(pitGroups.iterator().next());
                  }
                }
              }
            }
          }
          
          if (membershipGuiContainer.isTraceMembershipTimelineShowPITAudit()) {
            Set<PITMembershipView> pitMembershipsStarted = GrouperDAOFactory.getFactory().getPITMembershipView().findAllByPITMemberAndPITFieldAndStartTimeRange(pitMember.getId(), pitField.getId(), fromDate, toDate);
            for (PITMembershipView pitMembershipStarted : pitMembershipsStarted) {
              
              PITGroup currentPITGroup = GrouperDAOFactory.getFactory().getPITGroup().findById(pitMembershipStarted.getOwnerGroupId(), true);
              
              if (!isWheelOrRoot) {
                Group currentGroup = GrouperDAOFactory.getFactory().getGroup().findByUuid(currentPITGroup.getSourceId(), false);
  
                if (currentGroup == null || !currentGroup.canHavePrivilege(loggedInGrouperSession.getSubject(), "read", false)) {
                  // no access so return
                  continue;
                }
              }
              
              eventsPITAddMembership.get(momentOfInterestTimestamp).add(pitMembershipStarted);
              eventsPITAddMembershipGroup.get(momentOfInterestTimestamp).add(currentPITGroup);
              
              if (pitGroupsForTimelineStatesWithAdditional.size() < maxStatesCount) {
                pitGroupsForTimelineStatesWithAdditional.add(currentPITGroup);
              }
            }
            
            Set<PITMembershipView> pitMembershipsEnded = GrouperDAOFactory.getFactory().getPITMembershipView().findAllByPITMemberAndPITFieldAndEndTimeRange(pitMember.getId(), pitField.getId(), fromDate, toDate);
            for (PITMembershipView pitMembershipEnded : pitMembershipsEnded) {
              
              PITGroup currentPITGroup = GrouperDAOFactory.getFactory().getPITGroup().findById(pitMembershipEnded.getOwnerGroupId(), true);
              
              if (!isWheelOrRoot) {
                Group currentGroup = GrouperDAOFactory.getFactory().getGroup().findByUuid(currentPITGroup.getSourceId(), false);
  
                if (currentGroup == null || !currentGroup.canHavePrivilege(loggedInGrouperSession.getSubject(), "read", false)) {
                  // no access so return
                  continue;
                }
              }
              
              eventsPITDeleteMembership.get(momentOfInterestTimestamp).add(pitMembershipEnded);
              eventsPITDeleteMembershipGroup.get(momentOfInterestTimestamp).add(currentPITGroup);
              
              if (pitGroupsForTimelineStatesWithAdditional.size() < maxStatesCount) {
                pitGroupsForTimelineStatesWithAdditional.add(currentPITGroup);
              }
            }
          }
          
          if (membershipGuiContainer.isTraceMembershipTimelineShowProvisioningEvents()) {
            List<GcGrouperSyncMembership> gcGrouperSyncMembershipsStarted = GrouperProvisioningService.retrieveGcGrouperSyncMembershipsByMemberIdAndInTargetStartTimeRange(pitMember.getSourceId(), fromDate, toDate);

            for (GcGrouperSyncMembership gcGrouperSyncMembership : gcGrouperSyncMembershipsStarted) {
              String currentGroupId = gcGrouperSyncMembership.getGrouperSyncGroup().getGroupId();

              if (!isWheelOrRoot) {
                Group currentGroup = GrouperDAOFactory.getFactory().getGroup().findByUuid(currentGroupId, false);
                GrouperProvisioningTarget currentGrouperProvisioningTarget = allProvisioningTargets.get(gcGrouperSyncMembership.getGrouperSync().getProvisionerName());
                if (currentGroup == null || currentGrouperProvisioningTarget == null ||
                    !GrouperProvisioningService.isTargetViewable(currentGrouperProvisioningTarget, loggedInGrouperSession.getSubject(), currentGroup) ||
                    !currentGroup.canHavePrivilege(loggedInGrouperSession.getSubject(), "read", false)) {
                  // no access so return
                  continue;
                }
              }
              
              eventsProvisioningInTargetStart.get(momentOfInterestTimestamp).add(gcGrouperSyncMembership);        
              if (!foundGroupIdsForAdditionalStates.contains(currentGroupId) && pitGroupsForTimelineStatesWithAdditional.size() < maxStatesCount) {
                foundGroupIdsForAdditionalStates.add(currentGroupId);
                Set<PITGroup> pitGroups = GrouperDAOFactory.getFactory().getPITGroup().findBySourceId(currentGroupId, false);
                if (pitGroups.size() > 0) {
                  pitGroupsForTimelineStatesWithAdditional.add(pitGroups.iterator().next());
                }
              }
            }
            
            List<GcGrouperSyncMembership> gcGrouperSyncMembershipsEnded = GrouperProvisioningService.retrieveGcGrouperSyncMembershipsByMemberIdAndInTargetEndTimeRange(pitMember.getSourceId(), fromDate, toDate);

            for (GcGrouperSyncMembership gcGrouperSyncMembership : gcGrouperSyncMembershipsEnded) {
              String currentGroupId = gcGrouperSyncMembership.getGrouperSyncGroup().getGroupId();

              if (!isWheelOrRoot) {
                Group currentGroup = GrouperDAOFactory.getFactory().getGroup().findByUuid(currentGroupId, false);
                GrouperProvisioningTarget currentGrouperProvisioningTarget = allProvisioningTargets.get(gcGrouperSyncMembership.getGrouperSync().getProvisionerName());
                if (currentGroup == null || currentGrouperProvisioningTarget == null ||
                    !GrouperProvisioningService.isTargetViewable(currentGrouperProvisioningTarget, loggedInGrouperSession.getSubject(), currentGroup) ||
                    !currentGroup.canHavePrivilege(loggedInGrouperSession.getSubject(), "read", false)) {
                  // no access so return
                  continue;
                }
              }
              
              eventsProvisioningInTargetEnd.get(momentOfInterestTimestamp).add(gcGrouperSyncMembership);
              if (!foundGroupIdsForAdditionalStates.contains(currentGroupId) && pitGroupsForTimelineStatesWithAdditional.size() < maxStatesCount) {
                foundGroupIdsForAdditionalStates.add(currentGroupId);
                Set<PITGroup> pitGroups = GrouperDAOFactory.getFactory().getPITGroup().findBySourceId(currentGroupId, false);
                if (pitGroups.size() > 0) {
                  pitGroupsForTimelineStatesWithAdditional.add(pitGroups.iterator().next());
                }
              }
            }
          }
        }
        
        for (int i = 0; i < momentsOfInterest.size(); i++) {
          Timestamp momentOfInterestTimestamp = momentsOfInterest.get(i);
          Timestamp toDate = toDates.get(momentOfInterestTimestamp);

          for (PITGroup pitGroupForState : pitGroupsForTimelineStatesWithAdditional) {
            
            Set<PITMembershipView> pitMembershipsForState = GrouperDAOFactory.getFactory().getPITMembershipView().findAllByPITOwnerAndPITMemberAndPITField(pitGroupForState.getId(), pitMember.getId(), pitField.getId(), toDate, toDate, null);
            if (pitMembershipsForState.size() > 0) {
              states.get(momentOfInterestTimestamp).put(pitGroupForState, true);
            } else {
              states.get(momentOfInterestTimestamp).put(pitGroupForState, false);
            }
          }
        }
        
        return null;
      }
    });
    
    if (momentsOfInterest.size() == 0) {
      return;
    }
    
    StringBuilder result = new StringBuilder();
    result.append("<ul>\n");
    
    for (int i = 0; i < momentsOfInterest.size(); i++) {
      
      Timestamp momentOfInterestTimestamp = momentsOfInterest.get(i);
      List<GuiAuditEntry> currEventsUserAudits = eventsUserAudits.get(momentOfInterestTimestamp);
      List<PITMembershipView> currEventsPITAddMembership = eventsPITAddMembership.get(momentOfInterestTimestamp);
      List<PITGroup> currEventsPITAddMembershipGroup = eventsPITAddMembershipGroup.get(momentOfInterestTimestamp);
      List<PITMembershipView> currEventsPITDeleteMembership = eventsPITDeleteMembership.get(momentOfInterestTimestamp);
      List<PITGroup> currEventsPITDeleteMembershipGroup = eventsPITDeleteMembershipGroup.get(momentOfInterestTimestamp);
      List<GcGrouperSyncMembership> currEventsProvisioningInTargetStart = eventsProvisioningInTargetStart.get(momentOfInterestTimestamp);
      List<GcGrouperSyncMembership> currEventsProvisioningInTargetEnd = eventsProvisioningInTargetEnd.get(momentOfInterestTimestamp);
      Map<PITGroup, Boolean> currStates = states.get(momentOfInterestTimestamp);
      
      membershipGuiContainer.setGuiAuditDateCurrent(momentOfInterestTimestamp);
      result.append(TextContainer.retrieveFromRequest().getText().get("membershipTraceTimelineMomentOfInterest")).append("\n");
      
      result.append("<ul><li>" + TextContainer.retrieveFromRequest().getText().get("membershipTraceTimelineMomentOfInterestEventsLabel") + "</li>");
      result.append("<ul>");
      
      Map<Long, TreeSet<String>> sortedEvents = new TreeMap<Long, TreeSet<String>>();
      
      for (GuiAuditEntry guiUserAudit : currEventsUserAudits) {
        boolean isAddMembership = false;
        boolean isUpdateMembership = false;
        boolean isDeleteMembership = false;
        boolean isDeleteGroup = false;
        if (guiUserAudit.getAuditEntry().getAuditType().equals(addGroupMembershipAuditType)) {
          isAddMembership = true;
        } else if (guiUserAudit.getAuditEntry().getAuditType().equals(deleteGroupMembershipAuditType)) {
          isDeleteMembership = true;
        } else if (guiUserAudit.getAuditEntry().getAuditType().equals(updateGroupMembershipAuditType)) {
          isUpdateMembership = true;
        } else if (guiUserAudit.getAuditEntry().getAuditType().equals(deleteGroupAuditType)) {
          isDeleteGroup = true;
        } else {
          continue;
        }
        
        membershipGuiContainer.setGuiAuditEntryCurrent(guiUserAudit);
        long timestamp = guiUserAudit.getAuditEntry().getCreatedOn().getTime();
        
        if (sortedEvents.get(timestamp) == null) {
          sortedEvents.put(timestamp, new TreeSet<String>());
        }
        
        if (isAddMembership) {
          sortedEvents.get(timestamp).add(TextContainer.retrieveFromRequest().getText().get("membershipTraceTimelineUserAuditAddMembership"));
        } else if (isUpdateMembership) {
          sortedEvents.get(timestamp).add(TextContainer.retrieveFromRequest().getText().get("membershipTraceTimelineUserAuditUpdateMembership"));
        } else if (isDeleteMembership) {
          sortedEvents.get(timestamp).add(TextContainer.retrieveFromRequest().getText().get("membershipTraceTimelineUserAuditDeleteMembership"));
        } else if (isDeleteGroup) {
          sortedEvents.get(timestamp).add(TextContainer.retrieveFromRequest().getText().get("membershipTraceTimelineUserAuditDeleteGroup"));
        }
      }
      
      for (int j = 0; j < currEventsPITAddMembership.size(); j++) {
        PITMembershipView pitMembershipStarted = currEventsPITAddMembership.get(j);
        PITGroup currentPITGroup = currEventsPITAddMembershipGroup.get(j);
        
        membershipGuiContainer.setGuiPITGroupCurrent(new GuiPITGroup(currentPITGroup));
        membershipGuiContainer.setGuiAuditDateCurrent(pitMembershipStarted.getStartTime());
        
        long timestamp = pitMembershipStarted.getStartTime().getTime();
        
        if (sortedEvents.get(timestamp) == null) {
          sortedEvents.put(timestamp, new TreeSet<String>());
        }
        
        sortedEvents.get(timestamp).add(TextContainer.retrieveFromRequest().getText().get("membershipTraceTimelinePITAuditAddMembership"));
      }
      
      for (int j = 0; j < currEventsPITDeleteMembership.size(); j++) {
        PITMembershipView pitMembershipEnded = currEventsPITDeleteMembership.get(j);
        PITGroup currentPITGroup = currEventsPITDeleteMembershipGroup.get(j);
        
        membershipGuiContainer.setGuiPITGroupCurrent(new GuiPITGroup(currentPITGroup));
        membershipGuiContainer.setGuiAuditDateCurrent(pitMembershipEnded.getEndTime());
        
        long timestamp = pitMembershipEnded.getEndTime().getTime();
        
        if (sortedEvents.get(timestamp) == null) {
          sortedEvents.put(timestamp, new TreeSet<String>());
        }
        
        sortedEvents.get(timestamp).add(TextContainer.retrieveFromRequest().getText().get("membershipTraceTimelinePITAuditDeleteMembership"));
      }
      
      for (GcGrouperSyncMembership gcGrouperSyncMembership : currEventsProvisioningInTargetStart) {
        membershipGuiContainer.setGuiGcGrouperSyncMembershipCurrent(new GuiGcGrouperSyncMembership(gcGrouperSyncMembership));
        
        long timestamp = gcGrouperSyncMembership.getInTargetStart().getTime();
        
        if (sortedEvents.get(timestamp) == null) {
          sortedEvents.put(timestamp, new TreeSet<String>());
        }
        
        sortedEvents.get(timestamp).add(TextContainer.retrieveFromRequest().getText().get("membershipTraceTimelineProvisioningTargetStart"));
      }
      
      for (GcGrouperSyncMembership gcGrouperSyncMembership : currEventsProvisioningInTargetEnd) {
        membershipGuiContainer.setGuiGcGrouperSyncMembershipCurrent(new GuiGcGrouperSyncMembership(gcGrouperSyncMembership));
        
        long timestamp = gcGrouperSyncMembership.getInTargetEnd().getTime();
        
        if (sortedEvents.get(timestamp) == null) {
          sortedEvents.put(timestamp, new TreeSet<String>());
        }
        
        sortedEvents.get(timestamp).add(TextContainer.retrieveFromRequest().getText().get("membershipTraceTimelineProvisioningTargetEnd"));
      }
      
      for (long timestamp : sortedEvents.keySet()) {
        for (String event : sortedEvents.get(timestamp)) {
          result.append(event + "\n");
        }
      }
      
      result.append("</ul>");
      result.append("<li>" + TextContainer.retrieveFromRequest().getText().get("membershipTraceTimelineMomentOfInterestStateLabel") + "</li>");
      result.append("<ul>");
      
      for (PITGroup currState : currStates.keySet()) {
        membershipGuiContainer.setGuiPITGroupCurrent(new GuiPITGroup(currState));
        
        if (currStates.get(currState)) {
          result.append(TextContainer.retrieveFromRequest().getText().get("membershipTraceTimelineStateMembershipYes")).append("\n");
        } else {
          result.append(TextContainer.retrieveFromRequest().getText().get("membershipTraceTimelineStateMembershipNo")).append("\n");
        }
      }
      
      result.append("</ul>");
      
      result.append("</ul>");
    }
    
    result.append("</ul>\n");

    grouperRequestContainer.getMembershipGuiContainer().setTraceMembershipTimelineString(result.toString());
  }
  
  private void traceMembershipHelperCurrent(MembershipPathGroup membershipPathGroup, Subject subject, Set<String> memberIdsForTimelineAuditQuery, Set<PITGroup> pitGroupsForTimelineStates) {
    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    MembershipGuiContainer membershipGuiContainer = grouperRequestContainer.getMembershipGuiContainer();

    Set<MembershipPath> membershipPaths = GrouperUtil.nonNull(membershipPathGroup.getMembershipPaths());
    
    StringBuilder result = new StringBuilder();
    
    //massage the paths to only consider the ones that are allowed
    int membershipUnallowedCount = 0;
    List<MembershipPath> membershipPathsAllowed = new ArrayList<MembershipPath>();
    
    for (MembershipPath membershipPath : membershipPaths) {
      if (membershipPath.isPathAllowed()) {
        membershipPathsAllowed.add(membershipPath);
      } else {
        membershipUnallowedCount++;
      }
    }

    if (GrouperUtil.length(membershipPathsAllowed) == 0) {

      if (membershipUnallowedCount > 0) {
        result.append(TextContainer.retrieveFromRequest().getText().get("membershipTraceGroupNoPathsAllowed")).append("<br /><br />\n");
        
      } else {
        result.append(TextContainer.retrieveFromRequest().getText().get("membershipTraceGroupNoPaths")).append("<br /><br />\n");
      }
    } else if (membershipUnallowedCount > 0) {
      membershipGuiContainer.setPathCountNotAllowed(membershipUnallowedCount);
      result.append(TextContainer.retrieveFromRequest().getText().get("membershipTraceGroupPathsNotAllowed")).append("<br /><br />\n");
    }
    
    Set<String> groupIdsForTimeline = new LinkedHashSet<String>();
    
    //<p>Danielle Knotts is an <a href="#"><span class="label label-inverse">indirect member</span></a> of</p>
    //<p style="margin-left:20px;"><i class="icon-circle-arrow-right"></i> <a href="#">Root : Departments : Information Technology : Staff</a></p>
    //<p style="margin-left:40px;"><i class="icon-circle-arrow-right"></i> which is a <a href="#"><span class="label label-info">direct member</span></a> of</p>
    //<p style="margin-left:60px"><i class="icon-circle-arrow-right"></i> Root : Applications : Wiki : Editors</p><a href="#" class="pull-right btn btn-primary btn-cancel">Back to previous page</a>
    //<hr />
    boolean firstPath = true;
    // loop through each membership path
    for (MembershipPath membershipPath : membershipPathsAllowed) {
      
      if (!firstPath) {
        result.append("<hr />\n");
      }
      
      int pathLineNumber = 0;
      membershipGuiContainer.setLineNumber(pathLineNumber);
      result.append(TextContainer.retrieveFromRequest().getText().get("membershipTracePathFirstLine")).append("\n");
      pathLineNumber++;
      membershipGuiContainer.setLineNumber(pathLineNumber);
      
      boolean firstNode = true;
      
      Subject currentSubject = subject;

      //loop through each node in the path
      for (MembershipPathNode membershipPathNode : membershipPath.getMembershipPathNodes()) {
        
        Group ownerGroup = membershipPathNode.getOwnerGroup();
        groupIdsForTimeline.add(ownerGroup.getId());

        if (!firstNode) {

          if (membershipPathNode.isComposite()) {
            
            //dont know what branch of the composite we are on... so 
            Group factor = membershipPathNode.getOtherFactor();
            membershipGuiContainer.setGuiGroupFactor(new GuiGroup(factor));
            
            if (factor != null) {
              if (factor.canHavePrivilege(GrouperSession.staticGrouperSession().getSubject(), "read", false)) {
                groupIdsForTimeline.add(factor.getId());
              }
            }
            
            switch(membershipPathNode.getCompositeType()) {
              case UNION:
                
                result.append(TextContainer.retrieveFromRequest().getText().get("membershipTraceGroupCompositeOfUnion")).append("\n");
                break;
              case INTERSECTION:

                result.append(TextContainer.retrieveFromRequest().getText().get("membershipTraceGroupCompositeOfIntersection")).append("\n");
                break;
              case COMPLEMENT:
                
                result.append(TextContainer.retrieveFromRequest().getText().get("membershipTraceGroupCompositeOfMinus")).append("\n");
                break;
              default:
                throw new RuntimeException("Not expecting composite type: " + membershipPathNode.getCompositeType());  
            }
            
          } else {
            result.append(TextContainer.retrieveFromRequest().getText().get("membershipTraceGroupMemberOf")).append("\n");
            
          }

          pathLineNumber++;
          membershipGuiContainer.setLineNumber(pathLineNumber);

        }
        
        membershipGuiContainer.setGuiGroupCurrent(new GuiGroup(ownerGroup));
        
        Membership membership = MembershipFinder.findImmediateMembership(GrouperSession.staticGrouperSession(), ownerGroup, currentSubject, false);
        membershipGuiContainer.setGuiMembershipCurrent(new GuiMembership(membership));
        
        result.append(TextContainer.retrieveFromRequest().getText().get("membershipTraceGroupLine")).append("\n");
        
        firstNode = false;
        pathLineNumber++;
        membershipGuiContainer.setLineNumber(pathLineNumber);
        
        currentSubject = ownerGroup.toSubject();
      }
      
      firstPath = false;
    }
    
    if (result.length() > 0) {
      grouperRequestContainer.getMembershipGuiContainer().setTraceMembershipsString(result.toString());
    }
    
    pitGroupsForTimelineStates.addAll(GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdsActive(groupIdsForTimeline));
    
    for (Member member : GrouperDAOFactory.getFactory().getMember().findBySubjectIds(groupIdsForTimeline, "g:gsa")) {
      memberIdsForTimelineAuditQuery.add(member.getId());
    }
  }

  /**
   * trace group privileges
   * @param request
   * @param response
   */
  public void traceGroupPrivileges(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Group group = null;
    Subject subject = null;
  
    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.ADMIN).getGroup();
  
      if (group == null) {
        return;
      }
  
      subject = UiV2Subject.retrieveSubjectHelper(request, true);
  
      if (subject == null) {
        return;
      }
      
      Member member = MemberFinder.findBySubject(grouperSession, subject, false);
  
      if (member == null) {
        
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("privilegesTraceNoPrivilegesFound")));
        
        return;
      }
            
      MembershipGuiContainer membershipGuiContainer = grouperRequestContainer.getMembershipGuiContainer();
      
      //see where to go back to
      if (StringUtils.equalsIgnoreCase(request.getParameter("backTo"), "subject")) {
        membershipGuiContainer.setTraceMembershipFromSubject(true);
      }
  
      //this is a subobject
      grouperRequestContainer.getGroupContainer().getGuiGroup().setShowBreadcrumbLink(true);
      grouperRequestContainer.getSubjectContainer().getGuiSubject().setShowBreadcrumbLink(true);
  
      List<MembershipPath> allMembershipPaths = new ArrayList<MembershipPath>();
      {
        MembershipPathGroup membershipPathGroup = MembershipPathGroup.analyzePrivileges(group, member);
        allMembershipPaths.addAll(GrouperUtil.nonNull(membershipPathGroup.getMembershipPaths()));
      }
      
      //lets try with every entity too
      Subject everyEntitySubject = SubjectFinder.findAllSubject();
      {
        MembershipPathGroup membershipPathGroup = MembershipPathGroup.analyzePrivileges(group, everyEntitySubject);
        allMembershipPaths.addAll(GrouperUtil.nonNull(membershipPathGroup.getMembershipPaths()));
      }
            
      //massage the paths to only consider the ones that are allowed
      int membershipUnallowedCount = 0;
      List<MembershipPath> membershipPathsAllowed = new ArrayList<MembershipPath>();

      for (MembershipPath membershipPath : allMembershipPaths) {
        if (membershipPath.isPathAllowed()) {
          membershipPathsAllowed.add(membershipPath);
        } else {
          membershipUnallowedCount++;
        }
      }
  
      if (membershipUnallowedCount > 0) {
        membershipGuiContainer.setPathCountNotAllowed(membershipUnallowedCount);
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.info,
            TextContainer.retrieveFromRequest().getText().get("privilegesTraceGroupPathsNotAllowed")));
      }
  
      if (GrouperUtil.length(membershipPathsAllowed) == 0) {
  
        if (membershipUnallowedCount > 0) {
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
              TextContainer.retrieveFromRequest().getText().get("privilegesTraceGroupNoPathsAllowed")));
        } else {
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
              TextContainer.retrieveFromRequest().getText().get("privilegesTraceGroupNoPaths")));
        }
      }
      
      tracePrivilegesHelper(membershipPathsAllowed, true, false, false);
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/membership/tracePrivileges.jsp"));
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }

  /**
   * trace the 
   * @param membershipPathsAllowed
   * @param isGroup
   * @param isStem
   * @param isAttributeDef
   */
  public void tracePrivilegesHelper(List<MembershipPath> membershipPathsAllowed, boolean isGroup, boolean isStem, boolean isAttributeDef) {
    
    StringBuilder result = new StringBuilder();
    Subject everyEntitySubject = SubjectFinder.findAllSubject();

    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    
    MembershipGuiContainer membershipGuiContainer = grouperRequestContainer.getMembershipGuiContainer();
    
    //<p>Danielle Knotts is an <a href="#"><span class="label label-inverse">indirect member</span></a> of</p>
    //<p style="margin-left:20px;"><i class="icon-circle-arrow-right"></i> <a href="#">Root : Departments : Information Technology : Staff</a></p>
    //<p style="margin-left:40px;"><i class="icon-circle-arrow-right"></i> which is a <a href="#"><span class="label label-info">direct member</span></a> of</p>
    //<p style="margin-left:60px"><i class="icon-circle-arrow-right"></i> Root : Applications : Wiki : Editors</p><a href="#" class="pull-right btn btn-primary btn-cancel">Back to previous page</a>
    //<hr />
    boolean firstPath = true;
    // loop through each membership path
    for (MembershipPath membershipPath : membershipPathsAllowed) {
      
      if (!firstPath) {
        result.append("<hr />\n");
      }
      
      int pathLineNumber = 0;
      membershipGuiContainer.setLineNumber(pathLineNumber);
      
      //get privs like ADMIN, READ
      {
        StringBuilder privilegeNames = new StringBuilder();
        boolean first = true;
        for (Field field : GrouperUtil.nonNull(membershipPath.getFieldsIncludingImplied())) {
          if (!first) {
            privilegeNames.append(", ");
          }
          
          String textKey = "priv." + field.getName() + "Upper";

          String privilegeLabel = TextContainer.retrieveFromRequest().getText().get(textKey);
          
          privilegeNames.append(privilegeLabel);
          
          first = false;
        }
        
        membershipGuiContainer.setPrivilegeIncludingImpliedLabelsString(privilegeNames.toString());
      }

      //get privs like ADMIN, READ
      {
        StringBuilder privilegeNames = new StringBuilder();
        boolean first = true;
        for (Field field : GrouperUtil.nonNull(membershipPath.getFields())) {
          if (!first) {
            privilegeNames.append(", ");
          }
          
          String textKey = "priv." + field.getName() + "Upper";

          String privilegeLabel = TextContainer.retrieveFromRequest().getText().get(textKey);
          
          privilegeNames.append(privilegeLabel);
          
          first = false;
        }
        
        membershipGuiContainer.setPrivilegeLabelsString(privilegeNames.toString());
      }

      
      result.append(TextContainer.retrieveFromRequest().getText().get("privilegesTracePrivilegesLine")).append("\n");
      if (membershipPath.getMembershipType() == MembershipType.IMMEDIATE) {
        if (SubjectHelper.eq(everyEntitySubject, membershipPath.getMember().getSubject())) {
          result.append(TextContainer.retrieveFromRequest().getText().get("privilegesTracePathEveryEntityFirstLine")).append("\n");
        } else {
          result.append(TextContainer.retrieveFromRequest().getText().get("privilegesTracePathFirstLine")).append("\n");
        }
      } else {
        if (SubjectHelper.eq(everyEntitySubject, membershipPath.getMember().getSubject())) {
          result.append(TextContainer.retrieveFromRequest().getText().get("privilegesTraceMembershipPathEveryEntityFirstLine")).append("\n");
        } else {
          result.append(TextContainer.retrieveFromRequest().getText().get("membershipTracePathFirstLine")).append("\n");
        }
      }
      pathLineNumber++;
      membershipGuiContainer.setLineNumber(pathLineNumber);
      
      boolean firstNode = true;
      
      //loop through each node in the path
      for (int i = 0; i < GrouperUtil.length(membershipPath.getMembershipPathNodes()); i++) {
        
        MembershipPathNode membershipPathNode = membershipPath.getMembershipPathNodes().get(i);
        
        Group ownerGroup = membershipPathNode.getOwnerGroup();
        Stem ownerStem = membershipPathNode.getOwnerStem();
        AttributeDef ownerAttributeDef = membershipPathNode.getOwnerAttributeDef();
 
        if (!firstNode) {
 
          if (membershipPathNode.isComposite()) {
            
            //dont know what branch of the composite we are on... so 
            Group factor = membershipPathNode.getOtherFactor();
            membershipGuiContainer.setGuiGroupFactor(new GuiGroup(factor));
            switch(membershipPathNode.getCompositeType()) {
              case UNION:
                
                result.append(TextContainer.retrieveFromRequest().getText().get("membershipTraceGroupCompositeOfUnion")).append("\n");
                break;
              case INTERSECTION:
 
                result.append(TextContainer.retrieveFromRequest().getText().get("membershipTraceGroupCompositeOfIntersection")).append("\n");
                break;
              case COMPLEMENT:
                
                result.append(TextContainer.retrieveFromRequest().getText().get("membershipTraceGroupCompositeOfMinus")).append("\n");
                break;
              default:
                throw new RuntimeException("Not expecting composite type: " + membershipPathNode.getCompositeType());  
            }
            
          } else {
            
            //if last line
            if (i == GrouperUtil.length(membershipPath.getMembershipPathNodes()) - 1) {
              result.append(TextContainer.retrieveFromRequest().getText().get("privilegesTracePathLastLine")).append("\n");
              
            } else {
              result.append(TextContainer.retrieveFromRequest().getText().get("membershipTraceGroupMemberOf")).append("\n");
            }
            
            
          }
 
          pathLineNumber++;
          membershipGuiContainer.setLineNumber(pathLineNumber);
 
        }
        
        if (ownerGroup != null) {
          membershipGuiContainer.setGuiGroupCurrent(new GuiGroup(ownerGroup));
          result.append(TextContainer.retrieveFromRequest().getText().get("membershipTraceGroupLine")).append("\n");
        } else if (ownerStem != null) {
          membershipGuiContainer.setGuiStemCurrent(new GuiStem(ownerStem));
          result.append(TextContainer.retrieveFromRequest().getText().get("membershipTraceStemLine")).append("\n");
        } else if (ownerAttributeDef != null) {
          membershipGuiContainer.setGuiAttributeDefCurrent(new GuiAttributeDef(ownerAttributeDef));
          result.append(TextContainer.retrieveFromRequest().getText().get("membershipTraceAttributeDefLine")).append("\n");
        }
 
 
        firstNode = false;
        pathLineNumber++;
        membershipGuiContainer.setLineNumber(pathLineNumber);
      }
      
      firstPath = false;
    }
    
    grouperRequestContainer.getMembershipGuiContainer().setTraceMembershipsString(result.toString());
  }

  /**
   * trace stem privileges
   * @param request
   * @param response
   */
  public void traceStemPrivileges(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Stem stem = null;
    Subject subject = null;
  
    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      stem = UiV2Stem.retrieveStemHelper(request, true).getStem();
  
      if (stem == null) {
        return;
      }
  
      subject = UiV2Subject.retrieveSubjectHelper(request, true);
  
      if (subject == null) {
        return;
      }
      
      Member member = MemberFinder.findBySubject(grouperSession, subject, false);
  
      if (member == null) {
        
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("privilegesTraceNoPrivilegesFound")));
        
        return;
      }
            
      MembershipGuiContainer membershipGuiContainer = grouperRequestContainer.getMembershipGuiContainer();
      
      //see where to go back to
      if (StringUtils.equalsIgnoreCase(request.getParameter("backTo"), "subject")) {
        membershipGuiContainer.setTraceMembershipFromSubject(true);
      }
  
      //this is a subobject
      grouperRequestContainer.getStemContainer().getGuiStem().setShowBreadcrumbLink(true);
      grouperRequestContainer.getSubjectContainer().getGuiSubject().setShowBreadcrumbLink(true);
  
      List<MembershipPath> allMembershipPaths = new ArrayList<MembershipPath>();
      {
        MembershipPathGroup membershipPathGroup = MembershipPathGroup.analyzePrivileges(stem, member);
        allMembershipPaths.addAll(GrouperUtil.nonNull(membershipPathGroup.getMembershipPaths()));
      }
      
      //lets try with every entity too
      Subject everyEntitySubject = SubjectFinder.findAllSubject();
      {
        MembershipPathGroup membershipPathGroup = MembershipPathGroup.analyzePrivileges(stem, everyEntitySubject);
        allMembershipPaths.addAll(GrouperUtil.nonNull(membershipPathGroup.getMembershipPaths()));
      }
            
      //massage the paths to only consider the ones that are allowed
      int membershipUnallowedCount = 0;
      List<MembershipPath> membershipPathsAllowed = new ArrayList<MembershipPath>();
  
      for (MembershipPath membershipPath : allMembershipPaths) {
        if (membershipPath.isPathAllowed()) {
          membershipPathsAllowed.add(membershipPath);
        } else {
          membershipUnallowedCount++;
        }
      }

      if (membershipUnallowedCount > 0) {
        membershipGuiContainer.setPathCountNotAllowed(membershipUnallowedCount);
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.info,
            TextContainer.retrieveFromRequest().getText().get("privilegesTraceGroupPathsNotAllowed")));
      }
  
      if (GrouperUtil.length(membershipPathsAllowed) == 0) {
  
        if (membershipUnallowedCount > 0) {
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
              TextContainer.retrieveFromRequest().getText().get("privilegesTraceGroupNoPathsAllowed")));
        } else {
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
              TextContainer.retrieveFromRequest().getText().get("privilegesTraceStemNoPaths")));
        }
      }
      
      tracePrivilegesHelper(membershipPathsAllowed, false, true, false);
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/membership/traceStemPrivileges.jsp"));
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }

  /**
   * trace attribute definition privileges
   * @param request
   * @param response
   */
  public void traceAttributeDefPrivileges(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    AttributeDef attributeDef = null;
    Subject subject = null;
  
    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      attributeDef = UiV2AttributeDef.retrieveAttributeDefHelper(request, 
          AttributeDefPrivilege.ATTR_ADMIN, true).getAttributeDef();
  
      if (attributeDef == null) {
        return;
      }
  
      subject = UiV2Subject.retrieveSubjectHelper(request, true);
  
      if (subject == null) {
        return;
      }
      
      Member member = MemberFinder.findBySubject(grouperSession, subject, false);
  
      if (member == null) {
        
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("privilegesTraceNoPrivilegesFound")));
        
        return;
      }
            
      MembershipGuiContainer membershipGuiContainer = grouperRequestContainer.getMembershipGuiContainer();
      
      //see where to go back to
      if (StringUtils.equalsIgnoreCase(request.getParameter("backTo"), "subject")) {
        membershipGuiContainer.setTraceMembershipFromSubject(true);
      }
  
      //this is a subobject
      grouperRequestContainer.getAttributeDefContainer().getGuiAttributeDef().setShowBreadcrumbLink(true);
      grouperRequestContainer.getSubjectContainer().getGuiSubject().setShowBreadcrumbLink(true);
  
      List<MembershipPath> allMembershipPaths = new ArrayList<MembershipPath>();
      {
        MembershipPathGroup membershipPathGroup = MembershipPathGroup.analyzePrivileges(attributeDef, member);
        allMembershipPaths.addAll(GrouperUtil.nonNull(membershipPathGroup.getMembershipPaths()));
      }
      
      //lets try with every entity too
      Subject everyEntitySubject = SubjectFinder.findAllSubject();
      {
        MembershipPathGroup membershipPathGroup = MembershipPathGroup.analyzePrivileges(attributeDef, everyEntitySubject);
        allMembershipPaths.addAll(GrouperUtil.nonNull(membershipPathGroup.getMembershipPaths()));
      }
            
      //massage the paths to only consider the ones that are allowed
      int membershipUnallowedCount = 0;
      List<MembershipPath> membershipPathsAllowed = new ArrayList<MembershipPath>();
  
      for (MembershipPath membershipPath : allMembershipPaths) {
        if (membershipPath.isPathAllowed()) {
          membershipPathsAllowed.add(membershipPath);
        } else {
          membershipUnallowedCount++;
        }
      }
  
      if (membershipUnallowedCount > 0) {
        membershipGuiContainer.setPathCountNotAllowed(membershipUnallowedCount);
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.info,
            TextContainer.retrieveFromRequest().getText().get("privilegesTraceGroupPathsNotAllowed")));
      }
  
      if (GrouperUtil.length(membershipPathsAllowed) == 0) {
  
        if (membershipUnallowedCount > 0) {
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
              TextContainer.retrieveFromRequest().getText().get("privilegesTraceGroupNoPathsAllowed")));
        } else {
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
              TextContainer.retrieveFromRequest().getText().get("privilegesTraceAttributeDefNoPaths")));
        }
      }
      
      tracePrivilegesHelper(membershipPathsAllowed, false, false, true);
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/membership/traceAttributeDefPrivileges.jsp"));
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }

  /**
   * save a membership
   * @param request
   * @param response
   */
  public void saveMembership(HttpServletRequest request, HttpServletResponse response) {

    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Group group = null;
    Subject subject = null;
    Field field = null;
  
    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.READ).getGroup();
  
      if (group == null) {
        return;
      }
  
      subject = UiV2Subject.retrieveSubjectHelper(request, true);
  
      if (subject == null) {
        return;
      }
      
      Member member = MemberFinder.findBySubject(grouperSession, subject, false);
  
      if (member == null) {
        
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("membershipEditNoMembershipFound")));
        
        return;
      }
      
      field = UiV2Membership.retrieveFieldHelper(request, true);
      
      if (field == null) {
        return;
      }      
      
      MembershipGuiContainer membershipGuiContainer = grouperRequestContainer.getMembershipGuiContainer();

      membershipGuiContainer.setField(field);
      
      
      String hasMembershipString = request.getParameter("hasMembership[]");
      boolean hasMembership = GrouperUtil.booleanValue(hasMembershipString, false);


      Date startDate = null;
      try {
        String startDateString = request.getParameter("startDate");
        startDate = GrouperUtil.stringToTimestampTimeRequiredWithoutSeconds(startDateString);
      } catch (Exception e) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
            "#member-start-date",
            TextContainer.retrieveFromRequest().getText().get("membershipEditFromDateInvalid")));
        return;
      }

      Date endDate = null;
      try {
        String endDateString = request.getParameter("endDate");
        endDate = GrouperUtil.stringToTimestampTimeRequiredWithoutSeconds(endDateString);
      } catch (Exception e) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
            "#member-end-date",
            TextContainer.retrieveFromRequest().getText().get("membershipEditToDateInvalid")));
        return;
      }
      
      if (startDate != null && endDate != null && !endDate.after(startDate)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
            "#member-end-date",
            TextContainer.retrieveFromRequest().getText().get("membershipEditToDateAfterFromDateError")));
        return;
      }
      
      if (grouperRequestContainer.getGroupContainer().isCanAdmin()) { 
        boolean privOptins = GrouperUtil.booleanValue(request.getParameter("privilege_optins[]"), false);
        boolean privOptouts = GrouperUtil.booleanValue(request.getParameter("privilege_optouts[]"), false);
        boolean privViewers = GrouperUtil.booleanValue(request.getParameter("privilege_viewers[]"), false);
        boolean privReaders = GrouperUtil.booleanValue(request.getParameter("privilege_readers[]"), false);
        boolean privAdmins = GrouperUtil.booleanValue(request.getParameter("privilege_admins[]"), false);
        boolean privUpdaters = GrouperUtil.booleanValue(request.getParameter("privilege_updaters[]"), false);
        boolean privGroupAttrReaders = GrouperUtil.booleanValue(request.getParameter("privilege_groupAttrReaders[]"), false);
        boolean privGroupAttrUpdaters = GrouperUtil.booleanValue(request.getParameter("privilege_groupAttrUpdaters[]"), false);      
    
        if (!group.addOrEditMember(subject, false, hasMembership, privAdmins, privUpdaters, privReaders, privViewers, 
            privOptins, privOptouts, privGroupAttrReaders, privGroupAttrUpdaters, startDate, endDate, true)) {
    
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.info, 
              TextContainer.retrieveFromRequest().getText().get("membershipEditNoChange")));
          return;
        }
      } else {
        if (!group.addOrEditMember(subject, false, hasMembership, startDate, endDate, true)) {
    
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.info, 
              TextContainer.retrieveFromRequest().getText().get("membershipEditNoChange")));
          return;
        }
      }

      String backTo = request.getParameter("backTo");
      if (StringUtils.equals(backTo, "subject")) {
        guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2Subject.viewSubject&memberId=" + member.getId() + "');"));
              
      } else if (StringUtils.equals(backTo, "group")) {
        guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2Group.viewGroup&groupId=" + group.getId() + "');"));

      } else {
        throw new RuntimeException("not expecting backTo: " + backTo);
      }

      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("membershipEditSaveSuccess")));


    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

  }
  
  /**
   * edit membership
   * @param request
   * @param response
   */
  public void editMembership(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Group group = null;
    Subject subject = null;
    Field field = null;
  
    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.READ).getGroup();
  
      if (group == null) {
        return;
      }
  
      subject = UiV2Subject.retrieveSubjectHelper(request, true);
  
      if (subject == null) {
        return;
      }
      
      Member member = MemberFinder.findBySubject(grouperSession, subject, false);
  
      if (member == null) {
        
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("membershipEditNoMembershipFound")));
        
        return;
      }
      
      field = UiV2Membership.retrieveFieldHelper(request, true);
      
      if (field == null) {
        return;
      }      
      
      MembershipGuiContainer membershipGuiContainer = grouperRequestContainer.getMembershipGuiContainer();

      membershipGuiContainer.setField(field);
      
      //see where to go back to
      if (StringUtils.equalsIgnoreCase(request.getParameter("backTo"), "subject")) {
        membershipGuiContainer.setEditMembershipFromSubject(true);
      }
  
      //this is a subobject
      grouperRequestContainer.getGroupContainer().getGuiGroup().setShowBreadcrumbLink(true);
      grouperRequestContainer.getSubjectContainer().getGuiSubject().setShowBreadcrumbLink(true);

      // get the privileges for this subject
      MembershipResult membershipResult = new MembershipFinder().assignFieldType(FieldType.ACCESS)
          .addMemberId(member.getId()).addGroup(group).findMembershipResult();
      
      GuiMembershipSubjectContainer guiMembershipSubjectContainer = GuiMembershipSubjectContainer.convertOneFromFinder(membershipResult);
      
      if (guiMembershipSubjectContainer != null) {
        MembershipSubjectContainer privilegeMembershipSubjectContainer = guiMembershipSubjectContainer.getMembershipSubjectContainer();
        privilegeMembershipSubjectContainer.considerAccessPrivilegeInheritance();
        //reset the gui
        guiMembershipSubjectContainer = new GuiMembershipSubjectContainer(privilegeMembershipSubjectContainer);
        
        membershipGuiContainer.setPrivilegeGuiMembershipSubjectContainer(guiMembershipSubjectContainer);

      }
      
      membershipResult = new MembershipFinder().addField(field).addMemberId(member.getId()).addGroup(group).findMembershipResult();
      guiMembershipSubjectContainer = GuiMembershipSubjectContainer.convertOneFromFinder(membershipResult);
      
      if (guiMembershipSubjectContainer != null) {

        membershipGuiContainer.setGuiMembershipSubjectContainer(guiMembershipSubjectContainer);
        
        MembershipContainer membershipContainer = guiMembershipSubjectContainer.getMembershipSubjectContainer()
            .getMembershipContainers().get(Group.getDefaultList().getName());
        
        if (membershipContainer != null) {

          Membership immediateMembership = membershipContainer.getImmediateMembership();
          if (immediateMembership != null) {  
            membershipGuiContainer.setDirectGuiMembership(new GuiMembership(immediateMembership));
          }
          membershipGuiContainer.setDirectMembership(membershipContainer.getMembershipAssignType().isImmediate());
          membershipGuiContainer.setIndirectMembership(membershipContainer.getMembershipAssignType().isNonImmediate());
        }

      }

      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/membership/editMembership.jsp"));

      GrouperUserDataApi.recentlyUsedGroupAdd(GrouperUiUserData.grouperUiGroupNameForUserData(), 
          loggedInSubject, group);

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

  }
}
