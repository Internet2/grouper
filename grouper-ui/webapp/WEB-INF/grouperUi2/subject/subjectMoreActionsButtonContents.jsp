<%@ include file="../assetsJsp/commonTaglib.jsp"%>

 <!-- start subject/subjectMoreActionsButtonContents.jsp -->
 <%-- on the privs tab, show the add member button --%>            
 <c:choose>
   <c:when test="${grouperRequestContainer.stemContainer.showAddMember}">
     <a id="show-add-block" href="#" onclick="$('#add-block-stem-container').toggle('slow'); return false;" class="btn btn-medium btn-primary btn-block"
       ><i class="fa fa-plus"></i> ${textContainer.text['subjectViewMoreActionsAddMembersToStem'] }</a>
   </c:when>
   <c:when test="${grouperRequestContainer.attributeDefContainer.showAddMember}">
     <a id="show-add-block" href="#" onclick="$('#add-block-attributeDef-container').toggle('slow'); return false;" class="btn btn-medium btn-primary btn-block"
       ><i class="fa fa-plus"></i> ${textContainer.text['subjectViewMoreActionsAddMembersToAttributeDef'] }</a>
   </c:when>
   <c:when test="${grouperRequestContainer.subjectContainer.showEntityPrivilege}">
     <a id="show-add-block" href="javascript:void(0);" onclick="showHideMemberAddBlock()" 
       class="btn btn-medium btn-primary btn-block" role="button">
         <i class="fa fa-plus"></i> ${textContainer.text['groupViewMoreActionsAddMembers'] }
     </a>
   </c:when>
   <c:otherwise>
     <a id="show-add-block" href="#" onclick="$('#add-block-container').toggle('slow'); return false;" class="btn btn-medium btn-block btn-primary"
         style="white-space: nowrap;">
       <i class="fa fa-plus"></i> ${textContainer.text['subjectViewMoreActionsAddMembers'] }</a> 
   </c:otherwise>
 </c:choose>
 
 <div class="btn-group btn-block">
 
   <a data-toggle="dropdown" href="#" aria-label="${textContainer.text['ariaLabelGuiMoreSubjectActions']}" id="more-action-button" class="btn btn-medium btn-block dropdown-toggle" 
   	aria-haspopup="true" aria-expanded="false" role="menu" onclick="$('#subject-more-options').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#subject-more-options li').first().focus();return true;});">
   		${textContainer.text['subjectViewMoreActionsButton'] } <span class="caret"></span></a>

   <ul class="dropdown-menu dropdown-menu-right" id="subject-more-options">
     
     <%-- add or remove to/from my favorites, this causes a success message --%>
     <c:choose>
		   <c:when test="${grouperRequestContainer.subjectContainer.favorite}">
		     <li>
		       <a href="#" 
		         onclick="ajax('../app/UiV2Subject.removeFromMyFavorites?subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}'); return false;"
		       >${textContainer.text['subjectViewMoreActionsRemoveFromMyFavorites'] }</a>
		     </li>
		   </c:when>
		   <c:otherwise>
		     <li>
		       <a href="#" 
		         onclick="ajax('../app/UiV2Subject.addToMyFavorites?subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}'); return false;"
		       >${textContainer.text['subjectViewMoreActionsAddToMyFavorites']}</a>
		     </li>
		   </c:otherwise>
		 </c:choose>
 
     <li class="divider"></li>
     <li><a href="javascript:void(0)" onclick="return guiV2link('operation=UiV2SubjectPermission.subjectPermission&subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}'); return false;"
                              >${textContainer.text['subjectViewPermissionsButton'] }</a></li>
     
     <li class="divider"></li>
     <li>
       <a href="#" onclick="ajax('../app/UiV2SubjectAttributeAssignment.viewAttributeAssignments?subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}'); return false;">
         ${textContainer.text['subjectAttributeAssignmentsButton'] }
       </a>
     </li>
     <c:if test="${grouperRequestContainer.subjectContainer.canSeeAudits}">
     
       <li class="divider"></li>
       <li><a href="#" onclick="return guiV2link('operation=UiV2Subject.viewAudits&subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}'); return false;"
           >${textContainer.text['subjectViewMembershipAuditButton'] }</a></li>
       
       <li><a href="#" onclick="return guiV2link('operation=UiV2Subject.viewAudits&subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}&auditType=actions'); return false;"
           >${textContainer.text['subjectViewActionAuditButton'] }</a></li>
       
       <li><a href="#" onclick="return guiV2link('operation=UiV2Subject.viewAudits&subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}&auditType=privileges'); return false;"
           >${textContainer.text['subjectViewPrivilegeAuditButton'] }</a></li>
           
     </c:if>

      <c:if test="${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId == 'grouperEntities' && grouperRequestContainer.groupContainer.canAdmin }">
       	<li class="divider"></li>
        <li><a href="#" onclick="return guiV2link('operation=UiV2LocalEntity.localEntityEdit&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}'); return false;"
          >${textContainer.text['localEntityViewEditLocalEntityButton'] }</a></li>
        <li><a href="#" onclick="return guiV2link('operation=UiV2LocalEntity.localEntityDelete&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}'); return false;"
          >${textContainer.text['localEntityViewDeleteLocalEntityButton'] }</a></li>
      </c:if>

     <li class="divider"></li>
     <li><a href="javascript:void(0)" onclick="return guiV2link('operation=UiV2Visualization.subjectView&subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}'); return false;"
           >${textContainer.text['visualization.title'] }</a></li>
     
     <c:if test="${grouperRequestContainer.provisioningContainer.canReadProvisioningForSubject}">      
	   <li class="divider"></li>
	   <li><a href="javascript:void(0)" onclick="return guiV2link('operation=UiV2Provisioning.viewProvisioningOnSubject&subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}'); return false;"
           >${textContainer.text['subjectViewProvisioningButton'] }</a></li>
     </c:if>
     
     <c:if test="${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId == 'grouperEntities' && grouperRequestContainer.subjectContainer.canViewWsJwtKey }">
        <li class="divider"></li>
        <li><a href="#" onclick="return guiV2link('operation=UiV2LocalEntity.viewLocalEntityWSJwtKeys&subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}'); return false;"
          >${textContainer.text['localEntityWsJwtKeyLocalEntityButton'] }</a></li>
      </c:if>

   </ul>
 </div>
