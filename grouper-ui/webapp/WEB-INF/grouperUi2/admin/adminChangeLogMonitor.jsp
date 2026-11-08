<%@ include file="../assetsJsp/commonTaglib.jsp"%>
${grouper:title('changeLogMonitorTitle')}

<script src="../../grouperExternal/public/assets/js/grouperChangeLogMonitorChart.js" type="text/javascript"></script>

<%-- GRP-7510: Grouper sysadmins only, consumer names, group names and subjects are escaped --%>
<c:set var="changeLogMonitorContainer" value="${grouperRequestContainer.changeLogMonitorContainer}" />

<div class="bread-header-container">
  <ul class="breadcrumb">
    <li><a href="?operation=UiV2Main.indexMain" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Main.indexMain');">${textContainer.text['myServicesHomeBreadcrumb'] }</a><span
      class="divider"><i class='fa fa-angle-right'></i></span></li>
    <li><a href="?operation=UiV2Main.miscellaneous" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span
      class="divider"><i class='fa fa-angle-right'></i></span></li>
    <li class="active">${textContainer.text['changeLogMonitorBreadcrumb'] }</li>
  </ul>

  <div class="page-header blue-gradient">
    <div class="row-fluid">
      <div class="span10 pull-left">
        <h1>${textContainer.text['changeLogMonitorTitle'] }</h1>
        <p style="margin-top: -1em; margin-bottom: 1em">${textContainer.text['changeLogMonitorDescription'] }</p>
      </div>
    </div>
  </div>
</div>

<div class="row-fluid">
  <div class="span12">
    <form id="changeLogMonitorFormId" class="form-inline form-small form-filter"
      onsubmit="ajax('../app/UiV2ChangeLogMonitor.changeLogMonitor', {formIds: 'changeLogMonitorFormId'}); return false;">
      <label for="changeLogMonitorRangeHoursId">${textContainer.text['changeLogMonitorRangeLabel'] }</label>&nbsp;
      <select id="changeLogMonitorRangeHoursId" name="changeLogMonitorRangeHours" class="span2">
        <option value="1" ${changeLogMonitorContainer.rangeHours == 1 ? 'selected="selected"' : ''}>${textContainer.text['changeLogMonitorRange1'] }</option>
        <option value="6" ${changeLogMonitorContainer.rangeHours == 6 ? 'selected="selected"' : ''}>${textContainer.text['changeLogMonitorRange6'] }</option>
        <option value="24" ${changeLogMonitorContainer.rangeHours == 24 ? 'selected="selected"' : ''}>${textContainer.text['changeLogMonitorRange24'] }</option>
        <option value="72" ${changeLogMonitorContainer.rangeHours == 72 ? 'selected="selected"' : ''}>${textContainer.text['changeLogMonitorRange72'] }</option>
        <option value="168" ${changeLogMonitorContainer.rangeHours == 168 ? 'selected="selected"' : ''}>${textContainer.text['changeLogMonitorRange168'] }</option>
      </select>
      &nbsp;
      <button type="submit" class="btn btn-primary" id="changeLogMonitorRefreshButtonId">${textContainer.text['changeLogMonitorRefreshButton'] }</button>
      &nbsp;
      <span class="description">${textContainer.text['changeLogMonitorAsOf'] } ${grouper:escapeHtml(changeLogMonitorContainer.asOf)}</span>
    </form>
  </div>
</div>

<%-- temp change log --%>
<div class="row-fluid">
  <div class="span12">
    <h2 class="grouper-heading-as-h4">${textContainer.text['changeLogMonitorTempHeader'] }</h2>
    <p>${textContainer.text['changeLogMonitorTempDescription'] }</p>
    <table class="table table-condensed table-striped table-bordered">
      <tbody>
        <tr>
          <th style="width: 30%; white-space: nowrap;">${textContainer.text['changeLogMonitorPendingCount'] }</th>
          <td>${changeLogMonitorContainer.formatNumber(changeLogMonitorContainer.pendingCount)}</td>
        </tr>
        <tr>
          <th style="white-space: nowrap;">${textContainer.text['changeLogMonitorOldestPending'] }</th>
          <td>
            <c:choose>
              <c:when test="${empty changeLogMonitorContainer.oldestPendingDate}">${textContainer.text['changeLogMonitorNothingPending'] }</c:when>
              <c:otherwise>
                ${grouper:escapeHtml(changeLogMonitorContainer.oldestPendingAge)} ${textContainer.text['changeLogMonitorAgo'] }
                <span style="color: #767676;">(${grouper:escapeHtml(changeLogMonitorContainer.oldestPendingDate)})</span>
              </c:otherwise>
            </c:choose>
          </td>
        </tr>
        <tr>
          <th style="white-space: nowrap;">${textContainer.text['changeLogMonitorMovedLastHour'] }</th>
          <td>${changeLogMonitorContainer.formatNumber(changeLogMonitorContainer.movedLastHour)}
            <span style="color: #767676;">(${changeLogMonitorContainer.formatNumber(changeLogMonitorContainer.movedPerMinute)} ${textContainer.text['changeLogMonitorPerMinute'] })</span></td>
        </tr>
        <tr>
          <th style="white-space: nowrap;">${textContainer.text['changeLogMonitorEstimatedTimeToEmpty'] }</th>
          <td>
            <c:choose>
              <c:when test="${changeLogMonitorContainer.pendingCount == 0}">${textContainer.text['changeLogMonitorNothingPending'] }</c:when>
              <c:when test="${empty changeLogMonitorContainer.estimatedTimeToEmpty}"><span style="color: #b94a48;">${textContainer.text['changeLogMonitorNotMoving'] }</span></c:when>
              <c:otherwise>${grouper:escapeHtml(changeLogMonitorContainer.estimatedTimeToEmpty)}
                <br /><span class="description">${textContainer.text['changeLogMonitorEstimatedTimeToEmptyHint'] }</span></c:otherwise>
            </c:choose>
          </td>
        </tr>
        <tr>
          <th style="white-space: nowrap;">${textContainer.text['changeLogMonitorTempJobLastRun'] }</th>
          <td>
            <c:set var="jobRun" value="${changeLogMonitorContainer.tempToChangeLogLastRun}" />
            <c:choose>
              <c:when test="${jobRun == null}">${textContainer.text['changeLogMonitorNeverRun'] }</c:when>
              <c:otherwise>
                <span style="${jobRun.error ? 'color: #b94a48; font-weight: bold;' : ''}">${grouper:escapeHtml(jobRun.status)}</span>
                ${textContainer.text['changeLogMonitorOnHost'] } ${grouper:escapeHtml(jobRun.host)},
                ${grouper:escapeHtml(jobRun.lastUpdatedAge)} ${textContainer.text['changeLogMonitorAgo'] }
                <span style="color: #767676;">(${grouper:escapeHtml(jobRun.lastUpdated)})</span>
              </c:otherwise>
            </c:choose>
            &nbsp;<a href="?operation=UiV2Admin.viewLogs&jobName=CHANGE_LOG_changeLogTempToChangeLog"
              onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Admin.viewLogs&jobName=CHANGE_LOG_changeLogTempToChangeLog');">${textContainer.text['changeLogMonitorViewLogs'] }</a>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</div>

<%-- change log and consumers --%>
<div class="row-fluid">
  <div class="span12">
    <h2 class="grouper-heading-as-h4">${textContainer.text['changeLogMonitorConsumersHeader'] }</h2>
    <p>${textContainer.text['changeLogMonitorConsumersDescription'] }</p>
    <table class="table table-condensed table-striped table-bordered">
      <tbody>
        <tr>
          <th style="width: 30%; white-space: nowrap;">${textContainer.text['changeLogMonitorMaxSequence'] }</th>
          <td>
            <c:choose>
              <c:when test="${changeLogMonitorContainer.maxSequenceNumber == null}">${textContainer.text['changeLogMonitorChangeLogEmpty'] }</c:when>
              <c:otherwise>${changeLogMonitorContainer.formatNumber(changeLogMonitorContainer.maxSequenceNumber)}</c:otherwise>
            </c:choose>
          </td>
        </tr>
        <tr>
          <th style="white-space: nowrap;">${textContainer.text['changeLogMonitorOldestEntry'] }</th>
          <td>
            <c:if test="${!empty changeLogMonitorContainer.oldestEntryDate}">${grouper:escapeHtml(changeLogMonitorContainer.oldestEntryDate)}</c:if>
            <span style="color: #767676;">(${textContainer.text['changeLogMonitorRetention'] }
              <c:choose>
                <c:when test="${changeLogMonitorContainer.changeLogRetentionDays == -1}">${textContainer.text['changeLogMonitorRetentionForever'] }</c:when>
                <c:otherwise>${changeLogMonitorContainer.changeLogRetentionDays} ${textContainer.text['changeLogMonitorDays'] }</c:otherwise>
              </c:choose>, loader.retain.db.change_log_entry.days)</span>
          </td>
        </tr>
      </tbody>
    </table>

    <c:choose>
      <c:when test="${empty changeLogMonitorContainer.consumers}">
        <p>${textContainer.text['changeLogMonitorNoConsumers'] }</p>
      </c:when>
      <c:otherwise>
        <table class="table table-hover table-bordered table-striped table-condensed data-table">
          <thead>
            <tr>
              <th>${textContainer.text['changeLogMonitorConsumerName'] }</th>
              <th style="text-align: right;">${textContainer.text['changeLogMonitorConsumerBehind'] }</th>
              <th>${textContainer.text['changeLogMonitorConsumerOldestUnprocessed'] }</th>
              <th>${textContainer.text['changeLogMonitorConsumerLastProgress'] }</th>
              <th>${textContainer.text['changeLogMonitorConsumerLastRun'] }</th>
            </tr>
          </thead>
          <tbody>
            <c:forEach items="${changeLogMonitorContainer.consumers}" var="consumer">
              <tr>
                <td>
                  <c:choose>
                    <c:when test="${consumer.configured}">
                      <a href="?operation=UiV2Admin.viewLogs&jobName=${grouper:escapeUrl(consumer.jobName)}"
                        onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Admin.viewLogs&jobName=${grouper:escapeUrl(consumer.jobName)}');">${grouper:escapeHtml(consumer.name)}</a>
                    </c:when>
                    <c:otherwise>
                      ${grouper:escapeHtml(consumer.name)}
                      <br /><span style="color: #767676;">${textContainer.text['changeLogMonitorConsumerNotConfigured'] }</span>
                    </c:otherwise>
                  </c:choose>
                </td>
                <td style="text-align: right;">${changeLogMonitorContainer.formatNumber(consumer.entriesBehind)}</td>
                <td>
                  <c:choose>
                    <c:when test="${!consumer.configured}"></c:when>
                    <c:when test="${consumer.entriesBehind == 0}">${textContainer.text['changeLogMonitorConsumerUpToDate'] }</c:when>
                    <c:when test="${!empty consumer.oldestUnprocessedAge}">${grouper:escapeHtml(consumer.oldestUnprocessedAge)} ${textContainer.text['changeLogMonitorAgo'] }</c:when>
                  </c:choose>
                  <c:if test="${consumer.entriesDeletedBeforeProcessed}">
                    <br /><span style="color: #b94a48;">${textContainer.text['changeLogMonitorConsumerEntriesDeleted'] }</span>
                  </c:if>
                </td>
                <td>${grouper:escapeHtml(consumer.lastProgress)}</td>
                <td>
                  <c:if test="${consumer.lastRun != null}">
                    <span style="${consumer.lastRun.error ? 'color: #b94a48; font-weight: bold;' : ''}">${grouper:escapeHtml(consumer.lastRun.status)}</span>,
                    ${grouper:escapeHtml(consumer.lastRun.lastUpdatedAge)} ${textContainer.text['changeLogMonitorAgo'] }
                  </c:if>
                </td>
              </tr>
            </c:forEach>
          </tbody>
        </table>
      </c:otherwise>
    </c:choose>
  </div>
</div>

<%-- graphs --%>
<div class="row-fluid">
  <div class="span12">
    <h2 class="grouper-heading-as-h4">${textContainer.text['changeLogMonitorChartsHeader'] }</h2>
    <p>${textContainer.text['changeLogMonitorChartsDescription'] }
      ${changeLogMonitorContainer.loaderLogRetentionDays} ${textContainer.text['changeLogMonitorDays'] } (loader.retain.db.logs.days).</p>

    <h3 class="grouper-heading-as-h4" style="font-size: 14px;">${textContainer.text['changeLogMonitorChartTempTitle'] } (${grouper:escapeHtml(changeLogMonitorContainer.bucketLabel)})</h3>
    <div id="changeLogMonitorTempChart" style="height: 300px" role="img"
      aria-label="${grouper:escapeHtml(textContainer.text['changeLogMonitorChartTempTitle'])}"></div>

    <h3 class="grouper-heading-as-h4" style="font-size: 14px;">${textContainer.text['changeLogMonitorChartConsumersTitle'] } (${grouper:escapeHtml(changeLogMonitorContainer.bucketLabel)})</h3>
    <c:choose>
      <c:when test="${changeLogMonitorContainer.consumerChartHasData}">
        <div id="changeLogMonitorConsumersChart" style="height: 350px" role="img"
          aria-label="${grouper:escapeHtml(textContainer.text['changeLogMonitorChartConsumersTitle'])}"></div>
      </c:when>
      <c:otherwise>
        <p>${textContainer.text['changeLogMonitorChartConsumersNone'] }</p>
      </c:otherwise>
    </c:choose>
  </div>
</div>

<%-- breakdown of the pending changes, only on request since it reads all of grouper_change_log_entry_temp --%>
<div class="row-fluid">
  <div class="span12">
    <h2 class="grouper-heading-as-h4">${textContainer.text['changeLogMonitorBreakdownHeader'] }</h2>
    <p>${textContainer.text['changeLogMonitorBreakdownDescription'] }</p>
    <div id="changeLogMonitorBreakdownDivId" role="region" aria-live="polite"
      aria-label="${grouper:escapeHtml(textContainer.text['changeLogMonitorBreakdownHeader'])}">
      <button type="button" class="btn" id="changeLogMonitorBreakdownButtonId" aria-controls="changeLogMonitorBreakdownDivId"
        onclick="ajax('../app/UiV2ChangeLogMonitor.changeLogMonitorBreakdown'); return false;">${textContainer.text['changeLogMonitorBreakdownButton'] }</button>
    </div>
  </div>
</div>
