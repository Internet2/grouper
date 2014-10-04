<%@ include file="../assetsJsp/commonTaglib.jsp"%>
                    <!-- start indexStemsImanage.jsp -->
                    <h4>${textContainer.text['indexStemsImanageStemsImanage'] }</h4>


                    <c:choose>
                      <c:when test="${grouperRequestContainer.indexContainer.stemsImanageRetrieved}">
                        <ul class="unstyled list-widget">
                          <c:forEach items="${grouperRequestContainer.indexContainer.guiStemsUserManagesAbbreviated}" var="guiStem">
                            <li>
                            ${guiStem.shortLinkWithIconAndPath }
                            </li>
                          </c:forEach>
                        </ul>
                      </c:when>
                      <c:otherwise>
                        <a href="#" onclick="ajax('UiV2Main.indexColStemsImanage?col=${col}&storePref=false'); return false;">${textContainer.text['indexStemsImanageStemsImanageLoad'] }</a></li>
                        <br /><br /><br /><br/>
                      </c:otherwise>
                    </c:choose>


                    
                    <p><strong><a href="#" 
                  onclick="return guiV2link('operation=UiV2MyStems.myStems');">${textContainer.text['indexMyStemsViewAllStems'] }</a>  </strong></p>
                    <!-- end indexStemsImanage.jsp -->
