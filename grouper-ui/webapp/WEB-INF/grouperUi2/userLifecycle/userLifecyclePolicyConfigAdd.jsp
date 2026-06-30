<%@ include file="../assetsJsp/commonTaglib.jsp"%>
${grouper:title('miscellaneousUserLifecyclePoliciesAddBreadcrumb')}

   <div class="bread-header-container">
       <ul class="breadcrumb">
          <li><a href="?operation=UiV2Main.indexMain" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Main.indexMain');">${textContainer.text['myServicesHomeBreadcrumb'] }</a><span
            class="divider"><i class='fa fa-angle-right'></i></span></li>
          <li><a href="?operation=UiV2Main.miscellaneous" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span
            class="divider"><i class='fa fa-angle-right'></i></span></li>
            
            <li><a href="?operation=UiV2UserLifecycle.viewUserLifecycleSummary" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2UserLifecycle.viewUserLifecycleSummary');">${textContainer.text['miscellaneousUserLifecycleBreadcrumb'] }</a><span
      class="divider"><i class='fa fa-angle-right'></i></span></li>
            
            <li><a href="?operation=UiV2UserLifecycle.viewUserLifecyclePolicies" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2UserLifecycle.viewUserLifecyclePolicies');">${textContainer.text['miscellaneousUserLifecyclePoliciesBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
            
            <li class="active">${textContainer.text['miscellaneousUserLifecyclePoliciesAddBreadcrumb'] }</li> 
        </ul>
       
       <div class="page-header blue-gradient">
       
         <div class="row-fluid">
           <div class="lead span9 pull-left"><h4>${textContainer.text['miscellaneousUserLifecyclePoliciesMainDescription'] }</h4></div>
           <div class="span3 pull-right">
             <%@ include file="userLifecyclePoliciesMoreActionsButtonContents.jsp"%>
           </div>
         </div>
       </div>
     </div>
     
     
     <div class="row-fluid">
    <div class="span12">
     <div id="messages"></div>
         
         <form class="form-inline form-small form-filter" id="userLifecyclePoliciesConfigDetails">
            <table class="table table-condensed table-striped">
              <tbody>
                <%@ include file="userLifecyclePolicyConfigAddHelper.jsp" %>
                <tr>
                  <td>
                    <input type="hidden" name="mode" value="add">
                  </td>
                  <td></td>
                  <td
                    style="white-space: nowrap; padding-top: 2em; padding-bottom: 2em;">
                    <input type="submit" class="btn btn-primary"
                    aria-controls="userLifecyclePoliciesConfigDetails" id="submitId"
                    value="${textContainer.text['dataFieldConfigAddFormSubmitButton'] }"
                    onclick="ajax('../app/UiV2UserLifecycle.addUserLifecyclePolicyConfigSubmit', {formIds: 'userLifecyclePoliciesConfigDetails'}); return false;">
                    &nbsp;
                  <a class="btn btn-cancel" role="button"
                          onclick="return guiV2link('operation=UiV2UserLifecycle.viewUserLifecyclePolicies'); return false;"
                          >${textContainer.text['dataFieldConfigAddFormCancelButton'] }</a>
                  </td>
                </tr>

              </tbody>
            </table>
            
          </form>
      
    </div>
  </div>
