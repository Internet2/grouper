<%@ include file="../assetsJsp/commonTaglib.jsp"%>

${grouper:title('grouperReportAllConfigsPageTitle')}

<div class="bread-header-container">
  <ul class="breadcrumb">
    <li><a href="?operation=UiV2Main.indexMain" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Main.indexMain');">${textContainer.text['myServicesHomeBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
    <li><a href="?operation=UiV2Main.miscellaneous" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
    <li class="active">${textContainer.text['grouperReportAllConfigsBreadcrumb'] }</li>
  </ul>
  <div class="page-header blue-gradient">
    <h1>${textContainer.text['grouperReportAllConfigsTitle'] }</h1>
    <p style="margin-top: -1em; margin-bottom: 1em">${textContainer.text['grouperReportAllConfigsSubtitle'] }</p>
  </div>
</div>

<div class="row-fluid">
  <div class="span12">
    <div id="messages"></div>

    <c:choose>
      <c:when test="${fn:length(grouperRequestContainer.grouperReportContainer.guiReportConfigsOverall) > 0}">

        <table class="table table-hover table-bordered table-striped table-condensed data-table">
          <thead>
            <tr>
              <th>${textContainer.text['grouperReportAllConfigsTableHeaderOwner']}</th>
              <th>${textContainer.text['grouperReportAllConfigsTableHeaderReportType']}</th>
              <th>${textContainer.text['grouperReportConfigTableHeaderReportName']}</th>
              <th>${textContainer.text['grouperReportConfigTableHeaderReportEnabled']}</th>
              <th>${textContainer.text['grouperReportConfigTableHeaderReportLastRunTime']}</th>
              <th>${textContainer.text['grouperReportConfigTableHeaderReportStatus']}</th>
              <th>${textContainer.text['grouperReportConfigTableHeaderReportNumberOfRows']}</th>
              <th>${textContainer.text['grouperReportConfigTableHeaderReportCronSchedule']}</th>
              <th>${textContainer.text['grouperReportConfigTableHeaderReportActions']}</th>
            </tr>
          </thead>
          <tbody>
            <c:set var="i" value="0" />
            <c:forEach items="${grouperRequestContainer.grouperReportContainer.guiReportConfigsOverall}" var="guiReportConfigOverall">
              <tr>
                <td style="white-space: nowrap;">
                  <c:choose>
                    <c:when test="${guiReportConfigOverall.groupOwner}">
                      ${guiReportConfigOverall.guiOwnerGroup.shortLinkWithIcon}
                    </c:when>
                    <c:otherwise>
                      ${guiReportConfigOverall.guiOwnerStem.shortLinkWithIcon}
                    </c:otherwise>
                  </c:choose>
                </td>
                <td style="white-space: nowrap;">
                  ${grouper:escapeHtml(guiReportConfigOverall.reportConfigBean.reportConfigType)}
                </td>
                <td style="white-space: nowrap;">
                  <c:choose>
                    <c:when test="${guiReportConfigOverall.groupOwner}">
                      <a href="?operation=UiV2GrouperReport.viewAllReportInstancesForGroup&attributeAssignmentMarkerId=${grouper:escapeUrl(guiReportConfigOverall.reportConfigBean.attributeAssignmentMarkerId)}&groupId=${grouper:escapeUrl(guiReportConfigOverall.ownerGroup.id)}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2GrouperReport.viewAllReportInstancesForGroup&attributeAssignmentMarkerId=${grouper:escapeUrl(guiReportConfigOverall.reportConfigBean.attributeAssignmentMarkerId)}&groupId=${grouper:escapeUrl(guiReportConfigOverall.ownerGroup.id)}');">
                        ${grouper:escapeHtml(guiReportConfigOverall.reportConfigBean.reportConfigName)}</a>
                    </c:when>
                    <c:otherwise>
                      <a href="?operation=UiV2GrouperReport.viewAllReportInstancesForFolder&attributeAssignmentMarkerId=${grouper:escapeUrl(guiReportConfigOverall.reportConfigBean.attributeAssignmentMarkerId)}&stemId=${grouper:escapeUrl(guiReportConfigOverall.ownerStem.id)}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2GrouperReport.viewAllReportInstancesForFolder&attributeAssignmentMarkerId=${grouper:escapeUrl(guiReportConfigOverall.reportConfigBean.attributeAssignmentMarkerId)}&stemId=${grouper:escapeUrl(guiReportConfigOverall.ownerStem.id)}');">
                        ${grouper:escapeHtml(guiReportConfigOverall.reportConfigBean.reportConfigName)}</a>
                    </c:otherwise>
                  </c:choose>
                </td>
                <td style="white-space: nowrap;">
                  <c:choose>
                    <c:when test="${guiReportConfigOverall.reportConfigBean.reportConfigEnabled}">
                      ${textContainer.text['grouperReportConfigTableReportEnabledTrueValue']}
                    </c:when>
                    <c:otherwise>
                      ${textContainer.text['grouperReportConfigTableReportEnabledFalseValue']}
                    </c:otherwise>
                  </c:choose>
                </td>
                <td style="white-space: nowrap;">
                  <c:choose>
                    <c:when test="${not empty guiReportConfigOverall.lastRunTime}">
                      ${grouper:escapeHtml(guiReportConfigOverall.lastRunTime)}
                    </c:when>
                    <c:otherwise>
                      ${textContainer.text['grouperReportInstanceNeverRun']}
                    </c:otherwise>
                  </c:choose>
                </td>
                <td style="white-space: nowrap;">
                  <c:choose>
                    <c:when test="${not empty guiReportConfigOverall.mostRecentReportInstance}">
                      ${grouper:escapeHtml(guiReportConfigOverall.mostRecentReportInstance.reportInstanceStatus)}
                    </c:when>
                    <c:otherwise>
                      ${textContainer.text['grouperReportInstanceNeverRun']}
                    </c:otherwise>
                  </c:choose>
                </td>
                <td style="white-space: nowrap;">
                  <c:choose>
                    <c:when test="${not empty guiReportConfigOverall.mostRecentReportInstance}">
                      ${grouper:escapeHtml(guiReportConfigOverall.mostRecentReportInstance.reportInstanceSizeFriendly)}
                    </c:when>
                    <c:otherwise>
                      ${textContainer.text['grouperReportInstanceNeverRun']}
                    </c:otherwise>
                  </c:choose>
                </td>
                <td style="white-space: nowrap;">
                  ${grouper:escapeHtml(guiReportConfigOverall.reportConfigBean.reportConfigQuartzCron)}
                  <br/>
                  ${grouper:escapeHtml(guiReportConfigOverall.userFriendlyCron)}
                </td>
                <td>
                  <div class="btn-group">
                    <button type="button" data-toggle="dropdown" aria-label="${textContainer.text['ariaLabelGuiMoreOptions']}" class="btn btn-mini dropdown-toggle"
                      aria-haspopup="true" aria-expanded="false" onclick="$('#more-options${i}').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#more-options${i} li').first().focus();return true;});">
                      ${textContainer.text['stemViewActionsButton'] }
                      <span class="caret"></span>
                    </button>
                    <ul class="dropdown-menu dropdown-menu-right" id="more-options${i}">
                      <c:choose>
                        <c:when test="${guiReportConfigOverall.groupOwner}">
                          <li><a href="?operation=UiV2GrouperReport.viewReportConfigsOnGroup&groupId=${grouper:escapeUrl(guiReportConfigOverall.ownerGroup.id)}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2GrouperReport.viewReportConfigsOnGroup&groupId=${grouper:escapeUrl(guiReportConfigOverall.ownerGroup.id)}');">${textContainer.text['grouperReportAllConfigsActionsViewOnGroup'] }</a></li>
                        </c:when>
                        <c:otherwise>
                          <li><a href="?operation=UiV2GrouperReport.viewReportConfigsOnFolder&stemId=${grouper:escapeUrl(guiReportConfigOverall.ownerStem.id)}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2GrouperReport.viewReportConfigsOnFolder&stemId=${grouper:escapeUrl(guiReportConfigOverall.ownerStem.id)}');">${textContainer.text['grouperReportAllConfigsActionsViewOnFolder'] }</a></li>
                        </c:otherwise>
                      </c:choose>

                      <c:if test="${grouperRequestContainer.adminContainer.daemonJobsShow}">
                        <li><a href="?operation=UiV2Admin.viewLogs&jobName=grouper_report_${grouper:escapeUrl(guiReportConfigOverall.ownerId)}_${grouper:escapeUrl(guiReportConfigOverall.reportConfigBean.attributeAssignmentMarkerId)}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Admin.viewLogs&jobName=grouper_report_${grouper:escapeUrl(guiReportConfigOverall.ownerId)}_${grouper:escapeUrl(guiReportConfigOverall.reportConfigBean.attributeAssignmentMarkerId)}');">${textContainer.text['grouperReportConfigTableReportActionsReportLogs'] }</a></li>
                      </c:if>

                      <c:if test="${not empty guiReportConfigOverall.mostRecentReportInstance}">
                        <c:choose>
                          <c:when test="${guiReportConfigOverall.groupOwner}">
                            <li><a href="../app/UiV2GrouperReport.downloadReportForGroup?attributeAssignId=${grouper:escapeUrl(guiReportConfigOverall.mostRecentReportInstance.attributeAssignId)}&groupId=${grouper:escapeUrl(guiReportConfigOverall.ownerGroup.id)}">${textContainer.text['grouperReportConfigTableReportActionsDownloadMostRecent'] }</a></li>
                            <li><a href="?operation=UiV2GrouperReport.viewReportInstanceDetailsForGroup&attributeAssignId=${grouper:escapeUrl(guiReportConfigOverall.mostRecentReportInstance.attributeAssignId)}&groupId=${grouper:escapeUrl(guiReportConfigOverall.ownerGroup.id)}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2GrouperReport.viewReportInstanceDetailsForGroup&attributeAssignId=${grouper:escapeUrl(guiReportConfigOverall.mostRecentReportInstance.attributeAssignId)}&groupId=${grouper:escapeUrl(guiReportConfigOverall.ownerGroup.id)}');">${textContainer.text['grouperReportConfigTableReportActionsViewMostRecent'] }</a></li>
                          </c:when>
                          <c:otherwise>
                            <li><a href="../app/UiV2GrouperReport.downloadReportForFolder?attributeAssignId=${grouper:escapeUrl(guiReportConfigOverall.mostRecentReportInstance.attributeAssignId)}&stemId=${grouper:escapeUrl(guiReportConfigOverall.ownerStem.id)}">${textContainer.text['grouperReportConfigTableReportActionsDownloadMostRecent'] }</a></li>
                            <li><a href="?operation=UiV2GrouperReport.viewReportInstanceDetailsForFolder&attributeAssignId=${grouper:escapeUrl(guiReportConfigOverall.mostRecentReportInstance.attributeAssignId)}&stemId=${grouper:escapeUrl(guiReportConfigOverall.ownerStem.id)}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2GrouperReport.viewReportInstanceDetailsForFolder&attributeAssignId=${grouper:escapeUrl(guiReportConfigOverall.mostRecentReportInstance.attributeAssignId)}&stemId=${grouper:escapeUrl(guiReportConfigOverall.ownerStem.id)}');">${textContainer.text['grouperReportConfigTableReportActionsViewMostRecent'] }</a></li>
                          </c:otherwise>
                        </c:choose>
                      </c:if>
                    </ul>
                  </div>
                </td>
              </tr>
              <c:set var="i" value="${i + 1}" />
            </c:forEach>
          </tbody>
        </table>

      </c:when>
      <c:otherwise>
        <div class="row-fluid">
          <div class="span9"><p><b>${textContainer.text['grouperReportAllConfigsNoneFound'] }</b></p></div>
        </div>
      </c:otherwise>
    </c:choose>

  </div>
</div>
