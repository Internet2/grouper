<%@ include file="../assetsJsp/commonTaglib.jsp"%>

${grouper:titleFromKeyAndText('subjectViewAuditsPageTitle', grouperRequestContainer.subjectContainer.guiSubject.subject.name)}

            <%@ include file="subjectHeader.jsp" %>

            <div class="row-fluid">
              <div class="span12">
                <div id="messages"></div>
                <ul class="nav nav-tabs">
                  <li><a role="tab" href="?operation=UiV2Subject.viewSubject&subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Subject.viewSubject&subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}', {dontScrollTop: true});" >${textContainer.text['subjectMembershipsTab'] }</a></li>
                  <c:if test="${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId == 'grouperEntities' && grouperRequestContainer.groupContainer.canAdmin}">
                    <li><a role="tab" href="?operation=UiV2LocalEntity.localEntityPrivileges&groupId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2LocalEntity.localEntityPrivileges&groupId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}', {dontScrollTop: true});" >${textContainer.text['groupPrivilegesTab'] }</a></li>
                  </c:if>
                  <li><a role="tab" aria-selected="true" href="#" onclick="return false;" >${textContainer.text['subjectPrivilegesTab'] }</a></li>
                  <li><a role="tab" href="?operation=UiV2Subject.thisSubjectsStemPrivileges&subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Subject.thisSubjectsStemPrivileges&subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}', {dontScrollTop: true});" >${textContainer.text['subjectStemPrivilegesTab'] }</a></li>
                  <li><a role="tab" href="?operation=UiV2Subject.thisSubjectsAttributeDefPrivileges&subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Subject.thisSubjectsAttributeDefPrivileges&subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}', {dontScrollTop: true});" >${textContainer.text['subjectAttributePrivilegesTab'] }</a></li>
                  <c:if test="${grouperRequestContainer.rulesContainer.canReadPrivilegeInheritance}">
                    <%@ include file="subjectMoreTab.jsp" %>
                  </c:if>
                </ul>

                <p class="lead">${textContainer.text['subjectDataFieldConfigsDescription'] }</p>

                <div id="subjectDataConfigResultsId">
                </div>                
              </div>
            </div>
            <!-- end group/groupViewAudits.jsp -->