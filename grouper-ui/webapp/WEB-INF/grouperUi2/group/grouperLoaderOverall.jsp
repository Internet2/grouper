<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <div class="bread-header-container">
              <ul class="breadcrumb">
                  <li><a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');">${textContainer.text['myServicesHomeBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                  <li><a href="#" onclick="return guiV2link('operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                  <li class="active">${textContainer.text['miscellaneousGrouperLoaderOverallBreadcrumb'] }</li>
              </ul>
              <div class="page-header blue-gradient">
                <h1>${textContainer.text['miscellaneousLoaderOverallDecription'] }</h1>
                <p style="margin-top: -1em; margin-bottom: 1em">${textContainer.text['miscellaneousLoaderOverallSubtitle']}</p>
              </div>
            </div>

            <div class="row-fluid">
              <div class="span12">
                <c:choose>
                  <c:when test="${fn:length(grouperRequestContainer.grouperLoaderContainer.guiGrouperLoaderJobs) == 0}">
                    <p>${textContainer.text['miscellaneousLoaderOverallNoJobs'] }</p>
                  </c:when>
                  <c:otherwise>
      
                    <table class="table table-hover table-bordered table-striped table-condensed data-table table-bulk-update table-privileges footable">
                      <thead>
                        <tr>
                          <th data-hide="phone" style="white-space: nowrap; text-align: left;">
                            <span rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                        data-original-title="${textContainer.textEscapeDouble['grouperLoaderOverallColumnTooltipGroup']}">${textContainer.text['grouperLoaderOverallColumnHeaderGroup'] }</span></th>
                          <th data-hide="phone" style="white-space: nowrap; text-align: left;">
                          <span rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                        data-original-title="${textContainer.textEscapeDouble['grouperLoaderOverallColumnTooltipStatus']}">${textContainer.text['grouperLoaderOverallColumnHeaderStatus'] }</span></th>
                          <th data-hide="phone" style="white-space: nowrap; text-align: left;">
                          <span rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                        data-original-title="${textContainer.textEscapeDouble['grouperLoaderOverallColumnTooltipActions']}">${textContainer.text['grouperLoaderOverallColumnHeaderActions'] }</span></th>
                          <th data-hide="phone" style="white-space: nowrap; text-align: left;">
                          <span rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                        data-original-title="${textContainer.textEscapeDouble['grouperLoaderOverallColumnTooltipCount']}">${textContainer.text['grouperLoaderOverallColumnHeaderCount'] }</span></th>
                          <th data-hide="phone" style="white-space: nowrap; text-align: left;">
                          <span rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                        data-original-title="${textContainer.textEscapeDouble['grouperLoaderOverallColumnTooltipChanges']}">${textContainer.text['grouperLoaderOverallColumnHeaderChanges'] }</span></th>
                          <th data-hide="phone" style="white-space: nowrap; text-align: left;">
                          <span rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                        data-original-title="${textContainer.textEscapeDouble['grouperLoaderOverallColumnTooltipType']}">${textContainer.text['grouperLoaderOverallColumnHeaderType'] }</span></th>
                          <th data-hide="phone" style="white-space: nowrap; text-align: left;">
                          <span rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                        data-original-title="${textContainer.textEscapeDouble['grouperLoaderOverallColumnTooltipSource']}">${textContainer.text['grouperLoaderOverallColumnHeaderSource'] }</span></th>
                          <th data-hide="phone" style="white-space: nowrap; text-align: left;">
                          <span rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                        data-original-title="${textContainer.textEscapeDouble['grouperLoaderOverallColumnTooltipSchedule']}">${textContainer.text['grouperLoaderOverallColumnHeaderSchedule'] }</span></th>
                          <th data-hide="phone" style="white-space: nowrap; text-align: left;">
                          <span rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                        data-original-title="${textContainer.textEscapeDouble['grouperLoaderOverallColumnTooltipQuery']}">${textContainer.text['grouperLoaderOverallColumnHeaderQuery'] }</span></th>
                        </tr>
                      </thead>
                      <tbody>
                        <c:set var="i" value="0" />
                        <c:forEach  items="${grouperRequestContainer.grouperLoaderContainer.guiGrouperLoaderJobs}" 
                          var="guiGrouperLoaderJob">
                          <tr>
                            <td class="expand foo-clicker" style="white-space: nowrap;">${guiGrouperLoaderJob.guiGroup.shortLinkWithIcon}
                            </td>
                            <td class="expand foo-clicker" 
                               style="color: White; 
                               background-color: ${guiGrouperLoaderJob.status == 'SUCCESS' ? 'green' : (guiGrouperLoaderJob.status == 'ERROR' ? 'red' : '')}; 
                               font-weight: bold;">
                            <span rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" class="grouperTooltip" style="border-bottom-color: white;"
                                data-original-title="${grouper:escapeHtml(guiGrouperLoaderJob.statusDescription)}">${guiGrouperLoaderJob.status}</span>
                            </td>
                            <td class="expand foo-clicker">
                              <div class="btn-group btn-block">

					                      <a data-toggle="dropdown" href="#" aria-label="${textContainer.text['ariaLabelGuiMoreGrouperLoaderActions']}" id="more-action-button" class="btn btn-medium btn-block dropdown-toggle" 
					                        aria-haspopup="true" aria-expanded="false" role="menu" onclick="$('#grouper-loader-more-options').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#grouper-loader-more-options li').first().focus();return true;});">
					                          ${textContainer.text['grouperLoaderViewMoreActionsButton'] } <span class="caret"></span></a>
					  
					                      <ul class="dropdown-menu dropdown-menu-right" id="grouper-loader-more-options">

					                        <li><a href="#" onclick="return guiV2link('operation=UiV2GrouperLoader.loader&groupId=${guiGrouperLoaderJob.guiGroup.group.id}'); return false;"
					                            >${textContainer.text['grouperLoaderMoreActionsViewLoader'] }</a></li>
					                            
				                          <li><a href="#" onclick="return guiV2link('operation=UiV2GrouperLoader.viewLogs&groupId=${guiGrouperLoaderJob.guiGroup.group.id}'); return false;"
				                              >${textContainer.text['grouperLoaderMoreActionsViewLoaderLogs'] }</a></li>
  
			                            <li><a href="#" onclick="ajax('../app/UiV2Group.updateLoaderGroup?groupId=${guiGrouperLoaderJob.guiGroup.group.id}'); return false;"
			                              >${textContainer.text['groupRunLoaderProcessButton'] }</a></li>

				                          <li><a href="#" onclick="return guiV2link('operation=UiV2GrouperLoader.loaderDiagnostics&groupId=${guiGrouperLoaderJob.guiGroup.group.id}'); return false;"
				                              >${textContainer.text['grouperLoaderDiagnosticsButton'] }</a></li>

				                          <li><a href="#" onclick="return guiV2link('operation=UiV2GrouperLoader.editGrouperLoader?groupId=${guiGrouperLoaderJob.guiGroup.group.id}'); return false;"
				                              >${textContainer.text['grouperLoaderEditConfiguration'] }</a></li>

				                          <li><a href="#" onclick="ajax('../app/UiV2Group.scheduleLoaderGroup?groupId=${guiGrouperLoaderJob.guiGroup.group.id}'); return false;"
				                            >${textContainer.text['groupScheduleLoaderProcessButton'] }</a></li>
					                            
					                      </ul>
					                    </div>
                            </td>
                            <td class="expand foo-clicker">${guiGrouperLoaderJob.count}
                            </td>
                            <td class="expand foo-clicker">${guiGrouperLoaderJob.changes}
                            </td>
                            <td class="expand foo-clicker">${guiGrouperLoaderJob.type}
                            </td>
                            <td class="expand foo-clicker"><span rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" class="grouperTooltip"
                                data-original-title="${grouper:escapeHtml(guiGrouperLoaderJob.sourceDescription)}">${guiGrouperLoaderJob.source}</span>
                            
                            </td>
                            <td class="expand foo-clicker">${guiGrouperLoaderJob.schedule}
                            </td>
                            <td class="expand foo-clicker"><input type="text" value="${grouper:escapeHtml(guiGrouperLoaderJob.query)}" style="width: 50em"/>
                            </td>
                          </tr>
                          <c:set var="i" value="${i+1}" />
                        </c:forEach>
                      </tbody>
                    </table>
                  </c:otherwise>
                </c:choose>
              </div>
            </div>
