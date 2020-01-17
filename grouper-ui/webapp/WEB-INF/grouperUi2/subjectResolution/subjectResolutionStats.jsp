<%@ include file="../assetsJsp/commonTaglib.jsp"%>

 <table class="table table-hover table-bordered table-striped table-condensed data-table table-bulk-update footable">
                    <thead>
                      <tr>
                        <th data-hide="phone" style="white-space: nowrap;" style="width: 200px;">${textContainer.text['subjectResolutionStatsTableHeaderSourceName']}</th>
                        <th data-hide="phone" style="white-space: nowrap;">${textContainer.text['subjectResolutionStatsTableHeaderUnresolvableCount']}</th>
                        <th data-hide="phone" style="white-space: nowrap;">${textContainer.text['subjectResolutionStatsTableHeaderResolvableCount']}</th>
                        <th data-hide="phone" style="white-space: nowrap;">${textContainer.text['subjectResolutionStatsTableHeaderDeletedCount']}</th>
                      </tr>
                    </thead>
                    <tbody>
                      <c:forEach items="${grouperRequestContainer.subjectResolutionContainer.subjectResolutionStats}" 
                        var="subjectResolutionStat" >
                        <tr>
                          <td style="white-space: nowrap;">${subjectResolutionStat.source}</td>
                          <td>${subjectResolutionStat.unresolvedCount}</td>
                          <td>${subjectResolutionStat.resolvedCount}</td>
                          <td>${subjectResolutionStat.deletedCount}</td>
                        </tr>
                      </c:forEach>
                    </tbody>
                  </table>