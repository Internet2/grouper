<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <%-- for the new group or new stem button --%>
            <input type="hidden" name="objectStemId" value="${grouperRequestContainer.groupContainer.guiGroup.group.parentUuid}" />

            <%-- show the add member button for privileges --%>
            <c:set target="${grouperRequestContainer.groupContainer}" property="showAddMember" value="false" />
            <%@ include file="groupHeader.jsp" %>

            <div class="row-fluid">
              <div class="span12 tab-interface">
                <ul class="nav nav-tabs">
                  <li><a role="tab" href="#" onclick="return guiV2link('operation=UiV2Group.viewGroup&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}', {dontScrollTop: true});" >${textContainer.text['groupMembersTab'] }</a></li>
                  <c:if test="${grouperRequestContainer.groupContainer.canAdmin}">
                    <li><a role="tab" href="#" onclick="return guiV2link('operation=UiV2Group.groupPrivileges&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}', {dontScrollTop: true});" >${textContainer.text['groupPrivilegesTab'] }</a></li>
                  </c:if>
                  <%@ include file="groupMoreTab.jsp" %>
                </ul>
                <p class="lead">${textContainer.text['groupPrivilegesInheritedFromFoldersDecription'] }</p>
                <c:choose>
                  <c:when test="${fn:length(grouperRequestContainer.rulesContainer.guiRuleDefinitions) == 0}">
                    <p>${textContainer.text['groupPrivilegesInheritedNone'] }</p>
                  </c:when>
                  <c:otherwise>
      
                    <p style="margin-top: -1em;">${textContainer.text['groupPrivilegesInheritedSubtitle'] }</p>
      
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