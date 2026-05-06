<%@ include file="../assetsJsp/commonTaglib.jsp"%>

${grouper:title('myPreferencesPageTitle')}

            <div class="bread-header-container">
              <ul class="breadcrumb">
                <li><a href="?operation=UiV2Main.indexMain" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Main.indexMain');">${textContainer.text['myPreferencesHomeBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                <li class="active">${textContainer.text['myPreferencesBreadcrumb'] }</li>
              </ul>
              <div class="page-header blue-gradient">
                <h1>${textContainer.text['myPreferencesTitle'] }</h1>
              </div>

            </div>
            <div class="row-fluid">
              <div class="span12">

                <p class="lead">${textContainer.text['myPreferencesDescription'] }</p>

                <form class="form-horizontal" id="myPreferencesForm">
                  <div class="control-group">
                    <label class="control-label" for="myPreferencesGroupMembersDefaultDirectId">${textContainer.text['myPreferencesGroupMembersDefaultDirectLabel'] }</label>
                    <div class="controls">
                      <select id="myPreferencesGroupMembersDefaultDirectId" name="defaultMembershipType">
                        <option value="">${textContainer.text['groupFilterAllAssignments'] }</option>
                        <option value="IMMEDIATE" ${grouperRequestContainer.indexContainer.groupMembersDefaultDirect ? 'selected="selected"' : ''}>${textContainer.text['groupFilterDirectAssignments'] }</option>
                      </select>
                      <span class="help-block">${textContainer.text['myPreferencesGroupMembersDefaultDirectDescription'] }</span>
                    </div>
                  </div>
                  <div class="form-actions">
                    <button type="submit" class="btn btn-primary"
                        onclick="ajax('../app/UiV2Main.myPreferencesSubmit', {formIds: 'myPreferencesForm'}); return false;">${textContainer.text['myPreferencesSaveButton'] }</button>
                  </div>
                </form>

              </div>
            </div>
