<%@ include file="../assetsJsp/commonTaglib.jsp"%>

${grouper:titleFromKeyAndText('inheritedPrivilegesPageTitle', grouperRequestContainer.subjectContainer.guiSubject.subject.name)}

            <%@ include file="subjectHeader.jsp" %>

            <div class="row-fluid">
              <div class="span12">
                <ul class="nav nav-tabs">
                  <li><a role="tab" href="#" onclick="return guiV2link('operation=UiV2Subject.viewSubject&subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}', {dontScrollTop: true});" >${textContainer.text['subjectMembershipsTab'] }</a></li>
                  <c:if test="${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId == 'grouperEntities' && grouperRequestContainer.groupContainer.canAdmin}">
                    <li><a role="tab" href="#" onclick="return guiV2link('operation=UiV2LocalEntity.localEntityPrivileges&groupId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}', {dontScrollTop: true});" >${textContainer.text['groupPrivilegesTab'] }</a></li>
                  </c:if>
                  <li><a role="tab" href="#" onclick="return guiV2link('operation=UiV2Subject.thisSubjectsGroupPrivileges&subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}', {dontScrollTop: true});" >${textContainer.text['subjectPrivilegesTab'] }</a></li>
                  <li><a role="tab" href="#" onclick="return guiV2link('operation=UiV2Subject.thisSubjectsStemPrivileges&subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}', {dontScrollTop: true});" >${textContainer.text['subjectStemPrivilegesTab'] }</a></li>
                  <li><a role="tab" href="#" onclick="return guiV2link('operation=UiV2Subject.thisSubjectsAttributeDefPrivileges&subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}', {dontScrollTop: true});" >${textContainer.text['subjectAttributePrivilegesTab'] }</a></li>
                  <c:if test="${grouperRequestContainer.rulesContainer.canReadPrivilegeInheritance}">
                    <%@ include file="subjectMoreTab.jsp" %>
                  </c:if>
                </ul>
                <p class="lead">${textContainer.text['subjectPrivilegesInheritedFromFoldersDecription'] }</p>
                <c:choose>
                  <c:when test="${fn:length(grouperRequestContainer.rulesContainer.guiRuleDefinitions) == 0}">
                    <p>${textContainer.text['subjectPrivilegesInheritedNone'] }</p>
                  </c:when>
                  <c:otherwise>
      
                    <p style="margin-top: -1em;">${textContainer.text['subjectPrivilegesInheritedFromFoldersSubtitle'] }</p>
      
                    <table class="table table-hover table-bordered table-striped table-condensed data-table table-bulk-update table-privileges footable">
                      <thead>
                        <tr>
                          <th data-hide="phone" style="white-space: nowrap; text-align: left;">${textContainer.text['stemPrivilegesInheritColumnHeaderStemName'] }</th>
                          <th data-hide="phone" style="white-space: nowrap; text-align: left;">${textContainer.text['stemPrivilegesInheritColumnHeaderAssignedTo'] }</th>
                          <th data-hide="phone" style="white-space: nowrap; text-align: left;">${textContainer.text['stemPrivilegesInheritColumnHeaderObjectType'] }</th>
                          <th data-hide="phone" style="white-space: nowrap; text-align: left;">${textContainer.text['stemPrivilegesInheritColumnHeaderLevels'] }</th>
                          <th data-hide="phone" style="white-space: nowrap; text-align: left;">${textContainer.text['stemPrivilegesInheritColumnHeaderPrivileges'] }</th>
                        </tr>
                      </thead>
                      <tbody>
                        <c:set var="i" value="0" />
                        <c:forEach  items="${grouperRequestContainer.rulesContainer.guiRuleDefinitions}" 
                          var="guiRuleDefinition" >
    
                          <tr>
                            <td class="expand foo-clicker" style="white-space: nowrap;">${guiRuleDefinition.ownerGuiStem.shortLinkWithIcon}
                            </td>
                            <td class="expand foo-clicker">${guiRuleDefinition.thenArg0subject.shortLinkWithIcon}
                            </td>
                            <td class="expand foo-clicker">${guiRuleDefinition.thenTypeLabel}
                            </td>
                            <td class="expand foo-clicker">
                              <c:if test="${guiRuleDefinition.checkStemScopeOne}">
                                ${textContainer.text['rulesStemScopeOne'] }
                              </c:if>
                              <c:if test="${guiRuleDefinition.checkStemScopeSub}">
                                ${textContainer.text['rulesStemScopeSub'] }
                              </c:if>
                                                    
                            </td>
                            <td>
                              ${guiRuleDefinition.thenArg1privileges}
                            </td>
                          </tr>
    
                          <c:set var="i" value="${i+1}" />
                        </c:forEach>
                      </tbody>
                    </table>
                  </c:otherwise>
                </c:choose>
              </div>
            </div>
            
