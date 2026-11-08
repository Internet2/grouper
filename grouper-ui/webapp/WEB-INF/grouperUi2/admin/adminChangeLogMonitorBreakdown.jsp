<%@ include file="../assetsJsp/commonTaglib.jsp"%>

<%-- GRP-7510: group names, audit descriptions and subjects come from the database, so they are escaped --%>
<c:set var="changeLogMonitorContainer" value="${grouperRequestContainer.changeLogMonitorContainer}" />

<p>
  <button type="button" class="btn" id="changeLogMonitorBreakdownButtonId" aria-controls="changeLogMonitorBreakdownDivId"
    onclick="ajax('../app/UiV2ChangeLogMonitor.changeLogMonitorBreakdown'); return false;">${textContainer.text['changeLogMonitorBreakdownRefreshButton'] }</button>
  &nbsp;<span class="description">${textContainer.text['changeLogMonitorAsOf'] } ${grouper:escapeHtml(changeLogMonitorContainer.asOf)}</span>
</p>

<c:choose>
  <c:when test="${empty changeLogMonitorContainer.pendingByType}">
    <p>${textContainer.text['changeLogMonitorNothingPending'] }</p>
  </c:when>
  <c:otherwise>

    <h3 class="grouper-heading-as-h4" style="font-size: 14px;">${textContainer.text['changeLogMonitorByTypeHeader'] }</h3>
    <table class="table table-hover table-bordered table-striped table-condensed data-table">
      <thead>
        <tr>
          <th>${textContainer.text['changeLogMonitorByTypeCategory'] }</th>
          <th>${textContainer.text['changeLogMonitorByTypeAction'] }</th>
          <th style="text-align: right;">${textContainer.text['changeLogMonitorPendingColumn'] }</th>
          <th>${textContainer.text['changeLogMonitorOldestPending'] }</th>
        </tr>
      </thead>
      <tbody>
        <c:forEach items="${changeLogMonitorContainer.pendingByType}" var="pendingType">
          <tr>
            <td>${grouper:escapeHtml(pendingType.category)}</td>
            <td>${grouper:escapeHtml(pendingType.action)}</td>
            <td style="text-align: right;">${changeLogMonitorContainer.formatNumber(pendingType.count)}</td>
            <td><c:if test="${!empty pendingType.oldestAge}">${grouper:escapeHtml(pendingType.oldestAge)} ${textContainer.text['changeLogMonitorAgo'] }</c:if></td>
          </tr>
        </c:forEach>
      </tbody>
    </table>

    <h3 class="grouper-heading-as-h4" style="font-size: 14px;">${textContainer.text['changeLogMonitorByOwnerHeader'] }</h3>
    <c:choose>
      <c:when test="${empty changeLogMonitorContainer.pendingByOwner}">
        <p>${textContainer.text['changeLogMonitorByOwnerNone'] }</p>
      </c:when>
      <c:otherwise>
        <table class="table table-hover table-bordered table-striped table-condensed data-table">
          <thead>
            <tr>
              <th>${textContainer.text['changeLogMonitorByOwnerName'] }</th>
              <th style="text-align: right;">${textContainer.text['changeLogMonitorByOwnerMemberships'] }</th>
              <th style="text-align: right;">${textContainer.text['changeLogMonitorByOwnerPrivileges'] }</th>
            </tr>
          </thead>
          <tbody>
            <c:forEach items="${changeLogMonitorContainer.pendingByOwner}" var="pendingOwner">
              <tr>
                <td>${grouper:escapeHtml(pendingOwner.name)}</td>
                <td style="text-align: right;">${changeLogMonitorContainer.formatNumber(pendingOwner.membershipCount)}</td>
                <td style="text-align: right;">${changeLogMonitorContainer.formatNumber(pendingOwner.privilegeCount)}</td>
              </tr>
            </c:forEach>
          </tbody>
        </table>
        <c:if test="${changeLogMonitorContainer.pendingByOwnerTruncated}">
          <p style="color: #767676;">${textContainer.text['changeLogMonitorByOwnerTruncated'] }</p>
        </c:if>
      </c:otherwise>
    </c:choose>

    <h3 class="grouper-heading-as-h4" style="font-size: 14px;">${textContainer.text['changeLogMonitorBySourceHeader'] }</h3>
    <p class="description">${textContainer.text['changeLogMonitorBySourceDescription'] }</p>
    <table class="table table-hover table-bordered table-striped table-condensed data-table">
      <thead>
        <tr>
          <th style="text-align: right;">${textContainer.text['changeLogMonitorPendingColumn'] }</th>
          <th>${textContainer.text['changeLogMonitorBySourceFirstPending'] }</th>
          <th>${textContainer.text['changeLogMonitorBySourceAction'] }</th>
          <th>${textContainer.text['changeLogMonitorBySourceWho'] }</th>
          <th>${textContainer.text['changeLogMonitorBySourceDescriptionColumn'] }</th>
        </tr>
      </thead>
      <tbody>
        <c:forEach items="${changeLogMonitorContainer.pendingBySource}" var="pendingSource">
          <tr>
            <td style="text-align: right;">${changeLogMonitorContainer.formatNumber(pendingSource.count)}</td>
            <td><c:if test="${!empty pendingSource.firstPendingAge}">${grouper:escapeHtml(pendingSource.firstPendingAge)} ${textContainer.text['changeLogMonitorAgo'] }</c:if></td>
            <td>
              <c:choose>
                <c:when test="${empty pendingSource.contextId}">${textContainer.text['changeLogMonitorBySourceNoContext'] }</c:when>
                <c:when test="${empty pendingSource.auditAction}">${textContainer.text['changeLogMonitorBySourceNotAudited'] }</c:when>
                <c:otherwise>${grouper:escapeHtml(pendingSource.auditAction)}</c:otherwise>
              </c:choose>
              <c:if test="${!empty pendingSource.contextId}">
                <br /><span style="color: #767676;">${textContainer.text['changeLogMonitorBySourceContextId'] } ${grouper:escapeHtml(pendingSource.contextId)}</span>
              </c:if>
            </td>
            <td>
              ${grouper:escapeHtml(pendingSource.loggedInSubject)}
              <c:if test="${!empty pendingSource.engine || !empty pendingSource.serverHost}">
                <br /><span style="color: #767676;">${grouper:escapeHtml(pendingSource.engine)} ${grouper:escapeHtml(pendingSource.serverHost)}</span>
              </c:if>
            </td>
            <td>${grouper:escapeHtml(pendingSource.description)}</td>
          </tr>
        </c:forEach>
      </tbody>
    </table>
    <c:if test="${changeLogMonitorContainer.pendingBySourceTruncated}">
      <p style="color: #767676;">${textContainer.text['changeLogMonitorBySourceTruncated'] }</p>
    </c:if>

  </c:otherwise>
</c:choose>
