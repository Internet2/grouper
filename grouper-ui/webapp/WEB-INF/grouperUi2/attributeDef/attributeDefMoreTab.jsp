<%@ include file="../assetsJsp/commonTaglib.jsp"%>

                <li class="dropdown"><a role="tab" data-toggle="dropdown" aria-label="${textContainer.text['ariaLabelGuiMoreOptions']}" href="#" class="dropdown-toggle"
                	aria-haspopup="true" aria-expanded="false" role="menu" onclick="$('#attribute-more-tab').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#attribute-more-tab li').first().focus();return true;});">${textContainer.text['attributeDefMoreTab'] } <b class="caret"></b></a>
                  <ul class="dropdown-menu" id="attribute-more-tab">
                  	<li><a href="#" onclick="return guiV2link('operation=UiV2AttributeDef.attributeDefActions&attributeDefId=${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.id}', {dontScrollTop: true});">${textContainer.text['thisAttributeDefsActions'] }</a></li>
                    <c:if test="${grouperRequestContainer.attributeDefContainer.canReadPrivilegeInheritance}">   
                      <li><a href="#" onclick="return guiV2link('operation=UiV2AttributeDef.thisAttributeDefsPrivilegesInheritedFromFolders&attributeDefId=${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.id}', {dontScrollTop: true});">${textContainer.text['thisAttributeDefsPrivilegesFromFolders'] }</a></li>
                    </c:if>
                  </ul>
                </li>
