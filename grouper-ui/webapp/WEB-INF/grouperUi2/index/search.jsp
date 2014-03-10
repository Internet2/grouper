<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <div class="bread-header-container">
              <%--
              <ul class="breadcrumb">
                <li><a href="index.html">Home </a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                <li class="active">Search Results</li>
              </ul>
              --%>
              <ul class="breadcrumb">
                <li><a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');">Home </a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                <li class="active">${textContainer.text['searchBreadcrumb'] }</li>
              </ul>
              <div class="page-header blue-gradient">
                <h1>${textContainer.text['searchTitle'] }</h1>
              </div>

            </div>
            <div class="row-fluid">
              <div class="span12">
                <form class="form-inline form-small form-filter" id="searchPageForm"
                    onsubmit="ajax('../app/UiV2Main.searchFormSubmit', {formIds: 'searchPageForm'}); return false;">
                  <div class="row-fluid">
                    <div class="span2">
                      <label for="searchFormSearch">Search for:</label>
                    </div>
                    <div class="span3" style="white-space: nowrap;">
                      <input type="text" name="searchQuery" id="searchQueryId" value="${grouper:escapeHtml(grouperRequestContainer.indexContainer.searchQuery) }" style="width: 25em" />
                    </div>
                  </div>
                  <div class="row-fluid" style="margin-top: 5px">
                    <div class="span2">
                      <label for="people-filter">Filter for:</label>
                    </div>
                    <div class="span3">
                      <select id="people-filter" class="span12" name="filterType">
                        <option value="all">${textContainer.text['searchTypeAll'] }</option>
                        <option value="stems">${textContainer.text['searchTypeStems'] }</option>
                        <option value="groups">${textContainer.text['searchTypeGroups'] }</option>
                        <option value="subjects">${textContainer.text['searchTypeSubjects'] }</option>
                        <option value="attributeDefNames">${textContainer.text['searchTypeAttributeDefNames'] }</option>
                        <option value="attributeDefs">${textContainer.text['searchTypeAttributeDefs'] }</option>
                      </select>
                    </div>
                  </div>
                  <div class="form-actions">
                    <div class="span2">&nbsp;</div>
                    <a href="#" class="btn btn-primary" onclick="ajax('../app/UiV2Main.searchFormSubmit', {formIds: 'searchPageForm'}); return false;">${textContainer.text['searchButton'] }</a> 
                    &nbsp;
                    <a href="#" onclick="ajax('../app/UiV2Main.searchReset'); return false;" class="btn btn-cancel">${textContainer.text['searchResetButton'] }</a>
                  </div>
                </form>
                <%-- this div will be filled with searchContents.jsp via ajax --%>
                <div id="searchResultsId">
                </div>
              </div>
            </div>
            