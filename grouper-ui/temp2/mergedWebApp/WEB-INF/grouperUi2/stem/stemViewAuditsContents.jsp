<%@ include file="../assetsJsp/commonTaglib.jsp"%>

                <%-- tell add member to refresh audits --%>
                <form id="stemRefreshPartFormId">
                  <input type="hidden" name="stemRefreshPart" value="audits" /> 
                </form> 
                <form id="stemQuerySortAscendingFormId">
                  <input type="hidden" name="querySortAscending" value="${grouperRequestContainer.stemContainer.guiSorting.ascending}" /> 
                </form> 

                <table class="table table-hover table-bordered table-striped table-condensed data-table">
                  <thead>
                    <tr>
                      <th class="${grouperRequestContainer.stemContainer.guiSorting.columnCssClass['lastUpdatedDb']}"
                         onclick="ajax('../app/UiV2Stem.viewAuditsFilter?stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}&querySortAscending=${!grouperRequestContainer.stemContainer.guiSorting.ascending}', {formIds: 'stemFilterAuditFormId,stemPagingAuditFormId'}); return false;"
                         >${textContainer.text['groupAuditLogFilterColumnDate']}</th>
                      <th>${textContainer.text['groupAuditLogFilterColumnActor']}</th>
                      <th>${textContainer.text['groupAuditLogFilterColumnEngine']}</th>
                      <th>${textContainer.text['groupAuditLogFilterColumnSummary']}</th>
                      <c:if test="${grouperRequestContainer.stemContainer.auditExtendedResults }" >
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
                    <c:forEach items="${grouperRequestContainer.stemContainer.guiAuditEntries}" 
                      var="guiAuditEntry" >
                      <c:set var="auditEntry" value="${guiAuditEntry.auditEntry}" />
                      <%--
                      <tr>
                        <td>18 Dec 2012 12:30:45</td>
                        <td><a href="#">Jane Smith</td>
                        <td>grouperUI</td>
                        <td><strong>Assigned membership</strong><br /><a href="#">Bob Smith</a> was added as an <em>immediate member</em> to the <em>members list</em> of <a href="#">Editors.</td>
                      </tr>
                      --%>
                      <tr>
                        <td>${guiAuditEntry.guiDate}</td>
                        <td style="white-space: nowrap">${guiAuditEntry.guiSubjectPerformedAction.shortLinkWithIcon}</td>
                        <td>${guiAuditEntry.grouperEngineLabel}</td>
                        <td>${guiAuditEntry.auditLine }</td>
                        <c:if test="${grouperRequestContainer.stemContainer.auditExtendedResults }" >
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
                  <grouper:paging2 guiPaging="${grouperRequestContainer.stemContainer.guiPaging}" formName="stemPagingAuditForm" ajaxFormIds="stemFilterAuditFormId, stemQuerySortAscendingFormId"
                    refreshOperation="../app/UiV2Stem.viewAuditsFilter?stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}" />
                </div>


