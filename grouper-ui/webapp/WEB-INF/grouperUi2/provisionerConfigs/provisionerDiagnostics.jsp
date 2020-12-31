<%@ include file="../assetsJsp/commonTaglib.jsp"%>

	 <div class="bread-header-container">
       <ul class="breadcrumb">
           <li><a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');">${textContainer.text['myServicesHomeBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
           <li><a href="#" onclick="return guiV2link('operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
           <li><a href="#" onclick="return guiV2link('operation=UiV2ProvisionerConfiguration.viewProvisionerConfigurations');">${textContainer.text['miscellaneousProvisionerConfigurationsBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
           <li class="active">${textContainer.text['miscellaneousProvisionerDiagnosticsBreadcrumb'] }</li>
       </ul>
       
       <div class="page-header blue-gradient">
       
         <div class="row-fluid">
           <div class="lead span9 pull-left">
           <h4>${textContainer.text['miscellaneousProvisionerConfigurationsDiagnosticsMainDescription'] }</h4>
           
           <h1>
           	<br />
            <small>
              <c:choose>
                <c:when test="${grouperRequestContainer.provisionerDiagnosticsContainer.progressBean.complete}">
                  ${textContainer.text['provisionerDiagnosticsSubheading']}
                </c:when>
                <c:otherwise>
                  <i class="fa fa-spinner fa-spin"></i> ${textContainer.text['provisionerDiagnosticsSubheading']}
                </c:otherwise>
              </c:choose>
             </small>
           </h1>
           
           </div>
           <div class="span2 pull-right">
             <%@ include file="provisionerConfigsMoreActionsButtonContents.jsp"%>
           </div>
         </div>
                  
       </div>
     </div>
     
     
     <div class="row-fluid">
       <div class="span12">
         <p class="lead">${textContainer.text['provisionerDiagnosticsPageSummary']}</p>
         ${grouperRequestContainer.provisionerDiagnosticsContainer.report}
       </div>
       
     </div>
     
