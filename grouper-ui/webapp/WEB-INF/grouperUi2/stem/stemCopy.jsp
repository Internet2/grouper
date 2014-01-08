<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <%-- for the new group or new stem button --%>
            <input type="hidden" name="objectStemId" value="${grouperRequestContainer.stemContainer.guiStem.stem.id}" />

            <div class="bread-header-container">
              <%--
              <ul class="breadcrumb">
                <li><a href="index.html">Home </a><span class="divider"><i class='icon-angle-right'></i></span></li>
                <li class="active">Applications</li>
              </ul>
              --%>
              ${grouperRequestContainer.stemContainer.guiStem.breadcrumbs}
              <div class="page-header blue-gradient">
                <h1> <i class="icon-folder-close"></i> ${grouper:escapeHtml(grouperRequestContainer.stemContainer.guiStem.guiDisplayExtension)}
                <br /><small>${textContainer.text['stemCopyTitle'] }</small></h1>
              </div>
            </div>
            <div class="row-fluid">
              <div class="span12">
                <div id="folder-search" tabindex="-1" role="dialog" aria-labelledby="group-search-label" aria-hidden="true" class="modal hide fade">
                  <div class="modal-header"><a href="#" data-dismiss="modal" aria-hidden="true" class="close">x</a>
                    <h3 id="group-search-label">${textContainer.text['stemCopySearchForFolder'] }</h3>
                  </div>
                  <div class="modal-body">
                    <form class="form form-inline" id="stemSearchFormId">
                      <input name="stemSearch" type="text" placeholder="${textContainer.textEscapeXml['stemCopySearchForFolderPlaceholder']}" value=""/> 
                      <button class="btn" onclick="ajax('../app/UiV2Stem.stemSearchFormSubmit?stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}', {formIds: 'stemSearchFormId'}); return false;">${textContainer.text['stemCopySearchButton'] }</button>
                    </form>
                    <div id="folderSearchResultsId"></div>
                    
                  </div>
                  <div class="modal-footer">
                    <button data-dismiss="modal" aria-hidden="true" class="btn">${textContainer.text['stemCopyCloseButton'] }</button>
                  </div>
                </div>
                <form class="form-horizontal" name="stemCopyFormName" id="stemCopyFormId">
                  <div class="control-group">
                    <label for="folder-name" class="control-label">${textContainer.text['stemCopyNewStemNameLabel']}</label>
                    <div class="controls">
                      <input type="text" name="displayExtension" value="${grouper:escapeHtml(grouperRequestContainer.stemContainer.guiStem.guiDisplayExtension)}" id="folder-name" /><span class="help-block">${textContainer.text['stemCopyNewStemNameDescription']}</span>
                    </div>
                  </div>
                  <div class="control-group">
                    <label for="folder-id" class="control-label">${textContainer.text['stemCopyNewStemIdLabel'] }</label>
                    <div class="controls">
                      <input type="text" name="extension" id="folder-id" value="${grouper:escapeHtml(grouperRequestContainer.stemContainer.guiStem.stem.extension)}" /><span class="help-block">${textContainer.text['stemCopyNewStemIdDescription'] }</span>
                    </div>
                  </div>
                  <div class="control-group">
                    <label for="parentFolderComboId" class="control-label">${textContainer.text['stemCopyIntoFolder'] }</label>
                    <div class="controls">
                      
                      <%-- placeholder: Enter a folder name --%>
                      <grouper:combobox2 idBase="parentFolderCombo" style="width: 30em"
                        filterOperation="../app/UiV2Stem.stemCopyParentFolderFilter?stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}"/>
                      
                      <%-- a href="#folder-search" role="button" data-toggle="modal" class="btn"><i class="icon-search"></i></a --%>
                      <span class="help-block">${textContainer.text['stemCopyIntoFolderDescription'] }</span>
                    </div>
                  </div>
                  <div class="control-group">
                    <div class="controls">
                      <label class="checkbox">
                        <input type="checkbox" name="copyGroupAttributes" checked="checked" value="true">${textContainer.text['stemCopyGroupAttributes'] }<span class="help-block">${textContainer.text['stemCopyGroupAttributesHelp'] }</span>
                      </label>
                    </div>
                  </div>
                  <div class="control-group">
                    <div class="controls">
                      <label class="checkbox">
                        <input type="checkbox" name="copyListMemberships" checked="checked" value="true">${textContainer.text['stemCopyListMemberships'] }<span class="help-block">${textContainer.text['stemCopyListMembershipsHelp'] }</span>
                      </label>
                    </div>
                  </div>
                  <div class="control-group">
                    <div class="controls">
                      <label class="checkbox">
                        <input type="checkbox" name="copyGroupPrivileges" checked="checked" value="true">${textContainer.text['stemCopyGroupPrivileges'] }<span class="help-block">${textContainer.text['stemCopyGroupPrivilegesHelp'] }</span>
                      </label>
                    </div>
                  </div>
                  <div class="control-group">
                    <div class="controls">
                      <label class="checkbox">
                        <input type="checkbox"  name="copyListMembershipsInOtherGroups" checked="checked" value="true">${textContainer.text['stemCopyGroupsAsMembers'] }<span class="help-block">${textContainer.text['stemCopyGroupsAsMembersHelp'] }</span>
                      </label>
                    </div>
                  </div>
                  <div class="control-group">
                    <div class="controls">
                      <label class="checkbox">
                        <input type="checkbox" name="copyPrivsInOtherGroups"  checked="checked" value="true">${textContainer.text['stemCopyGroupsAsPrivilegees'] }<span class="help-block">${textContainer.text['stemCopyGroupsAsPrivilegees'] }</span>
                      </label>
                    </div>
                  </div>
                  <div class="control-group">
                    <div class="controls">
                      <label class="checkbox">
                        <input type="checkbox" name="copyFolderPrivs" checked="checked" value="true">${textContainer.text['stemCopyFolderPrivileges'] }<span class="help-block">${textContainer.text['stemCopyFolderPrivilegesHelp'] }</span>
                      </label>
                    </div>
                  </div>
                  <div class="form-actions"><a href="#" class="btn btn-primary" onclick="ajax('../app/UiV2Stem.stemCopySubmit?stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}', {formIds: 'stemCopyFormId'}); return false;">${textContainer.text['stemCopyCopyButton'] }</a> 
                  <a href="#" class="btn btn-cancel" onclick="return guiV2link('operation=UiV2Stem.viewStem&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}');" >${textContainer.text['stemCopyCancelButton'] }</a></div>
                </form>
              </div>
            </div>
