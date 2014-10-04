<%@ include file="../assetsJsp/commonTaglib.jsp"%>
                    <!-- start indexRecentlyUsed.jsp -->
                    <h4>${textContainer.text['indexRecentlyUsedRecentlyUsed'] }</h4>
                    
                    <c:choose>
                      <c:when test="${grouperRequestContainer.indexContainer.recentlyUsedRetrieved}">
                        <ul class="unstyled list-widget">
                          <c:forEach items="${grouperRequestContainer.indexContainer.guiGroupsRecentlyUsedAbbreviated}" var="guiGroup">
                            <li>
                            ${guiGroup.shortLinkWithIconAndPath }
                            </li>
                          </c:forEach>
    
                          <c:forEach items="${grouperRequestContainer.indexContainer.guiStemsRecentlyUsedAbbreviated}" var="guiStem">
                            <li>
                            ${guiStem.shortLinkWithIconAndPath }
                            </li>
                          </c:forEach>
    
                          <c:forEach items="${grouperRequestContainer.indexContainer.guiMembersRecentlyUsedAbbreviated}" var="guiMember">
                            <li>
                            ${guiMember.shortLinkWithIcon }
                            </li>
                          </c:forEach>
    
                          <c:forEach items="${grouperRequestContainer.indexContainer.guiAttributeDefNamesRecentlyUsedAbbreviated}" var="guiAttributeDefName">
                            <li>
                            ${guiAttributeDefName.shortLinkWithIcon }
                            </li>
                          </c:forEach>
    
                          <c:forEach items="${grouperRequestContainer.indexContainer.guiAttributeDefsRecentlyUsedAbbreviated}" var="guiAttributeDef">
                            <li>
                            ${guiAttributeDef.shortLinkWithIcon }
                            </li>
                          </c:forEach>
    
                        </ul>
                      </c:when>
                      <c:otherwise>
                        <a href="#" onclick="ajax('UiV2Main.indexColRecentlyUsed?col=${col}&storePref=false'); return false;">${textContainer.text['indexRecentlyUsedRecentlyUsedLoad'] }</a>
                        <br /><br /><br /><br/>
                      </c:otherwise>
                    </c:choose>
                    
                    
<%-- TODO add in view all recently used --%>
                    <!-- start indexRecentlyUsed.jsp -->
                    