<%@ include file="../assetsJsp/commonTaglib.jsp"%>
   <c:set  value="${grouperRequestContainer.provisionerConfigurationContainer.guiProvisionerConfiguration}" var="guiProvisionerConfiguration"/>
	 <div class="bread-header-container">
       <ul class="breadcrumb">
           <li><a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');">${textContainer.text['myServicesHomeBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
           <li><a href="#" onclick="return guiV2link('operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
           <li><a href="#" onclick="return guiV2link('operation=UiV2ProvisionerConfiguration.viewProvisionerConfigurations');">${textContainer.text['miscellaneousProvisionerConfigurationsBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
           <li class="active">${textContainer.text['miscellaneousProvisionerConfigAddBreadcrumb'] }</li>
       </ul>
       
       <div class="page-header blue-gradient">
       
         <div class="row-fluid">
           <div class="lead span9 pull-left">
            <h1>${textContainer.text['miscellaneousProvisionerConfigurationsMainDescription'] }</h1>
           </div>
           <div class="span2 pull-right">
             <c:set var="buttonSize" value="btn-medium" />
             <c:set var="buttonBlock" value="btn-block" />
             <%@ include file="provisionerConfigsMoreActionsButtonContents.jsp"%>
           </div>
         </div>
         
         <div class="row-fluid">
          <div class="span12">
            <p style="margin-top: -1em; margin-bottom: 1em">
              ${guiProvisionerConfiguration.provisionerConfiguration.description}
            </p>
            <p style="margin-top: -1em; margin-bottom: 1em">
              ${guiProvisionerConfiguration.provisionerConfiguration.documentation}
            </p>
          </div>
        </div>
        
        <c:if test="${grouperRequestContainer.provisionerConfigurationContainer.provisionerStartWith != null}">
          <div class="row-fluid">
            <div class="span12">
              <p style="margin-top: 1em; margin-bottom: 1em">
                ${grouperRequestContainer.provisionerConfigurationContainer.provisionerStartWith.startWithDescription}
              </p>
              <p style="margin-top: -1em; margin-bottom: 1em">
                ${grouperRequestContainer.provisionerConfigurationContainer.provisionerStartWith.startWithDocumentation}
              </p>
            </div>
          </div>
        </c:if>
        
         
       </div>
     </div>
     
     
     <div class="row-fluid">
	  <div class="span12">
	   <div id="messages"></div>
         
         <form class="form-inline form-small form-filter" id="provisionerConfigDetails">
         	<input type="hidden" name="previousProvisionerConfigId" value="${grouperRequestContainer.provisionerConfigurationContainer.guiProvisionerConfiguration.provisionerConfiguration.configId}" />
         	<input type="hidden" name="previousProvisionerConfigType" value="${grouperRequestContainer.provisionerConfigurationContainer.guiProvisionerConfiguration.provisionerConfiguration['class'].name}" />
         	<input type="hidden" name="previousProvisionerStartWithClass" value="${grouperRequestContainer.provisionerConfigurationContainer.provisionerStartWith['class'].name}" />
            <table class="table table-condensed table-striped">
              <tbody>
                <%@ include file="provisionerConfigAddHelper.jsp" %>
                <tr>
                  <td>
                    <input type="hidden" name="mode" value="add">
                  </td>
                  <td></td>
                  
                    <td
                      style="white-space: nowrap; padding-top: 2em; padding-bottom: 2em;">
                    <input type="submit" class="btn btn-primary"
                      aria-controls="provisionerConfigSubmitId" id="submitId"
                      value="${textContainer.text['provisionerConfigAddFormSubmitButton'] }"
                      onclick="ajax('../app/UiV2ProvisionerConfiguration.addProvisionerConfigurationSubmit', {formIds: 'provisionerConfigDetails'}); return false;">
                      &nbsp;
                    <a class="btn btn-cancel" role="button"
                        onclick="return guiV2link('operation=UiV2ProvisionerConfiguration.viewProvisionerConfigurations'); return false;"
                    >${textContainer.text['provisionerConfigAddFormCancelButton'] }</a>
                    </td>
                  
                  
                </tr>

              </tbody>
            </table>
            
          </form>
	  	
	  </div>
	</div>
