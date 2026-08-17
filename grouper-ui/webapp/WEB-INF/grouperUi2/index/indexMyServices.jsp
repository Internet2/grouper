<%@ include file="../assetsJsp/commonTaglib.jsp"%>
                    <!-- start indexMyServices.jsp -->
                    <h4>${textContainer.text['indexMyServicesSectionTitle'] }</h4>

                    <c:choose>
                      <c:when test="${grouperRequestContainer.indexContainer.myServicesRetrieved}">
                        <ul class="unstyled list-widget">
                          <c:forEach items="${grouperRequestContainer.indexContainer.guiMyServices}" var="guiService">
                            <li>${guiService.shortLinkWithIcon }
                            </li>
                          </c:forEach>
                        </ul>
                      </c:when>
                      <c:otherwise>
                        <button type="button" class="btn-link" style="padding:0;border:0;background:none;font-family:inherit;font-size:inherit;line-height:inherit;vertical-align:baseline;" onclick="ajax('UiV2Main.indexColMyServices?col=${col}&storePref=false'); return false;">${textContainer.text['indexMyServicesSectionTitleLoad'] }</button>
                        <br /><br /><br /><br/>
                      </c:otherwise>
                    </c:choose>

                    <p><strong><a href="?operation=UiV2Main.myServices"
                  onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Main.myServices');">${textContainer.text['indexMyServicesViewAllServices'] }</a></strong></p>
                    <!-- start indexMyServices.jsp -->
                    