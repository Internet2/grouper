<%@ include file="../assetsJsp/commonTaglib.jsp"%>

                <table class="table table-hover table-bordered table-striped table-condensed data-table table-paths">
                  <thead>
                    <tr>
                      <th>${textContainer.text['myGroupsFolderHeader'] }</th>
                      <th>${textContainer.text['myGroupsGroupHeader'] }</th>
                      <th></th>
                    </tr>
                  </thead>
                  <tbody>
                    <%-- <tr>
                      <td>Root : Applications : Directories</td>
                      <td><i class="fa fa-group"></i>&nbsp; <a href="view-group.html">Admins</a></td>
                    </tr>  --%>
                    <c:forEach  items="${grouperRequestContainer.myGroupsContainer.guiMembershipSubjectContainers}" 
                        var="guiMembershipSubjectContainer" >

                      <c:set var="guiMembershipContainer" value="${guiMembershipSubjectContainer.guiMembershipContainers['members']}" />

                      <tr>
                        <td>${guiMembershipSubjectContainer.guiGroup.parentGuiStem.linkWithIcon}</td>
                        <td>${guiMembershipSubjectContainer.guiGroup.shortLinkWithIcon}</td>
                        <td>
                          <c:if test="${guiMembershipContainer.membershipContainer.membershipAssignType.immediate && guiMembershipSubjectContainer.membershipSubjectContainer.hasOptout }">
                            <a href="#" onclick="ajax('../app/UiV2MyGroups.leaveGroup?groupId=${guiMembershipSubjectContainer.guiGroup.group.id}'); return false;" class="btn btn-mini" role="button">${textContainer.text['myGroupsMembershipsLeaveButton']}</a>
                          </c:if>
                        </td>
                      </tr>

                    </c:forEach>
                  </tbody>
                </table>
                <div class="data-table-bottom gradient-background">
                  <grouper:paging2 guiPaging="${grouperRequestContainer.myGroupsContainer.myGroupsGuiPaging}" 
                    formName="myGroupsPagingForm" ajaxFormIds="myGroupsForm"
                    refreshOperation="../app/UiV2MyGroups.myGroupsMembershipsSubmit" />
                </div>