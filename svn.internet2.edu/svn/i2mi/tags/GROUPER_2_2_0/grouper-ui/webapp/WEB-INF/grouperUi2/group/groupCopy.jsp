<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <%-- for the new group or new stem button --%>
            <input type="hidden" name="objectStemId" value="${grouperRequestContainer.groupContainer.guiGroup.group.parentUuid}" />

            <div class="bread-header-container">
              <%--
              <ul class="breadcrumb">
                <li><a href="index.html">Home </a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                <li class="active">Applications</li>
              </ul>
              --%>
              ${grouperRequestContainer.groupContainer.guiGroup.breadcrumbs}
              <div class="page-header blue-gradient">
                <h1> <i class="fa fa-folder"></i> ${grouper:escapeHtml(grouperRequestContainer.groupContainer.guiGroup.group.displayExtension)}
                <br /><small>${textContainer.text['groupCopyTitle'] }</small></h1>
              </div>
            </div>
            <div class="row-fluid">
              <div class="span12">
                <div id="folder-search" tabindex="-1" role="dialog" aria-labelledby="group-search-label" aria-hidden="true" class="modal hide fade">
                  <div class="modal-header"><a href="#" data-dismiss="modal" aria-hidden="true" class="close">x</a>
                    <h3 id="group-search-label">${textContainer.text['groupCopySearchForFolder'] }</h3>
                  </div>
                  <div class="modal-body">
                    <form class="form form-inline" id="stemSearchFormId">
                      <input name="stemSearch" type="text" placeholder="${textContainer.textEscapeXml['groupCopySearchForFolderPlaceholder']}" value=""/> 
                      <button class="btn" onclick="ajax('../app/UiV2Stem.stemSearchGroupFormSubmit', {formIds: 'stemSearchFormId'}); return false;">${textContainer.text['groupCopySearchButton'] }</button>
                    </form>
                    <div id="folderSearchResultsId"></div>
                    
                  </div>
                  <div class="modal-footer">
                    <button data-dismiss="modal" aria-hidden="true" class="btn">${textContainer.text['groupCopyCloseButton'] }</button>
                  </div>
                </div>
                <form class="form-horizontal" name="groupCopyFormName" id="groupCopyFormId">
                  <div class="control-group">
                    <label for="group-name" class="control-label">${textContainer.text['groupCopyNewGroupNameLabel']}</label>
                    <div class="controls">
                      <input type="text" name="displayExtension" value="${grouper:escapeHtml(grouperRequestContainer.groupContainer.guiGroup.group.displayExtension)}" id="group-name" /><span class="help-block">${textContainer.text['groupCopyNewGroupNameDescription']}</span>
                    </div>
                  </div>
                  <div class="control-group">
                    <label for="group-id" class="control-label">${textContainer.text['groupCopyNewGroupIdLabel'] }</label>
                    <div class="controls">
                      <input type="text" name="extension" id="group-id" value="${grouper:escapeHtml(grouperRequestContainer.groupContainer.guiGroup.group.extension)}" /><span class="help-block">${textContainer.text['groupCopyNewGroupIdDescription'] }</span>
                    </div>
                  </div>
                  <div class="control-group">
                    <label for="parentFolderComboId" class="control-label">${textContainer.text['groupCopyIntoFolder'] }</label>
                    <div class="controls">
                      
                      <%-- placeholder: Enter a folder name --%>
                      <grouper:combobox2 idBase="parentFolderCombo" style="width: 30em"
                        filterOperation="../app/UiV2Stem.createGroupParentFolderFilter?stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}"/>
                      
                      <%-- a href="#folder-search" role="button" data-toggle="modal" class="btn"><i class="fa fa-search"></i></a --%>
                      <span class="help-block">${textContainer.text['groupCopyIntoFolderDescription'] }</span>
                    </div>
                  </div>
                  <div class="control-group">
                    <div class="controls">
                      <label class="checkbox">
                        <input type="checkbox" name="copyGroupAttributes" checked="checked" value="true">${textContainer.text['groupCopyGroupAttributes'] }<span class="help-block">${textContainer.text['groupCopyGroupAttributesHelp'] }</span>
                      </label>
                    </div>
                  </div>
                  <div class="control-group">
                    <div class="controls">
                      <label class="checkbox">
                        <input type="checkbox" name="copyListMemberships" checked="checked" value="true">${textContainer.text['groupCopyListMemberships'] }<span class="help-block">${textContainer.text['groupCopyListMembershipsHelp'] }</span>
                      </label>
                    </div>
                  </div>
                  <div class="control-group">
                    <div class="controls">
                      <label class="checkbox">
                        <input type="checkbox" name="copyGroupPrivileges" checked="checked" value="true">${textContainer.text['groupCopyGroupPrivileges'] }<span class="help-block">${textContainer.text['groupCopyGroupPrivilegesHelp'] }</span>
                      </label>
                    </div>
                  </div>
                  <div class="control-group">
                    <div class="controls">
                      <label class="checkbox">
                        <input type="checkbox"  name="copyListMembershipsInOtherGroups" checked="checked" value="true">${textContainer.text['groupCopyGroupsAsMembers'] }<span class="help-block">${textContainer.text['groupCopyGroupsAsMembersHelp'] }</span>
                      </label>
                    </div>
                  </div>
                  <div class="control-group">
                    <div class="controls">
                      <label class="checkbox">
                        <input type="checkbox" name="copyPrivsInOtherGroups"  checked="checked" value="true">${textContainer.text['groupCopyGroupsAsPrivilegees'] }<span class="help-block">${textContainer.text['groupCopyGroupsAsPrivilegees'] }</span>
                      </label>
                    </div>
                  </div>
                  <div class="form-actions"><a href="#" class="btn btn-primary" onclick="ajax('../app/UiV2Group.groupCopySubmit?groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}', {formIds: 'groupCopyFormId'}); return false;">${textContainer.text['groupCopyCopyButton'] }</a> 
                  <a href="#" class="btn btn-cancel" onclick="return guiV2link('operation=UiV2Group.viewGroup&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}');" >${textContainer.text['groupCopyCancelButton'] }</a></div>
                </form>
              </div>
            </div>
