<%@ include file="../assetsJsp/commonTaglib.jsp"%>

                <c:forEach items="${grouperRequestContainer.groupImportContainer.groupImportExtraGuiSubjects}" 
                  var="guiSubject" varStatus="status">
                
                  <%-- c:if test="${status.count>1}" --%>
                  <br />

                  <input type="hidden" name="extraSourceIdSubjectId_${status.count-1}" value="${grouper:escapeHtml(guiSubject.sourceIdSubjectId)}" />            
                  ${guiSubject.shortLinkWithIcon} <a href="#" onclick="ajax('../app/UiV2GroupImport.groupImportRemoveSubject?removeSubjectSourceAndId=${grouper:escapeUrl(guiSubject.sourceIdSubjectId)}', {formIds: 'importGroupFormId'}); return false;"><i class="fa fa-times" style="color: #990000"></i></a>
                
                </c:forEach>
