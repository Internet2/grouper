<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <div class="bread-header-container">
              <ul class="breadcrumb">
                <li><a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');">Home </a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                <li class="active">${textContainer.text['stemNewBreadcrumb'] }</li>
              </ul>
              <div class="page-header blue-gradient">
                <h1>${textContainer.text['stemNewTitle'] }</h1>
              </div>
            </div>
            <div class="row-fluid">
              <div class="span12">
                <div id="folder-search" tabindex="-1" role="dialog" aria-labelledby="stem-search-label" aria-hidden="true" class="modal hide fade">
                  <div class="modal-header"><a href="#" data-dismiss="modal" aria-hidden="true" class="close">x</a>
                    <h3 id="stem-search-label">${textContainer.text['stemCreateSearchForFolderTitle'] }</h3>
                  </div>
                  <div class="modal-body">
                    <form class="form form-inline" id="stemSearchFormId">
                      <input name="stemSearch" type="text" placeholder="${textContainer.textEscapeXml['stemCreateSearchPlaceholder'] }" value=""/> 
                      <button class="btn" onclick="ajax('../app/UiV2Stem.stemSearchFormSubmit?stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}', {formIds: 'stemSearchFormId'}); return false;">${textContainer.text['stemCreateSearchButton'] }</button>
                    </form>
                    <div id="folderSearchResultsId"></div>
                    
                  </div>
                  <div class="modal-footer">
                    <button data-dismiss="modal" aria-hidden="true" class="btn">${textContainer.text['stemCreateSearchClose'] }</button>
                  </div>
                </div>
                <form id="addStemForm" class="form-horizontal">
                  <div class="control-group">
                    <label for="folder-path" class="control-label">${textContainer.text['stemCreateFolderLabel'] }</label>
                    <div class="controls">
                      <%-- placeholder: Enter a folder name --%>
                      <grouper:combobox2 idBase="parentFolderCombo" style="width: 30em" 
                        filterOperation="../app/UiV2Stem.createStemParentFolderFilter"
                        value="${grouper:escapeHtml(grouperRequestContainer.stemContainer.objectStemId)}"
                        />
                      <span class="help-block">${textContainer.text['stemCreateIntoFolderDescription'] }</span>
                    
                    </div>
                  </div>
                  <div class="control-group">
                    <label for="stemName" class="control-label">${textContainer.text['stemCreateNameLabel'] }</label>
                    <div class="controls">
                      <input type="text" id="stemName" name="displayExtension" 
                        onkeyup="syncNameAndId('stemName', 'stemId', 'nameDifferentThanIdId', false, null); return true;"
                      /><span class="help-block">${textContainer.text['stemCreateNameDescription'] }</span>
                    </div>
                  </div>
                  <div class="control-group">
                    <label for="stemId" class="control-label">${textContainer.text['stemCreateIdLabel'] }</label>
                    <div class="controls">

                      <span onclick="syncNameAndId('stemName', 'stemId', 'nameDifferentThanIdId', true, '${textContainer.textEscapeXml['stemNewAlertWhenClickingOnDisabledId']}'); return true;">
                        <input type="text" id="stemId" name="extension" disabled="disabled"  /> 
                      </span>
                      <span style="white-space: nowrap;">
                        <input type="checkbox" name="nameDifferentThanId" id="nameDifferentThanIdId" value="true"
                          onchange="syncNameAndId('stemName', 'stemId', 'nameDifferentThanIdId', false, null); return true;"
                        />
                        ${textContainer.text['stemNewEditTheId'] }
                      </span>
                      <span class="help-block">${textContainer.text['stemCreateIdDescription'] }</span>
                    </div>
                  </div>
                  <div class="control-group">
                    <label for="stem-description" class="control-label">${textContainer.text['stemCreateDescriptionLabel'] }</label>
                    <div class="controls">
                      <textarea id="stem-description" name=description rows="3" cols="40" class="input-block-level"></textarea><span class="help-block">${textContainer.text['stemCreateDescriptionDescription'] }</span>
                    </div>
                  </div>
                  <div class="form-actions"><a href="#" class="btn btn-primary" onclick="ajax('../app/UiV2Stem.newStemSubmit', {formIds: 'addStemForm'}); return false;">${textContainer.text['stemCreateSaveButton'] }</a> 
                  <a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');" class="btn btn-cancel">${textContainer.text['stemCreateCancelButton'] }</a></div>
                </form>
              </div>
            </div>
            
            
            