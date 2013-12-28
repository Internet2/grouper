<%@ include file="../assetsJsp/commonTaglib.jsp"%>

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
                    <h3 id="group-search-label">Search or browse for a folder</h3>
                  </div>
                  <div class="modal-body">
                    <form class="form form-inline" id="stemSearchFormId">
                      <input name="stemSearch" type="text" placeholder="Search for a folder" value=""/> 
                      <button class="btn" onclick="ajax('../app/UiV2Group.stemSearchFormSubmit?stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}', {formIds: 'stemSearchFormId'}); return false;">Search</button>
                    </form>
                    <div id="folderSearchResultsId"></div>
                    
                  </div>
                  <div class="modal-footer">
                    <button data-dismiss="modal" aria-hidden="true" class="btn">Close</button>
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
                      <span class="help-block">${textContainer.text['stemCopyIntoFolderDescription'] }Enter a folder name or <a href="#folder-search" data-toggle="modal" onclick="$('#folderSearchResultsId').empty();" role="button" style="text-decoration: underline !important;">search for a folder where you are allowed to create new subfolders</a>.</span>
                    </div>
                  </div>
                  <div class="control-group">
                    <div class="controls">
                      <label class="checkbox">
                        <input type="checkbox" name="copyGroupAttributes" checked="checked" value="true">Copy group attributes?<span class="help-block">If you select this option, all custom attributes for the groups in the folder will be copied to the new groups.</span>
                      </label>
                    </div>
                  </div>
                  <div class="control-group">
                    <div class="controls">
                      <label class="checkbox">
                        <input type="checkbox" name="copyListMemberships" checked="checked" value="true">Copy list memberships of groups?<span class="help-block">If you select this option, all members of the groups in the folder in the default list along with any custom lists will be copied to the new groups.</span>
                      </label>
                    </div>
                  </div>
                  <div class="control-group">
                    <div class="controls">
                      <label class="checkbox">
                        <input type="checkbox" name="copyGroupPrivileges" checked="checked" value="true">Copy privileges of groups?<span class="help-block">If you select this option, all privileges of the groups in the folder will be copied to the new groups.</span>
                      </label>
                    </div>
                  </div>
                  <div class="control-group">
                    <div class="controls">
                      <label class="checkbox">
                        <input type="checkbox"  name="copyListMembershipsInOtherGroups" checked="checked" value="true">Copy list memberships where groups in the folder being copied are members of other groups?<span class="help-block">If you select this option and groups in the folder being copied are members of other groups, the new copied groups will be added to the other groups&#39; membership list.  If you do not have access to add members to the other groups, you will get a privilege error.</span>
                      </label>
                    </div>
                  </div>
                  <div class="control-group">
                    <div class="controls">
                      <label class="checkbox">
                        <input type="checkbox" name="copyPrivsInOtherGroups"  checked="checked" value="true">Copy privileges where the groups in the folder being copied have privileges to other groups or folders?<span class="help-block">If you select this option and groups in the folder being copied have privileges to other groups or folders, the new copied groups will also be given privileges to those other groups or folders.  If you do not have access to add privileges to the other groups or folders, you will get a privilege error.'</span>
                      </label>
                    </div>
                  </div>
                  <div class="control-group">
                    <div class="controls">
                      <label class="checkbox">
                        <input type="checkbox" name="copyFolderPrivs" checked="checked" value="true">Copy folder privileges?<span class="help-block">If you select this option, all folder privileges will be copied.</span>
                      </label>
                    </div>
                  </div>
                  <div class="form-actions"><a href="#" class="btn btn-primary" onclick="ajax('../app/UiV2Stem.stemCopySubmit?stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}', {formIds: 'stemCopyFormId'}); return false;">Copy</a> 
                  <a href="#" class="btn btn-cancel" onclick="return guiV2link('operation=UiV2Stem.viewStem&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}');" >Cancel</a></div>
                </form>
              </div>
            </div>
