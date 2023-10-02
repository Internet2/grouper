<%@ include file="../assetsJsp/commonTaglib.jsp"%>

   <c:set value="${grouperRequestContainer.provisionerConfigurationContainer.guiProvisionerConfiguration}" var="guiProvisionerConfiguration" />

	 <div class="bread-header-container">
       <ul class="breadcrumb">
           <li><a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');">${textContainer.text['myServicesHomeBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
           <li><a href="#" onclick="return guiV2link('operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
           <li><a href="#" onclick="return guiV2link('operation=UiV2ProvisionerConfiguration.viewProvisionerConfigurations');">${textContainer.text['miscellaneousProvisionerConfigurationsBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
           <li class="active">${textContainer.text['miscellaneousProvisionerConfigViewActivityBreadcrumb'] }</li>
       </ul>
       
       <div class="page-header blue-gradient">
       
         <div class="row-fluid">
           <div class="lead span9 pull-left"><h1>${textContainer.text['miscellaneousProvisionerConfigurationsRecentActivityTitle'] }</h1>
            
           </div>
           <div class="span2 pull-right">
             <c:set var="buttonSize" value="btn-medium" />
             <c:set var="buttonBlock" value="btn-block" />
             <%@ include file="provisionerConfigsMoreActionsButtonContents.jsp"%>
           </div>
         </div>
         
        <div class="row-fluid">
          <div class="span12">
            <p style="margin-top: -1em; margin-bottom: 1em">${textContainer.text['miscellaneousProvisionerConfigurationsRecentActivityDescription']}</p>
          </div>
        </div>
                
       </div>
     </div>
     
     
     <div class="row-fluid">
	  <div class="span12">
	   <div id="messages"></div>
         
         <form class="form-inline form-small form-filter" id="provisionerConfigViewActivity">
         	<input type="hidden" name="provisionerConfigId" value="${grouperRequestContainer.provisionerConfigurationContainer.guiProvisionerConfiguration.provisionerConfiguration.configId}" />
         	<input type="hidden" name="provisionerConfigType" value="${grouperRequestContainer.provisionerConfigurationContainer.guiProvisionerConfiguration.provisionerConfiguration['class'].name}" />
            <table class="table table-condensed table-striped">
              <tbody>
              	<tr>
				  <td style="vertical-align: top; white-space: nowrap;"><strong><label>${textContainer.text['provisionerConfigIdLabel']}</label></strong></td>
				    <td>
				     ${grouper:escapeHtml(grouperRequestContainer.provisionerConfigurationContainer.guiProvisionerConfiguration.provisionerConfiguration.configId)}
				    </td>
				</tr>
				
				<tr>
				  <td style="vertical-align: top; white-space: nowrap;"><strong><label for="provisionerConfigObjectTypeId">${textContainer.text['provisionerConfigObjectType']}</label></strong></td>
				    <td>
                       <select name="provisionerConfigObjectType" id="provisionerConfigObjectTypeId" style="width: 30em">
                         <option value=""></option>
                         <option ${grouperRequestContainer.provisionerConfigurationContainer.provisionerConfigObjectType == 'group' ? 'selected="selected"' : '' } value="group">${textContainer.textEscapeXml['provisionerConfigObjectTypeGroup']}</option>
                         <option ${grouperRequestContainer.provisionerConfigurationContainer.provisionerConfigObjectType == 'entity' ? 'selected="selected"' : '' } value="entity">${textContainer.textEscapeXml['provisionerConfigObjectTypeEntity']}</option>
                         <option ${grouperRequestContainer.provisionerConfigurationContainer.provisionerConfigObjectType == 'membership' ? 'selected="selected"' : '' } value="membership">${textContainer.textEscapeXml['provisionerConfigObjectTypeMembership']}</option>
                       </select>
                       <br />
                       <span class="description">${textContainer.text['provisionerConfigObjectTypeHint']}</span>
                     </td>
				</tr>
				
                <tr>
                  <td></td>
                  <td
                    style="white-space: nowrap; padding-top: 2em; padding-bottom: 2em;">
                    <input type="submit" class="btn btn-primary"
                    aria-controls="provisionerConfigSubmitId" id="submitId"
                    value="${textContainer.text['provisionerConfigRunFullSyncFormSubmitButton'] }"
                    onclick="ajax('../app/UiV2ProvisionerConfiguration.viewProvisionerActivitySubmit', {formIds: 'provisionerConfigViewActivity'}); return false;">
                    &nbsp;
                  <a class="btn btn-cancel" role="button"
                          onclick="return guiV2link('operation=UiV2ProvisionerConfiguration.viewProvisionerConfigurations'); return false;"
                          >${textContainer.text['provisionerConfigViewActivityFormCancelButton'] }</a>
                  </td>
                </tr>

              </tbody>
            </table>
            
          </form>
	  	
	  </div>
	</div>
	
	<c:if test="${grouperRequestContainer.provisionerConfigurationContainer.provisionerConfigObjectType == 'group' or grouperRequestContainer.provisionerConfigurationContainer.provisionerConfigObjectType == '' }">
		<%@ include file="provisioningConfigActivityGroup.jsp"%>	
	</c:if>
	<c:if test="${grouperRequestContainer.provisionerConfigurationContainer.provisionerConfigObjectType == 'entity' or grouperRequestContainer.provisionerConfigurationContainer.provisionerConfigObjectType == ''}">
		<%@ include file="provisioningConfigActivityMember.jsp"%>	
	</c:if>
	<c:if test="${grouperRequestContainer.provisionerConfigurationContainer.provisionerConfigObjectType == 'membership' or grouperRequestContainer.provisionerConfigurationContainer.provisionerConfigObjectType == ''}">
		<%@ include file="provisioningConfigActivityMembership.jsp"%>	
	</c:if>
	
	
