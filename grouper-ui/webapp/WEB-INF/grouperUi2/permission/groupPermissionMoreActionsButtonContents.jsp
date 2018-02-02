<%@ include file="../assetsJsp/commonTaglib.jsp"%>

                    <div class="btn-group btn-block">
                    
                      <a data-toggle="dropdown" href="#" aria-label="${textContainer.text['ariaLabelGuiMoreGroupPermissionActions']}" id="more-action-button"
                         class="btn btn-medium btn-block dropdown-toggle" 
                         aria-haspopup="true" aria-expanded="false" role="menu" onclick="$('#group-permission-more-options').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#group-permission-more-options li').first().focus();return true;});">
                          ${textContainer.text['groupPermissionViewMoreActionsButton'] } <span class="caret"></span></a>
  
                      <ul class="dropdown-menu dropdown-menu-right" id="group-permission-more-options">
                        
                          <li><a href="#" onclick="return guiV2link('operation=UiV2Permission.groupViewPermissions&groupId=${grouperRequestContainer.permissionContainer.guiGroup.group.id}'); return false;"
                              >${textContainer.text['groupPermissionnMoreActionsViewPermission'] }</a></li>
                          
                          <c:if test="${grouperRequestContainer.permissionContainer.canAssignPermission}">
                          	<li><a href="#" onclick="return guiV2link('operation=UiV2Permission.groupAssignPermission&groupId=${grouperRequestContainer.permissionContainer.guiGroup.group.id}'); return false;"
                              >${textContainer.text['groupPermissionnMoreActionsAssignPermission'] }</a></li>
                          </c:if>      
                          

                      </ul>
                    </div>