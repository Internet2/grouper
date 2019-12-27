<%@ include file="../assetsJsp/commonTaglib.jsp"%>

                <form id="membersToDeleteFormId">
                  <table class="table table-hover table-bordered table-striped table-condensed data-table table-bulk-update footable">
                    <thead>
                      <tr>
                        <tr>
                          <td colspan="11" class="table-toolbar gradient-background">
                            <a href="#" onclick="ajax('../app/UiV2SubjectResolution.removeMembers', {formIds: 'unresolvedSubjectsPagingFormPageNumberId,membersToDeleteFormId,usduFormId'}); return false;" class="btn" role="button">${textContainer.text['subjectResolutionDeleteSelectedEntitiesButton'] }</a>
                          </td>
                        </tr>
                        
                        <th>
                          <label class="checkbox checkbox-no-padding">
                            <input type="checkbox" name="notImportantXyzName" id="notImportantXyzId" onchange="$('.usduCheckbox').prop('checked', $('#notImportantXyzId').prop('checked'));" />
                          </label>
                        </th>
                      
                        <th class="sorted">${textContainer.text['subjectResolutionUnresolvedSubjectsTableHeaderSubjectSource']}</th>
                        <th class="sorted">${textContainer.text['subjectResolutionUnresolvedSubjectsTableHeaderSubjectId']}</th>
                        <th class="sorted">${textContainer.text['subjectResolutionUnresolvedSubjectsTableHeaderSubjectIdentifier']}</th>
                        <th class="sorted" style="width: 200px;">${textContainer.text['subjectResolutionUnresolvedSubjectsTableHeaderSubjectName']}</th>
                        <th class="sorted">${textContainer.text['subjectResolutionUnresolvedSubjectsTableHeaderSubjectDescription']}</th>
                        <th class="sorted">${textContainer.text['subjectResolutionUnresolvedSubjectsTableHeaderStatus']}</th>
                        <th class="sorted">${textContainer.text['subjectResolutionUnresolvedSubjectsTableHeaderLastResolved']}</th>
                        <th class="sorted">${textContainer.text['subjectResolutionUnresolvedSubjectsTableHeaderLastChecked']}</th>
                        <th class="sorted">${textContainer.text['subjectResolutionUnresolvedSubjectsTableHeaderDaysUnresolved']}</th>
                        <th class="sorted">${textContainer.text['subjectResolutionUnresolvedSubjectsTableHeaderDeleteDate']}</th>
                      </tr>
                    </thead>
                    
                    <tbody>
                      <c:set var="i" value="0" />
                      <c:forEach items="${grouperRequestContainer.subjectResolutionContainer.unresolvedSubjects}"
                        var="unresolvedSubject" >
                        <tr>
                          <td>
                            <label class="checkbox checkbox-no-padding">
                              <c:choose>
                                <c:when test="${! unresolvedSubject.deleted}">
                                  <input type="checkbox" aria-label="${textContainer.text['subjectResolutionCheckboxAriaLabel']}" name="memberRow_${i}" value="${unresolvedSubject.member.id}" class="usduCheckbox" />
                                </c:when>
                              </c:choose>
                            </label>
                          </td>
                          <td>${grouper:escapeHtml(unresolvedSubject.member.subjectSourceId)}</td>
                          <td>
                            <a href="#" onclick="return guiV2link('operation=UiV2Subject.viewSubject&subjectId=${grouper:escapeUrl(unresolvedSubject.member.subjectId)}&sourceId=${grouper:escapeUrl(unresolvedSubject.member.subjectSourceId)}');"
                       >${unresolvedSubject.member.subjectId}</a>
                          </td>
                          <td>${grouper:escapeHtml(unresolvedSubject.member.subjectIdentifier0)}</td>
                          <td>${grouper:escapeHtml(unresolvedSubject.member.name)}</td>
                          <td>${grouper:escapeHtml(unresolvedSubject.member.description)}</td>
                          <td>${unresolvedSubject.deleted ? textContainer.text['subjectResolutionUnresolvedSubjectsTableStatusDeleted'] : textContainer.text['subjectResolutionUnresolvedSubjectsTableStatusUnresolved']}</td>
                          <td>${unresolvedSubject.subjectResolutionDateLastResolvedString}</td>
                          <td>${unresolvedSubject.subjectResolutionDateLastCheckedString}</td>
                          <td>${unresolvedSubject.subjectResolutionDaysUnresolvedString}</td>
                          <td>${unresolvedSubject.dateSubjectWillBeDeletedString}</td>
                        </tr>
                        <c:set var="i" value="${i+1}" />
                      </c:forEach>
                    </tbody>
                  </table>
                </form>
          <div class="data-table-bottom gradient-background">
            <grouper:paging2 guiPaging="${grouperRequestContainer.subjectResolutionContainer.guiPaging}" formName="unresolvedSubjectsPagingForm" ajaxFormIds="usduFormId,unresolvedSubjectsPagingFormPageNumberId"
              refreshOperation="../app/UiV2SubjectResolution.viewUnresolvedSubjects" />
          </div>