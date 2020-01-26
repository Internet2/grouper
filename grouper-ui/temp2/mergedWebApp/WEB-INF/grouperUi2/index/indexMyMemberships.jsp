<%@ include file="../assetsJsp/commonTaglib.jsp"%>
                    <!-- start indexMyMemberships.jsp -->
                    <h4>${textContainer.text['indexMyMembershipsMyMemberships'] }</h4>
                    
                    <c:choose>
                      <c:when test="${grouperRequestContainer.indexContainer.myMembershipsRetrieved}">
                        <ul class="unstyled list-widget">
                          <c:forEach items="${grouperRequestContainer.indexContainer.guiGroupsMyMembershipsAbbreviated}" var="guiGroup">
                            <li>
                            ${guiGroup.shortLinkWithIconAndPath }
                            </li>
                          
                          </c:forEach>
                          
                        </ul>
                      </c:when>
                      <c:otherwise>
                        <a href="#" onclick="ajax('UiV2Main.indexColMyMemberships?col=${col}&storePref=false'); return false;">${textContainer.text['indexMyMembershipsMyMembershipsLoad'] }</a>
                        <br /><br /><br /><br/>
                      </c:otherwise>
                    </c:choose>
                    
                    <p><strong><a href="#" 
                  onclick="return guiV2link('operation=UiV2MyGroups.myGroupsMemberships');">${textContainer.text['indexMyMembershipsViewAllMyMemberships'] }</a></strong></p>
                    
                    <!-- end indexMyMemberships.jsp -->
                    