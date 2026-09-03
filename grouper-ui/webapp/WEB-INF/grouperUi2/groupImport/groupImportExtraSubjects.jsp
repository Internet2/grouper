<%@ include file="../assetsJsp/commonTaglib.jsp"%>

                <c:forEach items="${grouperRequestContainer.groupImportContainer.groupImportExtraGuiSubjects}" 
                  var="guiSubject" varStatus="status">
                
                  <%-- c:if test="${status.count>1}" --%>
                  <br />

                  <input type="hidden" name="extraSourceIdSubjectId_${status.count-1}" value="${grouper:escapeHtml(guiSubject.sourceIdSubjectId)}" />            
                  <%-- role="button" + aria-label give this icon-only remove link an accessible name (GRP-7095) --%>
                  ${guiSubject.shortLinkWithIcon} <a class="grouper-icon-btn" href="#" role="button" aria-label="Remove subject ${grouper:escapeHtml(guiSubject.screenLabel)}" onclick="ajax('../app/UiV2GroupImport.groupImportRemoveSubject?removeSubjectSourceAndId=${grouper:escapeUrl(guiSubject.sourceIdSubjectId)}', {formIds: 'importGroupFormId'}); return false;"><i class="fa fa-times" style="color: #aaaaaa" aria-hidden="true"></i></a>
                
                </c:forEach>
