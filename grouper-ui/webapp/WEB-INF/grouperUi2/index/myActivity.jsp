<%@ include file="../assetsJsp/commonTaglib.jsp"%>

${grouper:title('myActivityPageTitle')}

            <div class="bread-header-container">
              <ul class="breadcrumb">
                <li><a href="?operation=UiV2Main.indexMain" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Main.indexMain');">${textContainer.text['myActivityHomeBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                <li class="active">${textContainer.text['myActivityBreadcrumb'] }</li>
              </ul>
              <div class="page-header blue-gradient">
                <h1>${textContainer.text['myActivityTitle'] }</h1>
              </div>

            </div>
            <div class="row-fluid">
              <div class="span12">
                <form class="form-horizontal form-filter" id="myActivityForm"
                    onsubmit="ajax('../app/UiV2Main.myActivitySubmit', {formIds: 'myActivityForm'}); return false;">
                  <fieldset style="margin:0; padding:0;">
                    <legend style="font-size: inherit; font-weight: bold; border: 0; margin-bottom: 8px; width: auto; line-height: inherit;">${textContainer.text['myActivityFilterFor'] }</legend>
                    <div class="control-group">
                      <label class="control-label" for="myActivityStartDate">${textContainer.text['myActivitySearchRangeFromPlaceholder'] }</label>
                      <div class="controls">
                        <input type="date" name="startDate" id="myActivityStartDate" style="width:100%; max-width:220px;" />
                      </div>
                    </div>
                    <div class="control-group">
                      <label class="control-label" for="myActivityEndDate">${textContainer.text['myActivitySearchRangeToPlaceholder'] }</label>
                      <div class="controls">
                        <input type="date" name="endDate" id="myActivityEndDate" style="width:100%; max-width:220px;" />
                      </div>
                    </div>
                  </fieldset>
                  <div class="form-actions">
                    <button type="submit" class="btn" aria-controls="myActivityResultsId" onclick="ajax('../app/UiV2Main.myActivitySubmit', {formIds: 'myActivityForm'}); return false;">${textContainer.text['myActivityApplyFilterButton'] }</button>
                    <button type="submit" onclick="ajax('../app/UiV2Main.myActivityReset'); return false;" class="btn">${textContainer.text['myActivityResetButton'] }</button>
                  </div>
                </form>
                <div id="myActivityResultsId" role="region" aria-live="polite">
                </div>
              </div>

            </div>