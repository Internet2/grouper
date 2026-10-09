<%@ include file="../assetsJsp/commonTaglib.jsp"%>
${grouper:title('aiAgentReportBreadcrumb')}

<div class="bread-header-container">
  <ul class="breadcrumb">
    <li><a href="?operation=UiV2Main.indexMain" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Main.indexMain');">${textContainer.text['myServicesHomeBreadcrumb'] }</a><span
      class="divider"><i class='fa fa-angle-right'></i></span></li>
    <li><a href="?operation=UiV2Main.miscellaneous" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span
      class="divider"><i class='fa fa-angle-right'></i></span></li>

    <li class="active">${textContainer.text['aiAgentReportBreadcrumb'] }</li>
  </ul>

  <div class="page-header blue-gradient">
    <div class="row-fluid">
      <div class="span10 pull-left">
        <h1>${textContainer.text['aiAgentReportTitle'] }</h1>
        <p style="margin-top: -1em; margin-bottom: 1em">${textContainer.text['aiAgentReportDescription'] }</p>
      </div>
    </div>
  </div>
</div>

<div class="row-fluid">
  <div class="span12">
    <%-- stacked, with a legend and a visible label on each field, as for other filter forms
         (GRP-7310) --%>
    <form id="aiAgentUsageReportFormId" class="form-horizontal form-filter"
      onsubmit="ajax('../app/UiV2AiAgent.aiAgentUsageReportSubmit', {formIds: 'aiAgentUsageReportFormId'}); return false;">
      <fieldset style="margin:0; padding:0;">
        <legend style="font-size: inherit; font-weight: bold; border: 0; margin-bottom: 8px; width: auto; line-height: inherit;">${textContainer.text['aiAgentReportDays'] }</legend>
        <div class="control-group">
          <label class="control-label" for="aiAgentReportFromId">${textContainer.text['aiAgentReportFrom'] }</label>
          <div class="controls">
            <input type="date" id="aiAgentReportFromId" name="aiAgentReportFrom" style="width:100%; max-width:220px;"
              value="${grouper:escapeHtml(grouperRequestContainer.aiAgentContainer.usageReportFrom)}" />
          </div>
        </div>
        <div class="control-group">
          <label class="control-label" for="aiAgentReportToId">${textContainer.text['aiAgentReportTo'] }</label>
          <div class="controls">
            <input type="date" id="aiAgentReportToId" name="aiAgentReportTo" style="width:100%; max-width:220px;"
              value="${grouper:escapeHtml(grouperRequestContainer.aiAgentContainer.usageReportTo)}" />
          </div>
        </div>
      </fieldset>
      <div class="form-actions">
        <button type="submit" class="btn btn-primary" aria-controls="aiAgentUsageReportResultsId"
          >${textContainer.text['aiAgentReportSubmit'] }</button>
      </div>
    </form>
  </div>
</div>

<div class="row-fluid">
  <div class="span12">
    <div id="aiAgentUsageReportResultsId" role="region" aria-live="polite"
      aria-label="${grouper:escapeHtml(textContainer.text['aiAgentReportResultsLabel'])}">
      <%@ include file="aiAgentUsageReportResults.jsp"%>
    </div>
  </div>
</div>
