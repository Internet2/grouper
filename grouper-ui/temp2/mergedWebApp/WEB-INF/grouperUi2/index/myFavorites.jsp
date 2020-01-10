<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <div class="bread-header-container">
              <ul class="breadcrumb">
                <li><a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');">${textContainer.text['myFavoritesHomeBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                <li class="active">${textContainer.text['myFavoritesBreadcrumb'] }</li>
              </ul>
              <div class="page-header blue-gradient">
                <h1>${textContainer.text['myFavoritesTitle'] }</h1>
              </div>

            </div>
            <div class="row-fluid">
              <div class="span12">
                <form class="form-inline form-filter" id="myFavoritesForm"
                    onsubmit="ajax('../app/UiV2Main.myFavoritesSubmit', {formIds: 'myFavoritesForm'}); return false;">
                  <div class="row-fluid">

                    <div class="span1">
                      <label for="myFavoritesFilterId" style="white-space: nowrap;">${textContainer.text['myFavoritesFilterFor'] }</label>
                    </div>
                    <div class="span3"  style="white-space: nowrap;">
                      <input type="text" name="myFavoritesFilter" placeholder="${textContainer.textEscapeXml['myFavoritesSearchNamePlaceholder'] }" id="myFavoritesFilterId" class="span12"/>
                    </div>
                    <div class="span3"> &nbsp; &nbsp;
                      <button type="submit" class="btn" aria-controls="myFavoritesResultsId" onclick="ajax('../app/UiV2Main.myFavoritesSubmit', {formIds: 'myFavoritesForm'}); return false;">${textContainer.text['myFavoritesApplyFilterButton'] }</button>
                      <button type="submit" onclick="ajax('../app/UiV2Main.myFavoritesReset'); return false;" class="btn">${textContainer.text['myFavoritesResetButton'] }</button>
                    </div>
                  </div>
                </form>
                <div id="myFavoritesResultsId" role="region" aria-live="polite">
                </div>
              </div>


