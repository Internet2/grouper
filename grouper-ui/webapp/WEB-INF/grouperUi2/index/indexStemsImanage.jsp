<%@ include file="../assetsJsp/commonTaglib.jsp"%>
                    <!-- start indexStemsImanage.jsp -->
                    <h4>${textContainer.text['indexStemsImanageStemsImanage'] }</h4>
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
