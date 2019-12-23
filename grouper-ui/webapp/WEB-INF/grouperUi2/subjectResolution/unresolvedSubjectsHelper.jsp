<%@ include file="../assetsJsp/commonTaglib.jsp"%>

 <table class="table table-hover table-bordered table-striped table-condensed data-table table-bulk-update footable">
                    <thead>
                      <tr>
                        <th class="sorted">${textContainer.text['subjectResolutionUnresolvedSubjectsTableHeaderSubjectSource']}</th>
                        <th class="sorted">${textContainer.text['subjectResolutionUnresolvedSubjectsTableHeaderSubjectId']}</th>
                        <th class="sorted">${textContainer.text['subjectResolutionUnresolvedSubjectsTableHeaderSubjectIdentifier']}</th>
                        <th class="sorted" style="width: 200px;">${textContainer.text['subjectResolutionUnresolvedSubjectsTableHeaderSubjectName']}</th>
                        <th class="sorted">${textContainer.text['subjectResolutionUnresolvedSubjectsTableHeaderSubjectDescription']}</th>
                        <th class="sorted">${textContainer.text['subjectResolutionUnresolvedSubjectsTableHeaderLastResolved']}</th>
                        <th class="sorted">${textContainer.text['subjectResolutionUnresolvedSubjectsTableHeaderLastChecked']}</th>
                        <th class="sorted">${textContainer.text['subjectResolutionUnresolvedSubjectsTableHeaderDaysUnresolved']}</th>
                        <th class="sorted">${textContainer.text['subjectResolutionUnresolvedSubjectsTableHeaderDeleteDate']}</th>
                      </tr>
                    </thead>
                    
                    <tbody>
                      <c:forEach items="${grouperRequestContainer.subjectResolutionContainer.unresolvedSubjects}"
                        var="unresolvedSubject" >
                        <tr>
                          <td>${unresolvedSubject.member.subjectSourceId}</td>
                          <td>${unresolvedSubject.member.subjectId}</td>
                          <td>${unresolvedSubject.member.subjectIdentifier0}</td>
                          <td>${unresolvedSubject.member.name}</td>
                          <td>${unresolvedSubject.member.description}</td>
                          <td>${unresolvedSubject.subjectResolutionDateLastResolvedString}</td>
                          <td>${unresolvedSubject.subjectResolutionDateLastCheckedString}</td>
                          <td>${unresolvedSubject.subjectResolutionDaysUnresolvedString}</td>
                          <td>${unresolvedSubject.dateSubjectWillBeDeletedString}</td>
                        </tr>
                      </c:forEach>
                    </tbody>
                  </table>
          <div class="data-table-bottom gradient-background">
            <grouper:paging2 guiPaging="${grouperRequestContainer.subjectResolutionContainer.guiPaging}" formName="unresolvedSubjectsPagingForm"
              refreshOperation="../app/UiV2SubjectResolution.viewUnresolvedSubjects" />
          </div>