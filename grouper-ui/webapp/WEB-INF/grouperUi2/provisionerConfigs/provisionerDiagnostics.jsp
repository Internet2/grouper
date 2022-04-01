<%@ include file="../assetsJsp/commonTaglib.jsp"%>

   <c:set value="${grouperRequestContainer.provisionerConfigurationContainer.guiProvisionerConfiguration}" var="guiProvisionerConfiguration" />

	 <div class="bread-header-container">
       <ul class="breadcrumb">
           <li><a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');">${textContainer.text['myServicesHomeBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
           <li><a href="#" onclick="return guiV2link('operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
           <li><a href="#" onclick="return guiV2link('operation=UiV2ProvisionerConfiguration.viewProvisionerConfigurations');">${textContainer.text['miscellaneousProvisionerConfigurationsBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
           <li class="active">${textContainer.text['miscellaneousGrouperProvisioningDiagnosticsBreadcrumb'] }</li>
       </ul>
       
       <div class="page-header blue-gradient">
       
         <div class="row-fluid">
           <div class="lead span9 pull-left">
           <h1>${textContainer.text['miscellaneousProvisionerConfigurationsDiagnosticsMainDescription'] }</h1>
           
           <h1>
            <small>
              <c:choose>
                <c:when test="${grouperRequestContainer.grouperProvisioningDiagnosticsContainer.progressBean.complete}">
                  ${textContainer.text['grouperProvisioningDiagnosticsSubheading']}
                </c:when>
                <c:otherwise>
                  <i class="fa fa-spinner fa-spin"></i> ${textContainer.text['grouperProvisioningDiagnosticsSubheading']}
                </c:otherwise>
              </c:choose>
             </small>
           </h1>
           
           </div>
           <div class="span2 pull-right">
             <c:set var="buttonSize" value="btn-medium" />
             <c:set var="buttonBlock" value="btn-block" />
             <%@ include file="provisionerConfigsMoreActionsButtonContents.jsp"%>
           </div>
         </div>
                  
       </div>
     </div>
     
     
     <div class="row-fluid">
       <div class="span12">
         ${grouperRequestContainer.grouperProvisioningDiagnosticsContainer.reportFinal}
       </div>
       
     </div>
     
