<%@ include file="../assetsJsp/commonTaglib.jsp"%>

                    <div class="btn-group btn-block">
                    
                      <a data-toggle="dropdown" href="#" aria-label="${textContainer.text['ariaLabelGuiMoreGrouperReportActions']}" id="more-action-button" class="btn btn-medium btn-block dropdown-toggle" 
                        aria-haspopup="true" aria-expanded="false" role="menu" onclick="$('#grouper-report-more-options').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#grouper-report-more-options li').first().focus();return true;});">
                          ${textContainer.text['grouperReportMoreActionsButton'] } <span class="caret"></span></a>

                      <ul class="dropdown-menu dropdown-menu-right" id="grouper-report-more-options">

                        <c:if test="${grouperRequestContainer.grouperReportContainer.canReadGrouperReports}" >
                          <li><a href="#" onclick="return guiV2link('operation=UiV2GrouperReport.viewReportOnFolder&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}'); return false;"
                              >${textContainer.text['grouperReportMoreActionsStemViewReports'] }</a></li>
                        </c:if>

                        <c:if test="${grouperRequestContainer.grouperReportContainer.canWriteGrouperReports}" >
	                            
	                        <li><a href="#" onclick="return guiV2link('operation=UiV2GrouperReport.reportOnFolderAdd&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}'); return false;"
                              >${textContainer.text['grouperReportMoreActionsStemAddReport'] }</a></li>
                          
                          <li><a href="#" onclick="return guiV2link('operation=UiV2GrouperReport.reportOnFolderEdit&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}'); return false;"
                              >${textContainer.text['grouperReportMoreActionsStemEditReports'] }</a></li>
                        </c:if>

                       <%--  <li><a href="#" onclick="return guiV2link('operation=UiV2Deprovisioning.deprovisioningMain'); return false;"
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
                          <li><a href="#" onclick="ajax('../app/UiV2Deprovisioning.runDaemon'); return false;">${textContainer.text['groupDeprovisioningMoreActionsRunDaemon'] }</a></li>
	                    </c:if> --%>
                      </ul>

                    </div>