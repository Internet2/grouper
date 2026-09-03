<%@ include file="../assetsJsp/commonTaglib.jsp"%>

${grouper:title('configurationUpgradeTasksPageTitle')}
            <grouper:browserPage jspName="configureUpgradeTasks" />
            <div class="bread-header-container">
              <ul class="breadcrumb">
                <li><a href="?operation=UiV2Main.indexMain" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Main.indexMain');">${textContainer.text['myServicesHomeBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                <li><a href="?operation=UiV2Main.miscellaneous" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                <li><a href="?operation=UiV2Configure.index" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Configure.index');">${textContainer.text['configurationIndexBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                <li class="active">${textContainer.text['configurationUpgradeTasksBreadcrumb'] }</li>
              </ul>
              <div class="page-header blue-gradient">
                <h1>${textContainer.text['configurationUpgradeTasksTitle'] }</h1>
                <p style="margin-top: -1em; margin-bottom: 1em">${textContainer.text['configurationUpgradeTasksSubtitle']}</p>
              </div>

            </div>
            <div class="row-fluid">
              <div class="span12">
                <div class="form-actions">
                  <button type="button" class="btn btn-primary" onclick="ajax('../app/UiV2Configure.upgradeTasksSubmit'); return false;">${textContainer.text['configurationUpgradeTasksLoadButton'] }</button>
                </div>
              </div>
            </div>
            <c:if test="${grouperRequestContainer.configurationContainer.guiUpgradeTasks != null}">
            <div class="row-fluid">
              <div class="span12">
                <table class="table table-hover table-bordered table-striped table-condensed data-table">
                  <thead>
                    <tr>
                      <th style="white-space: nowrap;">${textContainer.text['configurationUpgradeTasksColumnActions'] }</th>
                      <th style="white-space: nowrap;">${textContainer.text['configurationUpgradeTasksColumnVersion'] }</th>
                      <th>${textContainer.text['configurationUpgradeTasksColumnDescription'] }</th>
                      <th style="white-space: nowrap;">${textContainer.text['configurationUpgradeTasksColumnType'] }</th>
                      <th style="white-space: nowrap;">${textContainer.text['configurationUpgradeTasksColumnReleasedInVersion'] }</th>
                      <th style="white-space: nowrap;">${textContainer.text['configurationUpgradeTasksColumnStatus'] }</th>
                      <th>${textContainer.text['configurationUpgradeTasksColumnDetail'] }</th>
                    </tr>
                  </thead>
                  <tbody>
                    <c:forEach items="${grouperRequestContainer.configurationContainer.guiUpgradeTasks}" var="guiUpgradeTask">
                      <tr>
                        <%-- first column: actions dropdown (check status / run / mark complete / mark not complete) --%>
                        <td style="vertical-align: top; white-space: nowrap;">
                          <div class="btn-group">
                            <a data-toggle="dropdown" href="#" class="btn btn-mini dropdown-toggle"
                              aria-haspopup="true" aria-expanded="false" role="button"
                              aria-label="${textContainer.text['configurationUpgradeTasksActionsButton'] }">
                              ${textContainer.text['configurationUpgradeTasksActionsButton'] }
                              <span class="caret"></span>
                            </a>
                            <ul class="dropdown-menu" id="upgradeTaskActions_${guiUpgradeTask.version}">
                              <%-- actions in alphabetical order by label --%>
                              <c:choose>
                                <%-- unexpected task: recorded complete in the database but with no matching Java upgrade
                                     task, so there is nothing to run/check/mark complete.  Only offer a cleanup that
                                     removes the stray version from the metadata group's attribute assignments. --%>
                                <c:when test="${guiUpgradeTask.unexpected}">
                                  <li><a href="#" onclick="if (confirm('${textContainer.text['configurationUpgradeTasksActionMarkNotCompleteConfirm'] } V${guiUpgradeTask.version}?')) { ajax('../app/UiV2Configure.upgradeTasksMarkNotComplete?version=${guiUpgradeTask.version}'); } return false;">${textContainer.text['configurationUpgradeTasksActionMarkNotComplete'] }</a></li>
                                </c:when>
                                <c:otherwise>
                                  <%-- check status: runs the live DDL applicability check for this one task --%>
                                  <li><button type="button" class="grouper-menuitem" onclick="ajax('../app/UiV2Configure.upgradeTasksCheckStatus?version=${guiUpgradeTask.version}'); return false;">${textContainer.text['configurationUpgradeTasksActionCheckStatus'] }</button></li>
                                  <%-- mark complete without running, so confirm first --%>
                                  <li><a href="#" onclick="if (confirm('${textContainer.text['configurationUpgradeTasksActionMarkCompleteConfirm'] } V${guiUpgradeTask.version}?')) { ajax('../app/UiV2Configure.upgradeTasksMarkComplete?version=${guiUpgradeTask.version}'); } return false;">${textContainer.text['configurationUpgradeTasksActionMarkComplete'] }</a></li>
                                  <%-- mark not complete so it runs again, so confirm first --%>
                                  <li><a href="#" onclick="if (confirm('${textContainer.text['configurationUpgradeTasksActionMarkNotCompleteConfirm'] } V${guiUpgradeTask.version}?')) { ajax('../app/UiV2Configure.upgradeTasksMarkNotComplete?version=${guiUpgradeTask.version}'); } return false;">${textContainer.text['configurationUpgradeTasksActionMarkNotComplete'] }</a></li>
                                  <%-- run task: MUTATES the database, so confirm first --%>
                                  <li><a href="#" onclick="if (confirm('${textContainer.text['configurationUpgradeTasksActionRunConfirm'] } V${guiUpgradeTask.version}?')) { ajax('../app/UiV2Configure.upgradeTasksRun?version=${guiUpgradeTask.version}'); } return false;">${textContainer.text['configurationUpgradeTasksActionRun'] }</a></li>
                                </c:otherwise>
                              </c:choose>
                            </ul>
                          </div>
                        </td>
                        <%-- version number --%>
                        <td style="vertical-align: top; white-space: nowrap;">V${guiUpgradeTask.version}</td>
                        <%-- externalized description --%>
                        <td style="vertical-align: top;">${grouper:escapeHtml(guiUpgradeTask.description)}</td>
                        <%-- type: DDL (schema) vs data/maintenance (blank for unexpected tasks, which have no Java task) --%>
                        <td style="vertical-align: top; white-space: nowrap;">
                          <c:choose>
                            <c:when test="${guiUpgradeTask.unexpected}">&nbsp;</c:when>
                            <c:when test="${guiUpgradeTask.ddl}">${textContainer.text['configurationUpgradeTasksTypeDdl'] }</c:when>
                            <c:otherwise>${textContainer.text['configurationUpgradeTasksTypeData'] }</c:otherwise>
                          </c:choose>
                        </td>
                        <%-- grouper release the task was introduced in --%>
                        <td style="vertical-align: top; white-space: nowrap;">${grouper:escapeHtml(guiUpgradeTask.releasedInVersion)}</td>
                        <%-- color coded status badge --%>
                        <td style="vertical-align: top; white-space: nowrap;">
                          <span style="color: ${guiUpgradeTask.statusColor}; font-weight: bold;">${textContainer.text[guiUpgradeTask.statusLabelKey] }</span>
                        </td>
                        <%-- optional detail (e.g. result of a check status) --%>
                        <td style="vertical-align: top;">${grouper:escapeHtml(guiUpgradeTask.detail)}</td>
                      </tr>
                    </c:forEach>
                  </tbody>
                </table>
              </div>
            </div>
            </c:if>
