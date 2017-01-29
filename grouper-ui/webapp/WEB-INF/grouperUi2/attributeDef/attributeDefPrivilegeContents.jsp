<%@ include file="../assetsJsp/commonTaglib.jsp"%>

              <%-- tell add member to refresh audits --%>
              <form id="attributeDefRefreshPartFormId">
                <input type="hidden" name="attributeDefRefreshPart" value="privileges" /> 
              </form> 

              <form class="form-inline form-small" name="attributeDefPrivilegeFormName" id="attributeDefPrivilegeFormId">
                <table class="table table-hover table-bordered table-striped table-condensed data-table table-bulk-update table-privileges footable">
                  <thead>
                    <tr>
                      <td colspan="11" class="table-toolbar gradient-background">
                        <div class="row-fluid">
                          <div class="span1">
                            <label for="people-update">${textContainer.text['attributeDefPrivilegesUpdateBulkLabel'] }</label>
                          </div>
                          <div class="span4">

                            <select id="people-update" class="span12" name="attributeDefPrivilegeBatchUpdateOperation">

                              <option value="assign_attrAdmins">${textContainer.text['attributeDefPrivilegesAssignAttrAdminPrivilege'] }</option>
                              <option value="assign_attrUpdaters">${textContainer.text['attributeDefPrivilegesAssignAttrUpdatePrivilege'] }</option>
                              <option value="assign_attrReadersAttrUpdaters">${textContainer.text['attributeDefPrivilegesAssignAttrReadUpdatePrivilege'] }</option>
                              <option value="assign_attrReaders">${textContainer.text['attributeDefPrivilegesAssignAttrReadPrivilege'] }</option>
                              <option value="assign_attrViewers">${textContainer.text['attributeDefPrivilegesAssignAttrViewPrivilege'] }</option>
                              <option value="assign_attributeDefAttrReaders">${textContainer.text['attributeDefPrivilegesAssignAttrDefAttributeReadPrivilege'] }</option>
                              <option value="assign_attributeDefAttrUpdaters">${textContainer.text['attributeDefPrivilegesAssignAttrDefAttributeUpdatePrivilege'] }</option>
                              <option value="assign_attrOptins">${textContainer.text['attributeDefPrivilegesAssignAttrOptinPrivilege'] }</option>
                              <option value="assign_attrOptouts">${textContainer.text['attributeDefPrivilegesAssignAttrOptoutPrivilege'] }</option>
                              <option value="revoke_attrAdmins">${textContainer.text['attributeDefPrivilegesRevokeAttrAdminPrivilege'] }</option>
                              <option value="revoke_attrUpdaters">${textContainer.text['attributeDefPrivilegesRevokeAttrUpdatePrivilege'] }</option>
                              <option value="revoke_attrReadersAttrUpdaters">${textContainer.text['attributeDefPrivilegesRevokeAttrReadUpdatePrivilege'] }</option>
                              <option value="revoke_attrReaders">${textContainer.text['attributeDefPrivilegesRevokeAttrReadPrivilege'] }</option>
                              <option value="revoke_attrViewers">${textContainer.text['attributeDefPrivilegesRevokeAttrViewPrivilege'] }</option>
                              <option value="revoke_attributeDefAttrReaders">${textContainer.text['attributeDefPrivilegesRevokeAttrDefAttributeReadPrivilege'] }</option>
                              <option value="revoke_attributeDefAttrUpdaters">${textContainer.text['attributeDefPrivilegesRevokeAttrDefAttributeUpdatePrivilege'] }</option>
                              <option value="revoke_attrOptins">${textContainer.text['attributeDefPrivilegesRevokeAttrOptinPrivilege'] }</option>
                              <option value="revoke_attrOptouts">${textContainer.text['attributeDefPrivilegesRevokeAttrOptoutPrivilege'] }</option>
                              <option value="revoke_all">${textContainer.text['attributeDefPrivilegesRevokeAttrAllPrivilege'] }</option>

                            </select>
                          </div>
                          <div class="span4">
                            <button type="submit" class="btn" 
                              onclick="ajax('../app/UiV2AttributeDef.assignPrivilegeBatch?attributeDefId=${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.id}', {formIds: 'attributeDefFilterPrivilegesFormId,attributeDefPagingPrivilegesFormId,attributeDefPagingPrivilegesFormPageNumberId,attributeDefPrivilegeFormId'}); return false;">${textContainer.text['attributeDefPrivilegesUpdateSelected'] }</button>
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
                      <th data-hide="phone" style="white-space: nowrap; text-align: center; width: 10em;">${textContainer.text['priv.colAttrAdmin'] }</th>
                      <th data-hide="phone" style="white-space: nowrap; text-align: center; width: 10em;">${textContainer.text['priv.colAttrRead'] }</th>
                      <th data-hide="phone" style="white-space: nowrap; text-align: center; width: 10em;">${textContainer.text['priv.colAttrUpdate'] }</th>
                      <th data-hide="phone" style="white-space: nowrap; text-align: center; width: 10em;">${textContainer.text['priv.colAttrOptin'] }</th>
                      <th data-hide="phone" style="white-space: nowrap; text-align: center; width: 10em;">${textContainer.text['priv.colAttrOptout'] }</th>
                      <th data-hide="phone" style="white-space: nowrap; text-align: center; width: 10em;">${textContainer.text['priv.colAttrDefAttributeRead'] }</th>
                      <th data-hide="phone" style="white-space: nowrap; text-align: center; width: 10em;">${textContainer.text['priv.colAttrDefAttributeUpdate'] }</th>
                      <th data-hide="phone" style="white-space: nowrap; text-align: center; width: 10em;">${textContainer.text['priv.colAttrView'] }</th>
                      <th style="width:100px;"></th>
                    </tr>
                  </thead>
                  <tbody>
                    <c:set var="i" value="0" />
                    <c:forEach  items="${grouperRequestContainer.attributeDefContainer.privilegeGuiMembershipSubjectContainers}" 
                        var="guiMembershipSubjectContainer" >
                      <tr>
                        <td>
                          <label class="checkbox checkbox-no-padding">
                            <input type="checkbox" name="privilegeSubjectRow_${i}[]" value="${guiMembershipSubjectContainer.guiMember.member.uuid}" class="privilegeCheckbox" />
                          </label>
                        </td>
                        <td class="expand foo-clicker" style="white-space: nowrap">${guiMembershipSubjectContainer.guiSubject.shortLinkWithIcon}
                        </td>
                        <%-- loop through the fields for attributeDefs --%>
                        <c:forEach items="attrAdmins,attrReaders,attrUpdaters,attrOptins,attrOptouts,attrDefAttrReaders,attrDefAttrUpdaters,attrViewers" var="fieldName">
                          <td data-hide="phone,medium" class="direct-actions privilege" >
                            <c:set value="${guiMembershipSubjectContainer.guiMembershipContainers[fieldName]}" var="guiMembershipContainer" />
                            <%-- if there is a container, then there is an assignment of some sort... --%>
                            <c:choose>
                              <c:when test="${guiMembershipContainer != null 
                                   && guiMembershipContainer.membershipContainer.membershipAssignType.immediate}">
                                <i class="fa fa-check fa-direct" tabindex="0" aria-label="${textContainer.text['attributeDefPrivilegesTitleRemoveThisPrivilege'] }" onkeydown="if (event.keyCode == 13) {if (confirmChange('${textContainer.textEscapeSingleDouble['attributeDefConfirmChanges']}')) {ajax('../app/UiV2AttributeDef.assignPrivilege?assign=false&attributeDefId=${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.id}&fieldName=${fieldName}&memberId=${guiMembershipSubjectContainer.guiMember.member.uuid}', {formIds: 'attributeDefFilterPrivilegesFormId,attributeDefPagingPrivilegesFormId,attributeDefPagingPrivilegesFormPageNumberId'});} return false;}"></i>
                                <a title="${textContainer.text['attributeDefPrivilegesTitleRemoveThisPrivilege'] }" class="btn btn-inverse btn-super-mini remove" href="#" 
                                   onclick="if (confirmChange('${textContainer.textEscapeSingleDouble['attributeDefConfirmChanges']}')) {ajax('../app/UiV2AttributeDef.assignPrivilege?assign=false&attributeDefId=${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.id}&fieldName=${fieldName}&memberId=${guiMembershipSubjectContainer.guiMember.member.uuid}', {formIds: 'attributeDefFilterPrivilegesFormId,attributeDefPagingPrivilegesFormId,attributeDefPagingPrivilegesFormPageNumberId'});} return false;"
                                  ><i class="fa fa-times"></i></a>
                              </c:when>
                              <c:otherwise>
                                <c:if test="${guiMembershipContainer != null}"><i class="fa fa-check fa-disabled" tabindex="0" aria-label="${textContainer.text['attributeDefPrivilegesTitleAssignThisPrivilege'] }" onkeydown="if (event.keyCode == 13) {if (confirmChange('${textContainer.textEscapeSingleDouble['attributeDefConfirmChanges']}')) {ajax('../app/UiV2AttributeDef.assignPrivilege?assign=true&attributeDefId=${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.id}&fieldName=${fieldName}&memberId=${guiMembershipSubjectContainer.guiMember.member.uuid}', {formIds: 'attributeDefFilterPrivilegesFormId,attributeDefPagingPrivilegesFormId,attributeDefPagingPrivilegesFormPageNumberId'});} return false;}"></i></c:if>
                                <a title="${textContainer.text['attributeDefPrivilegesTitleAssignThisPrivilege'] }" class="btn btn-inverse btn-super-mini remove" href="#" 
                                   onclick="if (confirmChange('${textContainer.textEscapeSingleDouble['attributeDefConfirmChanges']}')) {ajax('../app/UiV2AttributeDef.assignPrivilege?assign=true&attributeDefId=${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.id}&fieldName=${fieldName}&memberId=${guiMembershipSubjectContainer.guiMember.member.uuid}', {formIds: 'attributeDefFilterPrivilegesFormId,attributeDefPagingPrivilegesFormId,attributeDefPagingPrivilegesFormPageNumberId'});} return false;"
                                  ><i class="fa fa-plus"></i></a>
                              </c:otherwise>
                            </c:choose>
                          </td>
                        </c:forEach>
                        <td>
                          <div class="btn-group">
                          	<a data-toggle="dropdown" aria-label="${textContainer.text['ariaLabelGuiMoreOptions']}" href="#" class="btn btn-mini dropdown-toggle"
                          		aria-haspopup="true" aria-expanded="false" role="menu" onclick="$('#more-options${i}').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#more-options${i} li').first().focus();return true;});">
                          		${textContainer.text['attributeDefPrivilegesActions'] } 
                          			<span class="caret"></span>
                          	</a>
                            <ul class="dropdown-menu dropdown-menu-right" id="more-options${i}">
                            
                              <li><a href="#" onclick="return guiV2link('operation=UiV2Membership.editMembership&attributeDefId=${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.id}&memberId=${guiMembershipSubjectContainer.guiMember.member.uuid}&field=members');" class="actions-revoke-membership">${textContainer.text['attributeDefViewEditMembershipsAndPrivilegesButton'] }</a></li>
                            
                              <c:if test="${guiMembershipSubjectContainer.membershipSubjectContainer.hasNonImmediate}">
                                <li><a href="#"  onclick="return guiV2link('operation=UiV2Membership.traceAttributeDefPrivileges&groupId=${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.id}&memberId=${guiMembershipSubjectContainer.guiMember.member.uuid}'); return false;" class="actions-revoke-membership">${textContainer.text['attributeDefViewTracePrivilegeButton'] }</a></li>
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
                <grouper:paging2 guiPaging="${grouperRequestContainer.attributeDefContainer.privilegeGuiPaging}" formName="attributeDefPagingPrivilegesForm" ajaxFormIds="attributeDefFilterPrivilegesFormId"
                    refreshOperation="../app/UiV2AttributeDef.filterPrivileges?attributeDefId=${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.id}" />
              </div>     
 
 
