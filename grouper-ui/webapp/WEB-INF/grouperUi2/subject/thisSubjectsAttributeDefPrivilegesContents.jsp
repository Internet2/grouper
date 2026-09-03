<%@ include file="../assetsJsp/commonTaglib.jsp"%>

              <form class="form-inline form-small" name="groupPrivilegeFormName" id="groupPrivilegeFormId">
                <table class="table table-hover table-bordered table-striped table-condensed data-table table-bulk-update table-privileges footable">
                  <thead>
                    <tr>
                      <td colspan="11" class="table-toolbar gradient-background">
                        <div class="row-fluid">
                          <div class="span1">
                            <label for="people-update">${textContainer.text['groupPrivilegesUpdateBulkLabel']}</label>
                          </div>
                          <div class="span4">

                            <select id="people-update" class="span12" name="groupPrivilegeBatchUpdateOperation">
                              <%-- create group should be the default, so list it first --%>

                              <option value="assign_attrAdmins">${textContainer.text['groupPrivilegesAssignAttrAdminPrivilege'] }</option>
                              <option value="assign_attrUpdaters">${textContainer.text['groupPrivilegesAssignAttrUpdatePrivilege'] }</option>
                              <option value="assign_readersUpdaters">${textContainer.text['groupPrivilegesAssignAttrReadUpdatePrivilege'] }</option>
                              <option value="assign_attrReaders">${textContainer.text['groupPrivilegesAssignAttrReadPrivilege'] }</option>
                              <option value="assign_attrViewers">${textContainer.text['groupPrivilegesAssignAttrViewPrivilege'] }</option>
                              <option value="assign_attrDefAttrReaders">${textContainer.text['groupPrivilegesAssignAttrDefAttributeReadPrivilege'] }</option>
                              <option value="assign_attrDefAttrUpdaters">${textContainer.text['groupPrivilegesAssignAttrDefAttributeUpdatePrivilege'] }</option>
                              <option value="assign_attrOptins">${textContainer.text['groupPrivilegesAssignAttrOptinPrivilege'] }</option>
                              <option value="assign_attrOptouts">${textContainer.text['groupPrivilegesAssignAttrOptoutPrivilege'] }</option>
                              <option value="revoke_attrAdmins">${textContainer.text['groupPrivilegesRevokeAttrAdminPrivilege'] }</option>
                              <option value="revoke_attrUpdaters">${textContainer.text['groupPrivilegesRevokeAttrUpdatePrivilege'] }</option>
                              <option value="revoke_readersUpdaters">${textContainer.text['groupPrivilegesRevokeAttrReadUpdatePrivilege'] }</option>
                              <option value="revoke_attrReaders">${textContainer.text['groupPrivilegesRevokeAttrReadPrivilege'] }</option>
                              <option value="revoke_attrViewers">${textContainer.text['groupPrivilegesRevokeAttrViewPrivilege'] }</option>
                              <option value="revoke_attrDefAttrReaders">${textContainer.text['groupPrivilegesRevokeAttrDefAttributeReadPrivilege'] }</option>
                              <option value="revoke_attrDefAttrUpdaters">${textContainer.text['groupPrivilegesRevokeAttrDefAttributeUpdatePrivilege'] }</option>
                              <option value="revoke_attrOptins">${textContainer.text['groupPrivilegesRevokeAttrOptinPrivilege'] }</option>
                              <option value="revoke_attrOptouts">${textContainer.text['groupPrivilegesRevokeAttrOptoutPrivilege'] }</option>
                              <option value="revoke_all">${textContainer.text['groupPrivilegesRevokeAttrAllPrivilege'] }</option>

                            </select>
                          </div>
                          <div class="span4">
                            <button type="submit" class="btn" 
                              onclick="ajax('../app/UiV2Subject.thisSubjectsPrivilegesAssignAttributeDefPrivilegeBatch?subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}', {formIds: 'groupFilterPrivilegesFormId,groupPagingPrivilegesFormId,groupPagingPrivilegesFormPageNumberId,groupPrivilegeFormId'}); return false;">${textContainer.text['thisSubjectPrivilegeUpdateSelectedButton'] }</button>
                          </div>
                        </div>
                      </td>
                    </tr>
                    <tr>
                      <th>
                        <label class="checkbox checkbox-no-padding">
                          <input type="checkbox" name="notImportantXyzName" id="notImportantXyzId"
                           onchange="$('.privilegeCheckbox').prop('checked', $('#notImportantXyzId').prop('checked'));" />
                          <span class="sr-only">${textContainer.text['subjectPrivilegesInAttributeDefCheckboxAriaLabel']}</span>
                        </label>
                      </th>
                      <th>
                        ${textContainer.text['thisSubjectsPrivilegesAttributeDefColumn'] }
                      </th>
                      <th data-hide="phone" style="white-space: nowrap; text-align: center; width: 10em;">${textContainer.text['priv.colAttrAdmin'] }</th>
                      <th data-hide="phone" style="white-space: nowrap; text-align: center; width: 10em;">${textContainer.text['priv.colAttrRead'] }</th>
                      <th data-hide="phone" style="white-space: nowrap; text-align: center; width: 10em;">${textContainer.text['priv.colAttrUpdate'] }</th>
                      <th data-hide="phone" style="white-space: nowrap; text-align: center; width: 10em;">${textContainer.text['priv.colAttrOptin'] }</th>
                      <th data-hide="phone" style="white-space: nowrap; text-align: center; width: 10em;">${textContainer.text['priv.colAttrOptout'] }</th>
                      <th data-hide="phone" style="white-space: nowrap; text-align: center; width: 10em;">${textContainer.text['priv.colAttrDefAttributeRead'] }</th>
                      <th data-hide="phone" style="white-space: nowrap; text-align: center; width: 10em;">${textContainer.text['priv.colAttrDefAttributeUpdate'] }</th>
                      <th data-hide="phone" style="white-space: nowrap; text-align: center; width: 10em;">${textContainer.text['priv.colAttrView'] }</th>
                      <th style="width:100px;">${textContainer.text['headerChooseAction']}</th>
                    </tr>
                  </thead>
                  <tbody>
                    <c:set var="i" value="0" />
                    <c:forEach  items="${grouperRequestContainer.subjectContainer.privilegeGuiMembershipSubjectContainers}" 
                        var="guiMembershipSubjectContainer" >
                      <tr>
                        <td>
                          <label class="checkbox checkbox-no-padding">
                            <input type="checkbox" name="privilegeSubjectRow_${i}[]" value="${guiMembershipSubjectContainer.guiAttributeDef.attributeDef.id}" class="privilegeCheckbox" />
                          </label>
                        </td>
                        <td class="expand foo-clicker" style="white-space: nowrap">${guiMembershipSubjectContainer.guiAttributeDef.shortLinkWithIcon}
                        </td>
                        <%-- loop through the fields for groups --%>
                        <c:forEach items="attrAdmins,attrReaders,attrUpdaters,attrOptins,attrOptouts,attrDefAttrReaders,attrDefAttrUpdaters,attrViewers" var="fieldName">
                          <td data-hide="phone,medium" class="direct-actions privilege" >
                            <c:set value="${guiMembershipSubjectContainer.guiMembershipContainers[fieldName]}" var="guiMembershipContainer" />
                            <%-- GRP-7096: build specific accessible names for this cell, e.g. "Remove Admin for someAttributeDef" (column label minus its line break) --%>
                            <c:choose>
                              <c:when test="${fieldName == 'attrAdmins'}"><c:set var="privName" value="${fn:replace(textContainer.text['priv.colAttrAdmin'], '<br />', ' ')}" /></c:when>
                              <c:when test="${fieldName == 'attrReaders'}"><c:set var="privName" value="${fn:replace(textContainer.text['priv.colAttrRead'], '<br />', ' ')}" /></c:when>
                              <c:when test="${fieldName == 'attrUpdaters'}"><c:set var="privName" value="${fn:replace(textContainer.text['priv.colAttrUpdate'], '<br />', ' ')}" /></c:when>
                              <c:when test="${fieldName == 'attrOptins'}"><c:set var="privName" value="${fn:replace(textContainer.text['priv.colAttrOptin'], '<br />', ' ')}" /></c:when>
                              <c:when test="${fieldName == 'attrOptouts'}"><c:set var="privName" value="${fn:replace(textContainer.text['priv.colAttrOptout'], '<br />', ' ')}" /></c:when>
                              <c:when test="${fieldName == 'attrDefAttrReaders'}"><c:set var="privName" value="${fn:replace(textContainer.text['priv.colAttrDefAttributeRead'], '<br />', ' ')}" /></c:when>
                              <c:when test="${fieldName == 'attrDefAttrUpdaters'}"><c:set var="privName" value="${fn:replace(textContainer.text['priv.colAttrDefAttributeUpdate'], '<br />', ' ')}" /></c:when>
                              <c:otherwise><c:set var="privName" value="${fn:replace(textContainer.text['priv.colAttrView'], '<br />', ' ')}" /></c:otherwise>
                            </c:choose>
                            <c:set var="ariaRemovePrivilege" value="${textContainer.text['ariaLabelPrivilegeRemove']} ${privName} ${textContainer.text['ariaLabelPrivilegeForSubject']} ${guiMembershipSubjectContainer.guiAttributeDef.attributeDef.extension}" />
                            <c:set var="ariaAssignPrivilege" value="${textContainer.text['ariaLabelPrivilegeAssign']} ${privName} ${textContainer.text['ariaLabelPrivilegeForSubject']} ${guiMembershipSubjectContainer.guiAttributeDef.attributeDef.extension}" />
                            <%-- if there is a container, then there is an assignment of some sort... --%>
                            <c:choose>
                              <c:when test="${guiMembershipContainer != null 
                                   && guiMembershipContainer.membershipContainer.membershipAssignType.immediate}">
                                <button type="button" class="privilege-check-btn fa fa-check fa-direct" title="${grouper:escapeHtml(ariaRemovePrivilege)}" aria-label="${grouper:escapeHtml(ariaRemovePrivilege)}" onclick="if (confirmChange('${textContainer.textEscapeSingleDouble['groupConfirmChanges']}')) {ajax('../app/UiV2Subject.thisSubjectsPrivilegesAssignAttributeDefPrivilege?assign=false&subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}&fieldName=${fieldName}&parentAttributeDefId=${guiMembershipSubjectContainer.guiAttributeDef.attributeDef.id}', {formIds: 'groupFilterPrivilegesFormId,groupPagingPrivilegesFormId,groupPagingPrivilegesFormPageNumberId'});} return false;"></button>
                                <button type="button" title="${grouper:escapeHtml(ariaRemovePrivilege)}" aria-label="${grouper:escapeHtml(ariaRemovePrivilege)}" class="btn btn-inverse btn-super-mini remove" 
                                   onclick="if (confirmChange('${textContainer.textEscapeSingleDouble['groupConfirmChanges']}')) {ajax('../app/UiV2Subject.thisSubjectsPrivilegesAssignAttributeDefPrivilege?assign=false&subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}&fieldName=${fieldName}&parentAttributeDefId=${guiMembershipSubjectContainer.guiAttributeDef.attributeDef.id}', {formIds: 'groupFilterPrivilegesFormId,groupPagingPrivilegesFormId,groupPagingPrivilegesFormPageNumberId'});} return false;"
                                  ><i class="fa fa-times" aria-hidden="true"></i></button>
                              </c:when>
                              <c:otherwise>
                                <c:if test="${guiMembershipContainer != null}"><button type="button" class="privilege-check-btn fa fa-check fa-disabled" title="${grouper:escapeHtml(ariaAssignPrivilege)}" aria-label="${grouper:escapeHtml(ariaAssignPrivilege)}" onclick="if (confirmChange('${textContainer.textEscapeSingleDouble['groupConfirmChanges']}')) {ajax('../app/UiV2Subject.thisSubjectsPrivilegesAssignAttributeDefPrivilege?assign=true&subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}&fieldName=${fieldName}&parentAttributeDefId=${guiMembershipSubjectContainer.guiAttributeDef.attributeDef.id}', {formIds: 'groupFilterPrivilegesFormId,groupPagingPrivilegesFormId,groupPagingPrivilegesFormPageNumberId'});} return false;"></button></c:if>
                                <button type="button" title="${grouper:escapeHtml(ariaAssignPrivilege)}" aria-label="${grouper:escapeHtml(ariaAssignPrivilege)}" class="btn btn-inverse btn-super-mini remove" 
                                   onclick="if (confirmChange('${textContainer.textEscapeSingleDouble['groupConfirmChanges']}')) {ajax('../app/UiV2Subject.thisSubjectsPrivilegesAssignAttributeDefPrivilege?assign=true&subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}&fieldName=${fieldName}&parentAttributeDefId=${guiMembershipSubjectContainer.guiAttributeDef.attributeDef.id}', {formIds: 'groupFilterPrivilegesFormId,groupPagingPrivilegesFormId,groupPagingPrivilegesFormPageNumberId'});} return false;"
                                  ><i class="fa fa-plus" aria-hidden="true"></i></button>
                              </c:otherwise>
                            </c:choose>
                          </td>
                        </c:forEach>
                        <td>
                          <div class="btn-group">
                          	<button type="button" data-toggle="dropdown" aria-label="${textContainer.text['ariaLabelGuiMoreOptions']}" class="btn btn-mini dropdown-toggle"
                          		aria-haspopup="true" aria-expanded="false" onclick="$('#subject-attribute-more-options${i}').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#subject-attribute-more-options${i} li').first().focus();return true;});">
                          		${textContainer.text['thisSubjectsPrivilegesActionsButton']} 
                          		<span class="caret"></span>
                          	</button>
                            <ul class="dropdown-menu dropdown-menu-right" id="subject-attribute-more-options${i}">
                              <c:if test="${guiMembershipContainer.membershipContainer.membershipAssignType.nonImmediate}">
                                <li><a href="?operation=UiV2Membership.traceAttributeDefPrivileges&attributeDefId=${guiMembershipSubjectContainer.guiAttributeDef.attributeDef.id}&memberId=${guiMembershipSubjectContainer.guiMember.member.uuid}&backTo=subject"  onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Membership.traceAttributeDefPrivileges&attributeDefId=${guiMembershipSubjectContainer.guiAttributeDef.attributeDef.id}&memberId=${guiMembershipSubjectContainer.guiMember.member.uuid}&backTo=subject'); return false;" class="actions-revoke-membership">${textContainer.text['thisSubjectsPrivilegesActionsMenuTracePrivileges'] }</a></li>
                              </c:if>

                              <li><a href="?operation=UiV2AttributeDef.viewAttributeDef&attributeDefId=${guiMembershipSubjectContainer.guiAttributeDef.attributeDef.id}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2AttributeDef.viewAttributeDef&attributeDefId=${guiMembershipSubjectContainer.guiAttributeDef.attributeDef.id}');">${textContainer.text['thisSubjectsPrivilegesActionsMenuViewAttributeDef']}</a></li>
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
                <grouper:paging2 guiPaging="${grouperRequestContainer.subjectContainer.privilegeGuiPaging}" formName="groupPagingPrivilegesForm" ajaxFormIds="groupFilterPrivilegesFormId"
                  refreshOperation="../app/UiV2Subject.filterThisSubjectsAttributeDefPrivileges?subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}" />
              </div>
