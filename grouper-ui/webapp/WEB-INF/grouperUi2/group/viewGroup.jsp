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
                        <form class="form form-inline">
                          <input type="text" placeholder="Search for an entity" value="smith"/> 
                          <button class="btn">Search</button>
                        </form>
                        <table class="table table-hover table-bordered table-striped table-condensed">
                          <thead>
                            <tr>
                              <th class="sorted">Entity Name</th>
                            </tr>
                          </thead>
                          <tbody>
                            <tr>
                              <td><a href="#" data-dismiss="modal"><i class="icon-user"></i> Smith, Jane</a></td>
                            </tr>
                            <tr>
                              <td><a href="#" data-dismiss="modal"><i class="icon-user"></i> Smith, Joe</a></td>
                            </tr>
                            <tr>
                              <td><a href="#" data-dismiss="modal"><i class="icon-user"></i> Smith, Michael</a></td>
                            </tr>
                            <tr>
                              <td><a href="#" data-dismiss="modal"><i class="icon-group"></i> The Smith Group</a></td>
                            </tr>
                          </tbody>
                        </table>
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
                                <div id="autocomplete">
                                  <p><strong><i>Favorites</i></strong><br />Danforth, Joe (jdanforth)<br />Smith, Daniel (dsmith)</p>
                                  <p><strong><i>All Results</i></strong><br />The Dan Group (dangroup)<br />Daniels, Bob (bdaniels)<br />Danielson, Jane (jdanielson)<br />...<i>too many results</i></p>
                                </div>
                                <input type="text" placeholder="Enter the name of a person, group, or other entity" id="add-block-input"/> <a href="#member-search" role="button" data-toggle="modal" class="btn"><i class="icon-search"></i></a>
                              </div>
                            </div>
                          </div>
                          <div id="add-members-privileges-select" class="control-group">
                            <label class="control-label">Assign these privileges:</label>
                            <div class="controls">
                              <label class="radio inline">
                                <input type="radio" id="priv1" value="default" name="privilege-options" checked="checked"/>Default privileges
                              </label>
                              <label class="radio inline">
                                <input type="radio" id="priv2" value="custom" name="privilege-options"/>Custom privileges
                              </label>
                            </div>
                          </div>
                          <div id="add-members-privileges" class="control-group hide">
                            <div class="controls">
                              <label class="checkbox inline">
                                <input type="checkbox" id="privilege1" value="ADMIN" checked="checked"/>MEMBER
                              </label>
                              <label class="checkbox inline">
                                <input type="checkbox" id="privilege2" value="ADMIN"/>ADMIN
                              </label>
                              <label class="checkbox inline">
                                <input type="checkbox" id="privilege3" value="UPDATE"/>UPDATE
                              </label>
                              <label class="checkbox inline">
                                <input type="checkbox" id="privilege4" value="READ" checked="checked"/>READ
                              </label>
                              <label class="checkbox inline">
                                <input type="checkbox" id="privilege5" value="VIEW" checked="checked"/>VIEW
                              </label>
                              <label class="checkbox inline">
                                <input type="checkbox" id="privilege6" value="OPTIN"/>OPTIN
                              </label>
                              <label class="checkbox inline">
                                <input type="checkbox" id="privilege7" value="OPTOUT"/>OPTOUT
                              </label>
                            </div>
                          </div>
                          <div class="control-group">
                            <div class="controls">
                              <button id="add-members-submit" type="submit" class="btn btn-primary">Add</button> or <a href="bulk-add.html" class="blue-link">import a list of members</a> from a file.
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
                  <div class="span2"><a id="show-add-block" href="#" class="btn btn-medium btn-primary btn-block"><i class="icon-plus"></i> Add members</a>
                    <div class="btn-group btn-block"><a data-toggle="dropdown" href="#" class="btn btn-medium btn-block dropdown-toggle">More actions <span class="caret"></span></a>
                      <ul class="dropdown-menu dropdown-menu-right">
                        <li><a href="#" class="add-to-my-favorites">Add to My Favorites</a></li>
                        <li><a href="join-group.html">Join group</a></li>
                        <li class="divider"></li>
                        <li><a href="copy-group.html">Copy group</a></li>
                        <li><a href="delete-group.html">Delete group</a></li>
                        <li><a href="edit-group.html">Edit group</a></li>
                        <li><a href="move-group.html">Move group</a></li>
                        <li class="divider"></li>
                        <li><a href="export-group.html">Export members</a></li>
                        <li><a href="bulk-add.html">Import members</a></li>
                        <li><a href="invite-external-users.html">Invite external users</a></li>
                        <li><a href="remove-all-members.html">Remove all members</a></li>
                        <li class="divider"></li>
                        <li><a href="view-audit-log.html">View audit log</a></li>
                      </ul>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <div class="row-fluid">
              <div class="span12">
                <div id="messages"></div>
                <ul class="nav nav-tabs">
                  <li class="active"><a href="view-group.html">Members</a></li>
                  <li><a href="view-group-privileges.html">Privileges</a></li>
                  <li class="dropdown"><a data-toggle="dropdown" href="#" class="dropdown-toggle">More <b class="caret"></b></a>
                    <ul class="dropdown-menu">
                      <li><a href="view-group-membership.html">This Group's Memberships</a></li>
                      <li><a href="view-group-group-privileges.html">This Group's Privileges</a></li>
                    </ul>
                  </li>
                </ul>
                <p class="lead">The following table lists all entities which are members of this group.</p>
                <form class="form-inline form-small form-filter">
                  <div class="row-fluid">
                    <div class="span1">
                      <label for="people-filter">Filter for:</label>
                    </div>
                    <div class="span4">
                      <select id="people-filter" class="span12">
                        <option>All members</option>
                        <option>Direct members</option>
                        <option>Indirect members</option>
                      </select>
                    </div>
                    <div class="span4">
                      <input type="text" placeholder="Member name" class="span12">
                    </div>
                    <div class="span3">
                      <button type="submit" class="btn">Apply filter</button>
                      <button type="submit" class="btn">Reset</button>
                    </div>
                  </div>
                </form>
                <table class="table table-hover table-bordered table-striped table-condensed data-table table-bulk-update footable">
                  <thead>
                    <tr>
                      <td colspan="4" class="table-toolbar gradient-background"><a class="btn">Remove selected members</a></td>
                    </tr>
                    <tr>
                      <th>
                        <label class="checkbox checkbox-no-padding">
                          <input type="checkbox">
                        </label>
                      </th>
                      <th class="sorted">Entity Name</th>
                      <th data-hide="phone">Membership</th>
                      <th style="width:100px;"></th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr>
                      <td>
                        <label class="checkbox checkbox-no-padding">
                          <input type="checkbox"/>
                        </label>
                      </td>
                      <td class="expand foo-clicker"><i class="icon-group"></i> Senior Administrators
                      </td>
                      <td data-hide="phone">Direct</td>
                      <td>
                        <div class="btn-group"><a data-toggle="dropdown" href="#" class="btn btn-mini dropdown-toggle">Actions <span class="caret"></span></a>
                          <ul class="dropdown-menu dropdown-menu-right">
                            <li><a href="edit-person-membership.html">Edit membership &amp; privileges</a></li>
                            <li><a href="#" class="actions-revoke-membership">Revoke membership</a></li>
                            <li><a href="view-group.html">View group</a>
                            </li>
                          </ul>
                        </div>
                      </td>
                    </tr>
                    <tr>
                      <td>
                        <label class="checkbox checkbox-no-padding">
                          <input type="checkbox"/>
                        </label>
                      </td>
                      <td class="expand foo-clicker"><i class="icon-group"></i> Staff
                      </td>
                      <td data-hide="phone">Direct</td>
                      <td>
                        <div class="btn-group"><a data-toggle="dropdown" href="#" class="btn btn-mini dropdown-toggle">Actions <span class="caret"></span></a>
                          <ul class="dropdown-menu dropdown-menu-right">
                            <li><a href="edit-person-membership.html">Edit membership &amp; privileges</a></li>
                            <li><a href="#" class="actions-revoke-membership">Revoke membership</a></li>
                            <li><a href="view-group.html">View group</a>
                            </li>
                          </ul>
                        </div>
                      </td>
                    </tr>
                    <tr>
                      <td>
                        <label class="checkbox checkbox-no-padding">
                          <input type="checkbox"/>
                        </label>
                      </td>
                      <td class="expand foo-clicker"><i class="icon-user"></i> Abbott, Jane
                      </td>
                      <td data-hide="phone">Direct</td>
                      <td>
                        <div class="btn-group"><a data-toggle="dropdown" href="#" class="btn btn-mini dropdown-toggle">Actions <span class="caret"></span></a>
                          <ul class="dropdown-menu dropdown-menu-right">
                            <li><a href="edit-person-membership.html">Edit membership &amp; privileges</a></li>
                            <li><a href="#" class="actions-revoke-membership">Revoke membership</a></li>
                            <li><a href="view-person.html">View profile</a>
                            </li>
                          </ul>
                        </div>
                      </td>
                    </tr>
                    <tr>
                      <td>
                        <label class="checkbox checkbox-no-padding">
                          <input type="checkbox"/>
                        </label>
                      </td>
                      <td class="expand foo-clicker"><i class="icon-user"></i> Carlin, Hank
                      </td>
                      <td data-hide="phone">Direct</td>
                      <td>
                        <div class="btn-group"><a data-toggle="dropdown" href="#" class="btn btn-mini dropdown-toggle">Actions <span class="caret"></span></a>
                          <ul class="dropdown-menu dropdown-menu-right">
                            <li><a href="edit-person-membership.html">Edit membership &amp; privileges</a></li>
                            <li><a href="#" class="actions-revoke-membership">Revoke membership</a></li>
                            <li><a href="view-person.html">View profile</a>
                            </li>
                          </ul>
                        </div>
                      </td>
                    </tr>
                    <tr>
                      <td>
                        <label class="checkbox checkbox-no-padding">
                          <input type="checkbox"/>
                        </label>
                      </td>
                      <td class="expand foo-clicker"><i class="icon-user"></i> Daniels, Joe
                      </td>
                      <td data-hide="phone">Direct, Indirect</td>
                      <td>
                        <div class="btn-group"><a data-toggle="dropdown" href="#" class="btn btn-mini dropdown-toggle">Actions <span class="caret"></span></a>
                          <ul class="dropdown-menu dropdown-menu-right">
                            <li><a href="edit-person-membership.html">Edit membership &amp; privileges</a></li>
                            <li><a href="#" class="actions-revoke-membership">Revoke membership</a></li>
                            <li><a href="trace-membership.html">Trace membership</a></li>
                            <li><a href="view-person.html">View profile</a>
                            </li>
                          </ul>
                        </div>
                      </td>
                    </tr>
                    <tr>
                      <td>
                        <label class="checkbox checkbox-no-padding">
                          <input type="checkbox"/>
                        </label>
                      </td>
                      <td class="expand foo-clicker"><i class="icon-user"></i> Jules, Mark
                      </td>
                      <td data-hide="phone">Direct</td>
                      <td>
                        <div class="btn-group"><a data-toggle="dropdown" href="#" class="btn btn-mini dropdown-toggle">Actions <span class="caret"></span></a>
                          <ul class="dropdown-menu dropdown-menu-right">
                            <li><a href="edit-person-membership.html">Edit membership &amp; privileges</a></li>
                            <li><a href="#" class="actions-revoke-membership">Revoke membership</a></li>
                            <li><a href="view-person.html">View profile</a>
                            </li>
                          </ul>
                        </div>
                      </td>
                    </tr>
                    <tr>
                      <td>
                        <label class="checkbox checkbox-no-padding">
                          <input type="checkbox" disabled="disabled"/>
                        </label>
                      </td>
                      <td class="expand foo-clicker"><i class="icon-user"></i> Knotts, Danielle
                      </td>
                      <td data-hide="phone">Indirect</td>
                      <td>
                        <div class="btn-group"><a data-toggle="dropdown" href="#" class="btn btn-mini dropdown-toggle">Actions <span class="caret"></span></a>
                          <ul class="dropdown-menu dropdown-menu-right">
                            <li><a href="trace-membership.html">Trace membership</a></li>
                            <li><a href="view-person.html">View profile</a>
                            </li>
                          </ul>
                        </div>
                      </td>
                    </tr>
                    <tr>
                      <td>
                        <label class="checkbox checkbox-no-padding">
                          <input type="checkbox" disabled="disabled"/>
                        </label>
                      </td>
                      <td class="expand foo-clicker"><i class="icon-user"></i> Lee, Wendy
                      </td>
                      <td data-hide="phone">Indirect</td>
                      <td>
                        <div class="btn-group"><a data-toggle="dropdown" href="#" class="btn btn-mini dropdown-toggle">Actions <span class="caret"></span></a>
                          <ul class="dropdown-menu dropdown-menu-right">
                            <li><a href="trace-membership.html">Trace membership</a></li>
                            <li><a href="view-person.html">View profile</a>
                            </li>
                          </ul>
                        </div>
                      </td>
                    </tr>
                    <tr>
                      <td>
                        <label class="checkbox checkbox-no-padding">
                          <input type="checkbox"/>
                        </label>
                      </td>
                      <td class="expand foo-clicker"><i class="icon-user"></i> Oswalt, Michael
                      </td>
                      <td data-hide="phone">Direct</td>
                      <td>
                        <div class="btn-group"><a data-toggle="dropdown" href="#" class="btn btn-mini dropdown-toggle">Actions <span class="caret"></span></a>
                          <ul class="dropdown-menu dropdown-menu-right">
                            <li><a href="edit-person-membership.html">Edit membership &amp; privileges</a></li>
                            <li><a href="#" class="actions-revoke-membership">Revoke membership</a></li>
                            <li><a href="view-person.html">View profile</a>
                            </li>
                          </ul>
                        </div>
                      </td>
                    </tr>
                    <tr>
                      <td>
                        <label class="checkbox checkbox-no-padding">
                          <input type="checkbox"/>
                        </label>
                      </td>
                      <td class="expand foo-clicker"><i class="icon-user"></i> Williams, George
                      </td>
                      <td data-hide="phone">Direct</td>
                      <td>
                        <div class="btn-group"><a data-toggle="dropdown" href="#" class="btn btn-mini dropdown-toggle">Actions <span class="caret"></span></a>
                          <ul class="dropdown-menu dropdown-menu-right">
                            <li><a href="edit-person-membership.html">Edit membership &amp; privileges</a></li>
                            <li><a href="#" class="actions-revoke-membership">Revoke membership</a></li>
                            <li><a href="view-person.html">View profile</a>
                            </li>
                          </ul>
                        </div>
                      </td>
                    </tr>
                  </tbody>
                </table>
                <div class="data-table-bottom gradient-background">
                  <div class="pull-right">Showing 1-10 of 25 &middot; <a href="#">First</a> | <a href="#">Prev</a> | <a href="#">Next</a> | <a href="#">Last</a></div>
                  <form class="form-inline form-small">
                    <label for="show-entries">Show:&nbsp;</label>
                    <select id="show-entries" class="span2">
                      <option>10</option>
                      <option>25</option>
                      <option>50</option>
                      <option>100</option>
                    </select>
                  </form>
                </div>
              </div>
            </div>
            <!-- end group/viewGroup.jsp -->