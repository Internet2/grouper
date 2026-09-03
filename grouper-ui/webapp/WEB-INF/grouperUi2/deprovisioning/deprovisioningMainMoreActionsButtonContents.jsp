<%@ include file="../assetsJsp/commonTaglib.jsp"%>

                  <div class="span10">
                    <div class="btn-group btn-block">
                    
                      <button type="button" data-toggle="dropdown" aria-label="${textContainer.text['ariaLabelGuiMoreDeprovisionMainActions']}" id="more-action-button" class="btn btn-medium btn-block dropdown-toggle" 
                        aria-haspopup="true" aria-expanded="false" onclick="$('#deprovisioning-main-more-options').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#deprovisioning-main-more-options li').first().focus();return true;});">
                          ${textContainer.text['deprovisioningMainMoreActionsButton'] } <span class="caret"></span></button>
  
                      <ul class="dropdown-menu dropdown-menu-right" id="deprovisioning-main-more-options">

                        <li><a href="?operation=UiV2Deprovisioning.viewRecentlyDeprovisionedUsers&affiliation=${grouperRequestContainer.deprovisioningContainer.affiliation}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Deprovisioning.viewRecentlyDeprovisionedUsers&affiliation=${grouperRequestContainer.deprovisioningContainer.affiliation}'); return false;"
                              >${textContainer.text['deprovisioningMainMoreActionsDefault'] }</a></li>
                        <c:if test="${grouperRequestContainer.deprovisioningContainer.allowedToDeprovision}">
                          <li><a href="?operation=UiV2Deprovisioning.deprovisionUser&affiliation=${grouperRequestContainer.deprovisioningContainer.affiliation}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Deprovisioning.deprovisionUser&affiliation=${grouperRequestContainer.deprovisioningContainer.affiliation}'); return false;"
                              >${textContainer.text['deprovisioningMainMoreActionsDeprovision'] }</a></li>
                        </c:if>

                      </ul>
                    </div>
                  </div>