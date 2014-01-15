<%@ include file="../assetsJsp/commonTaglib.jsp"%>

                <table class="table table-hover table-bordered table-striped table-condensed data-table table-bulk-update footable">
                  <thead>
                    <tr>
                      <td colspan="4" class="table-toolbar gradient-background"><a class="btn">Remove selected members</a></td>
                    </tr>
                    <tr>
                      <th>
                        <label class="checkbox checkbox-no-padding">
                          <input type="checkbox">
                        </label>
                      </th>
                      <th class="sorted">Entity Name</th>
                      <th data-hide="phone">Membership</th>
                      <th style="width:100px;"></th>
                    </tr>
                  </thead>
                  <tbody>
                    <c:set var="i" value="0" />
                    <c:forEach items="${grouperRequestContainer.groupContainer.guiMembershipSubjectContainers}" 
                      var="guiMembershipSubjectContainer" >
                      <c:set var="guiMembershipContainer" value="${guiMembershipSubjectContainer.guiMembershipContainers['members']}" />
                      <tr>
                        <td>
                          <label class="checkbox checkbox-no-padding">
                            <c:choose>
                              <c:when test="${guiMembershipContainer.membershipContainer.membershipAssignType.immediate}">
                                <input type="checkbox" name="membershipSubjectRow_${i}[]" value="${guiMembershipSubjectContainer.guiMember.member.uuid}" class="membershipCheckbox" />
                              </c:when>
                              <c:otherwise>
                                <input type="checkbox" disabled="disabled"/>
                              </c:otherwise>
                            </c:choose>
                          </label>
                        </td>
                        <td class="expand foo-clicker">${guiMembershipSubjectContainer.guiSubject.shortLinkWithIcon} <br/>
                        </td>
                        <td data-hide="phone">
                          ${textContainer.text[grouper:concat2('groupMembershipAssignType_',guiMembershipContainer.membershipContainer.membershipAssignType)] }
                        </td>
                        <td>
                          <div class="btn-group"><a data-toggle="dropdown" href="#" class="btn btn-mini dropdown-toggle">${textContainer.text['groupViewActionsButton'] } <span class="caret"></span></a>
                            <ul class="dropdown-menu dropdown-menu-right">
                              <li><a href="edit-person-membership.html">${textContainer.text['groupViewEditMembershipsAndPrivilegesButton'] }</a></li>
                              <c:if test="${guiMembershipContainer.membershipContainer.membershipAssignType.immediate}">
                                <li><a href="#" onclick="ajax('../app/UiV2Group.removeMember?groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}&memberId=${guiMembershipSubjectContainer.guiMember.member.uuid}', {formIds: 'groupFilterFormId,groupPagingFormId'}); return false;" class="actions-revoke-membership">${textContainer.text['groupViewRevokeMembershipButton'] }</a></li>
                              </c:if>
                              <c:if test="${guiMembershipSubjectContainer.guiSubject.group}">
                                <li><a href="#" onclick="return guiV2link('operation=UiV2Group.viewGroup&groupId=${guiMembershipSubjectContainer.guiSubject.subject.id}');">${textContainer.text['groupViewViewGroupButton'] }</a></li>
                              </c:if>
                            </ul>
                          </div>
                        </td>
                      </tr>
                      <c:set var="i" value="${i+1}" />
                    </c:forEach>
                  </tbody>
                </table>
               <grouper:paging2 guiPaging="${grouperRequestContainer.groupContainer.guiPaging}" formName="groupPagingForm" ajaxFormIds="groupFilterFormId"
                  refreshOperation="../app/UiV2Group.filter?groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}" />
                