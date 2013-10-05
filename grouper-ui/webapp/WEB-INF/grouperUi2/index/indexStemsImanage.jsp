<%@ include file="../assetsJsp/commonTaglib.jsp"%>
                    <!-- start indexStemsImanage.jsp -->
                    <h4>My folders</h4>
                    <ul class="unstyled list-widget">
                      <c:forEach items="${grouperRequestContainer.indexContainer.guiStemsUserManagesAbbreviated}" var="guiStem">
                        <li>
                        ${guiStem.shortLinkWithIconAndPath }
                        </li>
                      </c:forEach>
                    </ul>
                    <p><strong><a href="my-stems.html">View all folders</a>  </strong></p>
                    <!-- end indexStemsImanage.jsp -->
