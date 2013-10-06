<%@ include file="../assetsJsp/commonTaglib.jsp"%>
                    <!-- start indexMyMemberships.jsp -->
                    <h4>My memberships</h4>
                    <ul class="unstyled list-widget">
                      <c:forEach items="${grouperRequestContainer.indexContainer.guiGroupsMyMembershipsAbbreviated}" var="guiGroup">
                        <li>
                        ${guiGroup.shortLinkWithIconAndPath }
                        </li>
                      
                      </c:forEach>
                      
                    </ul>
                    <p><strong><a href="my-groups.html">View all my memberships</a>  </strong></p>
                    <!-- end indexMyMemberships.jsp -->
                    