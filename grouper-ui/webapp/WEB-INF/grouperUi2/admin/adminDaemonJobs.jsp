<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <div class="bread-header-container">
              <ul class="breadcrumb">
                <li><a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');">${textContainer.text['adminDaemonJobsHomeBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                <li><a href="#" onclick="return guiV2link('operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                <li class="active">${textContainer.text['adminDaemonJobsBreadcrumb'] }</li>
              </ul>
              <div class="page-header blue-gradient">
                <h1>${textContainer.text['adminDaemonJobsTitle'] }</h1>
              </div>

            </div>
            <script language="javascript">
              var daemonJobsRefreshCountRemaining = ${grouperRequestContainer.adminContainer.daemonJobsRefreshCount};
              var daemonJobsRefreshSeconds = ${grouperRequestContainer.adminContainer.daemonJobsRefreshInterval};
              var daemonJobsNextRefreshSeconds = ${grouperRequestContainer.adminContainer.daemonJobsRefreshInterval};
              var theFunction = function() {
                if (!$("#daemonJobsNextRefreshSeconds").length || daemonJobsRefreshCountRemaining <= 0 || daemonJobsNextRefreshSeconds < 0) {
                  if (daemonJobsRefreshCountRemaining <= 0) {
                    $("#daemonJobsNextRefreshSeconds").text("${textContainer.text['daemonJobsMaxRefreshesReached'] }");
                  }
                  clearInterval(daemonJobsRefreshInterval);
                  daemonJobsRefreshInterval = null;
                } else {
                  if ($("#daemonJobsRefreshed").val() != "1") {
                  } else if (daemonJobsNextRefreshSeconds == 0) {
                    $("#daemonJobsRefreshed").val("0");
                    daemonJobsNextRefreshSeconds = ${grouperRequestContainer.adminContainer.daemonJobsRefreshInterval};
                    daemonJobsRefreshCountRemaining = daemonJobsRefreshCountRemaining - 1;
                    ajax('../app/UiV2Admin.daemonJobsSubmit', {formIds: 'daemonJobsFilterFormId, daemonJobsPagingFormId, daemonJobsPagingFormPageNumberId'});
                  } else {
                    daemonJobsNextRefreshSeconds = daemonJobsNextRefreshSeconds - 1;
                  }
                  
                  $("#daemonJobsNextRefreshSeconds").text(daemonJobsNextRefreshSeconds);
                }
              };
              var daemonJobsRefreshInterval = setInterval(theFunction, 1000);
            </script>
            <div class="row-fluid">
              <div class="span12">
                <form class="form-inline form-filter" id="daemonJobsFilterFormId"
                    onsubmit="ajax('../app/UiV2Admin.daemonJobsSubmit', {formIds: 'daemonJobsFilterFormId, daemonJobsPagingFormId'}); return false;">
                  <div class="row-fluid">
                    <div class="span1">
                      <label for="daemonJobsFilterId" style="white-space: nowrap;">${textContainer.text['daemonJobsFilterFor'] }</label>
                    </div>
                    <div class="span4" style="white-space: nowrap;">
                      <input type="text" name="daemonJobsFilter" placeholder="${textContainer.textEscapeXml['daemonJobsSearchNamePlaceholder'] }" id="daemonJobsFilterId" class="span12"/>
                    </div>
                  </div>
                  <div class="row-fluid" style="margin-top: 0.5em">
                    <div class="span1">&nbsp;</div>
                    <div class="span3" style="white-space: nowrap;">
                      <label class="checkbox">
                        <input type="checkbox" name="daemonJobsFilterShowExtendedResults">${textContainer.text['daemonJobsFilterShowExtendedResults']}
                      </label>
                    </div>
                  </div>
                  <div class="row-fluid" style="margin-top: 0.5em; margin-bottom: 0.5em">
                    <div class="span1">&nbsp;</div>
                    <div class="span3" style="white-space: nowrap;">
                      <label class="checkbox">
                        <input type="checkbox" name="daemonJobsFilterShowOnlyErrors">${textContainer.text['daemonJobsFilterShowOnlyErrors']}
                      </label>
                    </div>
                  </div>
                  <div class="row-fluid">
                    <div class="span1">&nbsp;</div>
                      <a class="btn" role="button" aria-controls="daemonJobsResultsId" href="#" onclick="daemonJobsNextRefreshSeconds=-1;ajax('../app/UiV2Admin.daemonJobsSubmit', {formIds: 'daemonJobsFilterFormId, daemonJobsPagingFormId'}); return false;">${textContainer.text['daemonJobsSearchButton'] }</a> &nbsp;
                      <a href="#" onclick="daemonJobsNextRefreshSeconds=-1;ajax('../app/UiV2Admin.daemonJobsReset', {formIds: 'daemonJobsPagingFormId'}); return false;" class="btn" role="button">${textContainer.text['daemonJobsResetButton'] }</a>
                    </div>
                  </div>
                </form>
                <div style="white-space: nowrap;" class="span3">${textContainer.text['daemonJobsNextRefresh']} <span id="daemonJobsNextRefreshSeconds">${grouperRequestContainer.adminContainer.daemonJobsRefreshInterval}</span></div>
                <div id="daemonJobsResultsId" role="region" aria-live="polite">
                </div>
              </div>
            </div>
