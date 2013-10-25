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
                <div class="row-fluid">
                  <div class="span10">
                    <h1><i class="icon-folder-close"></i> ${grouper:escapeHtml(grouperRequestContainer.stemContainer.guiStem.stem.displayExtension)}</h1>
                    <p>${grouper:escapeHtml(grouperRequestContainer.stemContainer.guiStem.stem.description)}</p>
                    <div id="stemDetailsId" style="display: none;">
                      <table class="table table-condensed table-striped">
                        <tbody>
                          <tr>
                            <td><strong>${textContainer.text['stemLabelPath'] }</strong></td>
                            <td>${grouper:escapeHtml(grouperRequestContainer.stemContainer.guiStem.pathColonSpaceSeparated)}</td>
                          </tr>
                          <tr>
                            <td><strong>${textContainer.text['stemLabelIdPath'] }</strong></td>
                            <td>${grouper:escapeHtml(grouperRequestContainer.stemContainer.guiStem.stem.name)}</td>
                          </tr>
                          <tr>
                            <td><strong>${textContainer.text['stemLabelAlternateIdPath'] }</strong></td>
                            <td>${grouper:escapeHtml(grouperRequestContainer.stemContainer.guiStem.stem.alternateName)}</td>
                          </tr>
                          <tr>
                            <td><strong>${textContainer.text['stemLabelId'] }</strong></td>
                            <td>${grouper:escapeHtml(grouperRequestContainer.stemContainer.guiStem.stem.extension)}</td>
                          </tr>
                          <tr>
                            <td><strong>${textContainer.text['stemLabelCreated'] }</strong></td>
                            <td>${grouper:formatDateLong(grouperRequestContainer.stemContainer.guiStem.stem.createTimeLong)}</td>
                          </tr>
                          <tr>
                            <td><strong>${textContainer.text['stemLabelCreator'] }</strong></td>
                            <td>${grouper:subjectStringLabelShort2fromMemberId(grouperRequestContainer.stemContainer.guiStem.stem.creatorUuid)}</td>
                          </tr>
                          <tr>
                            <td><strong>${textContainer.text['stemLabelLastEdited'] }</strong></td>
                            <td>${grouper:formatDateLong(grouperRequestContainer.stemContainer.guiStem.stem.modifyTimeLong)}</td>
                          </tr>
                          <tr>
                            <td><strong>${textContainer.text['stemLabelLastEditor'] }</strong></td>
                            <td>${grouper:subjectStringLabelShort2fromMemberId(grouperRequestContainer.stemContainer.guiStem.stem.modifierUuid)}</td>
                          </tr>
                          
                          
                          
                        </tbody>
                      </table>
                    </div>
                    <p id="stemDetailsMoreId"><a href="#" onclick="$('#stemDetailsId').show('slow'); $('#stemDetailsMoreId').hide(); $('#stemDetailsLessId').show(); return false" >${textContainer.text['guiMore']} <i class="icon-angle-down"></i></a></p>
                    <p id="stemDetailsLessId" style="display: none"><a href="#" onclick="$('#stemDetailsId').hide('slow'); $('#stemDetailsLessId').hide(); $('#stemDetailsMoreId').show(); return false" >${textContainer.text['guiLess']} <i class="icon-angle-up"></i></a></p>
                     
                  </div>
                  <div class="span2"><a href="edit-folder.html" class="btn btn-medium btn-block btn-primary">Edit folder</a>
                    <div class="btn-group btn-block"><a data-toggle="dropdown" href="#" class="btn btn-medium btn-block dropdown-toggle">More actions <span class="caret"></span></a>
                      <ul class="dropdown-menu dropdown-menu-right">
                        <li><a href="#" class="add-to-my-favorites">Add to My Favorites</a></li>
                        <li class="divider"></li>
                        <li><a href="copy-folder.html">Copy folder</a></li>
                        <li><a href="delete-folder.html">Delete folder</a></li>
                        <li><a href="edit-folder.html">Edit folder</a></li>
                        <li><a href="move-folder.html">Move folder</a></li>
                        <li class="divider"></li>
                        <li><a href="voew-audit-log.html">View audit log</a></li>
                      </ul>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <div class="row-fluid">
              <div class="span12">
                <ul class="nav nav-tabs">
                  <li class="active"><a href="view-folder-applications.html">Folder Contents</a></li>
                  <li><a href="#">Privileges</a></li>
                </ul>
                <form class="form-inline form-filter">
                  <div class="row-fluid">
                    <div class="span1">
                      <label for="table-filter" style="white-space: nowrap;">Filter for:</label>
                    </div>
                    <div class="span4">
                      <input type="text" placeholder="Folder or group name" id="table-filter" class="span12"/>
                    </div>
                    <div class="span3"><a class="btn">Apply filter</a> <a class="btn">Reset</a></div>
                  </div>
                </form>
                <table class="table table-hover table-bordered table-striped table-condensed data-table">
                  <thead>
                    <tr>
                      <th class="sorted">Folder or Group Name</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr>
                      <td><i class="icon-chevron-up"></i> <a href="index.html">Up one folder</a></td>
                    </tr>
                    <tr>
                      <td><i class="icon-folder-close"></i><a href="#"> Directories</a>
                      </td>
                    </tr>
                    <tr>
                      <td><i class="icon-folder-close"></i><a href="#"> Service Q</a>
                      </td>
                    </tr>
                    <tr>
                      <td><i class="icon-folder-close"></i><a href="#"> Virtual Private Network</a>
                      </td>
                    </tr>
                    <tr>
                      <td><i class="icon-folder-close"></i><a href="view-folder.html"> Wiki</a>
                      </td>
                    </tr>
                    <tr>
                      <td><i class="icon-folder-close"></i><a href="#"> Wordpress</a>
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
