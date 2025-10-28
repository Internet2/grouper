<%@ include file="../assetsJsp/commonTaglib.jsp"%>

                <ul class="nav nav-tabs">
                  <%-- if grouperCurrentTab is summary then this one is selected --%>
                  <c:choose>
                    <c:when test="${grouperCurrentTab == 'summary'}">
                      <li class="active"><a role="tab" aria-selected="true" href="#" onclick="return false;" >${textContainer.text['groupSummaryTab'] }</a></li>
                    </c:when>
                    <c:otherwise>
                      <li><a role="tab" href="?operation=UiV2Group.viewGroupSummary&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}" onclick="return guiV2link('operation=UiV2Group.viewGroupSummary&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}', {dontScrollTop: true});">${textContainer.text['groupSummaryTab'] }</a></li>
                    </c:otherwise>
                  </c:choose>

                  <%-- Members tab --%>
                  <c:choose>
                    <c:when test="${grouperCurrentTab == 'members'}">
                      <li class="active"><a role="tab" aria-selected="true" href="#" onclick="return false;" >${textContainer.text['groupMembersTab'] }</a></li>
                    </c:when>
                    <c:otherwise>
                      <li><a role="tab" href="?operation=UiV2Group.viewGroupMembers&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}" onclick="return guiV2link('operation=UiV2Group.viewGroupMembers&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}', {dontScrollTop: true});" >${textContainer.text['groupMembersTab'] }</a></li>
                    </c:otherwise>
                  </c:choose>

                  <%-- Privileges tab (only when canAdmin) --%>
                  <c:if test="${grouperRequestContainer.groupContainer.canAdmin}">
                    <c:choose>
                      <c:when test="${grouperCurrentTab == 'privileges'}">
                        <li class="active"><a role="tab"  aria-selected="true" href="#" onclick="return false;" >${textContainer.text['groupPrivilegesTab'] }</a></li>
                      </c:when>
                      <c:otherwise>
                        <li><a role="tab" href="?operation=UiV2Group.groupPrivileges&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Group.groupPrivileges&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}', {dontScrollTop: true});" >${textContainer.text['groupPrivilegesTab'] }</a></li>
                      </c:otherwise>
                    </c:choose>
                  </c:if>

                  <%@ include file="groupMoreTab.jsp" %>
                </ul>
                