<%@ include file="../assetsJsp/commonTaglib.jsp"%>
                    <!-- start indexMyMemberships.jsp -->
                    <h4>${textContainer.text['indexMyMembershipsMyMemberships'] }</h4>
                    <ul class="unstyled list-widget">
                      <c:forEach items="${grouperRequestContainer.indexContainer.guiGroupsMyMembershipsAbbreviated}" var="guiGroup">
                        <li>
                        ${guiGroup.shortLinkWithIconAndPath }
                        </li>
                      
                      </c:forEach>
                      
                    </ul>
                    <p><strong><a href="#" 
                  onclick="return guiV2link('operation=UiV2MyGroups.myGroupsMemberships');">${textContainer.text['indexMyMembershipsViewAllMyMemberships'] }</a></strong></p>
                    
                    <!-- end indexMyMemberships.jsp -->
                    