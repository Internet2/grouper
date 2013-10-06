<%@ include file="../assetsJsp/commonTaglib.jsp"%>
                    <!-- start indexMyServices.jsp -->
                    <h4>My services</h4>
                    <ul class="unstyled list-widget">
                      <c:forEach items="${grouperRequestContainer.indexContainer.guiAttributeDefNamesMyServices}" var="guiAttributeDefName">
                        <li>${guiAttributeDefName.shortLinkWithIcon }
                        </li>
                      </c:forEach>
                    </ul>
                    <p><strong><a href="my-services.html">View all services</a></strong></p>
                    <!-- start indexMyServices.jsp -->
                    