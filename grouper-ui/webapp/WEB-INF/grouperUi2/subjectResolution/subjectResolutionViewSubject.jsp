<%@ include file="../assetsJsp/commonTaglib.jsp"%>

<div id="groupDetailsId">
                      <table class="table table-condensed table-striped">
                        <tbody>

                          <tr>
                            <td><strong>${textContainer.text['subjectResolutionViewSubjectTableSubjectSource']}</strong></td>
                            <c:choose>
                              <c:when test="${empty grouperRequestContainer.subjectResolutionContainer.guiSubjectResolutionSubject.guiSubject}">
                                <td>${grouperRequestContainer.subjectResolutionContainer.guiSubjectResolutionSubject.subjectResolutionAttributeValue.member.subjectSourceId}</td>
                              </c:when>
                              <c:otherwise>
                                <td>${grouperRequestContainer.subjectResolutionContainer.guiSubjectResolutionSubject.guiSubject.subject.sourceId}</td>
                              </c:otherwise>
                            </c:choose>
                          </tr>

                          <tr>
                            <td><strong>${textContainer.text['subjectResolutionViewSubjectTableSubjectId']}</strong></td>
                            <c:choose>
                              <c:when test="${empty grouperRequestContainer.subjectResolutionContainer.guiSubjectResolutionSubject.guiSubject}">
                                <c:set var="subjectId" value="${grouperRequestContainer.subjectResolutionContainer.guiSubjectResolutionSubject.subjectResolutionAttributeValue.member.subjectId}" />
                                <c:set var="sourceId" value="${grouperRequestContainer.subjectResolutionContainer.guiSubjectResolutionSubject.subjectResolutionAttributeValue.member.subjectSourceId}" />
                              </c:when>
                              <c:otherwise>
                                <c:set var="subjectId" value="${grouperRequestContainer.subjectResolutionContainer.guiSubjectResolutionSubject.guiSubject.subject.id}" />
                                <c:set var="sourceId" value="${grouperRequestContainer.subjectResolutionContainer.guiSubjectResolutionSubject.guiSubject.subject.sourceId}" />
                              </c:otherwise>
                            </c:choose>
                            <td><a href="#" onclick="return guiV2link('operation=UiV2Subject.viewSubject&subjectId=${grouper:escapeUrl(subjectId)}&sourceId=${grouper:escapeUrl(sourceId)}');"
                              >${grouper:escapeHtml(subjectId)}</a>
                            </td>
                          </tr>

                          <tr>
                            <td><strong>${textContainer.text['subjectResolutionViewSubjectTableSubjectIdentifier']}</strong></td>
                            <c:choose>
                              <c:when test="${empty grouperRequestContainer.subjectResolutionContainer.guiSubjectResolutionSubject.guiSubject}">
                                <td>${grouperRequestContainer.subjectResolutionContainer.guiSubjectResolutionSubject.subjectResolutionAttributeValue.member.subjectIdentifier0}</td>
                              </c:when>
                              <c:otherwise>
                                <td>${grouperRequestContainer.subjectResolutionContainer.guiSubjectResolutionSubject.guiSubject.member.subjectIdentifier0}</td>
                              </c:otherwise>
                            </c:choose>
                          </tr>

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
                            <td><strong>${textContainer.text['subjectResolutionViewSubjectTableSubjectDescription']}</strong></td>
                            <c:choose>
                              <c:when test="${empty grouperRequestContainer.subjectResolutionContainer.guiSubjectResolutionSubject.guiSubject}">
                                <td>${grouperRequestContainer.subjectResolutionContainer.guiSubjectResolutionSubject.subjectResolutionAttributeValue.member.description}</td>
                              </c:when>
                              <c:otherwise>
                                <td>${grouperRequestContainer.subjectResolutionContainer.guiSubjectResolutionSubject.guiSubject.subject.description}</td>
                              </c:otherwise>
                            </c:choose>
                          </tr>
                          
                          
                          
                          <tr>
                            <td><strong>${textContainer.text['subjectResolutionViewSubjectTableSubjectStatus']}</strong></td>
                            
                            <c:choose>
                              <c:when test="${empty grouperRequestContainer.subjectResolutionContainer.guiSubjectResolutionSubject.subjectResolutionAttributeValue}">
                                <td>${textContainer.text['subjectResolutionViewSubjectTableSubjectResolvableYes']}</td>
                              </c:when>
                              <c:otherwise>
                                <td>
                                ${grouperRequestContainer.subjectResolutionContainer.guiSubjectResolutionSubject.subjectResolutionAttributeValue.deleted ? textContainer.text['subjectResolutionUnresolvedSubjectsTableStatusDeleted'] : textContainer.text['subjectResolutionUnresolvedSubjectsTableStatusUnresolved']}
                                </td>
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
                            <tr>
                              <td><strong>${textContainer.text['subjectResolutionViewSubjectTableSubjectLastCheckedDate']}</strong></td>
                              <td>${grouperRequestContainer.subjectResolutionContainer.guiSubjectResolutionSubject.subjectResolutionAttributeValue.subjectResolutionDateLastCheckedString}</td>
                            </tr>
                            
                            <tr>
                              <td><strong>${textContainer.text['subjectResolutionViewSubjectTableSubjectLastResolvedDate']}</strong></td>
                              <td>${grouperRequestContainer.subjectResolutionContainer.guiSubjectResolutionSubject.subjectResolutionAttributeValue.subjectResolutionDateLastResolvedString}</td>
                            </tr>
                            
                            <tr>
                              <td><strong>${textContainer.text['subjectResolutionViewSubjectTableSubjectDaysUnresolved']}</strong></td>
                              <td>${grouperRequestContainer.subjectResolutionContainer.guiSubjectResolutionSubject.subjectResolutionAttributeValue.subjectResolutionDaysUnresolvedString}</td>
                            </tr>
                            
                            <tr>
                              <td><strong>${textContainer.text['subjectResolutionViewSubjectTableSubjectIsDeleted']}</strong></td>
                              <td>${grouperRequestContainer.subjectResolutionContainer.guiSubjectResolutionSubject.subjectResolutionAttributeValue.subjectResolutionDeletedString}</td>
                            </tr>
                            
                            <tr>
                              <td><strong>${textContainer.text['subjectResolutionViewSubjectTableSubjectDeletedDate']}</strong></td>
                              <td>${grouperRequestContainer.subjectResolutionContainer.guiSubjectResolutionSubject.subjectResolutionAttributeValue.subjectResolutionDateDelete}</td>
                            </tr>
                                                        
                          </c:if>

                        </tbody>
                      </table>
                    </div>