<%@ include file="../assetsJsp/commonTaglib.jsp"%>

                <li class="dropdown"><a data-toggle="dropdown" href="#" class="dropdown-toggle">${textContainer.text['stemMoreTab'] } <b class="caret"></b></a>
                  <ul class="dropdown-menu">
                    <c:if test="${grouperRequestContainer.stemContainer.canReadPrivilegeInheritance}">
                      <li><a href="#" onclick="return guiV2link('operation=UiV2Stem.privilegesInheritedToObjectsInFolder&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}', {dontScrollTop: true});">${textContainer.text['stemPrivilegesInheritedToObjectsInFolder'] }</a></li>
                    </c:if>
                  </ul>
                </li>
