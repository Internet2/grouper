<%@ include file="../assetsJsp/commonTaglib.jsp"%>

                  <div class="span10">
                    <div class="btn-group btn-block">
                    
                      <button type="button" data-toggle="dropdown" aria-label="${textContainer.text['ariaLabelGuiMoreSubjectResolutionMainActions']}" id="more-action-button" class="btn btn-medium btn-block dropdown-toggle" 
                        aria-haspopup="true" aria-expanded="false" onclick="$('#subject-resolution-main-more-options').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#subject-resolution-main-more-options li').first().focus();return true;});">
                          ${textContainer.text['deprovisioningMainMoreActionsButton'] } <span class="caret"></span></button>
  
                      <ul class="dropdown-menu dropdown-menu-right" id="subject-resolution-main-more-options">


                        <c:if test="${grouperRequestContainer.subjectResolutionContainer.allowedToSubjectResolution}">
                          
                          <li><a href="?operation=UiV2SubjectResolution.subjectResolutionMain" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2SubjectResolution.subjectResolutionMain'); return false;"
                              >${textContainer.text['subjectResolutionMainMoreActionsStats'] }</a></li>
                        
                          <li><a href="?operation=UiV2SubjectResolution.searchSubjects" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2SubjectResolution.searchSubjects'); return false;"
                              >${textContainer.text['subjectResolutionMainMoreActionsSearchSubjects'] }</a></li>
                              
                          <li><a href="?operation=UiV2SubjectResolution.viewUnresolvedSubjects" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2SubjectResolution.viewUnresolvedSubjects'); return false;"
                              >${textContainer.text['subjectResolutionMainMoreActionsViewUnresolved'] }</a></li>
                              
                          <li><a href="?operation=UiV2SubjectResolution.viewSubjectDeleteAudits" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2SubjectResolution.viewSubjectDeleteAudits'); return false;"
                              >${textContainer.text['subjectResolutionMainMoreActionsViewAuditLogs'] }</a></li>  
                              
                          <li><a href="?operation=UiV2Admin.daemonJobs&daemonJobsFilter=usduDaemon" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Admin.daemonJobs&daemonJobsFilter=usduDaemon'); return false;" style="white-space: nowrap;"
                              >${textContainer.text['subjectResolutionMainMoreActionsViewDaemonLogs'] }</a></li>    
                              
                        </c:if>
                        
                      </ul>
                    </div>
                  </div>