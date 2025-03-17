<%@ include file="../assetsJsp/commonTaglib.jsp"%>
${grouper:titleFromKeyAndText('stemExportPageTitle', grouperRequestContainer.stemContainer.guiStem.stem.displayName)}

            <%-- for the new group or new stem button --%>
            <input type="hidden" name="objectStemId" value="${grouperRequestContainer.stemContainer.guiStem.stem.id}" />

            <div class="bread-header-container">
              ${grouperRequestContainer.stemContainer.guiStem.breadcrumbs}
              <div class="page-header blue-gradient">
                <h1><i class="fa fa-folder"></i> ${grouper:escapeHtml(grouperRequestContainer.stemContainer.guiStem.guiDisplayExtension)}
                <br /><small>${textContainer.text['stemExportTitle'] }</small></h1>
                
              </div>
            </div>

            <form class="form-horizontal" id="groupExportTypeFormId">
              <div class="control-group">
                <label class="control-label">${textContainer.text['stemExportWhatData'] }</label>
                <div class="controls">
                  <label class="radio">
                    <input type="radio" name="group-export-options" value="ids" checked="checked"
                      onchange="ajax('../app/UiV2GroupImport.groupExportTypeChange?groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}', {formIds: 'groupExportTypeFormId'}); return true;"
                      >${textContainer.text['stemExportEntityIds'] }
                  </label>
                  <label class="radio">
                    <input type="radio" name="group-export-options" value="all"
                      onchange="ajax('../app/UiV2GroupImport.groupExportTypeChange?groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}', {formIds: 'groupExportTypeFormId'}); return true;"
                      >${textContainer.text['stemExportAllMemberData'] }    
                  </label>
                </div>
              </div>
              <div class="form-actions" id="formActionsDivId">
                <%@ include file="groupExportButtons.jsp"%>
              </div>
            </form>
