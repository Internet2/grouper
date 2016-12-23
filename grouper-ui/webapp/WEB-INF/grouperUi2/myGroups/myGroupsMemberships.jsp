<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <div class="bread-header-container">
              <ul class="breadcrumb">
                <li><a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');">${textContainer.text['myGroupsHomeBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                <li class="active">${textContainer.text['myGroupsBreadcrumb'] }</li>
              </ul>
              <div class="page-header blue-gradient">
                <h1>${textContainer.text['myGroupsTitle'] }</h1>
              </div>

            </div>
            <div class="row-fluid">
              <div class="span12">
                <ul class="nav nav-tabs">
                  <li><a role="tab" href="#" onclick="return guiV2link('operation=UiV2MyGroups.myGroups', {dontScrollTop: true});" >${textContainer.text['myGroupsTabMyGroups'] }</a></li>
                  <li class="active"><a role="tab"  aria-selected="true" href="#" onclick="return false">${textContainer.text['myGroupsTabMyMemberships'] }</a></li>
                  <li><a role="tab" href="#" onclick="return guiV2link('operation=UiV2MyGroups.myGroupsJoin', {dontScrollTop: true});" >${textContainer.text['myGroupsTabGroupsCanJoin'] }</a></li>
                </ul>
                <p class="lead">${textContainer.text['myGroupsMembershipsDescription'] }</p>
                <form class="form-inline form-filter" id="myGroupsForm"
                    onsubmit="ajax('../app/UiV2MyGroups.myGroupsMembershipsSubmit', {formIds: 'myGroupsForm, myGroupsPagingFormId'}); return false;">
                  <div class="row-fluid">
                    <div class="span1">
                      <label for="myGroupsFilterId" style="white-space: nowrap;">${textContainer.text['myGroupsFilterFor'] }</label>
                    </div>
                    <div class="span4" style="white-space: nowrap;">
                      <input type="text" name="myGroupsFilter" placeholder="${textContainer.textEscapeXml['myGroupsSearchNamePlaceholder'] }" id="myGroupsFilterId" class="span12"/>
                    </div>
                    
                    <div class="span3">&nbsp; &nbsp; <a class="btn" role="button" href="#" onclick="ajax('../app/UiV2MyGroups.myGroupsMembershipsSubmit', {formIds: 'myGroupsForm, myGroupsPagingFormId'}); return false;">${textContainer.text['myGroupsSearchButton'] }</a> &nbsp;
                    <a href="#" onclick="ajax('../app/UiV2MyGroups.myGroupsMembershipsReset', {formIds: 'myGroupsPagingFormId'}); return false;" class="btn" role="button">${textContainer.text['myGroupsResetButton'] }</a></div>
                  </div>
                </form>
                <div id="myGroupsResultsId">
                </div>
              </div>
            </div>


