<%@ include file="../assetsJsp/commonTaglib.jsp"%>

                <c:forEach items="${grouperRequestContainer.groupImportContainer.groupImportExtraGuiGroups}" 
                  var="guiGroup" varStatus="status">
                
                  <%-- c:if test="${status.count>1}" --%>
                  <br />

                  <input type="hidden" name="extraGroupId_${status.count-1}" value="${grouper:escapeHtml(guiGroup.group.id)}" />            
                  ${guiGroup.linkWithIcon} <a href="#" role="button" aria-label="Remove group ${grouper:escapeHtml(guiGroup.group.displayExtension)}" onclick="ajax('../app/UiV2GroupImport.groupImportRemoveGroup?removeGroupId=${grouper:escapeUrl(guiGroup.group.id)}', {formIds: 'importGroupFormId'}); return false;"><i class="fa fa-times" style="color: #aaaaaa" aria-hidden="true"></i></a>
                
                </c:forEach>
