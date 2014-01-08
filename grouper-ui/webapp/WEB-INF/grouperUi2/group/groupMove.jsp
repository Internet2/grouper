<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <%-- for the new group or new stem button --%>
            <input type="hidden" name="objectStemId" value="${grouperRequestContainer.groupContainer.guiGroup.group.parentUuid}" />

            <div class="bread-header-container">
              <%--
              <ul class="breadcrumb">
                <li><a href="index.html">Home </a><span class="divider"><i class='icon-angle-right'></i></span></li>
                <li class="active">Applications</li>
              </ul>
              --%>
              ${grouperRequestContainer.groupContainer.guiGroup.breadcrumbs}
              <div class="page-header blue-gradient">
                <h1> <i class="icon-folder-close"></i> ${grouper:escapeHtml(grouperRequestContainer.groupContainer.guiGroup.group.displayExtension)}
                <br /><small>${textContainer.text['groupMoveTitle'] }</small></h1>
              </div>
            </div>
            <div class="row-fluid">
              <div class="span12">
                <div id="folder-search" tabindex="-1" role="dialog" aria-labelledby="group-search-label" aria-hidden="true" class="modal hide fade">
                  <div class="modal-header"><a href="#" data-dismiss="modal" aria-hidden="true" class="close">x</a>
                    <h3 id="group-search-label">${textContainer.text['groupMoveSearchForFolderLink'] }</h3>
                  </div>
                  <div class="modal-body">
                    <form class="form form-inline" id="stemSearchFormId">
                      <input name="groupSearch" type="text" placeholder="${textContainer.textEscapeXml['groupMoveSearchForFolderPlaceholder'] }" value=""/> 
                      <button class="btn" onclick="ajax('../app/UiV2Stem.stemSearchGroupFormSubmit', {formIds: 'stemSearchFormId'}); return false;">Search</button>
                    </form>
                    <div id="folderSearchResultsId"></div>
                    
                  </div>
                  <div class="modal-footer">
                    <button data-dismiss="modal" aria-hidden="true" class="btn">Close</button>
                  </div>
                </div>
                <form class="form-horizontal" name="groupMoveFormName" id="groupCopyFormId">
                  <div class="control-group">
                    <label for="parentFolderComboId" class="control-label">${textContainer.text['groupMoveIntoFolder'] }</label>
                    <div class="controls">
                      
                      <%-- placeholder: Enter a folder name --%>
                      <grouper:combobox2 idBase="parentFolderCombo" style="width: 30em"
                        filterOperation="../app/UiV2Stem.createGroupParentFolderFilter"/>
                      
                      <%-- a href="#folder-search" role="button" data-toggle="modal" class="btn"><i class="icon-search"></i></a --%>
                      <span class="help-block">${textContainer.text['groupMoveIntoFolderDescription'] }</span>
                    </div>
                  </div>
                  <div class="control-group">
                    <div class="controls">
                      <label class="checkbox">
                        <input type="checkbox" name="moveChangeAlternateNames" checked="checked" value="true" />${textContainer.text['groupMoveChangeAlternateNamesDescription']}
                      </label>
                    </div>
                  </div>
                  <div class="form-actions"><a href="#" class="btn btn-primary" onclick="ajax('../app/UiV2Group.groupMoveSubmit?groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}', {formIds: 'groupCopyFormId'}); return false;">${textContainer.text['groupMoveMoveButton'] }</a> 
                  <a href="#" class="btn btn-cancel" onclick="return guiV2link('operation=UiV2Group.viewGroup&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}');" >${textContainer.text['groupMoveCancelButton'] }</a></div>
                </form>
              </div>
            </div>
