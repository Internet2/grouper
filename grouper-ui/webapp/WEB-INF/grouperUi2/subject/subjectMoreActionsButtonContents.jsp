<%@ include file="../assetsJsp/commonTaglib.jsp"%>

                    <!-- start subject/subjectMoreActionsButtonContents.jsp -->
                    <a id="show-add-block" href="#" onclick="$('#add-block-container').toggle('slow'); return false;" class="btn btn-medium btn-block btn-primary"
                        style="white-space: nowrap;">
                      <i class="icon-plus"></i> ${textContainer.text['subjectViewMoreActionsAddMembers'] }</a> 

                      <%-- add or remove to/from my favorites, this causes a success message --%>
                      <c:choose>
                        <c:when test="${grouperRequestContainer.subjectContainer.favorite}">
                          <a href="#" 
                          onclick="ajax('../app/UiV2Subject.removeFromMyFavorites?subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}'); return false;"
                          class="btn btn-medium btn-block add-to-my-favorites"
                          style="white-space: nowrap;" 
                          >${textContainer.text['subjectViewMoreActionsRemoveFromMyFavorites'] }</a>
                        </c:when>
                        <c:otherwise>
                          <a href="#" 
                          onclick="ajax('../app/UiV2Subject.addToMyFavorites?subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}'); return false;"
                          class="btn btn-medium btn-block add-to-my-favorites"
                          style="white-space: nowrap;" 
                          >${textContainer.text['subjectViewMoreActionsAddToMyFavorites']}</a>
                        </c:otherwise>
                      </c:choose>
                    
                    


