<%@ include file="../assetsJsp/commonTaglib.jsp"%>

              <form class="form-inline form-small" name="stemPrivilegeFormName" id="stemPrivilegeFormId">
                <table class="table table-hover table-bordered table-striped table-condensed data-table table-bulk-update table-privileges footable">
                  <thead>
                    <tr>
                      <td colspan="7" class="table-toolbar gradient-background">
                        <div class="row-fluid">
                          <div class="span1">
                            <label for="people-update">Update:</label>
                          </div>
                          <div class="span5">
                            <select id="people-update" class="span12" name="stemPrivilegeBatchUpdateOperation">
                              <%-- create group should be the default, so list it first --%>
                              <option value="assign_creators">Assign the CREATE GROUP privilege</option>
                              <option value="assign_stemmers">Assign the CREATE FOLDER privilege</option>
                              <option value="assign_stemAttrReaders">Assign the ATTRIBUTE READ privilege</option>
                              <option value="assign_stemAttrUpdaters">Assign the ATTRIBUTE UPDATE privilege</option>
                              <option value="assign_all">Assign ALL privileges</option>
                              <option value="revoke_creators">Remove the CREATE GROUP privilege</option>
                              <option value="revoke_stemmers">Remove the CREATE FOLDER privilege</option>
                              <option value="revoke_stemAttrReaders">Remove the ATTRIBUTE READ privilege</option>
                              <option value="revoke_stemAttrUpdaters">Remove the ATTRIBUTE UPDATE privilege</option>
                              <option value="revoke_all">Remove ALL privileges</option>
                            </select>
                          </div>
                          <div class="span4">
                            <button type="submit" class="btn" 
                              onclick="ajax('../app/UiV2Stem.assignPrivilegeBatch?stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}', {formIds: 'stemFilterPrivilegesFormId,stemPagingPrivilegesFormId,stemPagingPrivilegesFormPageNumberId,stemPrivilegeFormId'}); return false;">${textContainer.text['stemPrivilegesUpdateSelected'] }</button>
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
                      <th class="sorted">Name</th>
                      <th data-hide="phone" style="white-space: nowrap; text-align: center;">Create<br />folder</th>
                      <th data-hide="phone" style="white-space: nowrap; text-align: center;">Create<br />group</th>
                      <th data-hide="phone" style="white-space: nowrap; text-align: center;">Attribute<br />read</th>
                      <th data-hide="phone" style="white-space: nowrap; text-align: center;">Attribute<br />update</th>
                      <th></th>
                    </tr>
                  </thead>
                  <tbody>
                    <c:set var="i" value="0" />
                    <c:forEach  items="${grouperRequestContainer.stemContainer.privilegeGuiMembershipSubjectContainers}" 
                      var="guiMembershipSubjectContainer" >

                      <tr>
                        <td>
                          <label class="checkbox checkbox-no-padding">
                            <input type="checkbox" name="privilegeSubjectRow_${i}[]" value="${guiMembershipSubjectContainer.guiMember.member.uuid}" class="privilegeCheckbox" />
                          </label>
                        </td>
                        <td class="expand foo-clicker">${guiMembershipSubjectContainer.guiSubject.shortLinkWithIcon}
                        </td>
                        <%-- loop through the fields for stems --%>
                        <c:forEach items="stemmers,creators,stemAttrReaders,stemAttrUpdaters" var="fieldName">
                          <td data-hide="phone,medium" class="direct-actions privilege" >
                            <c:set value="${guiMembershipSubjectContainer.guiMembershipContainers[fieldName]}" var="guiMembershipContainer" />
                            <%-- if there is a container, then there is an assignment of some sort... --%>
                            <c:choose>
                              <c:when test="${guiMembershipContainer != null && guiMembershipContainer.membershipContainer.membershipAssignType.immediate}">
                                <i class="fa fa-check fa-direct"></i><a title="${textContainer.text['stemPrivilegesTitleRemoveThisPrivilege'] }" class="btn btn-inverse btn-super-mini remove" href="#" 
                                   onclick="if (confirmChange('${textContainer.textEscapeSingleDouble['stemConfirmChanges']}')) {ajax('../app/UiV2Stem.assignPrivilege?assign=false&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}&fieldName=${fieldName}&memberId=${guiMembershipSubjectContainer.guiMember.member.uuid}', {formIds: 'stemFilterPrivilegesFormId,stemPagingPrivilegesFormId,stemPagingPrivilegesFormPageNumberId'});} return false;"
                                  ><i class="fa fa-times"></i></a>
                              </c:when>
                              <c:otherwise>
                                <c:if test="${guiMembershipContainer != null}"><i class="fa fa-check fa-disabled"></i></c:if><a  
                                  title="${textContainer.text['attributeDefPrivilegesTitleAssignThisPrivilege'] }" class="btn btn-inverse btn-super-mini remove" href="#" 
                                   onclick="if (confirmChange('${textContainer.textEscapeSingleDouble['stemConfirmChanges']}')) {ajax('../app/UiV2Stem.assignPrivilege?assign=true&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}&fieldName=${fieldName}&memberId=${guiMembershipSubjectContainer.guiMember.member.uuid}', {formIds: 'stemFilterPrivilegesFormId,stemPagingPrivilegesFormId,stemPagingPrivilegesFormPageNumberId'});} return false;"
                                  ><i class="fa fa-plus"></i></a>
                              </c:otherwise>
                            </c:choose>
                          </td>
                        </c:forEach>
                        <td>
                          <div class="btn-group"><a data-toggle="dropdown" href="#" class="btn btn-mini dropdown-toggle">${textContainer.text['stemPrivilegesActions'] } <span class="caret"></span></a>
                            <ul class="dropdown-menu dropdown-menu-right">
                              <c:if test="${guiMembershipContainer.membershipContainer.membershipAssignType.nonImmediate}">
                                <li><a href="#"  onclick="return guiV2link('operation=UiV2Membership.traceStemPrivileges&stemId=${guiMembershipSubjectContainer.guiStem.stem.id}&memberId=${guiMembershipSubjectContainer.guiMember.member.uuid}'); return false;" class="actions-revoke-membership">${textContainer.text['thisSubjectsPrivilegesActionsMenuTracePrivileges'] }</a></li>
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
                <grouper:paging2 guiPaging="${grouperRequestContainer.stemContainer.privilegeGuiPaging}" formName="stemPagingPrivilegesForm" ajaxFormIds="stemFilterPrivilegesFormId"
                  refreshOperation="../app/UiV2Stem.filterPrivileges?stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}" />
              </div>