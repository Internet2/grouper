<%@ include file="../assetsJsp/commonTaglib.jsp"%>

                <li class="dropdown"><a data-toggle="dropdown" href="#" class="dropdown-toggle" aria-haspopup="true" aria-expanded="false" role="menu"
                onclick="$('#stem-more-tab').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#stem-more-tab li').first().focus();return true;});">${textContainer.text['stemMoreTab'] } <b class="caret"></b></a>
                  <ul class="dropdown-menu" id="stem-more-tab">
                    <c:if test="${grouperRequestContainer.stemContainer.canReadPrivilegeInheritance}">
                      <li><a href="#" onclick="return guiV2link('operation=UiV2Stem.privilegesInheritedToObjectsInFolder&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}', {dontScrollTop: true});">${textContainer.text['stemPrivilegesInheritedToObjectsInFolder'] }</a></li>
                    </c:if>
                    <c:if test="${grouperRequestContainer.stemContainer.canReadPrivilegeInheritance}">   
                      <li><a href="#" onclick="return guiV2link('operation=UiV2Stem.thisStemsPrivilegesInheritedFromFolders&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}', {dontScrollTop: true});">${textContainer.text['thisFoldersPrivilegesFromFolders'] }</a></li>
                    </c:if>
                  </ul>
                </li>
