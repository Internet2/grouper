<%@ include file="../assetsJsp/commonTaglib.jsp"%>

                <li class="dropdown"><a data-toggle="dropdown" href="#" class="dropdown-toggle">${textContainer.text['attributeDefMoreTab'] } <b class="caret"></b></a>
                  <ul class="dropdown-menu">
                    <c:if test="${grouperRequestContainer.attributeDefContainer.canReadPrivilegeInheritance}">   
                      <li><a href="#" onclick="return guiV2link('operation=UiV2AttributeDef.thisAttributeDefsPrivilegesInheritedFromFolders&attributeDefId=${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.id}', {dontScrollTop: true});">${textContainer.text['thisAttributeDefsPrivilegesFromFolders'] }</a></li>
                    </c:if>
                  </ul>
                </li>
