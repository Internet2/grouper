<%@ include file="../assetsJsp/commonTaglib.jsp"%>

${grouper:title('workflowFormsPageTitle')}

            <div class="bread-header-container">
              <ul class="breadcrumb">
                <li><a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');">${textContainer.text['myServicesHomeBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                <li><a href="#" onclick="return guiV2link('operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                <li class="active">${textContainer.text['workflowMiscFormsLink'] }</li>
              </ul>
              
              <div class="page-header blue-gradient">
                <h1>${textContainer.text['workflowMiscFormsTitle'] }</h1>
              </div>

            </div>
            
            <div class="row-fluid">
              <div class="span12">
                <div class="row-fluid">
                  <div class="span1">
                    <a href="#" onclick="return guiV2link('operation=UiV2GrouperWorkflow.formsUserSubmitted');" style="white-space: nowrap;"
                      >${textContainer.text['workflowMiscMyFormsLink'] }</a>
                      
                    <br /><br />
                    <a href="#" onclick="return guiV2link('operation=UiV2GrouperWorkflow.formsWaitingForApproval');" style="white-space: nowrap;"
                      >${textContainer.text['workflowMiscFormsWaitingForApprovalLink'] }</a>
                      
                  </div>
                </div>
              </div>
            </div>


