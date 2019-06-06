<%@ include file="../assetsJsp/commonTaglib.jsp"%>

                    <div class="btn-group btn-block">
                    
                      <a data-toggle="dropdown" href="#" aria-label="${textContainer.text['ariaLabelGuiMoreProvisioningActions']}" id="more-action-button" class="btn btn-medium btn-block dropdown-toggle" 
                        aria-haspopup="true" aria-expanded="false" role="menu" onclick="$('#grouperTypes-more-options').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#provisioning-more-options li').first().focus();return true;});">
                          ${textContainer.text['provisioningMoreActionsButton'] } <span class="caret"></span></a>

                      <ul class="dropdown-menu dropdown-menu-right" id="provisioning-more-options">

                        <c:if test="${grouperRequestContainer.provisioningContainer.canReadProvisioning}" >
                          <li><a href="#" onclick="return guiV2link('operation=UiV2Provisioning.viewProvisioningOnFolder&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}'); return false;"
                              >${textContainer.text['provisioningMoreActionsViewSettings'] }</a></li>
                        </c:if>

                        <c:if test="${grouperRequestContainer.provisioningContainer.canWriteProvisioning}" >
	                        <li><a href="#" onclick="return guiV2link('operation=UiV2Provisioning.editProvisioningOnFolder&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}'); return false;"
	                            >${textContainer.text['provisioningMoreActionsEditSettings'] }</a></li>
                        </c:if>

                        <c:if test="${grouperRequestContainer.provisioningContainer.canRunDaemon}" >
                          <li><a href="#" onclick="ajax('../app/UiV2Provisioning.runDaemon'); return false;"
                              >${textContainer.text['provisioningMoreActionsRunDaemon'] }</a></li>
                        </c:if>

                      </ul>
                    </div>
