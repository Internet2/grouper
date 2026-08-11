<%@ include file="../assetsJsp/commonTaglib.jsp"%>

              <%-- tell add member to refresh audits --%>
              <form id="groupRefreshPartFormId">
                <input type="hidden" name="groupRefreshPart" value="thisGroupsStemPrivileges" /> 
              </form> 

              <form class="form-inline form-small" name="groupPrivilegeFormName" id="groupPrivilegeFormId">
                <table class="table table-hover table-bordered table-striped table-condensed data-table table-bulk-update table-privileges footable">
                  <thead>
                    <tr>
                      <td colspan="11" class="table-toolbar gradient-background">
                        <div class="row-fluid">
                          <div class="span1">
                            <label for="people-update">Update:</label>
                          </div>
                          <div class="span4">

                            <select id="people-update" class="span12" name="groupPrivilegeBatchUpdateOperation">
                              <%-- create group should be the default, so list it first --%>

                              <option value="assign_stemAdmins">${textContainer.text['groupPrivilegesAssignStemAdminPrivilege'] }</option>
                              <option value="assign_creators">${textContainer.text['groupPrivilegesAssignCreatePrivilege'] }</option>
                              <option value="assign_stemAttrReaders">${textContainer.text['groupPrivilegesAssignStemAttributeReadPrivilege'] }</option>
                              <option value="assign_stemAttrUpdaters">${textContainer.text['groupPrivilegesAssignStemAttributeUpdatePrivilege'] }</option>
                              <option value="assign_stemViewers">${textContainer.text['groupPrivilegesAssignStemViewPrivilege'] }</option>
                              <option value="revoke_stemAdmins">${textContainer.text['groupPrivilegesRevokeStemAdminPrivilege'] }</option>
                              <option value="revoke_creators">${textContainer.text['groupPrivilegesRevokeCreatePrivilege'] }</option>
                              <option value="revoke_stemAttrReaders">${textContainer.text['groupPrivilegesRevokeStemAttributeReadPrivilege'] }</option>
                              <option value="revoke_stemAttrUpdaters">${textContainer.text['groupPrivilegesRevokeStemAttributeUpdatePrivilege'] }</option>
                              <option value="revoke_stemViewers">${textContainer.text['groupPrivilegesRevokeStemViewPrivilege'] }</option>
                              <option value="revoke_all">${textContainer.text['groupPrivilegesRevokeAllStemPrivilege'] }</option>

                            </select>
                          </div>
                          <div class="span4">
                            <button type="submit" class="btn" 
                              onclick="ajax('../app/UiV2Group.thisGroupsPrivilegesAssignStemPrivilegeBatch?groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}', {formIds: 'groupFilterPrivilegesFormId,groupPagingPrivilegesFormId,groupPagingPrivilegesFormPageNumberId,groupPrivilegeFormId'}); return false;">${textContainer.text['thisGroupPrivilegeUpdateSelectedButton'] }</button>
                          </div>
                        </div>
                      </td>
                    </tr>
                    <tr>
                      <th>
                        <label class="checkbox checkbox-no-padding">
                          <input type="checkbox" name="notImportantXyzName" id="notImportantXyzId" onchange="$('.privilegeCheckbox').prop('checked', $('#notImportantXyzId').prop('checked'));" />
                          <span class="sr-only">${textContainer.text['groupPrivilegesInOtherFoldersCheckboxAriaLabel']}</span>
                        </label>
                      </th>
                      <th>
                        ${textContainer.text['thisGroupsPrivilegesStemColumn'] }
                      </th>
                      <th data-hide="phone" style="white-space: nowrap; text-align: center; width: 10em;">${textContainer.text['priv.colStemAdmin'] }</th>
                      <th data-hide="phone" style="white-space: nowrap; text-align: center; width: 10em;">${textContainer.text['priv.colCreate'] }</th>
                      <th data-hide="phone" style="white-space: nowrap; text-align: center; width: 10em;">${textContainer.text['priv.colStemAttributeRead'] }</th>
                      <th data-hide="phone" style="white-space: nowrap; text-align: center; width: 10em;">${textContainer.text['priv.colStemAttributeUpdate'] }</th>
                      <th data-hide="phone" style="white-space: nowrap; text-align: center; width: 10em;">${textContainer.text['priv.colStemView'] }</th>
                      <th style="width:100px;">${textContainer.text['headerChooseAction']}</th>
                    </tr>
                  </thead>
                  <tbody>
                    <c:set var="i" value="0" />
                    <c:forEach  items="${grouperRequestContainer.groupContainer.privilegeGuiMembershipSubjectContainers}" 
                        var="guiMembershipSubjectContainer" >
                      <tr>
                        <td>
                          <label class="checkbox checkbox-no-padding">
                            <input type="checkbox" name="privilegeSubjectRow_${i}[]" aria-label="${textContainer.text['groupPrivilegesInOtherFoldersCheckboxAriaLabel'] }"
                            value="${guiMembershipSubjectContainer.guiStem.stem.id}" class="privilegeCheckbox" />
                          </label>
                        </td>
                        <td class="expand foo-clicker" style="white-space: nowrap">${guiMembershipSubjectContainer.guiStem.shortLinkWithIcon}
                        </td>
                        <%-- loop through the fields for groups --%>
                        <c:forEach items="stemAdmins,creators,stemAttrReaders,stemAttrUpdaters,stemViewers" var="fieldName">
                          <td data-hide="phone,medium" class="direct-actions privilege" >
                            <c:set value="${guiMembershipSubjectContainer.guiMembershipContainers[fieldName]}" var="guiMembershipContainer" />
                            <%-- GRP-7096: build specific accessible names for this cell, e.g. "Remove Attribute update for someFolder" (column label minus its line break) --%>
                            <c:choose>
                              <c:when test="${fieldName == 'stemAdmins'}"><c:set var="privName" value="${fn:replace(textContainer.text['priv.colStemAdmin'], '<br />', ' ')}" /></c:when>
                              <c:when test="${fieldName == 'creators'}"><c:set var="privName" value="${fn:replace(textContainer.text['priv.colCreate'], '<br />', ' ')}" /></c:when>
                              <c:when test="${fieldName == 'stemAttrReaders'}"><c:set var="privName" value="${fn:replace(textContainer.text['priv.colStemAttributeRead'], '<br />', ' ')}" /></c:when>
                              <c:when test="${fieldName == 'stemAttrUpdaters'}"><c:set var="privName" value="${fn:replace(textContainer.text['priv.colStemAttributeUpdate'], '<br />', ' ')}" /></c:when>
                              <c:otherwise><c:set var="privName" value="${fn:replace(textContainer.text['priv.colStemView'], '<br />', ' ')}" /></c:otherwise>
                            </c:choose>
                            <c:set var="ariaRemovePrivilege" value="${textContainer.text['ariaLabelPrivilegeRemove']} ${privName} ${textContainer.text['ariaLabelPrivilegeForSubject']} ${guiMembershipSubjectContainer.guiStem.stem.displayExtension}" />
                            <c:set var="ariaAssignPrivilege" value="${textContainer.text['ariaLabelPrivilegeAssign']} ${privName} ${textContainer.text['ariaLabelPrivilegeForSubject']} ${guiMembershipSubjectContainer.guiStem.stem.displayExtension}" />
                            <%-- if there is a container, then there is an assignment of some sort... --%>
                            <c:choose>
                              <c:when test="${guiMembershipContainer != null 
                                   && guiMembershipContainer.membershipContainer.membershipAssignType.immediate}">
                                <button type="button" class="privilege-check-btn fa fa-check fa-direct" title="${grouper:escapeHtml(ariaRemovePrivilege)}" aria-label="${grouper:escapeHtml(ariaRemovePrivilege)}" onclick="if (confirmChange('${textContainer.textEscapeSingleDouble['groupConfirmChanges']}')) {ajax('../app/UiV2Group.thisGroupsPrivilegesAssignStemPrivilege?assign=false&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}&fieldName=${fieldName}&parentStemId=${guiMembershipSubjectContainer.guiStem.stem.id}', {formIds: 'groupFilterPrivilegesFormId,groupPagingPrivilegesFormId,groupPagingPrivilegesFormPageNumberId'});} return false;"></button>
                                <button type="button" title="${grouper:escapeHtml(ariaRemovePrivilege)}" aria-label="${grouper:escapeHtml(ariaRemovePrivilege)}" class="btn btn-inverse btn-super-mini remove" 
                                   onclick="if (confirmChange('${textContainer.textEscapeSingleDouble['groupConfirmChanges']}')) {ajax('../app/UiV2Group.thisGroupsPrivilegesAssignStemPrivilege?assign=false&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}&fieldName=${fieldName}&parentStemId=${guiMembershipSubjectContainer.guiStem.stem.id}', {formIds: 'groupFilterPrivilegesFormId,groupPagingPrivilegesFormId,groupPagingPrivilegesFormPageNumberId'});} return false;"
                                  ><i class="fa fa-times" aria-hidden="true"></i></button>
                              </c:when>
                              <c:otherwise>
                                <c:if test="${guiMembershipContainer != null}"><button type="button" class="privilege-check-btn fa fa-check fa-disabled" title="${grouper:escapeHtml(ariaAssignPrivilege)}" aria-label="${grouper:escapeHtml(ariaAssignPrivilege)}" onclick="if (confirmChange('${textContainer.textEscapeSingleDouble['groupConfirmChanges']}')) {ajax('../app/UiV2Group.thisGroupsPrivilegesAssignStemPrivilege?assign=true&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}&fieldName=${fieldName}&parentStemId=${guiMembershipSubjectContainer.guiStem.stem.id}', {formIds: 'groupFilterPrivilegesFormId,groupPagingPrivilegesFormId,groupPagingPrivilegesFormPageNumberId'});} return false;"></button></c:if>
                                <button type="button" title="${grouper:escapeHtml(ariaAssignPrivilege)}" aria-label="${grouper:escapeHtml(ariaAssignPrivilege)}" class="btn btn-inverse btn-super-mini remove" 
                                   onclick="if (confirmChange('${textContainer.textEscapeSingleDouble['groupConfirmChanges']}')) {ajax('../app/UiV2Group.thisGroupsPrivilegesAssignStemPrivilege?assign=true&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}&fieldName=${fieldName}&parentStemId=${guiMembershipSubjectContainer.guiStem.stem.id}', {formIds: 'groupFilterPrivilegesFormId,groupPagingPrivilegesFormId,groupPagingPrivilegesFormPageNumberId'});} return false;"
                                  ><i class="fa fa-plus" aria-hidden="true"></i></button>
                              </c:otherwise>
                            </c:choose>
                          </td>
                        </c:forEach>
                        <td>
                          <div class="btn-group">
                          	<a data-toggle="dropdown" href="#" aria-label="${textContainer.text['ariaLabelGuiMoreOptions']}" class="btn btn-mini dropdown-toggle"
                          		aria-haspopup="true" aria-expanded="false" role="button" onclick="$('#more-options${i}').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#more-options${i} li').first().focus();return true;});">
                          		${textContainer.text['thisGroupsPrivilegesActionsButton']} 
                          			<span class="caret"></span>
                          	</a>
                            <ul class="dropdown-menu dropdown-menu-right" id="more-options${i}">
                              <c:if test="${guiMembershipContainer.membershipContainer.membershipAssignType.nonImmediate}">
                                <li><a href="?operation=UiV2Membership.traceStemPrivileges&stemId=${guiMembershipSubjectContainer.guiStem.stem.id}&memberId=${guiMembershipSubjectContainer.guiMember.member.uuid}&backTo=subject"  onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Membership.traceStemPrivileges&stemId=${guiMembershipSubjectContainer.guiStem.stem.id}&memberId=${guiMembershipSubjectContainer.guiMember.member.uuid}&backTo=subject'); return false;" class="actions-revoke-membership">${textContainer.text['thisGroupsPrivilegesActionsMenuTracePrivileges'] }</a></li>
                              </c:if>
                              <li><a href="?operation=UiV2Stem.viewStem&stemId=${guiMembershipSubjectContainer.guiStem.stem.id}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Stem.viewStem&stemId=${guiMembershipSubjectContainer.guiStem.stem.id}');">${textContainer.text['thisGroupsPrivilegesActionsMenuViewStem']}</a></li>
                            </ul>
                          </div>
                        </td>
                      </tr>
                      <c:set var="i" value="${i+1}" />
                    </c:forEach>
                  </tbody>
                </table>
              </form>
              <div class="data-table-bottom gradient-background">
                <grouper:paging2 guiPaging="${grouperRequestContainer.groupContainer.privilegeGuiPaging}" formName="groupPagingPrivilegesForm" ajaxFormIds="groupFilterPrivilegesFormId"
                  refreshOperation="../app/UiV2Group.filterThisGroupsStemPrivileges?groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}" />
              </div>
              
