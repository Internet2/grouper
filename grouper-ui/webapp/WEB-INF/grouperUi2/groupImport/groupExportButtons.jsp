<%@ include file="../assetsJsp/commonTaglib.jsp"%>

<%-- this changes when a radio of which type is changed so the url has the right type --%>

                <%-- original synchronous download; best for small/medium groups --%>
                <a href="../app/UiV2GroupImport.groupExportSubmit/groupId%3d${grouperRequestContainer.groupContainer.guiGroup.group.id}/${grouperRequestContainer.groupImportContainer.exportAll ? 'all' : 'ids'}/${grouperRequestContainer.groupImportContainer.exportFileName}" class="btn btn-primary">${textContainer.text['groupExportExportButton'] }</a> 
                <%-- async export; recommended for large groups, emails a secure download link --%>
                <button type="button" class="btn btn-primary" onclick="ajax('../app/UiV2GroupImport.groupExportEmailSubmit?groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}', {formIds: 'groupExportTypeFormId'}); return false;">${textContainer.text['groupExportEmailButton'] }</button>
                <button type="button" class="btn btn-cancel" onclick="return guiV2link('operation=UiV2Group.viewGroup&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}');" >${textContainer.text['groupExportReturnToGroupButton'] }</button>
                
