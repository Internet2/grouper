<%@ include file="../assetsJsp/commonTaglib.jsp"%>

${grouper:title('loaderJobsPageTitle')}

            <div class="bread-header-container">
              <ul class="breadcrumb">
                  <li><a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');">${textContainer.text['myServicesHomeBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                  <li><a href="#" onclick="return guiV2link('operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                  <li class="active">${textContainer.text['miscellaneousGrouperLoaderOverallBreadcrumb'] }</li>
              </ul>
              <div class="page-header blue-gradient">
                <div class="row-fluid">
              
                <div class="span2 pull-right" id="daemonMoreActionsButtonContentsDivId">
                  <%@ include file="../admin/adminDaemonJobsMoreActionsButtonContents.jsp"%>
                </div>
                <div class="span10 pull-left">
                  <h1>${textContainer.text['miscellaneousLoaderOverallDecription'] }</h1>
                  <p style="margin-top: -1em; margin-bottom: 1em">${textContainer.text['miscellaneousLoaderOverallSubtitle']}</p>
                </div>
              </div>
            </div>

            <div class="row-fluid">
              <div class="span12">
                <form class="form-inline form-filter" id="loaderJobsFilterFormId">
                  <div class="row-fluid">
                    <div class="span1">
                      <label for="loaderJobsFilterId" style="white-space: nowrap;">${textContainer.text['grouperLoaderOverallColumnHeaderGroup'] }:</label>
                    </div>
                    <div class="span4" style="white-space: nowrap;">
                      <input type="text" name="loaderJobsFilter" id="loaderJobsFilterId" class="span12"
                          placeholder="${textContainer.text['grouperLoaderOverallColumnHeaderGroup'] }"
                      />
                    </div>
                  </div>

                  <div class="row-fluid" style="margin-top: 0.5em">
                    <div class="span1">
                      <label for="loaderJobsCommonFilterId">${textContainer.text['grouperLoaderOverallColumnHeaderType'] }:</label>
                    </div>
                    <div class="span4" style="white-space: nowrap;">
                      <select name="loaderJobsCommonFilter" id="loaderJobsCommonFilterId">
                        <option value="" style="color:#aaaaaa !important">${textContainer.retrieveFromRequest().getText().get('daemonJobsCommonSearchNamePlaceholder')}</option>
                        <option>SQL_SIMPLE</option>option>
                        <option>SQL_GROUP_LIST</option>option>
                        <option>LDAP_SIMPLE</option>option>
                        <option>LDAP_GROUP_LIST</option>option>
                        <option>LDAP_GROUPS_FROM_ATTRIBUTES</option>option>
                      </select>
                    </div>
                  </div>

                  <div class="row-fluid" style="margin-top: 0.5em">
                    <div class="span1">
                      <label for="xxx">${textContainer.text['grouperLoaderOverallColumnHeaderStatus'] }:</label>
                    </div>
                    <div class="span4" style="white-space: nowrap;">
                      <select name="loaderJobsStatusFilter" id="loaderJobsStatusFilterId">
                        <option value="" style="color:#aaaaaa !important">${textContainer.retrieveFromRequest().getText().get('daemonJobsStatusSearchNamePlaceholder')}</option>
                          <option value="SUCCESS">${textContainer.retrieveFromRequest().getText().get("grouperLoaderStatus_SUCCESS")}</option>
                          <option value="STARTED">${textContainer.retrieveFromRequest().getText().get("grouperLoaderStatus_STARTED")}</option>
                          <option value="RUNNING">${textContainer.retrieveFromRequest().getText().get("grouperLoaderStatus_RUNNING")}</option>
                          <option value="ANY_ERROR">${textContainer.retrieveFromRequest().getText().get("grouperLoaderStatus_ANY_ERROR")}</option>
                          <option value="ERROR_FAILSAFE">${textContainer.retrieveFromRequest().getText().get("grouperLoaderStatus_ERROR_FAILSAFE")}</option>
                          <option value="SUBJECT_PROBLEMS">${textContainer.retrieveFromRequest().getText().get("grouperLoaderStatus_SUBJECT_PROBLEMS")}</option>
                          <option value="WARNING">${textContainer.retrieveFromRequest().getText().get("grouperLoaderStatus_WARNING")}</option>
                          <option value="CONFIG_ERROR">${textContainer.retrieveFromRequest().getText().get("grouperLoaderStatus_CONFIG_ERROR")}</option>
                      </select>
                    </div>
                  </div>

                  <div class="row-fluid" style="margin-top: 1em; margin-bottom: 1em">
                    <div class="span2">&nbsp;</div>
                    <a class="btn" role="button" id="applyfilterloaderjobs" aria-controls="groupFilterResultsId" href="#"
                       onclick="ajax('../app/UiV2GrouperLoader.loaderOverallFilter', {formIds: 'loaderJobsFilterFormId'}); return false;">
                        ${textContainer.text['daemonJobsSearchButton'] }
                    </a>
                    <a href="#" class="btn" role="button"
                       onclick="document.getElementById('loaderJobsFilterFormId').reset(); ajax('../app/UiV2GrouperLoader.loaderOverall', {formIds: 'loaderJobsFilterFormId'}); return false;">
                        ${textContainer.text['daemonJobsResetButton'] }
                    </a>
                  </div>
                </form>

                <c:choose>
                  <c:when test="${fn:length(grouperRequestContainer.grouperLoaderContainer.guiGrouperLoaderJobs) == 0}">
                    <p>${textContainer.text['miscellaneousLoaderOverallNoJobs'] }</p>
                  </c:when>
                  <c:otherwise>

                    <div id="loaderJobsResultsId" role="region" aria-live="polite">
                      </div>

                  </c:otherwise>
                </c:choose>
              </div>
            </div>
