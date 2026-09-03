<%@ include file="../assetsJsp/commonTaglib.jsp"%>
${grouper:title('miscellaneousDataProvidersBreadcrumb')}

<div class="bread-header-container">
  <ul class="breadcrumb">
    <li><a href="?operation=UiV2Main.indexMain" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Main.indexMain');">${textContainer.text['myServicesHomeBreadcrumb'] }</a><span
      class="divider"><i class='fa fa-angle-right'></i></span></li>
    <li><a href="?operation=UiV2Main.miscellaneous" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span
      class="divider"><i class='fa fa-angle-right'></i></span></li>
      
      <li><a href="?operation=UiV2EntityDataFields.viewEntityDataFieldsSummary" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2EntityDataFields.viewEntityDataFieldsSummary');">${textContainer.text['miscellaneousEntityDataFieldsBreadcrumb'] }</a><span
      class="divider"><i class='fa fa-angle-right'></i></span></li>
      
    <li class="active">${textContainer.text['miscellaneousDataProvidersBreadcrumb'] }</li>
  </ul>
 
  <div class="page-header blue-gradient">

    <div class="row-fluid">
      <div class="lead span9 pull-left">
        <h1 class="grouper-heading-as-h4">${textContainer.text['miscellaneousDataProvidersMainDescription'] }</h1>
      </div>
      <div class="span3 pull-right">
        <%@ include file="dataProviderConfigsMoreActionsButtonContents.jsp"%>
      </div>
    </div>
  </div>
</div>

<div class="row-fluid">

      <table
        class="table table-hover table-bordered table-striped table-condensed data-table">
        <thead>
          <tr>
            <th>${textContainer.text['entityDataFieldsHeaderActions']}</th>
            <th>${textContainer.text['entityDataFieldsHeaderConfigId']}</th>
            <th>${textContainer.text['dataProvidersHeaderSubjectSourceId']}</th>
            <th>${textContainer.text['dataProvidersHeaderNumberOfQueries']}</th>
            <th>${textContainer.text['dataProvidersHeaderFullSyncDaemon']}</th>
            <th>${textContainer.text['dataProvidersHeaderIncrementalSyncDaemon']}</th>
          </tr>
        </thead>
        <tbody>
         <c:set var="i" value="0" />
         <c:forEach items="${grouperRequestContainer.entityDataFieldsContainer.guiDataProviderConfigurations}" var="guiDataProviderConfiguration">
              
            <tr>
              <td>
                <div class="btn-group">
                  <button type="button" data-toggle="dropdown"
                    aria-label="${textContainer.text['ariaLabelGuiMoreOptions']}"
                    class="btn btn-mini dropdown-toggle"
                    aria-haspopup="true" aria-expanded="false"
                    onclick="$('#more-options${i}').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#more-options${i} li').first().focus();return true;});">
                    ${textContainer.text['entityDataFieldsRowActionsButton'] }
                    <span class="caret"></span>
                  </button>
                  <ul class="dropdown-menu"
                    id="more-options${i}">

                    <li><a href="?operation=UiV2EntityDataFields.editDataProviderConfig&dataProviderConfigId=${guiDataProviderConfiguration.grouperDataProviderConfiguration.configId}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2EntityDataFields.editDataProviderConfig&dataProviderConfigId=${guiDataProviderConfiguration.grouperDataProviderConfiguration.configId}');">${textContainer.text['dataFieldsEditActionsOption'] }</a></li>

                    <li>&nbsp;</li>                                  
                    <li><a href="#" onclick="if (confirm('${textContainer.textEscapeSingleDouble['dataProviderConfigsConfirmDeleteConfig']}')) { return guiV2link('operation=UiV2EntityDataFields.deleteDataProviderConfig&dataProviderConfigId=${guiDataProviderConfiguration.grouperDataProviderConfiguration.configId}');}">${textContainer.text['dataFieldsDeleteActionsOption'] }</a></li>
                    
                    
                  </ul>
                </div>
               </td>

              <td style="white-space: nowrap;">
                ${guiDataProviderConfiguration.grouperDataProviderConfiguration.configId}
              </td>

              <td>
                ${grouper:escapeHtml(guiDataProviderConfiguration.subjectSourceId)}
              </td>

              <td>
                ${guiDataProviderConfiguration.numberOfQueries}
              </td>

              <td>
                <c:choose>
                  <c:when test="${not empty guiDataProviderConfiguration.fullSyncDaemonJobName}">
                    <a href="?operation=UiV2Admin.viewLogs&jobName=${guiDataProviderConfiguration.fullSyncDaemonJobName}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Admin.viewLogs&jobName=${guiDataProviderConfiguration.fullSyncDaemonJobName}');">${textContainer.text['dataProvidersDaemonViewLogs']}</a>
                  </c:when>
                  <c:otherwise>
                    &nbsp;
                  </c:otherwise>
                </c:choose>
              </td>

              <td>
                <c:choose>
                  <c:when test="${not empty guiDataProviderConfiguration.incrementalSyncDaemonJobName}">
                    <a href="?operation=UiV2Admin.viewLogs&jobName=${guiDataProviderConfiguration.incrementalSyncDaemonJobName}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Admin.viewLogs&jobName=${guiDataProviderConfiguration.incrementalSyncDaemonJobName}');">${textContainer.text['dataProvidersDaemonViewLogs']}</a>
                  </c:when>
                  <c:otherwise>
                    &nbsp;
                  </c:otherwise>
                </c:choose>
              </td>
              </tr>
              
         </c:forEach>
              
        </tbody>
      </table>

</div>
