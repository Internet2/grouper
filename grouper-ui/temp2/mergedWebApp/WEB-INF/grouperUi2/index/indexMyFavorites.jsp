<!-- ./webapp/WEB-INF/grouperUi2/index/indexMyFavorites.jsp -->

<%@ include file="../assetsJsp/commonTaglib.jsp"%>
                    <!-- start indexMyFavorites.jsp -->
                    <%-- HJ 20150319 
                    <h4>My favorites</h4>
                    --%>
                    <h4>${textContainer.text['indexMyFavoritesTitle'] }</h4>
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
                    <p><strong><a href="#"
                  onclick="return guiV2link('operation=UiV2Main.myFavorites');">${textContainer.text['indexMyFavoritesViewAllFavorites'] }</a></strong></p>
                    <!-- start indexMyFavorites.jsp -->
