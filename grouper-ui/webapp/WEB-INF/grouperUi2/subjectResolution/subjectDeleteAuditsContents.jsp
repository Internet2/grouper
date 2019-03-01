<%@ include file="../assetsJsp/commonTaglib.jsp"%>

 <table class="table table-hover table-bordered table-striped table-condensed data-table table-bulk-update footable">
                    <thead>
                      <tr>
                      <th>${textContainer.text['subjectResolutionSubjectDeleteAuditLogFilterColumnDate']}</th>
                      <th>${textContainer.text['subjectResolutionSubjectDeleteLogFilterColumnActor']}</th>
                      <th>${textContainer.text['subjectResolutionSubjectDeleteLogFilterColumnEngine']}</th>
                      <th>${textContainer.text['subjectResolutionSubjectDeleteLogFilterColumnSummary']}</th>
                      <c:if test="${grouperRequestContainer.groupContainer.auditExtendedResults }" >
                        <th>${textContainer.text['groupAuditLogFilterColumnDuration']}</th>
                        <th>${textContainer.text['groupAuditLogFilterColumnQueryCount']}</th>
                        <th>${textContainer.text['groupAuditLogFilterColumnServerUsername']}</th>
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
                        <td style="white-space: nowrap">${guiAuditEntry.guiSubjectPerformedAction.shortLinkWithIcon}</td>
                        <td>${guiAuditEntry.grouperEngineLabel}</td>
                        <td>${guiAuditEntry.auditLine }</td>
                        <c:if test="${grouperRequestContainer.groupContainer.auditExtendedResults }" >
                          <td>${guiAuditEntry.durationLabel }</td>
                          <td>${auditEntry.queryCount}</td>
                          <td>${auditEntry.serverUserName }</td>
                          <td>${auditEntry.serverHost }</td>
                          <td>${auditEntry.userIpAddress }</td>
                          <td>${auditEntry.id }</td>
                          <td>${auditEntry.description }</td>
                        </c:if>
                      </tr>
                    </c:forEach>
                    </tbody>
                  </table>
          <div class="data-table-bottom gradient-background">
            <grouper:paging2 guiPaging="${grouperRequestContainer.subjectResolutionContainer.guiPaging}" formName="unresolvedSubjectsPagingForm" ajaxFormIds="subjectDeleteAuditFormId"
              refreshOperation="../app/UiV2SubjectResolution.viewSubjectDeleteAudits" />
          </div>