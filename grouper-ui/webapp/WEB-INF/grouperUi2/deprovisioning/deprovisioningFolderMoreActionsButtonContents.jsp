<%@ include file="../assetsJsp/commonTaglib.jsp"%>

                    <!-- start deprovisioning/deprovisioningFolderMoreActionsButtonContents.jsp -->

                    <div class="btn-group btn-block">
                    
                      <a data-toggle="dropdown" href="#" aria-label="${textContainer.text['ariaLabelGuiMoreDeprovisioningActions']}" id="more-action-button" class="btn btn-medium btn-block dropdown-toggle" 
                        aria-haspopup="true" aria-expanded="false" role="menu" onclick="$('#deprovisioning-more-options').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#deprovisioning-more-options li').first().focus();return true;});">
                          ${textContainer.text['deprovisioningMoreActionsButton'] } <span class="caret"></span></a>

                      <ul class="dropdown-menu dropdown-menu-right" id="deprovisioning-more-options">

                        <c:if test="${grouperRequestContainer.deprovisioningContainer.canReadDeprovisioning}" >
                          <li><a href="#" onclick="return guiV2link('operation=UiV2Deprovisioning.deprovisioningOnFolder&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}'); return false;"
                              >${textContainer.text['deprovisioningMoreActionsStemSettings'] }</a></li>
                        </c:if>

                        <c:if test="${grouperRequestContainer.deprovisioningContainer.canWriteDeprovisioning}" >
	                        <li><a href="#" onclick="return guiV2link('operation=UiV2Deprovisioning.deprovisioningOnFolderEdit&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}'); return false;"
	                            >${textContainer.text['deprovisioningMoreActionsStemEditSettings'] }</a></li>
                        </c:if>

                        <li><a href="#" onclick="return guiV2link('operation=UiV2Deprovisioning.deprovisioningMain'); return false;"
                            >${textContainer.text['deprovisioningMoreActionsOverallDeprovision'] }</a></li>

                        <c:if test="${grouperRequestContainer.deprovisioningContainer.canWriteDeprovisioning}" >
	                        <li><a href="#" onclick="return guiV2link('operation=UiV2Deprovisioning.deprovisioningOnFolderReport&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}'); return false;"
	                            >${textContainer.text['deprovisioningMoreActionsDeprovisioningReport'] }</a></li>
	                    </c:if>
                            
                        <c:if test="${grouperRequestContainer.deprovisioningContainer.canWriteDeprovisioning}" >
                          <li><a href="#" onclick="ajax('../app/UiV2Deprovisioning.updateFolderLastCertifiedDateClear'); return false;"
                              >${textContainer.text['deprovisioningMoreActionsClearCertify'] }</a></li>
                        </c:if>
                        <c:if test="${grouperRequestContainer.deprovisioningContainer.canRunDaemon}" >
                          <li><a href="#" onclick="ajax('../app/UiV2Deprovisioning.runDaemon'); return false;"  	                            >${textContainer.text['groupDeprovisioningMoreActionsRunDaemon'] }</a></li>
	                    </c:if>
                      </ul>

                    </div>

                    <!-- end deprovisioning/deprovisioningFolderMoreActionsButtonContents.jsp -->
