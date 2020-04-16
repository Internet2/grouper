<%@ include file="../assetsJsp/commonTaglib.jsp"%>

                    <!-- start admin/adminDaemonJobsMoreActionsButtonContents.jsp -->

                    <div class="btn-group btn-block">
                    
                      <a data-toggle="dropdown" href="#" aria-label="${textContainer.text['daemonJobsMoreActionsAria']}" id="more-action-button" class="btn btn-medium btn-block dropdown-toggle" 
                        aria-haspopup="true" aria-expanded="false" role="menu" onclick="$('#daemon-more-options').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#daemon-more-options li').first().focus();return true;});">
                          ${textContainer.text['daemonJobsViewMoreActionsButton'] } <span class="caret"></span></a>
  
                      <ul class="dropdown-menu dropdown-menu-right" id="daemon-more-options">

                        <li><a href="#" onclick="return guiV2link('operation=UiV2Admin.daemonJobs'); return false;"
                            >${textContainer.text['daemonJobsButtonAllDaemonJobs'] }</a></li>

                        <li><a href="#" onclick="return guiV2link('operation=UiV2GrouperLoader.loaderOverall'); return false;"
                            >${textContainer.text['adminLoaderLink'] }</a></li>

                        <li><a href="#" onclick="return guiV2link('operation=UiV2Admin.jobHistoryChart'); return false;"
                            >${textContainer.text['adminJobHistoryChart'] }</a></li>

                      </ul>
                    </div>

                    <!-- end admin/adminDaemonJobsMoreActionsButtonContents.jsp -->
