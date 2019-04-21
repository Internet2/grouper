<%@ include file="../assetsJsp/commonTaglib.jsp"%>

                    <div class="btn-group btn-block">
                    
                      <a data-toggle="dropdown" href="#" aria-label="${textContainer.text['ariaLabelGuiMoreGrouperReportActions']}" id="more-action-button" class="btn btn-medium btn-block dropdown-toggle" 
                        aria-haspopup="true" aria-expanded="false" role="menu" onclick="$('#grouper-report-more-options').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#grouper-report-more-options li').first().focus();return true;});">
                          ${textContainer.text['grouperReportMoreActionsButton'] } <span class="caret"></span></a>

                      <ul class="dropdown-menu dropdown-menu-right" id="grouper-report-more-options">
                        
                         <li><a href="#" onclick="return guiV2link('operation=UiV2GrouperReport.viewReportConfigsOnGroup&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}'); return false;"
                             >${textContainer.text['grouperReportMoreActionsStemViewReports'] }</a></li>

                        <c:if test="${grouperRequestContainer.grouperReportContainer.canWriteGrouperReports}" >
	                            
	                        <li><a href="#" onclick="return guiV2link('operation=UiV2GrouperReport.reportOnGroupAdd&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}'); return false;"
                              >${textContainer.text['grouperReportMoreActionsStemAddReport'] }</a></li>
                          
                          <li><a href="#" onclick="return guiV2link('operation=UiV2GrouperReport.reportOnGroupEdit&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}'); return false;"
                              >${textContainer.text['grouperReportMoreActionsStemEditReports'] }</a></li>
                        </c:if>

                      </ul>

                    </div>