<%@ include file="../assetsJsp/commonTaglib.jsp"%>
${grouper:titleFromKeyAndText('stemMovePageTitle', grouperRequestContainer.stemContainer.guiStem.stem.displayName)}


            <%-- for the new group or new stem button --%>
            <input type="hidden" name="objectStemId" value="${grouperRequestContainer.stemContainer.guiStem.stem.id}" />

            <div class="bread-header-container">
              ${grouperRequestContainer.stemContainer.guiStem.breadcrumbs}
              <div class="page-header blue-gradient">
                <h1> <i class="fa fa-folder"></i> ${grouper:escapeHtml(grouperRequestContainer.stemContainer.guiStem.guiDisplayExtension)}
                <br /><small>${textContainer.text['stemMoveTitle'] }</small></h1>
              </div>
            </div>
            <div class="row-fluid">
              <div class="span12">
                <div id="folder-search" tabindex="-1" role="dialog" aria-labelledby="group-search-label" aria-hidden="true" class="modal hide fade">
                  <div class="modal-header"><a href="#" data-dismiss="modal" aria-hidden="true" class="close">x</a>
                    <h3 id="group-search-label">${textContainer.text['stemMoveSearchForFolderLink'] }</h3>
                  </div>
                  <div class="modal-body">
                    <form class="form form-inline" id="stemSearchFormId">
                      <input name="stemSearch" type="text" placeholder="${textContainer.textEscapeXml['stemMoveSearchForFolderPlaceholder'] }" value=""/> 
                      <button class="btn" onclick="ajax('../app/UiV2Stem.stemSearchFormSubmit?stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}', {formIds: 'stemSearchFormId'}); return false;">Search</button>
                    </form>
                    <div id="folderSearchResultsId"></div>
                    
                  </div>
                  <div class="modal-footer">
                    <button data-dismiss="modal" aria-hidden="true" class="btn">Close</button>
                  </div>
                </div>
                <form class="form-horizontal" name="stemMoveFormName" id="stemCopyFormId">
                  <div class="control-group">
                    <label for="parentFolderComboId" class="control-label">${textContainer.text['stemMoveIntoFolder'] }</label>
                    <div class="controls">
                      
                      <%-- placeholder: Enter a folder name --%>
                      <grouper:combobox2 idBase="parentFolderCombo" style="width: 30em"
                        filterOperation="../app/UiV2Stem.stemCopyParentFolderFilter?stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}"/>
                      
                      <%-- a href="#folder-search" role="button" data-toggle="modal" class="btn"><i class="fa fa-search"></i></a --%>
                      <span class="help-block">${textContainer.text['stemMoveIntoFolderDescription'] }</span>
                    </div>
                  </div>
                  <div class="control-group">
                    <div class="controls">
                      <label class="checkbox">
                        <input type="checkbox" name="moveChangeAlternateNames" checked="checked" value="true" />${textContainer.text['stemMoveChangeAlternateNamesDescription']}
                      </label>
                    </div>
                  </div>
                  <div class="form-actions"><a href="#" class="btn btn-primary" role="button" onclick="ajax('../app/UiV2Stem.stemMoveSubmit?stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}', {formIds: 'stemCopyFormId'}); return false;">${textContainer.text['stemMoveMoveButton'] }</a> 
                  <a href="#" class="btn btn-cancel" role="button" onclick="return guiV2link('operation=UiV2Stem.viewStem&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}');" >${textContainer.text['stemMoveCancelButton'] }</a></div>
                </form>
              </div>
            </div>
