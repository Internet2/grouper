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
                    <h1><i class="icon-folder-close"></i> ${grouper:escapeHtml(grouperRequestContainer.stemContainer.guiStem.guiDisplayExtension)}</h1>
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
                          <tr>
                            <td><strong>${textContainer.text['stemLabelUuid'] }</strong></td>
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
                <form class="form-inline form-filter" id="stemFilterFormId">
                  <div class="row-fluid">
                    <div class="span1">
                      <label for="table-filter" style="white-space: nowrap;">Filter for:</label>
                    </div>
                    <div class="span4">
                      <input type="text" placeholder="${textContainer.textEscapeDouble['stemFilterFormPlaceholder']}" 
                         name="filterText" id="table-filter" class="span12"/>
                    </div>
                    <div class="span3"><input type="submit" class="btn"  id="filterSubmitId" value="${textContainer.textEscapeDouble['stemApplyFilterButton'] }"
                      onclick="ajax('../app/UiV2Stem.filter?stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}', {formIds: 'stemFilterFormId'}); return false;"> 
                    <a class="btn" onclick="$('#table-filter').val(''); $('#filterSubmitId').click(); return false;">${textContainer.text['stemResetButton'] }</a></div>
                  </div>
                </form>
                <div id="stemFilterResultsId">
                  <%@ include file="../stem/stemContents.jsp"%>
                </div>
              </div>
            </div>
