<%@ include file="../assetsJsp/commonTaglib.jsp"%>

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

                              <option value="assign_admins">Assign the ADMIN privilege</option>
                              <option value="assign_updaters">Assign the UPDATE privilege</option>
                              <option value="assign_readersUpdaters">Assign the READ/UPDATE privileges</option>
                              <option value="assign_readers">Assign the READ privilege</option>
                              <option value="assign_viewers">Assign the VIEW privilege</option>
                              <option value="assign_groupAttrReaders">Assign the ATTRIBUTE READ privilege</option>
                              <option value="assign_groupAttrUpdaters">Assign the ATTRIBUTE UPDATE privilege</option>
                              <option value="assign_optins">Assign the OPTIN privilege</option>
                              <option value="assign_optouts">Assign the OPTOUT privilege</option>
                              <option value="assign_all">Assign ALL privileges</option>
                              <option value="revoke_admins">Remove the ADMIN privilege</option>
                              <option value="revoke_updaters">Remove the UPDATE privilege</option>
                              <option value="revoke_readersUpdaters">Remove the READ/UPDATE privileges</option>
                              <option value="revoke_readers">Remove the READ privilege</option>
                              <option value="revoke_viewers">Remove the VIEW privilege</option>
                              <option value="revoke_groupAttrReaders">Remove the ATTRIBUTE READ privilege</option>
                              <option value="revoke_groupAttrUpdaters">Remove the ATTRIBUTE UPDATE privilege</option>
                              <option value="revoke_optins">Remove the OPTIN privilege</option>
                              <option value="revoke_optouts">Remove the OPTOUT privilege</option>
                              <option value="revoke_all">Remove ALL privileges</option>

                            </select>
                          </div>
                          <div class="span4">
                            <button type="submit" class="btn" 
                              onclick="ajax('../app/UiV2Group.assignPrivilegeBatch?groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}', {formIds: 'groupFilterPrivilegesFormId,groupPagingPrivilegesFormId,groupPagingPrivilegesFormPageNumberId,groupPrivilegeFormId'}); return false;">Update selected</button>
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
                      <th data-hide="phone" style="white-space: nowrap; text-align: center; width: 10em;">Admin</th>
                      <th data-hide="phone" style="white-space: nowrap; text-align: center; width: 10em;">Read</th>
                      <th data-hide="phone" style="white-space: nowrap; text-align: center; width: 10em;">Update</th>
                      <th data-hide="phone" style="white-space: nowrap; text-align: center; width: 10em;">OptIn</th>
                      <th data-hide="phone" style="white-space: nowrap; text-align: center; width: 10em;">OptOut</th>
                      <th data-hide="phone" style="white-space: nowrap; text-align: center; width: 10em;">Attribute<br />read</th>
                      <th data-hide="phone" style="white-space: nowrap; text-align: center; width: 10em;">Attribute<br />update</th>
                      <th data-hide="phone" style="white-space: nowrap; text-align: center; width: 10em;">View</th>
                      <th style="width:100px;"></th>
                    </tr>
                  </thead>
                  <tbody>
                    <c:set var="i" value="0" />
                    <c:forEach  items="${grouperRequestContainer.groupContainer.privilegeGuiMembershipSubjectContainers}" 
                        var="guiMembershipSubjectContainer" >
                      <tr>
                        <td>
                          <label class="checkbox checkbox-no-padding">
                            <input type="checkbox" name="privilegeSubjectRow_${i}[]" value="${guiMembershipSubjectContainer.guiMember.member.uuid}" class="privilegeCheckbox" />
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
                              <c:when test="${guiMembershipContainer != null && guiMembershipContainer.membershipContainer.membershipAssignType.immediate}">
                                <i class="icon-ok icon-direct"></i><a title="Remove this privilege" class="btn btn-inverse btn-super-mini remove" href="#" 
                                   onclick="if (confirmChange('${textContainer.textEscapeSingleDouble['groupConfirmChanges']}')) {ajax('../app/UiV2Group.assignPrivilege?assign=false&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}&fieldName=${fieldName}&memberId=${guiMembershipSubjectContainer.guiMember.member.uuid}', {formIds: 'groupFilterPrivilegesFormId,groupPagingPrivilegesFormId,groupPagingPrivilegesFormPageNumberId'});} return false;"
                                  ><i class="icon-remove"></i></a>
                              </c:when>
                              <c:otherwise>
                                <c:if test="${guiMembershipContainer != null}"><i class="icon-ok icon-disabled"></i></c:if><a  
                                  title="Assign this privilege" class="btn btn-inverse btn-super-mini remove" href="#" 
                                   onclick="if (confirmChange('${textContainer.textEscapeSingleDouble['groupConfirmChanges']}')) {ajax('../app/UiV2Group.assignPrivilege?assign=true&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}&fieldName=${fieldName}&memberId=${guiMembershipSubjectContainer.guiMember.member.uuid}', {formIds: 'groupFilterPrivilegesFormId,groupPagingPrivilegesFormId,groupPagingPrivilegesFormPageNumberId'});} return false;"
                                  ><i class="icon-plus"></i></a>
                              </c:otherwise>
                            </c:choose>
                          </td>
                        </c:forEach>
                        <td>
                          <div class="btn-group"><a data-toggle="dropdown" href="#" class="btn btn-mini dropdown-toggle">Actions <span class="caret"></span></a>
                            <ul class="dropdown-menu dropdown-menu-right">
                              <li><a href="edit-person-membership.html">Edit membership &amp; privileges</a></li>
                              <li><a href="trace-privileges.html">Trace privileges</a></li>
                              <c:choose>
                                <c:when test="${guiMembershipSubjectContainer.guiSubject.group}">
                                  <li><a href="#" onclick="return guiV2link('operation=UiV2Group.viewGroup&groupId=${guiMembershipSubjectContainer.guiSubject.subject.id}');">View group</a></li>
                                </c:when>
                                <c:otherwise>
                                  <li><a href="view-profile.html">View Profile</a></li>
                                </c:otherwise>
                              </c:choose>
                              </li>
                            </ul>
                          </div>
                        </td>
                      </tr>
                      <c:set var="i" value="${i+1}" />
                    </c:forEach>
                  </tbody>
                </table>
              </form>
              <grouper:paging2 guiPaging="${grouperRequestContainer.groupContainer.privilegeGuiPaging}" formName="groupPagingPrivilegesForm" ajaxFormIds="groupFilterPrivilegesFormId"
                  refreshOperation="../app/UiV2Group.filterPrivileges?groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}&filterText=${grouper:escapeUrl(grouperRequestContainer.groupContainer.privilegeFilterText)}" />
     
 
 
