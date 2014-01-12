<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <div class="bread-header-container">
              <%--
              <ul class="breadcrumb">
                <li><a href="index.html">Home </a><span class="divider"><i class='icon-angle-right'></i></span></li>
                <li class="active">Search Results</li>
              </ul>
              --%>
              <ul class="breadcrumb">
                <li><a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');">${textContainer.text['myServicesHomeBreadcrumb'] }</a><span class="divider"><i class='icon-angle-right'></i></span></li>
                <li class="active">${textContainer.text['myServicesBreadcrumb'] }</li>
              </ul>
              <div class="page-header blue-gradient">
                <h1>${textContainer.text['myServicesTitle'] }</h1>
              </div>

            </div>
            <div class="row-fluid">
              <div class="span12">
                <form class="form-inline form-filter" id="myServicesForm"
                    onsubmit="ajax('../app/UiV2Main.myServicesSubmit', {formIds: 'myServicesForm'}); return false;">
                  <div class="row-fluid">

                    <div class="span1">
                      <label for="myServicesFilterId" style="white-space: nowrap;">${textContainer.text['myServicesFilterFor'] }</label>
                    </div>
                    <div class="span3"  style="white-space: nowrap;">
                      <input type="text" name="myServicesFilter" placeholder="${textContainer.textEscapeXml['myServicesSearchNamePlaceholder'] }" id="myServicesFilterId" class="span12"/>
                    </div>
                    <div class="span3"> &nbsp; &nbsp;
                      <button type="submit" class="btn" onclick="ajax('../app/UiV2Main.myServicesSubmit', {formIds: 'myServicesForm'}); return false;">${textContainer.text['myServicesApplyFilterButton'] }</button>
                      <button type="submit" onclick="ajax('../app/UiV2Main.myServicesReset'); return false;" class="btn">${textContainer.text['myServicesResetButton'] }</button>
                    </div>
                  </div>
                </form>
                <div id="myServicesResultsId">
                </div>
              </div>


