<%@ include file="../assetsJsp/commonTaglib.jsp"%>
                    <!-- start indexMyFavorites.jsp -->
                    <h4>${textContainer.text['indexMyFavoritesTitle'] }</h4>
                    
                    <c:choose>
                      <c:when test="${grouperRequestContainer.indexContainer.myFavoritesRetrieved}">
                        <ul class="unstyled list-widget">
                          <c:forEach items="${grouperRequestContainer.indexContainer.guiGroupsMyFavoritesAbbreviated}" var="guiGroup">
                            <li>
                            ${guiGroup.shortLinkWithIconAndPath }
                            </li>
                          </c:forEach>
    
                          <c:forEach items="${grouperRequestContainer.indexContainer.guiStemsMyFavoritesAbbreviated}" var="guiStem">
                            <li>
                            ${guiStem.shortLinkWithIconAndPath }
                            </li>
                          </c:forEach>
    
                          <c:forEach items="${grouperRequestContainer.indexContainer.guiMembersMyFavoritesAbbreviated}" var="guiMember">
                            <li>
                            ${guiMember.shortLinkWithIcon }
                            </li>
                          </c:forEach>
    
                          <c:forEach items="${grouperRequestContainer.indexContainer.guiAttributeDefNamesMyFavoritesAbbreviated}" var="guiAttributeDefName">
                            <li>
                            ${guiAttributeDefName.shortLinkWithIcon }
                            </li>
                          </c:forEach>
    
                          <c:forEach items="${grouperRequestContainer.indexContainer.guiAttributeDefsMyFavoritesAbbreviated}" var="guiAttributeDef">
                            <li>
                            ${guiAttributeDef.shortLinkWithIcon }
                            </li>
                          </c:forEach>
                        </ul>
                      </c:when>
                      <c:otherwise>
                        <a href="#" onclick="ajax('UiV2Main.indexColMyFavorites?col=${col}&storePref=false'); return false;">${textContainer.text['indexMyFavoritesButtonLoad'] }</a>
                        <br /><br /><br /><br/>
                      </c:otherwise>
                    </c:choose>
                    
                    
                    <p><strong><a href="#" 
                  onclick="return guiV2link('operation=UiV2Main.myFavorites');">${textContainer.text['indexMyFavoritesViewAllFavorites'] }</a></strong></p>
                    <!-- start indexMyFavorites.jsp -->
                    