<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <div class="bread-header-container">
              <ul class="breadcrumb">
                <li><a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');">${textContainer.text['myActivityHomeBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                <li class="active">${textContainer.text['myActivityBreadcrumb'] }</li>
              </ul>
              <div class="page-header blue-gradient">
                <h1>${textContainer.text['myActivityTitle'] }</h1>
              </div>

            </div>
            <div class="row-fluid">
              <div class="span12">
                <form class="form-inline form-filter" id="myActivityForm"
                    onsubmit="ajax('../app/UiV2Main.myActivitySubmit', {formIds: 'myActivityForm'}); return false;">
                  <div class="row-fluid">

                    <div class="span2">
                      <label for="myActivityFilterId" style="white-space: nowrap;">${textContainer.text['myActivityFilterFor'] }</label>
                    </div>
                    
                    <div class="span6 input-daterange" id="dateRangeFilters"  style="white-space: nowrap;">
                      <input type="text" name="startDate" placeholder="${textContainer.textEscapeXml['myActivitySearchRangeFromPlaceholder'] }" class="span6" id="myActivityStartDate" />
					  <input type="text" name="endDate" placeholder="${textContainer.textEscapeXml['myActivitySearchRangeToPlaceholder'] }" class="span6" id="myActivityEndDate" />
                    </div>
                    
                    <div class="span4"> &nbsp; &nbsp;
                      <button type="submit" class="btn" aria-controls="myActivityResultsId" onclick="ajax('../app/UiV2Main.myActivitySubmit', {formIds: 'myActivityForm'}); return false;">${textContainer.text['myActivityApplyFilterButton'] }</button>
                      <button type="submit" onclick="ajax('../app/UiV2Main.myActivityReset'); return false;" class="btn">${textContainer.text['myActivityResetButton'] }</button>
                    </div>
                    
                  </div>
                </form>
                <div id="myActivityResultsId" role="region" aria-live="polite">
                </div>
              </div>


              <script type="text/javascript">
                  $('#dateRangeFilters input').datepicker({
                	  orientation: "top auto"
                  });
              </script>