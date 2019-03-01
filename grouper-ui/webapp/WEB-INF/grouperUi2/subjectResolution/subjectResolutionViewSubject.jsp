<%@ include file="../assetsJsp/commonTaglib.jsp"%>

<div id="groupDetailsId">
                      <table class="table table-condensed table-striped">
                        <tbody>
                          <tr>
                            <td><strong>${textContainer.text['subjectResolutionViewSubjectTableSubjectName']}</strong></td>
                            <td>${grouperRequestContainer.subjectResolutionContainer.guiSubjectResolutionSubject.guiSubject.subject.name}</td>
                          </tr>
                          <tr>
                            <td><strong>${textContainer.text['subjectResolutionViewSubjectTableSubjectIsResolvable']}</strong></td>
                            
                            <c:if test="${empty grouperRequestContainer.subjectResolutionContainer.guiSubjectResolutionSubject.subjectResolutionAttributeValue}">
                              <td>${textContainer.text['subjectResolutionViewSubjectTableSubjectResolvableYes']}</td>
                            </c:if>
                            
                            <c:if test="${not empty grouperRequestContainer.subjectResolutionContainer.guiSubjectResolutionSubject.subjectResolutionAttributeValue}">
                              <td>${textContainer.text['subjectResolutionViewSubjectTableSubjectResolvableNo']}</td>
                            </c:if>
                            
                          </tr>

                        </tbody>
                      </table>
                    </div>