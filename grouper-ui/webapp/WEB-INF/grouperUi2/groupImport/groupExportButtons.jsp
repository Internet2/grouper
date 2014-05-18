<%@ include file="../assetsJsp/commonTaglib.jsp"%>

<%-- this changes when a radio of which type is changed so the url has the right type --%>

                <a href="../app/UiV2GroupImport.groupExportSubmit/groupId%3d${grouperRequestContainer.groupContainer.guiGroup.group.id}/${grouperRequestContainer.groupImportContainer.exportAll ? 'all' : 'ids'}/${grouperRequestContainer.groupImportContainer.exportFileName}" class="btn btn-primary">${textContainer.text['groupExportExportButton'] }</a> 
                <a href="#" class="btn btn-cancel" onclick="return guiV2link('operation=UiV2Group.viewGroup&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}');" >${textContainer.text['groupExportReturnToGroupButton'] }</a>
                