<%@ include file="../assetsJsp/commonTaglib.jsp"%>

              <form class="form-inline form-small" name="stemPrivilegeFormName" id="stemPrivilegeFormId">
                <table class="table table-hover table-bordered table-striped table-condensed data-table table-bulk-update table-privileges footable">
                  <thead>
                    <tr>
                      <td colspan="7" class="table-toolbar gradient-background">
                        <c:if test="${grouperRequestContainer.stemContainer.canUpdateAttributes}">
                          <a href="#" onclick="ajax('../app/UiV2Group.removeMembers?groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}', {formIds: 'groupFilterFormId,groupPagingFormId,membersToDeleteFormId'}); return false;" class="btn">${textContainer.text['groupRemoveSelectedMembersButton'] }</a>
                        </c:if>
                      </td>
                    </tr>
                    <tr>
                      <th>
                        <label class="checkbox checkbox-no-padding">
                          <input type="checkbox" name="notImportantXyzName" id="notImportantXyzId" onchange="$('.privilegeCheckbox').prop('checked', $('#notImportantXyzId').prop('checked'));" />
                        </label>
                      </th>
                      <th data-hide="phone" style="white-space: nowrap; text-align: left;">${textContainer.text['stemPrivilegesInheritColumnHeaderStemName'] }</th>
                      <th data-hide="phone" style="white-space: nowrap; text-align: left;">${textContainer.text['stemPrivilegesInheritColumnHeaderAssignedTo'] }</th>
                      <th data-hide="phone" style="white-space: nowrap; text-align: left;">${textContainer.text['stemPrivilegesInheritColumnHeaderObjectType'] }</th>
                      <th data-hide="phone" style="white-space: nowrap; text-align: left;">${textContainer.text['stemPrivilegesInheritColumnHeaderDirect'] }</th>
                      <th data-hide="phone" style="white-space: nowrap; text-align: left;">${textContainer.text['stemPrivilegesInheritColumnHeaderLevels'] }</th>
                      <th data-hide="phone" style="white-space: nowrap; text-align: left;">${textContainer.text['stemPrivilegesInheritColumnHeaderPrivileges'] }</th>
                    </tr>
                  </thead>
                  <tbody>
                    <c:set var="i" value="0" />
                    <c:forEach  items="${grouperRequestContainer.rulesContainer.guiRuleDefinitions}" 
                      var="guiRuleDefinition" >

                      <tr>
                        <td>
                          <c:if test="${guiRuleDefinition.direct}">
                            <label class="checkbox checkbox-no-padding">
                              <input type="checkbox" name="privilegeSubjectRow_${i}[]" value="${guiMembershipSubjectContainer.guiMember.member.uuid}" class="privilegeCheckbox" />
                            </label>
                          </c:if>
                        </td>
                        <td class="expand foo-clicker">${guiRuleDefinition.ownerGuiStem.shortLinkWithIcon}
                        </td>
                        <td class="expand foo-clicker">${guiRuleDefinition.thenArg0subject.shortLinkWithIcon}
                        </td>
                        <td class="expand foo-clicker">${guiRuleDefinition.thenTypeLabel}
                        </td>
                        <td class="expand foo-clicker">
                          <c:choose>
                            <c:when test="${guiRuleDefinition.direct}">${textContainer.text['rulesCheckDirect'] }</c:when>
                            <c:otherwise>${textContainer.text['rulesCheckIndirect'] }</c:otherwise>
                          </c:choose>
                        
                        
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
              </form>
