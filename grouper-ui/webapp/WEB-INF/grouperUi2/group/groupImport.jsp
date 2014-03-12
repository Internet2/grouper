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
                <h3 id="group-search-label">${textContainer.text['groupImportSearchForGroupButton']}</h3>
              </div>

              <div class="modal-body">
                <form class="form form-inline" id="addGroupSearchFormId">
                  <input id="addGroupSearchId" name="addGroupSearch" type="text" placeholder="${textContainer.text['groupImportSearchGroupPlaceholder']}" />
                  <button class="btn" onclick="ajax('../app/UiV2Group.groupImportGroupSearch', {formIds: 'addGroupSearchFormId'}); return false;" >${textContainer.text['groupImportSearchButton'] }</button>
                  <br />
                  <span style="white-space: nowrap;"><input type="checkbox" name="matchExactId" value="true"/> ${textContainer.text['subjectSearchExactIdMatch'] }</span>
                </form>
                <div id="addGroupResults">
                </div>
              </div>
              <div class="modal-footer">
                <button data-dismiss="modal" aria-hidden="true" class="btn">${textContainer.text['subjectSearchCloseButton']}</button>
              </div>
            </div>


            <form class="form-horizontal" id="importGroupFormId">
              <div class="bulk-add-group-input-container">
                <div class="control-group bulk-add-group-block">
                  <label for="add-entities" style="position:absolute" class="control-label">${textContainer.text['groupImportAddMembersToGroupLabel'] }</label>
                  <div class="controls">
                    <grouper:combobox2 idBase="groupImportGroupCombo" style="width: 30em" 
                       value="${grouperRequestContainer.groupContainer.guiGroup.group.id}"
                       filterOperation="../app/UiV2Group.groupUpdateFilter" />
                    <br />
                    <%-- onclick="$('#groupSearchResults').empty();" --%>
                    ${textContainer.text['groupImportGroupLabelPreComboLink']} <a href="#group-search" 
                      onclick="$('#addGroupSearchId').val(''); $('#addGroupResults').empty(); return true;"
                      role="button" data-toggle="modal" style="text-decoration: underline !important;">${textContainer.text['groupImportGroupSearchLink']}</a>

                    <div id="groupImportExtraGroupsDivId">
                      <%@ include file="groupImportExtraGroups.jsp"%>
                    </div>

                  </div>
                </div>
                <div class="control-group">
                  <div class="controls"><a href="#" 
                    onclick="ajax('../app/UiV2Group.groupImportAddGroup', {formIds: 'importGroupFormId'}); return false;"
                    class="btn bulk-add-another-group">${textContainer.text['groupImportAddAnotherGroupButton']}</a></div>
                </div>
              </div>
            </form>


