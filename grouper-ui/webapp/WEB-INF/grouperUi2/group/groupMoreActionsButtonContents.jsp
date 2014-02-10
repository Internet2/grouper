<%@ include file="../assetsJsp/commonTaglib.jsp"%>

                    <!-- start group/groupMoreActionsButtonContents.jsp -->

                    <a id="show-add-block" href="#" onclick="$('#add-block-container').toggle('slow'); return false;" class="btn btn-medium btn-primary btn-block"><i class="icon-plus"></i> ${textContainer.text['groupViewMoreActionsAddMembers'] }</a>
                    <div class="btn-group btn-block"><a data-toggle="dropdown" href="#" class="btn btn-medium btn-block dropdown-toggle">More actions <span class="caret"></span></a>
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
                        <li><a href="join-group.html">Join group</a></li>
                        <c:if test="${grouperRequestContainer.groupContainer.canAdmin }">
                          <li class="divider"></li>
                          <li><a href="#" onclick="return guiV2link('operation=UiV2Group.groupCopy&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}'); return false;"
                              >${textContainer.text['groupViewCopyGroupButton'] }</a></li>
                          <li><a href="#" onclick="return guiV2link('operation=UiV2Group.groupDelete&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}'); return false;"
                            >${textContainer.text['groupViewDeleteGroupButton'] }</a></li>
                          <li><a href="#" onclick="return guiV2link('operation=UiV2Group.groupEdit&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}'); return false;"
                            >${textContainer.text['groupViewEditGroupButton'] }</a></li>
                          <li><a href="#" onclick="return guiV2link('operation=UiV2Group.groupMove&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}'); return false;"
                              >${textContainer.text['groupViewMoveGroupButton'] }</a></li>
                        </c:if>
                        

                        
                        
                        
                        <li class="divider"></li>
                        <li><a href="export-group.html">Export members</a></li>
                        <li><a href="bulk-add.html">Import members</a></li>
                        <li><a href="invite-external-users.html">Invite external users</a></li>
                        <li><a href="remove-all-members.html">Remove all members</a></li>
                        <li class="divider"></li>
                        <li><a href="view-audit-log.html">View audit log</a></li>
                      </ul>
                    </div>

                    <!-- end group/groupMoreActionsButtonContents.jsp -->
                    