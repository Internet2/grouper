<%@ include file="../assetsJsp/commonTaglib.jsp"%>
                    <!-- start indexGroupsIManage.jsp -->
                    <h4>${textContainer.text['indexMyGroupsTitle'] }</h4>
                    <ul class="unstyled list-widget">
                      <c:forEach items="${grouperRequestContainer.indexContainer.guiGroupsUserManagesAbbreviated}" var="guiGroup">
                        <li>
                        ${guiGroup.shortLinkWithIconAndPath }
                        </li>
                      </c:forEach>
                    </ul>
                    <p><strong><a href="#" 
                  onclick="return guiV2link('operation=UiV2MyGroups.myGroups');">${textContainer.text['indexMyGroupsViewAllGroups'] }</a>  </strong></p>
                    <!-- end indexGroupsIManage.jsp -->
                    