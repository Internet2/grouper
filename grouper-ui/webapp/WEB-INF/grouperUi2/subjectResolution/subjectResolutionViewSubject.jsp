<%@ include file="../assetsJsp/commonTaglib.jsp"%>

<div id="groupDetailsId">
                      <table class="table table-condensed table-striped">
                        <tbody>
                          <tr>
                            <td><strong>${textContainer.text['subjectResolutionViewSubjectTableSubjectName']}</strong></td>
                            <c:choose>
                              <c:when test="${empty grouperRequestContainer.subjectResolutionContainer.guiSubjectResolutionSubject.guiSubject}">
                                <td>${grouperRequestContainer.subjectResolutionContainer.guiSubjectResolutionSubject.subjectResolutionAttributeValue.member.name}</td>
                              </c:when>
                              <c:otherwise>
                                <td>${grouperRequestContainer.subjectResolutionContainer.guiSubjectResolutionSubject.guiSubject.subject.name}</td>
                              </c:otherwise>
                            </c:choose>
                          </tr>
                          
                          <tr>
                            <td><strong>${textContainer.text['subjectResolutionViewSubjectTableSubjectIsResolvable']}</strong></td>
                            
                            <c:choose>
                              <c:when test="${empty grouperRequestContainer.subjectResolutionContainer.guiSubjectResolutionSubject.guiSubject}">
                                <td>${textContainer.text['subjectResolutionViewSubjectTableSubjectResolvableNo']}</td>
                              </c:when>
                              <c:otherwise>
                                <td>${textContainer.text['subjectResolutionViewSubjectTableSubjectResolvableYes']}</td>
                              </c:otherwise>
                            </c:choose>
                            
                          </tr>
                          
                          <c:if test="${not empty grouperRequestContainer.subjectResolutionContainer.guiSubjectResolutionSubject.subjectResolutionAttributeValue}">
                          
                            <c:if test="${not empty grouperRequestContainer.subjectResolutionContainer.guiSubjectResolutionSubject.subjectResolutionAttributeValue.subjectResolutionDateLastResolvedString}">
                              <tr>
	                              <td><strong>${textContainer.text['subjectResolutionViewSubjectTableSubjectLastResolvedDate']}</strong></td>
	                              <td>${grouperRequestContainer.subjectResolutionContainer.guiSubjectResolutionSubject.subjectResolutionAttributeValue.subjectResolutionDateLastResolvedString}</td>
                              </tr>
                            </c:if>
                            
                            <c:if test="${not empty grouperRequestContainer.subjectResolutionContainer.guiSubjectResolutionSubject.subjectResolutionAttributeValue.subjectResolutionDaysUnresolvedString}">
                              <tr>
                                <td><strong>${textContainer.text['subjectResolutionViewSubjectTableSubjectDaysUnresolved']}</strong></td>
                                <td>${grouperRequestContainer.subjectResolutionContainer.guiSubjectResolutionSubject.subjectResolutionAttributeValue.subjectResolutionDaysUnresolvedString}</td>
                              </tr>
                            </c:if>
                            
                            <c:if test="${not empty grouperRequestContainer.subjectResolutionContainer.guiSubjectResolutionSubject.subjectResolutionAttributeValue.subjectResolutionDeletedString}">
                              <tr>
                                <td><strong>${textContainer.text['subjectResolutionViewSubjectTableSubjectIsDeleted']}</strong></td>
                                <td>${grouperRequestContainer.subjectResolutionContainer.guiSubjectResolutionSubject.subjectResolutionAttributeValue.subjectResolutionDeletedString}</td>
                              </tr>
                            </c:if>
                            
                            <c:if test="${not empty grouperRequestContainer.subjectResolutionContainer.guiSubjectResolutionSubject.subjectResolutionAttributeValue.subjectResolutionDateDelete}">
                              <tr>
                                <td><strong>${textContainer.text['subjectResolutionViewSubjectTableSubjectDeletedDate']}</strong></td>
                                <td>${grouperRequestContainer.subjectResolutionContainer.guiSubjectResolutionSubject.subjectResolutionAttributeValue.subjectResolutionDateDelete}</td>
                              </tr>
                            </c:if>
                                                        
                          </c:if>

                        </tbody>
                      </table>
                    </div>