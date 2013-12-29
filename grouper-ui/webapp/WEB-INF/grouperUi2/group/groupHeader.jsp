<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <!-- start group/viewGroup.jsp -->
            <div class="bread-header-container">
              <%--
              <ul class="breadcrumb">
                <li><a href="index.html">Home </a><span class="divider"><i class='icon-angle-right'></i></span></li>
                <li><a href="#">Root </a><span class="divider"><i class='icon-angle-right'></i></span></li>
                <li><a href="view-folder-applications.html">Applications </a><span class="divider"><i class='icon-angle-right'></i></span></li>
                <li><a href="view-folder.html">Wiki </a><span class="divider"><i class='icon-angle-right'></i></span></li>
                <li class="active">Editors</li>
              </ul>
              --%>
              ${grouperRequestContainer.groupContainer.guiGroup.breadcrumbs}
              <div class="page-header blue-gradient">
                <div class="row-fluid">
                  <div class="span10">
                    <h1><i class="icon-group icon-header"></i> ${grouper:escapeHtml(grouperRequestContainer.groupContainer.guiGroup.group.displayExtension)}</h1>
                    <div id="member-search" tabindex="-1" role="dialog" aria-labelledby="member-search-label" aria-hidden="true" class="modal hide fade">
                      <div class="modal-header"><a href="#" data-dismiss="modal" aria-hidden="true" class="close">x</a>
                        <h3 id="member-search-label">Search for an entity</h3>
                      </div>
                      <div class="modal-body">
                        <form class="form form-inline" id="addMemberSearchFormId">
                          <input name="addMemberSubjectSearch" type="text" placeholder="Search for an entity"/>
                          <button class="btn" onclick="ajax('../app/UiV2Group.addMemberSearch?groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}', {formIds: 'addMemberSearchFormId'}); return false;" >Search</button>
                        </form>
                        <div id="addMemberResults">
                        </div>
                      </div>
                      <div class="modal-footer">
                        <button data-dismiss="modal" aria-hidden="true" class="btn">Close</button>
                      </div>
                    </div>
                    <div id="add-block-container" class="well hide">
                      <div id="add-members">
                        <form id="add-members-form" target="#" class="form-horizontal form-highlight">
                          <div class="control-group">
                            <label for="add-block-input" class="control-label">Member name or ID:</label>
                            <div class="controls">
                              <div id="add-members-container">

                                <%-- placeholder: Enter the name of a person, group, or other entity --%>
                                <grouper:combobox2 idBase="groupAddMemberCombo" style="width: 30em"
                                  filterOperation="../app/UiV2Group.addMemberFilter?groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}"/>
                                <%--a href="#member-search" onclick="$('#addMemberResults').empty();" role="button" data-toggle="modal" class="btn"><i class="icon-search"></i></a --%>
                                <br />
                                Enter an entity name or ID, or <a href="#member-search" onclick="$('#addMemberResults').empty();" role="button" data-toggle="modal" style="text-decoration: underline !important;">search for an entity</a>.
                                
                              </div>
                            </div>
                          </div>
                          <div id="add-members-privileges-select" class="control-group">
                            <label class="control-label">Assign these privileges:</label>
                            <div class="controls">
                              <label class="radio inline">
                                <input type="radio" id="priv1" value="default" name="privilege-options" checked="checked" onclick="this.blur();" value="true" onchange="$('#add-members-privileges').hide('slow');"/>Default privileges
                              </label>
                              <label class="radio inline">
                                <input type="radio" id="priv2" value="custom" name="privilege-options" onclick="this.blur();" value="true" onchange="$('#add-members-privileges').show('slow');"/>Custom privileges
                              </label>
                            </div>
                          </div>
                          <div id="add-members-privileges" class="control-group hide">
                            <div class="controls">
                              <label class="checkbox inline">
                                <input type="checkbox" name="privileges_members" value="true" checked="checked"/>MEMBER
                              </label>
                              <label class="checkbox inline">
                                <input type="checkbox" name="privileges_admins" value="true" 
                                  ${grouperRequestContainer.groupContainer.configDefaultGroupsCreateGrantAllAdmin ? 'checked="checked"' : '' } />ADMIN
                              </label>
                              <label class="checkbox inline">
                                <input type="checkbox" name="privileges_updaters" value="true"
                                  ${grouperRequestContainer.groupContainer.configDefaultGroupsCreateGrantAllUpdate ? 'checked="checked"' : '' } />UPDATE
                              </label>
                              <label class="checkbox inline">
                                <input type="checkbox" name="privileges_readers" value="true" 
                                  ${grouperRequestContainer.groupContainer.configDefaultGroupsCreateGrantAllRead ? 'checked="checked"' : '' } />READ
                              </label>
                              <label class="checkbox inline">
                                <input type="checkbox" name="privileges_viewers" value="true" 
                                  ${grouperRequestContainer.groupContainer.configDefaultGroupsCreateGrantAllView ? 'checked="checked"' : '' } />VIEW
                              </label>
                              <label class="checkbox inline">
                                <input type="checkbox" name="privileges_optins" value="true"
                                  ${grouperRequestContainer.groupContainer.configDefaultGroupsCreateGrantAllOptin ? 'checked="checked"' : '' } />OPTIN
                              </label>
                              <label class="checkbox inline">
                                <input type="checkbox" name="privileges_optouts" value="true" 
                                  ${grouperRequestContainer.groupContainer.configDefaultGroupsCreateGrantAllOptout ? 'checked="checked"' : '' } />OPTOUT
                              </label>
                              <label class="checkbox inline">
                                <input type="checkbox" name="privileges_groupAttrReaders" value="true" 
                                  ${grouperRequestContainer.groupContainer.configDefaultGroupsCreateGrantAllAttrRead ? 'checked="checked"' : '' } />ATTRIBUTE READ
                              </label>
                              <label class="checkbox inline">
                                <input type="checkbox" name="privileges_groupAttrUpdaters" value="true"
                                  ${grouperRequestContainer.groupContainer.configDefaultGroupsCreateGrantAllAttrUpdate ? 'checked="checked"' : '' } />ATTRIBUTE UPDATE
                              </label>
                            </div>
                          </div>
                          <div class="control-group">
                            <div class="controls">
                              <button onclick="ajax('../app/UiV2Group.addMemberSubmit?groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}', {formIds: 'add-members-form,groupFilterFormId,groupPagingFormId'}); return false;" 
                                id="add-members-submit" type="submit" class="btn btn-primary">Add</button> or <a href="bulk-add.html" class="blue-link">import a list of members</a> from a file.
                            </div>
                          </div>
                        </form>
                      </div>
                    </div>
                    <p>${grouper:escapeHtml(grouperRequestContainer.groupContainer.guiGroup.group.description)}</p>
                    <div id="groupDetailsId" style="display: none;">
                      <table class="table table-condensed table-striped">
                        <tbody>
                          <tr>
                            <td><strong>${textContainer.text['groupLabelPath']}</strong></td>
                            <td>${grouper:escapeHtml(grouperRequestContainer.groupContainer.guiGroup.pathColonSpaceSeparated)}</td>
                          </tr>
                          <tr>
                            <td><strong>${textContainer.text['groupLabelIdPath']}</strong></td>
                            <td>${grouper:escapeHtml(grouperRequestContainer.groupContainer.guiGroup.group.name)}</td>
                          </tr>
                          <tr>
                            <td><strong>${textContainer.text['groupLabelAlternateIdPath']}</strong></td>
                            <td>${grouper:escapeHtml(grouperRequestContainer.groupContainer.guiGroup.group.alternateName)}</td>
                          </tr>
                          <tr>
                            <td><strong>${textContainer.text['groupLabelId']}</strong></td>
                            <td>${grouper:escapeHtml(grouperRequestContainer.groupContainer.guiGroup.group.extension)}</td>
                          </tr>
                          <tr>
                            <td><strong>Created:</strong></td>
                            <td>Tue Sep 25 12:01:07 CDT 2012</td>
                          </tr>
                          <tr>
                            <td><strong>Creator ID (entity ID):</strong></td>
                            <td>61889660C</td>
                          </tr>
                          <tr>
                            <td><strong>Creator entity type:</strong></td>
                            <td>person</td>
                          </tr>
                          <tr>
                            <td><strong>Last editor ID (entity ID):</strong></td>
                            <td>61889660C</td>
                          </tr>
                          <tr>
                            <td><strong>Last editor entity type:</strong></td>
                            <td>person</td>
                          </tr>
                          <tr>
                            <td><strong>Last edited:</strong></td>
                            <td>Tue Sep 25 12:01:07 CDT 2012</td>
                          </tr>
                          <tr>
                            <td><strong>Entity type:</strong></td>
                            <td>group</td>
                          </tr>
                          <tr>
                            <td><strong>UUID:</strong></td>
                            <td>ab8efeb26a034b0c8c435dcd0a7a3a33</td>
                          </tr>
                        </tbody>
                      </table>
                    </div>
                    <p id="groupDetailsMoreId"><a href="#" onclick="$('#groupDetailsId').show('slow'); $('#groupDetailsMoreId').hide(); $('#groupDetailsLessId').show(); return false" >${textContainer.text['guiMore']} <i class="icon-angle-down"></i></a></p>
                    <p id="groupDetailsLessId" style="display: none"><a href="#" onclick="$('#groupDetailsId').hide('slow'); $('#groupDetailsLessId').hide(); $('#groupDetailsMoreId').show(); return false" >${textContainer.text['guiLess']} <i class="icon-angle-up"></i></a></p>
                  </div>
                  <div class="span2" id="groupMoreActionsButtonContentsDivId">
                    <%@ include file="groupMoreActionsButtonContents.jsp"%>
                  </div>
                </div>
              </div>
            </div>
