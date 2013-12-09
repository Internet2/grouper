<%@ include file="../assetsJsp/commonTaglib.jsp"%>

                    <!-- start group/groupMoreActionsButtonContents.jsp -->

                    <a id="show-add-block" href="#" onclick="$('#add-block-container').toggle('slow'); return false;" class="btn btn-medium btn-primary btn-block"><i class="icon-plus"></i> Add members</a>
                    <div class="btn-group btn-block"><a data-toggle="dropdown" href="#" class="btn btn-medium btn-block dropdown-toggle">More actions <span class="caret"></span></a>
                      <ul class="dropdown-menu dropdown-menu-right">
                        <%-- add or remove to/from my favorites, this causes a success message --%>
                        <c:choose>
                          <c:when test="${grouperRequestContainer.groupContainer.favorite}">
                            <li><a href="#" 
                            onclick="ajax('../app/UiV2Group.removeFromMyFavorites?groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}'); return false;" 
                            >Remove from my favorites</a></li>
                          </c:when>
                          <c:otherwise>
                            <li><a href="#" 
                            onclick="ajax('../app/UiV2Group.addToMyFavorites?groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}'); return false;" 
                            >Add to my favorites</a></li>
                          </c:otherwise>
                        </c:choose>
                        <li><a href="join-group.html">Join group</a></li>
                        <li class="divider"></li>
                        <li><a href="copy-group.html">Copy group</a></li>
                        <li><a href="delete-group.html">Delete group</a></li>
                        <li><a href="edit-group.html">Edit group</a></li>
                        <li><a href="move-group.html">Move group</a></li>
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
                    