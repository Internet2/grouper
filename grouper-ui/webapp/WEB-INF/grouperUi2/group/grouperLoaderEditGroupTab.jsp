<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <%-- for the new group or new stem button --%>
            <input type="hidden" name="objectStemId" value="${grouperRequestContainer.groupContainer.guiGroup.group.parentUuid}" />

            <%-- show the add member button for privileges --%>
            <c:set target="${grouperRequestContainer.groupContainer}" property="showAddMember" value="false" />
            <%@ include file="groupHeader.jsp" %>

            <div class="row-fluid">
              <div class="span12">
                <ul class="nav nav-tabs">
                  <li><a role="tab" href="#" onclick="return guiV2link('operation=UiV2Group.viewGroup&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}', {dontScrollTop: true});" >${textContainer.text['groupMembersTab'] }</a></li>
                  <c:if test="${grouperRequestContainer.groupContainer.canAdmin}">
                    <li><a role="tab" href="#" onclick="return guiV2link('operation=UiV2Group.groupPrivileges&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}', {dontScrollTop: true});" >${textContainer.text['groupPrivilegesTab'] }</a></li>
                  </c:if>
                  <%@ include file="groupMoreTab.jsp" %>
                </ul>
                <p class="lead">${textContainer.text['grouperLoaderGroupDecription'] }</p>
                <c:choose>
                  <c:when test="${grouperRequestContainer.grouperLoaderContainer.loaderGroup}">
                    <p>${textContainer.text['grouperLoaderIsGrouperLoader'] }</p>
                  </c:when>
                  <c:otherwise>
<%-- style="margin-top: -1em;" --%>
                    <p>${textContainer.text['grouperLoaderIsNotGrouperLoader'] }</p>
                  </c:otherwise>
                </c:choose>
                
                Edit

              </div>
            </div>
