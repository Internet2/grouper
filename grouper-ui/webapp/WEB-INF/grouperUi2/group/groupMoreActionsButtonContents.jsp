<%@ include file="../assetsJsp/commonTaglib.jsp"%>

                    <!-- start group/groupMoreActionsButtonContents.jsp -->

                    <a id="show-add-block" href="#" onclick="$('#add-block-container').toggle('slow'); return false;" class="btn btn-medium btn-primary btn-block"><i class="fa fa-plus"></i> ${textContainer.text['groupViewMoreActionsAddMembers'] }</a>
                    <div class="btn-group btn-block"><a data-toggle="dropdown" href="#" class="btn btn-medium btn-block dropdown-toggle">${textContainer.text['groupViewMoreActionsButton'] } <span class="caret"></span></a>
                      <ul class="dropdown-menu dropdown-menu-right">
                        <%-- add or remove to/from my favorites, this causes a success message --%>
                        <c:choose>
                          <c:when test="${grouperRequestContainer.groupContainer.favorite}">
                            <li><a href="#" 
                            onclick="ajax('../app/UiV2Group.removeFromMyFavorites?groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}'); return false;" 
                            >${textContainer.text['groupViewMoreActionsRemoveFromMyFavorites'] }</a></li>
                          </c:when>
                          <c:otherwise>
                            <li><a href="#" 
                            onclick="ajax('../app/UiV2Group.addToMyFavorites?groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}'); return false;" 
                            >${textContainer.text['groupViewMoreActionsAddToMyFavorites']}</a></li>
                          </c:otherwise>
                        </c:choose>

                        <c:if test="${grouperRequestContainer.groupContainer.directMember && grouperRequestContainer.groupContainer.canOptout }">
                          <li><a href="#" onclick="ajax('../app/UiV2Group.leaveGroup?groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}'); return false;" >${textContainer.text['groupViewLeaveGroupButton']}</a></li>
                        </c:if>
                        <c:if test="${!grouperRequestContainer.groupContainer.directMember && grouperRequestContainer.groupContainer.canOptin }">
                          <li><a href="#" onclick="ajax('../app/UiV2Group.joinGroup?groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}'); return false;">${textContainer.text['groupViewJoinGroupButton']}</a></li>
                        </c:if>

                        <c:if test="${grouperRequestContainer.groupContainer.canUpdate }">
                          <li class="divider"></li>
                        </c:if>
                        <c:if test="${grouperRequestContainer.groupContainer.canAdmin }">
                          <li><a href="#" onclick="return guiV2link('operation=UiV2Group.groupCopy&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}'); return false;"
                              >${textContainer.text['groupViewCopyGroupButton'] }</a></li>
                          <li><a href="#" onclick="return guiV2link('operation=UiV2Group.groupDelete&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}'); return false;"
                            >${textContainer.text['groupViewDeleteGroupButton'] }</a></li>
                          <li><a href="#" onclick="return guiV2link('operation=UiV2Group.groupEdit&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}'); return false;"
                            >${textContainer.text['groupViewEditGroupButton'] }</a></li>
                        </c:if>
                        <c:if test="${grouperRequestContainer.groupContainer.canUpdate }">
                          <li><a href="#" onclick="return guiV2link('operation=UiV2Group.groupEditComposite&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}'); return false;"
                            >${textContainer.text['groupViewEditGroupCompositeButton'] }</a></li>
                        </c:if>
                        <c:if test="${grouperRequestContainer.groupContainer.canAdmin }">
                          <li><a href="#" onclick="return guiV2link('operation=UiV2Group.groupMove&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}'); return false;"
                              >${textContainer.text['groupViewMoveGroupButton'] }</a></li>
                        </c:if>
                        
                        <li class="divider"></li>
                        <c:if test="${grouperRequestContainer.groupContainer.canRead}">
                          <li><a href="#" onclick="return guiV2link('operation=UiV2GroupImport.groupExport&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}'); return false;">${textContainer.text['groupExportMenuButton'] }</a></li>
                        </c:if>
                        <c:if test="${!grouperRequestContainer.groupContainer.guiGroup.hasComposite && grouperRequestContainer.groupContainer.canUpdate}">
                          <li><a href="#" onclick="return guiV2link('operation=UiV2GroupImport.groupImport&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}&backTo=group'); return false;">${textContainer.text['groupImportMenuButton'] }</a></li>
                          <li><a href="invite-external-users.html">Invite external users</a></li>
                          <li><a href="#" onclick="return guiV2link('operation=UiV2Group.groupRemoveAllMembers&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}'); return false;"
                              >${textContainer.text['groupViewRemoveAllMembersButton'] }</a></li>
                        </c:if>
                        <li class="divider"></li>
                        <c:if test="${grouperRequestContainer.groupContainer.canAdmin}">
                          <li><a href="#" onclick="return guiV2link('operation=UiV2Group.viewAudits&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}'); return false;"
                              >${textContainer.text['groupViewAuditButton'] }</a></li>
                        </c:if>
                      </ul>
                    </div>

                    <!-- end group/groupMoreActionsButtonContents.jsp -->
                    