<%@ include file="../assetsJsp/commonTaglib.jsp"%>
                    <!-- start indexGroupsIManage.jsp -->
                    <h4>Groups I Manage</h4>
                    <ul class="unstyled list-widget">
                      <c:forEach items="${grouperRequestContainer.indexContainer.guiGroupsUserManagesAbbreviated}" var="guiGroup">
                        <li>
                        ${guiGroup.shortLinkWithIconAndPath }
                        </li>
                      
                      
                      </c:forEach>
                      
                    </ul>
                    <p><strong><a href="my-groups.html">View all groups</a>  </strong></p>
                    <!-- end indexGroupsIManage.jsp -->
                    