<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <div class="bread-header-container">
              <ul class="breadcrumb">
                  <li><a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');">${textContainer.text['myServicesHomeBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                  <li><a href="#" onclick="return guiV2link('operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                  <li class="active">${textContainer.text['miscellaneousInheritedPrivilegesBreadcrumb'] }</li>
              </ul>
              <div class="page-header blue-gradient">
                <h1>${textContainer.text['miscellaneousPrivilegesInheritedFromFoldersDecription'] }</h1>
                <p style="margin-top: -1em; margin-bottom: 1em">${textContainer.text['miscellaneousPrivilegesInheritedFromFoldersSubtitle']}</p>
              </div>

            </div>

            <div class="row-fluid">
              <div class="span12">
                <c:choose>
                  <c:when test="${fn:length(grouperRequestContainer.rulesContainer.guiRuleDefinitions) == 0}">
                    <p>${textContainer.text['miscellaneousPrivilegesInheritedNone'] }</p>
                  </c:when>
                  <c:otherwise>
      
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
            
