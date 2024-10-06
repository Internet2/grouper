<%@ include file="../assetsJsp/commonTaglib.jsp"%>

${grouper:titleFromKeyAndText('subjectProvisioningPageTitle', grouperRequestContainer.subjectContainer.guiSubject.subject.name)}

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
			      <div class="lead span9">${textContainer.text['provisioningSubjectProvisioningTitle'] }</div>
			      <div class="span3" id="grouperProvisioningSubjectMoreActionsButtonContentsDivId">
			        <%@ include file="provisioningSubjectMoreActionsButtonContents.jsp"%>
			      </div>
			    </div>
			    
			    <%-- <div class="row-fluid">
			      <div class="span9"> <p>${textContainer.text['provisioningGroupProvisioningDescription'] }</p></div>
			    </div> --%>

                <%@ include file="provisioningSubjectProvisionersTableHelper.jsp" %>

              </div>
            </div>
