<!-- ./webapp/WEB-INF/grouperUi2/index/indexStemsImanage.jsp -->

<%@ include file="../assetsJsp/commonTaglib.jsp"%>
                    <!-- start indexStemsImanage.jsp -->
                    <%-- HJ 20150319
                    <h4>My folders</h4>
                    --%>
                    <h4>${textContainer.text['myStemsTitle'] }</h4>

                    <ul class="unstyled list-widget">
                      <c:forEach items="${grouperRequestContainer.indexContainer.guiStemsUserManagesAbbreviated}" var="guiStem">
                        <li>
                        ${guiStem.shortLinkWithIconAndPath }
                        </li>
                      </c:forEach>
                    </ul>

                    <p><strong><a href="#"
                  onclick="return guiV2link('operation=UiV2MyStems.myStems');">${textContainer.text['indexMyStemsViewAllStems'] }</a>  </strong></p>
                    <!-- end indexStemsImanage.jsp -->
~