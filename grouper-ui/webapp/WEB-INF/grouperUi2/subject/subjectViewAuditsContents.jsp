<%@ include file="../assetsJsp/commonTaglib.jsp"%>

                <%-- tell add member to refresh audits --%>
                <form id="subjectRefreshPartFormId">
                  <input type="hidden" name="groupRefreshPart" value="audits" /> 
                </form> 
                <form id="subjectQuerySortAscendingFormId">
                  <input type="hidden" name="querySortAscending" value="${grouperRequestContainer.subjectContainer.guiSorting.ascending}" /> 
                </form> 

                <table class="table table-hover table-bordered table-striped table-condensed data-table">
                  <thead>
                    <tr>
                      <th class="${grouperRequestContainer.subjectContainer.guiSorting.columnCssClass['lastUpdatedDb']}"
                         onclick="ajax('../app/UiV2Subject.viewAuditsFilter?subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&querySortAscending=${!grouperRequestContainer.subjectContainer.guiSorting.ascending}', {formIds: 'subjectFilterAuditFormId,subjectPagingAuditFormId'}); return false;"
                         >${textContainer.text['subjectAuditLogFilterColumnDate']}</th>
                      <th>${textContainer.text['subjectAuditLogFilterColumnActor']}</th>
                      <th>${textContainer.text['subjectAuditLogFilterColumnEngine']}</th>
                      <th>${textContainer.text['subjectAuditLogFilterColumnSummary']}</th>
                      <c:if test="${grouperRequestContainer.subjectContainer.auditExtendedResults }" >
                        <th>${textContainer.text['subjectAuditLogFilterColumnDuration']}</th>
                        <th>${textContainer.text['subjectAuditLogFilterColumnQueryCount']}</th>
                        <th>${textContainer.text['subjectAuditLogFilterColumnServerUsername']}</th>
                        <th>${textContainer.text['subjectAuditLogFilterColumnServer']}</th>
                        <th>${textContainer.text['subjectAuditLogFilterColumnUserIpAddress']}</th>
                        <th>${textContainer.text['subjectAuditLogFilterColumnEntryId']}</th>
                        <th>${textContainer.text['subjectAuditLogFilterColumnRawDescription']}</th>
                      </c:if>
                    </tr>
                  </thead>
                  <tbody>
                    <c:forEach items="${grouperRequestContainer.subjectContainer.guiAuditEntries}" 
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
                  <grouper:paging2 guiPaging="${grouperRequestContainer.subjectContainer.guiPaging}" formName="subjectPagingAuditForm" ajaxFormIds="subjectFilterAuditFormId, subjectQuerySortAscendingFormId"
                    refreshOperation="../app/UiV2Subject.viewAuditsFilter?subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}" />
                </div>


