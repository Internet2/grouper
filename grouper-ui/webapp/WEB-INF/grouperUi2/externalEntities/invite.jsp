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
              <ul class="breadcrumb">
                <li><a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');">${textContainer.text['myGroupsHomeBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                <li class="active">${textContainer.text['groupImportMembersBreadcrumb'] }</li>
              </ul>
              <div class="page-header blue-gradient">
                <h1>Invite external users</h1>
              </div>
            </div>
            <div class="row-fluid">
              <div class="span12">

                <div id="group-search" tabindex="-1" role="dialog" aria-labelledby="group-search-label" aria-hidden="true" class="modal hide fade">
                  <div class="modal-header"><a href="#" data-dismiss="modal" aria-hidden="true" class="close">x</a>
                    <h3 id="group-search-label">${textContainer.text['inviteExternalSearchGroupPanel']}</h3>
                  </div>
                  <div class="modal-body">
                    <form class="form form-inline" id="groupSearchFormId">
                      <input name="groupFilter" type="text" placeholder="${textContainer.text['inviteExternalSearchGroupPlaceholder']}" value=""/> 
                      <button class="btn" onclick="ajax('../app/UiV2ExternalEntities.inviteSearchGroupFormSubmit', {formIds: 'groupSearchFormId'}); return false;">${textContainer.text['inviteExternalSearchGroupButton']}</button>
                      <br />
                      <span style="white-space: nowrap;"><input type="checkbox" name="matchExactId" value="true"/> ${textContainer.text['inviteExternalSearchExactIdMatch'] }</span>
                    </form>
                    <div id="groupSearchResultsId"></div>
                  </div>
                  <div class="modal-footer">
                    <button data-dismiss="modal" aria-hidden="true" class="btn">${textContainer.text['groupCopyCloseButton'] }</button>
                  </div>
                </div>
                
                
                <form class="form-horizontal" name="inviteFormId" id="inviteFormName">
                  <div class="control-group">
                    <label for="external-invite-options" class="control-label">Invite by:</label>
                    <div class="controls">
                      <label class="radio">
                        <input type="radio" name="external-invite-options" id="external-invite-options-email" value="email" checked>Email address
                      </label>
                      <label class="radio">
                        <input type="radio" name="external-invite-options" id="external-invite-options-id" value="id">Login ID
                      </label><span class="help-block">Choose if you want to invite people by email address or login ID, if you know the exact login ID.  If you are not sure, invite by email address or find out from them what it is.</span>
                    </div>
                  </div>
                  <div class="invite-external-email-container">
                    <div class="control-group">
                      <label for="external-invite-emails" class="control-label">Email address of people to invite:</label>
                      <div class="controls">
                        <textarea id="external-invite-emails" rows="3" cols="40" class="input-block-level"></textarea><span class="help-block">Enter the email addresses of people to invite separated by space, newline, comma, or semicolon.</span>
                      </div>
                    </div>
                    <div class="control-group">
                      <label for="external-invite-subject" class="control-label">Email subject:</label>
                      <div class="controls">
                        <input type="text" id="external-invite-subject" value="Register to access applications"><span class="help-block">Enter an optional subject for the email to the users.</span>
                      </div>
                    </div>
                    <div class="control-group">
                      <label for="external-invite-message" class="control-label">Message to users:</label>
                      <div class="controls">
                        <textarea id="external-invite-message" rows="10" cols="40" class="input-block-level">Hello,&#10;&#10;This is an invitation to register at our site to be able to access our applications. This invitation expires in 7 days. Click on the link below and sign in with your InCommon credentials. If you do not have InCommon credentials you can register at a site like protectnetwork.com and use those credentials.&#10;&#10;$inviteLink$&#10;&#10;Regards.</textarea><span class="help-block">Enter an optional message to users in the email that will be sent to them.  There will be other instructions and a link in the email as well.</span>
                      </div>
                    </div>
                    <div class="control-group">
                      <label for="external-invite-notify" class="control-label">Email address to notify when registered:</label>
                      <div class="controls">
                        <input type="text" id="external-invite-notify"><span class="help-block">This email address will be notified when the registration is complete.  You can enter multiple email addresses in this field separated by space, comma, or semicolon.</span>
                      </div>
                    </div>
                  </div>
                  <div class="invite-external-id-container hide">
                    <div class="control-group">
                      <label for="external-invite-ids" class="control-label">Login IDs of people to invite:</label>
                      <div class="controls">
                        <textarea id="external-invite-emails" rows="3" cols="40" class="input-block-level"></textarea><span class="help-block">Enter the login IDs of people to invite separated by space, newline, comma, or semicolon.  You must know the exact login ID.  If you are not sure, invite by email address or find out from them what it is.  This is the ID that the institutional authentication system sends to this application when the user logs in.  It might be in the form: username@school.edu</span>
                      </div>
                    </div>
                  </div>
                  <div class="bulk-add-group-input-container">
                    <div class="control-group bulk-add-group-block">
                      <label for="add-entities" class="control-label">Group to assign to new users:</label>
                      <div class="controls">

                        <grouper:combobox2 idBase="inviteAddGroupCombo" style="width: 30em"
                          filterOperation="../app/UiV2ExternalEntities.addGroupFilter"/>
                        <br />
                        <span class="help-block">${textContainer.text['inviteExternalComboHelpText'] }</span>

                      </div>
                    </div>
                    <div class="control-group">
                      <div class="controls"><a href="#" class="btn bulk-add-another-group">Add another group</a></div>
                    </div>
                  </div>
                  <div class="form-actions"><a href="#" class="btn btn-primary">Invite</a> <a href="#" class="btn btn-cancel">Cancel</a></div>
                </form>
                
                
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
