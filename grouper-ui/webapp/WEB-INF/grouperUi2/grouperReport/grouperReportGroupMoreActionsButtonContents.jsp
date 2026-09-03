<%@ include file="../assetsJsp/commonTaglib.jsp"%>

                    <div class="btn-group btn-block">
                    
                      <button type="button" data-toggle="dropdown" aria-label="${textContainer.text['ariaLabelGuiMoreGrouperReportActions']}" id="grouper-report-more-action-button" aria-controls="grouper-report-more-options" class="btn btn-medium btn-block dropdown-toggle" 
                        aria-haspopup="true" aria-expanded="false" onclick="$('#grouper-report-more-options').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#grouper-report-more-options li').first().focus();return true;});">
                          ${textContainer.text['grouperReportMoreActionsButton'] } <span class="caret"></span></button>

                      <ul class="dropdown-menu dropdown-menu-right" id="grouper-report-more-options">
                        
                         <li><a href="?operation=UiV2GrouperReport.viewReportConfigsOnGroup&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2GrouperReport.viewReportConfigsOnGroup&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}'); return false;"
                             >${textContainer.text['grouperReportMoreActionsStemViewReports'] }</a></li>

                        <c:if test="${grouperRequestContainer.grouperReportContainer.canWriteGrouperReports}" >
	                            
	                        <li><a href="?operation=UiV2GrouperReport.reportOnGroupAdd&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2GrouperReport.reportOnGroupAdd&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}'); return false;"
                              >${textContainer.text['grouperReportMoreActionsStemAddReport'] }</a></li>
                          
                          <li><a href="?operation=UiV2GrouperReport.reportOnGroupEdit&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2GrouperReport.reportOnGroupEdit&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}'); return false;"
                              >${textContainer.text['grouperReportMoreActionsStemEditReports'] }</a></li>
                        </c:if>

                      </ul>

                    </div>