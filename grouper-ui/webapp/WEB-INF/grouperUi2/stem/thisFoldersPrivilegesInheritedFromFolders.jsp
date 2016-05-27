<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <%-- for the new group or new stem button --%>
            <input type="hidden" name="objectStemId" value="${grouperRequestContainer.stemContainer.guiStem.stem.id}" />

            <%-- show the add member button for privileges --%>
            <c:set target="${grouperRequestContainer.stemContainer}" property="showAddMember" value="false" />
            <%@ include file="stemHeader.jsp" %>

            <div class="row-fluid">
              <div class="span12">
                <ul class="nav nav-tabs">
                  <li><a href="#" onclick="return guiV2link('operation=UiV2Stem.viewStem&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}', {dontScrollTop: true});" >${textContainer.text['stemContents'] }</a></li>
                  <c:if test="${grouperRequestContainer.stemContainer.canAdminPrivileges}">
                    <li><a href="#" onclick="return guiV2link('operation=UiV2Stem.stemPrivileges&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}', {dontScrollTop: true});" >${textContainer.text['stemPrivileges'] }</a></li>
                  </c:if>
                  <c:if test="${grouperRequestContainer.stemContainer.canReadPrivilegeInheritance}">
                    
                    <%@ include file="stemMoreTab.jsp" %>
                    
                  </c:if>
                </ul>
                <p class="lead">${textContainer.text['stemPrivilegesInheritedFromFoldersDecription'] }</p>
                <c:choose>
                  <c:when test="${fn:length(grouperRequestContainer.rulesContainer.guiRuleDefinitions) == 0}">
                    <p>${textContainer.text['stemPrivilegesInheritedFromStemNone'] }</p>
                  </c:when>
                  <c:otherwise>
      
                    <p style="margin-top: -1em;">${textContainer.text['stemPrivilegesInheritedSubtitle'] }</p>
      
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
            
