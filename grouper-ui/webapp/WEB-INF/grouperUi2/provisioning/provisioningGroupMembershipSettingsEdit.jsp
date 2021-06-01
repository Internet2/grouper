<%@ include file="../assetsJsp/commonTaglib.jsp"%>

<%-- for the new group or new stem button --%>
<input type="hidden" name="objectStemId" value="${grouperRequestContainer.groupContainer.guiGroup.group.parentUuid}" />

<%@ include file="../group/groupHeader.jsp" %>

<div class="row-fluid">
  <div class="span12">
    <div id="messages"></div>
        
    <div class="tab-interface">
      <ul class="nav nav-tabs">
        <li><a role="tab" href="#" onclick="return guiV2link('operation=UiV2Group.viewGroup&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}', {dontScrollTop: true});" >${textContainer.text['groupMembersTab'] }</a></li>
        <c:if test="${grouperRequestContainer.groupContainer.canAdmin}">
          <li><a role="tab" href="#" onclick="return guiV2link('operation=UiV2Group.groupPrivileges&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}', {dontScrollTop: true});" >${textContainer.text['groupPrivilegesTab'] }</a></li>
        </c:if>
        <%@ include file="../group/groupMoreTab.jsp" %>
      </ul>
    </div>
    <div class="row-fluid">
      <div class="lead span12">${textContainer.text['provisioningMembershipProvisioningTitle'] }</div>
    </div>
    
    <div class="row-fluid">
     <%--  <div class="span9"> <p>${textContainer.text['provisioningMembershipProvisioningDescription'] }</p></div> --%>
    </div>
    
    <div class="row-fluid">
    	<div class="span9">
    		${textContainer.text['provisioningMembershipProvisioningSubjectFor']}
    		${grouperRequestContainer.subjectContainer.guiSubject.shortLink}
    	</div>
    </div>
    
    <form class="form-inline form-small form-filter" id="editProvisioningFormId">
       <input type="hidden" name="subjectId" value="${grouperRequestContainer.subjectContainer.guiSubject.subject.id}" />
       <table class="table table-condensed table-striped">
         <tbody>
           <%@ include file="provisioningGroupMembershipSettingsEditHelper.jsp" %>
           <tr>
             <td></td>
             <td
               style="white-space: nowrap; padding-top: 2em; padding-bottom: 2em;">
               <input type="submit" class="btn btn-primary"
               aria-controls="objectTypeSubmitId" id="submitId"
               value="${textContainer.text['provisioningEditButtonSave'] }"
               onclick="ajax('../app/UiV2Provisioning.editProvisioningOnGroupMembershipSave?subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}', {formIds: 'editProvisioningFormId'}); return false;">
               &nbsp; <a class="btn btn-cancel" role="button"
               onclick="return guiV2link('operation=UiV2Provisioning.viewProvisioningOnGroupMembership&subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}'); return false;"
               >${textContainer.text['provisioningEditButtonCancel'] }</a>
             </td>
           </tr>

         </tbody>
       </table>
       
     </form>
    
  </div>
</div>
        