<%@ include file="../assetsJsp/commonTaglib.jsp"%>

<div class="bread-header-container">
  <ul class="breadcrumb">
    <li><a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');">${textContainer.text['myServicesHomeBreadcrumb'] }</a><span
      class="divider"><i class='fa fa-angle-right'></i></span></li>
    <li><a href="#" onclick="return guiV2link('operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span
      class="divider"><i class='fa fa-angle-right'></i></span></li>
    <li class="active">${textContainer.text['miscellaneousEntityDataFieldsBreadcrumb'] }</li>
  </ul>

  <div class="page-header blue-gradient">

    <div class="row-fluid">
      <div class="lead span9 pull-left">
        <h4>${textContainer.text['miscellaneousEntityDataFieldsMainDescription'] }</h4>
      </div>
      <%-- <div class="span2 pull-right">
        <%@ include file="externalSystemsMoreActionsButtonContents.jsp"%>
      </div> --%>
    </div>
  </div>
</div>

<div class="row-fluid">

      <table
        class="table table-hover table-bordered table-striped table-condensed data-table">
        <thead>
          <tr>
            <th>${textContainer.text['entityDataFieldsSummaryHeaderName']}</th>
            <th>${textContainer.text['entityDataFieldsSummaryHeaderNumberOfConfigs']}</th>
          </tr>
        </thead>
        <tbody>
        
          <tr>
              <td style="white-space: nowrap;">
                <a href="#" onclick="return guiV2link('operation=UiV2EntityDataFields.viewPrivacyRealmConfigs');" style="white-space: nowrap;"
                      >${textContainer.text['entityDataFieldsSummaryTablePrivacyRealms'] }</a>
              </td>

              <td style="white-space: nowrap;">
                ${grouperRequestContainer.entityDataFieldsContainer.privacyRealmNumberOfConfigs}
              </td>

              </tr>
        
        
            <tr>
              <td style="white-space: nowrap;">
                <a href="#" onclick="return guiV2link('operation=UiV2EntityDataFields.viewEntityDataFields');" style="white-space: nowrap;"
                      >${textContainer.text['entityDataFieldsSummaryTableDataFields'] }</a>
              </td>

              <td style="white-space: nowrap;">
                ${grouperRequestContainer.entityDataFieldsContainer.dataFieldsNumberOfConfigs}
              </td>

              </tr>
              
              <tr>
              <td style="white-space: nowrap;">
                <a href="#" onclick="return guiV2link('operation=UiV2EntityDataFields.viewEntityDataRows');" style="white-space: nowrap;"
                      >${textContainer.text['entityDataFieldsSummaryTableDataRows'] }</a>
              </td>

              <td style="white-space: nowrap;">
                ${grouperRequestContainer.entityDataFieldsContainer.dataRowsNumberOfConfigs}
              </td>

              </tr>
              
              <tr>
              <td style="white-space: nowrap;">
                <a href="#" onclick="return guiV2link('operation=UiV2EntityDataFields.viewDataProviders');" style="white-space: nowrap;"
                      >${textContainer.text['entityDataFieldsSummaryTableDataProviders'] }</a>
              </td>

              <td style="white-space: nowrap;">
                ${grouperRequestContainer.entityDataFieldsContainer.dataProvidersNumberOfConfigs}
              </td>
              
              </tr>
              
             <tr>
              <td style="white-space: nowrap;">
                <a href="#" onclick="return guiV2link('operation=UiV2EntityDataFields.viewEntityDataProviderQueries');" style="white-space: nowrap;"
                      >${textContainer.text['entityDataFieldsSummaryTableDataProviderQueries'] }</a>
              </td>

              <td style="white-space: nowrap;">
                ${grouperRequestContainer.entityDataFieldsContainer.dataProviderQueriesNumberOfConfigs}
              </td>

             </tr>
             <tr>
              <td style="white-space: nowrap;">
                <a href="#" onclick="return guiV2link('operation=UiV2EntityDataFields.viewEntityDataProviderChangeLogQueries');" style="white-space: nowrap;"
                      >${textContainer.text['entityDataFieldsSummaryTableDataProviderChangeLogQueries'] }</a>
              </td>

              <td style="white-space: nowrap;">
                ${grouperRequestContainer.entityDataFieldsContainer.dataProviderChangeLogQueriesNumberOfConfigs}
              </td>

             </tr>
              
        </tbody>
      </table>

    

</div>
