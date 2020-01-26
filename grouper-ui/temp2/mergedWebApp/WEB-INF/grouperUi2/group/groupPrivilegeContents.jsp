<%@ include file="../assetsJsp/commonTaglib.jsp"%>

              <%-- tell add member to refresh audits --%>
              <form id="groupRefreshPartFormId">
                <input type="hidden" name="groupRefreshPart" value="privileges" /> 
              </form> 

              <form class="form-inline form-small" name="groupPrivilegeFormName" id="groupPrivilegeFormId">
                <table class="table table-hover table-bordered table-striped table-condensed data-table table-bulk-update table-privileges footable">
                  <thead>
                    <tr>
                      <td colspan="11" class="table-toolbar gradient-background">
                        <div class="row-fluid">
                          <div class="span1">
                            <label for="people-update">${textContainer.text['groupPrivilegesUpdateBulkLabel'] }</label>
                          </div>
                          <div class="span4">

                            <select id="people-update" class="span12" name="groupPrivilegeBatchUpdateOperation">
                              <%-- create group should be the default, so list it first --%>

                              <option value="assign_admins">${textContainer.text['groupPrivilegesAssignAdminPrivilege'] }</option>
                              <option value="assign_updaters">${textContainer.text['groupPrivilegesAssignUpdatePrivilege'] }</option>
                              <option value="assign_readersUpdaters">${textContainer.text['groupPrivilegesAssignReadUpdatePrivilege'] }</option>
                              <option value="assign_readers">${textContainer.text['groupPrivilegesAssignReadPrivilege'] }</option>
                              <option value="assign_viewers">${textContainer.text['groupPrivilegesAssignViewPrivilege'] }</option>
                              <option value="assign_groupAttrReaders">${textContainer.text['groupPrivilegesAssignGroupAttributeReadPrivilege'] }</option>
                              <option value="assign_groupAttrUpdaters">${textContainer.text['groupPrivilegesAssignGroupAttributeUpdatePrivilege'] }</option>
                              <option value="assign_optins">${textContainer.text['groupPrivilegesAssignOptinPrivilege'] }</option>
                              <option value="assign_optouts">${textContainer.text['groupPrivilegesAssignOptoutPrivilege'] }</option>
                              <option value="revoke_admins">${textContainer.text['groupPrivilegesRevokeAdminPrivilege'] }</option>
                              <option value="revoke_updaters">${textContainer.text['groupPrivilegesRevokeUpdatePrivilege'] }</option>
                              <option value="revoke_readersUpdaters">${textContainer.text['groupPrivilegesRevokeReadUpdatePrivilege'] }</option>
                              <option value="revoke_readers">${textContainer.text['groupPrivilegesRevokeReadPrivilege'] }</option>
                              <option value="revoke_viewers">${textContainer.text['groupPrivilegesRevokeViewPrivilege'] }</option>
                              <option value="revoke_groupAttrReaders">${textContainer.text['groupPrivilegesRevokeGroupAttributeReadPrivilege'] }</option>
                              <option value="revoke_groupAttrUpdaters">${textContainer.text['groupPrivilegesRevokeGroupAttributeUpdatePrivilege'] }</option>
                              <option value="revoke_optins">${textContainer.text['groupPrivilegesRevokeOptinPrivilege'] }</option>
                              <option value="revoke_optouts">${textContainer.text['groupPrivilegesRevokeOptoutPrivilege'] }</option>
                              <option value="revoke_all">${textContainer.text['groupPrivilegesRevokeAllPrivilege'] }</option>

                            </select>
                          </div>
                          <div class="span4">
                            <button type="submit" class="btn" 
                              onclick="ajax('../app/UiV2Group.assignPrivilegeBatch?groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}', {formIds: 'groupFilterPrivilegesFormId,groupPagingPrivilegesFormId,groupPagingPrivilegesFormPageNumberId,groupPrivilegeFormId'}); return false;">${textContainer.text['groupPrivilegesUpdateSelected'] }</button>
                          </div>
                        </div>
                      </td>
                    </tr>
                    <tr>
                      <th>
                        <label class="checkbox checkbox-no-padding">
                          <input type="checkbox" name="notImportantXyzName" id="notImportantXyzId" onchange="$('.privilegeCheckbox').prop('checked', $('#notImportantXyzId').prop('checked'));" />
                        </label>
                      </th>
                      <th class="sorted" style="width: 35em">Entity name</th>
                      <th data-hide="phone" style="white-space: nowrap; text-align: center; width: 10em;">${textContainer.text['priv.colAdmin'] }</th>
                      <th data-hide="phone" style="white-space: nowrap; text-align: center; width: 10em;">${textContainer.text['priv.colRead'] }</th>
                      <th data-hide="phone" style="white-space: nowrap; text-align: center; width: 10em;">${textContainer.text['priv.colUpdate'] }</th>
                      <th data-hide="phone" style="white-space: nowrap; text-align: center; width: 10em;">${textContainer.text['priv.colOptin'] }</th>
                      <th data-hide="phone" style="white-space: nowrap; text-align: center; width: 10em;">${textContainer.text['priv.colOptout'] }</th>
                      <th data-hide="phone" style="white-space: nowrap; text-align: center; width: 10em;">${textContainer.text['priv.colAttributeRead'] }</th>
                      <th data-hide="phone" style="white-space: nowrap; text-align: center; width: 10em;">${textContainer.text['priv.colAttributeUpdate'] }</th>
                      <th data-hide="phone" style="white-space: nowrap; text-align: center; width: 10em;">${textContainer.text['priv.colView'] }</th>
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
                            <input type="checkbox" name="privilegeSubjectRow_${i}[]" aria-label="${textContainer.text['groupViewDetailsPrivilegesCheckboxAriaLabel']}"
                            value="${guiMembershipSubjectContainer.guiMember.member.uuid}" class="privilegeCheckbox" />
                          </label>
                        </td>
                        <td class="expand foo-clicker" style="white-space: nowrap">${guiMembershipSubjectContainer.guiSubject.shortLinkWithIcon}
                        </td>
                        <%-- loop through the fields for groups --%>
                        <c:forEach items="admins,readers,updaters,optins,optouts,groupAttrReaders,groupAttrUpdaters,viewers" var="fieldName">
                          <td data-hide="phone,medium" class="direct-actions privilege" >
                            <c:set value="${guiMembershipSubjectContainer.guiMembershipContainers[fieldName]}" var="guiMembershipContainer" />
                            <%-- if there is a container, then there is an assignment of some sort... --%>
                            <c:choose>
                              <c:when test="${guiMembershipContainer != null 
                                   && guiMembershipContainer.membershipContainer.membershipAssignType.immediate}">
                                <i class="fa fa-check fa-direct" tabindex="0" aria-label="${textContainer.text['groupPrivilegesTitleRemoveThisPrivilege'] }" onkeydown="if (event.keyCode == 13) {if (confirmChange('${textContainer.textEscapeSingleDouble['groupConfirmChanges']}')) {ajax('../app/UiV2Group.assignPrivilege?assign=false&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}&fieldName=${fieldName}&memberId=${guiMembershipSubjectContainer.guiMember.member.uuid}', {formIds: 'groupFilterPrivilegesFormId,groupPagingPrivilegesFormId,groupPagingPrivilegesFormPageNumberId'});} return false;}"></i>
                                <a title="${textContainer.text['groupPrivilegesTitleRemoveThisPrivilege'] }" class="btn btn-inverse btn-super-mini remove" href="#" 
                                   onclick="if (confirmChange('${textContainer.textEscapeSingleDouble['groupConfirmChanges']}')) {ajax('../app/UiV2Group.assignPrivilege?assign=false&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}&fieldName=${fieldName}&memberId=${guiMembershipSubjectContainer.guiMember.member.uuid}', {formIds: 'groupFilterPrivilegesFormId,groupPagingPrivilegesFormId,groupPagingPrivilegesFormPageNumberId'});} return false;"
                                  ><i class="fa fa-times"></i></a>
                              </c:when>
                              <c:otherwise>
                                <c:if test="${guiMembershipContainer != null}"><i class="fa fa-check fa-disabled" aria-label="${textContainer.text['attributeDefPrivilegesTitleAssignThisPrivilege'] }" tabindex="0" onkeydown="if (event.keyCode == 13) {if (confirmChange('${textContainer.textEscapeSingleDouble['groupConfirmChanges']}')) {ajax('../app/UiV2Group.assignPrivilege?assign=true&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}&fieldName=${fieldName}&memberId=${guiMembershipSubjectContainer.guiMember.member.uuid}', {formIds: 'groupFilterPrivilegesFormId,groupPagingPrivilegesFormId,groupPagingPrivilegesFormPageNumberId'});} return false;}"></i></c:if>
                                <a title="${textContainer.text['attributeDefPrivilegesTitleAssignThisPrivilege'] }" class="btn btn-inverse btn-super-mini remove" href="#" 
                                   onclick="if (confirmChange('${textContainer.textEscapeSingleDouble['groupConfirmChanges']}')) {ajax('../app/UiV2Group.assignPrivilege?assign=true&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}&fieldName=${fieldName}&memberId=${guiMembershipSubjectContainer.guiMember.member.uuid}', {formIds: 'groupFilterPrivilegesFormId,groupPagingPrivilegesFormId,groupPagingPrivilegesFormPageNumberId'});} return false;"
                                  ><i class="fa fa-plus"></i></a>
                              </c:otherwise>
                            </c:choose>
                          </td>
                        </c:forEach>
                        <td>
                          <div class="btn-group"><a data-toggle="dropdown" href="#" aria-label="${textContainer.text['ariaLabelGuiMoreOptions']}" class="btn btn-mini dropdown-toggle" 
                          	aria-haspopup="true" aria-expanded="false" role="menu" onclick="$('#membership-more-options${i}').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#membership-more-options${i} li').first().focus();return true;});">
                          	${textContainer.text['groupPrivilegesActions']} 
                          		<span class="caret"></span>
                          	</a>
                            <ul class="dropdown-menu dropdown-menu-right" id="membership-more-options${i}">
                                                        
                              <li><a href="#" onclick="return guiV2link('operation=UiV2Membership.editMembership&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}&memberId=${guiMembershipSubjectContainer.guiMember.member.uuid}&field=members');" class="actions-revoke-membership">${textContainer.text['groupViewEditMembershipsAndPrivilegesButton'] }</a></li>
                            
                              <c:if test="${guiMembershipSubjectContainer.membershipSubjectContainer.hasNonImmediate}">
                                <li><a href="#"  onclick="return guiV2link('operation=UiV2Membership.traceGroupPrivileges&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}&memberId=${guiMembershipSubjectContainer.guiMember.member.uuid}'); return false;" class="actions-revoke-membership">${textContainer.text['groupViewTracePrivilegeButton'] }</a></li>
                              </c:if>
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
                    refreshOperation="../app/UiV2Group.filterPrivileges?groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}" />
              </div>     
 
 
