<%@ include file="../assetsJsp/commonTaglib.jsp"%>

                    <!-- start subject/subjectMoreActionsButtonContents.jsp -->

                    <div class="span3"><a id="show-add-block" href="#" class="btn btn-medium btn-block btn-primary">
                      <i class="icon-plus"></i> ${textContainer.text['subjectViewMoreActionsAddMembers'] }</a> 

                      <%-- add or remove to/from my favorites, this causes a success message --%>
                      <c:choose>
                        <c:when test="${grouperRequestContainer.groupContainer.favorite}">
                          <li><a href="#" 
                          onclick="ajax('../app/UiV2Subject.removeFromMyFavorites?subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}'); return false;" 
                          >${textContainer.text['subjectViewMoreActionsRemoveFromMyFavorites'] }</a></li>
                        </c:when>
                        <c:otherwise>
                          <li><a href="#" 
                          onclick="ajax('../app/UiV2Subject.addToMyFavorites?subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}'); return false;" 
                          >${textContainer.text['subjectViewMoreActionsAddToMyFavorites']}</a></li>
                        </c:otherwise>
                      </c:choose>
                    
                    </div>


