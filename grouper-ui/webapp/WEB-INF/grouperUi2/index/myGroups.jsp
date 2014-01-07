<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <div class="bread-header-container">
              <%--
              <ul class="breadcrumb">
                <li><a href="index.html">Home </a><span class="divider"><i class='icon-angle-right'></i></span></li>
                <li class="active">Search Results</li>
              </ul>
              --%>
              <ul class="breadcrumb">
                <li><a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');">Home </a><span class="divider"><i class='icon-angle-right'></i></span></li>
                <li class="active">${textContainer.text['myGroupsBreadcrumb'] }</li>
              </ul>
              <div class="page-header blue-gradient">
                <h1>${textContainer.text['myGroupsTitle'] }</h1>
              </div>

            </div>
            <div class="row-fluid">
              <div class="span12">
                <ul class="nav nav-tabs">
                  <li class="active"><a href="#" onclick="return false;">${textContainer.text['myGroupsTabMyGroups'] }</a></li>
                  <li><a href="my-groups-memberships.html">${textContainer.text['myGroupsTabMyMemberships'] }</a></li>
                  <li><a href="my-groups-join.html">${textContainer.text['myGroupsTabGroupsCanJoin'] }</a></li>
                </ul>
                <p class="lead">${textContainer.text['myGroupsDescription'] }</p>
                <form class="form-inline form-filter" id="myGroupsForm"
                    onsubmit="ajax('../app/UiV2Main.myGroupsSubmit', {formIds: 'myGroupsForm'}); return false;">
                  <div class="row-fluid">
                    <div class="span1">
                      <label for="myGroupsFilterId">${textContainer.text['myGroupsFilterFor'] }</label>
                    </div>
                    <div class="span4" style="white-space: nowrap;">
                      <input type="text" name="myGroupsFilter" placeholder="${textContainer.text['myGroupsSearchNamePlaceholder'] }" id="myGroupsFilterId" class="span12"/>
                    </div>
                    
                    <div class="span3"><a class="btn" href="#" onclick="ajax('../app/UiV2Main.myGroupsSubmit', {formIds: 'myGroupsForm'}); return false;">${textContainer.text['myGroupsSearchButton'] }</a> &nbsp;
                    <a href="#" onclick="ajax('../app/UiV2Main.myGroupsReset'); return false;" class="btn">${textContainer.text['myGroupsResetButton'] }</a></div>
                  </div>
                </form>
                <div id="myGroupsResultsId">
                </div>
              </div>


