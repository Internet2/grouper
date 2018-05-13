<%@ include file="../assetsJsp/commonTaglib.jsp"%>

                  <div class="span10">
                    <div class="btn-group btn-block">
                    
                      <a data-toggle="dropdown" href="#" aria-label="${textContainer.text['ariaLabelGuiMoreDeprovisionMainActions']}" id="more-action-button" class="btn btn-medium btn-block dropdown-toggle" 
                        aria-haspopup="true" aria-expanded="false" role="menu" onclick="$('#deprovisioning-main-more-options').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#deprovisioning-main-more-options li').first().focus();return true;});">
                          ${textContainer.text['deprovisioningMainMoreActionsButton'] } <span class="caret"></span></a>
  
                      <ul class="dropdown-menu dropdown-menu-right" id="deprovisioning-main-more-options">

                        <li><a href="#" onclick="return guiV2link('operation=UiV2Deprovisioning.viewRecentlyDeprovisionedUsers&realm=${grouperRequestContainer.deprovisioningContainer.realm}'); return false;"
                              >${textContainer.text['deprovisioningMainMoreActionsDefault'] }</a></li>
                        <c:if test="${grouperRequestContainer.deprovisioningContainer.allowedToDeprovision}">
                          <li><a href="#" onclick="return guiV2link('operation=UiV2Deprovisioning.deprovisionUser1&realm=${grouperRequestContainer.deprovisioningContainer.realm}'); return false;"
                              >${textContainer.text['deprovisioningMainMoreActionsDeprovision'] }</a></li>
                        </c:if>

                      </ul>
                    </div>
                  </div>