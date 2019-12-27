<%@ include file="../assetsJsp/commonTaglib.jsp"%>

 <table class="table table-hover table-bordered table-striped table-condensed data-table table-bulk-update footable">
                    <thead>
                      <tr>
                      <th style="width: 120px;">${textContainer.text['subjectResolutionSubjectDeleteAuditLogFilterColumnDate']}</th>
                      <th>${textContainer.text['subjectResolutionSubjectDeleteLogFilterColumnEngine']}</th>
                      <th>${textContainer.text['subjectResolutionSubjectDeleteLogFilterColumnSummary']}</th>
                      <th>${textContainer.text['groupAuditLogFilterColumnUser']}</th>
                      <c:if test="${grouperRequestContainer.subjectResolutionContainer.auditExtendedResults }" >
                        <th>${textContainer.text['groupAuditLogFilterColumnDuration']}</th>
                        <th>${textContainer.text['groupAuditLogFilterColumnQueryCount']}</th>
                        <th>${textContainer.text['groupAuditLogFilterColumnServer']}</th>
                        <th>${textContainer.text['groupAuditLogFilterColumnUserIpAddress']}</th>
                        <th>${textContainer.text['groupAuditLogFilterColumnEntryId']}</th>
                        <th>${textContainer.text['groupAuditLogFilterColumnRawDescription']}</th>
                      </c:if>
                    </tr>
                      
                    </thead>
                    <tbody>
                      <c:forEach items="${grouperRequestContainer.subjectResolutionContainer.guiAuditEntries}" 
                      var="guiAuditEntry" >
                      <c:set var="auditEntry" value="${guiAuditEntry.auditEntry}" />
                      <tr>
                        <td>${guiAuditEntry.guiDate}</td>
                        <td>${guiAuditEntry.grouperEngineLabel}</td>
                        <td>${guiAuditEntry.auditLine }</td>
                        <td style="white-space: nowrap">${guiAuditEntry.guiSubjectPerformedAction.shortLinkWithIcon}</td>
                        <c:if test="${grouperRequestContainer.subjectResolutionContainer.auditExtendedResults }" >
                          <td>${guiAuditEntry.durationLabel }</td>
                          <td>${auditEntry.queryCount}</td>
                          <td>${auditEntry.serverHost }</td>
                          <td>${auditEntry.userIpAddress }</td>
                          <td>${auditEntry.id }</td>
                          <td style="white-space: nowrap">${auditEntry.description }</td>
                        </c:if>
                      </tr>
                    </c:forEach>
                    </tbody>
                  </table>
          <div class="data-table-bottom gradient-background">
            <grouper:paging2 guiPaging="${grouperRequestContainer.subjectResolutionContainer.guiPaging}" formName="unresolvedSubjectsPagingForm" ajaxFormIds="subjectDeleteAuditFormId"
              refreshOperation="../app/UiV2SubjectResolution.viewSubjectDeleteAudits" />
          </div>