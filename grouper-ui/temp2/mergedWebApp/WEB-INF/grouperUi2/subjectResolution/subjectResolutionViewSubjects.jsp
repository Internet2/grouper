<%@ include file="../assetsJsp/commonTaglib.jsp"%>

 <table class="table table-hover table-bordered table-striped table-condensed data-table table-bulk-update footable">
                    <thead>
                      <tr>
                        <th class="sorted" style="width: 200px;">${textContainer.text['subjectResolutionUnresolvedSubjectsTableHeaderSubjectName']}</th>
                        <th class="sorted">${textContainer.text['subjectResolutionSubjectsSearchTableHeaderIsSubjectResolvable']}</th>
                      </tr>
                    </thead>
                    <tbody>
                      <c:forEach items="${grouperRequestContainer.subjectResolutionContainer.subjectsWithStatus}"
                        var="subjectStatus" >
                        <tr>
                          <td>${subjectStatus.key}</td>
                          <td>${subjectStatus.value}</td>
                        </tr>
                      </c:forEach>
                    </tbody>
                  </table>
