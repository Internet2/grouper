<%@ include file="../assetsJsp/commonTaglib.jsp"%>

   <div class="bread-header-container">
     <ul class="breadcrumb">
       <li><a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');">${textContainer.text['myServicesHomeBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
       <li><a href="#" onclick="return guiV2link('operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
       <li><a href="#" onclick="return guiV2link('operation=UiV2GrouperWorkflow.forms');">${textContainer.text['workflowMiscFormsLink'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
       <li class="active">${textContainer.text['workflowMiscFormsWaitingForApprovalTitle'] }</li>
     </ul>
     
     <div class="page-header blue-gradient">
       <h1>${textContainer.text['workflowMiscFormsInstanceDetails'] }</h1>
     </div>
              
   </div>
   
   <c:set  value="${grouperRequestContainer.workflowContainer.workflowInstance}" var="instance"/>
   <form class="form-inline form-small form-filter" id="approveWorkflowId">
   <div class="row-fluid">
     <div class="span12">
     
       <table class="table table-condensed table-striped">
        <tbody>
        
        <tr>
          <td colspan="2">
            ${grouperRequestContainer.workflowContainer.htmlForm}
          </td>
        </tr>
        
        <tr>
        <td colspan="2">
        
          <input type="submit" class="btn btn-primary"
            aria-controls="workflowConfigSubmitId" id="submitId"
            value="${textContainer.text['workflowApproveWorkflowButton'] }"
            onclick="ajax('../app/UiV2GrouperWorkflow.workflowApprove?attributeAssignId=${instance.attributeAssignId}', {formIds: 'approveWorkflowId'}); return false;">
                          &nbsp;
                          
          <input type="submit" class="btn btn-primary"
            aria-controls="workflowConfigSubmitId" id="submitId"
            value="${textContainer.text['workflowDisapproveWorkflowButton'] }"
            onclick="ajax('../app/UiV2GrouperWorkflow.workflowDisapprove?attributeAssignId=${instance.attributeAssignId}', {formIds: 'approveWorkflowId'}); return false;">
                          &nbsp; 
          <a class="btn btn-cancel" role="button"
                          onclick="return guiV2link('operation=UiV2GrouperWorkflow.formsWaitingForApproval'); return false;"
                          >${textContainer.text['workflowCancelWorkflowButton'] }</a>
        
        </td>
        </tr>
        
      </tbody>
    </table>
       
     </div>
   </div>
   </form>