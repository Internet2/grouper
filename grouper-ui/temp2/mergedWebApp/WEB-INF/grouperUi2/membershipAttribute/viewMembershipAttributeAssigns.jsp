<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <%-- show the add member button for privileges --%>
            <c:set target="${grouperRequestContainer.groupContainer}" property="showAddMember" value="false" />

            <%@ include file="../subject/subjectHeader.jsp" %>

            <div class="row-fluid">
              <div class="span12">
                <div id="messages"></div>
                
                <ul class="nav nav-tabs">
                  <li><a role="tab" href="#" onclick="return guiV2link('operation=UiV2Subject.viewSubject&subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}', {dontScrollTop: true});" >${textContainer.text['subjectMembershipsTab'] }</a></li>
                  <li><a role="tab" aria-selected="true" href="#" onclick="return false;" >${textContainer.text['subjectPrivilegesTab'] }</a></li>
                  <li><a href="#" role="tab" onclick="return guiV2link('operation=UiV2Subject.thisSubjectsStemPrivileges&subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}', {dontScrollTop: true});" >${textContainer.text['subjectStemPrivilegesTab'] }</a></li>
                  <li><a href="#" role="tab" onclick="return guiV2link('operation=UiV2Subject.thisSubjectsAttributeDefPrivileges&subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}', {dontScrollTop: true});" >${textContainer.text['subjectAttributePrivilegesTab'] }</a></li>
                  <c:if test="${grouperRequestContainer.rulesContainer.canReadPrivilegeInheritance}">
                    <%@ include file="../subject/subjectMoreTab.jsp" %>
                  </c:if>
                </ul>
                
                <div id="membershipAttributeAssignments">
                  <%@ include file="membershipAttributeAssignmentSection.jsp"%>
                </div>

              </div>
            </div>