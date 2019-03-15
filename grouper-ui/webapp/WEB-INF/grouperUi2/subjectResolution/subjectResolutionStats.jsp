<%@ include file="../assetsJsp/commonTaglib.jsp"%>

 <table class="table table-hover table-bordered table-striped table-condensed data-table table-bulk-update footable">
                    <thead>
                      <tr>
                        <th class="sorted" style="width: 200px;">${textContainer.text['subjectResolutionStatsTableHeaderSourceName']}</th>
                        <th class="sorted">${textContainer.text['subjectResolutionStatsTableHeaderUnresolvableCount']}</th>
                        <th class="sorted">${textContainer.text['subjectResolutionStatsTableHeaderResolvableCount']}</th>
                        <th class="sorted">${textContainer.text['subjectResolutionStatsTableHeaderDeletedCount']}</th>
                      </tr>
                    </thead>
                    <tbody>
                      <c:forEach items="${grouperRequestContainer.subjectResolutionContainer.subjectResolutionStats}" 
                        var="subjectResolutionStat" >
                        <tr>
                          <td>${subjectResolutionStat.source}</td>
                          <td>${subjectResolutionStat.unresolvedCount}</td>
                          <td>${subjectResolutionStat.resolvedCount}</td>
                          <td>${subjectResolutionStat.deletedCount}</td>
                        </tr>
                      </c:forEach>
                    </tbody>
                  </table>