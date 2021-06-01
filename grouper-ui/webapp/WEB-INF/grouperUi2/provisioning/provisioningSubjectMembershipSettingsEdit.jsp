<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <%-- show the add member button for privileges --%>
            <c:set target="${grouperRequestContainer.stemContainer}" property="showAddMember" value="true" />
            
            <%@ include file="../subject/subjectHeader.jsp" %>

            <div class="row-fluid">
              <div class="span12">
                <div id="messages"></div>

                <ul class="nav nav-tabs">
                  <li><a role="tab" href="#" onclick="return guiV2link('operation=UiV2Subject.viewSubject&subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}', {dontScrollTop: true});" >${textContainer.text['subjectMembershipsTab'] }</a></li>
                  <c:if test="${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId == 'grouperEntities' && grouperRequestContainer.groupContainer.canAdmin}">
                    <li><a role="tab" href="#" onclick="return guiV2link('operation=UiV2LocalEntity.localEntityPrivileges&groupId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}', {dontScrollTop: true});" >${textContainer.text['groupPrivilegesTab'] }</a></li>
                  </c:if>
                  <li><a href="#" role="tab" onclick="return guiV2link('operation=UiV2Subject.thisSubjectsGroupPrivileges&subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}', {dontScrollTop: true});" >${textContainer.text['subjectPrivilegesTab'] }</a></li>
                  <li><a role="tab" href="#" onclick="return false;" >${textContainer.text['subjectStemPrivilegesTab'] }</a></li>
                  <li><a role="tab" href="#" onclick="return guiV2link('operation=UiV2Subject.thisSubjectsAttributeDefPrivileges&subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}', {dontScrollTop: true});" >${textContainer.text['subjectAttributePrivilegesTab'] }</a></li>
                  <c:if test="${grouperRequestContainer.rulesContainer.canReadPrivilegeInheritance}">
                    <%@ include file="../subject/subjectMoreTab.jsp" %>
                  </c:if>
                </ul>
                
                <div class="row-fluid">
			      <div class="lead span9">${textContainer.text['provisioningMembershipProvisioningTitle'] }</div>
			      <div class="span3" id="grouperProvisioningSubjectMembershipMoreActionsButtonContentsDivId">
			        <%@ include file="provisioningSubjectMembershipMoreActionsButtonContents.jsp"%>
			      </div>
			    </div>
			    
			    <div class="row-fluid">
			    	<div class="span9">
			    		${textContainer.text['provisioningMembershipProvisioningGroupFor']}
			    		${grouperRequestContainer.groupContainer.guiGroup.shortLink}
			    	</div>
			    </div>
			    
                <form class="form-inline form-small form-filter" id="editProvisioningFormId">
                  <input type="hidden" name="subjectId" value="${grouperRequestContainer.subjectContainer.guiSubject.subject.id}" />
                  <table class="table table-condensed table-striped">
                    <tbody>
                      <%@ include file="provisioningSubjectMembershipSettingsEditHelper.jsp" %>
                      <tr>
                        <td></td>
                        <td
                          style="white-space: nowrap; padding-top: 2em; padding-bottom: 2em;">
                          <input type="submit" class="btn btn-primary"
                          aria-controls="objectTypeSubmitId" id="submitId"
                          value="${textContainer.text['provisioningEditButtonSave'] }"
                          onclick="ajax('../app/UiV2Provisioning.editProvisioningOnSubjectMembershipSave?subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}', {formIds: 'editProvisioningFormId'}); return false;">
                          &nbsp; <a class="btn btn-cancel" role="button"
                          onclick="return guiV2link('operation=UiV2Provisioning.viewProvisioningOnSubjectMembership&subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}'); return false;"
                          >${textContainer.text['provisioningEditButtonCancel'] }</a>
                        </td>
                      </tr>

                    </tbody>
                  </table>
                  
                </form>

              </div>
            </div>
        