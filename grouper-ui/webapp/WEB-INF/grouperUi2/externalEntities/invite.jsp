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
                      <input type="hidden" name="groupId" value="${grouperRequestContainer.groupContainer.guiGroup.group.id}" />
                      <input name="groupFilter" type="text" id="groupFilterId" placeholder="${textContainer.text['inviteExternalSearchGroupPlaceholder']}" value=""/> 
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
                
                
                <form class="form-horizontal" name="inviteFormName" id="inviteFormId">
                  <c:if test="grouperRequestContainer.inviteExternalContainer.allowInviteByIdentifier">
                    <div class="control-group">
                      <label for="external-invite-options" class="control-label">${textContainer.text['inviteExternalLabelInviteBy']}</label>
                      <div class="controls">
                        <label class="radio">
                          <input type="radio" name="inviteBy" id="external-invite-options-email" value="email" 
                            onchange="$('.invite-external-email-container').slideDown('fast'); $('.invite-external-id-container').slideUp('fast'); return true;"
                            checked="checked">${textContainer.text['inviteExternalInviteTypeEmail']}
                        </label>
                        <label class="radio">
                          <input type="radio" name="inviteBy" id="external-invite-options-id" 
                            onchange="$('.invite-external-email-container').slideUp('fast'); $('.invite-external-id-container').slideDown('fast'); return true;"
                            value="id">${textContainer.text['inviteExternalInviteTypeLoginId']}
                        </label><span class="help-block">${textContainer.text['inviteExternalInviteTypeHelp'] }</span>
                      </div>
                    </div>
                  </c:if>
                  <div class="invite-external-email-container">
                    <div class="control-group">
                      <label for="external-invite-emails" class="control-label">${textContainer.text['inviteExternalLabelEmail']}</label>
                      <div class="controls">
                        <textarea id="external-invite-emails" rows="3" cols="40" class="input-block-level"
                          name="emailAddressesToInvite"
                          ></textarea><span class="help-block">${textContainer.text['inviteExternalEmailHelp']}</span>
                      </div>
                    </div>
                    <div class="control-group">
                      <label for="external-invite-subject" class="control-label">${textContainer.text['inviteExternalLabelEmailSubject']}</label>
                      <div class="controls">
                        <input type="text" id="external-invite-subject" name="emailSubject"
                          value="${textContainer.textEscapeXml['inviteExternalEmailSubjectDefault'] }"><span class="help-block">${textContainer.text['inviteExternalEmailSubjectHelp']}</span>
                      </div>
                    </div>
                    <div class="control-group">
                      <label for="external-invite-message" class="control-label">${textContainer.text['inviteExternalLabelEmailBody']}</label>
                      <div class="controls">
                        <textarea id="external-invite-message" rows="10" cols="40" class="input-block-level" name="messageToUsers"
                          >${textContainer.text['inviteExternalEmailMessageSample'] }</textarea><span class="help-block">${textContainer.text['inviteExternalEmailMessageHelp'] }</span>
                      </div>
                    </div>
                    <div class="control-group">
                      <label for="external-invite-notify" class="control-label">${textContainer.text['inviteExternalEmailAddressesToNotify'] }</label>
                      <div class="controls">
                        <input type="text" id="external-invite-notify" name="ccEmailAddress"
                          ><span class="help-block">${textContainer.text['inviteExternalEmailAddressesToNotifyHelp'] }</span>
                      </div>
                    </div>
                  </div>
                  <c:if test="grouperRequestContainer.inviteExternalContainer.allowInviteByIdentifier">
                    <div class="invite-external-id-container hide">
                      <div class="control-group">
                        <label for="external-invite-ids" class="control-label">${textContainer.text['inviteExternalLabelUserIds']}</label>
                        <div class="controls">
                          <textarea id="loginIdsToInvite" rows="3" cols="40" class="input-block-level"></textarea><span class="help-block">${textContainer.text['inviteExternalUserIdsHelp']}</span>
                        </div>
                      </div>
                    </div>
                  </c:if>                  
                  <div class="bulk-add-group-input-container">
                    <div class="control-group bulk-add-group-block">
                      <label for="add-entities" style="position:absolute" class="control-label">${textContainer.text['inviteExternalLabelGroupToAddToNewUsers'] }</label>
                      <div class="controls">
                        <grouper:combobox2 idBase="inviteAddGroupCombo" style="width: 30em"
                           value="${grouperRequestContainer.groupContainer.guiGroup.group.id}"
                          filterOperation="../app/UiV2ExternalEntities.addGroupFilter"/>
                        <span id="inviteExternalGroupComboIdErrorId"></span>
                        
                        <br />
                        <span class="help-block">${textContainer.text['inviteExternalComboHelpText'] }</span>

                      </div>
                      <br />
                      <div class="control-group" style="margin-bottom: 5px">
                        <div class="controls"><a href="#" id="addAnotherGroupButtonId"
                          onclick="ajax('../app/UiV2ExternalEntities.inviteAddGroup', {formIds: 'inviteFormId'}); return false;"
                          class="btn bulk-add-another-group">${textContainer.text['inviteExternalAddAnotherGroupButton']}</a></div>
                      </div>
    
                      <div class="controls">
                        <div id="inviteExtraGroupsDivId">
                          <%@ include file="inviteExtraGroups.jsp"%>
                        </div>
                      </div>
                    </div>
                  </div>                  
                  
                  <div class="form-actions"><a href="#" 
                    onclick="ajax('../app/UiV2ExternalEntities.inviteSubmit', {formIds: 'inviteFormId'}); return false;"
                    class="btn btn-primary">${textContainer.text['inviteExternalInviteButton']}</a> 
                  <a href="#" onclick="return guiV2link('operation=UiV2Group.viewGroup&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}');"
                    class="btn btn-cancel">${textContainer.text['inviteExternalCancelButton'] }</a></div>
                </form>
                
                
              </div>
            </div>
