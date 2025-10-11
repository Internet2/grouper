<%@ include file="../assetsJsp/commonTaglib.jsp"%>

<div class="bread-header-container">
  <ul class="breadcrumb">
    <li><a href="?operation=UiV2Main.indexMain" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Main.indexMain');">${textContainer.text['myServicesHomeBreadcrumb'] }</a><span
      class="divider"><i class='fa fa-angle-right'></i></span></li>
    <li><a href="?operation=UiV2Main.miscellaneous" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span
      class="divider"><i class='fa fa-angle-right'></i></span></li>
    <li class="active">${textContainer.text['miscellaneousUserLifecycleBreadcrumb'] }</li>
  </ul>

  <div class="page-header blue-gradient">

    <div class="row-fluid">
      <div class="lead span9 pull-left">
        <h4>${textContainer.text['miscellaneousUserLifecycleMainDescription'] }</h4>
      </div>
    </div>
  </div>
</div>

<div class="row-fluid">

      <table
        class="table table-hover table-bordered table-striped table-condensed data-table">
        <thead>
          <tr>
            <th>${textContainer.text['userLifecycleSummaryTableHeaderName']}</th>
            <th>${textContainer.text['userLifecycleSummaryTableHeaderNumberOfConfigs']}</th>
          </tr>
        </thead>
        <tbody>
        
          <tr>
              <td style="white-space: nowrap;">
                <a href="?operation=UiV2UserLifecycle.viewUserLifecycleEvents" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2UserLifecycle.viewUserLifecycleEvents');" style="white-space: nowrap;"
                      >${textContainer.text['userLifecycleSummaryTableUserLifecycleEvents'] }</a>
              </td>

              <td style="white-space: nowrap;">
                ${grouperRequestContainer.userLifecycleContainer.userLifecycleEventCount}
              </td>

              </tr>
        
        
            <tr>
              <td style="white-space: nowrap;">
                <a href="?operation=UiV2UserLifecycle.viewUserLifecycleActions" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2UserLifecycle.viewUserLifecycleActions');" style="white-space: nowrap;"
                      >${textContainer.text['userLifecycleSummaryTableUserLifecycleActions'] }</a>
              </td>

              <td style="white-space: nowrap;">
                ${grouperRequestContainer.userLifecycleContainer.userLifecycleActionCount}
              </td>

              </tr>
              
              <tr>
              <td style="white-space: nowrap;">
                <a href="?operation=UiV2UserLifecycle.viewUserLifecyclePolicies" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2UserLifecycle.viewUserLifecyclePolicies');" style="white-space: nowrap;"
                      >${textContainer.text['userLifecycleSummaryTableUserLifecyclePolicies'] }</a>
              </td>

              <td style="white-space: nowrap;">
                ${grouperRequestContainer.userLifecycleContainer.userLifecyclePolicyCount}
              </td>

              </tr>
              
               <tr>
              <td style="white-space: nowrap;">
                <a href="?operation=UiV2UserLifecycle.viewUserLifecyclePolicyParts" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2UserLifecycle.viewUserLifecyclePolicyParts');" style="white-space: nowrap;"
                      >${textContainer.text['userLifecycleSummaryTableUserLifecyclePolicyParts'] }</a>
              </td>

              <td style="white-space: nowrap;">
                ${grouperRequestContainer.userLifecycleContainer.userLifecyclePolicyPartCount}
              </td>

              </tr>
              
        </tbody>
      </table>

    

</div>
