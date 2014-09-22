<%@ include file="../assetsJsp/commonTaglib.jsp"%>
                    <!-- start indexMyServices.jsp -->
                    <h4>${textContainer.text['indexMyServicesSectionTitle'] }</h4>
                    <ul class="unstyled list-widget">
                      <c:forEach items="${grouperRequestContainer.indexContainer.guiMyServices}" var="guiService">
                        <li>${guiService.shortLinkWithIcon }
                        </li>
                      </c:forEach>
                    </ul>
                    <p><strong><a href="#" 
                  onclick="return guiV2link('operation=UiV2Main.myServices');">${textContainer.text['indexMyServicesViewAllServices'] }</a></strong></p>
                    <!-- start indexMyServices.jsp -->
                    