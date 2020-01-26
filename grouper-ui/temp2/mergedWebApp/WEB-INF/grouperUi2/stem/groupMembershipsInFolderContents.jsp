<%@ include file="../assetsJsp/commonTaglib.jsp"%>

                <form id="membersToDeleteFormId">
                  <table class="table table-hover table-bordered table-striped table-condensed data-table table-bulk-update footable">
                    <thead>
                      <tr>
                        <td colspan="${grouperRequestContainer.stemContainer.showEnabledStatus ? 9 : 6}" class="table-toolbar gradient-background">
                          <c:if test="${!grouperRequestContainer.stemContainer.showPointInTimeAudit}">
                            <a href="#" onclick="ajax('../app/UiV2Stem.removeGroupMembers?stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}', {formIds: 'groupFilterFormId,groupPagingFormId,membersToDeleteFormId'}); return false;" class="btn" role="button">${textContainer.text['groupRemoveSelectedMembersButton'] }</a>
                          </c:if>
                        </td>
                      </tr>
                      <tr>
                        <th>
                          <c:if test="${!grouperRequestContainer.stemContainer.showPointInTimeAudit}">
                            <label class="checkbox checkbox-no-padding">
                              <input type="checkbox" name="notImportantXyzName" id="notImportantXyzId" onchange="$('.membershipCheckbox').prop('checked', $('#notImportantXyzId').prop('checked'));" />
                            </label>
                          </c:if>
                        </th>
                        <th class="sorted">${textContainer.text['groupViewDetailsHeaderEntityName']}</th>
                        <th>${textContainer.text['groupViewDetailsHeaderFolderName']}</th>
                        <th>${textContainer.text['groupViewDetailsHeaderGroupName']}</th>
                        <c:if test="${grouperRequestContainer.stemContainer.showEnabledStatus}">
                          <th data-hide="phone">${textContainer.text['groupViewDetailsHeaderStatus']}</th>
                          <th data-hide="phone">${textContainer.text['groupViewDetailsHeaderEnabledDate']}</th>
                          <th data-hide="phone">${textContainer.text['groupViewDetailsHeaderDisabledDate']}</th>
                        </c:if>
                        <c:if test="${grouperRequestContainer.stemContainer.showPointInTimeAudit}">
                          <th data-hide="phone">${textContainer.text['groupViewDetailsHeaderPITStartDate']}</th>
                          <th data-hide="phone">${textContainer.text['groupViewDetailsHeaderPITEndDate']}</th>
                        </c:if>
                        <c:if test="${!grouperRequestContainer.stemContainer.showPointInTimeAudit}">
                          <th data-hide="phone">${textContainer.text['groupViewDetailsHeaderMembership']}</th>
                          <th style="width:100px;">${textContainer.text['headerChooseAction']}</th>
                        </c:if>
                      </tr>
                    </thead>
                    <tbody>
                      <c:set var="i" value="0" />
                      <c:if test="${grouperRequestContainer.stemContainer.showPointInTimeAudit}">
                        <c:forEach items="${grouperRequestContainer.stemContainer.guiPITMembershipViews}" 
                          var="guiPITMembershipView" >
                          <tr>
                            <!-- ${guiPITMembershipView.getPITMembershipView().getId()} -->
                            <td>&nbsp;</td>
                            <td class="expand foo-clicker">${guiPITMembershipView.guiSubject.shortLinkWithIcon} <br/>
                            </td>
                            <td class="expand foo-clicker">${guiPITMembershipView.ownerGuiGroup.parentGuiStem.shortLinkWithIcon} <br/>
                            </td>
                            <td class="expand foo-clicker">${guiPITMembershipView.ownerGuiGroup.shortLinkWithIcon} <br/>
                            </td>
                            <td>${guiPITMembershipView.getStartTimeLabel()}</td>
                            <td>${guiPITMembershipView.getEndTimeLabel()}</td>
                          </tr>
                          <c:set var="i" value="${i+1}" />
                        </c:forEach>
                      </c:if>
                      <c:if test="${!grouperRequestContainer.stemContainer.showPointInTimeAudit}">
                        <c:forEach items="${grouperRequestContainer.stemContainer.guiMembershipSubjectContainers}" 
                          var="guiMembershipSubjectContainer" >
                          <c:set var="guiMembershipContainer" value="${guiMembershipSubjectContainer.guiMembershipContainers['members']}" />
                          <tr>
                            <td>
                              <label class="checkbox checkbox-no-padding">
                                <c:choose>
                                  <c:when test="${guiMembershipSubjectContainer.guiGroup.canUpdate && guiMembershipContainer.membershipContainer.membershipAssignType.immediate}">
                                    <input type="checkbox" aria-label="${textContainer.text['groupViewDetailsMembershipCheckboxAriaLabel']}" name="membershipRow_${i}" value="${guiMembershipContainer.membershipContainer.immediateMembership.uuid}" class="membershipCheckbox" />
                                  </c:when>
                                  <c:otherwise>
                                    <input type="checkbox" disabled="disabled"/>
                                  </c:otherwise>
                                </c:choose>
                              </label>
                            </td>
                            <td class="expand foo-clicker">${guiMembershipSubjectContainer.guiSubject.shortLinkWithIcon} <br/>
                            </td>
                            <td class="expand foo-clicker">${guiMembershipSubjectContainer.guiGroup.parentGuiStem.shortLinkWithIcon} <br/>
                            </td>
                            <td class="expand foo-clicker">${guiMembershipSubjectContainer.guiGroup.shortLinkWithIcon} <br/>
                            </td>
                            <c:if test="${grouperRequestContainer.stemContainer.showEnabledStatus}">
                              <td>${guiMembershipContainer.getImmediateMembershipEnabledLabel()}</td>
                              <td>${guiMembershipContainer.getImmediateMembershipStartDateLabel()}</td>
                              <td>${guiMembershipContainer.getImmediateMembershipEndDateLabel()}</td>
                            </c:if>
                            <td data-hide="phone">
                              ${textContainer.text[grouper:concat2('groupMembershipAssignType_',guiMembershipContainer.membershipContainer.membershipAssignType)] }
                            </td>
                            <td>
                              <c:if test="${guiMembershipSubjectContainer.guiGroup.canRead
                                  || (guiMembershipContainer.membershipContainer.membershipAssignType.immediate && guiMembershipSubjectContainer.guiGroup.canUpdate)
                                  || guiMembershipContainer.membershipContainer.membershipAssignType.nonImmediate
                                  || guiMembershipSubjectContainer.guiSubject.group}">
                                <div class="btn-group"><a data-toggle="dropdown" aria-label="${textContainer.text['ariaLabelGuiMoreMembershipActions']}" href="#" class="btn btn-mini dropdown-toggle"
                                	aria-haspopup="true" aria-expanded="false" role="menu" onclick="$('#more-options${i}').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#more-options${i} li').first().focus();return true;});">
                                		${textContainer.text['groupViewActionsButton'] } 
                                		<span class="caret"></span>
                                	</a>
                                  <ul class="dropdown-menu dropdown-menu-right" id="more-options${i}">
                                    <c:if test="${guiMembershipSubjectContainer.guiGroup.canRead}">
                                      <li><a href="#" onclick="return guiV2link('operation=UiV2Membership.editMembership&groupId=${guiMembershipSubjectContainer.guiGroup.group.id}&memberId=${guiMembershipSubjectContainer.guiMember.member.uuid}&field=members');" class="actions-revoke-membership">${textContainer.text['groupViewEditMembershipsAndPrivilegesButton'] }</a></li>
                                    </c:if>
                                    <c:if test="${guiMembershipContainer.membershipContainer.membershipAssignType.immediate && guiMembershipSubjectContainer.guiGroup.canUpdate}">
                                      <li><a href="#" onclick="ajax('../app/UiV2Stem.removeGroupMember?stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}&groupId=${guiMembershipSubjectContainer.guiGroup.group.id}&memberId=${guiMembershipSubjectContainer.guiMember.member.uuid}', {formIds: 'groupFilterFormId,groupPagingFormId'}); return false;" class="actions-revoke-membership">${textContainer.text['groupViewRevokeMembershipButton'] }</a></li>
                                    </c:if>
                                    <c:if test="${guiMembershipContainer.membershipContainer.membershipAssignType.nonImmediate}">
                                      <li><a href="#"  onclick="return guiV2link('operation=UiV2Membership.traceMembership&groupId=${guiMembershipSubjectContainer.guiGroup.group.id}&memberId=${guiMembershipSubjectContainer.guiMember.member.uuid}&field=members');" class="actions-revoke-membership">${textContainer.text['groupViewTraceMembershipButton'] }</a></li>
                                    </c:if>
                                    <c:if test="${guiMembershipSubjectContainer.guiSubject.group}">
                                      <li><a href="#" onclick="return guiV2link('operation=UiV2Group.viewGroup&groupId=${guiMembershipSubjectContainer.guiSubject.subject.id}');">${textContainer.text['groupViewViewGroupButton'] }</a></li>
                                    </c:if>
                                  </ul>
                                </div>
                              </c:if>
                            </td>
                          </tr>
                          <c:set var="i" value="${i+1}" />
                        </c:forEach>
                      </c:if>
                    </tbody>
                  </table>
                </form>
                <div class="data-table-bottom gradient-background">
                  <grouper:paging2 guiPaging="${grouperRequestContainer.stemContainer.guiPaging}" formName="groupPagingForm" ajaxFormIds="groupFilterFormId"
                    refreshOperation="../app/UiV2Stem.groupMembershipsInFolderFilter?stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}" />
                </div>
                
