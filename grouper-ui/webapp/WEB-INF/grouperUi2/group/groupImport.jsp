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
                <h1>${textContainer.text['groupImportTitle'] }</h1>
              </div>
            </div>

            <div id="group-search" tabindex="-1" role="dialog" aria-labelledby="group-search-label" aria-hidden="true" class="modal hide fade">
              <div class="modal-header"><a href="#" data-dismiss="modal" aria-hidden="true" class="close">x</a>
                <h3 id="group-search-label">Search for a group</h3>
              </div>
              <div class="modal-body">
                <form class="form form-inline">
                  <input type="text" placeholder="Search for a group" value=""/> 
                  <button class="btn">Search</button>
                </form>
                <p>The table below lists groups in which you are allowed to manage memberships.</p>
                <table class="table table-hover table-bordered table-striped table-condensed data-table">
                  <thead>
                    <tr>
                      <th class="sorted">Folder</th>
                      <th>Group Name</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr>
                      <td>Root : Applications : Directories</td>
                      <td><i class="fa fa-group"></i> <a href="#" data-dismiss="modal">Admins</a></td>
                    </tr>
                    <tr>
                      <td>Root : Applications : Directories</td>
                      <td><i class="fa fa-group"></i> <a href="#" data-dismiss="modal">Managers</a></td>
                    </tr>
                    <tr>
                      <td>Root : Applications : Wiki</td>
                      <td><i class="fa fa-group"></i> <a href="#" data-dismiss="modal">Approvers</a></td>
                    </tr>
                    <tr>
                      <td>Root : Applications : Wiki</td>
                      <td><i class="fa fa-group"></i> <a href="#" data-dismiss="modal">Editors</a></td>
                    </tr>
                    <tr>
                      <td>Root : Applications : Wiki</td>
                      <td><i class="fa fa-group"></i> <a href="#" data-dismiss="modal">Interns</a></td>
                    </tr>
                    <tr>
                      <td>Root : Applications : Wiki</td>
                      <td><i class="fa fa-group"></i> <a href="#" data-dismiss="modal">Senior Approvers</a></td>
                    </tr>
                    <tr>
                      <td>Root : Applications : Wiki</td>
                      <td><i class="fa fa-group"></i> <a href="#" data-dismiss="modal">Senior Editors</a></td>
                    </tr>
                    <tr>
                      <td>Root : Applications : Virtual Private Network</td>
                      <td><i class="fa fa-group"></i> <a href="#" data-dismiss="modal">Admins</a></td>
                    </tr>
                    <tr>
                      <td>Root : Applications : Virtual Private Network</td>
                      <td><i class="fa fa-group"></i> <a href="#" data-dismiss="modal">Senior Managers</a></td>
                    </tr>
                    <tr>
                      <td>Root : Applications : Virtual Private Network</td>
                      <td><i class="fa fa-group"></i> <a href="#" data-dismiss="modal">Support</a></td>
                    </tr>
                  </tbody>
                </table>
                <div class="data-table-bottom clearfix">
                  <div class="pull-right">Showing 1-10 of 25 &middot; <a href="#">First</a> | <a href="#">Prev</a> | <a href="#">Next</a> | <a href="#">Last</a></div>    
                </div>
              </div>
              <div class="modal-footer">
                <button data-dismiss="modal" aria-hidden="true" class="btn">Close</button>
              </div>
            </div>


            <form class="form-horizontal">
              <div class="bulk-add-group-input-container">
                <div class="control-group bulk-add-group-block">
                  <label for="add-entities" style="position:absolute" class="control-label">${textContainer.text['groupImportAddMembersToGroupLabel'] }</label>
                  <div class="controls">
                    <grouper:combobox2 idBase="groupImportGroupCombo" style="width: 30em" 
                       filterOperation="../app/UiV2Group.groupUpdateFilter" />
                    <br />
                    <%-- onclick="$('#groupSearchResults').empty();" --%>
                    ${textContainer.text['groupImportGroupLabelPreComboLink']} <a href="#group-search"  role="button" data-toggle="modal" style="text-decoration: underline !important;">${textContainer.text['groupImportGroupSearchLink']}</a>
                    
                    <div>
                    a:b:c <i class="fa fa-times"></i><br />
                    a:b:c
                    </div>
                    
                  </div>
                </div>
                <div class="control-group">
                  <div class="controls"><a href="#" class="btn bulk-add-another-group">Add another group</a></div>
                </div>
              </div>
            </form>


