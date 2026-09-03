<%@ include file="../assetsJsp/commonTaglib.jsp"%>
                    <!-- start indexMyMemberships.jsp -->
                    <h3 class="grouper-heading-as-h4">${textContainer.text['indexMyMembershipsMyMemberships'] }</h3>
                    
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
                        <button type="button" class="grouper-linkbutton" onclick="ajax('UiV2Main.indexColMyMemberships?col=${col}&storePref=false'); return false;">${textContainer.text['indexMyMembershipsMyMembershipsLoad'] }</button>
                        <br /><br /><br /><br/>
                      </c:otherwise>
                    </c:choose>
                    
                    <p><strong><a href="?operation=UiV2MyGroups.myGroupsMemberships"
                  onclick="return handleGuiV2LinkClick(event, 'operation=UiV2MyGroups.myGroupsMemberships');">${textContainer.text['indexMyMembershipsViewAllMyMemberships'] }</a></strong></p>
                    
                    <!-- end indexMyMemberships.jsp -->
                    