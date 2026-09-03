<%@ include file="../assetsJsp/commonTaglib.jsp"%>
${grouper:title('miscellaneousUserLifecycleActionsBreadcrumb')}

<div class="bread-header-container">
  <ul class="breadcrumb">
    <li><a href="?operation=UiV2Main.indexMain" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Main.indexMain');">${textContainer.text['myServicesHomeBreadcrumb'] }</a><span
      class="divider"><i class='fa fa-angle-right'></i></span></li>
    <li><a href="?operation=UiV2Main.miscellaneous" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span
      class="divider"><i class='fa fa-angle-right'></i></span></li>
      
      <li><a href="?operation=UiV2UserLifecycle.viewUserLifecycleSummary" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2UserLifecycle.viewUserLifecycleSummary');">${textContainer.text['miscellaneousUserLifecycleBreadcrumb'] }</a><span
      class="divider"><i class='fa fa-angle-right'></i></span></li>
      
    <li class="active">${textContainer.text['miscellaneousUserLifecycleActionsBreadcrumb'] }</li>
  </ul>

  <div class="page-header blue-gradient">

    <div class="row-fluid">
      <div class="lead span8 pull-left">
        <h1 class="grouper-heading-as-h4">${textContainer.text['miscellaneousUserLifecycleActionsMainDescription'] }</h1>
      </div>
      <div class="span3 pull-right">
        <%@ include file="userLifecycleActionsMoreActionsButtonContents.jsp"%>
      </div>
    </div>
  </div>
</div>

<div class="row-fluid">

      <table
        class="table table-hover table-bordered table-striped table-condensed data-table">
        <thead>
          <tr>
            <th>${textContainer.text['entityDataFieldsHeaderConfigId']}</th>
            <th>${textContainer.text['userLifecycleTableNameColumn']}</th>
            <th>${textContainer.text['userLifecycleTableActionTypeColumn']}</th>
            <th>${textContainer.text['entityDataFieldsHeaderActions']}</th>
          </tr>
        </thead>
        <tbody>
         <c:set var="i" value="0" />
         <c:forEach items="${grouperRequestContainer.userLifecycleContainer.guiUserLifecycleActionConfigurations}" var="guiUserLifecycleActionConfig">
              
            <tr>
              <td style="white-space: nowrap;">
                ${guiUserLifecycleActionConfig.userLifecycleActionConfiguration.configId}
              </td>
              <td style="white-space: nowrap;">
                ${guiUserLifecycleActionConfig.userLifecycleActionConfiguration.name}
              </td>
              <td style="white-space: nowrap;">
                ${guiUserLifecycleActionConfig.userLifecycleActionConfiguration.actionType}
              </td>

              <td>
                <div class="btn-group">
                  <a data-toggle="dropdown" href="#"
                    aria-label="${textContainer.text['ariaLabelGuiMoreOptions']}"
                    class="btn btn-mini dropdown-toggle"
                    aria-haspopup="true" aria-expanded="false"
                    role="button"
                    onclick="$('#more-options${i}').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#more-options${i} li').first().focus();return true;});">
                    ${textContainer.text['entityDataFieldsRowActionsButton'] }
                    <span class="caret"></span>
                  </a>
                  <ul class="dropdown-menu dropdown-menu-right"
                    id="more-options${i}">

                    <li><a href="?operation=UiV2UserLifecycle.editUserLifecycleActionConfig&configId=${guiUserLifecycleActionConfig.userLifecycleActionConfiguration.configId}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2UserLifecycle.editUserLifecycleActionConfig&configId=${guiUserLifecycleActionConfig.userLifecycleActionConfiguration.configId}');">${textContainer.text['dataFieldsEditActionsOption'] }</a></li>

                    <li>&nbsp;</li>                                  
                    <li><a href="#" onclick="if (confirm('${textContainer.textEscapeSingleDouble['userLifecycleActionConfigsConfirmDeleteConfig']}')) { return guiV2link('operation=UiV2UserLifecycle.deleteUserLifecycleActionConfig&configId=${guiUserLifecycleActionConfig.userLifecycleActionConfiguration.configId}');}">${textContainer.text['dataFieldsDeleteActionsOption'] }</a></li>
                    
                    
                  </ul>
                </div>
               </td>
              </tr>
              
         </c:forEach>
              
        </tbody>
      </table>

</div>
